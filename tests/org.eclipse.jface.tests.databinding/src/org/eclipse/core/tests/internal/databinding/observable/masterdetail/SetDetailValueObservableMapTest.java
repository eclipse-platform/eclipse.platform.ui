/*******************************************************************************
 * Copyright (c) 2010 Ovidio Mallo and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Ovidio Mallo - initial API and implementation (bug 305367)
 ******************************************************************************/

package org.eclipse.core.tests.internal.databinding.observable.masterdetail;

import java.util.HashMap;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.core.databinding.beans.BeansObservables;
import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.masterdetail.IObservableFactory;
import org.eclipse.core.databinding.observable.set.WritableSet;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.core.internal.databinding.observable.masterdetail.SetDetailValueObservableMap;
import org.eclipse.jface.databinding.conformance.util.MapChangeEventTracker;
import org.eclipse.jface.examples.databinding.model.SimplePerson;
import org.eclipse.jface.tests.databinding.AbstractDefaultRealmTestCase;

/**
 * @since 1.3
 */
public class SetDetailValueObservableMapTest extends
		AbstractDefaultRealmTestCase {

	public static Test suite() {
		TestSuite suite = new TestSuite(SetDetailValueObservableMapTest.class
				.getName());
		suite.addTestSuite(SetDetailValueObservableMapTest.class);
		return suite;
	}

	public void testGetValueType() {
		SetDetailValueObservableMap sdom = new SetDetailValueObservableMap(
				new WritableSet(), BeansObservables.valueFactory("name"),
				String.class);

		assertSame(String.class, sdom.getValueType());
	}

	public void testGetObserved() {
		WritableSet masterKeySet = new WritableSet();
		SetDetailValueObservableMap sdom = new SetDetailValueObservableMap(
				masterKeySet, BeansObservables.valueFactory("name"),
				String.class);

		// The observed object is the master key set.
		assertSame(masterKeySet, sdom.getObserved());
	}

	public void testMasterSetInitiallyNotEmpty() {
		WritableSet masterKeySet = new WritableSet();
		SimplePerson person = new SimplePerson();
		person.setName("name");
		masterKeySet.add(person);
		SetDetailValueObservableMap sdom = new SetDetailValueObservableMap(
				masterKeySet, BeansObservables.valueFactory("name"),
				String.class);

		// Make sure that a non-empty master key set is initialized correctly.
		assertEquals(masterKeySet.size(), sdom.size());
		assertEquals(person.getName(), sdom.get(person));
	}

	public void testAddRemove() {
		WritableSet masterKeySet = new WritableSet();
		SetDetailValueObservableMap sdom = new SetDetailValueObservableMap(
				masterKeySet, BeansObservables.valueFactory("name"),
				String.class);

		// Initially, the detail map is empty.
		assertTrue(sdom.isEmpty());

		// Add a first person and check that its name is in the detail list.
		SimplePerson p1 = new SimplePerson();
		p1.setName("name1");
		masterKeySet.add(p1);
		assertEquals(masterKeySet.size(), sdom.size());
		assertEquals(p1.getName(), sdom.get(p1));

		// Add a second person and check that it's name is in the detail list.
		SimplePerson p2 = new SimplePerson();
		p2.setName("name2");
		masterKeySet.add(p2);
		assertEquals(masterKeySet.size(), sdom.size());
		assertEquals(p2.getName(), sdom.get(p2));

		// Remove the first person from the master list and check that we still
		// have the name of the second person in the detail list.
		masterKeySet.remove(p1);
		assertEquals(masterKeySet.size(), sdom.size());
		assertEquals(p2.getName(), sdom.get(p2));

		// Remove the second person as well.
		masterKeySet.remove(p2);
		assertTrue(sdom.isEmpty());
	}

	public void testChangeDetail() {
		WritableSet masterKeySet = new WritableSet();
		SetDetailValueObservableMap sdom = new SetDetailValueObservableMap(
				masterKeySet, BeansObservables.valueFactory("name"),
				String.class);

		// Change the detail attribute explicitly.
		SimplePerson p1 = new SimplePerson();
		p1.setName("name1");
		masterKeySet.add(p1);
		assertEquals(p1.getName(), sdom.get(p1));
		p1.setName("name2");
		assertEquals(p1.getName(), sdom.get(p1));

		// Change the detail attribute by changing the master.
		SimplePerson p2 = new SimplePerson();
		p2.setName("name3");
		masterKeySet.add(p2);
		assertEquals(p2.getName(), sdom.get(p2));
	}

	public void testPut() {
		WritableSet masterKeySet = new WritableSet();
		SetDetailValueObservableMap sdom = new SetDetailValueObservableMap(
				masterKeySet, BeansObservables.valueFactory("name"),
				String.class);

		// Change the detail attribute explicitly.
		SimplePerson person = new SimplePerson();
		person.setName("name1");
		masterKeySet.add(person);
		assertEquals(person.getName(), sdom.get(person));

		// Set a new name on the detail map.
		sdom.put(person, "name2");
		// Check that the name has been propagated to the master.
		assertEquals("name2", person.getName());
		assertEquals(person.getName(), sdom.get(person));
	}

	public void testContainsValue() {
		WritableSet masterKeySet = new WritableSet();
		SetDetailValueObservableMap sdom = new SetDetailValueObservableMap(
				masterKeySet, BeansObservables.valueFactory("name"),
				String.class);

		// Add a person with a given name.
		SimplePerson person = new SimplePerson();
		person.setName("name");
		masterKeySet.add(person);

		// Make sure the name of the person is contained.
		assertTrue(sdom.containsValue(person.getName()));

		// Remove the person and make sure that it's name cannot be found
		// anymore.
		masterKeySet.remove(person);
		assertFalse(sdom.containsValue(person.getName()));
	}

	public void testRemove() {
		WritableSet masterKeySet = new WritableSet();
		SetDetailValueObservableMap sdom = new SetDetailValueObservableMap(
				masterKeySet, BeansObservables.valueFactory("name"),
				String.class);

		// Add two person objects to the map.
		SimplePerson p1 = new SimplePerson();
		SimplePerson p2 = new SimplePerson();
		masterKeySet.add(p1);
		masterKeySet.add(p2);

		// Initially, both person objects should be contained in the detail map.
		assertTrue(sdom.containsKey(p1));
		assertTrue(sdom.containsKey(p2));

		// Remove one person and check that it is not contained anymore.
		sdom.remove(p1);
		assertFalse(sdom.containsKey(p1));
		assertTrue(sdom.containsKey(p2));

		// Trying to remove a non-existent is allowed but has no effect.
		sdom.remove(p1);
		assertFalse(sdom.containsKey(p1));
		assertTrue(sdom.containsKey(p2));
	}

	public void testDetailObservableChangeEvent() {
		WritableSet masterKeySet = new WritableSet();
		SetDetailValueObservableMap sdom = new SetDetailValueObservableMap(
				masterKeySet, BeansObservables.valueFactory("name"),
				String.class);

		MapChangeEventTracker changeTracker = MapChangeEventTracker
				.observe(sdom);

		SimplePerson person = new SimplePerson();
		person.setName("old name");

		// Initially, we should not have received any event.
		assertEquals(0, changeTracker.count);

		// Add the person and check that we receive an addition event on the
		// correct index and with the correct value.
		masterKeySet.add(person);
		assertEquals(1, changeTracker.count);
		assertEquals(1, changeTracker.event.diff.getAddedKeys().size());
		assertEquals(0, changeTracker.event.diff.getRemovedKeys().size());
		assertEquals(0, changeTracker.event.diff.getChangedKeys().size());
		assertSame(person, changeTracker.event.diff.getAddedKeys().iterator()
				.next());
		assertNull(changeTracker.event.diff.getOldValue(person));
		assertEquals("old name", changeTracker.event.diff.getNewValue(person));

		// Change the detail property and check that we receive a replace
		person.setName("new name");
		assertEquals(2, changeTracker.count);
		assertEquals(0, changeTracker.event.diff.getAddedKeys().size());
		assertEquals(0, changeTracker.event.diff.getRemovedKeys().size());
		assertEquals(1, changeTracker.event.diff.getChangedKeys().size());
		assertSame(person, changeTracker.event.diff.getChangedKeys().iterator()
				.next());
		assertEquals("old name", changeTracker.event.diff.getOldValue(person));
		assertEquals("new name", changeTracker.event.diff.getNewValue(person));
	}

	public void testMasterNull() {
		WritableSet masterKeySet = new WritableSet();
		SetDetailValueObservableMap sdom = new SetDetailValueObservableMap(
				masterKeySet, BeansObservables.valueFactory("name"),
				String.class);

		// Make sure null values are handled gracefully.
		masterKeySet.add(null);
		assertEquals(1, sdom.size());
		assertNull(sdom.get(null));
	}

	public void testDetailObservableValuesAreDisposed() {
		final Map detailObservables = new HashMap();
		IObservableFactory detailValueFactory = new IObservableFactory() {
			public IObservable createObservable(Object target) {
				WritableValue detailObservable = new WritableValue();
				// Remember the created observables.
				detailObservables.put(target, detailObservable);
				return detailObservable;
			}
		};

		WritableSet masterKeySet = new WritableSet();
		SetDetailValueObservableMap sdom = new SetDetailValueObservableMap(
				masterKeySet, detailValueFactory, null);

		Object master1 = new Object();
		Object master2 = new Object();
		masterKeySet.add(master1);
		masterKeySet.add(master2);

		// Attach a listener in order to ensure that all detail observables are
		// actually created.
		MapChangeEventTracker.observe(sdom);

		assertEquals(sdom.size(), detailObservables.size());

		// No detail observables should be disposed yet.
		assertFalse(((WritableValue) detailObservables.get(master1))
				.isDisposed());
		assertFalse(((WritableValue) detailObservables.get(master2))
				.isDisposed());

		// Only the detail observable for the removed master should be disposed.
		masterKeySet.remove(master2);
		assertFalse(((WritableValue) detailObservables.get(master1))
				.isDisposed());
		assertTrue(((WritableValue) detailObservables.get(master2))
				.isDisposed());

		// After disposing the detail map, all detail observables should be
		// disposed.
		sdom.dispose();
		assertTrue(((WritableValue) detailObservables.get(master1))
				.isDisposed());
		assertTrue(((WritableValue) detailObservables.get(master2))
				.isDisposed());
	}

	public void testDisposeOnMasterDisposed() {
		WritableSet masterKeySet = new WritableSet();
		SetDetailValueObservableMap sdom = new SetDetailValueObservableMap(
				masterKeySet, BeansObservables.valueFactory("name"),
				String.class);

		// Initially, nothing should be disposed.
		assertFalse(masterKeySet.isDisposed());
		assertFalse(sdom.isDisposed());

		// Upon disposing the master list, the detail list should be disposed as
		// well.
		masterKeySet.dispose();
		assertTrue(masterKeySet.isDisposed());
		assertTrue(sdom.isDisposed());
	}
}
