/*******************************************************************************
 * Copyright (c) 2007, 2009 Brad Reynolds and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Brad Reynolds - initial API and implementation
 *     Matthew Hall - bug 246625, 194734
 ******************************************************************************/

package org.eclipse.core.tests.internal.databinding.beans;

import java.beans.PropertyDescriptor;

import org.eclipse.core.databinding.beans.BeansObservables;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.internal.databinding.beans.BeanObservableValueDecorator;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.tests.databinding.AbstractDefaultRealmTestCase;
import org.eclipse.swt.widgets.Display;

/**
 * @since 3.3
 */
public class BeanObservableValueDecoratorTest extends AbstractDefaultRealmTestCase {
	private Bean bean;
	private IObservableValue observableValue;
	private BeanObservableValueDecorator decorator;
	private PropertyDescriptor propertyDescriptor;
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		bean = new Bean();
		propertyDescriptor = new PropertyDescriptor("value", Bean.class);
		observableValue = BeansObservables.observeValue(SWTObservables
				.getRealm(Display.getDefault()), bean, "value");
		decorator = new BeanObservableValueDecorator(observableValue,
				propertyDescriptor);
	}

	public void testGetDelegate() throws Exception {
		assertSame(observableValue, decorator.getDecorated());
	}
	
	public void testGetObserved() throws Exception {
		assertSame(bean, decorator.getObserved());
	}

	public void testGetPropertyDescriptor() throws Exception {
		assertSame(propertyDescriptor, decorator.getPropertyDescriptor());
	}
}
