/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.actions.variables;

 
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IValueModification;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.debug.internal.ui.DebugPluginImages;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.IDebugHelpContextIds;
import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
import org.eclipse.debug.internal.ui.VariableValueEditorManager;
import org.eclipse.debug.internal.ui.actions.ActionMessages;
import org.eclipse.debug.internal.ui.views.variables.VariablesView;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.actions.IVariableValueEditor;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.SelectionProviderAction;

import com.ibm.icu.text.MessageFormat;

/**
 * Action for changing the value of primitives and <code>String</code> variables.
 * This action will attempt to delegate the editing operation to a registered
 * variable value editor, if any is provided for the variable's debug model.
 * @see org.eclipse.debug.ui.actions.VariableValueEditorManager
 */
public class ChangeVariableValueAction extends SelectionProviderAction {
    
	protected IVariable fVariable = null;
    private VariablesView fView = null;
    private boolean fEditing = false;
	
    /**
     * Creates a new ChangeVariableValueAction for the given variables view
     * @param view the varibles view in which this action will appear
     */
	public ChangeVariableValueAction(VariablesView view) {
		super(view.getViewer(), ActionMessages.ChangeVariableValue_title); 
		setDescription(ActionMessages.ChangeVariableValue_toolTipText); 
		setImageDescriptor(DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_ELCL_CHANGE_VARIABLE_VALUE));
		setHoverImageDescriptor(DebugPluginImages.getImageDescriptor(IDebugUIConstants.IMG_LCL_CHANGE_VARIABLE_VALUE));
		setDisabledImageDescriptor(DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_DLCL_CHANGE_VARIABLE_VALUE));
		PlatformUI.getWorkbench().getHelpSystem().setHelp(this,	IDebugHelpContextIds.CHANGE_VALUE_ACTION);
		fView = view;
	}

    /**
     * Edits the variable using the default variable editor
     * @param shell a shell for prompting the user
     */
    protected void doDefaultEdit(Shell shell) {
	    String name = ""; //$NON-NLS-1$
		String value = ""; //$NON-NLS-1$
		try {
			name = fVariable.getName();
			value = fVariable.getValue().getValueString();
		} catch (DebugException exception) {
			DebugUIPlugin.errorDialog(shell, ActionMessages.ChangeVariableValue_errorDialogTitle,ActionMessages.ChangeVariableValue_errorDialogMessage, exception);	// 
			return;
		}
		ChangeVariableValueInputDialog inputDialog = new ChangeVariableValueInputDialog(shell, ActionMessages.ChangeVariableValue_1, MessageFormat.format(ActionMessages.ChangeVariableValue_2, new String[] {name}), value, new IInputValidator() { // 
			/**
			 * Returns an error string if the input is invalid
			 */
			public String isValid(String input) {
				try {
					if (fVariable.verifyValue(input)) {
						return null; // null means valid
					}
				} catch (DebugException exception) {
					return ActionMessages.ChangeVariableValue_3; 
				}
				return ActionMessages.ChangeVariableValue_4; 
			}
		});
		
		inputDialog.open();
		String newValue= inputDialog.getValue();
		if (newValue != null) {
			// null value means cancel was pressed
			try {
				fVariable.setValue(newValue);
				getSelectionProvider().setSelection(new StructuredSelection(fVariable));
			} catch (DebugException de) {
				DebugUIPlugin.errorDialog(shell, ActionMessages.ChangeVariableValue_errorDialogTitle,ActionMessages.ChangeVariableValue_errorDialogMessage, de);	// 
			}
		}
	}

	/**
	 * @see IAction#run()
	 */
	public void run() {
		// If a previous edit is still in progress, don't start another		
	    if (fEditing) {
	        return;
	    }
	    fEditing = true;
	    IWorkspaceRunnable wr = new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				Shell shell = fView.getViewSite().getShell();
				fVariable = (IVariable) getStructuredSelection().getFirstElement();
				boolean edits = false;
				//attempt to delegate first to contributors of variable value editors
				String mid = fVariable.getModelIdentifier();
		        IVariableValueEditor editor= VariableValueEditorManager.getDefault().getVariableValueEditor(mid);
		        if (editor != null) {
		            edits = editor.editVariable(fVariable, shell);
		        }
			    if (!edits) {
			        doDefaultEdit(shell);
			    }
			    fEditing = false;
			}
	    };
	    try {
			ResourcesPlugin.getWorkspace().run(wr, new NullProgressMonitor());
		} catch (CoreException e) {DebugUIPlugin.log(e);}
	}
	
	/**
	 * @see SelectionProviderAction#selectionChanged(org.eclipse.jface.viewers.IStructuredSelection)
	 */
	public void selectionChanged(IStructuredSelection sel) {
		Object object = sel.getFirstElement();
		if (object instanceof IValueModification) {
			IValueModification varMod = (IValueModification)object;
			if (!varMod.supportsValueModification()) {
				setEnabled(false);
				return;
			}
			setEnabled(sel.size() == 1);
			return;
		}
		setEnabled(false);
	}
}

