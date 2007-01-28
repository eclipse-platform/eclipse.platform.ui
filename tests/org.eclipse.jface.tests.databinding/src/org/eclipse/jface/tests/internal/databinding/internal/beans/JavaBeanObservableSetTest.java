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

import org.eclipse.core.internal.databinding.internal.beans.JavaBeanObservableSet;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.swt.widgets.Display;

/**
 * @since 3.3
 */
public class JavaBeanObservableSetTest extends TestCase {
	private JavaBeanObservableSet observableSet;
	private Bean model;
	private PropertyDescriptor propertyDescriptor;

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		model = new Bean();
		propertyDescriptor = new PropertyDescriptor("set", Bean.class);

		observableSet = new JavaBeanObservableSet(SWTObservables
				.getRealm(Display.getDefault()), model, propertyDescriptor,
				Bean.class);
	}

	public void testGetObserved() throws Exception {
		assertEquals(model, observableSet.getObserved());
	}

	public void testGetPropertyDescriptor() throws Exception {
		assertEquals(propertyDescriptor, observableSet.getPropertyDescriptor());
	}
	
	public void testGetElementType() throws Exception {
		assertEquals(Bean.class, observableSet.getElementType());
	}
}
