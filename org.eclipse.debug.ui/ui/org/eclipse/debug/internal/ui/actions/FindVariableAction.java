/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.actions;

import org.eclipse.debug.internal.ui.views.variables.VariablesView;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;

/**
 * Action which prompts the user to help them find a variable in
 * the variables view.
 */
public class FindVariableAction implements IViewActionDelegate {
    
    private VariablesView fView;

    public FindVariableAction() {
        super();
    }

    public void init(IViewPart view) {
        fView= (VariablesView) view;
    }

    public void run(IAction action) {
        Shell shell = fView.getSite().getShell();
        FindVariableDialog dialog= new FindVariableDialog(shell, fView);
        dialog.open();
    }

    public void selectionChanged(IAction action, ISelection selection) {
    }

}
