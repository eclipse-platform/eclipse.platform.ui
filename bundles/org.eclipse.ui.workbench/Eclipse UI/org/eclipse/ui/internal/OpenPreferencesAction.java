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
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.internal.dialogs.WorkbenchPreferenceDialog;

/**
 * Open the preferences dialog
 */
public class OpenPreferencesAction extends Action implements
        ActionFactory.IWorkbenchAction {

    /**
     * The workbench window; or <code>null</code> if this
     * action has been <code>dispose</code>d.
     */
    private IWorkbenchWindow workbenchWindow;

    /**
     * Create a new <code>OpenPreferenceAction</code>
     * This default constructor allows the the action to be called from the welcome page.
     */
    public OpenPreferencesAction() {
        this(PlatformUI.getWorkbench().getActiveWorkbenchWindow());
    }

    /**
     * Create a new <code>OpenPreferenceAction</code> and initialize it 
     * from the given resource bundle.
     */
    public OpenPreferencesAction(IWorkbenchWindow window) {
        super(WorkbenchMessages.getString("OpenPreferences.text")); //$NON-NLS-1$
        if (window == null) {
            throw new IllegalArgumentException();
        }
        this.workbenchWindow = window;
        setActionDefinitionId("org.eclipse.ui.window.preferences"); //$NON-NLS-1$
        // @issue action id not set
        setToolTipText(WorkbenchMessages.getString("OpenPreferences.toolTip")); //$NON-NLS-1$
        WorkbenchHelp.setHelp(this, IHelpContextIds.OPEN_PREFERENCES_ACTION);
    }

    /* (non-Javadoc)
     * Method declared on Action.
     */
    public void run() {
        if (workbenchWindow == null) {
            // action has been dispose
            return;
        }
        PreferenceManager pm = WorkbenchPlugin.getDefault()
                .getPreferenceManager();

        if (pm != null) {
            PreferenceDialog d = new WorkbenchPreferenceDialog(workbenchWindow
                    .getShell(), pm);
            d.create();
            WorkbenchHelp.setHelp(d.getShell(),
                    IHelpContextIds.PREFERENCE_DIALOG);
            d.open();
        }
    }

    /* (non-Javadoc)
     * Method declared on ActionFactory.IWorkbenchAction.
     */
    public void dispose() {
        workbenchWindow = null;
    }

}