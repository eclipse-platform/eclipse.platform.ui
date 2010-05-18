/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
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
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.Viewer;

/**
 * Duplicates the selected launch configuration.
 */
public class DuplicateLaunchConfigurationAction extends AbstractLaunchConfigurationAction {
	
	/**
	 * Action identifier for IDebugView#getAction(String)
	 */
	public static final String ID_DUPLICATE_ACTION = DebugUIPlugin.getUniqueIdentifier() + ".ID_DUPLICATE_ACTION"; //$NON-NLS-1$

	/**
	 * Constructs an action to duplicate a launch configuration 
	 */
	public DuplicateLaunchConfigurationAction(Viewer viewer, String mode) {
		super(LaunchConfigurationsMessages.DuplicateLaunchConfigurationAction__Duplicate_1, viewer, mode); 
	}

	/**
	 * @see AbstractLaunchConfigurationAction#performAction()
	 */
	protected void performAction() {
		ILaunchConfiguration original = (ILaunchConfiguration)getStructuredSelection().getFirstElement();
		String newName = DebugPlugin.getDefault().getLaunchManager().generateLaunchConfigurationName(original.getName());
		try {
			ILaunchConfigurationWorkingCopy newWorkingCopy = original.copy(newName);
			newWorkingCopy.doSave();
		} catch (CoreException e) {
			errorDialog(e);
		}
	}

	/**
	 * @see org.eclipse.ui.actions.SelectionListenerAction#updateSelection(org.eclipse.jface.viewers.IStructuredSelection)
	 */
	protected boolean updateSelection(IStructuredSelection selection) {
		return selection.size() == 1 && selection.getFirstElement() instanceof ILaunchConfiguration;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.Action#getDisabledImageDescriptor()
	 */
	public ImageDescriptor getDisabledImageDescriptor() {
		return DebugUITools.getImageDescriptor(IInternalDebugUIConstants.IMG_DLCL_DUPLICATE_CONFIG);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.Action#getImageDescriptor()
	 */
	public ImageDescriptor getImageDescriptor() {
		return DebugUITools.getImageDescriptor(IInternalDebugUIConstants.IMG_ELCL_DUPLICATE_CONFIG);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.Action#getToolTipText()
	 */
	public String getToolTipText() {
		return LaunchConfigurationsMessages.LaunchConfigurationsDialog_5;
	}

}
