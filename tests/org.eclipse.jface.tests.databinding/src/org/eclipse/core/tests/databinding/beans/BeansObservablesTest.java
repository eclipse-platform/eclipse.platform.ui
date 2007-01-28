/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Brad Reynolds - bug 164268, 171616
 ******************************************************************************/

package org.eclipse.core.tests.databinding.beans;

import java.util.Arrays;

import junit.framework.TestCase;

import org.eclipse.core.databinding.beans.BeansObservables;
import org.eclipse.core.databinding.beans.IBeanObservable;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.masterdetail.IObservableFactory;
import org.eclipse.core.databinding.observable.set.IObservableSet;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.core.internal.databinding.DetailObservableValue;
import org.eclipse.core.internal.databinding.internal.beans.BeanObservableListDecorator;
import org.eclipse.core.internal.databinding.internal.beans.BeanObservableSetDecorator;
import org.eclipse.core.internal.databinding.internal.beans.BeanObservableValueDecorator;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.tests.internal.databinding.internal.beans.Bean;
import org.eclipse.swt.widgets.Display;

/**
 * @since 3.2
 */
public class BeansObservablesTest extends TestCase {
	Bean[] elements = null;
	Bean model = null;
	Class elementType = null;

	protected void setUp() throws Exception {
		Realm.setDefault(SWTObservables.getRealm(Display.getDefault()));

		elements = new Bean[] { new Bean("1"), new Bean("2"), new Bean("3") };
		model = new Bean(Arrays.asList(elements));
		elementType = Bean.class;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		Realm.setDefault(null);
	}

	public void testObserveListArrayInferredElementType() throws Exception {
		IObservableList list = BeansObservables.observeList(Realm.getDefault(),
				model, "listArray", null);
		assertEquals("element type", elementType, list.getElementType());
	}

	public void testObserveListNonInferredElementType() throws Exception {
		elementType = Object.class;
		IObservableList list = BeansObservables.observeList(Realm.getDefault(),
				model, "list", null);
		assertEquals("element type", elementType, list.getElementType());
	}

	public void testListFactory() throws Exception {
		IObservableFactory factory = BeansObservables.listFactory(Realm
				.getDefault(), "list", elementType);
		IObservableList list = (IObservableList) factory
				.createObservable(model);

		assertTrue("elements of the list", Arrays.equals(elements, list
				.toArray(new Bean[list.size()])));
		assertEquals("element type", elementType, list.getElementType());
	}

	public void testObserveDetailListElementType() throws Exception {
		WritableValue parent = new WritableValue(Bean.class);
		parent.setValue(model);
		IObservableList list = BeansObservables.observeDetailList(Realm
				.getDefault(), parent, "list", elementType);

		assertEquals("element type", elementType, list.getElementType());
		assertTrue("elements of list", Arrays.equals(elements, list
				.toArray(new Bean[list.size()])));
	}

	public void testObserveDetailValueIBeanObservable() throws Exception {
		WritableValue parent = new WritableValue(Bean.class);
		parent.setValue(new Bean());

		IObservableValue detailValue = BeansObservables.observeDetailValue(
				Realm.getDefault(), parent, "value", String.class);
		assertTrue(detailValue instanceof IBeanObservable);

		BeanObservableValueDecorator beanObservable = (BeanObservableValueDecorator) detailValue;
		assertEquals("property descriptor", Bean.class.getMethod("getValue",
				null), beanObservable.getPropertyDescriptor().getReadMethod());
		assertEquals("observed", parent, beanObservable.getObserved());
		assertTrue("delegate",
				beanObservable.getDelegate() instanceof DetailObservableValue);
	}

	public void testObservableDetailListIBeanObservable() throws Exception {
		WritableValue parent = new WritableValue(Bean.class);
		parent.setValue(new Bean());

		IObservableList detailList = BeansObservables.observeDetailList(Realm
				.getDefault(), parent, "list", Bean.class);
		assertTrue("detail is not an IBeanObservable", detailList instanceof IBeanObservable);

		BeanObservableListDecorator beanObservable = (BeanObservableListDecorator) detailList;
		assertEquals("property descriptor", Bean.class.getMethod("getList",
				null), beanObservable.getPropertyDescriptor().getReadMethod());
		assertEquals("observed", parent, beanObservable.getObserved());
		
		//DetailObservableList is package level we can do a straight instanceof check
		assertTrue("delegate is the observed", beanObservable.getDelegate().equals(detailList));
	}
	
	public void testObservableDetailSetIBeanObservable() throws Exception {
		WritableValue parent = new WritableValue(Bean.class);
		parent.setValue(new Bean());
		
		IObservableSet detailSet = BeansObservables.observeDetailSet(Realm.getDefault(), parent, "set", Bean.class);
		assertTrue("detail is not an IBeanObservable", detailSet instanceof IBeanObservable);
		
		BeanObservableSetDecorator beanObservable = (BeanObservableSetDecorator) detailSet;
		assertEquals("property descriptor", Bean.class.getMethod("getSet",
				null), beanObservable.getPropertyDescriptor().getReadMethod());
		assertEquals("observed", parent, beanObservable.getObserved());
		
		//DetailObservableSet is package level we can't do a straight instanceof check
		assertTrue("delegate is the observed", beanObservable.getDelegate().equals(detailSet));
	}
	
	public void testObserveSetElementType() throws Exception {
		Bean bean = new Bean();
		IObservableSet observableSet = BeansObservables.observeSet(Realm.getDefault(), bean, "set", Bean.class);
		assertEquals(Bean.class, observableSet.getElementType());
	}
	
	public void testObserveSetNonInferredElementType() throws Exception {
		Bean bean = new Bean();
		IObservableSet observableSet = BeansObservables.observeSet(Realm.getDefault(), bean, "set");
		assertEquals(Object.class, observableSet.getElementType());
	}
}
