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
package org.eclipse.debug.internal.ui.launchConfigurations;


import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.Viewer;

/**
 * Creates a new launch configuration based on the selection.
 */
public class CreateLaunchConfigurationAction extends AbstractLaunchConfigurationAction {

	/**
	 * Action identifier for IDebugView#getAction(String)
	 */
	public static final String ID_CREATE_ACTION = DebugUIPlugin.getUniqueIdentifier() + ".ID_CREATE_ACTION"; //$NON-NLS-1$
	
	/**
	 * Constructs an action to create a launch configuration 
	 */
	public CreateLaunchConfigurationAction(Viewer viewer, String mode) {
		super(LaunchConfigurationsMessages.CreateLaunchConfigurationAction_Ne_w_1, viewer, mode); 
	}

	/**
	 * @see AbstractLaunchConfigurationAction#performAction()
	 */
	protected void performAction() {
		Object object = getStructuredSelection().getFirstElement();
		ILaunchConfigurationType type= null;
		if (object instanceof ILaunchConfiguration) {
			ILaunchConfiguration config= (ILaunchConfiguration) object;
			try {
				type = config.getType();
			} catch (CoreException e) {
				errorDialog(e);
				return;
			}
		} else {
			type = (ILaunchConfigurationType) object;
		}

		try {
			type.newInstance(null, DebugPlugin.getDefault().getLaunchManager().generateUniqueLaunchConfigurationNameFrom(LaunchConfigurationsMessages.CreateLaunchConfigurationAction_New_configuration_2)).doSave();
		} catch (CoreException e) {
			errorDialog(e);
			return;
		}
	}

	/**
	 * @see org.eclipse.ui.actions.SelectionListenerAction#updateSelection(org.eclipse.jface.viewers.IStructuredSelection)
	 */
	protected boolean updateSelection(IStructuredSelection selection) {
		return selection.size() == 1;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.Action#getDisabledImageDescriptor()
	 */
	public ImageDescriptor getDisabledImageDescriptor() {
		return DebugUITools.getImageDescriptor(IInternalDebugUIConstants.IMG_DLCL_NEW_CONFIG);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.Action#getImageDescriptor()
	 */
	public ImageDescriptor getImageDescriptor() {
		return DebugUITools.getImageDescriptor(IInternalDebugUIConstants.IMG_ELCL_NEW_CONFIG);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.Action#getToolTipText()
	 */
	public String getToolTipText() {
		return LaunchConfigurationsMessages.LaunchConfigurationsDialog_0;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.Action#getText()
	 */
	public String getText() {
		return LaunchConfigurationsMessages.LaunchConfigurationDialog_Ne_w_13;
	}
}
