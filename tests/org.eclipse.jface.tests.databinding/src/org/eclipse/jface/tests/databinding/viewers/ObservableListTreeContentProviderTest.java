/*******************************************************************************
 * Copyright (c) 2007, 2018 Matthew Hall and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 207858)
 *     Matthew Hall - bug 263956
 *******************************************************************************/

package org.eclipse.jface.tests.databinding.viewers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.Collections;

import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.core.databinding.observable.masterdetail.IObservableFactory;
import org.eclipse.core.databinding.observable.set.IObservableSet;
import org.eclipse.core.runtime.AssertionFailedException;
import org.eclipse.jface.databinding.viewers.ObservableListTreeContentProvider;
import org.eclipse.jface.tests.databinding.AbstractDefaultRealmTestCase;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ObservableListTreeContentProviderTest extends
		AbstractDefaultRealmTestCase {
	private Shell shell;
	private TreeViewer viewer;
	private Tree tree;
	private ObservableListTreeContentProvider<Object> contentProvider;
	private Object input;

	@Override
	@Before
	public void setUp() throws Exception {
		super.setUp();
		shell = new Shell();
		tree = new Tree(shell, SWT.NONE);
		viewer = new TreeViewer(tree);
		input = new Object();
	}

	@Override
	@After
	public void tearDown() throws Exception {
		shell.dispose();
		tree = null;
		viewer = null;
		input = null;
		super.tearDown();
	}

	private void initContentProvider(IObservableFactory<Object, IObservableList<Object>> listFactory) {
		contentProvider = new ObservableListTreeContentProvider<>(listFactory, null);
		viewer.setContentProvider(contentProvider);
		viewer.setInput(input);
	}

	@Test
	public void testConstructor_NullArgumentThrowsException() {
		try {
			initContentProvider(null);
			fail("Constructor should have thrown AssertionFailedException");
		} catch (AssertionFailedException expected) {
		}
	}

	@Test
	public void testGetElements_ChangesFollowObservedList() {
		final IObservableList<Object> elements = new WritableList<>();
		final Object input = new Object();
		initContentProvider(target -> target == input ? elements : null);

		assertTrue(Arrays.equals(new Object[0], contentProvider
				.getElements("unknown input")));

		Object element0 = new Object();
		elements.add(element0);

		assertTrue(Arrays.equals(new Object[] { element0 }, contentProvider
				.getElements(input)));

		Object element1 = new Object();
		elements.add(element1);

		assertTrue(Arrays.equals(new Object[] { element0, element1 },
				contentProvider.getElements(input)));
	}

	@Test
	public void testViewerUpdate_RemoveElementAfterMutation() {
		final IObservableList<Object> children = new WritableList<>();
		initContentProvider(target -> target == input ? children : null);

		Mutable element = new Mutable();
		children.add(element);
		assertEquals(1, tree.getItemCount());

		element.mutate();
		children.remove(element);
		assertEquals(0, tree.getItemCount());
	}

	@Test
	public void testInputChanged_ClearsKnownElements() {
		input = new Object();
		final Object input2 = new Object();

		final IObservableList<Object> children = new WritableList<>();
		final IObservableList<Object> children2 = new WritableList<>();
		initContentProvider(target -> {
			if (target == input)
				return children;
			if (target == input2)
				return children2;
			return null;
		});

		Object element = new Object();
		children.add(element);

		IObservableSet<Object> knownElements = contentProvider.getKnownElements();
		assertEquals(Collections.singleton(element), knownElements);
		viewer.setInput(input2);
		assertEquals(Collections.EMPTY_SET, knownElements);
	}

	@Test
	public void testInputChanged_ClearsRealizedElements() {
		input = new Object();
		final Object input2 = new Object();

		final IObservableList<Object> children = new WritableList<>();
		final IObservableList<Object> children2 = new WritableList<>();
		initContentProvider(target -> {
			if (target == input)
				return children;
			if (target == input2)
				return children2;
			return null;
		});

		// Realized elements must be allocated before adding the element
		// otherwise we'd have to spin the event loop to see the new element
		IObservableSet<Object> realizedElements = contentProvider.getRealizedElements();

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
