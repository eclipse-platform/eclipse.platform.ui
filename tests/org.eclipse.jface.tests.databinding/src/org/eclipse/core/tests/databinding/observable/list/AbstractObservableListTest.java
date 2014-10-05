/*******************************************************************************
 * Copyright (c) 2006, 2008 Brad Reynolds and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Brad Reynolds - initial API and implementation
 *     Brad Reynolds - bug 167204
 *     Matthew Hall - bugs 208858, 213145, 247367, 349038
 ******************************************************************************/

package org.eclipse.core.tests.databinding.observable.list;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.core.databinding.observable.ChangeEvent;
import org.eclipse.core.databinding.observable.Diffs;
import org.eclipse.core.databinding.observable.DisposeEvent;
import org.eclipse.core.databinding.observable.IChangeListener;
import org.eclipse.core.databinding.observable.IDisposeListener;
import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.IObservableCollection;
import org.eclipse.core.databinding.observable.IStaleListener;
import org.eclipse.core.databinding.observable.ObservableTracker;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.StaleEvent;
import org.eclipse.core.databinding.observable.list.AbstractObservableList;
import org.eclipse.core.databinding.observable.list.IListChangeListener;
import org.eclipse.core.databinding.observable.list.ListChangeEvent;
import org.eclipse.core.databinding.observable.list.ListDiff;
import org.eclipse.core.databinding.observable.list.ListDiffEntry;
import org.eclipse.jface.databinding.conformance.ObservableListContractTest;
import org.eclipse.jface.databinding.conformance.delegate.AbstractObservableCollectionContractDelegate;
import org.eclipse.jface.databinding.conformance.util.CurrentRealm;
import org.eclipse.jface.databinding.conformance.util.RealmTester;

/**
 * @since 3.2
 */
public class AbstractObservableListTest extends TestCase {
	private AbstractObservableListStub list;

	@Override
	protected void setUp() throws Exception {
		RealmTester.setDefault(new CurrentRealm(true));
		list = new AbstractObservableListStub();
	}

	@Override
	protected void tearDown() throws Exception {
		RealmTester.setDefault(null);
	}

	public void testFireChangeRealmChecks() throws Exception {
		RealmTester.exerciseCurrent(new Runnable() {
			@Override
			public void run() {
				list.fireChange();
			}
		});
	}

	public void testFireStaleRealmChecks() throws Exception {
		RealmTester.exerciseCurrent(new Runnable() {
			@Override
			public void run() {
				list.fireStale();
			}
		});
	}

	public void testFireListChangeRealmChecks() throws Exception {
		RealmTester.exerciseCurrent(new Runnable() {
			@Override
			public void run() {
				list.fireListChange(null);
			}
		});
	}

	public void testMove_FiresListChanges() throws Exception {
		list = new MutableObservableListStub();
		final Object element = new Object();
		list.add(element);
		list.add(new Object());

		final List diffEntries = new ArrayList();
		list.addListChangeListener(new IListChangeListener() {
			@Override
			public void handleListChange(ListChangeEvent event) {
				diffEntries.addAll(Arrays.asList(event.diff.getDifferences()));
			}
		});

		list.move(0, 1);

		assertEquals(2, diffEntries.size());

		ListDiffEntry entry = (ListDiffEntry) diffEntries.get(0);
		assertEquals(element, entry.getElement());
		assertEquals(false, entry.isAddition());
		assertEquals(0, entry.getPosition());

		entry = (ListDiffEntry) diffEntries.get(1);
		assertEquals(element, entry.getElement());
		assertEquals(true, entry.isAddition());
		assertEquals(1, entry.getPosition());
	}

	public void testMove_MovesElement() throws Exception {
		list = new MutableObservableListStub();
		final Object element0 = new Object();
		final Object element1 = new Object();
		list.add(element0);
		list.add(element1);

		list.move(0, 1);

		assertEquals(element1, list.get(0));
		assertEquals(element0, list.get(1));
	}

	public void testAddListChangeListener_AfterDispose() {
		list.dispose();
		list.addListChangeListener(new IListChangeListener() {
			@Override
			public void handleListChange(ListChangeEvent event) {
				// do nothing
			}
		});
	}

