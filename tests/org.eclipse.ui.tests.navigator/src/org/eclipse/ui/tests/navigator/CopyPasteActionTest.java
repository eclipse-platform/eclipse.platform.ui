/*******************************************************************************
 * Copyright (c) 2026 vogella GmbH and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     vogella GmbH - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.navigator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.part.ResourceTransfer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for Copy, Cut and Paste actions in the Project Explorer.
 *
 * Uses TEST_VIEWER instead of ProjectExplorer.VIEW_ID to avoid JDT's action
 * providers overriding the navigator's edit actions.
 */
public class CopyPasteActionTest extends NavigatorTestBase {

	private Clipboard _clipboard;

	public CopyPasteActionTest() {
		_navigatorInstanceId = TEST_VIEWER;
	}

	@Override
	@AfterEach
	public void tearDown() throws CoreException {
		if (_clipboard != null) {
			_clipboard.dispose();
			_clipboard = null;
		}
		super.tearDown();
	}

	private Clipboard getClipboard() {
		if (_clipboard == null) {
			_clipboard = new Clipboard(Display.getDefault());
		}
		return _clipboard;
	}

	private ActionContributionItem getAction(IStructuredSelection sel, String label) {
		Object item = verifyMenu(sel, label);
		assertNotNull(item, label + " action not found in context menu");
		assertTrue(item instanceof ActionContributionItem, label + " item should be an ActionContributionItem");
		return (ActionContributionItem) item;
	}

	// --- Copy tests ---

	@Test
	public void testCopyEnablement() throws Exception {
		IFile file = _project.getFile("model.properties");
		IStructuredSelection sel = new StructuredSelection(file);
		_viewer.setSelection(sel);

		ActionContributionItem copyActionItem = getAction(sel, "Copy");
		assertTrue(copyActionItem.getAction().isEnabled(), "Copy action should be enabled for a file");
	}

	@Test
	public void testCopyDisabledForMixedSelection() throws Exception {
		IFile file = _project.getFile("model.properties");
		IStructuredSelection sel = new StructuredSelection(new Object[] { _project, file });
		_viewer.setSelection(sel);

		ActionContributionItem copyActionItem = getAction(sel, "Copy");
		assertFalse(copyActionItem.getAction().isEnabled(), "Copy action should be disabled for mixed selection");
	}

	@Test
	public void testCopyDisabledForEmptySelection() throws Exception {
		IStructuredSelection sel = StructuredSelection.EMPTY;
		_viewer.setSelection(sel);

		Object copyItem = verifyMenu(sel, "Copy");
		assertNull(copyItem, "Copy action should be absent for empty selection");
	}

	@Test
	public void testCopyToClipboard() throws Exception {
		IFile file = _project.getFile("model.properties");
		IStructuredSelection sel = new StructuredSelection(file);
		_viewer.setSelection(sel);

		ActionContributionItem copyActionItem = getAction(sel, "Copy");
		copyActionItem.getAction().run();

		Object contents = getClipboard().getContents(ResourceTransfer.getInstance());
		assertNotNull(contents, "Clipboard should contain resources");
		IResource[] resources = (IResource[]) contents;
		assertEquals(1, resources.length);
		assertEquals(file, resources[0]);
	}

	@Test
	public void testCopyTextTransfer() throws Exception {
		IFile file = _project.getFile("model.properties");
		IStructuredSelection sel = new StructuredSelection(file);
		_viewer.setSelection(sel);

		ActionContributionItem copyActionItem = getAction(sel, "Copy");
		copyActionItem.getAction().run();

		String textContents = (String) getClipboard().getContents(TextTransfer.getInstance());
		assertNotNull(textContents, "Clipboard should contain text");
		assertEquals(file.getName(), textContents.trim());
	}

	@Test
	public void testPasteEnablement() throws Exception {
		IFile file = _project.getFile("model.properties");

		getClipboard().setContents(new Object[] { new IResource[] { file }, file.getName() },
				new Transfer[] { ResourceTransfer.getInstance(), TextTransfer.getInstance() });

		IStructuredSelection sel = new StructuredSelection(_p1);
		_viewer.setSelection(sel);

		ActionContributionItem pasteActionItem = getAction(sel, "Paste");
		assertTrue(pasteActionItem.getAction().isEnabled(), "Paste action should be enabled when clipboard has resources");
	}

	@Test
	public void testCopyPasteRoundTrip() throws Exception {
		IFile file = _project.getFile("model.properties");
		assertTrue(file.exists());

		IStructuredSelection selCopy = new StructuredSelection(file);
		_viewer.setSelection(selCopy);
		getAction(selCopy, "Copy").getAction().run();

		IStructuredSelection selPaste = new StructuredSelection(_p1);
		_viewer.setSelection(selPaste);
		ActionContributionItem pasteActionItem = getAction(selPaste, "Paste");
		assertTrue(pasteActionItem.getAction().isEnabled());
		pasteActionItem.getAction().run();

		IFile pastedFile = _p1.getFile(file.getName());
		waitForCondition("File should be pasted", () -> pastedFile.exists());
		assertTrue(pastedFile.exists(), "Pasted file should exist in target project");
		assertTrue(file.exists(), "Original file should still exist after copy-paste");
	}

