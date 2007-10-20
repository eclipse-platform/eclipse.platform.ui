/*******************************************************************************
 * Copyright (c) 2007 Brad Reynolds and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Brad Reynolds - initial API and implementation
 ******************************************************************************/

package org.eclipse.core.tests.internal.databinding.internal.beans;

import java.beans.PropertyDescriptor;
import java.util.Arrays;
import java.util.HashSet;

import junit.framework.TestCase;

import org.eclipse.core.databinding.observable.set.ISetChangeListener;
import org.eclipse.core.databinding.observable.set.SetChangeEvent;
import org.eclipse.core.internal.databinding.internal.beans.JavaBeanObservableSet;
import org.eclipse.jface.databinding.conformance.util.ChangeEventTracker;
import org.eclipse.jface.databinding.conformance.util.CurrentRealm;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.swt.widgets.Display;

/**
 * @since 3.3
 */
public class JavaBeanObservableSetTest extends TestCase {
	private JavaBeanObservableSet observableSet;
	private Bean bean;
	private PropertyDescriptor propertyDescriptor;
	private String propertyName;
	private SetChangeListener listener;

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		bean = new Bean();
		propertyName = "set";
		propertyDescriptor = new PropertyDescriptor(propertyName, Bean.class);

		observableSet = new JavaBeanObservableSet(SWTObservables
				.getRealm(Display.getDefault()), bean, propertyDescriptor,
				Bean.class);
		listener = new SetChangeListener();
	}

	public void testGetObserved() throws Exception {
		assertEquals(bean, observableSet.getObserved());
	}

	public void testGetPropertyDescriptor() throws Exception {
		assertEquals(propertyDescriptor, observableSet.getPropertyDescriptor());
	}
	
	public void testGetElementType() throws Exception {
		assertEquals(Bean.class, observableSet.getElementType());
	}
	
	public void testRegistersListenerAfterFirstListenerIsAdded() throws Exception {
		assertFalse(bean.changeSupport.hasListeners(propertyName));
		observableSet.addSetChangeListener(new SetChangeListener());
		assertTrue(bean.changeSupport.hasListeners(propertyName));
	}
    
    public void testRemovesListenerAfterLastListenerIsRemoved() throws Exception {
		observableSet.addSetChangeListener(listener);
		
		assertTrue(bean.changeSupport.hasListeners(propertyName));
		observableSet.removeSetChangeListener(listener);
		assertFalse(bean.changeSupport.hasListeners(propertyName));
	}
	
	public void testFiresChangeEvents() throws Exception {
		observableSet.addSetChangeListener(listener);
		assertEquals(0, listener.count);
		bean.setSet(new HashSet(Arrays.asList(new String[] {"1"})));
		assertEquals(1, listener.count);
	}

	public void testConstructor_RegisterListeners() throws Exception {
		bean = new Bean();

		observableSet = new JavaBeanObservableSet(new CurrentRealm(true), bean,
				propertyDescriptor, Bean.class);
		assertFalse(bean.hasListeners(propertyName));
		ChangeEventTracker.observe(observableSet);
		assertTrue(bean.hasListeners(propertyName));
	}

	public void testConstructore_SkipsRegisterListeners() throws Exception {
		bean = new Bean();

		observableSet = new JavaBeanObservableSet(new CurrentRealm(true), bean,
				propertyDescriptor, Bean.class, false);
		assertFalse(bean.hasListeners(propertyName));
		ChangeEventTracker.observe(observableSet);
		assertFalse(bean.hasListeners(propertyName));
	}
	
	static class SetChangeListener implements ISetChangeListener {
		int count;
		public void handleSetChange(SetChangeEvent event) {
			count++;
		}
	}
}
