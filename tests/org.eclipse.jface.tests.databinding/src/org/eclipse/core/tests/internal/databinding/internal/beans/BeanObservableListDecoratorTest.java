/*******************************************************************************
 * Copyright (c) 2007 Brad Reynolds and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Brad Reynolds - initial API and implementation
 *     Matthew Hall - bugs 208858, 213145
 ******************************************************************************/

package org.eclipse.core.tests.internal.databinding.internal.beans;

import java.beans.PropertyDescriptor;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.IObservableCollection;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.core.internal.databinding.internal.beans.BeanObservableListDecorator;
import org.eclipse.core.internal.databinding.internal.beans.JavaBeanObservableList;
import org.eclipse.jface.databinding.conformance.MutableObservableListContractTest;
import org.eclipse.jface.databinding.conformance.delegate.AbstractObservableCollectionContractDelegate;
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
				"list", Bean.class,"getList","setList");
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

	public static Test suite() {
		TestSuite suite = new TestSuite(BeanObservableListDecoratorTest.class.getName());
		suite.addTestSuite(BeanObservableListDecoratorTest.class);
		suite.addTest(MutableObservableListContractTest.suite(new Delegate()));
		return suite;
	}

	static class Delegate extends AbstractObservableCollectionContractDelegate {
		public IObservableCollection createObservableCollection(Realm realm,
				int elementCount) {
			final WritableList delegate = new WritableList(realm);
			for (int i = 0; i < elementCount; i++)
				delegate.add(createElement(delegate));
			return new BeanObservableListDecorator(delegate, null, null);
		}

		private int counter;

		public Object createElement(IObservableCollection collection) {
			return Integer.toString(counter++);
		}

		public void change(IObservable observable) {
			IObservableList list = (IObservableList) observable;
			list.add(createElement(list));
		}
	}
}
