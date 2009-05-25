/*******************************************************************************
 * Copyright (c) 2006, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Brad Reynolds - bugs 164268, 171616
 *     Mike Evans - bug 217558
 *     Matthew Hall - bugs 221351, 246625, 260329, 264619
 *******************************************************************************/

package org.eclipse.core.tests.databinding.beans;

import java.util.Arrays;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.databinding.beans.BeansObservables;
import org.eclipse.core.databinding.beans.IBeanObservable;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.masterdetail.IObservableFactory;
import org.eclipse.core.databinding.observable.set.IObservableSet;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.core.databinding.property.value.IValueProperty;
import org.eclipse.core.internal.databinding.beans.BeanObservableListDecorator;
import org.eclipse.core.internal.databinding.beans.BeanObservableSetDecorator;
import org.eclipse.core.internal.databinding.beans.BeanObservableValueDecorator;
import org.eclipse.core.tests.internal.databinding.beans.Bean;
import org.eclipse.jface.tests.databinding.AbstractDefaultRealmTestCase;

/**
 * @since 3.2
 */
public class BeansObservablesTest extends AbstractDefaultRealmTestCase {
	Bean[] elements = null;
	Bean model = null;
	Class elementType = null;

	protected void setUp() throws Exception {
		super.setUp();

		elements = new Bean[] { new Bean("1"), new Bean("2"), new Bean("3") };
		model = new Bean(elements);
		model.setList(Arrays.asList(elements));
		elementType = Bean.class;
	}

	public void testObserveListArrayInferredElementType() throws Exception {
		IObservableList list = BeansObservables.observeList(Realm.getDefault(),
				model, "list", null);
		assertEquals("element type", Object.class, list.getElementType());
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
		WritableValue parent = WritableValue.withValueType(Bean.class);
		parent.setValue(model);
		IObservableList list = BeansObservables.observeDetailList(parent,
				"list", elementType);

		assertEquals("element type", elementType, list.getElementType());
		assertTrue("elements of list", Arrays.equals(elements, list
				.toArray(new Bean[list.size()])));
	}

	public void testObserveDetailValueIBeanObservable() throws Exception {
		WritableValue parent = WritableValue.withValueType(Bean.class);
		Bean bean = new Bean();
		parent.setValue(bean);

		IObservableValue detailValue = BeansObservables.observeDetailValue(
				parent, "value", String.class);
		assertTrue(detailValue instanceof IBeanObservable);

		BeanObservableValueDecorator beanObservable = (BeanObservableValueDecorator) detailValue;
		assertEquals("property descriptor", Bean.class.getMethod("getValue",
				null), beanObservable.getPropertyDescriptor().getReadMethod());
		assertEquals("observed", bean, beanObservable.getObserved());
		assertTrue("delegate", beanObservable.getDecorated().getClass()
				.getName().endsWith("DetailObservableValue"));
	}

	public void testObserveDetailValueNullOuterElementType() throws Exception {
		WritableValue parent = new WritableValue(new Bean(), null);

		IObservableValue detailValue = BeansObservables.observeDetailValue(
				parent, "value", String.class);

		assertNull("property descriptor", ((IBeanObservable) detailValue)
				.getPropertyDescriptor());
	}

	public void testObservableDetailListIBeanObservable() throws Exception {
		WritableValue parent = WritableValue.withValueType(Bean.class);
		Bean bean = new Bean();
		parent.setValue(bean);

		IObservableList detailList = BeansObservables.observeDetailList(parent,
				"list", Bean.class);
		assertTrue("detail is not an IBeanObservable",
				detailList instanceof IBeanObservable);

		BeanObservableListDecorator beanObservable = (BeanObservableListDecorator) detailList;
		assertEquals("property descriptor", Bean.class.getMethod("getList",
				null), beanObservable.getPropertyDescriptor().getReadMethod());
		assertEquals("observed", bean, beanObservable.getObserved());

		// DetailObservableList is package level we can do a straight instanceof
		// check
		assertTrue("delegate is the observed", beanObservable.getDecorated()
				.equals(detailList));
	}

