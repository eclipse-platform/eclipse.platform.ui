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
package org.eclipse.debug.internal.ui.actions;

 
import java.util.Iterator;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IValueModification;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.debug.internal.ui.DebugPluginImages;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.IDebugHelpContextIds;
import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.SelectionProviderAction;
import org.eclipse.ui.help.WorkbenchHelp;

/**
 * Action for changing the value of primitives and <code>String</code> variables.
 */
public class ChangeVariableValueAction extends SelectionProviderAction {

	private ChangeVariableValueInputDialog fInputDialog;
	protected IVariable fVariable;
	
	public ChangeVariableValueAction(Viewer viewer) {
		super(viewer, ActionMessages.getString("ChangeVariableValue.title")); //$NON-NLS-1$
		setDescription(ActionMessages.getString("ChangeVariableValue.toolTipText")); //$NON-NLS-1$
		setImageDescriptor(DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_ELCL_CHANGE_VARIABLE_VALUE));
		setHoverImageDescriptor(DebugPluginImages.getImageDescriptor(IDebugUIConstants.IMG_LCL_CHANGE_VARIABLE_VALUE));
		setDisabledImageDescriptor(DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_DLCL_CHANGE_VARIABLE_VALUE));
		WorkbenchHelp.setHelp(
			this,
			IDebugHelpContextIds.CHANGE_VALUE_ACTION);
	}
	
	/**
	 * Edit the variable value with an inline text editor.  
	 */
	protected void doActionPerformed(final IVariable variable) {
		IWorkbenchWindow window= DebugUIPlugin.getActiveWorkbenchWindow();
		if (window == null) {
			return;
		}
		Shell activeShell= window.getShell();
		
		// If a previous edit is still in progress, don't start another
		if (fInputDialog != null) {
			return;
		}

		fVariable = variable;
		String name= ""; //$NON-NLS-1$
		String value= ""; //$NON-NLS-1$
		try {
			name= fVariable.getName();
			value= fVariable.getValue().getValueString();
		} catch (DebugException exception) {
			DebugUIPlugin.errorDialog(activeShell, ActionMessages.getString("ChangeVariableValue.errorDialogTitle"),ActionMessages.getString("ChangeVariableValue.errorDialogMessage"), exception);	//$NON-NLS-2$ //$NON-NLS-1$
			fInputDialog= null;
			return;
		}
		fInputDialog= new ChangeVariableValueInputDialog(activeShell, ActionMessages.getString("ChangeVariableValueSet_Variable_Value_1"), ActionMessages.getString("ChangeVariableValueEnter_a_new_value_for__2") + name + ':', value, new IInputValidator() { //$NON-NLS-1$ //$NON-NLS-2$
			/**
			 * Returns an error string if the input is invalid
			 */
			public String isValid(String input) {
				try {
					if (fVariable.verifyValue(input)) {
						return null; // null means valid
					}
				} catch (DebugException exception) {
					return ActionMessages.getString("ChangeVariableValueAn_exception_occurred_3"); //$NON-NLS-1$
				}
				return ActionMessages.getString("ChangeVariableValueInvalid_value_4"); //$NON-NLS-1$
			}
		});
		
		fInputDialog.open();
		String newValue= fInputDialog.getValue();
		if (newValue != null) {
			// null value means cancel was pressed
			try {
				fVariable.setValue(newValue);
				getSelectionProvider().setSelection(new StructuredSelection(variable));
			} catch (DebugException de) {
				DebugUIPlugin.errorDialog(activeShell, ActionMessages.getString("ChangeVariableValue.errorDialogTitle"),ActionMessages.getString("ChangeVariableValue.errorDialogMessage"), de);	//$NON-NLS-2$ //$NON-NLS-1$
				fInputDialog= null;
				return;
			}
		}
		fInputDialog= null;
	}
		
	/**
	 * Updates the enabled state of this action based
	 * on the selection
	 */
	protected void update(IStructuredSelection sel) {
		Iterator iter= sel.iterator();
		if (iter.hasNext()) {
			Object object= iter.next();
			if (object instanceof IValueModification) {
				IValueModification varMod= (IValueModification)object;
				if (!varMod.supportsValueModification()) {
					setEnabled(false);
					return;
				}
				setEnabled(!iter.hasNext());
				return;
			}
		}
		setEnabled(false);
	}

	/**
	 * @see IAction#run()
	 */
	public void run() {
		Iterator iterator= getStructuredSelection().iterator();
		doActionPerformed((IVariable)iterator.next());
	}
	
	/**
	 * @see SelectionProviderAction#selectionChanged(org.eclipse.jface.viewers.IStructuredSelection)
	 */
	public void selectionChanged(IStructuredSelection sel) {
		update(sel);
	}
}