	public void testRemoveListChangeListener_AfterDispose() {
		list.dispose();
		list.removeListChangeListener(new IListChangeListener() {
			@Override
			public void handleListChange(ListChangeEvent event) {
				// do nothing
			}
		});
	}

	public void testAddChangeListener_AfterDispose() {
		list.dispose();
		list.addChangeListener(new IChangeListener() {
			@Override
			public void handleChange(ChangeEvent event) {
				// do nothing
			}
		});
	}

	public void testRemoveChangeListener_AfterDispose() {
		list.dispose();
		list.removeChangeListener(new IChangeListener() {
			@Override
			public void handleChange(ChangeEvent event) {
				// do nothing
			}
		});
	}

	public void testAddStaleListener_AfterDispose() {
		list.dispose();
		list.addStaleListener(new IStaleListener() {
			@Override
			public void handleStale(StaleEvent staleEvent) {
				// do nothing
			}
		});
	}

	public void testRemoveStaleListener_AfterDispose() {
		list.dispose();
		list.removeStaleListener(new IStaleListener() {
			@Override
			public void handleStale(StaleEvent staleEvent) {
				// do nothing
			}
		});
	}

	public void testAddDisposeListener_AfterDispose() {
		list.dispose();
		list.addDisposeListener(new IDisposeListener() {
			@Override
			public void handleDispose(DisposeEvent event) {
				// do nothing
			}
		});
	}

	public void testRemoveDisposeListener_AfterDispose() {
		list.dispose();
		list.removeDisposeListener(new IDisposeListener() {
			@Override
			public void handleDispose(DisposeEvent event) {
				// do nothing
			}
		});
	}

	public void testHasListeners_AfterDispose() {
		list.dispose();
		list.hasListeners();
	}

	public static Test suite() {
		TestSuite suite = new TestSuite(AbstractObservableListTest.class.getName());
		suite.addTestSuite(AbstractObservableListTest.class);
		suite.addTest(ObservableListContractTest.suite(new Delegate()));
		return suite;
	}

	/* package */static class Delegate extends
			AbstractObservableCollectionContractDelegate {

		@Override
		public IObservableCollection createObservableCollection(Realm realm,
				final int itemCount) {

			String[] items = new String[itemCount];
			for (int i = 0; i < itemCount; i++) {
				items[i] = String.valueOf(i);
			}

			AbstractObservableListStub observable = new AbstractObservableListStub(realm, Arrays.asList(items));
			observable.elementType = String.class;
			return observable;
		}

		@Override
		public Object getElementType(IObservableCollection collection) {
			return String.class;
		}

		@Override
		public void change(IObservable observable) {
			((AbstractObservableListStub) observable).fireChange();
		}
	}

	static class AbstractObservableListStub extends AbstractObservableList {
		Object elementType;

		List wrappedList;

		public AbstractObservableListStub() {
			super();
			wrappedList = new ArrayList();
		}

		public AbstractObservableListStub(Realm realm, List list) {
			super(realm);
			this.wrappedList = list;
		}

		@Override
		protected int doGetSize() {
			return wrappedList.size();
		}

		@Override
		public Object get(int index) {
			ObservableTracker.getterCalled(this);
			return wrappedList.get(index);
		}

		@Override
		public Object getElementType() {
			return elementType;
		}

		@Override
		protected void fireChange() {
			super.fireChange();
		}

		@Override
		protected void fireStale() {
			super.fireStale();
		}

		@Override
		protected void fireListChange(ListDiff diff) {
			super.fireListChange(diff);
		}

		@Override
		protected synchronized boolean hasListeners() {
			return super.hasListeners();
		}
	}

	static class MutableObservableListStub extends AbstractObservableListStub {
		// These methods are present so we can test AbstractObservableList.move()

		@Override
		public void add(int index, Object element) {
			checkRealm();
			wrappedList.add(index, element);
			fireListChange(Diffs.createListDiff(Diffs.createListDiffEntry(
					index, true, element)));
		}

		@Override
		public Object remove(int index) {
			checkRealm();
			Object element = wrappedList.remove(index);
			fireListChange(Diffs.createListDiff(Diffs.createListDiffEntry(
					index, false, element)));
			return element;
		}
	}
}
