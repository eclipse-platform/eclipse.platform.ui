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

import org.eclipse.jface.action.Action;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory;

/**
 * Hides or shows the editor area within the current
 * perspective of the workbench page.
 */
public class ToggleEditorsVisibilityAction extends Action implements
        IPerspectiveListener, ActionFactory.IWorkbenchAction {

    /**
     * The workbench window; or <code>null</code> if this
     * action has been <code>dispose</code>d.
     */
    private IWorkbenchWindow workbenchWindow;

    /* (non-Javadoc)
     * Method declared on IPerspectiveListener
     */
    public void perspectiveActivated(IWorkbenchPage page,
            IPerspectiveDescriptor perspective) {
        if (page.isEditorAreaVisible()) {
            setText(WorkbenchMessages.ToggleEditor_hideEditors); 
        } else {
            setText(WorkbenchMessages.ToggleEditor_showEditors);
        }
    }

    /* (non-Javadoc)
     * Method declared on IPerspectiveListener
     */
    public void perspectiveChanged(IWorkbenchPage page,
            IPerspectiveDescriptor perspective, String changeId) {
        if (changeId == IWorkbenchPage.CHANGE_RESET
                || changeId == IWorkbenchPage.CHANGE_EDITOR_AREA_HIDE
                || changeId == IWorkbenchPage.CHANGE_EDITOR_AREA_SHOW) {
            if (page.isEditorAreaVisible()) {
                setText(WorkbenchMessages.ToggleEditor_hideEditors); 
            } else {
                setText(WorkbenchMessages.ToggleEditor_showEditors); 
            }
        }
    }

    /**
     * Creates a new <code>ToggleEditorsVisibilityAction</code>
     * 
     * @param window the window
     */
    public ToggleEditorsVisibilityAction(IWorkbenchWindow window) {
        super(WorkbenchMessages.ToggleEditor_hideEditors);
        if (window == null) {
            throw new IllegalArgumentException();
        }
        this.workbenchWindow = window;
        setActionDefinitionId("org.eclipse.ui.window.hideShowEditors"); //$NON-NLS-1$
        // @issue missing action id
        setToolTipText(WorkbenchMessages.ToggleEditor_toolTip);
        workbenchWindow.getWorkbench().getHelpSystem().setHelp(this,
                IWorkbenchHelpContextIds.TOGGLE_EDITORS_VISIBILITY_ACTION);
        setEnabled(false);

        // Once the API on IWorkbenchPage to hide/show
        // the editor area is removed, then switch
        // to using the internal perspective service
        workbenchWindow.addPerspectiveListener(this);
    }

    /* (non-Javadoc)
     * Method declared on IAction.
     */
    public void run() {
        if (workbenchWindow == null) {
            // action has been disposed
            return;
        }
        IWorkbenchPage page = workbenchWindow.getActivePage();
        if (page == null) {
            return;
        }

        boolean visible = page.isEditorAreaVisible();
        if (visible) {
            page.setEditorAreaVisible(false);
            setText(WorkbenchMessages.ToggleEditor_showEditors); 
        } else {
            page.setEditorAreaVisible(true);
            setText(WorkbenchMessages.ToggleEditor_hideEditors);
        }
    }

    /* (non-Javadoc)
     * Method declared on ActionFactory.IWorkbenchAction.
     */
    public void dispose() {
        if (workbenchWindow == null) {
            // already disposed
            return;
        }
        workbenchWindow.removePerspectiveListener(this);
        workbenchWindow = null;
    }

}
