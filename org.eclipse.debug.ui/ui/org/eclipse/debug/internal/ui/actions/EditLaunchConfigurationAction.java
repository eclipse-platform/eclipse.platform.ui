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


import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.internal.ui.DebugPluginImages;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.IDebugHelpContextIds;
import org.eclipse.debug.internal.ui.launchConfigurations.LaunchGroupExtension;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.actions.SelectionListenerAction;
import org.eclipse.ui.help.WorkbenchHelp;

/**
 * Opens the launch configuration dialog on a single launch configuration, based
 * on the the launch associated with the selected element.
 */
public class EditLaunchConfigurationAction extends SelectionListenerAction {
	
	private ILaunchConfiguration fConfiguration = null;
	private String fMode =null;

	/**
	 * Constructs a new action.
	 */
	public EditLaunchConfigurationAction() {
		super(""); //$NON-NLS-1$
		setEnabled(false);
		WorkbenchHelp.setHelp(this, IDebugHelpContextIds.EDIT_LAUNCH_CONFIGURATION_ACTION);
	}

	/**
	 * @see org.eclipse.ui.actions.SelectionListenerAction#updateSelection(org.eclipse.jface.viewers.IStructuredSelection)
	 */
	protected boolean updateSelection(IStructuredSelection selection) {
		setLaunchConfiguration(null);
		setMode(null);
		if (selection.size() == 1) {
			Object object = selection.getFirstElement();
			ILaunch launch = null;
			if (object instanceof IAdaptable) {
				launch = (ILaunch)((IAdaptable)object).getAdapter(ILaunch.class); 
			}
			if (launch == null) {
				if (object instanceof ILaunch) {
					launch = (ILaunch)object;
				} else if (object instanceof IDebugElement) {
					launch = ((IDebugElement)object).getLaunch();
				} else if (object instanceof IProcess) {
					launch = ((IProcess)object).getLaunch();
				}
			}
			if (launch != null) {
				ILaunchConfiguration configuration = launch.getLaunchConfiguration();
				if (configuration != null) {
					setLaunchConfiguration(configuration);
					setMode(launch.getLaunchMode());
					setText(configuration.getName() + "..."); //$NON-NLS-1$
					ImageDescriptor descriptor = null;
					try {
						descriptor = DebugPluginImages.getImageDescriptor(configuration.getType().getIdentifier());
					} catch (CoreException e) {
						DebugUIPlugin.log(e);
					}
					setImageDescriptor(descriptor);
				}
			}
		}
		
		// Disable the action if the launch config is private
		ILaunchConfiguration config = getLaunchConfiguration();
		if (config == null) {
			return false;
		} else {
			try {
				return !config.getAttribute(IDebugUIConstants.ATTR_PRIVATE, false);
			} catch (CoreException ce) {
			}
		}
		return false;
	}

	protected void setLaunchConfiguration(ILaunchConfiguration configuration) {
		fConfiguration = configuration;
	}
	
	protected ILaunchConfiguration getLaunchConfiguration() {
		return fConfiguration;
	}
	
	protected void setMode(String mode) {
		fMode = mode;
	}
	
	protected String getMode() {
		return fMode;
	}
	
	/**
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	public void run() {
		LaunchGroupExtension group = DebugUIPlugin.getDefault().getLaunchConfigurationManager().getLaunchGroup(getLaunchConfiguration(), getMode());
		if (group != null) {
			DebugUITools.openLaunchConfigurationDialog(
				DebugUIPlugin.getShell(), getLaunchConfiguration(),
				group.getIdentifier(), null);
		}
	}

}
