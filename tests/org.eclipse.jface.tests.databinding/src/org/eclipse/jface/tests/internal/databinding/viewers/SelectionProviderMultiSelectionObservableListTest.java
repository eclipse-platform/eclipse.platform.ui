/*******************************************************************************
 * Copyright (c) 2006, 2010 Brad Reynolds.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Brad Reynolds - initial API and implementation
 *     Brad Reynolds - bug 116920
 *     Matthew Hall - bug 194734
 *     Ovidio Mallo - bug 270494
 *******************************************************************************/
package org.eclipse.jface.tests.internal.databinding.viewers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import junit.framework.TestCase;

import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.ListDiff;
import org.eclipse.jface.databinding.conformance.util.ListChangeEventTracker;
import org.eclipse.jface.databinding.viewers.ViewersObservables;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.IPostSelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;

/**
 * Tests for SelectionProviderMultiSelectionObservableList.
 * 
 * @since 1.2
 */
public class SelectionProviderMultiSelectionObservableListTest extends TestCase {
	private IPostSelectionProvider selectionProvider;

	private TableViewer viewer;

	private static String[] model = new String[] { "element0", "element1",
			"element2", "element3" };

	protected void setUp() throws Exception {
		Shell shell = new Shell();
		viewer = new TableViewer(shell, SWT.MULTI);
		viewer.setContentProvider(new ArrayContentProvider());
		viewer.setInput(model);
		selectionProvider = viewer;
	}

	protected void tearDown() throws Exception {
		Shell shell = viewer.getTable().getShell();
		if (!shell.isDisposed())
			shell.dispose();
	}

	public void testConstructorIllegalArgumentException() {
		try {
			ViewersObservables.observeMultiSelection(null);
			fail();
		} catch (IllegalArgumentException e) {
		}
	}

	public void testAddRemove_NormalSelection() {
		doTestAddRemove(false);
	}

	public void testAddRemove_PostSelection() {
		doTestAddRemove(true);
	}

	/**
	 * Asserts that when a selection is set on the viewer:
	 * <ul>
	 * <li>the selection is available in the observable</li>
	 * <li>Value change events are fired with appropriate diff values</li>
	 * </ul>
	 * 
	 * @param postSelection
	 *            <code>true</code> for observing the post selection,
	 *            <code>false</code> for observing the normal selection.
	 */
	private void doTestAddRemove(boolean postSelection) {
		IObservableList observable;
		if (postSelection) {
			observable = ViewersObservables
					.observeMultiPostSelection(selectionProvider);
		} else {
			observable = ViewersObservables
					.observeMultiSelection(selectionProvider);
		}

		ListChangeEventTracker listener = ListChangeEventTracker
				.observe(observable);
		assertEquals(0, observable.size());

		selectionProvider.setSelection(new StructuredSelection(model[0]));
		assertEquals(1, listener.count);
		assertDiff(listener.event.diff, Collections.EMPTY_LIST, Collections
				.singletonList(model[0]));
		assertEquals(observable, listener.event.getObservableList());
		assertEquals(1, observable.size());
		assertEquals(model[0], observable.get(0));

		selectionProvider.setSelection(new StructuredSelection(model[1]));
		assertEquals(2, listener.count);
		assertEquals(2, listener.event.diff.getDifferences().length);
		assertDiff(listener.event.diff, Collections.singletonList(model[0]),
				Collections.singletonList(model[1]));
		assertEquals(observable, listener.event.getObservableList());
		assertEquals(1, observable.size());
		assertEquals(model[1], observable.get(0));

		selectionProvider.setSelection(new StructuredSelection(new Object[] {
				model[2], model[3] }));
		assertEquals(3, listener.count);
		assertEquals(3, listener.event.diff.getDifferences().length);
		assertDiff(listener.event.diff, Collections.singletonList(model[1]),
				Arrays.asList(new Object[] { model[2], model[3] }));
		assertEquals(observable, listener.event.getObservableList());
		assertEquals(2, observable.size());
		assertEquals(model[2], observable.get(0));
		assertEquals(model[3], observable.get(1));

		selectionProvider.setSelection(StructuredSelection.EMPTY);
		assertEquals(4, listener.count);
		assertEquals(2, listener.event.diff.getDifferences().length);
		assertDiff(listener.event.diff, Arrays.asList(new Object[] { model[2],
				model[3] }), Collections.EMPTY_LIST);
		assertEquals(observable, listener.event.getObservableList());
		assertEquals(0, observable.size());

		observable.add(model[1]);
		assertEquals(5, listener.count);
		assertEquals(1, listener.event.diff.getDifferences().length);
		assertDiff(listener.event.diff, Collections.EMPTY_LIST, Collections
				.singletonList(model[1]));
		assertEquals(observable, listener.event.getObservableList());
		assertEquals(1, ((IStructuredSelection) viewer.getSelection()).size());

		observable.add(0, model[2]);
		assertEquals(6, listener.count);
		assertEquals(1, listener.event.diff.getDifferences().length);
		// This is a bit surprising (we added at index 0 but the event says
		// index 1). It is to the fact that the observable list tracks the
		// underlying selection provider's notion of which element is at which
		// index.
		assertDiff(listener.event.diff, Collections.singletonList(model[1]),
				Arrays.asList(new Object[] { model[1], model[2] }));
		assertEquals(observable, listener.event.getObservableList());
		assertEquals(2, ((IStructuredSelection) viewer.getSelection()).size());

		observable.clear();
		assertEquals(7, listener.count);
		assertEquals(2, listener.event.diff.getDifferences().length);
		assertDiff(listener.event.diff, Arrays.asList(new Object[] { model[1],
				model[2] }), Collections.EMPTY_LIST);
		assertEquals(observable, listener.event.getObservableList());
		assertEquals(0, ((IStructuredSelection) viewer.getSelection()).size());
	}

	private void assertDiff(ListDiff diff, List oldList, List newList) {
		// defensive copy in case arg is unmodifiable
		oldList = new ArrayList(oldList);
		diff.applyTo(oldList);
		assertEquals("applying diff to list did not produce expected result",
				newList, oldList);
	}
}
