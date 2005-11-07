/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.actions;

import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.IDebugHelpContextIds;
import org.eclipse.debug.internal.ui.views.variables.VariablesView;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.IUpdate;
import org.eclipse.ui.texteditor.IWorkbenchActionDefinitionIds;

/**
 * Action which prompts the user to help them find a variable in
 * the variables view.
 */
public class FindVariableAction extends Action implements IUpdate {
	
	private class FindVariableDelegate extends AbstractListenerActionDelegate {

	    protected void doAction(Object element) {
	        VariablesView view= (VariablesView) getView();
	        Shell shell = view.getSite().getShell();
	        FindVariableDialog dialog= new FindVariableDialog(shell, view);
	        dialog.open();
	    }

	    protected void update(IAction action, ISelection s) {
	    	if (action != null) {
	    		((IUpdate) action).update();
	    	}
	    }

	    protected void doHandleDebugEvent(DebugEvent event) {
	        update(getAction(), null);
	    }

		public void run(IAction action) {
			doAction(null);
		}
	}
	
	private AbstractListenerActionDelegate fDelegate;

    public FindVariableAction(VariablesView view) {
        setText(ActionMessages.FindVariableAction_0); 
		setId(DebugUIPlugin.getUniqueIdentifier() + ".FindVariableAction"); //$NON-NLS-1$
        PlatformUI.getWorkbench().getHelpSystem().setHelp(this, IDebugHelpContextIds.FIND_VARIABLE_ACTION);
		setActionDefinitionId(IWorkbenchActionDefinitionIds.FIND_REPLACE);
        fDelegate= new FindVariableDelegate();
        fDelegate.init(view);
        fDelegate.setAction(this);
    }
    
    public void run() {
    	fDelegate.run(this);
    }

	public void update() {
		VariablesView view= (VariablesView) fDelegate.getView();
		if (view != null) {
			Viewer viewer = view.getViewer();
			if (viewer != null) {
				setEnabled(viewer.getInput() instanceof IStackFrame);
				return;
			}
		}
		setEnabled(false);
	}

    public void dispose() {
        fDelegate.dispose();
    }
}
