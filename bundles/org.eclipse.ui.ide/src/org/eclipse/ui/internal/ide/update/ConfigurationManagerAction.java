/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.ide.update;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.update.ui.UpdateManagerUI;

/**
 * Action to invoke the Update configuration manager.
 * 
 * @since 3.0
 */
public class ConfigurationManagerAction implements
        IWorkbenchWindowActionDelegate {

    private IWorkbenchWindow window;

    private ApplicationWindow applicationWindow;

    /**
     * The constructor.
     */
    public ConfigurationManagerAction() {
        // do nothing
    }

    /**
     * Runs the action when selected
     */
    public void run(IAction action) {
        BusyIndicator.showWhile(window.getShell().getDisplay(), new Runnable() {
            public void run() {
                UpdateManagerUI.openConfigurationManager(window.getShell());
            }
        });
    }

    /**
     */
    public void selectionChanged(IAction action, ISelection selection) {
        // do nothing
    }

    /**
     * @see IWorkbenchWindowActionDelegate#dispose
     */
    public void dispose() {
        // do nothing
    }

    /**
     * @see IWorkbenchWindowActionDelegate#init
     */
    public void init(IWorkbenchWindow window) {
        this.window = window;
    }
}