/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.jface.databinding.conformance;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.ListDiffEntry;
import org.eclipse.jface.databinding.conformance.delegate.IObservableCollectionContractDelegate;
import org.eclipse.jface.databinding.conformance.util.ChangeEventTracker;
import org.eclipse.jface.databinding.conformance.util.ListChangeEventTracker;


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

	protected void setUp() throws Exception {
		super.setUp();
		list = (IObservableList) getObservable();
	}

	public void testAdd_ListChangeEvent() throws Exception {
		assertListChangeEventFired(new Runnable() {
			public void run() {
				list.add(delegate.createElement(list));
			}
		}, "List.add(Object)", list);
	}

	public void testAdd_ListDiffEntry() throws Exception {
		list.add(delegate.createElement(list));
		final Object element = delegate.createElement(list);

		assertAddDiffEntry(new Runnable() {
			public void run() {
				list.add(element);
			}
		}, "List.add(Object)", list, element, 1);
	}

	public void testAddAtIndex_ChangeEvent() throws Exception {
		assertChangeEventFired(new Runnable() {
			public void run() {
				list.add(0, delegate.createElement(list));
			}
		}, "List.add(int, Object)", list);
	}

	public void testAddAtIndex_ListChangeEvent() throws Exception {
		assertListChangeEventFired(new Runnable() {
			public void run() {
				list.add(0, delegate.createElement(list));
			}
		}, "List.add(int, Object)", list);
	}

	public void testAddAtIndex_ChangeEventFiredAfterElementIsAdded()
			throws Exception {
		final Object element = delegate.createElement(list);

		assertContainsDuringChangeEvent(new Runnable() {
			public void run() {
				list.add(0, element);
			}
		}, "List.add(int, Collection)", list, element);
	}

	public void testAddAtIndex_ListDiffEntry() throws Exception {
		list.add(delegate.createElement(list));
		final Object element = delegate.createElement(list);

		assertAddDiffEntry(new Runnable() {
			public void run() {
				list.add(1, element);
			}
		}, "List.add(int, Object)", list, element, 1);
	}

	public void testAddAll_ListChangeEvent() throws Exception {
		assertListChangeEventFired(new Runnable() {
			public void run() {
				list.addAll(Arrays.asList(new Object[] { delegate
						.createElement(list) }));
			}
		}, "List.addAll(Collection", list);
	}

	public void testAddAll_ListDiffEntry() throws Exception {
		final Object element = delegate.createElement(list);

		assertAddDiffEntry(new Runnable() {
			public void run() {
				list.addAll(Arrays.asList(new Object[] { element }));
			}
		}, "List.addAll(Collection)", list, element, 0);
	}

	public void testAddAllAtIndex_ChangeEvent() throws Exception {
		assertChangeEventFired(new Runnable() {
			public void run() {
				list.addAll(0, Arrays.asList(new Object[] { delegate
						.createElement(list) }));
			}
		}, "List.addAll(int, Collection)", list);
	}

	public void testAddAllAtIndex_ListChangeEvent() throws Exception {
		assertListChangeEventFired(new Runnable() {
			public void run() {
				list.addAll(0, Arrays.asList(new Object[] { delegate
						.createElement(list) }));
			}
		}, "List.addAll(int, Collection)", list);
	}

	public void testAddAllAtIndex_ChangeEventFiredAfterElementIsAdded()
			throws Exception {
		final Object element = delegate.createElement(list);

		assertContainsDuringChangeEvent(new Runnable() {
			public void run() {
				list.addAll(0, Arrays.asList(new Object[] { element }));
			}
		}, "List.addAll(int, Collection)", list, element);
	}

	public void testAddAllAtIndex_ListDiffEntry() throws Exception {
		list.add(delegate.createElement(list));
		final Object element = delegate.createElement(list);

		assertAddDiffEntry(new Runnable() {
			public void run() {
				list.addAll(1, Arrays.asList(new Object[] { element }));
			}
		}, "List.addAll(int, Collection)", list, element, 1);
	}

	public void testSet_ChangeEvent() throws Exception {
		list.add(delegate.createElement(list));

		assertChangeEventFired(new Runnable() {
			public void run() {
				list.set(0, delegate.createElement(list));
			}
		}, "List.set(int, Object)", list);
	}

	public void testSet_ListChangeEvent() throws Exception {
		list.add(delegate.createElement(list));

		assertListChangeEventFired(new Runnable() {
			public void run() {
				list.set(0, delegate.createElement(list));
			}
		}, "List.set(int, Object)", list);
	}

	public void testSet_ChangeEventFiredAfterElementIsSet() throws Exception {
		Object element1 = delegate.createElement(list);
		list.add(element1);
		final Object element2 = delegate.createElement(list);

		assertContainsDuringChangeEvent(new Runnable() {
			public void run() {
				list.set(0, element2);
			}
		}, "List.set(int, Object)", list, element2);
	}

	public void testSet_ListDiffEntry() throws Exception {
		list.add(delegate.createElement(list));
		Object oldElement = delegate.createElement(list);
		list.add(oldElement);

		ListChangeEventTracker listener = ListChangeEventTracker.observe(list);

		Object newElement = delegate.createElement(list);
		list.set(1, newElement);

		ListDiffEntry[] entries = listener.event.diff.getDifferences();
		assertEquals(
				"List.set(int, Object) should result in 2 list diff entries.",
				2, entries.length);

		ListDiffEntry add = null;
		ListDiffEntry remove = null;

		if (entries[0].isAddition() && !entries[1].isAddition()) {
			add = entries[0];
			remove = entries[1];
		} else if (!entries[0].isAddition() && entries[1].isAddition()) {
			add = entries[1];
			remove = entries[0];
		} else {
			fail("List.set(int, Object) should result in an add and a remove entry.");
		}

		assertEquals(
				"List.set(int, Object) removed element should be the old element.",
				oldElement, remove.getElement());
		assertEquals(
				"List.set(int, Object) removed index should be the index the new element was set at.",
				1, remove.getPosition());

		assertEquals(
				"List.set(int, Object) added element should be the set element.",
				newElement, add.getElement());
		assertEquals(
				"List.set(int, Object) add index should be the index the new element was set at.",
				1, add.getPosition());
	}

	public void testRemove_ListChangeEvent() throws Exception {
		final Object element = delegate.createElement(list);
		list.add(element);

		assertListChangeEventFired(new Runnable() {
			public void run() {
				list.remove(element);
			}
		}, "List.remove(Object)", list);
	}

	public void testRemove_ListDiffEntry() throws Exception {
		list.add(delegate.createElement(list));
		final Object element = delegate.createElement(list);
		list.add(element);

		assertRemoveDiffEntry(new Runnable() {
			public void run() {
				list.remove(element);
			}
		}, "List.remove(Object)", list, element, 1);
	}

	public void testRemoveAtIndex_ChangeEvent() throws Exception {
		list.add(delegate.createElement(list));

		assertChangeEventFired(new Runnable() {
			public void run() {
				list.remove(0);
			}
		}, "List.remove(int)", list);
	}

	public void testRemoveAtIndex_ListChangeEvent() throws Exception {
		list.add(delegate.createElement(list));

		assertListChangeEventFired(new Runnable() {
			public void run() {
				list.remove(0);
			}
		}, "List.remove(int)", list);
	}

	public void testRemoveAtIndex_ChangeEventFiredAfterElementIsRemoved()
			throws Exception {
		final Object element = delegate.createElement(list);
		list.add(element);

		assertDoesNotContainDuringChangeEvent(new Runnable() {
			public void run() {
				list.remove(0);
			}
		}, "List.remove(int)", list, element);
	}

	public void testRemoveAtIndex_ListDiffEntry() throws Exception {
		list.add(delegate.createElement(list));
		Object element = delegate.createElement(list);
		list.add(element);

		assertRemoveDiffEntry(new Runnable() {
			public void run() {
				list.remove(1);
			}
		}, "List.remove(int)", list, element, 1);
	}

	public void testRemoveAll_ListChangeEvent() throws Exception {
		final Object element = delegate.createElement(list);

		assertListChangeEventFired(new Runnable() {
			public void run() {
				list.removeAll(Arrays.asList(new Object[] { element }));
			}
		}, "List.removeAll(Collection)", list);
	}

	public void testRemoveAll_ListDiffEntry() throws Exception {
		final Object element = delegate.createElement(list);
		list.add(element);

		assertRemoveDiffEntry(new Runnable() {
			public void run() {
				list.removeAll(Arrays.asList(new Object[] { element }));
			}
		}, "List.removeAll(Collection)", list, element, 0);
	}

	public void testRetainAll_ListChangeEvent() throws Exception {
		final Object element1 = delegate.createElement(list);
		list.add(element1);
		list.add(delegate.createElement(list));

		assertListChangeEventFired(new Runnable() {
			public void run() {
				list.retainAll(Arrays.asList(new Object[] { element1 }));
			}
		}, "List.retainAll(Collection", list);
	}

	public void testRetainAll_ListDiffEntry() throws Exception {
		final Object element1 = delegate.createElement(list);
		list.add(element1);
		Object element2 = delegate.createElement(list);
		list.add(delegate.createElement(list));

		assertRemoveDiffEntry(new Runnable() {
			public void run() {
				list.retainAll(Arrays.asList(new Object[] { element1 }));
			}
		}, "List.retainAll(Collection)", list, element2, 1);
	}

	public void testClear_ListChangeEvent() throws Exception {
		list.add(delegate.createElement(list));

		assertListChangeEventFired(new Runnable() {
			public void run() {
				list.clear();
			}
		}, "List.clear()", list);
	}

	public void testClear_ListDiffEntry() throws Exception {
		Object element = delegate.createElement(list);
		list.add(element);

		assertRemoveDiffEntry(new Runnable() {
			public void run() {
				list.clear();
			}
		}, "List.clear()", list, element, 0);
	}

	/**
	 * Asserts standard behaviors of firing list change events.
	 * <ul>
	 * <li>Event fires once.</li>
	 * <li>Source of the event is the provided <code>list</code>.
	 * <li>The list change event is fired after the change event.</li>
	 * </ul>
	 * 
	 * @param runnable
	 * @param methodName
	 * @param list
	 */
	private void assertListChangeEventFired(Runnable runnable,
			String methodName, IObservableList list) {
		List queue = new ArrayList();
		ListChangeEventTracker listListener = new ListChangeEventTracker(queue);
		ChangeEventTracker changeListener = new ChangeEventTracker(queue);

		list.addListChangeListener(listListener);
		list.addChangeListener(changeListener);

		runnable.run();

		assertEquals(formatFail(methodName + " should fire one ListChangeEvent."), 1,
				listListener.count);
		assertEquals(formatFail(methodName
				+ "'s change event observable should be the created List."),
				list, listListener.event.getObservable());

		assertEquals(formatFail("Two notifications should have been received."), 2, queue
				.size());
		assertEquals("ChangeEvent of " + methodName
				+ " should have fired before the ListChangeEvent.",
				changeListener, queue.get(0));
		assertEquals("ListChangeEvent of " + methodName
				+ " should have fired after the ChangeEvent.", listListener,
				queue.get(1));
	}

	/**
	 * Asserts the list diff entry for a remove operation.
	 * 
	 * @param runnable
	 * @param methodName
	 * @param list
	 * @param element
	 * @param index
	 */
	private void assertRemoveDiffEntry(Runnable runnable, String methodName,
			IObservableList list, Object element, int index) {
		ListChangeEventTracker listener = new ListChangeEventTracker();
		list.addListChangeListener(listener);

		runnable.run();

		ListDiffEntry[] entries = listener.event.diff.getDifferences();
		assertEquals(methodName + " should result in one diff entry.", 1,
				entries.length);

		ListDiffEntry entry = entries[0];
		assertFalse(methodName
				+ " should result in a diff entry that is an removal.", entry
				.isAddition());
		assertEquals(methodName
				+ " remove diff entry should have removed the element.",
				element, entry.getElement());
		assertEquals(
				methodName
						+ " remove diff entry should have removed the element from the provided index.",
				index, entry.getPosition());
	}

	/**
	 * Asserts the list diff entry for an add operation.
	 * 
	 * @param runnable
	 * @param methodName
	 * @param list
	 * @param element
	 * @param index
	 */
	private void assertAddDiffEntry(Runnable runnable, String methodName,
			IObservableList list, Object element, int index) {
		ListChangeEventTracker listener = new ListChangeEventTracker();
		list.addListChangeListener(listener);

		runnable.run();

		ListDiffEntry[] entries = listener.event.diff.getDifferences();
		assertEquals(methodName + " should result in one diff entry.", 1,
				entries.length);

		ListDiffEntry entry = entries[0];
		assertTrue(methodName
				+ " should result in a diff entry that is an addition.", entry
				.isAddition());
		assertEquals(methodName
				+ " add diff entry should have added the element.", element,
				entry.getElement());
		assertEquals(
				methodName
						+ "add diff entry should have added the element at the provided index.",
				index, entry.getPosition());
	}
}
