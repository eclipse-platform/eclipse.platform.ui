/*******************************************************************************
 * Copyright (c) 2007, 2009 Brad Reynolds and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Brad Reynolds - initial API and implementation
 *     Matthew Hall - bug 246625, 194734
 ******************************************************************************/

package org.eclipse.core.tests.internal.databinding.beans;

import static org.junit.Assert.assertSame;

import java.beans.PropertyDescriptor;

import org.eclipse.core.databinding.beans.BeansObservables;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.internal.databinding.beans.BeanObservableValueDecorator;
import org.eclipse.jface.databinding.swt.DisplayRealm;
import org.eclipse.jface.tests.databinding.AbstractDefaultRealmTestCase;
import org.eclipse.swt.widgets.Display;
import org.junit.Before;
import org.junit.Test;

/**
 * @since 3.3
 */
public class BeanObservableValueDecoratorTest extends AbstractDefaultRealmTestCase {
	private Bean bean;
	private IObservableValue observableValue;
	private BeanObservableValueDecorator decorator;
	private PropertyDescriptor propertyDescriptor;

	@Before
	@Override
	public void setUp() throws Exception {
		super.setUp();

		bean = new Bean();
		propertyDescriptor = new PropertyDescriptor("value", Bean.class);
		observableValue = BeansObservables.observeValue(DisplayRealm
				.getRealm(Display.getDefault()), bean, "value");
		decorator = new BeanObservableValueDecorator(observableValue,
				propertyDescriptor);
	}

	@Test
	public void testGetDelegate() throws Exception {
		assertSame(observableValue, decorator.getDecorated());
	}

	@Test
	public void testGetObserved() throws Exception {
		assertSame(bean, decorator.getObserved());
	}

	@Test
	public void testGetPropertyDescriptor() throws Exception {
		assertSame(propertyDescriptor, decorator.getPropertyDescriptor());
	}
}
