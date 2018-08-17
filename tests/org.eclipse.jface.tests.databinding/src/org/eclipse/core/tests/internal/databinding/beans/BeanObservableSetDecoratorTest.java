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
import org.eclipse.core.databinding.observable.set.IObservableSet;
import org.eclipse.core.internal.databinding.beans.BeanObservableSetDecorator;
import org.eclipse.jface.databinding.swt.DisplayRealm;
import org.eclipse.swt.widgets.Display;
import org.junit.Before;
import org.junit.Test;

/**
 * @since 3.3
 */
public class BeanObservableSetDecoratorTest {
	private PropertyDescriptor propertyDescriptor;
	private IObservableSet observableSet;
	private BeanObservableSetDecorator decorator;
	private Bean bean;

	@Before
	public void setUp() throws Exception {

		bean = new Bean();
		propertyDescriptor = new PropertyDescriptor("set", Bean.class);
		observableSet = BeansObservables.observeSet(DisplayRealm
				.getRealm(Display.getDefault()), bean, "set");
		decorator = new BeanObservableSetDecorator(observableSet,
				propertyDescriptor);
	}

	@Test
	public void testGetDecorated() throws Exception {
		assertSame(observableSet, decorator.getDecorated());
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
