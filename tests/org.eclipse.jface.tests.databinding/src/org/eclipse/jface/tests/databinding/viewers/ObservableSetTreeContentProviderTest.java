/*******************************************************************************
 * Copyright (c) 2007, 2009 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 207858)
 *     Matthew Hall - bug 263956
 *******************************************************************************/

package org.eclipse.jface.tests.databinding.viewers;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.masterdetail.IObservableFactory;
import org.eclipse.core.databinding.observable.set.IObservableSet;
import org.eclipse.core.databinding.observable.set.WritableSet;
import org.eclipse.core.runtime.AssertionFailedException;
import org.eclipse.jface.databinding.viewers.ObservableSetTreeContentProvider;
import org.eclipse.jface.internal.databinding.viewers.ObservableViewerElementSet;
import org.eclipse.jface.tests.databinding.AbstractDefaultRealmTestCase;
import org.eclipse.jface.viewers.IElementComparer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;

public class ObservableSetTreeContentProviderTest extends
		AbstractDefaultRealmTestCase {
	private Shell shell;
	private TreeViewer viewer;
	private Tree tree;
	private ObservableSetTreeContentProvider contentProvider;
	private Object input;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		shell = new Shell();
		tree = new Tree(shell, SWT.NONE);
		viewer = new TreeViewer(tree);
		input = new Object();
	}

	@Override
	protected void tearDown() throws Exception {
		shell.dispose();
		tree = null;
		viewer = null;
		input = null;
		super.tearDown();
	}

	private void initContentProvider(IObservableFactory listFactory) {
		contentProvider = new ObservableSetTreeContentProvider(listFactory, null);
		viewer.setContentProvider(contentProvider);
		viewer.setInput(input);
	}

	public void testConstructor_NullArgumentThrowsException() {
		try {
			initContentProvider(null);
			fail("Constructor should have thrown AssertionFailedException");
		} catch (AssertionFailedException expected) {
		}
	}

	public void testGetElements_ChangesFollowObservedList() {
		final IObservableSet elements = new WritableSet();
		final Object input = new Object();
		initContentProvider(new IObservableFactory() {
			@Override
			public IObservable createObservable(Object target) {
				return target == input ? elements : null;
			}
		});

		assertTrue(Arrays.equals(new Object[0], contentProvider
				.getElements("unknown input")));

		Object element0 = new Object();
		elements.add(element0);

		assertTrue(Arrays.equals(new Object[] { element0 }, contentProvider
				.getElements(input)));

		Object element1 = new Object();
		elements.add(element1);

		List elementList = Arrays.asList(contentProvider.getElements(input));
		assertEquals(2, elementList.size());
		assertTrue(elementList.containsAll(Arrays.asList(new Object[] {
				element0, element1 })));
	}

	public void testViewerUpdate_RemoveElementAfterMutation() {
		IElementComparer comparer = new IElementComparer() {
			@Override
			public boolean equals(Object a, Object b) {
				return a == b;
			}

			@Override
			public int hashCode(Object element) {
				return System.identityHashCode(element);
			}
		};
		viewer.setComparer(comparer);

		final IObservableSet children = ObservableViewerElementSet
				.withComparer(Realm.getDefault(), null, comparer);
		initContentProvider(new IObservableFactory() {
			@Override
			public IObservable createObservable(Object target) {
				return target == input ? children : null;
			}
		});

		Mutable element = new Mutable();
		children.add(element);
		assertEquals(1, tree.getItemCount());

		element.mutate();
		assertTrue(children.remove(element));
		assertEquals(0, tree.getItemCount());
	}

	public void testInputChanged_ClearsKnownElements() {
		input = new Object();
		final Object input2 = new Object();

		final IObservableSet children = new WritableSet();
		final IObservableSet children2 = new WritableSet();
		initContentProvider(new IObservableFactory() {
			@Override
			public IObservable createObservable(Object target) {
				if (target == input)
					return children;
				if (target == input2)
					return children2;
				return null;
			}
		});

		Object element = new Object();
		children.add(element);

		IObservableSet knownElements = contentProvider.getKnownElements();
		assertEquals(Collections.singleton(element), knownElements);
		viewer.setInput(input2);
		assertEquals(Collections.EMPTY_SET, knownElements);
	}

	public void testInputChanged_ClearsRealizedElements() {
		input = new Object();
		final Object input2 = new Object();

		final IObservableSet children = new WritableSet();
		final IObservableSet children2 = new WritableSet();
		initContentProvider(new IObservableFactory() {
			@Override
			public IObservable createObservable(Object target) {
				if (target == input)
					return children;
				if (target == input2)
					return children2;
				return null;
			}
		});

		// Realized elements must be allocated before adding the element
		// otherwise we'd have to spin the event loop to see the new element
		IObservableSet realizedElements = contentProvider.getRealizedElements();

		Object element = new Object();
		children.add(element);

		assertEquals(Collections.singleton(element), realizedElements);
		viewer.setInput(input2);
		assertEquals(Collections.EMPTY_SET, realizedElements);
	}

	static class Mutable {
		private int id;

		public Mutable() {
			this(0);
		}

		public Mutable(int id) {
			this.id = id;
		}

		public void mutate() {
			id++;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == this)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Mutable that = (Mutable) obj;
			return this.id == that.id;
		}

		@Override
		public int hashCode() {
			return id;
		}
	}
}
