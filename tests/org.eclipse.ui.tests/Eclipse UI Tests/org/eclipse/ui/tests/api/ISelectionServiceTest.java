/*******************************************************************************
 * Copyright (c) 2000, 2013 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.api;

import static org.eclipse.ui.tests.harness.util.UITestUtil.openTestWindow;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.tests.SelectionProviderView;
import org.eclipse.ui.tests.harness.util.UITestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests the ISelectionService class.
 */
@RunWith(JUnit4.class)
public class ISelectionServiceTest extends UITestCase implements
		ISelectionListener {
	private IWorkbenchWindow fWindow;

	private IWorkbenchPage fPage;

	// Event state.
	private boolean eventReceived;

	private ISelection eventSelection;

	private IWorkbenchPart eventPart;

	public ISelectionServiceTest() {
		super(ISelectionServiceTest.class.getSimpleName());
	}

	@Override
	protected void doSetUp() throws Exception {
		super.doSetUp();
		fWindow = openTestWindow();
		fPage = fWindow.getActivePage();
	}

	/**
	 * Tests the addSelectionListener method.
	 */
	@Test
	public void testAddSelectionListener() throws Throwable {
		// From Javadoc: "Adds the given selection listener.
		// Has no effect if an identical listener is already registered."

		// Add listener.
		fPage.addSelectionListener(this);

		// Open a view and select something.
		// Verify events are received.
		clearEventState();
		SelectionProviderView view = (SelectionProviderView) fPage
				.showView(SelectionProviderView.ID);
		view.setSelection("Selection");
		assertTrue("EventReceived", eventReceived);
	}

	/**
	 * Tests the removePageListener method.
	 */
	@Test
	public void testRemoveSelectionListener() throws Throwable {
		// From Javadoc: "Removes the given selection listener.
		// Has no affect if an identical listener is not registered."

		// Add and remove listener.
		fPage.addSelectionListener(this);
		fPage.removeSelectionListener(this);

		// Open a view and select something.
		// Verify no events are received.
		clearEventState();
		SelectionProviderView view = (SelectionProviderView) fPage
				.showView(SelectionProviderView.ID);
		view.setSelection("Selection");
		assertTrue("EventReceived", !eventReceived);
	}

	/**
	 * Tests getActivePage.
	 */
	@Test
	public void testGetSelection() throws Throwable {
		// From Javadoc: "Returns the current selection in the active part.
		// If the selection in the active part is <em>undefined</em> (the
		// active part has no selection provider) the result will be
		// <code>null</code>"
		Object actualSel, sel1 = "Selection 1", sel2 = "Selection 2";

		// Open view.
		SelectionProviderView view = (SelectionProviderView) fPage
				.showView(SelectionProviderView.ID);

		// Fire selection and verify.
		view.setSelection(sel1);
		actualSel = unwrapSelection(fPage.getSelection());
		assertEquals("Selection", sel1, actualSel);

		// Fire selection and verify.
		view.setSelection(sel2);
		actualSel = unwrapSelection(fPage.getSelection());
		assertEquals("Selection", sel2, actualSel);

		// Close view and verify.
		fPage.hideView(view);
		assertNull("getSelection", fPage.getSelection());
	}

	/**
	 * Tests getting a selection service local to the part site
	 */
	@Test
	public void testLocalSelectionService() throws Throwable {
		Object sel1 = "Selection 1";

		// Open view.
		SelectionProviderView view2 = (SelectionProviderView) fPage
				.showView(SelectionProviderView.ID_2);
		SelectionProviderView view = (SelectionProviderView) fPage
				.showView(SelectionProviderView.ID);

		ISelectionService service = fWindow.getSelectionService();
		ISelectionService windowService = fWindow
				.getService(ISelectionService.class);
		ISelectionService slaveService = view2.getSite()
				.getService(ISelectionService.class);

		assertTrue(service != slaveService);
		assertEquals(service, windowService);
		assertNotNull(service);
		assertNotNull(slaveService);

		slaveService.addSelectionListener(this);
		view.setSelection(sel1);

		// Should receive selection events
		assertTrue("EventReceived", eventReceived);
		assertEquals("EventPart", view, eventPart);
		assertEquals("Event Selection", sel1, unwrapSelection(eventSelection));

		fPage.hideView(view2);
		clearEventState();

		view.setSelection(sel1);

		// Should not receive selection events
		assertFalse(eventReceived);
		assertNull(eventPart);
		assertNull(eventSelection);
	}

	/**
	 * Test event firing for inactive parts. In this scenario the event should
	 * not be fired.
	 */
	@Test
	public void testSelectionEventWhenInactive() throws Throwable {
		Object sel1 = "Selection 1", sel2 = "Selection 2";

		// Add listener.
		fPage.addSelectionListener(this);

		// Open two views.
		SelectionProviderView view1 = (SelectionProviderView) fPage
				.showView(SelectionProviderView.ID);
		SelectionProviderView view2 = (SelectionProviderView) fPage
				.showView(SelectionProviderView.ID_2);

		// Fire selection from the second.
		// Verify it is received.
		clearEventState();
		view2.setSelection(sel2);
		assertTrue("EventReceived", eventReceived);
		assertEquals("EventPart", view2, eventPart);
		assertEquals("Event Selection", sel2, unwrapSelection(eventSelection));

		// Fire selection from the first.
		// Verify it is NOT received.
		clearEventState();
		view1.setSelection(sel1);
		assertTrue("Unexpected selection events received", !eventReceived);
	}

	/**
	 * Test event firing when activated.
	 */
	@Test
	public void testSelectionEventWhenActivated() throws Throwable {
		// From Javadoc: "Adds the given selection listener.
		// Has no effect if an identical listener is already registered."
		Object sel1 = "Selection 1", sel2 = "Selection 2";

		// Add listener.
		fPage.addSelectionListener(this);

		// Open a view and select something.
		SelectionProviderView view1 = (SelectionProviderView) fPage
				.showView(SelectionProviderView.ID);
		view1.setSelection(sel1);

		// Open another view and select something.
		SelectionProviderView view2 = (SelectionProviderView) fPage
				.showView(SelectionProviderView.ID_2);
		view2.setSelection(sel2);

		// Activate the first view.
		// Verify that selection events are fired.
		clearEventState();
		fPage.activate(view1);
		assertTrue("EventReceived", eventReceived);
		assertEquals("EventPart", view1, eventPart);
		assertEquals("Event Selection", sel1, unwrapSelection(eventSelection));

		// Activate the second view.
		// Verify that selection events are fired.
		clearEventState();
		fPage.activate(view2);
		assertTrue("EventReceived", eventReceived);
		assertEquals("EventPart", view2, eventPart);
		assertEquals("Event Selection", sel2, unwrapSelection(eventSelection));
	}

	/**
	 * Unwrap a selection.
	 */
	private Object unwrapSelection(ISelection sel) {
		if (sel instanceof IStructuredSelection) {
			IStructuredSelection struct = (IStructuredSelection) sel;
			if (struct.size() == 1) {
				return struct.getFirstElement();
			}
		}
		return null;
	}

	/**
	 * Clear the event state.
	 */
	private void clearEventState() {
		eventReceived = false;
		eventPart = null;
		eventSelection = null;
	}

	/*
	 * @see ISelectionListener#selectionChanged(IWorkbenchPart, ISelection)
	 */
	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		eventReceived = true;
		eventPart = part;
		eventSelection = selection;
	}

}
