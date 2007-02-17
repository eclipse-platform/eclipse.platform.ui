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

package org.eclipse.jface.tests.internal.databinding.internal.beans;

import java.beans.PropertyDescriptor;
import java.util.Arrays;

import org.eclipse.core.databinding.observable.list.IListChangeListener;
import org.eclipse.core.databinding.observable.list.ListChangeEvent;
import org.eclipse.core.internal.databinding.internal.beans.JavaBeanObservableList;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.tests.databinding.AbstractDefaultRealmTestCase;
import org.eclipse.swt.widgets.Display;

/**
 * @since 3.3
 */
public class JavaBeanObservableListTest extends AbstractDefaultRealmTestCase {
	private JavaBeanObservableList observableList;
	private PropertyDescriptor propertyDescriptor;
	private Bean bean;
	private String propertyName;
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		
		propertyName = "list";
		propertyDescriptor = new PropertyDescriptor(propertyName, Bean.class);
		bean = new Bean();
		
		observableList = new JavaBeanObservableList(SWTObservables
				.getRealm(Display.getDefault()), bean, propertyDescriptor,
				Bean.class);
	}

	public void testGetObserved() throws Exception {
		assertEquals(bean, observableList.getObserved());
	}

	public void testGetPropertyDescriptor() throws Exception {
		assertEquals(propertyDescriptor, observableList.getPropertyDescriptor());
	}
	
	public void testRegistersListenerAfterFirstListenerIsAdded() throws Exception {
		assertFalse(bean.changeSupport.hasListeners(propertyName));
		observableList.addListChangeListener(new ListChangeListener());
		assertTrue(bean.changeSupport.hasListeners(propertyName));
	}
    
    public void testRemovesListenerAfterLastListenerIsRemoved() throws Exception {
    	ListChangeListener listener = new ListChangeListener();
		observableList.addListChangeListener(listener);
		
		assertTrue(bean.changeSupport.hasListeners(propertyName));
		observableList.removeListChangeListener(listener);
		assertFalse(bean.changeSupport.hasListeners(propertyName));
	}
    
    public void testFiresListChangeEvents() throws Exception {
    	ListChangeListener listener = new ListChangeListener();
    	observableList.addListChangeListener(listener);
    	
    	assertEquals(0, listener.count);
    	bean.setList(Arrays.asList(new String[] {"value"}));
    	assertEquals(1, listener.count);
	}
    
    static class ListChangeListener implements IListChangeListener {
    	int count;
		public void handleListChange(ListChangeEvent event) {
			count++;
		}
    }
}
