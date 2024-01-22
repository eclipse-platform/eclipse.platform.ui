/*******************************************************************************
 * Copyright (c) 2007, 2015 IBM Corporation and others.
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

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPersistableEditor;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.XMLMemento;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.internal.IWorkbenchConstants;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchPage;
import org.eclipse.ui.internal.dialogs.DialogUtil;

/**
 * Open a new editor on the active editor's input.
 *
 */
public class NewEditorHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) {
		IWorkbenchWindow activeWorkbenchWindow = HandlerUtil.getActiveWorkbenchWindow(event);
		IWorkbenchPage page = activeWorkbenchWindow.getActivePage();
		if (page == null) {
			return null;
		}
		IEditorPart editor = page.getActiveEditor();
		if (editor == null) {
			return null;
		}
		String editorId = editor.getSite().getId();
		if (editorId == null) {
			return null;
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
			DialogUtil.openError(activeWorkbenchWindow.getShell(), WorkbenchMessages.Error, e.getMessage(), e);
		}
		return null;
	}

}
