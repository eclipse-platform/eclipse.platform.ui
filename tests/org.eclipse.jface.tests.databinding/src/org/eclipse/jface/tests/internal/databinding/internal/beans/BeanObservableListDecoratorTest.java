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

import junit.framework.TestCase;

import org.eclipse.core.internal.databinding.internal.beans.BeanObservableListDecorator;
import org.eclipse.core.internal.databinding.internal.beans.JavaBeanObservableList;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.swt.widgets.Display;

/**
 * @since 3.3
 */
public class BeanObservableListDecoratorTest extends TestCase {
	private PropertyDescriptor propertyDescriptor;
	private JavaBeanObservableList observableList;
	private BeanObservableListDecorator decorator;

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		
		Bean bean = new Bean();
		propertyDescriptor = new PropertyDescriptor(
				"list", Bean.class);
		observableList = new JavaBeanObservableList(
				SWTObservables.getRealm(Display.getDefault()), bean,
				propertyDescriptor, Bean.class);
		decorator = new BeanObservableListDecorator(observableList, observableList, propertyDescriptor);
	}

	public void testGetDelegate() throws Exception {
		assertEquals(observableList, decorator.getDelegate());
	}

	public void testGetObserved() throws Exception {
		assertEquals(observableList, decorator.getObserved());
	}

	public void testGetPropertyDescriptor() throws Exception {
		assertEquals(propertyDescriptor, decorator.getPropertyDescriptor());
	}
}
