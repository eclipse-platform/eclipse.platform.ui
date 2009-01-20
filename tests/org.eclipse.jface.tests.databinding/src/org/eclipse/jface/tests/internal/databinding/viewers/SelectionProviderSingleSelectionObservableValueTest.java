/*******************************************************************************
 * Copyright (c) 2006 Brad Reynolds.
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
 *******************************************************************************/
package org.eclipse.jface.tests.internal.databinding.viewers;

import junit.framework.TestCase;

import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.jface.databinding.conformance.util.ValueChangeEventTracker;
import org.eclipse.jface.databinding.viewers.ViewersObservables;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;

/**
 * Tests for SelectionProviderSingleSelectionObservableValue.
 * 
 * @since 1.1
 */
public class SelectionProviderSingleSelectionObservableValueTest extends
		TestCase {
	private ISelectionProvider selectionProvider;

	private TableViewer viewer;

	private static String[] model = new String[] { "0", "1" };

	/*
	 * (non-Javadoc)
	 *
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		Shell shell = new Shell();
		viewer = new TableViewer(shell, SWT.NONE);
		viewer.setContentProvider(new ContentProvider());
		viewer.setInput(model);
		selectionProvider = viewer;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see junit.framework.TestCase#tearDown()
	 */
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

	/**
	 * Asserts that when a selection is set on the viewer:
	 * <ul>
	 * <li>the selection is available in the observable</li>
	 * <li>Value change events are fired with appropriate diff values</li>
	 * </ul>
	 */
	public void testGetSetValue() {
		IObservableValue observable = ViewersObservables
				.observeSingleSelection(selectionProvider);
		ValueChangeEventTracker listener = new ValueChangeEventTracker();
		observable.addValueChangeListener(listener);
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
		ValueChangeEventTracker listener = new ValueChangeEventTracker();
		observable.addValueChangeListener(listener);

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

	private class ContentProvider implements IStructuredContentProvider {
		public void dispose() {
			// TODO Auto-generated method stub

		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			// TODO Auto-generated method stub

		}

		public Object[] getElements(Object inputElement) {
			return (String[]) inputElement;
		}
	}

}
