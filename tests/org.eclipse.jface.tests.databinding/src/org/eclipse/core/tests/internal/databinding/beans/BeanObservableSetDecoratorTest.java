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

package org.eclipse.core.tests.internal.databinding.beans;

import java.beans.PropertyDescriptor;

import junit.framework.TestCase;

import org.eclipse.core.internal.databinding.internal.beans.BeanObservableSetDecorator;
import org.eclipse.core.internal.databinding.internal.beans.JavaBeanObservableSet;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.swt.widgets.Display;

/**
 * @since 3.3
 */
public class BeanObservableSetDecoratorTest extends TestCase {
	private PropertyDescriptor propertyDescriptor;
	private JavaBeanObservableSet observableSet;
	private BeanObservableSetDecorator decorator;

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();

		Bean bean = new Bean();
		propertyDescriptor = new PropertyDescriptor("set",
				Bean.class);
		observableSet = new JavaBeanObservableSet(
				SWTObservables.getRealm(Display.getDefault()), bean,
				propertyDescriptor, String.class);
		decorator = new BeanObservableSetDecorator(
				observableSet, observableSet, propertyDescriptor);
	}

	public void testGetDelegate() throws Exception {
		assertEquals(observableSet, decorator.getDelegate());
	}

	public void testGetObserved() throws Exception {
		assertEquals(observableSet, decorator.getObserved());
	}

	public void testGetPropertyDescriptor() throws Exception {
		assertEquals(propertyDescriptor, decorator.getPropertyDescriptor());
	}
}
