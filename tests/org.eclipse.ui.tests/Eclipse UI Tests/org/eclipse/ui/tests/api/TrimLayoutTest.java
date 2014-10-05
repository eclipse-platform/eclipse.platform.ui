/*******************************************************************************
 * Copyright (c) 2005, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.api;

import java.util.Iterator;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IWorkbenchPreferenceConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.XMLMemento;
import org.eclipse.ui.internal.IWorkbenchConstants;
import org.eclipse.ui.internal.WindowTrimProxy;
import org.eclipse.ui.internal.WorkbenchWindow;
import org.eclipse.ui.internal.layout.ITrimManager;
import org.eclipse.ui.internal.layout.IWindowTrim;
import org.eclipse.ui.internal.util.PrefUtil;
import org.eclipse.ui.tests.harness.util.UITestCase;

/**
 * Test to exercise the trim layout save/restore/modify support.
 * 
 * @since 3.2
 */
public class TrimLayoutTest extends UITestCase {

	private static final String BUTTON_B_ID = "my.button.b";

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
		WorkbenchWindow window = openWorkbenchWindow();
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
		WorkbenchWindow window = openWorkbenchWindow();
		ITrimManager trimManager = window.getTrimManager();		
		validateDefaultBottomLayout(trimManager);
	}

	/**
	 * Swap the status line with the ProgressRegion, and check that the control
	 * order has been updated.
	 * 
	 * @throws Throwable
	 */
	public void testMoveStatusLine() throws Throwable {
		WorkbenchWindow window = openWorkbenchWindow();
		ITrimManager trimManager = window.getTrimManager();		
		validateDefaultBottomLayout(trimManager);
		
		// Capture the ids of the 1st and 3rd elements
		@SuppressWarnings("rawtypes")
		List trim = trimManager.getAreaTrim(SWT.BOTTOM);		
		String id1 = ((IWindowTrim) trim.get(1)).getId();
		String id3 = ((IWindowTrim) trim.get(3)).getId();

		// Swap the first and third trim elements
		swapPostition(trim, 1, 3);
		trimManager.updateAreaTrim(ITrimManager.BOTTOM, trim, false);
		window.getShell().layout(true, true);

		// Check the swap
		trim = trimManager.getAreaTrim(ITrimManager.BOTTOM);
		assertTrue("element failed to swap", getIndexOf(trim, id1) == 3);
		assertTrue("element failed to swap", getIndexOf(trim, id3) == 1);
	}

	/**
	 * Swap the fast view bar and the progress region, and then check that the
	 * controls have been updated.
	 * 
	 * @throws Throwable
	 */
	public void testMoveFastViewBar() throws Throwable {
		WorkbenchWindow window = openWorkbenchWindow();
		ITrimManager trimManager = window.getTrimManager();				
		validateDefaultBottomLayout(trimManager);
		
		// Capture the ids of the 1st and 3rd elements
		@SuppressWarnings("rawtypes")
		List trim = trimManager.getAreaTrim(SWT.BOTTOM);		
		String id0 = ((IWindowTrim) trim.get(0)).getId();
		String id3 = ((IWindowTrim) trim.get(3)).getId();

		// Swap the zero'th and third trim elements
		swapPostition(trim, 0, 3);
		trimManager.updateAreaTrim(ITrimManager.BOTTOM, trim, false);
		window.getShell().layout(true, true);

		// Check the swap
		trim = trimManager.getAreaTrim(ITrimManager.BOTTOM);
		assertTrue("element failed to swap", getIndexOf(trim, id0) == 3);
		assertTrue("element failed to swap", getIndexOf(trim, id3) == 0);
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
		WorkbenchWindow window = openWorkbenchWindow();
		ITrimManager trimManager = window.getTrimManager();		
		validateDefaultBottomLayout(trimManager);

		// Remove the Heap Status
		@SuppressWarnings("rawtypes")
		List trim = trimManager.getAreaTrim(ITrimManager.BOTTOM);
		int hsIndex = getIndexOf(trim, "org.eclipse.ui.internal.HeapStatus");
		trim.remove(hsIndex);
		trimManager.updateAreaTrim(ITrimManager.BOTTOM, trim, true);
		window.getShell().layout(true, true);

		// Make sure that its gone
		trim = trimManager.getAreaTrim(ITrimManager.BOTTOM);
		hsIndex = getIndexOf(trim, "org.eclipse.ui.internal.HeapStatus");
		assertTrue("HeapStatus failed to remove", hsIndex == -1);
	}

	/**
	 * Test the public API to add a piece of trim to the end of the trim area.
	 * 
	 * @throws Throwable
	 */
	public void testAddExtraTrim() throws Throwable {
		WorkbenchWindow window = openWorkbenchWindow();
		ITrimManager trimManager = window.getTrimManager();
		assertTrue(
				"The window should have it's top banner in place",
				trimManager
						.getTrim("org.eclipse.ui.internal.WorkbenchWindow.topBar") != null);
		
		TrimList trimList = new TrimList(window.getShell());
		trimManager.addTrim(ITrimManager.TOP, trimList);
		window.getShell().layout();

		@SuppressWarnings("rawtypes")
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
		WorkbenchWindow window = openWorkbenchWindow();
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

		@SuppressWarnings("rawtypes")
		List topTrim = trimManager.getAreaTrim(ITrimManager.TOP);
		validatePositions(TOP_BUTTON_TRIM, topTrim);
	}

	/**
	 * Test that the save-state is recording trim layout.
	 * 
	 * @throws Throwable
	 */
	public void testSaveWorkbenchWindow() throws Throwable {
		WorkbenchWindow window = openWorkbenchWindow();

		XMLMemento state = XMLMemento
				.createWriteRoot(IWorkbenchConstants.TAG_WINDOW);
//		IStatus rc = window.saveState(state);
//		assertEquals(IStatus.OK, rc.getSeverity());

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
		WorkbenchWindow window = openWorkbenchWindow();
		ITrimManager trimManager = window.getTrimManager();
		validateDefaultBottomLayout(trimManager);
		
		int bottomTrimCount = trimManager.getAreaTrim(SWT.BOTTOM).size();
		
		// Create a memento and write the state to it...
		XMLMemento state = XMLMemento
				.createWriteRoot(IWorkbenchConstants.TAG_WINDOW);
//		IStatus rc = window.saveState(state);
		
		// Did the save work?
//		assertEquals(IStatus.OK, rc.getSeverity());
		
		// Does it have the right info?
		IMemento trimMemento = state.getChild(IWorkbenchConstants.TAG_TRIM);
		assertNotNull(trimMemento);

		// Is the child content the same
		IMemento[] children = trimMemento
				.getChildren(IWorkbenchConstants.TAG_TRIM_AREA);
		int childIdx = 0;
		IMemento bottomTrim = null;
		String bottomId = new Integer(SWT.BOTTOM).toString();

		// Find the 'bottom' trim
		for (; childIdx < children.length; childIdx++) {
			if (children[childIdx].getID().equals(bottomId)) {
				bottomTrim = children[childIdx];
				break;
			}
		}
		assertNotNull(bottomTrim);

		// Make sure we have the right number of entries
		children = bottomTrim.getChildren(IWorkbenchConstants.TAG_TRIM_ITEM);
		assertEquals(bottomTrimCount, children.length);

		// 'swap' the 0 and 3 trim using only the stored ids
		String id0 = children[0].getID();
		String id3 = children[3].getID();
		children[0].putString(IMemento.TAG_ID, id3);
		children[3].putString(IMemento.TAG_ID, id0);

		// Restore the trim from the modified state
//		window.restoreState(state, window.getActivePage().getPerspective());
		// FIXME: windowRestoreState was a compile error
		fail("window.restoreState() was a compile error");

		window.getShell().layout(true, true);

		@SuppressWarnings("rawtypes")
		List trim = trimManager.getAreaTrim(ITrimManager.BOTTOM);
		assertTrue("Restore has wrong layout", getIndexOf(trim, id0) == 3);
		assertTrue("Restore has wrong layout", getIndexOf(trim, id3) == 0);
	}

	/**
	 * Test that the workbench window can restore trim state from an
	 * <code>IMemento</code> where a trim item has moved sides.
	 * 
	 * @throws Throwable
	 */
	public void testRestoreStateWithLocationChange() throws Throwable {
		WorkbenchWindow window = openWorkbenchWindow();
		ITrimManager trimManager = window.getTrimManager();
		validateDefaultBottomLayout(trimManager);
		
		int bottomTrimCount = trimManager.getAreaTrim(SWT.BOTTOM).size();

		XMLMemento state = XMLMemento
				.createWriteRoot(IWorkbenchConstants.TAG_WINDOW);
		
//		IStatus rc = window.saveState(state);
		
		// FIXME: window.saveState() was a compile error
		fail("window.saveState() was a compile error");
		
//		assertEquals(IStatus.OK, rc.getSeverity());
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
		assertEquals(bottomTrimCount, children.length);
		
		// kinda fake to remove the fast view bar from the bottom
		String id = children[0].getID();
		children[0].putString(IMemento.TAG_ID, children[3].getID());

		IMemento left = trim.createChild(IWorkbenchConstants.TAG_TRIM_AREA,
				new Integer(SWT.LEFT).toString());
		left.createChild(IWorkbenchConstants.TAG_TRIM_ITEM, id);
		
//		window.restoreState(state, window.getActivePage().getPerspective());
		
		// FIXME: window.restoreState() was a compile error
		fail("window.restoreState() was a compile error");

		window.getShell().layout(true, true);

		@SuppressWarnings("rawtypes")
		List windowTrim = trimManager.getAreaTrim(ITrimManager.BOTTOM);
		assertEquals(bottomTrimCount-1, windowTrim.size());

		windowTrim = trimManager.getAreaTrim(ITrimManager.LEFT);
		assertEquals(1, windowTrim.size());
	}

	/**
	 * These tests use 'internal' methods from the <code>WorkbenchWindow</code>.
	 * This method ensures that the tests will fail if the <code>openTestWindow</code>
	 * ever returns anything else
	 * 
	 * @return The type-safe WorkbenchWindow 
	 */
	private WorkbenchWindow openWorkbenchWindow() {
		IWorkbenchWindow iw = openTestWindow();
		assertTrue("Window must be a WorkbenchWindow", (iw instanceof WorkbenchWindow));
		
		return (WorkbenchWindow)iw;
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
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void swapPostition(List trim, int pos1, int pos2) {
		Object tmp = trim.get(pos1);
		trim.set(pos1, trim.get(pos2));
		trim.set(pos2, tmp);
	}

	/**
	 * Get the position of the given trim element from the trim
	 * 
	 * @param trimIds The list of ids returned by the TrimManager
	 * @param id The id of the trim to get the index of
	 * @return The zero-based index or -1 if not found
	 */
	@SuppressWarnings("rawtypes")
	private int getIndexOf(List trimIds, String id) {
		int index = 0;
		for (Iterator iterator = trimIds.iterator(); iterator.hasNext();) {
			IWindowTrim trim = (IWindowTrim) iterator.next();
			if (id.equals(trim.getId()))
				return index;
			index++;
		}
		
		return -1;
	}
	
	/**
	 * Ensure that all the base trim is there and has
	 * the correct -relative- positions
	 * 
	 * @param descs The ordered list of trim descriptors
	 * for the bottom trim area
	 */
	private void validateDefaultBottomLayout(ITrimManager trimManager) {
		@SuppressWarnings("rawtypes")
		List descs = trimManager.getAreaTrim(SWT.BOTTOM);

		// Must have at least 4 elements
		assertTrue("Too few trim elements", descs.size() >= 4);

		// Ensure that all the base trim is there and has
		// the correct -relative- positions
		int fvbIndex = getIndexOf(descs, "org.eclise.ui.internal.FastViewBar");
		assertTrue("Fast View Bar not found", fvbIndex != -1);
		int slIndex = getIndexOf(descs, "org.eclipse.jface.action.StatusLineManager");
		assertTrue("StatusLine not found", slIndex != -1);
		int hsIndex = getIndexOf(descs, "org.eclipse.ui.internal.HeapStatus");
		assertTrue("Heap Status not found", hsIndex != -1);
		int prIndex = getIndexOf(descs, "org.eclipse.ui.internal.progress.ProgressRegion");
		assertTrue("Progress Region not found", prIndex != -1);
		
		assertTrue("Fast View out of position", fvbIndex < slIndex);
		assertTrue("Status Line out of position", slIndex < hsIndex);
		assertTrue("Heap Status out of position", hsIndex < prIndex);
	}
	
	/**
	 * Match the returned set of IDs exactly with expected IDs.
	 * 
	 * @param expectedIDs
	 *            the string IDs in order.
	 * @param retrievedIDs
	 *            the current IDs in order.
	 */
	@SuppressWarnings("rawtypes")
	private void validatePositions(String[] expectedIDs, List retrievedIDs) {
		assertEquals("Number of trim items don't match", expectedIDs.length,
				retrievedIDs.size());

		for (int i = 0; i < expectedIDs.length; ++i) {
			assertEquals("Failed for postition " + i, expectedIDs[i],
					((IWindowTrim) retrievedIDs.get(i)).getId());
		}
	}

	@Override
	protected void doSetUp() throws Exception {
		super.doSetUp();
		
		// Ensure that the HeapStatus is showing
		fHeapStatusPref = PrefUtil.getAPIPreferenceStore().getBoolean(
				IWorkbenchPreferenceConstants.SHOW_MEMORY_MONITOR);
		PrefUtil.getAPIPreferenceStore().setValue(
				IWorkbenchPreferenceConstants.SHOW_MEMORY_MONITOR, true);
	}

	@Override
	protected void doTearDown() throws Exception {
		PrefUtil.getAPIPreferenceStore().setValue(
				IWorkbenchPreferenceConstants.SHOW_MEMORY_MONITOR,
				fHeapStatusPref);
		super.doTearDown();
	}

}
