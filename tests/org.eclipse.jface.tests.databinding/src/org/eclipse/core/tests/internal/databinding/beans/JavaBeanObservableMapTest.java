/*******************************************************************************
 * Copyright (c) 2006, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Matthew Hall - bugs 213145, 241585, 246103, 194734, 268688
 *     Ovidio Mallo - bug 247741
 *******************************************************************************/

package org.eclipse.core.tests.internal.databinding.beans;

import java.beans.PropertyDescriptor;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.databinding.beans.BeansObservables;
import org.eclipse.core.databinding.beans.IBeanObservable;
import org.eclipse.core.databinding.beans.IBeanProperty;
import org.eclipse.core.databinding.beans.PojoObservables;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.map.IObservableMap;
import org.eclipse.core.databinding.observable.map.MapDiff;
import org.eclipse.core.databinding.observable.set.WritableSet;
import org.eclipse.core.tests.databinding.observable.ThreadRealm;
import org.eclipse.jface.databinding.conformance.util.ChangeEventTracker;
import org.eclipse.jface.databinding.conformance.util.CurrentRealm;
import org.eclipse.jface.databinding.conformance.util.MapChangeEventTracker;
import org.eclipse.jface.tests.databinding.AbstractDefaultRealmTestCase;

/**
 * @since 3.2
 * 
 */
public class JavaBeanObservableMapTest extends AbstractDefaultRealmTestCase {
	private Bean model1;

	private Bean model2;

	private WritableSet set;

	private PropertyDescriptor propertyDescriptor;

	private IObservableMap map;
	private IBeanObservable beanObservable;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		ThreadRealm realm = new ThreadRealm();
		realm.init(Thread.currentThread());
		model1 = new Bean("1");
		model2 = new Bean("2");

		set = new WritableSet(realm, new HashSet(), Bean.class);
		set.add(model1);
		set.add(model2);

