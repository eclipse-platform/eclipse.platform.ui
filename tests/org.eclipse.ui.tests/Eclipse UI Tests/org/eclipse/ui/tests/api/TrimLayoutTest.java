/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.api;

import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.ITrimManager;
import org.eclipse.ui.IWindowTrim;
import org.eclipse.ui.IWorkbenchPreferenceConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.XMLMemento;
import org.eclipse.ui.internal.IWorkbenchConstants;
import org.eclipse.ui.internal.WindowTrimProxy;
import org.eclipse.ui.internal.WorkbenchWindow;
import org.eclipse.ui.internal.util.PrefUtil;
import org.eclipse.ui.tests.harness.util.UITestCase;

/**
 * Test to exercise the trim layout save/restore/modify support.
 * 
 * @since 3.2
 */
public class TrimLayoutTest extends UITestCase {

	private static final String BUTTON_B_ID = "my.button.b";

	public static final String[] DEFAULT_BOTTOM = {
			"org.eclise.ui.internal.FastViewBar",
			"org.eclipse.jface.action.StatusLineManager",
			"org.eclipse.ui.internal.HeapStatus",
			"org.eclipse.ui.internal.progress.ProgressRegion" };

	public static final String[] SWAPPED_STATUS_LINE = {
			"org.eclise.ui.internal.FastViewBar",
			"org.eclipse.ui.internal.progress.ProgressRegion",
			"org.eclipse.ui.internal.HeapStatus",
			"org.eclipse.jface.action.StatusLineManager" };

	public static final String[] SWAPPED_FASTVIEW = {
			"org.eclipse.ui.internal.progress.ProgressRegion",
			"org.eclipse.jface.action.StatusLineManager",
			"org.eclipse.ui.internal.HeapStatus",
			"org.eclise.ui.internal.FastViewBar", };

	public static final String[] REMOVED_HEAP_STATUS = {
			"org.eclise.ui.internal.FastViewBar",
			"org.eclipse.jface.action.StatusLineManager",
			"org.eclipse.ui.internal.progress.ProgressRegion" };

	public static final String[] TOP_TRIM_LIST = {
			"org.eclipse.ui.internal.WorkbenchWindow.topBar",
			TrimList.TRIM_LIST_ID };

	public static final String[] TOP_BUTTON_TRIM = {
			"org.eclipse.ui.internal.WorkbenchWindow.topBar", BUTTON_B_ID,
			TrimList.TRIM_LIST_ID };

	private boolean fHeapStatusPref;

	/**
	 * @param testName
	 */
	public TrimLayoutTest(String testName) {
		super(testName);
	}

	/**
	 * Will test that the trim area IDs are returned.
	 * 
	 * @throws Throwable
	 */
	public void testGetIDs() throws Throwable {
		IWorkbenchWindow window = openTestWindow();
		ITrimManager trimManager = window.getTrimManager();
		int[] ids = trimManager.getAreaIds();
		assertEquals("number of trim areas", 4, ids.length);
	}

	/**
	 * Test the basic trim layout of a workbench window.
	 * 
	 * @throws Throwable
	 *             on error
	 */
	public void testTrimInformation() throws Throwable {
		IWorkbenchWindow window = openTestWindow();
		ITrimManager trimManager = window.getTrimManager();

		List descs = trimManager.getAreaTrim(SWT.BOTTOM);
		validatePositions(DEFAULT_BOTTOM, descs);
	}

	/**
	 * Swap the status line with the ProgressRegion, and check that the control
	 * order has been updated.
	 * 
	 * @throws Throwable
	 */
	public void testMoveStatusLine() throws Throwable {
		IWorkbenchWindow window = openTestWindow();
		ITrimManager trimManager = window.getTrimManager();

		List trim = trimManager.getAreaTrim(ITrimManager.BOTTOM);
		validatePositions(DEFAULT_BOTTOM, trim);

		swapPostition(trim, 1, 3);
		trimManager.updateAreaTrim(ITrimManager.BOTTOM, trim, false);

		window.getShell().layout(true, true);

		trim = trimManager.getAreaTrim(ITrimManager.BOTTOM);
		validatePositions(SWAPPED_STATUS_LINE, trim);
	}

	/**
	 * Swap the fast view bar and the progress region, and then check that the
	 * controls have been updated.
	 * 
	 * @throws Throwable
	 */
	public void testMoveFastViewBar() throws Throwable {
		IWorkbenchWindow window = openTestWindow();
		ITrimManager trimManager = window.getTrimManager();

		List trim = trimManager.getAreaTrim(ITrimManager.BOTTOM);
		validatePositions(DEFAULT_BOTTOM, trim);

		swapPostition(trim, 0, 3);
		trimManager.updateAreaTrim(ITrimManager.BOTTOM, trim, false);

		window.getShell().layout(true, true);

		trim = trimManager.getAreaTrim(ITrimManager.BOTTOM);
		validatePositions(SWAPPED_FASTVIEW, trim);
	}

