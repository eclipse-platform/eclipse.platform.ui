/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
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
 * @since 3.4
 * 
 */
public class NewEditorHandler extends AbstractHandler {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.ExecutionEvent)
	 */
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow activeWorkbenchWindow = HandlerUtil
				.getActiveWorkbenchWindow(event);
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
			if (editor instanceof IPersistableEditor) {
				XMLMemento editorState = XMLMemento
						.createWriteRoot(IWorkbenchConstants.TAG_EDITOR_STATE);
				((IPersistableEditor) editor).saveState(editorState);
				((WorkbenchPage) page).openEditor(editor.getEditorInput(),
						editorId, true, IWorkbenchPage.MATCH_NONE, editorState);
			} else {
				page.openEditor(editor.getEditorInput(), editorId, true,
						IWorkbenchPage.MATCH_NONE);
			}
		} catch (PartInitException e) {
			DialogUtil.openError(activeWorkbenchWindow.getShell(),
					WorkbenchMessages.Error, e.getMessage(), e);
		}
		return null;
	}

}