	@Test
	public void testCopyDisabledForWorkspaceRoot() throws Exception {
		IStructuredSelection sel = new StructuredSelection(ResourcesPlugin.getWorkspace().getRoot());
		_viewer.setSelection(sel);

		ActionContributionItem copyActionItem = getAction(sel, "Copy");
		assertFalse(copyActionItem.getAction().isEnabled(), "Copy action should be disabled for Workspace Root");
	}

	// --- Cut tests ---

	@Test
	public void testCutEnablement() throws Exception {
		IFile file = _project.getFile("model.properties");
		IStructuredSelection sel = new StructuredSelection(file);
		_viewer.setSelection(sel);

		ActionContributionItem cutActionItem = getAction(sel, "Cut");
		assertTrue(cutActionItem.getAction().isEnabled(), "Cut action should be enabled for a file");
	}

	@Test
	public void testCutDisabledForMixedSelection() throws Exception {
		IFile file = _project.getFile("model.properties");
		IStructuredSelection sel = new StructuredSelection(new Object[] { _project, file });
		_viewer.setSelection(sel);

		ActionContributionItem cutActionItem = getAction(sel, "Cut");
		assertFalse(cutActionItem.getAction().isEnabled(), "Cut action should be disabled for mixed selection");
	}

	@Test
	public void testCutToClipboard() throws Exception {
		IFile file = _project.getFile("model.properties");
		IStructuredSelection sel = new StructuredSelection(file);
		_viewer.setSelection(sel);

		ActionContributionItem cutActionItem = getAction(sel, "Cut");
		cutActionItem.getAction().run();

		Object contents = getClipboard().getContents(ResourceTransfer.getInstance());
		assertNotNull(contents, "Clipboard should contain resources after cut");
		IResource[] resources = (IResource[]) contents;
		assertEquals(1, resources.length);
		assertEquals(file, resources[0]);
	}

	@Test
	public void testCutPasteMovesResource() throws Exception {
		IFile file = _project.getFile("model.properties");
		assertTrue(file.exists());

		IStructuredSelection selCut = new StructuredSelection(file);
		_viewer.setSelection(selCut);
		getAction(selCut, "Cut").getAction().run();

		IStructuredSelection selPaste = new StructuredSelection(_p1);
		_viewer.setSelection(selPaste);
		ActionContributionItem pasteActionItem = getAction(selPaste, "Paste");
		assertTrue(pasteActionItem.getAction().isEnabled());
		pasteActionItem.getAction().run();

		IFile movedFile = _p1.getFile(file.getName());
		waitForCondition("File should be moved", () -> movedFile.exists());
		assertTrue(movedFile.exists(), "Moved file should exist in target project");
		waitForCondition("Original file should be gone", () -> !file.exists());
		assertFalse(file.exists(), "Original file should no longer exist after cut-paste");
	}

	@Test
	public void testCopyAfterCutResetsCutState() throws Exception {
		IFile file1 = _project.getFile("model.properties");
		IFile file2 = _p2.getFile("file1.txt");

		// Cut file1
		IStructuredSelection selCut = new StructuredSelection(file1);
		_viewer.setSelection(selCut);
		getAction(selCut, "Cut").getAction().run();

		// Copy file2 (resets cut state)
		IStructuredSelection selCopy = new StructuredSelection(file2);
		_viewer.setSelection(selCopy);
		getAction(selCopy, "Copy").getAction().run();

		// Paste into _p1
		IStructuredSelection selPaste = new StructuredSelection(_p1);
		_viewer.setSelection(selPaste);
		getAction(selPaste, "Paste").getAction().run();

		// Verify it was a copy, not a move
		IFile pastedFile = _p1.getFile(file2.getName());
		waitForCondition("File should be pasted", () -> pastedFile.exists());
		assertTrue(pastedFile.exists());
		assertTrue(file2.exists(), "Source file should still exist (it was a copy)");
		assertTrue(file1.exists(), "Cut file should still exist (copy reset the cut state)");
	}

	@Test
	public void testContextMenuContainsEditActions() throws Exception {
		IFile file = _project.getFile("model.properties");
		IStructuredSelection sel = new StructuredSelection(file);
		_viewer.setSelection(sel);

		getAction(sel, "Cut");
		getAction(sel, "Copy");
		getAction(sel, "Paste");
		getAction(sel, "Delete");
	}
}
