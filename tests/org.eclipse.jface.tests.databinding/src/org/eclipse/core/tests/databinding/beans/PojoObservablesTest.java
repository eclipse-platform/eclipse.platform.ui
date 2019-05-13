/*******************************************************************************
 * Copyright (c) 2007, 2009 IBM Corporation and others.
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
 *     Matthew Hall - bugs 194734, 264619
 *******************************************************************************/

package org.eclipse.core.tests.databinding.beans;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.core.databinding.beans.IBeanObservable;
import org.eclipse.core.databinding.beans.PojoObservables;
import org.eclipse.core.databinding.beans.typed.PojoProperties;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.map.IObservableMap;
import org.eclipse.core.databinding.observable.masterdetail.IObservableFactory;
import org.eclipse.core.databinding.observable.set.IObservableSet;
import org.eclipse.core.databinding.observable.set.WritableSet;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.property.value.IValueProperty;
import org.eclipse.core.tests.internal.databinding.beans.Bean;
import org.eclipse.jface.databinding.conformance.util.ChangeEventTracker;
import org.eclipse.jface.databinding.conformance.util.CurrentRealm;
import org.eclipse.jface.tests.databinding.AbstractDefaultRealmTestCase;
import org.junit.Before;
import org.junit.Test;

/**
 * @since 3.2
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class PojoObservablesTest extends AbstractDefaultRealmTestCase {
	private Bean pojo;
	private String propertyName;

	@Before
	@Override
	public void setUp() throws Exception {
		super.setUp();

		pojo = new Bean();
		propertyName = "value";
	}

	@Test
	public void testObserveValue_ReturnsIBeanObservable() throws Exception {
		IObservableValue value = PojoObservables.observeValue(pojo,
				propertyName);

		assertNotNull(value);
		assertTrue(value instanceof IBeanObservable);
	}

	@Test
	public void testObserveValue_DoesNotAttachListeners() throws Exception {
		IObservableValue value = PojoObservables.observeValue(pojo,
				propertyName);

		ChangeEventTracker.observe(value);
		assertFalse(pojo.hasListeners(propertyName));
	}

	@Test
	public void testObservableValueWithRealm_ReturnsIBeanObservable()
			throws Exception {
		CurrentRealm realm = new CurrentRealm(true);
		IObservableValue value = PojoObservables.observeValue(realm, pojo,
				propertyName);

		assertNotNull(value);
		assertTrue(value instanceof IBeanObservable);
	}

	@Test
	public void testObservableMap_ReturnsIBeanObservable() throws Exception {
		IObservableSet set = new WritableSet();
		set.add(new Bean());

		IObservableMap map = PojoObservables.observeMap(set, Bean.class,
				propertyName);
		assertNotNull(map);
		assertTrue(map instanceof IBeanObservable);
	}

	@Test
	public void testObservableMap_DoesNotAttachListeners() throws Exception {
		IObservableSet set = new WritableSet();
		set.add(pojo);

		IObservableMap map = PojoObservables.observeMap(set, Bean.class,
				propertyName);
		assertFalse(pojo.hasListeners(propertyName));
		ChangeEventTracker.observe(map);
		assertFalse(pojo.hasListeners(propertyName));
	}

	@Test
	public void testObserveMaps_ReturnsMaps() throws Exception {
		IObservableSet set = new WritableSet();
		set.add(pojo);

		IObservableMap[] maps = PojoObservables.observeMaps(set, Bean.class,
				new String[] { "value", "class" });
		assertEquals(2, maps.length);
	}

	@Test
	public void testObserveListWithElementType_ReturnsIBeanObservable()
			throws Exception {
		IObservableList list = PojoObservables.observeList(Realm.getDefault(),
				pojo, "list", String.class);
		assertTrue(list instanceof IBeanObservable);
	}

	@Test
	public void testObserveListWithElementType_DoesNotAttachListeners()
			throws Exception {
		IObservableList observable = PojoObservables.observeList(Realm
				.getDefault(), pojo, "list", String.class);
		assertFalse(pojo.hasListeners("list"));
		ChangeEventTracker.observe(observable);
		assertFalse(pojo.hasListeners("list"));
	}

	@Test
	public void testObserveList_ReturnsIBeanObservable() throws Exception {
		IObservableList observable = PojoObservables.observeList(Realm
				.getDefault(), pojo, "list");
		assertTrue(observable instanceof IBeanObservable);
	}

	@Test
	public void testObserveList_DoesNotAttachListeners() throws Exception {
		IObservableList observable = PojoObservables.observeList(Realm
				.getDefault(), pojo, "list");
		assertFalse(pojo.hasListeners("list"));
		ChangeEventTracker.observe(observable);
		assertFalse(pojo.hasListeners("list"));
	}

	@Test
	public void testObserveSetWithElementType_ReturnsIBeanObservable()
			throws Exception {
		IObservableSet list = PojoObservables.observeSet(Realm.getDefault(),
				pojo, "set", String.class);
		assertTrue(list instanceof IBeanObservable);
	}

	@Test
	public void testObserveSetWithElementType_DoesNotAttachListeners()
			throws Exception {
		IObservableSet observable = PojoObservables.observeSet(Realm
				.getDefault(), pojo, "set", String.class);
		assertFalse(pojo.hasListeners("set"));
		ChangeEventTracker.observe(observable);
		assertFalse(pojo.hasListeners("set"));
	}

	@Test
	public void testObserveSet_ReturnsIBeanObservable() throws Exception {
		IObservableSet list = PojoObservables.observeSet(Realm.getDefault(),
				pojo, "set");
		assertTrue(list instanceof IBeanObservable);
	}

	@Test
	public void testObserveSet_DoesNotAttachListeners() throws Exception {
		IObservableSet observable = PojoObservables.observeSet(Realm
				.getDefault(), pojo, "set");
		assertFalse(pojo.hasListeners("set"));
		ChangeEventTracker.observe(observable);
		assertFalse(pojo.hasListeners("set"));
	}

	@Test
	public void testValueFactory_DoesNotAttachListeners() throws Exception {
		IObservableFactory factory = PojoObservables.valueFactory(Realm
				.getDefault(), "value");
		IObservableValue observable = (IObservableValue) factory
				.createObservable(pojo);

		assertFalse(pojo.hasListeners("value"));
		ChangeEventTracker.observe(observable);
		assertFalse(pojo.hasListeners("value"));
	}

	@Test
	public void testListFactory_DoesNotAttachListeners() throws Exception {
		IObservableFactory factory = PojoObservables.listFactory(Realm
				.getDefault(), "list", String.class);
		IObservableList observable = (IObservableList) factory
				.createObservable(pojo);

		assertFalse(pojo.hasListeners("value"));
		ChangeEventTracker.observe(observable);
		assertFalse(pojo.hasListeners("value"));
	}

	@Test
	public void testSetFactory_DoesNotAttachListeners() throws Exception {
		IObservableFactory factory = PojoObservables.setFactory(Realm
				.getDefault(), propertyName);
		IObservableSet observable = (IObservableSet) factory
				.createObservable(pojo);

		assertFalse(pojo.hasListeners("set"));
		ChangeEventTracker.observe(observable);
		assertFalse(pojo.hasListeners("set"));
	}

	@Test
	public void testSetFactoryWithElementType_DoesNotAttachListeners()
			throws Exception {
		IObservableFactory factory = PojoObservables.setFactory(Realm
				.getDefault(), propertyName, String.class);
		IObservableSet observable = (IObservableSet) factory
				.createObservable(pojo);

		assertFalse(pojo.hasListeners("set"));
		ChangeEventTracker.observe(observable);
		assertFalse(pojo.hasListeners("set"));
	}

	@Test
	public void testObserveDetailValue_ValueType() {
		Bean inner = new Bean("string");
		Bean outer = new Bean(inner);
		IValueProperty beanProperty = PojoProperties.value("bean");
		IObservableValue beanObservable = beanProperty.observe(outer);
		assertEquals(Bean.class, beanObservable.getValueType());

		IValueProperty valueProperty = PojoProperties.value("value");
		IObservableValue valueObservable = valueProperty
				.observeDetail(beanObservable);
		assertEquals(String.class, valueObservable.getValueType());
	}
}