	public void testObservableDetailListNullOuterElementType() throws Exception {
		WritableValue parent = new WritableValue(new Bean(), null);

		IObservableList detailList = BeansObservables.observeDetailList(parent,
				"list", Bean.class);

		assertNull("property descriptor", ((IBeanObservable) detailList)
				.getPropertyDescriptor());
	}

	public void testObservableDetailSetIBeanObservable() throws Exception {
		WritableValue parent = WritableValue.withValueType(Bean.class);
		Bean bean = new Bean();
		parent.setValue(bean);

		IObservableSet detailSet = BeansObservables.observeDetailSet(parent,
				"set", Bean.class);
		assertTrue("detail is not an IBeanObservable",
				detailSet instanceof IBeanObservable);

		BeanObservableSetDecorator beanObservable = (BeanObservableSetDecorator) detailSet;
		assertEquals("property descriptor", Bean.class
				.getMethod("getSet", null), beanObservable
				.getPropertyDescriptor().getReadMethod());
		assertEquals("observed", bean, beanObservable.getObserved());

		// DetailObservableSet is package level we can't do a straight
		// instanceof check
		assertTrue("delegate is the observed", beanObservable.getDecorated()
				.equals(detailSet));
	}

	public void testObservableDetailSetNullOuterElementType() throws Exception {
		WritableValue parent = new WritableValue(new Bean(), null);

		IObservableSet detailSet = BeansObservables.observeDetailSet(parent,
				"set", Bean.class);

		assertNull("property descriptor", ((IBeanObservable) detailSet)
				.getPropertyDescriptor());
	}

	public void testObserveSetElementType() throws Exception {
		Bean bean = new Bean();
		IObservableSet observableSet = BeansObservables.observeSet(Realm
				.getDefault(), bean, "set", Bean.class);
		assertEquals(Bean.class, observableSet.getElementType());
	}

	public void testObserveSetNonInferredElementType() throws Exception {
		Bean bean = new Bean();
		IObservableSet observableSet = BeansObservables.observeSet(Realm
				.getDefault(), bean, "set");
		assertEquals(Object.class, observableSet.getElementType());
	}

	/**
	 * Test for fix for Bug 217558 [DataBinding] Databinding -
	 * BeansObservables.observeList() - error when external code modifies
	 * observed list.
	 */
	public void testHandleExternalChangeToProperty() {
		Bean targetBean = new Bean();
		IObservableList modelObservable = BeansObservables.observeList(Realm
				.getDefault(), model, "array", elementType);
		IObservableList targetObservable = BeansObservables.observeList(Realm
				.getDefault(), targetBean, "array", elementType);

		DataBindingContext context = new DataBindingContext(Realm.getDefault());
		try {
			// bind two beans and check the binding works
			context.bindList(targetObservable, modelObservable);
			assertTrue(Arrays.equals(elements, targetBean.getArray()));

			// set source direct - target databinding still works...
			Bean[] newElements = new Bean[] { new Bean("4"), new Bean("5"),
					new Bean("6") };
			model.setArray(newElements);
			assertTrue(Arrays.equals(newElements, targetBean.getArray()));

			// ... but setting the model's list breaks databinding the other
			// way...

			// ... so setting target direct breaks databinding without fix and
			// the assert would fail
			newElements = new Bean[] { new Bean("7"), new Bean("8"),
					new Bean("9") };
			targetBean.setArray(newElements);
			assertTrue(Arrays.equals(newElements, model.getArray()));
		} finally {
			// context only needed for this test so not put in setUp / tearDown
			context.dispose();
		}

	}

	public void testObserveDetailValue_ValueType() {
		Bean inner = new Bean("string");
		Bean outer = new Bean(inner);
		IValueProperty beanProperty = BeanProperties.value("bean");
		IObservableValue beanObservable = beanProperty.observe(outer);
		assertEquals(Bean.class, beanObservable.getValueType());

		IValueProperty valueProperty = BeanProperties.value("value");
		IObservableValue valueObservable = valueProperty
				.observeDetail(beanObservable);
		assertEquals(String.class, valueObservable.getValueType());
	}
}
