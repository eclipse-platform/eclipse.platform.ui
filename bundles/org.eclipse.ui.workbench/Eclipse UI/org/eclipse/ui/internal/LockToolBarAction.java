/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.help.WorkbenchHelp;

/**
 * The <code>LockToolBarAction</code> is used to lock the toolbars for the
 * workbench.  The toolbar for all perspectives is locked.
 */
public class LockToolBarAction extends Action implements
        ActionFactory.IWorkbenchAction {

    /**
     * The workbench window; or <code>null</code> if this
     * action has been <code>dispose</code>d.
     */
    private IWorkbenchWindow workbenchWindow;

    /**
     * Create a new instance of <code>LockToolBarAction</code>
     * 
     * @param window the workbench window this action applies to
     */
    public LockToolBarAction(IWorkbenchWindow window) {
        super(WorkbenchMessages.getString("LockToolBarAction.text")); //$NON-NLS-1$
        if (window == null) {
            throw new IllegalArgumentException();
        }
        this.workbenchWindow = window;
        setActionDefinitionId("org.eclipse.ui.window.lockToolBar"); //$NON-NLS-1$
        // @issue missing action id
        setToolTipText(WorkbenchMessages.getString("LockToolBarAction.toolTip")); //$NON-NLS-1$
        setEnabled(false);
        // queue the update for the checked state since this action is created 
        // before the coolbar
        window.getWorkbench().getDisplay().asyncExec(new Runnable() {
            public void run() {
                if (workbenchWindow != null) {
                    setChecked(((WorkbenchWindow) workbenchWindow)
                            .isCoolBarLocked());
                }
            }
        });
        WorkbenchHelp.setHelp(this, IHelpContextIds.LOCK_TOOLBAR_ACTION);
    }

    /* (non-Javadoc)
     * Method declared on IAction.
     */
    public void run() {
        if (workbenchWindow == null) {
            // action has been disposed
            return;
        }
        boolean locked = isChecked();
        ((WorkbenchWindow) workbenchWindow).lockCoolBar(locked);
    }

    /* (non-Javadoc)
     * Method declared on ActionFactory.IWorkbenchAction.
     */
    public void dispose() {
        workbenchWindow = null;
    }

}