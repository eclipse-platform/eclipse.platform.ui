/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
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
 ******************************************************************************/
package org.eclipse.ui.tests.systeminplaceeditor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.ide.IDE;
import org.junit.Ignore;


/**
 * Tests opening the
 * <code>org.eclipse.ui.internal.editorsupport.win32.OleEditor</code>.
 * <p>
 * <strong>Note:</strong> The tests pass on all platforms but
 * only perform for real when a system in-place editor is
 * available for *.doc.
 * </p>
 *
 * @since 3.4
 */
@Ignore
public class OpenSystemInPlaceEditorTest {

	public void testWorkspaceFile() throws Exception {
		if (!PlatformUI.getWorkbench().getEditorRegistry().isSystemInPlaceEditorAvailable("test.doc")) {
			return;
		}

		// Custom setup
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject("test" + System.currentTimeMillis());
		project.create(null);
		project.open(null);
		IFile file = project.getFile("test.doc");
		InputStream s = new ByteArrayInputStream("some test content".getBytes());
		file.create(s, false, null);

		IWorkbenchPage page = getWorkbenchPage();
		IEditorPart editor = IDE.openEditor(page, file);

		assertEquals(IEditorRegistry.SYSTEM_INPLACE_EDITOR_ID, editor.getEditorSite().getId());

		// Custom tear down
		page.closeEditor(editor, false);
		file.delete(false, null);
		project.delete(false, null);
	}

	public void testExternalFile() throws Exception {
		if (!PlatformUI.getWorkbench().getEditorRegistry().isSystemInPlaceEditorAvailable("test.doc")) {
			return;
		}

		// Custom setup
		File file = File.createTempFile("test", ".doc");

		IWorkbenchPage page = getWorkbenchPage();
		IEditorPart editor = IDE.openEditorOnFileStore(page, EFS.getStore(file.toURI()));

		assertEquals(IEditorRegistry.SYSTEM_INPLACE_EDITOR_ID, editor.getEditorSite().getId());
		assertFalse("is not error editor", "org.eclipse.ui.internal.ErrorEditorPart".equals(editor.getClass().getName()));

		// Custom tear down
		page.closeEditor(editor, false);
		file.delete();
	}

	private IWorkbenchPage getWorkbenchPage() throws WorkbenchException {
		IWorkbenchWindow window;
		if (PlatformUI.getWorkbench().getWorkbenchWindowCount() == 0) {
			window = PlatformUI.getWorkbench().openWorkbenchWindow(null);
		} else {
			window = PlatformUI.getWorkbench().getWorkbenchWindows()[0];
		}

		IWorkbenchPage[] pages = window.getPages();
		if (pages.length > 0) {
			return pages[0];
		}

		return window.openPage(null);
	}
}
