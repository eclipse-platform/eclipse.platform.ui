/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal;

import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;

/**
 * Closes the active editor.
 */
public class CloseEditorAction extends ActiveEditorAction {
    /**
     * Create an instance of this class.
     * 
     * @param window the window
     */
    public CloseEditorAction(IWorkbenchWindow window) {
        super(WorkbenchMessages.CloseEditorAction_text, window);
        setToolTipText(WorkbenchMessages.CloseEditorAction_toolTip);
        setId("close"); //$NON-NLS-1$
        window.getWorkbench().getHelpSystem().setHelp(this,
				IWorkbenchHelpContextIds.CLOSE_PART_ACTION);
        setActionDefinitionId("org.eclipse.ui.file.close"); //$NON-NLS-1$
    }

    /* (non-Javadoc)
     * Method declared on IAction.
     */
    public void run() {
        IEditorPart part = getActiveEditor();
        if (part != null) {
			getActivePage().closeEditor(part, true);
		}
    }
}
