/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.actions.PerspectiveMenu;
import org.eclipse.ui.internal.util.PrefUtil;

/**
 * Change the perspective of the active page in the window
 * to the selected one.
 */
public class ChangeToPerspectiveMenu extends PerspectiveMenu {

    /**
     * Constructor for ChangeToPerspectiveMenu.
     * 
     * @param window the workbench window this action applies to.
     */
    public ChangeToPerspectiveMenu(IWorkbenchWindow window, String id) {
        super(window, id);
        // indicate that a open perspectives submenu has been created
        ((WorkbenchWindow) window)
                .addSubmenu(WorkbenchWindow.OPEN_PERSPECTIVE_SUBMENU);
        showActive(true);
    }

    /**
     * Returns the available list of perspectives to display in the menu.
     * Extends the super implementation by ensuring that the current perspective
     * is included in the list.
     * 
     * @return an <code>ArrayList<code> of perspective items <code>IPerspectiveDescriptor</code>
     */
    /*
     protected ArrayList getPerspectiveItems() {
     ArrayList list = super.getPerspectiveItems();
     IWorkbenchWindow window = getWindow();
     IWorkbenchPage page = window.getActivePage();
     if (page != null) {
     IPerspectiveDescriptor desc = page.getPerspective();
     if (desc != null) {
     if (!list.contains(desc)) {
     list.add(desc);
     }
     }
     }
     return list;
     }
     */

    /* (non-Javadoc)
     * @see PerspectiveMenu#run(IPerspectiveDescriptor)
     */
    protected void run(IPerspectiveDescriptor desc) {
        IPreferenceStore store = PrefUtil.getInternalPreferenceStore();
        int mode = store.getInt(IPreferenceConstants.OPEN_PERSP_MODE);
        IWorkbenchPage page = getWindow().getActivePage();
        IPerspectiveDescriptor persp = null;
        if (page != null)
            persp = page.getPerspective();

        // Only open a new window if user preference is set and the window
        // has an active perspective.
        if (IPreferenceConstants.OPM_NEW_WINDOW == mode && persp != null) {
            try {
                IWorkbench workbench = getWindow().getWorkbench();
                IAdaptable input = ((Workbench) workbench)
                        .getDefaultPageInput();
                workbench.openWorkbenchWindow(desc.getId(), input);
            } catch (WorkbenchException e) {
                handleWorkbenchException(e);
            }
        } else {
            if (page != null) {
                page.setPerspective(desc);
            } else {
                try {
                    IWorkbench workbench = getWindow().getWorkbench();
                    IAdaptable input = ((Workbench) workbench)
                            .getDefaultPageInput();
                    getWindow().openPage(desc.getId(), input);
                } catch (WorkbenchException e) {
                    handleWorkbenchException(e);
                }
            }
        }
    }

    /**
     * Handles workbench exception
     */
    private void handleWorkbenchException(WorkbenchException e) {
        ErrorDialog.openError(getWindow().getShell(), WorkbenchMessages.ChangeToPerspectiveMenu_errorTitle, 
                e.getMessage(), e.getStatus());
    }
}
