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

import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;

/**
 * Hides or shows the editor area within the current
 * perspective of the workbench page.
 */
public class ToggleEditorsVisibilityAction extends PerspectiveAction implements
        IPerspectiveListener {

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
        super(window);
        setText(WorkbenchMessages.ToggleEditor_hideEditors);
        setActionDefinitionId("org.eclipse.ui.window.hideShowEditors"); //$NON-NLS-1$
        // @issue missing action id
        setToolTipText(WorkbenchMessages.ToggleEditor_toolTip);
        window.getWorkbench().getHelpSystem().setHelp(this,
                IWorkbenchHelpContextIds.TOGGLE_EDITORS_VISIBILITY_ACTION);
        window.addPerspectiveListener(this);
    }

    /* (non-Javadoc)
     * Method declared on PerspectiveAction.
     */
    protected void run(IWorkbenchPage page, IPerspectiveDescriptor persp) {
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
        if (getWindow() != null) {
            getWindow().removePerspectiveListener(this);
        }
        super.dispose();
    }

}
