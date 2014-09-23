/*******************************************************************************
 * Copyright (c) 2007, 2014 Brad Reynolds and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Brad Reynolds - initial API and implementation
 *     Matthew Hall - bugs 215531, 221351, 213145
 *     Simon Scholz <simon.scholz@vogella.com> - Bug 444829
 ******************************************************************************/

package org.eclipse.jface.databinding.conformance;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import junit.framework.Test;

import org.eclipse.core.databinding.observable.IObservablesListener;
import org.eclipse.core.databinding.observable.set.IObservableSet;
import org.eclipse.jface.databinding.conformance.delegate.IObservableCollectionContractDelegate;
import org.eclipse.jface.databinding.conformance.util.ChangeEventTracker;
import org.eclipse.jface.databinding.conformance.util.SetChangeEventTracker;
import org.eclipse.jface.databinding.conformance.util.SuiteBuilder;

/**
 */
public class MutableObservableSetContractTest extends
		MutableObservableCollectionContractTest {
	private IObservableCollectionContractDelegate delegate;

	private IObservableSet set;

	public MutableObservableSetContractTest(String testName,
			IObservableCollectionContractDelegate delegate) {
		super(testName, delegate);
		this.delegate = delegate;
	}

	/**
	 * @param delegate
	 */
	public MutableObservableSetContractTest(
			IObservableCollectionContractDelegate delegate) {
		super(delegate);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		set = (IObservableSet) getObservable();
	}

	public void testAdd_SetChangeEvent() throws Exception {
		assertSetChangeEventFired(new Runnable() {
			@Override
			public void run() {
				set.add(delegate.createElement(set));
			}
		}, "Set.add(Object)", set);
	}

	public void testAdd_SetDiffEntry() throws Exception {
		set.add(delegate.createElement(set));
		final Object element = delegate.createElement(set);

		assertAddDiffEntry(new Runnable() {
			@Override
			public void run() {
				set.add(element);
			}
		}, "Set.add(Object)", set, element);
	}

	public void testAdd_GetterCalled() throws Exception {
		assertGetterCalled(new Runnable() {
			@Override
			public void run() {
				set.add(delegate.createElement(set));
			}
		}, "Set.add(Object)", set);
	}

	public void testAddAll_SetChangeEvent() throws Exception {
		assertSetChangeEventFired(new Runnable() {
			@Override
			public void run() {
				set.addAll(Arrays.asList(new Object[] { delegate
						.createElement(set) }));
			}
		}, "Set.addAll(Collection", set);
	}

	public void testAddAll_SetDiffEntry() throws Exception {
		final Object element = delegate.createElement(set);

		assertAddDiffEntry(new Runnable() {
			@Override
			public void run() {
				set.addAll(Arrays.asList(new Object[] { element }));
			}
		}, "Set.addAll(Collection)", set, element);
	}

	public void testAddAll_GetterCalled() throws Exception {
		assertGetterCalled(new Runnable() {
			@Override
			public void run() {
				set.addAll(Collections.singleton(delegate.createElement(set)));
			}
		}, "Set.addAll(Collection)", set);
	}

	public void testRemove_SetChangeEvent() throws Exception {
		final Object element = delegate.createElement(set);
		set.add(element);

		assertSetChangeEventFired(new Runnable() {
			@Override
			public void run() {
				set.remove(element);
			}
		}, "Set.remove(Object)", set);
	}

	public void testRemove_SetDiffEntry() throws Exception {
		set.add(delegate.createElement(set));
		final Object element = delegate.createElement(set);
		set.add(element);

		assertRemoveDiffEntry(new Runnable() {
			@Override
			public void run() {
				set.remove(element);
			}
		}, "Set.remove(Object)", set, element);
	}

	public void testRemove_GetterCalled() throws Exception {
		final Object element = delegate.createElement(set);
		set.add(element);
		assertGetterCalled(new Runnable() {
			@Override
			public void run() {
				set.remove(element);
			}
		}, "Set.remove(Object)", set);
	}

	public void testRemoveAll_SetChangeEvent() throws Exception {
		final Object element = delegate.createElement(set);
		set.add(element);

		assertSetChangeEventFired(new Runnable() {
			@Override
			public void run() {
				set.removeAll(Arrays.asList(new Object[] { element }));
			}
		}, "Set.removeAll(Collection)", set);
	}

	public void testRemoveAll_SetDiffEntry() throws Exception {
		final Object element = delegate.createElement(set);
		set.add(element);

		assertRemoveDiffEntry(new Runnable() {
			@Override
			public void run() {
				set.removeAll(Arrays.asList(new Object[] { element }));
			}
		}, "Set.removeAll(Collection)", set, element);
	}

	public void testRemoveAll_GetterCalled() throws Exception {
		final Object element = delegate.createElement(set);
		set.add(element);
		assertGetterCalled(new Runnable() {
			@Override
			public void run() {
				set.removeAll(Collections.singleton(element));
			}
		}, "Set.removeAll(Collection)", set);
	}

	public void testRetainAll_SetChangeEvent() throws Exception {
		final Object element1 = delegate.createElement(set);
		set.add(element1);
		set.add(delegate.createElement(set));

		assertSetChangeEventFired(new Runnable() {
			@Override
			public void run() {
				set.retainAll(Arrays.asList(new Object[] { element1 }));
			}
		}, "Set.retainAll(Collection", set);
	}

	public void testRetainAll_SetDiffEntry() throws Exception {
		final Object element1 = delegate.createElement(set);
		set.add(element1);
		Object element2 = delegate.createElement(set);
		set.add(element2);

		assertRemoveDiffEntry(new Runnable() {
			@Override
			public void run() {
				set.retainAll(Arrays.asList(new Object[] { element1 }));
			}
		}, "Set.retainAll(Collection)", set, element2);
	}

	public void testRetainAll_GetterCalled() throws Exception {
		set.add(delegate.createElement(set));
		assertGetterCalled(new Runnable() {
			@Override
			public void run() {
				set.retainAll(Collections.EMPTY_SET);
			}
		}, "Set.retainAll(Collection)", set);
	}

	public void testClear_SetChangeEvent() throws Exception {
		set.add(delegate.createElement(set));

		assertSetChangeEventFired(new Runnable() {
			@Override
			public void run() {
				set.clear();
			}
		}, "Set.clear()", set);
	}

	public void testClear_SetDiffEntry() throws Exception {
		Object element = delegate.createElement(set);
		set.add(element);

		assertRemoveDiffEntry(new Runnable() {
			@Override
			public void run() {
				set.clear();
			}
		}, "Set.clear()", set, element);
	}

	public void testClear_GetterCalled() throws Exception {
		set.add(delegate.createElement(set));
		assertGetterCalled(new Runnable() {
			@Override
			public void run() {
				set.clear();
			}
		}, "Set.clear()", set);
	}

	/**
	 * Asserts standard behaviors of firing set change events.
	 * <ul>
	 * <li>Event fires once.</li>
	 * <li>Source of the event is the provided <code>set</code>.
	 * <li>The set change event is fired after the change event.</li>
	 * </ul>
	 *
	 * @param runnable
	 * @param methodName
	 * @param set
	 */
	private void assertSetChangeEventFired(Runnable runnable,
			String methodName, IObservableSet set) {
		List<IObservablesListener> queue = new ArrayList<IObservablesListener>();
		SetChangeEventTracker setListener = new SetChangeEventTracker(queue);
		ChangeEventTracker changeListener = new ChangeEventTracker(queue);

		set.addSetChangeListener(setListener);
		set.addChangeListener(changeListener);

		runnable.run();

		assertEquals(
				formatFail(methodName + " should fire one SetChangeEvent."), 1,
				setListener.count);
		assertEquals(formatFail(methodName
				+ "'s change event observable should be the created Set."),
				set, setListener.event.getObservable());

		assertEquals(
				formatFail("Two notifications should have been received."), 2,
				queue.size());
		assertEquals(formatFail("ChangeEvent of " + methodName
				+ " should have fired before the SetChangeEvent."),
				changeListener, queue.get(0));
		assertEquals(formatFail("SetChangeEvent of " + methodName
				+ " should have fired after the ChangeEvent."), setListener,
				queue.get(1));
	}

	/**
	 * Asserts the set diff entry for an add operation.
	 * 
	 * @param runnable
	 * @param methodName
	 * @param set
	 * @param element
	 */
	private void assertAddDiffEntry(Runnable runnable, String methodName,
			IObservableSet set, Object element) {
		SetChangeEventTracker listener = new SetChangeEventTracker();
		set.addSetChangeListener(listener);

		runnable.run();

		Set entries = listener.event.diff.getAdditions();
		assertEquals(formatFail(methodName
				+ " should result in one diff entry."), 1, entries.size());

		assertTrue(formatFail(methodName
				+ " should result in a diff entry that is an addition."),
				entries.contains(element));
	}

	/**
	 * Asserts the set diff entry for a remove operation.
	 * 
	 * @param runnable
	 * @param methodName
	 * @param set
	 * @param element
	 */
	private void assertRemoveDiffEntry(Runnable runnable, String methodName,
			IObservableSet set, Object element) {
		SetChangeEventTracker listener = new SetChangeEventTracker();
		set.addSetChangeListener(listener);

		runnable.run();

		Set entries = listener.event.diff.getRemovals();
		assertEquals(formatFail(methodName
				+ " should result in one diff entry."), 1, entries.size());

		assertTrue(formatFail(methodName
				+ " should result in a diff entry that is a removal."),
				entries.contains(element));
	}

	public static Test suite(IObservableCollectionContractDelegate delegate) {
		return new SuiteBuilder()
				.addObservableContractTest(
						MutableObservableSetContractTest.class, delegate)
				.addObservableContractTest(
						ObservableCollectionContractTest.class, delegate)
				.build();
	}
}
