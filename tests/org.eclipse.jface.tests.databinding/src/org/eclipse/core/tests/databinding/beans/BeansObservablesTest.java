/*******************************************************************************
 * Copyright (c) 2006, 2014 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Brad Reynolds - bugs 164268, 171616
 *     Mike Evans - bug 217558
 *     Matthew Hall - bugs 221351, 246625, 260329, 264619
 *     Simon Scholz <simon.scholz@vogella.com> - Bug 445446
 *******************************************************************************/

package org.eclipse.core.tests.databinding.beans;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.beans.typed.BeanProperties;
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
import org.junit.Before;
import org.junit.Test;

/**
 * @since 3.2
 */
public class BeansObservablesTest extends AbstractDefaultRealmTestCase {
	Bean[] elements = null;
	Bean model = null;
	Class<?> elementType = null;

	@Override
	@Before
	public void setUp() throws Exception {
		super.setUp();

		elements = new Bean[] { new Bean("1"), new Bean("2"), new Bean("3") };
		model = new Bean(elements);
		model.setList(new ArrayList<>(Arrays.asList(elements)));
		elementType = Bean.class;
	}

	@Test
	public void testObserveListArrayInferredElementType() throws Exception {
		IObservableList<Object> list = BeanProperties.list("list", null).observe(Realm.getDefault(), model);
		assertEquals("element type", Object.class, list.getElementType());
	}

	@Test
	public void testObserveListNonInferredElementType() throws Exception {
		elementType = Object.class;
		IObservableList<Object> list = BeanProperties.list("list", null).observe(Realm.getDefault(), model);
		assertEquals("element type", elementType, list.getElementType());
	}

	@Test
	public void testListFactory() throws Exception {
		IObservableFactory<Object, ? extends IObservableList<?>> factory = BeanProperties.list("list", elementType)
				.listFactory(Realm.getDefault());
		IObservableList<?> list = factory.createObservable(model);

		assertTrue("elements of the list",
				Arrays.equals(elements, list.toArray(new Bean[list.size()])));
		assertEquals("element type", elementType, list.getElementType());
	}

	@Test
	public void testObserveDetailListElementType() throws Exception {
		WritableValue<Bean> parent = WritableValue.withValueType(Bean.class);
		parent.setValue(model);
		IObservableList<?> list = BeanProperties.list("list", elementType).observeDetail(parent);

		assertEquals("element type", elementType, list.getElementType());
		assertTrue("elements of list",
				Arrays.equals(elements, list.toArray(new Bean[list.size()])));
	}

	@Test
	public void testObserveDetailValueIBeanObservable() throws Exception {
		WritableValue<Bean> parent = WritableValue.withValueType(Bean.class);
		Bean bean = new Bean();
		parent.setValue(bean);

		IObservableValue<String> detailValue = BeanProperties.value(Bean.class, "value", String.class)
				.observeDetail(parent);
		assertTrue(detailValue instanceof IBeanObservable);

		BeanObservableValueDecorator<?> beanObservable = (BeanObservableValueDecorator<?>) detailValue;
		assertEquals("property descriptor", Bean.class.getMethod("getValue"),
				beanObservable.getPropertyDescriptor().getReadMethod());
		assertEquals("observed", bean, beanObservable.getObserved());
		assertTrue("delegate", beanObservable.getDecorated().getClass()
				.getName().endsWith("DetailObservableValue"));
	}

	@Test
	public void testObserveDetailValueNullOuterElementType() throws Exception {
		WritableValue<Bean> parent = new WritableValue<>(new Bean(), null);

		IObservableValue<String> detailValue = BeanProperties.value("value", String.class).observeDetail(parent);

		assertNull("property descriptor",
				((IBeanObservable) detailValue).getPropertyDescriptor());
	}

