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
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.internal.ActiveEditorAction;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchPage;
import org.eclipse.ui.internal.dialogs.DialogUtil;

/**
 * Opens another editor of the same kind, and on the same input, as the active editor.
 * 
 * @since 3.1
 */
public class OpenAgainAction extends ActiveEditorAction {

	/**
	 * Creates a new <code>OpenAnotherEditorAction</code>.
	 * 
	 * @param window the window in which the action will appear
	 */
	public OpenAgainAction(IWorkbenchWindow window) {
		super(WorkbenchMessages.OpenAgainAction_text, window);
		setId("openAgainAction"); //$NON-NLS-1$
		setToolTipText(WorkbenchMessages.OpenAgainAction_tooltip);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.Action#run()
	 */
	public void run() {
		WorkbenchPage page = (WorkbenchPage) getActivePage();
		IEditorPart editor = getActiveEditor();
		if (page == null || editor == null) {
			return;
		}
		String editorId = editor.getSite().getId();
		if (editorId == null) {
			return;
		}
		try {
			page.openEditor(editor.getEditorInput(), editorId, true, false);
        } catch (PartInitException e) {
            DialogUtil.openError(page.getWorkbenchWindow().getShell(),
                    WorkbenchMessages.Error,
                    e.getMessage(), e);
		}
	}
}

