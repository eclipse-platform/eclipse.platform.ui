/*******************************************************************************
 * Copyright (c) 2006 Brad Reynolds.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Brad Reynolds - initial API and implementation
 *     Brad Reynolds - bug 116920
 *******************************************************************************/
package org.eclipse.jface.tests.internal.databinding.internal.viewers;

import junit.framework.TestCase;

import org.eclipse.core.databinding.observable.list.ListDiffEntry;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.internal.databinding.internal.viewers.SelectionProviderMultipleSelectionObservableList;
import org.eclipse.jface.tests.databinding.EventTrackers.ListChangeEventTracker;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * Tests for SelectionProviderMultiSelectionObservableList.
 * 
 * @since 1.2
 */
public class SelectionProviderMultiSelectionObservableListTest extends TestCase {
	private ISelectionProvider selectionProvider;

	private TableViewer viewer;

	private static String[] model = new String[] { "0", "1", "2", "3" };

	protected void setUp() throws Exception {
		Shell shell = new Shell();
		viewer = new TableViewer(shell, SWT.MULTI);
		viewer.setContentProvider(new ContentProvider());
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
			new SelectionProviderMultipleSelectionObservableList(SWTObservables
					.getRealm(Display.getDefault()), null, Object.class);
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
	public void testAddRemove() {
		SelectionProviderMultipleSelectionObservableList observable = new SelectionProviderMultipleSelectionObservableList(
				SWTObservables.getRealm(Display.getDefault()),
				selectionProvider, Object.class);
		ListChangeEventTracker listener = new ListChangeEventTracker();
		observable.addListChangeListener(listener);
		assertEquals(0, observable.size());

		selectionProvider.setSelection(new StructuredSelection(model[0]));
		assertEquals(1, listener.count);
		assertEquals(1, listener.event.diff.getDifferences().length);
		assertDiffEntry(listener.event.diff.getDifferences()[0], 0, model[0], true);
		assertEquals(observable, listener.event.getObservableList());
		assertEquals(1, observable.size());
		assertEquals(model[0], observable.get(0));

		selectionProvider.setSelection(new StructuredSelection(model[1]));
		assertEquals(2, listener.count);
		assertEquals(2, listener.event.diff.getDifferences().length);
		assertDiffEntry(listener.event.diff.getDifferences()[0], 0, model[1], true);
		assertDiffEntry(listener.event.diff.getDifferences()[1], 1, model[0], false);
		assertEquals(observable, listener.event.getObservableList());
		assertEquals(1, observable.size());
		assertEquals(model[1], observable.get(0));

		selectionProvider.setSelection(new StructuredSelection(new Object[]{model[2],model[3]}));
		assertEquals(3, listener.count);
		assertEquals(3, listener.event.diff.getDifferences().length);
		assertDiffEntry(listener.event.diff.getDifferences()[0], 0, model[2], true);
		assertDiffEntry(listener.event.diff.getDifferences()[1], 1, model[3], true);
		assertDiffEntry(listener.event.diff.getDifferences()[2], 2, model[1], false);
		assertEquals(observable, listener.event.getObservableList());
		assertEquals(2, observable.size());
		assertEquals(model[2], observable.get(0));
		assertEquals(model[3], observable.get(1));
		
		selectionProvider.setSelection(StructuredSelection.EMPTY);
		assertEquals(4, listener.count);
		assertEquals(2, listener.event.diff.getDifferences().length);
		assertDiffEntry(listener.event.diff.getDifferences()[0], 1, model[3], false);
		assertDiffEntry(listener.event.diff.getDifferences()[1], 0, model[2], false);
		assertEquals(observable, listener.event.getObservableList());
		assertEquals(0, observable.size());
		
		observable.add(model[1]);
		assertEquals(5, listener.count);
		assertEquals(1, listener.event.diff.getDifferences().length);
		assertDiffEntry(listener.event.diff.getDifferences()[0], 0, model[1], true);
		assertEquals(observable, listener.event.getObservableList());
		assertEquals(1, ((IStructuredSelection)viewer.getSelection()).size());

		observable.add(0, model[2]);
		assertEquals(6, listener.count);
		assertEquals(1, listener.event.diff.getDifferences().length);
		// This is a bit surprising (we added at index 0 but the event says index 1).
		// It is to the fact that the observable list tracks the underlying selection
		// provider's notion of which element is at which index.
		assertDiffEntry(listener.event.diff.getDifferences()[0], 1, model[2], true);
		assertEquals(observable, listener.event.getObservableList());
		assertEquals(2, ((IStructuredSelection)viewer.getSelection()).size());

		observable.clear();
		assertEquals(7, listener.count);
		assertEquals(2, listener.event.diff.getDifferences().length);
		assertDiffEntry(listener.event.diff.getDifferences()[0], 1, model[2], false);
		assertDiffEntry(listener.event.diff.getDifferences()[1], 0, model[1], false);
		assertEquals(observable, listener.event.getObservableList());
		assertEquals(0, ((IStructuredSelection)viewer.getSelection()).size());
}

	/**
	 * @param diffEntry
	 * @param position
	 * @param element
	 * @param isAddition
	 */
	private void assertDiffEntry(ListDiffEntry diffEntry, int position,
			String element, boolean isAddition) {
		assertEquals(isAddition, diffEntry.isAddition());
		assertEquals(position, diffEntry.getPosition());
		assertEquals(element, diffEntry.getElement());
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
