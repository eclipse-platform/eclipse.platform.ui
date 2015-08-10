/*******************************************************************************
 * Copyright (c) 2006, 2010 Brad Reynolds.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Brad Reynolds - initial API and implementation
 *    Brad Reynolds - bug 116920
 *    Ashley Cambrell - bug 198906
 *    Matthew Hall - bug 194734
 *    Ovidio Mallo - bug 270494
 *******************************************************************************/
package org.eclipse.jface.tests.internal.databinding.viewers;

import junit.framework.TestCase;

import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.jface.databinding.conformance.util.ValueChangeEventTracker;
import org.eclipse.jface.databinding.viewers.ViewersObservables;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.IPostSelectionProvider;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;

/**
 * Tests for SelectionProviderSingleSelectionObservableValue.
 *
 * @since 1.1
 */
public class SelectionProviderSingleSelectionObservableValueTest extends
		TestCase {
	private IPostSelectionProvider selectionProvider;

	private TableViewer viewer;

	private static String[] model = new String[] { "0", "1" };

	@Override
	protected void setUp() throws Exception {
		Shell shell = new Shell();
		viewer = new TableViewer(shell, SWT.NONE);
		viewer.setContentProvider(new ArrayContentProvider());
		viewer.setInput(model);
		selectionProvider = viewer;
	}

	@Override
	protected void tearDown() throws Exception {
		Shell shell = viewer.getTable().getShell();
		if (!shell.isDisposed())
			shell.dispose();
	}

	public void testConstructorIllegalArgumentException() {
		try {
			ViewersObservables.observeSingleSelection(null);
			fail();
		} catch (IllegalArgumentException e) {
		}
	}

	public void testSetValue() {
		IObservableValue observable = ViewersObservables
				.observeSingleSelection(selectionProvider);

		ValueChangeEventTracker listener = ValueChangeEventTracker
				.observe(observable);
		assertNull(observable.getValue());
		assertEquals(0, listener.count);

		observable.setValue(model[0]);
		assertEquals(model[0], getSelectedElement(selectionProvider));
		assertEquals(model[0], observable.getValue());
		assertEquals(1, listener.count);

		observable.setValue(model[1]);
		assertEquals(model[1], getSelectedElement(selectionProvider));
		assertEquals(model[1], observable.getValue());
		assertEquals(2, listener.count);

		observable.setValue(null);
		assertTrue(selectionProvider.getSelection().isEmpty());
		assertEquals(3, listener.count);
	}

	public void testSelectionChangesTracked() {
		doTestSelectionChangesTracked(false);
	}

	public void testPostSelectionChangesTracked() {
		doTestSelectionChangesTracked(true);
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
	private void doTestSelectionChangesTracked(boolean postSelection) {
		IObservableValue observable;
		if (postSelection) {
			observable = ViewersObservables
					.observeSinglePostSelection(selectionProvider);
		} else {
			observable = ViewersObservables
					.observeSingleSelection(selectionProvider);
		}

		ValueChangeEventTracker listener = ValueChangeEventTracker
				.observe(observable);
		assertNull(observable.getValue());

		selectionProvider.setSelection(new StructuredSelection(model[0]));
		assertEquals(1, listener.count);
		assertNull(listener.event.diff.getOldValue());
		assertEquals(model[0], listener.event.diff.getNewValue());
		assertEquals(observable, listener.event.getObservableValue());
		assertEquals(model[0], observable.getValue());

		selectionProvider.setSelection(new StructuredSelection(model[1]));
		assertEquals(2, listener.count);
		assertEquals(model[0], listener.event.diff.getOldValue());
		assertEquals(model[1], listener.event.diff.getNewValue());
		assertEquals(observable, listener.event.getObservableValue());
		assertEquals(model[1], observable.getValue());

		selectionProvider.setSelection(StructuredSelection.EMPTY);
		assertEquals(3, listener.count);
		assertEquals(model[1], listener.event.diff.getOldValue());
		assertNull(listener.event.diff.getNewValue());
		assertEquals(observable, listener.event.getObservableValue());
		assertEquals(null, observable.getValue());
	}

	public void testDispose() throws Exception {
		IObservableValue observable = ViewersObservables
				.observeSingleSelection(selectionProvider);
		ValueChangeEventTracker listener = ValueChangeEventTracker
				.observe(observable);

		selectionProvider.setSelection(new StructuredSelection(model[0]));
		assertEquals(1, listener.count);
		assertNull(listener.event.diff.getOldValue());
		assertEquals(model[0], listener.event.diff.getNewValue());
		assertEquals(observable, listener.event.getObservableValue());
		assertEquals(model[0], observable.getValue());

		observable.dispose();
		selectionProvider.setSelection(new StructuredSelection(model[1]));
		assertEquals(1, listener.count);
	}

	private static Object getSelectedElement(
			ISelectionProvider selectionProvider) {
		return ((IStructuredSelection) selectionProvider.getSelection())
				.getFirstElement();
	}
}
