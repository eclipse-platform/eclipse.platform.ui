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
package org.eclipse.debug.internal.ui.views.console;

import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.preferences.ConsolePreferencePage;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.preference.IPreferenceNode;
import org.eclipse.jface.preference.IPreferencePage;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.preference.PreferenceNode;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;

/**
 * ConsoleShowPreferencesAction Displays the Console's Preference page
 * 
 * @since 3.2
 */
public class ConsoleShowPreferencesAction extends Action implements IViewActionDelegate {

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.IViewActionDelegate#init(org.eclipse.ui.IViewPart)
     */
    public void init(IViewPart view) {
        // do nothing
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
     */
    public void run(IAction action) {
        IPreferencePage page = new ConsolePreferencePage();
        page.setTitle(ConsoleMessages.ConsoleShowPreferencesAction__run_debug_console);
        IPreferenceNode targetNode = new PreferenceNode("org.eclipse.debug.internal.ui.preferences.ConsolePreferencePage", page); //$NON-NLS-1$
        PreferenceManager manager = new PreferenceManager();
        manager.addToRoot(targetNode);
        PreferenceDialog dialog = new PreferenceDialog(DebugUIPlugin.getShell(), manager);
        dialog.create();
        dialog.setMessage(page.getDescription());
        dialog.open();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction,
     *      org.eclipse.jface.viewers.ISelection)
     */
    public void selectionChanged(IAction action, ISelection selection) {
        // do nothing
    }
}
