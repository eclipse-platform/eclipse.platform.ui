/*******************************************************************************
 * Copyright (c) 2007, 2026 IBM Corporation and others.
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

package org.eclipse.ui.internal.handlers;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPersistableEditor;
import org.eclipse.ui.ISources;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.XMLMemento;
import org.eclipse.ui.internal.IWorkbenchConstants;
import org.eclipse.ui.internal.WorkbenchPage;
import org.eclipse.ui.internal.dialogs.DialogUtil;

import jakarta.inject.Named;

/**
 * Open a new editor on the active editor's input.
 *
 */
public class NewEditorHandler {

	@Execute
	public void execute(@Named(ISources.ACTIVE_WORKBENCH_WINDOW_NAME) IWorkbenchWindow activeWorkbenchWindow) {
		IWorkbenchPage page = activeWorkbenchWindow.getActivePage();
		if (page == null) {
			return;
		}
		IEditorPart editor = page.getActiveEditor();
		if (editor == null) {
			return;
		}
		String editorId = editor.getSite().getId();
		if (editorId == null) {
			return;
		}
		try {
			int matchFlags = IWorkbenchPage.MATCH_NONE | IWorkbenchPage.MATCH_IGNORE_SIZE;
			if (editor instanceof IPersistableEditor) {
				XMLMemento editorState = XMLMemento.createWriteRoot(IWorkbenchConstants.TAG_EDITOR_STATE);
				((IPersistableEditor) editor).saveState(editorState);
				((WorkbenchPage) page).openEditor(editor.getEditorInput(), editorId, true, matchFlags,
						editorState, true);
			} else {
				page.openEditor(editor.getEditorInput(), editorId, true, matchFlags);
			}
		} catch (PartInitException e) {
			DialogUtil.openError(e.getMessage(), e);
		}
	}

}