	/**
	 * This test isn't really about removing trim, just testing that the if the
	 * heap status trim is not in the trim list, it's removed from the bottom
	 * trim area.
	 * 
	 * @throws Throwable
	 *             on error
	 */
	public void testRemoveHeapStatus() throws Throwable {
		IWorkbenchWindow window = openTestWindow();
		ITrimManager trimManager = window.getTrimManager();

		List trim = trimManager.getAreaTrim(ITrimManager.BOTTOM);
		validatePositions(DEFAULT_BOTTOM, trim);

		trim.remove(2);
		trimManager.updateAreaTrim(ITrimManager.BOTTOM, trim, true);

		window.getShell().layout(true, true);

		trim = trimManager.getAreaTrim(ITrimManager.BOTTOM);
		validatePositions(REMOVED_HEAP_STATUS, trim);
	}

	/**
	 * Test the public API to add a piece of trim to the end of the trim area.
	 * 
	 * @throws Throwable
	 */
	public void testAddExtraTrim() throws Throwable {
		IWorkbenchWindow window = openTestWindow();
		ITrimManager trimManager = window.getTrimManager();

		assertTrue(
				"The window should have it's top banner in place",
				trimManager
						.getTrim("org.eclipse.ui.internal.WorkbenchWindow.topBar") != null);
		
		TrimList trimList = new TrimList(window.getShell());
		trimManager.addTrim(ITrimManager.TOP, trimList);
		window.getShell().layout();

		List trim = trimManager.getAreaTrim(ITrimManager.TOP);
		validatePositions(TOP_TRIM_LIST, trim);
	}

	/**
	 * Test the public API to add a piece of trim before an existing piece of
	 * trim.
	 * 
	 * @throws Throwable
	 */
	public void testPlaceExtraTrim() throws Throwable {
		IWorkbenchWindow window = openTestWindow();
		ITrimManager trimManager = window.getTrimManager();
		
		TrimList trimList = new TrimList(window.getShell());
		trimManager.addTrim(ITrimManager.TOP, trimList);

		// WindowTrimProxy is an internal "quick and dirty" way
		// to just provide a control to the trim ... not public API
		Button b = new Button(window.getShell(), SWT.PUSH);
		b.setText("B");
		IWindowTrim buttonTrim = new WindowTrimProxy(b, BUTTON_B_ID,
				"Button B", SWT.TOP | SWT.BOTTOM, false);
		
		// find an existing piece of trim to use as a reference
		IWindowTrim trim = trimManager.getTrim(TrimList.TRIM_LIST_ID);
		assertTrue(trimList == trim);
		trimManager.addTrim(ITrimManager.TOP, buttonTrim, trim);
		window.getShell().layout();

		List topTrim = trimManager.getAreaTrim(ITrimManager.TOP);
		validatePositions(TOP_BUTTON_TRIM, topTrim);
	}

	/**
	 * Test that the save-state is recording trim layout.
	 * 
	 * @throws Throwable
	 */
	public void testSaveWorkbenchWindow() throws Throwable {
		WorkbenchWindow window = (WorkbenchWindow) openTestWindow();

		XMLMemento state = XMLMemento
				.createWriteRoot(IWorkbenchConstants.TAG_WINDOW);
		IStatus rc = window.saveState(state);
		assertEquals(IStatus.OK, rc.getSeverity());

		IMemento trim = state.getChild(IWorkbenchConstants.TAG_TRIM);
		assertNotNull(trim);

		int[] ids = window.getTrimManager().getAreaIds();
		IMemento[] children = trim
				.getChildren(IWorkbenchConstants.TAG_TRIM_AREA);
		assertTrue("Should have <= " + ids.length + " trim areas",
				children.length <= ids.length);
		assertEquals("Our trim configuration starts with", 2, children.length);
	}

