/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.actions;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.internal.IWorkbenchGraphicConstants;
import org.eclipse.ui.internal.WorkbenchImages;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.dialogs.SelectPerspectiveDialog;

/**
 * Action to open the Open Perspective dialog.
 * 
 * @since 3.1
 */
public class OpenPerspectiveDialogAction extends Action implements
        ActionFactory.IWorkbenchAction {

    private IWorkbenchWindow workbenchWindow;
    
    /**
     * Creates a new open perspective dialog action.
     * 
     * @param window the window containing the action
     */
    public OpenPerspectiveDialogAction(IWorkbenchWindow window) {
        Assert.isNotNull(window);
        this.workbenchWindow = window;
        setText(WorkbenchMessages.OpenPerspectiveDialogAction_text);
        setToolTipText(WorkbenchMessages.OpenPerspectiveDialogAction_tooltip);
        setImageDescriptor(WorkbenchImages.getImageDescriptor(
              IWorkbenchGraphicConstants.IMG_ETOOL_NEW_PAGE));
    }        

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.action.Action#run()
     */
    public void run() {
        if (workbenchWindow == null) {
            return;
        }
        SelectPerspectiveDialog dlg = new SelectPerspectiveDialog(workbenchWindow
                .getShell(), workbenchWindow.getWorkbench().getPerspectiveRegistry());
        dlg.open();
        if (dlg.getReturnCode() == Window.CANCEL) {
			return;
		}
        IPerspectiveDescriptor desc = dlg.getSelection();
        if (desc != null) {
            try {
                workbenchWindow.openPage(desc.getId(), null);
            } catch (WorkbenchException e) {
                WorkbenchPlugin.log("Error opening perspective ", e); //$NON-NLS-1$
            }
        }
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.actions.ActionFactory.IWorkbenchAction#dispose()
     */
    public void dispose() {
        workbenchWindow = null;
    }
}
