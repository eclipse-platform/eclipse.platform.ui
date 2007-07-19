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

package org.eclipse.jface.conformance.databinding;

import java.util.Arrays;

import org.eclipse.core.databinding.observable.ChangeEvent;
import org.eclipse.core.databinding.observable.IChangeListener;
import org.eclipse.core.databinding.observable.IObservableCollection;
import org.eclipse.jface.tests.databinding.EventTrackers.ChangeEventTracker;

/**
 * @since 3.2
 */
public class MutableObservableCollectionContractTest extends
		ObservableCollectionContractTest {
	private IObservableCollectionContractDelegate delegate;

	private IObservableCollection collection;

	public MutableObservableCollectionContractTest(
			IObservableCollectionContractDelegate delegate) {
		super(delegate);
		this.delegate = delegate;
	}

	public MutableObservableCollectionContractTest(String name,
			IObservableCollectionContractDelegate delegate) {
		super(name, delegate);
		this.delegate = delegate;
	}

	protected void setUp() throws Exception {
		super.setUp();

		collection = super.getObservableCollection();
	}

	public void testAdd_FiresChangeEvent() throws Exception {
		ChangeEventTracker listener = addElement(collection, delegate);

		assertEquals("Collection.add(...) should fire one ChangeEvent.", 1,
				listener.count);
	}

	public void testAdd_ObservableOfChangeEvent() throws Exception {
		ChangeEventTracker listener = addElement(collection, delegate);

		assertEquals(
				"Collection.add(..)'s change event observable should be the created Collection.",
				collection, listener.event.getObservable());
	}

	public void testAdd_ElementIsAdded() throws Exception {
		Object element = delegate.createElement(collection);

		// precondition
		assertFalse(collection.contains(element));
		collection.add(element);

		assertTrue(
				"Collection.add(...) should add the element to the Collection.",
				collection.contains(element));
	}

	public void testAdd_FiresChangeEventAfterElementIsAdded() throws Exception {
		final Object element = delegate.createElement(collection);

		ContainsListener listener = new ContainsListener(collection, element)
				.init();

		// precondition
		assertFalse(collection.contains(element));
		collection.add(element);

		assertTrue(
				"When Collection.add(...) fires the change event the element should already have been added to the Collection.",
				listener.contains);
	}

	private ChangeEventTracker addElement(IObservableCollection collection,
			IObservableCollectionContractDelegate delegate) {
		ChangeEventTracker listener = new ChangeEventTracker();
		collection.addChangeListener(listener);
		Object element = delegate.createElement(collection);
		collection.add(element);

		return listener;
	}

	public void testAddAll_FiresChangeEvent() throws Exception {
		ChangeEventTracker listener = addAllElements(collection, delegate);

		assertEquals("Collection.addAll(...) should fire one ChangeEvent.", 1,
				listener.count);
	}

	public void testAddAll_ObservableOfChangeEvent() throws Exception {
		ChangeEventTracker listener = addAllElements(collection, delegate);

		assertEquals(
				"Collection.addAll(..)'s change event observable should be the created Collection.",
				collection, listener.event.getObservable());
	}

	public void testAddAll_ElementsAreAdded() throws Exception {
		Object element = delegate.createElement(collection);

		// precondition
		assertFalse(collection.contains(element));
		collection.addAll(Arrays.asList(new Object[] { element }));

		assertTrue(
				"Collection.addAll(...) should add the element to the Collection.",
				collection.contains(element));
	}

	public void testAddAll_FiresChangeEventAfterElementsAreAdded()
			throws Exception {
		final Object element = delegate.createElement(collection);

		ContainsListener listener = new ContainsListener(collection, element)
				.init();

		// precondition
		assertFalse(collection.contains(element));
		collection.addAll(Arrays.asList(new Object[] { element }));

		assertTrue(
				"When Collection.addAll(...) fires the change event the element should already have been added to the Collection.",
				listener.contains);
	}

	private ChangeEventTracker addAllElements(IObservableCollection collection,
			IObservableCollectionContractDelegate delegate) {
		ChangeEventTracker listener = new ChangeEventTracker();
		collection.addChangeListener(listener);
		Object element = delegate.createElement(collection);
		collection.addAll(Arrays.asList(new Object[] { element }));

		return listener;
	}

	public void testRemove_FiresChangeEvent() throws Exception {
		ChangeEventTracker listener = removeElement(collection, delegate);

		assertEquals("Collection.remove(...) should fire one ChangeEvent.", 1,
				listener.count);
	}

	public void testRemove_ObervableOfChangeEvent() throws Exception {
		ChangeEventTracker listener = removeElement(collection, delegate);
		assertEquals(
				"Collection.remove(...)'s change event observable should be the created Collection.",
				collection, listener.event.getObservable());
	}

	public void testRemove_ElementIsRemoved() throws Exception {
		Object element = delegate.createElement(collection);
		collection.add(element);

		// precondition
		assertTrue(collection.contains(element));
		collection.remove(element);

		assertFalse(
				"Collection.remove(...) should remove the element from the Collection.",
				collection.contains(element));
	}

	public void testRemove_FiresChangeEventAfterElementIsRemoved()
			throws Exception {
		Object element = delegate.createElement(collection);
		collection.add(element);
		// precondition
		assertTrue(collection.contains(element));

		ContainsListener listener = new ContainsListener(collection, element)
				.init();
		listener.contains = true;
		collection.remove(element);
		assertFalse(
				"When Collection.remove(...) fires the change event the element should already have been removed from the Collection.",
				listener.contains);
	}

	private ChangeEventTracker removeElement(IObservableCollection collection,
			IObservableCollectionContractDelegate delegate) {
		ChangeEventTracker listener = new ChangeEventTracker();
		Object element = delegate.createElement(collection);
		collection.add(element);
		collection.addChangeListener(listener);
		collection.remove(element);

		return listener;
	}

	public void testRemoveAll_FiresChangeEvent() throws Exception {
		ChangeEventTracker listener = removeAllElements(collection, delegate);

		assertEquals("Collection.removeAll(...) should fire one ChangeEvent.",
				1, listener.count);
	}

	public void testRemoveAll_ObervableOfChangeEvent() throws Exception {
		ChangeEventTracker listener = removeAllElements(collection, delegate);
		assertEquals(
				"Collection.removeAll(...)'s change event observable should be the created Collection.",
				collection, listener.event.getObservable());
	}

	public void testRemoveAll_ElementsAreRemoved() throws Exception {
		Object element = delegate.createElement(collection);
		collection.add(element);

		// precondition
		assertTrue(collection.contains(element));
		collection.removeAll(Arrays.asList(new Object[] { element }));

		assertFalse(
				"Collection.removeAll(...) should remove the element from the Collection.",
				collection.contains(element));
	}

	public void testRemoveAll_FiresChangeEventAfterElementsAreRemoved()
			throws Exception {
		Object element = delegate.createElement(collection);
		collection.add(element);
		// precondition
		assertTrue(collection.contains(element));

		ContainsListener listener = new ContainsListener(collection, element)
				.init();
		listener.contains = true;
		collection.removeAll(Arrays.asList(new Object[] { element }));
		assertFalse(
				"When Collection.remove(...) fires the change event the element should already have been removed from the Collection.",
				listener.contains);
	}

	public void testRetainAll_FiresChangeEvent() throws Exception {
		ChangeEventTracker listener = retainAllElements(collection, delegate);

		assertEquals("Collection.retainAll(...) should fire one ChangeEvent.",
				1, listener.count);
	}

	public void testRetainAll_ObervableOfChangeEvent() throws Exception {
		ChangeEventTracker listener = retainAllElements(collection, delegate);
		assertEquals(
				"Collection.retainAll(...)'s change event observable should be the created Collection.",
				collection, listener.event.getObservable());
	}

	public void testRetainAll_ElementsAreRetained() throws Exception {
		Object element1 = delegate.createElement(collection);
		collection.add(element1);
		Object element2 = delegate.createElement(collection);
		collection.add(element2);

		// precondition
		assertTrue(collection.contains(element1));
		assertTrue(collection.contains(element2));

		collection.retainAll(Arrays.asList(new Object[] { element1 }));

		assertTrue(
				"Collection.retainAll(...) should retain the element in the Collection.",
				collection.contains(element1));

		assertFalse(
				"Collection.retainAll(...) should remove the discarded element from the Collection.",
				collection.contains(element2));
	}

	public void testRetainAll_FiresChangeEventAfterElementsAreRetained()
			throws Exception {
		Object element1 = delegate.createElement(collection);
		collection.add(element1);
		Object element2 = delegate.createElement(collection);
		collection.add(element2);

		// precondition
		assertTrue(collection.contains(element1));
		assertTrue(collection.contains(element2));

		ContainsListener listener1 = new ContainsListener(collection, element1)
				.init();
		ContainsListener listener2 = new ContainsListener(collection, element2)
				.init();

		//set contains the the opposite of the expected outcome to ensure they get set
		listener1.contains = false;
		listener2.contains = true;
		
		collection.retainAll(Arrays.asList(new Object[] { element1 }));
		assertTrue(
				"When Collection.retainAll(...) fires the change event the element should have been retained in the Collection.",
				listener1.contains);
		assertFalse(
				"When Collection.retainAll(...) fires the change event the element should have been removed from the Collection.",
				listener2.contains);
	}

	private ChangeEventTracker retainAllElements(
			IObservableCollection collection,
			IObservableCollectionContractDelegate delegate) {
		ChangeEventTracker listener = new ChangeEventTracker();
		Object element1 = delegate.createElement(collection);
		Object element2 = delegate.createElement(collection);

		collection.add(element1);
		collection.add(element2);

		collection.addChangeListener(listener);
		collection.retainAll(Arrays.asList(new Object[] { element1 }));

		return listener;
	}

	private ChangeEventTracker removeAllElements(
			IObservableCollection collection,
			IObservableCollectionContractDelegate delegate) {
		ChangeEventTracker listener = new ChangeEventTracker();
		Object element = delegate.createElement(collection);
		collection.add(element);
		collection.addChangeListener(listener);
		collection.removeAll(Arrays.asList(new Object[] { element }));

		return listener;
	}

	/* package */static class ContainsListener implements IChangeListener {
		boolean contains;

		final private Object element;

		final private IObservableCollection collection;

		ContainsListener(IObservableCollection collection, Object element) {
			this.element = element;
			this.collection = collection;
		}

		ContainsListener init() {
			collection.addChangeListener(this);
			return this;
		}

		public void handleChange(ChangeEvent event) {
			contains = collection.contains(element);
		}
	}
}
