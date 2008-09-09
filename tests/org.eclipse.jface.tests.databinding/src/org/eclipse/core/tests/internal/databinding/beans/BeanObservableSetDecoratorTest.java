/*******************************************************************************
 * Copyright (c) 2007 Brad Reynolds and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Brad Reynolds - initial API and implementation
 *     Matthew Hall - bug 245183
 ******************************************************************************/

package org.eclipse.core.tests.internal.databinding.beans;

import java.beans.PropertyDescriptor;

import org.eclipse.core.databinding.observable.set.IObservableSet;
import org.eclipse.core.databinding.observable.set.WritableSet;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.core.internal.databinding.beans.BeanObservableSetDecorator;
import org.eclipse.core.internal.databinding.beans.JavaBeanObservableSet;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.tests.databinding.AbstractDefaultRealmTestCase;
import org.eclipse.swt.widgets.Display;

/**
 * @since 3.3
 */
public class BeanObservableSetDecoratorTest extends AbstractDefaultRealmTestCase {
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

	public void testEquals_IdentityCheckShortcut() {
		IObservableSet delegate = new WritableSet() {
			public boolean equals(Object obj) {
				fail("ObservableList.equals() should return true instead of delegating to wrappedList when this == obj");
				return false;
			}
		};
		decorator = new BeanObservableSetDecorator(delegate, new WritableValue(
				new Bean(), Object.class), propertyDescriptor);
		assertTrue(decorator.equals(decorator));
	}
}