	@Test
	public void testObservableDetailListIBeanObservable() throws Exception {
		WritableValue<Bean> parent = WritableValue.withValueType(Bean.class);
		Bean bean = new Bean();
		parent.setValue(bean);

		IObservableList<Bean> detailList = BeanProperties.list(Bean.class, "list", Bean.class).observeDetail(parent);
		assertTrue("detail is not an IBeanObservable",
				detailList instanceof IBeanObservable);

		BeanObservableListDecorator<?> beanObservable = (BeanObservableListDecorator<?>) detailList;
		assertEquals("property descriptor", Bean.class.getMethod("getList"),
				beanObservable.getPropertyDescriptor().getReadMethod());
		assertEquals("observed", bean, beanObservable.getObserved());

		// DetailObservableList is package level we can do a straight instanceof
		// check
		assertTrue("delegate is the observed", beanObservable.getDecorated()
				.equals(detailList));
	}

	@Test
	public void testObservableDetailListNullOuterElementType() throws Exception {
		WritableValue<Bean> parent = new WritableValue<>(new Bean(), null);

		IObservableList<Bean> detailList = BeanProperties.list("list", Bean.class).observeDetail(parent);

		assertNull("property descriptor",
				((IBeanObservable) detailList).getPropertyDescriptor());
	}

	@Test
	public void testObservableDetailSetIBeanObservable() throws Exception {
		WritableValue<Bean> parent = WritableValue.withValueType(Bean.class);
		Bean bean = new Bean();
		parent.setValue(bean);

		IObservableSet<Bean> detailSet = BeanProperties.set(Bean.class, "set", Bean.class).observe(bean);
		assertTrue("detail is not an IBeanObservable",
				detailSet instanceof IBeanObservable);

		BeanObservableSetDecorator<?> beanObservable = (BeanObservableSetDecorator<?>) detailSet;
		assertEquals("property descriptor", Bean.class.getMethod("getSet"),
				beanObservable.getPropertyDescriptor().getReadMethod());
		assertEquals("observed", bean, beanObservable.getObserved());

		// DetailObservableSet is package level we can't do a straight
		// instanceof check
		assertTrue("delegate is the observed", beanObservable.getDecorated()
				.equals(detailSet));
	}

	@Test
	public void testObservableDetailSetNullOuterElementType() throws Exception {
		WritableValue<Bean> parent = new WritableValue<>(new Bean(), null);

		IObservableSet<Bean> detailSet = BeanProperties.set("set", Bean.class).observeDetail(parent);

		assertNull("property descriptor",
				((IBeanObservable) detailSet).getPropertyDescriptor());
	}

	@Test
	public void testObserveSetElementType() throws Exception {
		Bean bean = new Bean();
		IObservableSet<Bean> observableSet = BeanProperties.set("set", Bean.class).observe(Realm.getDefault(), bean);
		assertEquals(Bean.class, observableSet.getElementType());
	}

	@Test
	public void testObserveSetNonInferredElementType() throws Exception {
		Bean bean = new Bean();
		IObservableSet<?> observableSet = BeanProperties.set("set").observe(Realm.getDefault(), bean);
		assertEquals(Object.class, observableSet.getElementType());
	}

	/**
	 * Test for fix for Bug 217558 [DataBinding] Databinding -
	 * BeansObservables.observeList() - error when external code modifies
	 * observed list.
	 */
	@Test
	public void testHandleExternalChangeToProperty() {
		Bean targetBean = new Bean();
		IObservableList<?> modelObservable = BeanProperties.list("array", elementType).observe(Realm.getDefault(),
				model);
		IObservableList<?> targetObservable = BeanProperties.list("array", elementType).observe(Realm.getDefault(),
				targetBean);

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

	@Test
	public void testObserveDetailValue_ValueType() {
		Bean inner = new Bean("string");
		Bean outer = new Bean(inner);
		IValueProperty<Bean, Bean> beanProperty = BeanProperties.value("bean");
		IObservableValue<Bean> beanObservable = beanProperty.observe(outer);
		assertEquals(Bean.class, beanObservable.getValueType());

		IValueProperty<Bean, String> valueProperty = BeanProperties.value("value");
		IObservableValue<String> valueObservable = valueProperty.observeDetail(beanObservable);
		assertEquals(String.class, valueObservable.getValueType());
	}
}
