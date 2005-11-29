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
import org.eclipse.ui.IMemento;
import org.eclipse.ui.ITrimManager;
import org.eclipse.ui.IWorkbenchPreferenceConstants;
import org.eclipse.ui.XMLMemento;
import org.eclipse.ui.IWindowTrim;
import org.eclipse.ui.internal.IWorkbenchConstants;
import org.eclipse.ui.internal.WorkbenchWindow;
import org.eclipse.ui.internal.util.PrefUtil;
import org.eclipse.ui.tests.util.UITestCase;

/**
 * Test to exercise the trim layout save/restore/modify support.
 * 
 * @since 3.2
 */
public class TrimLayoutTest extends UITestCase {

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
		WorkbenchWindow window = (WorkbenchWindow) openTestWindow();
		ITrimManager layout = window.getTrimManager();
		int[] ids = layout.getAreaIds();
		assertEquals("number of trim areas", 4, ids.length);
	}

	/**
	 * Test the basic trim layout of a workbench window.
	 * 
	 * @throws Throwable
	 *             on error
	 */
	public void testTrimInformation() throws Throwable {
		WorkbenchWindow window = (WorkbenchWindow) openTestWindow();
		ITrimManager layout = window.getTrimManager();

		List descs = layout.getAreaDescription(SWT.BOTTOM);
		validatePositions(DEFAULT_BOTTOM, descs);
	}

	/**
	 * Swap the status line with the ProgressRegion, and check that the control
	 * order has been updated.
	 * 
	 * @throws Throwable
	 */
	public void testMoveStatusLine() throws Throwable {
		WorkbenchWindow window = (WorkbenchWindow) openTestWindow();
		ITrimManager layout = window.getTrimManager();

		List trim = layout.getAreaDescription(ITrimManager.BOTTOM);
		validatePositions(DEFAULT_BOTTOM, trim);

		swapPostition(trim, 1, 3);
		layout.updateAreaDescription(ITrimManager.BOTTOM, trim);

		window.getShell().layout(true, true);


		trim = layout.getAreaDescription(ITrimManager.BOTTOM);
		validatePositions(SWAPPED_STATUS_LINE, trim);
	}

	/**
	 * Swap the fast view bar and the progress region, and then check that the
	 * controls have been updated.
	 * 
	 * @throws Throwable
	 */
	public void testMoveFastViewBar() throws Throwable {
		WorkbenchWindow window = (WorkbenchWindow) openTestWindow();
		ITrimManager layout = window.getTrimManager();

		List trim = layout.getAreaDescription(ITrimManager.BOTTOM);
		validatePositions(DEFAULT_BOTTOM, trim);

		swapPostition(trim, 0, 3);
		layout.updateAreaDescription(ITrimManager.BOTTOM, trim);

		window.getShell().layout(true, true);

		trim = layout.getAreaDescription(ITrimManager.BOTTOM);
		validatePositions(SWAPPED_FASTVIEW, trim);
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
		ITrimManager layout = window.getTrimManager();

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


		List windowTrim = layout.getAreaDescription(ITrimManager.BOTTOM);
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
		ITrimManager layout = window.getTrimManager();

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


		List windowTrim = layout.getAreaDescription(ITrimManager.BOTTOM);
		assertEquals(3, windowTrim.size());

		windowTrim = layout.getAreaDescription(ITrimManager.LEFT);
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
