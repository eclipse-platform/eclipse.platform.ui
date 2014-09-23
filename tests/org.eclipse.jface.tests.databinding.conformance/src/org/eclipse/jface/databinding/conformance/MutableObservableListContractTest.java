/*******************************************************************************
 * Copyright (c) 2007, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Matthew Hall - bugs 208858, 221351, 213145, 244098
 *     Simon Scholz <simon.scholz@vogella.com> - Bug 444829
 ******************************************************************************/

package org.eclipse.jface.databinding.conformance;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import junit.framework.Test;

import org.eclipse.core.databinding.observable.IObservablesListener;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.ListDiff;
import org.eclipse.jface.databinding.conformance.delegate.IObservableCollectionContractDelegate;
import org.eclipse.jface.databinding.conformance.util.ChangeEventTracker;
import org.eclipse.jface.databinding.conformance.util.ListChangeEventTracker;
import org.eclipse.jface.databinding.conformance.util.SuiteBuilder;
import org.junit.Assert;

/**
 * Mutability tests for IObservableList.
 *
 * <p>
 * This class is experimental and can change at any time. It is recommended to
 * not subclass or assume the test names will not change. The only API that is
 * guaranteed to not change are the constructors. The tests will remain public
 * and not final in order to allow for consumers to turn off a test if needed by
 * subclassing.
 * </p>
 * 
 * @since 3.2
 */
