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

import org.eclipse.core.runtime.IStatus;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.XMLMemento;
import org.eclipse.ui.internal.IWindowTrim;
import org.eclipse.ui.internal.IWorkbenchConstants;
import org.eclipse.ui.internal.WorkbenchWindow;
import org.eclipse.ui.internal.layout.TrimLayout;
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
		TrimLayout layout = window.getTrimLayout();
		int[] id = layout.getAreaIDs();
		for (int i = 0; i < id.length; ++i) {
			assertEquals("temporary IDs", i, id[i]);
		}
	}

	/**
	 * Test the basic trim layout of a workbench window.
	 * 
	 * @throws Throwable
	 *             on error
	 */
	public void testTrimInformation() throws Throwable {
		WorkbenchWindow window = (WorkbenchWindow) openTestWindow();
		TrimLayout layout = window.getTrimLayout();
		// layout.refreshAreaDescriptions();
		IWindowTrim[] descs = layout.getAreaDescription(TrimLayout.BOTTOM);
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
		TrimLayout layout = window.getTrimLayout();
		// layout.refreshAreaDescriptions();
		IWindowTrim[] trim = layout.getAreaDescription(TrimLayout.BOTTOM);
		validatePositions(DEFAULT_BOTTOM, trim);

		swapPostition(trim, 1, 3);
		layout.updateAreaDescription(TrimLayout.BOTTOM, trim);
		// layout.applyAreaDescriptions();
		window.getShell().layout(true, true);
		// layout.refreshAreaDescriptions();

		trim = layout.getAreaDescription(TrimLayout.BOTTOM);
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
		TrimLayout layout = window.getTrimLayout();
		// layout.refreshAreaDescriptions();
		IWindowTrim[] trim = layout.getAreaDescription(TrimLayout.BOTTOM);
		validatePositions(DEFAULT_BOTTOM, trim);

		swapPostition(trim, 0, 3);
		layout.updateAreaDescription(TrimLayout.BOTTOM, trim);
		// layout.applyAreaDescriptions();
		window.getShell().layout(true, true);
		// layout.refreshAreaDescriptions();

		trim = layout.getAreaDescription(TrimLayout.BOTTOM);
		validatePositions(SWAPPED_FASTVIEW, trim);
	}

	/**
	 * Test that the save-state is recording trim layout.
	 * 
	 * @throws Throwable
	 */
	public void testSaveWorkbenchWindow() throws Throwable {
		WorkbenchWindow window = (WorkbenchWindow) openTestWindow();
		// TrimLayout layout = window.getTrimLayout();
		// layout.refreshAreaDescriptions();
		XMLMemento state = XMLMemento
				.createWriteRoot(IWorkbenchConstants.TAG_WINDOW);
		IStatus rc = window.saveState(state);
		assertEquals(IStatus.OK, rc.getSeverity());

		IMemento trim = state.getChild(IWorkbenchConstants.TAG_TRIM);
		assertNotNull(trim);

		int[] ids = window.getTrimLayout().getAreaIDs();
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
		TrimLayout layout = window.getTrimLayout();
		// layout.refreshAreaDescriptions();
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
		for (; childIdx < children.length; childIdx++) {
			if (children[childIdx].getID().equals("1")) {
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

		// TrimLayout layout = window.getTrimLayout();
		// layout.refreshAreaDescriptions();
		IWindowTrim[] windowTrim = layout.getAreaDescription(TrimLayout.BOTTOM);
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
		TrimLayout layout = window.getTrimLayout();
		// layout.refreshAreaDescriptions();
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
		for (; childIdx < children.length; childIdx++) {
			if (children[childIdx].getID().equals("1")) {
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

		IMemento left = trim
				.createChild(IWorkbenchConstants.TAG_TRIM_AREA, "2");
		left.createChild(IWorkbenchConstants.TAG_TRIM_ITEM, id);
		window.restoreState(state, window.getActivePage().getPerspective());
		window.getShell().layout(true, true);

		// TrimLayout layout = window.getTrimLayout();
		// layout.refreshAreaDescriptions();
		IWindowTrim[] windowTrim = layout.getAreaDescription(TrimLayout.BOTTOM);
		assertEquals(3, windowTrim.length);

		windowTrim = layout.getAreaDescription(TrimLayout.LEFT);
		assertEquals(1, windowTrim.length);
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
	private void swapPostition(IWindowTrim[] trim, int pos1, int pos2) {
		IWindowTrim tmp = trim[pos1];
		trim[pos1] = trim[pos2];
		trim[pos2] = tmp;
	}

	/**
	 * Match the returned set of IDs exactly with expected IDs.
	 * 
	 * @param expectedIDs
	 *            the string IDs in order.
	 * @param retrievedIDs
	 *            the current IDs in order.
	 */
	private void validatePositions(String[] expectedIDs,
			IWindowTrim[] retrievedIDs) {
		assertEquals("Number of trim items don't match", expectedIDs.length,
				retrievedIDs.length);
		for (int i = 0; i < expectedIDs.length; ++i) {
			assertEquals("Failed for postition " + i, expectedIDs[i],
					retrievedIDs[i].getId());
		}
	}
}
