/*******************************************************************************
 * Copyright (c) 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.actions;

import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.Dialog;

import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.activities.IMutableActivityManager;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.activities.ui.ActivityEnabler;
import org.eclipse.ui.roles.IRoleManager;

/**
 * Activates the Activity configuration dialog. 
 * 
 * @since 3.0
 */
public class ActivityEnablerAction extends Action implements ActionFactory.IWorkbenchAction {
    protected ActivityEnabler enabler;
	private IMutableActivityManager activityManager;
	private IRoleManager roleManager;

    /**
     * Create a new instance of the receiver.
     * 
     * @since 3.0
     */
    public ActivityEnablerAction(IMutableActivityManager activityManager, IRoleManager roleManager) {
        super(WorkbenchMessages.getString("ActivityEnablementAction.text")); //$NON-NLS-1$
        this.activityManager = activityManager;
        this.roleManager = roleManager;
    }

    /*
     * (non-Javadoc) @see org.eclipse.jface.action.IAction#run()
     */
    public void run() {
        Dialog d = new Dialog(Display.getCurrent().getActiveShell()) {

            /*
             * (non-Javadoc) @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
             */
            protected Control createDialogArea(Composite parent) {
                Composite composite = (Composite) super.createDialogArea(parent);
                GridData data = new GridData(GridData.FILL_BOTH);
                data.widthHint = 600;
                data.heightHint = 240;

                enabler = new ActivityEnabler(activityManager, roleManager);
                enabler.createControl(composite).setLayoutData(data);

                return composite;
            }

            /*
             * (non-Javadoc) @see org.eclipse.jface.dialogs.Dialog#okPressed()
             */
            protected void okPressed() {
                if (enabler != null) {
                    enabler.updateActivityStates();
                }
                super.okPressed();
            }
        };
        d.open();
    }

	/* (non-Javadoc)
	 * @see org.eclipse.ui.actions.ActionFactory.IWorkbenchAction#dispose()
	 */
	public void dispose() {
	}
}