		String propertyName = "value";
		propertyDescriptor = ((IBeanProperty) BeanProperties.value(Bean.class,
				propertyName)).getPropertyDescriptor();
		map = BeansObservables.observeMap(set, Bean.class, propertyName);
		beanObservable = (IBeanObservable) map;
	}

	public void testGetValue() throws Exception {
		assertEquals(
				"The 'value' from the map should be the value of the property of the model.",
				model1.getValue(), map.get(model1));
	}

	public void testGetValue_KeyOutOfDomain() {
		Bean model3 = new Bean("3");
		assertFalse(map.containsKey(model3));
		assertFalse(model3.getValue().equals(map.get(model3)));
	}

	public void testSetValueNotifications() throws Exception {
		String oldValue = model1.getValue();
		String newValue = model1.getValue() + model1.getValue();
		MapChangeEventTracker listener = new MapChangeEventTracker();

		map.addMapChangeListener(listener);
		assertEquals(0, listener.count);
		model1.setValue(newValue);
		assertEquals(1, listener.count);
		assertTrue(listener.event.diff.getChangedKeys().contains(model1));
		assertEquals(newValue, listener.event.diff.getNewValue(model1));
		assertEquals(oldValue, listener.event.diff.getOldValue(model1));
		assertFalse(listener.event.diff.getAddedKeys().contains(model1));
		assertFalse(listener.event.diff.getRemovedKeys().contains(model1));
	}

	public void testPutValue() throws Exception {
		String oldValue = model1.getValue();
		String newValue = model1.getValue() + model1.getValue();
		MapChangeEventTracker listener = new MapChangeEventTracker();
		map.addMapChangeListener(listener);

		assertEquals(0, listener.count);
		map.put(model1, newValue);
		assertEquals(1, listener.count);
		assertEquals(newValue, model1.getValue());
		assertEquals(oldValue, listener.event.diff.getOldValue(model1));
		assertEquals(newValue, listener.event.diff.getNewValue(model1));
		assertFalse(listener.event.diff.getAddedKeys().contains(model1));
		assertTrue(listener.event.diff.getChangedKeys().contains(model1));
		assertFalse(listener.event.diff.getRemovedKeys().contains(model1));
	}

	public void testAddKey() throws Exception {
		MapChangeEventTracker listener = new MapChangeEventTracker();
		map.addMapChangeListener(listener);

		Bean model3 = new Bean("3");

		assertEquals(0, listener.count);
		set.add(model3);
		assertEquals(1, listener.count);
		assertTrue(listener.event.diff.getAddedKeys().contains(model3));
		assertEquals(model3.getValue(), map.get(model3));

		String newValue = model3.getValue() + model3.getValue();
		model3.setValue(newValue);
		assertEquals(2, listener.count);
		assertEquals(3, map.size());
	}

	public void testRemoveKey() throws Exception {
		MapChangeEventTracker listener = new MapChangeEventTracker();
		map.addMapChangeListener(listener);

		assertEquals(0, listener.count);
		set.remove(model1);
		assertEquals(1, listener.count);
		assertFalse(listener.event.diff.getAddedKeys().contains(model1));
		assertFalse(listener.event.diff.getChangedKeys().contains(model1));
		assertTrue(listener.event.diff.getRemovedKeys().contains(model1));
		assertEquals(1, map.size());
	}

	public void testGetObserved() throws Exception {
		assertEquals(set, beanObservable.getObserved());
	}

	public void testGetPropertyDescriptor() throws Exception {
		assertEquals(propertyDescriptor, beanObservable.getPropertyDescriptor());
	}

	public void testConstructor_SkipRegisterListeners() throws Exception {
		Realm realm = new CurrentRealm(true);
		WritableSet set = new WritableSet(realm);
		Bean bean = new Bean();
		set.add(bean);

		IObservableMap observable = PojoObservables.observeMap(set, Bean.class,
				"value");
		assertFalse(bean.hasListeners("value"));
		ChangeEventTracker.observe(observable);

		assertFalse(bean.hasListeners("value"));
	}

	public void testConstructor_RegistersListeners() throws Exception {
		Realm realm = new CurrentRealm(true);
		WritableSet set = new WritableSet(realm);
		Bean bean = new Bean();
		set.add(bean);

		IObservableMap observable = BeansObservables.observeMap(set,
				Bean.class, "value");
		assertFalse(bean.hasListeners("value"));
		ChangeEventTracker.observe(observable);

		assertTrue(bean.hasListeners("value"));
	}

	public void testSetBeanProperty_CorrectForNullOldAndNewValues() {
		// The java bean spec allows the old and new values in a
		// PropertyChangeEvent to be null, which indicates that an unknown
		// change occured.

		// This test ensures that JavaBeanObservableValue fires the correct
		// value diff even if the bean implementor is lazy :-P

		WritableSet set = new WritableSet(new CurrentRealm(true));

		Bean bean = new AnnoyingBean();
		bean.setValue("old");
		set.add(bean);

		IObservableMap map = BeansObservables.observeMap(set, Bean.class,
				"value");
		MapChangeEventTracker tracker = MapChangeEventTracker.observe(map);

		bean.setValue("new");

		assertEquals(1, tracker.count);

		assertEquals(Collections.EMPTY_SET, tracker.event.diff.getAddedKeys());
		assertEquals(Collections.singleton(bean), tracker.event.diff
				.getChangedKeys());
		assertEquals(Collections.EMPTY_SET, tracker.event.diff.getRemovedKeys());

		assertEquals("old", tracker.event.diff.getOldValue(bean));
		assertEquals("new", tracker.event.diff.getNewValue(bean));
	}

	public void testModifyObservableMap_FiresMapChange() {
		Bean bean = new Bean(Collections.singletonMap("key", "oldValue"));
		IObservableMap observable = BeansObservables.observeMap(bean, "map");
		MapChangeEventTracker tracker = MapChangeEventTracker
				.observe(observable);

		observable.put("key", "newValue");

		assertEquals(1, tracker.count);
		assertDiff(tracker.event.diff, Collections.singletonMap("key",
				"oldValue"), Collections.singletonMap("key", "newValue"));
	}

	public void testSetBeanPropertyOutsideRealm_FiresEventInsideRealm() {
		Bean bean = new Bean(Collections.EMPTY_MAP);
		CurrentRealm realm = new CurrentRealm(true);
		IObservableMap observable = BeansObservables.observeMap(realm, bean,
				"map");
		MapChangeEventTracker tracker = MapChangeEventTracker
				.observe(observable);

		realm.setCurrent(false);
		bean.setMap(Collections.singletonMap("key", "value"));
		assertEquals(0, tracker.count);

		realm.setCurrent(true);
		assertEquals(1, tracker.count);
		assertDiff(tracker.event.diff, Collections.EMPTY_MAP, Collections
				.singletonMap("key", "value"));
	}

	private static void assertDiff(MapDiff diff, Map oldMap, Map newMap) {
		oldMap = new HashMap(oldMap); // defensive copy in case arg is
		// unmodifiable
		diff.applyTo(oldMap);
		assertEquals("applying diff to list did not produce expected result",
				newMap, oldMap);
	}

	public static Test suite() {
		TestSuite suite = new TestSuite(JavaBeanObservableMapTest.class
				.getName());
		suite.addTestSuite(JavaBeanObservableMapTest.class);
		return suite;
	}
}
