/*******************************************************************************
 * Copyright (c) 2006 Cerner Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Brad Reynolds - initial API and implementation
 ******************************************************************************/

package org.eclipse.jface.tests.internal.databinding.internal.observable;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.eclipse.core.databinding.observable.ChangeEvent;
import org.eclipse.core.databinding.observable.Diffs;
import org.eclipse.core.databinding.observable.IChangeListener;
import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.IStaleListener;
import org.eclipse.core.databinding.observable.Observables;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.StaleEvent;
import org.eclipse.core.databinding.observable.list.IListChangeListener;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.ListChangeEvent;
import org.eclipse.core.databinding.observable.list.ListDiff;
import org.eclipse.core.databinding.observable.list.ListDiffEntry;
import org.eclipse.core.databinding.observable.list.ObservableList;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.swt.widgets.Display;

public class UnmodifiableObservableListTest extends TestCase {
	ObservableList unmodifiable;
	ObservableList mutable;
	
	protected void setUp() throws Exception {
		Realm.setDefault(SWTObservables.getRealm(Display.getDefault()));
		
		List list = new ArrayList();
		list.add("1");
		list.add("2");
				
		mutable = new MutableObservableList(list, String.class);
		unmodifiable = (ObservableList) Observables.unmodifiableObservableList(mutable);
	}
	
	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		Realm.setDefault(null);
	}
	
	public void testFiresChangeEvents() throws Exception {
		ChangeCounter mutableListener = new ChangeCounter();
		ChangeCounter unmodifiableListener = new ChangeCounter();
		
		mutable.addChangeListener(mutableListener);
		unmodifiable.addChangeListener(unmodifiableListener);
		
		assertEquals(0, mutableListener.count);
		assertEquals(0, unmodifiableListener.count);
		mutable.add("3");
		assertEquals(1, mutableListener.count);
		assertEquals(1, unmodifiableListener.count);
	}
	
	public void testFiresListChangeEvents() throws Exception {
		ListChangeCounter mutableListener = new ListChangeCounter();
		ListChangeCounter unmodifiableListener = new ListChangeCounter();
		
		mutable.addListChangeListener(mutableListener);
		unmodifiable.addListChangeListener(unmodifiableListener);
		
		assertEquals(0, mutableListener.count);
		assertEquals(0, unmodifiableListener.count);

		String element = "3";
		mutable.add(element);
		assertEquals(1, mutableListener.count);
		assertEquals(mutable, mutableListener.source);
		assertEquals(1, mutableListener.diff.getDifferences().length);
		
		ListDiffEntry difference = mutableListener.diff.getDifferences()[0];
		assertEquals(element, difference.getElement());
		assertTrue(difference.isAddition());
		assertEquals(3, mutable.size());
		
		assertEquals(1, unmodifiableListener.count);
		assertEquals(unmodifiable, unmodifiableListener.source);
		assertEquals(1, unmodifiableListener.diff.getDifferences().length);
		
		difference = unmodifiableListener.diff.getDifferences()[0];
		assertEquals(element, difference.getElement());
		assertTrue(difference.isAddition());
		assertEquals(3, unmodifiable.size());
	}
	
	public void testFiresStaleEvents() throws Exception {
		StaleCounter mutableListener = new StaleCounter();
		StaleCounter unmodifiableListener = new StaleCounter();
		
		mutable.addStaleListener(mutableListener);
		unmodifiable.addStaleListener(unmodifiableListener);
		
		assertEquals(0, mutableListener.count);
		assertEquals(0, unmodifiableListener.count);
		mutable.setStale(true);
		assertEquals(1, mutableListener.count);
		assertEquals(mutable, mutableListener.source);
		assertTrue(mutable.isStale());
		assertEquals(1, unmodifiableListener.count);
		assertEquals(unmodifiable, unmodifiableListener.source);
		assertTrue(unmodifiable.isStale());
	}
	
	public void testIsStale() throws Exception {
		assertFalse(mutable.isStale());
		assertFalse(unmodifiable.isStale());
		mutable.setStale(true);
		assertTrue(mutable.isStale());
		assertTrue(unmodifiable.isStale());
	}
	
	public void testSetStaleOnUnmodifiableList() throws Exception {
		try {
			unmodifiable.setStale(true);
			fail("UnsupportedOperationException should have been thrown");
		} catch (UnsupportedOperationException e) {
		}		
	}
	
	private static class StaleCounter implements IStaleListener {
		int count;
		IObservable source;
		
		public void handleStale(StaleEvent event) {
			count++;
			this.source = event.getObservable();
		}
	}
	
	private static class ChangeCounter implements IChangeListener {
		int count;
		IObservable source;
		public void handleChange(ChangeEvent event) {
			count++;
			this.source = event.getObservable();
		}
	}
	
	private static class ListChangeCounter implements IListChangeListener {
		int count;
		IObservableList source;
		ListDiff diff;

		public void handleListChange(ListChangeEvent event) {
			count++;
			this.source = event.getObservableList();
			this.diff = event.diff;
		}
	}
	
	private static class MutableObservableList extends ObservableList {
		/**
		 * @param wrappedList
		 * @param elementType
		 */
		public MutableObservableList(List wrappedList, Object elementType) {
			super(wrappedList, elementType);
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.jface.internal.databinding.provisional.observable.list.ObservableList#add(java.lang.Object)
		 */
		public boolean add(Object o) {
			boolean result = wrappedList.add(o);
			fireListChange(Diffs.createListDiff(Diffs.createListDiffEntry(wrappedList.size() - 1, true, o)));
			
			return result;
		}
	}
}
