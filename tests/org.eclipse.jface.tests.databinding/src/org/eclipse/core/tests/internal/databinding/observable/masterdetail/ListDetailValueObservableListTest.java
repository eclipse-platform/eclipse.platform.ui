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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.core.databinding.beans.BeansObservables;
import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.IObservableCollection;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.ListDiff;
import org.eclipse.core.databinding.observable.list.ListDiffEntry;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.core.databinding.observable.masterdetail.IObservableFactory;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.core.internal.databinding.observable.masterdetail.ListDetailValueObservableList;
import org.eclipse.jface.databinding.conformance.ObservableListContractTest;
import org.eclipse.jface.databinding.conformance.delegate.AbstractObservableCollectionContractDelegate;
import org.eclipse.jface.databinding.conformance.util.ListChangeEventTracker;
import org.eclipse.jface.examples.databinding.model.SimplePerson;
import org.eclipse.jface.tests.databinding.AbstractDefaultRealmTestCase;

/**
 * @since 1.3
 */
public class ListDetailValueObservableListTest extends
		AbstractDefaultRealmTestCase {

	public static Test suite() {
		TestSuite suite = new TestSuite(
				ListDetailValueObservableListTest.class.getName());
		suite.addTestSuite(ListDetailValueObservableListTest.class);
		suite.addTest(ObservableListContractTest.suite(new Delegate()));
		return suite;
	}

	public void testUnmodifiability() {
		WritableList masterObservableList = new WritableList();
		masterObservableList.add(new SimplePerson());
		masterObservableList.add(new SimplePerson());
		ListDetailValueObservableList ldol = new ListDetailValueObservableList(
				masterObservableList, BeansObservables.valueFactory("name"),
				null);

		try {
			ldol.add("name");
			fail("ListDetailValueObservableList must not be modifiable.");
		} catch (UnsupportedOperationException e) {
			// expected exception
		}

		try {
			ldol.remove(masterObservableList.get(0));
			fail("ListDetailValueObservableList must not be modifiable.");
		} catch (UnsupportedOperationException e) {
			// expected exception
		}

		try {
			ldol.removeAll(Collections.singleton(masterObservableList.get(0)));
			fail("ListDetailValueObservableList must not be modifiable.");
		} catch (UnsupportedOperationException e) {
			// expected exception
		}

		try {
			ldol.retainAll(Collections.EMPTY_LIST);
			fail("ListDetailValueObservableList must not be modifiable.");
		} catch (UnsupportedOperationException e) {
			// expected exception
		}

		try {
			ldol.move(0, 1);
			fail("ListDetailValueObservableList must not be modifiable.");
		} catch (UnsupportedOperationException e) {
			// expected exception
		}
	}

	public void testGetElementType() {
		ListDetailValueObservableList ldol = new ListDetailValueObservableList(
				new WritableList(), BeansObservables.valueFactory("name"),
				String.class);

		assertSame(String.class, ldol.getElementType());
	}

	public void testGetObserved() {
		WritableList masterList = new WritableList();
		ListDetailValueObservableList ldol = new ListDetailValueObservableList(
				masterList, BeansObservables.valueFactory("name"), String.class);

		// The observed object is the master list.
		assertSame(masterList, ldol.getObserved());
	}

	public void testMasterListInitiallyNotEmpty() {
		WritableList masterList = new WritableList();
		SimplePerson person = new SimplePerson();
		person.setName("name");
		masterList.add(person);
		ListDetailValueObservableList ldol = new ListDetailValueObservableList(
				masterList, BeansObservables.valueFactory("name"), String.class);

		// Make sure that a non-empty master list is initialized correctly.
		assertEquals(masterList.size(), ldol.size());
		assertEquals(person.getName(), ldol.get(0));
	}

	public void testAddRemove() {
		WritableList masterList = new WritableList();
		ListDetailValueObservableList ldol = new ListDetailValueObservableList(
				masterList, BeansObservables.valueFactory("name"), String.class);

		// Initially, the detail list is empty.
		assertTrue(ldol.isEmpty());

		// Add a first person and check that its name is in the detail list.
		SimplePerson p1 = new SimplePerson();
		p1.setName("name1");
		masterList.add(p1);
		assertEquals(masterList.size(), ldol.size());
		assertEquals(p1.getName(), ldol.get(0));

		// Add a second person and check that it's name is in the detail list.
		SimplePerson p2 = new SimplePerson();
		p2.setName("name2");
		masterList.add(p2);
		assertEquals(masterList.size(), ldol.size());
		assertEquals(p2.getName(), ldol.get(1));

		// Remove the first person from the master list and check that we still
		// have the name of the second person in the detail list.
		masterList.remove(0);
		assertEquals(masterList.size(), ldol.size());
		assertEquals(p2.getName(), ldol.get(0));

		// Remove the second person as well.
		masterList.remove(0);
		assertTrue(ldol.isEmpty());
	}

	public void testChangeDetail() {
		WritableList masterList = new WritableList();
		ListDetailValueObservableList ldol = new ListDetailValueObservableList(
				masterList, BeansObservables.valueFactory("name"), String.class);

		// Change the detail attribute explicitly.
		SimplePerson p1 = new SimplePerson();
		p1.setName("name1");
		masterList.add(p1);
		assertEquals(p1.getName(), ldol.get(0));
		p1.setName("name2");
		assertEquals(p1.getName(), ldol.get(0));

		// Change the detail attribute by changing the master.
		SimplePerson p2 = new SimplePerson();
		p2.setName("name3");
		masterList.set(0, p2);
		assertEquals(p2.getName(), ldol.get(0));
	}

	public void testSet() {
		WritableList masterList = new WritableList();
		ListDetailValueObservableList ldol = new ListDetailValueObservableList(
				masterList, BeansObservables.valueFactory("name"), String.class);

		// Change the detail attribute explicitly.
		SimplePerson person = new SimplePerson();
		person.setName("name1");
		masterList.add(person);
		assertEquals(person.getName(), ldol.get(0));

		// Set a new name on the detail list.
		ldol.set(0, "name2");
		// Check that the name has been propagated to the master.
		assertEquals("name2", person.getName());
		assertEquals(person.getName(), ldol.get(0));
	}

	public void testDuplicateMasterElements() {
		WritableList masterList = new WritableList();
		ListDetailValueObservableList ldol = new ListDetailValueObservableList(
				masterList, BeansObservables.valueFactory("name"), String.class);

		SimplePerson master = new SimplePerson();
		master.setName("name1");

		// Add the same master twice.
		masterList.add(master);
		masterList.add(master);

		// Attach the change listener to the detail list.
		ListChangeEventTracker changeTracker = ListChangeEventTracker
				.observe(ldol);

		// Setting the name on master should trigger an event on both
		// occurrences of in the master list.
		master.setName("name2");

		// We should have 2 replace diffs, i.e. 4 diff entries.
		assertEquals(1, changeTracker.count);
		assertEquals(4, changeTracker.event.diff.getDifferences().length);
		assertReplaceDiffAt(changeTracker.event.diff, 0, 0, "name1", "name2");
		assertReplaceDiffAt(changeTracker.event.diff, 2, 0, "name1", "name2");

		// Remove one instance of the master (one will remain).
		masterList.remove(master);

		// It should still be possible to work on the remaining master instance.
		ldol.set(0, "name3");
		assertEquals("name3", master.getName());
	}

	public void testDetailObservableChangeEvent() {
		WritableList masterList = new WritableList();
		ListDetailValueObservableList ldol = new ListDetailValueObservableList(
				masterList, BeansObservables.valueFactory("name"), String.class);

		ListChangeEventTracker changeTracker = ListChangeEventTracker
				.observe(ldol);

		SimplePerson person = new SimplePerson();
		person.setName("old name");

		// Initially, we should not have received any event.
		assertEquals(0, changeTracker.count);

		// Add the person and check that we receive an addition event on the
		// correct index and with the correct value.
		masterList.add(person);
		assertEquals(1, changeTracker.count);
		assertEquals(1, changeTracker.event.diff.getDifferences().length);
		assertTrue(changeTracker.event.diff.getDifferences()[0].isAddition());
		assertEquals(0,
				changeTracker.event.diff.getDifferences()[0].getPosition());
		assertEquals(person.getName(),
				changeTracker.event.diff.getDifferences()[0].getElement());

		// Change the detail property and check that we receive a replace event.
		person.setName("new name");
		assertEquals(2, changeTracker.count);
		assertIsSingleReplaceDiff(changeTracker.event.diff, 0, "old name",
				"new name");
	}

	private void assertIsSingleReplaceDiff(ListDiff diff, int index,
			Object oldElement, Object newElement) {
		// We should have 2 diff entries.
		assertEquals(2, diff.getDifferences().length);

		// Check that it indeed is a replace diff.
		assertReplaceDiffAt(diff, 0, index, oldElement, newElement);
	}

	private void assertReplaceDiffAt(ListDiff diff, int diffOffset, int index,
			Object oldElement, Object newElement) {
		ListDiffEntry entry1 = diff.getDifferences()[0];
		ListDiffEntry entry2 = diff.getDifferences()[1];

		// One diff entry must be an addition, the other a removal.
		assertTrue(entry1.isAddition() != entry2.isAddition());

		// Check for the index on the diff entries.
		assertEquals(index, entry1.getPosition());
		assertEquals(index, entry2.getPosition());

		// Check for the old/new element values on both diff entries.
		if (entry1.isAddition()) {
			assertEquals(oldElement, entry2.getElement());
			assertEquals(newElement, entry1.getElement());
		} else {
			assertEquals(oldElement, entry1.getElement());
			assertEquals(newElement, entry2.getElement());
		}
	}

	public void testMasterNull() {
		WritableList masterObservableList = new WritableList();
		ListDetailValueObservableList ldol = new ListDetailValueObservableList(
				masterObservableList, BeansObservables.valueFactory("name"),
				String.class);

		// Make sure null values are handled gracefully.
		masterObservableList.add(null);
		assertEquals(1, ldol.size());
		assertNull(ldol.get(0));
	}

	public void testDetailObservableValuesAreDisposed() {
		final List detailObservables = new ArrayList();
		IObservableFactory detailValueFactory = new IObservableFactory() {
			public IObservable createObservable(Object target) {
				WritableValue detailObservable = new WritableValue();
				// Remember the created observables.
				detailObservables.add(detailObservable);
				return detailObservable;
			}
		};

		WritableList masterList = new WritableList();
		ListDetailValueObservableList ldol = new ListDetailValueObservableList(
				masterList, detailValueFactory, null);

		masterList.add(new Object());
		masterList.add(new Object());

		assertEquals(ldol.size(), detailObservables.size());

		// No detail observables should be disposed yet.
		assertFalse(((WritableValue) detailObservables.get(0)).isDisposed());
		assertFalse(((WritableValue) detailObservables.get(1)).isDisposed());

		// Only the detail observable for the removed master should be disposed.
		masterList.remove(1);
		assertFalse(((WritableValue) detailObservables.get(0)).isDisposed());
		assertTrue(((WritableValue) detailObservables.get(1)).isDisposed());

		// After disposing the detail list, all detail observables should be
		// disposed.
		ldol.dispose();
		assertTrue(((WritableValue) detailObservables.get(0)).isDisposed());
		assertTrue(((WritableValue) detailObservables.get(1)).isDisposed());
	}

	public void testDisposeOnMasterDisposed() {
		WritableList masterList = new WritableList();
		ListDetailValueObservableList ldol = new ListDetailValueObservableList(
				masterList, BeansObservables.valueFactory("name"), String.class);

		// Initially, nothing should be disposed.
		assertFalse(masterList.isDisposed());
		assertFalse(ldol.isDisposed());

		// Upon disposing the master list, the detail list should be disposed as
		// well.
		masterList.dispose();
		assertTrue(masterList.isDisposed());
		assertTrue(ldol.isDisposed());
	}

	private static class Delegate extends
			AbstractObservableCollectionContractDelegate {
		public IObservableCollection createObservableCollection(Realm realm,
				int elementCount) {
			WritableList masterList = new WritableList(realm);
			for (int i = 0; i < elementCount; i++) {
				masterList.add(new SimplePerson());
			}

			return new TestListDetailValueObservableList(masterList,
					BeansObservables.valueFactory(realm, "name"), String.class);
		}

		public void change(IObservable observable) {
			TestListDetailValueObservableList ldol = (TestListDetailValueObservableList) observable;
			ldol.masterList.add(new SimplePerson());
		}

		public Object getElementType(IObservableCollection collection) {
			return String.class;
		}
	}

	private static class TestListDetailValueObservableList extends
			ListDetailValueObservableList {
		final IObservableList masterList;

		public TestListDetailValueObservableList(IObservableList masterList,
				IObservableFactory detailValueFactory, Object detailType) {
			super(masterList, detailValueFactory, detailType);
			this.masterList = masterList;
		}
	}
}
