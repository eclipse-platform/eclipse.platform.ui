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

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.internal.ui.views.variables.VariablesView;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Shell;

/**
 * Action which prompts the user to help them find a variable in
 * the variables view.
 */
public class FindVariableAction extends AbstractListenerActionDelegate {

    public FindVariableAction() {
        super();
    }

    public void run(IAction action) {
        run();
    }
    
    public void run() {
        VariablesView view= (VariablesView) getView();
        Shell shell = view.getSite().getShell();
        FindVariableDialog dialog= new FindVariableDialog(shell, view);
        dialog.open();
    }

    protected void doAction(Object element) throws DebugException {
        run();
    }

    protected void update(IAction action, ISelection s) {
        IAdaptable debugContext = DebugUITools.getDebugContext();
        action.setEnabled(debugContext instanceof IStackFrame);
    }

    protected void doHandleDebugEvent(DebugEvent event) {
        if (getAction() != null) {
            update(getAction(), null);
        }
    }

}
