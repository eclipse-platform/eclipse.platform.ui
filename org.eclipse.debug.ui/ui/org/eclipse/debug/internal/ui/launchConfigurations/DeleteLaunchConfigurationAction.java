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
package org.eclipse.debug.internal.ui.launchConfigurations;


import java.util.Iterator;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.Viewer;

/**
 * Deletes the selected launch configuration(s).
 */
public class DeleteLaunchConfigurationAction extends AbstractLaunchConfigurationAction {

	/**
	 * Action identifier for IDebugView#getAction(String)
	 */
	public static final String ID_DELETE_ACTION = DebugUIPlugin.getUniqueIdentifier() + ".ID_DELETE_ACTION"; //$NON-NLS-1$
	
	class Confirmation implements IConfirmationRequestor {
		/**
		 * @see org.eclipse.debug.internal.ui.launchConfigurations.AbstractLaunchConfigurationAction.IConfirmationRequestor#getConfirmation()
		 */
		public boolean getConfirmation() {
			IStructuredSelection selection = getStructuredSelection();
			// Make the user confirm the deletion
			String dialogMessage = selection.size() > 1 ? LaunchConfigurationsMessages.LaunchConfigurationDialog_Do_you_wish_to_delete_the_selected_launch_configurations__1 : LaunchConfigurationsMessages.LaunchConfigurationDialog_Do_you_wish_to_delete_the_selected_launch_configuration__2; // 
			return MessageDialog.openQuestion(getShell(), LaunchConfigurationsMessages.LaunchConfigurationDialog_Confirm_Launch_Configuration_Deletion_3, dialogMessage); 
		}

	}
	
	/**
	 * Constructs an action to delete launch configuration(s) 
	 */
	public DeleteLaunchConfigurationAction(Viewer viewer, String mode) {
		super(LaunchConfigurationsMessages.DeleteLaunchConfigurationAction_Dele_te_1, viewer, mode); 
		setConfirmationRequestor(new Confirmation());
	}

	/**
	 * @see AbstractLaunchConfigurationAction#performAction()
	 */
	protected void performAction() {
		IStructuredSelection selection = getStructuredSelection();

		getViewer().getControl().setRedraw(false);
		Iterator iterator = selection.iterator();
		while (iterator.hasNext()) {
			ILaunchConfiguration configuration = (ILaunchConfiguration)iterator.next();
			try {
				configuration.delete();
			} catch (CoreException e) {
				errorDialog(e);
			}
		}
		getViewer().getControl().setRedraw(true);
	}

	/**
	 * @see org.eclipse.ui.actions.SelectionListenerAction#updateSelection(org.eclipse.jface.viewers.IStructuredSelection)
	 */
	protected boolean updateSelection(IStructuredSelection selection) {
		if (selection.isEmpty()) {
			return false;
		}
		Iterator items = selection.iterator();
		while (items.hasNext()) {
			if (!(items.next() instanceof ILaunchConfiguration)) {
				return false;
			}
		}
		return true;
	}

}
