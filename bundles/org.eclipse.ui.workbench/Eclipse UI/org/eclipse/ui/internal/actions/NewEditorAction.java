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

package org.eclipse.ui.internal.actions;

import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.internal.ActiveEditorAction;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.dialogs.DialogUtil;

/**
 * Opens another editor of the same kind, and on the same input, as the active editor.
 * 
 * @since 3.1
 */
public class NewEditorAction extends ActiveEditorAction {

	/**
	 * Creates a new <code>NewEditorAction</code>.
	 * 
	 * @param window the window in which the action will appear
	 */
	public NewEditorAction(IWorkbenchWindow window) {
		super(WorkbenchMessages.NewEditorAction_text, window);
		setId("newEditorAction"); //$NON-NLS-1$
		setToolTipText(WorkbenchMessages.NewEditorAction_tooltip);
        setActionDefinitionId("org.eclipse.ui.window.newEditor"); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.Action#run()
	 */
	public void run() {
		IWorkbenchPage page = getActivePage();
		IEditorPart editor = getActiveEditor();
		if (page == null || editor == null) {
			return;
		}
		String editorId = editor.getSite().getId();
		if (editorId == null) {
			return;
		}
		try {
			page.openEditor(editor.getEditorInput(), editorId, true, IWorkbenchPage.MATCH_NONE);
        } catch (PartInitException e) {
            DialogUtil.openError(page.getWorkbenchWindow().getShell(),
                    WorkbenchMessages.Error,
                    e.getMessage(), e);
		}
	}
}

