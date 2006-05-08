/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.ide.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.dialogs.IWorkingSetSelectionDialog;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;

/**
 * This action asks the user to select a working set, and then creates
 * and runs a corresponding BuildSetAction.
 * 
 * @since 3.0
 */
public class SelectBuildWorkingSetAction extends Action implements
        ActionFactory.IWorkbenchAction {
    private IWorkbenchWindow window;

    private IActionBarConfigurer actionBars;

    public SelectBuildWorkingSetAction(IWorkbenchWindow window,
            IActionBarConfigurer actionBars) {
        super(IDEWorkbenchMessages.SelectWorkingSetAction_text);
        this.window = window;
        this.actionBars = actionBars;
    }

    private IWorkingSet queryForWorkingSet() {
        IWorkingSetManager manager = window.getWorkbench()
                .getWorkingSetManager();
        IWorkingSetSelectionDialog dialog = manager
                .createWorkingSetSelectionDialog(window.getShell(), false);
        dialog.open();
        IWorkingSet[] sets = dialog.getSelection();
        //check for cancel
        if (sets == null || sets.length == 0) {
			return null;
		}
        return sets[0];
    }

    public void run() {
        IWorkingSet set = queryForWorkingSet();
        if (set != null) {
            new BuildSetAction(set, window, actionBars).run();
        }
    }

    public void dispose() {
    }

    public void setActionBars(IActionBarConfigurer actionBars) {
        this.actionBars = actionBars;
    }
}
