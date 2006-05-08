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

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

/**
 * Reset the layout within the active perspective.
 */
public class ResetPerspectiveAction extends PerspectiveAction {

    /**
     * This default constructor allows the the action to be called from the welcome page.
     */
    public ResetPerspectiveAction() {
        this(PlatformUI.getWorkbench().getActiveWorkbenchWindow());
    }

    /**
     * Create an instance of this class
     * @param window the window
     */
    public ResetPerspectiveAction(IWorkbenchWindow window) {
        super(window);
        setText(WorkbenchMessages.ResetPerspective_text);
        setActionDefinitionId("org.eclipse.ui.window.resetPerspective"); //$NON-NLS-1$
        // @issue missing action id
        setToolTipText(WorkbenchMessages.ResetPerspective_toolTip); 
        window.getWorkbench().getHelpSystem().setHelp(this,
				IWorkbenchHelpContextIds.RESET_PERSPECTIVE_ACTION);
    }

    /* (non-Javadoc)
     * Method declared on PerspectiveAction.
     */
    protected void run(IWorkbenchPage page, IPerspectiveDescriptor persp) {
        String message = NLS.bind(WorkbenchMessages.ResetPerspective_message, persp.getLabel() );
        String[] buttons = new String[] { IDialogConstants.OK_LABEL,
                IDialogConstants.CANCEL_LABEL };
        MessageDialog d = new MessageDialog(getWindow().getShell(),
                WorkbenchMessages.ResetPerspective_title,
                null, message, MessageDialog.QUESTION, buttons, 0);
        if (d.open() == 0) {
			page.resetPerspective();
		}
    }

}
