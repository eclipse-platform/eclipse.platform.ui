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

import org.eclipse.core.internal.databinding.internal.beans.JavaBeanObservableList;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.swt.widgets.Display;

/**
 * @since 3.3
 */
public class JavaBeanObservableListTest extends TestCase {
	private JavaBeanObservableList observableList;
	private PropertyDescriptor propertyDescriptor;
	private Bean bean;
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		propertyDescriptor = new PropertyDescriptor("list", Bean.class);
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
}
