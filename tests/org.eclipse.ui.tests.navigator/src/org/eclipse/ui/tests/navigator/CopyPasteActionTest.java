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
import org.eclipse.ui.navigator.resources.ProjectExplorer;
import org.eclipse.ui.part.ResourceTransfer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for Copy and Paste actions in the Project Explorer.
 */
public class CopyPasteActionTest extends NavigatorTestBase {

	private Clipboard _clipboard;

	public CopyPasteActionTest() {
		_navigatorInstanceId = ProjectExplorer.VIEW_ID;
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
		// Mixed selection (Project + File) should disable Copy action
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
		// For an empty selection, the Resource action provider is not even matched,
		// so the action should be absent from the menu.
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

		// Manually put the file on the clipboard
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

		// 1. Copy
		IStructuredSelection selCopy = new StructuredSelection(file);
		_viewer.setSelection(selCopy);
		ActionContributionItem copyActionItem = getAction(selCopy, "Copy");
		copyActionItem.getAction().run();

		// 2. Paste into _p1
		IStructuredSelection selPaste = new StructuredSelection(_p1);
		_viewer.setSelection(selPaste);
		ActionContributionItem pasteActionItem = getAction(selPaste, "Paste");
		assertTrue(pasteActionItem.getAction().isEnabled());
		pasteActionItem.getAction().run();

		// 3. Verify
		IFile pastedFile = _p1.getFile(file.getName());
		waitForCondition("File should be pasted", () -> pastedFile.exists());
		assertTrue(pastedFile.exists(), "Pasted file should exist in target project");
	}

	@Test
	public void testContextMenuContainsCopyPasteDelete() throws Exception {
		IFile file = _project.getFile("model.properties");
		IStructuredSelection sel = new StructuredSelection(file);
		_viewer.setSelection(sel);

		getAction(sel, "Copy");
		getAction(sel, "Paste");
		getAction(sel, "Delete");
	}

	@Test
	public void testCopyDisabledForWorkspaceRoot() throws Exception {
		// Selecting the workspace root should enable the resource action provider
		// but the Copy action itself should be disabled for the root.
		IStructuredSelection sel = new StructuredSelection(ResourcesPlugin.getWorkspace().getRoot());
		_viewer.setSelection(sel);

		ActionContributionItem copyActionItem = getAction(sel, "Copy");
		assertFalse(copyActionItem.getAction().isEnabled(), "Copy action should be disabled for Workspace Root");
	}
}