public class MutableObservableListContractTest extends
		MutableObservableCollectionContractTest {
	private IObservableCollectionContractDelegate delegate;

	private IObservableList list;

	/**
	 * @param delegate
	 */
	public MutableObservableListContractTest(
			IObservableCollectionContractDelegate delegate) {
		super(delegate);
		this.delegate = delegate;
	}

	public MutableObservableListContractTest(String testName,
			IObservableCollectionContractDelegate delegate) {
		super(testName, delegate);
		this.delegate = delegate;
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		list = (IObservableList) getObservable();
	}

	public void testAdd_ListChangeEvent() throws Exception {
		final Object element = delegate.createElement(list);
		assertListChangeEventFired(new Runnable() {
			@Override
			public void run() {
				list.add(element);
			}
		}, "List.add(Object)", list, Collections.singletonList(element));
	}

	public void testAdd_ListDiffEntry() throws Exception {
		Object element0 = delegate.createElement(list);
		list.add(element0);
		final Object element1 = delegate.createElement(list);

		assertListChangeEventFired(new Runnable() {
			@Override
			public void run() {
				list.add(element1);
			}
		}, "List.add(Object)", list,
				Arrays.asList(new Object[] { element0, element1 }));
	}

	public void testAddAtIndex_ChangeEvent() throws Exception {
		assertChangeEventFired(new Runnable() {
			@Override
			public void run() {
				list.add(0, delegate.createElement(list));
			}
		}, "List.add(int, Object)", list);
	}

	public void testAddAtIndex_ListChangeEvent() throws Exception {
		final Object element = delegate.createElement(list);
		assertListChangeEventFired(new Runnable() {
			@Override
			public void run() {
				list.add(0, element);
			}
		}, "List.add(int, Object)", list, Collections.singletonList(element));
	}

	public void testAddAtIndex_ChangeEventFiredAfterElementIsAdded()
			throws Exception {
		final Object element = delegate.createElement(list);

		assertContainsDuringChangeEvent(new Runnable() {
			@Override
			public void run() {
				list.add(0, element);
			}
		}, "List.add(int, Collection)", list, element);
	}

	public void testAddAtIndex_ListDiffEntry() throws Exception {
		Object element0 = delegate.createElement(list);
		list.add(element0);
		final Object element1 = delegate.createElement(list);

		assertListChangeEventFired(new Runnable() {
			@Override
			public void run() {
				list.add(1, element1);
			}
		}, "List.add(int, Object)", list,
				Arrays.asList(new Object[] { element0, element1 }));
	}

	public void testAddAll_ListChangeEvent() throws Exception {
		final Object element = delegate.createElement(list);
		assertListChangeEventFired(new Runnable() {
			@Override
			public void run() {
				list.addAll(Collections.singletonList(element));
			}
		}, "List.addAll(Collection", list, Collections.singletonList(element));
	}

	public void testAddAll_ListDiffEntry() throws Exception {
		final Object element = delegate.createElement(list);

		assertListChangeEventFired(new Runnable() {
			@Override
			public void run() {
				list.addAll(Collections.singletonList(element));
			}
		}, "List.addAll(Collection)", list, Collections.singletonList(element));
	}

	public void testAddAll_ListDiffEntry2() throws Exception {
		final Object element0 = delegate.createElement(list);
		list.add(element0);
		final Object element1 = delegate.createElement(list);

		assertListChangeEventFired(new Runnable() {
			@Override
			public void run() {
				list.addAll(Collections.singletonList(element1));
			}
		}, "List.addAll(Collection)", list,
				Arrays.asList(new Object[] { element0, element1 }));
	}

	public void testAddAllAtIndex_ChangeEvent() throws Exception {
		assertChangeEventFired(new Runnable() {
			@Override
			public void run() {
				list.addAll(0, Arrays.asList(new Object[] { delegate
						.createElement(list) }));
			}
		}, "List.addAll(int, Collection)", list);
	}

	public void testAddAllAtIndex_ListChangeEvent() throws Exception {
		final Object element = delegate.createElement(list);
		assertListChangeEventFired(new Runnable() {
			@Override
			public void run() {
				list.addAll(0, Collections.singletonList(element));
			}
		}, "List.addAll(int, Collection)", list,
				Collections.singletonList(element));
	}

	public void testAddAllAtIndex_ChangeEventFiredAfterElementIsAdded()
			throws Exception {
		final Object element = delegate.createElement(list);

		assertContainsDuringChangeEvent(new Runnable() {
			@Override
			public void run() {
				list.addAll(0, Arrays.asList(new Object[] { element }));
			}
		}, "List.addAll(int, Collection)", list, element);
	}

	public void testAddAllAtIndex_ListDiffEntry() throws Exception {
		Object element0 = delegate.createElement(list);
		list.add(element0);
		final Object element1 = delegate.createElement(list);

		assertListChangeEventFired(new Runnable() {
			@Override
			public void run() {
				list.addAll(1, Collections.singletonList(element1));
			}
		}, "List.addAll(int, Collection)", list,
				Arrays.asList(new Object[] { element0, element1 }));
	}

	public void testSet_ChangeEvent() throws Exception {
		list.add(delegate.createElement(list));

		assertChangeEventFired(new Runnable() {
			@Override
			public void run() {
				list.set(0, delegate.createElement(list));
			}
		}, "List.set(int, Object)", list);
	}

	public void testSet_ListChangeEvent() throws Exception {
		final Object element0 = delegate.createElement(list);
		list.add(element0);
		final Object element1 = delegate.createElement(list);

		assertListChangeEventFired(new Runnable() {
			@Override
			public void run() {
				assertSame(element0, list.set(0, element1));
			}
		}, "List.set(int, Object)", list,
				Arrays.asList(new Object[] { element1 }));
	}

	public void testSet_ChangeEventFiredAfterElementIsSet() throws Exception {
		final Object element1 = delegate.createElement(list);
		list.add(element1);
		final Object element2 = delegate.createElement(list);

		assertContainsDuringChangeEvent(new Runnable() {
			@Override
			public void run() {
				assertSame(element1, list.set(0, element2));
			}
		}, "List.set(int, Object)", list, element2);
	}

	public void testSet_ListChangeEvent2() throws Exception {
		Object element0 = delegate.createElement(list);
		list.add(element0);
		Object oldElement1 = delegate.createElement(list);
		list.add(oldElement1);
		final Object newElement1 = delegate.createElement(list);

		assertListChangeEventFired(new Runnable() {
			@Override
			public void run() {
				list.set(1, newElement1);
			}
		}, "List.set(int, Object)", list,
				Arrays.asList(new Object[] { element0, newElement1 }));
	}

	public void testMove_ChangeEvent() throws Exception {
		list.add(delegate.createElement(list));
		list.add(delegate.createElement(list));

		assertChangeEventFired(new Runnable() {
			@Override
			public void run() {
				list.move(0, 1);
			}
		}, "IObservableList.move(int, int)", list);
	}

	public void testMove_NoChangeEventAtSameIndex() throws Exception {
		Object element = delegate.createElement(list);
		list.add(element);

		ListChangeEventTracker tracker = ListChangeEventTracker.observe(list);

		final Object movedElement = list.move(0, 0);

		assertEquals(
				formatFail("IObservableList.move(int,int) should return the moved element"),
				element, movedElement);
		assertEquals(
				formatFail("IObservableLIst.move(int,int) should not fire a change event"
						+ "when the old and new indices are the same"), 0,
				tracker.count);
	}

	public void testMove_ListChangeEvent() throws Exception {
		final Object element0 = delegate.createElement(list);
		list.add(element0);
		final Object element1 = delegate.createElement(list);
		list.add(element1);

		assertListChangeEventFired(new Runnable() {
			@Override
			public void run() {
				assertSame(element0, list.move(0, 1));
			}
		}, "IObservableList.move(int, int)", list,
				Arrays.asList(new Object[] { element1, element0 }));
	}

	public void testMove_ChangeEventFiredAfterElementIsMoved() throws Exception {
		Object element0 = delegate.createElement(list);
		Object element1 = delegate.createElement(list);
		list.add(element0);
		list.add(element1);

		assertSame(element0, list.get(0));
		assertSame(element1, list.get(1));

		list.move(0, 1);

		assertSame(element1, list.get(0));
		assertSame(element0, list.get(1));
	}

	public void testMove_ListChangeEvent2() {
		Object element0 = delegate.createElement(list);
		list.add(element0);
		Object element1 = delegate.createElement(list);
		list.add(element1);

		assertListChangeEventFired(new Runnable() {
			@Override
			public void run() {
				list.move(0, 1);
			}
		}, "IObservableList.move(int, int)", list,
				Arrays.asList(new Object[] { element1, element0 }));
	}

	public void testRemove_ListChangeEvent() throws Exception {
		final Object element = delegate.createElement(list);
		list.add(element);

		assertListChangeEventFired(new Runnable() {
			@Override
			public void run() {
				list.remove(element);
			}
		}, "List.remove(Object)", list, Collections.EMPTY_LIST);
	}

	public void testRemove_ListDiffEntry() throws Exception {
		final Object element0 = delegate.createElement(list);
		list.add(element0);
		final Object element1 = delegate.createElement(list);
		list.add(element1);

		assertListChangeEventFired(new Runnable() {
			@Override
			public void run() {
				list.remove(element1);
			}
		}, "List.remove(Object)", list, Collections.singletonList(element0));
	}

	public void testRemoveAtIndex_ChangeEvent() throws Exception {
		list.add(delegate.createElement(list));

		assertChangeEventFired(new Runnable() {
			@Override
			public void run() {
				list.remove(0);
			}
		}, "List.remove(int)", list);
	}

	public void testRemoveAtIndex_ListChangeEvent() throws Exception {
		list.add(delegate.createElement(list));

		assertListChangeEventFired(new Runnable() {
			@Override
			public void run() {
				list.remove(0);
			}
		}, "List.remove(int)", list, Collections.EMPTY_LIST);
	}

	public void testRemoveAtIndex_ChangeEventFiredAfterElementIsRemoved()
			throws Exception {
		final Object element = delegate.createElement(list);
		list.add(element);

		assertDoesNotContainDuringChangeEvent(new Runnable() {
			@Override
			public void run() {
				list.remove(0);
			}
		}, "List.remove(int)", list, element);
	}

	public void testRemoveAtIndex_ListDiffEntry() throws Exception {
		Object element0 = delegate.createElement(list);
		list.add(element0);
		Object element1 = delegate.createElement(list);
		list.add(element1);

		assertListChangeEventFired(new Runnable() {
			@Override
			public void run() {
				list.remove(1);
			}
		}, "List.remove(int)", list, Collections.singletonList(element0));
	}

	public void testRemoveAll_ListChangeEvent() throws Exception {
		final Object element = delegate.createElement(list);
		list.add(element);

		assertListChangeEventFired(new Runnable() {
			@Override
			public void run() {
				list.removeAll(Collections.singletonList(element));
			}
		}, "List.removeAll(Collection)", list, Collections.EMPTY_LIST);
	}

	public void testRemoveAll_ListDiffEntry() throws Exception {
		final Object element = delegate.createElement(list);
		list.add(element);

		assertListChangeEventFired(new Runnable() {
			@Override
			public void run() {
				list.removeAll(Collections.singletonList(element));
			}
		}, "List.removeAll(Collection)", list, Collections.EMPTY_LIST);
	}

	public void testRemoveAll_ListDiffEntry2() throws Exception {
		Object element0 = delegate.createElement(list);
		list.add(element0);
		final Object element1 = delegate.createElement(list);
		list.add(element1);

		assertListChangeEventFired(new Runnable() {
			@Override
			public void run() {
				list.removeAll(Arrays.asList(new Object[] { element1 }));
			}
		}, "List.removeAll(Collection)", list,
				Collections.singletonList(element0));
	}

	public void testRetainAll_ListChangeEvent() throws Exception {
		final Object element0 = delegate.createElement(list);
		list.add(element0);
		list.add(delegate.createElement(list));

		assertListChangeEventFired(new Runnable() {
			@Override
			public void run() {
				list.retainAll(Arrays.asList(new Object[] { element0 }));
			}
		}, "List.retainAll(Collection", list,
				Collections.singletonList(element0));
	}

	public void testRetainAll_ListDiffEntry() throws Exception {
		final Object element = delegate.createElement(list);
		list.add(element);
		list.add(delegate.createElement(list));

		assertListChangeEventFired(new Runnable() {
			@Override
			public void run() {
				list.retainAll(Arrays.asList(new Object[] { element }));
			}
		}, "List.retainAll(Collection)", list,
				Collections.singletonList(element));
	}

	public void testClear_ListChangeEvent() throws Exception {
		list.add(delegate.createElement(list));

		assertListChangeEventFired(new Runnable() {
			@Override
			public void run() {
				list.clear();
			}
		}, "List.clear()", list, Collections.EMPTY_LIST);
	}

	public void testClear_ListDiffEntry() throws Exception {
		list.add(delegate.createElement(list));

		assertListChangeEventFired(new Runnable() {
			@Override
			public void run() {
				list.clear();
			}
		}, "List.clear()", list, Collections.EMPTY_LIST);
	}

	public void testClear_ClearsList() {
		Object element = delegate.createElement(list);
		list.add(element);
		Assert.assertEquals(Collections.singletonList(element), list);
		list.clear();
		Assert.assertEquals(Collections.EMPTY_LIST, list);
	}

	private void assertListChangeEventFired(Runnable runnable,
			String methodName, IObservableList list, List newList) {
		List oldList = new ArrayList(list);

		List<IObservablesListener> queue = new ArrayList<IObservablesListener>();
		ListChangeEventTracker listListener = new ListChangeEventTracker(queue);
		ChangeEventTracker changeListener = new ChangeEventTracker(queue);

		list.addListChangeListener(listListener);
		list.addChangeListener(changeListener);

		runnable.run();

		assertEquals(formatFail(methodName
				+ " should fire one ListChangeEvent."), 1, listListener.count);
		assertEquals(formatFail(methodName
				+ "'s change event observable should be the created List."),
				list, listListener.event.getObservable());

		assertEquals(
				formatFail("Two notifications should have been received."), 2,
				queue.size());
		assertEquals("ChangeEvent of " + methodName
				+ " should have fired before the ListChangeEvent.",
				changeListener, queue.get(0));
		assertEquals("ListChangeEvent of " + methodName
				+ " should have fired after the ChangeEvent.", listListener,
				queue.get(1));

		assertEquals(formatFail(methodName
				+ " did not leave observable list with the expected contents"),
				newList, list);

		ListDiff diff = listListener.event.diff;
		diff.applyTo(oldList);
		assertEquals(
				formatFail(methodName
						+ " fired a diff which does not represent the expected list change"),
				newList, oldList);

	}

	public static Test suite(IObservableCollectionContractDelegate delegate) {
		return new SuiteBuilder()
				.addObservableContractTest(
						MutableObservableListContractTest.class, delegate)
				.addObservableContractTest(ObservableListContractTest.class,
						delegate).build();
	}
}