	/**
	 * Test that the workbench window can restore trim state from an
	 * <code>IMemento</code>.
	 * 
	 * @throws Throwable
	 */
	public void testRestoreStateWithChange() throws Throwable {
		WorkbenchWindow window = (WorkbenchWindow) openTestWindow();
		ITrimManager trimManager = window.getTrimManager();

		XMLMemento state = XMLMemento
				.createWriteRoot(IWorkbenchConstants.TAG_WINDOW);
		IStatus rc = window.saveState(state);
		assertEquals(IStatus.OK, rc.getSeverity());
		IMemento trim = state.getChild(IWorkbenchConstants.TAG_TRIM);
		assertNotNull(trim);
		IMemento[] children = trim
				.getChildren(IWorkbenchConstants.TAG_TRIM_AREA);
		int childIdx = 0;
		IMemento bottomTrim = null;
		String bottomId = new Integer(SWT.BOTTOM).toString();

		for (; childIdx < children.length; childIdx++) {
			if (children[childIdx].getID().equals(bottomId)) {
				bottomTrim = children[childIdx];
				break;
			}
		}
		assertNotNull(bottomTrim);

		children = bottomTrim.getChildren(IWorkbenchConstants.TAG_TRIM_ITEM);
		assertEquals(4, children.length);

		String id = children[0].getID();
		children[0].putString(IMemento.TAG_ID, children[3].getID());
		children[3].putString(IMemento.TAG_ID, id);

		window.restoreState(state, window.getActivePage().getPerspective());
		window.getShell().layout(true, true);

		List windowTrim = trimManager.getAreaTrim(ITrimManager.BOTTOM);
		validatePositions(SWAPPED_FASTVIEW, windowTrim);
	}

	/**
	 * Test that the workbench window can restore trim state from an
	 * <code>IMemento</code> where a trim item has moved sides.
	 * 
	 * @throws Throwable
	 */
	public void testRestoreStateWithLocationChange() throws Throwable {
		WorkbenchWindow window = (WorkbenchWindow) openTestWindow();
		ITrimManager trimManager = window.getTrimManager();

		XMLMemento state = XMLMemento
				.createWriteRoot(IWorkbenchConstants.TAG_WINDOW);
		IStatus rc = window.saveState(state);
		assertEquals(IStatus.OK, rc.getSeverity());
		IMemento trim = state.getChild(IWorkbenchConstants.TAG_TRIM);
		assertNotNull(trim);

		IMemento[] children = trim
				.getChildren(IWorkbenchConstants.TAG_TRIM_AREA);
		int childIdx = 0;
		IMemento bottomTrim = null;
		String bottomId = new Integer(SWT.BOTTOM).toString();
		for (; childIdx < children.length; childIdx++) {
			if (children[childIdx].getID().equals(bottomId)) {
				bottomTrim = children[childIdx];
				break;
			}
		}
		assertNotNull(bottomTrim);

		children = bottomTrim.getChildren(IWorkbenchConstants.TAG_TRIM_ITEM);
		assertEquals(4, children.length);
		// kinda fake to remove the fast view bar from the bottom
		String id = children[0].getID();
		children[0].putString(IMemento.TAG_ID, children[3].getID());

		IMemento left = trim.createChild(IWorkbenchConstants.TAG_TRIM_AREA,
				new Integer(SWT.LEFT).toString());
		left.createChild(IWorkbenchConstants.TAG_TRIM_ITEM, id);
		window.restoreState(state, window.getActivePage().getPerspective());
		window.getShell().layout(true, true);

		List windowTrim = trimManager.getAreaTrim(ITrimManager.BOTTOM);
		assertEquals(3, windowTrim.size());

		windowTrim = trimManager.getAreaTrim(ITrimManager.LEFT);
		assertEquals(1, windowTrim.size());
	}

	/**
	 * Swap 2 IDs in the description.
	 * 
	 * @param descs
	 *            the description array
	 * @param pos1
	 *            position 1, from 0
	 * @param pos2
	 *            position 2, from 0
	 */
	private void swapPostition(List trim, int pos1, int pos2) {
		Object tmp = trim.get(pos1);
		trim.set(pos1, trim.get(pos2));
		trim.set(pos2, tmp);
	}

	/**
	 * Match the returned set of IDs exactly with expected IDs.
	 * 
	 * @param expectedIDs
	 *            the string IDs in order.
	 * @param retrievedIDs
	 *            the current IDs in order.
	 */
	private void validatePositions(String[] expectedIDs, List retrievedIDs) {
		assertEquals("Number of trim items don't match", expectedIDs.length,
				retrievedIDs.size());

		for (int i = 0; i < expectedIDs.length; ++i) {
			assertEquals("Failed for postition " + i, expectedIDs[i],
					((IWindowTrim) retrievedIDs.get(i)).getId());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.tests.util.UITestCase#doSetUp()
	 */
	protected void doSetUp() throws Exception {
		super.doSetUp();
		fHeapStatusPref = PrefUtil.getAPIPreferenceStore().getBoolean(
				IWorkbenchPreferenceConstants.SHOW_MEMORY_MONITOR);
		PrefUtil.getAPIPreferenceStore().setValue(
				IWorkbenchPreferenceConstants.SHOW_MEMORY_MONITOR, true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.tests.util.UITestCase#doTearDown()
	 */
	protected void doTearDown() throws Exception {
		PrefUtil.getAPIPreferenceStore().setValue(
				IWorkbenchPreferenceConstants.SHOW_MEMORY_MONITOR,
				fHeapStatusPref);
		super.doTearDown();
	}

}
