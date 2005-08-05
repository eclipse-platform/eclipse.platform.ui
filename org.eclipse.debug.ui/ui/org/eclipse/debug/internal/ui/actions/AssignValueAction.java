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
package org.eclipse.debug.internal.ui.actions;

import java.text.MessageFormat;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IValueModification;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.IDebugHelpContextIds;
import org.eclipse.debug.internal.ui.VariableValueEditorManager;
import org.eclipse.debug.internal.ui.views.variables.VariablesView;
import org.eclipse.debug.ui.actions.IVariableValueEditor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.SelectionProviderAction;

/**
 * Action which assigns a value to a variable from the detail pane
 * of the variables view.
 */
public class AssignValueAction extends SelectionProviderAction {
	private VariablesView variablesView;
	private ISourceViewer detailsViewer;

	public AssignValueAction(VariablesView varView, ISourceViewer detailViewer) {
		super(varView.getViewer(), ActionMessages.AssignValueAction_1); 
        PlatformUI.getWorkbench().getHelpSystem().setHelp(this, IDebugHelpContextIds.ASSIGN_VALUE_ACTION);
		variablesView = varView;
		detailsViewer = detailViewer;
		setEnabled(false);
		variablesView.getSite().getKeyBindingService().registerAction(this);
	}
		
	/* (non-Javadoc)
	 * @see org.eclipse.ui.actions.SelectionProviderAction#selectionChanged(org.eclipse.jface.viewers.IStructuredSelection)
	 */
	public void selectionChanged(IStructuredSelection selection) {
		boolean enabled = false;
		if ((selection.size() == 1) && (selection.getFirstElement() instanceof IValueModification)) {
			IValueModification valMod = (IValueModification) selection.getFirstElement();
			if (valMod.supportsValueModification()) {
				super.selectionChanged(selection);
				enabled = true;
			}
		} 
		setEnabled(enabled);		
	}	
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	public void run() {
		IVariable variable = (IVariable) getStructuredSelection().getFirstElement();
		
		Point selection = detailsViewer.getSelectedRange();
		String value = null;
		if (selection.y == 0) {
			value = detailsViewer.getDocument().get();
		} else {
			try {
				value = detailsViewer.getDocument().get(selection.x, selection.y);
			} catch (BadLocationException e1) {
			}
		}
		IWorkbenchWindow window= DebugUIPlugin.getActiveWorkbenchWindow();
		Shell activeShell= null;
		if (window != null) {
			activeShell= window.getShell();
		}
		
		String modelIdentifier = variable.getModelIdentifier();
		IVariableValueEditor editor = VariableValueEditorManager.getDefault().getVariableValueEditor(modelIdentifier);
		if (editor != null) {
		    if (editor.saveVariable(variable, value, activeShell)) {
		        // If we successfully delegate to an editor which performs the save,
		        // don't do any more work.
		        return;
		    }
		}
		
		try {
		    // If we failed to delegate to anyone, perform the default assignment.
			if (variable.verifyValue(value)) {
				variable.setValue(value);
			} else {
			    if (activeShell != null) {
			        DebugUIPlugin.errorDialog(activeShell, ActionMessages.AssignValueAction_2, MessageFormat.format(ActionMessages.AssignValueAction_3, new String[] {value, variable.getName()}), new StatusInfo(IStatus.ERROR, ActionMessages.AssignValueAction_4));  //  
			    }
			}
		} catch (DebugException e) {
            MessageDialog.openError(activeShell, ActionMessages.AssignValueAction_0, ActionMessages.AssignValueAction_5);
		}
		
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.IAction#getActionDefinitionId()
	 */
	public String getActionDefinitionId() {		
		return "org.eclipse.ui.file.save"; //$NON-NLS-1$
	}

}
