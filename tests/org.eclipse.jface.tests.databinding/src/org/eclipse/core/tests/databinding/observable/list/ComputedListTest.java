/*******************************************************************************
 * Copyright (c) 2007, 2008 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 211786)
 *     Matthew Hall - bug 213145
 ******************************************************************************/

package org.eclipse.core.tests.databinding.observable.list;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.core.databinding.observable.AbstractObservable;
import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.IObservableCollection;
import org.eclipse.core.databinding.observable.IStaleListener;
import org.eclipse.core.databinding.observable.ObservableTracker;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.StaleEvent;
import org.eclipse.core.databinding.observable.list.ComputedList;
import org.eclipse.jface.databinding.conformance.ObservableListContractTest;
import org.eclipse.jface.databinding.conformance.delegate.AbstractObservableCollectionContractDelegate;
import org.eclipse.jface.databinding.conformance.util.ListChangeEventTracker;
import org.eclipse.jface.tests.databinding.AbstractDefaultRealmTestCase;

public class ComputedListTest extends AbstractDefaultRealmTestCase {
	ComputedListStub list;

	protected void setUp() throws Exception {
		super.setUp();
		list = new ComputedListStub();
		list.size(); // Force list to compute
	}

	public void testDependency_Staleness() {
		assertFalse(list.isStale());
		list.dependency.fireStale();
		assertTrue(list.isStale());
	}

	public void testDependency_FiresListChange() {
		assertEquals(list.nextComputation, list);

		Object element = new Object();
		list.nextComputation.add(element);

		list.dependency.fireChange();

		List expectedList = new ArrayList();
		expectedList.add(element);
		assertEquals(expectedList, list);
	}

	public void testDependency_NoStaleEventIfAlreadyDirty() {
		list.dependency.fireChange();
		list.addStaleListener(new IStaleListener() {
			public void handleStale(StaleEvent staleEvent) {
				fail("Should not fire stale when list is already dirty");
			}
		});
		list.dependency.fireStale();
	}

	public void testDependency_ListChangeEventFiresOnlyWhenNotDirty() {
		ListChangeEventTracker tracker = ListChangeEventTracker.observe(list);

		list.dependency.fireChange();
		assertEquals(
				"ComputedList should fire list change event when its dependency changes",
				1, tracker.count);

		list.dependency.fireChange();
		assertEquals(
				"ComputedList should not fire list change events when dirty",
				1, tracker.count);

		list.size(); // Force list to recompute.
		list.dependency.fireChange();
		assertEquals(
				"ComputedList should fire list change event when its dependency changes",
				2, tracker.count);
	}

	static class ComputedListStub extends ComputedList {
		List nextComputation = new ArrayList();
		ObservableStub dependency;

		ComputedListStub() {
			this(Realm.getDefault());
		}

		ComputedListStub(Realm realm) {
			super(realm);
			dependency = new ObservableStub(realm);
		}

		protected List calculate() {
			ObservableTracker.getterCalled(dependency);
			return new ArrayList(nextComputation);
		}
	}

	static class ObservableStub extends AbstractObservable {
		public ObservableStub(Realm realm) {
			super(realm);
		}

		boolean stale;

		public boolean isStale() {
			return stale;
		}

		protected void fireStale() {
			stale = true;
			super.fireStale();
		}

		protected void fireChange() {
			super.fireChange();
		}
	}

	public static Test suite() {
		TestSuite suite = new TestSuite(ComputedListTest.class.getName());
		suite.addTestSuite(ComputedListTest.class);
		suite.addTest(ObservableListContractTest.suite(new Delegate()));
		return suite;
	}

	static class Delegate extends AbstractObservableCollectionContractDelegate {
		public IObservableCollection createObservableCollection(Realm realm,
				int elementCount) {
			final ComputedListStub list = new ComputedListStub(realm);
			for (int i = 0; i < elementCount; i++)
				list.nextComputation.add(createElement(list));
			list.size(); // force list to compute
			return list;
		}

		public void change(IObservable observable) {
			ComputedListStub list = (ComputedListStub) observable;
			list.nextComputation.add(new Object());
			list.dependency.fireChange();
		}

		public void setStale(IObservable observable, boolean stale) {
			if (stale)
				((ComputedListStub) observable).dependency.fireStale();
			else {
				ComputedListStub computedList = (ComputedListStub) observable;
				computedList.dependency.stale = false;
				computedList.dependency.fireChange();
			}
		}
	}
}
