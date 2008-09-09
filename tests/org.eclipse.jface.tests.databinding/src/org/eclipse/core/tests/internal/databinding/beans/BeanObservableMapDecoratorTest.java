/*******************************************************************************
 * Copyright (c) 2007 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 245183)
 ******************************************************************************/

package org.eclipse.core.tests.internal.databinding.beans;

import java.beans.PropertyDescriptor;

import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.map.IObservableMap;
import org.eclipse.core.databinding.observable.map.WritableMap;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.core.internal.databinding.beans.BeanObservableMapDecorator;
import org.eclipse.core.internal.databinding.beans.JavaBeanPropertyObservableMap;
import org.eclipse.jface.tests.databinding.AbstractDefaultRealmTestCase;

/**
 * @since 3.3
 */
public class BeanObservableMapDecoratorTest extends
		AbstractDefaultRealmTestCase {
	private PropertyDescriptor propertyDescriptor;
	private JavaBeanPropertyObservableMap observableMap;
	private BeanObservableMapDecorator decorator;

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();

		Bean bean = new Bean();
		propertyDescriptor = new PropertyDescriptor("map", Bean.class,
				"getMap", "setMap");
		observableMap = new JavaBeanPropertyObservableMap(Realm.getDefault(),
				bean, propertyDescriptor);
		decorator = new BeanObservableMapDecorator(observableMap,
				observableMap, propertyDescriptor);
	}

	public void testGetDelegate() throws Exception {
		assertEquals(observableMap, decorator.getDelegate());
	}

	public void testGetObserved() throws Exception {
		assertEquals(observableMap, decorator.getObserved());
	}

	public void testGetPropertyDescriptor() throws Exception {
		assertEquals(propertyDescriptor, decorator.getPropertyDescriptor());
	}

	public void testEquals_IdentityCheckShortcut() {
		IObservableMap delegate = new WritableMap() {
			public boolean equals(Object obj) {
				fail("ObservableList.equals() should return true instead of delegating to wrappedList when this == obj");
				return false;
			}
		};
		decorator = new BeanObservableMapDecorator(delegate, new WritableValue(
				new Bean(), Object.class), propertyDescriptor);
		assertTrue(decorator.equals(decorator));
	}
}
