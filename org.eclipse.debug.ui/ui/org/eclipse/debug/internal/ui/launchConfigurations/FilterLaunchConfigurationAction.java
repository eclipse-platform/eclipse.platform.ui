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

import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.preferences.LaunchConfigurationsPreferencePage;
import org.eclipse.jface.preference.IPreferenceNode;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.preference.PreferenceNode;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.custom.BusyIndicator;

/**
 * provides the iplementation of the filtering action for the launch configuration view
 * @since 3.2
 */
public class FilterLaunchConfigurationAction extends AbstractLaunchConfigurationAction {

	/**
	 * Action identifier for IDebugView#getAction(String)
	 */
	public static final String ID_FILTER_ACTION = DebugUIPlugin.getUniqueIdentifier() + ".ID_FILTER_ACTION"; //$NON-NLS-1$
	
	/**
	 * Constructor
	 * @param text the text for the action
	 * @param viewer the viewer the action acts upon
	 * @param mode the mode
	 */
	public FilterLaunchConfigurationAction(Viewer viewer, String mode) {
		super(LaunchConfigurationsMessages.FilterLaunchConfigurationAction_0, viewer, mode);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.launchConfigurations.AbstractLaunchConfigurationAction#performAction()
	 */
	protected void performAction() {
		final IPreferenceNode targetNode = new PreferenceNode("org.eclipse.debug.ui.LaunchConfigurationsPreferenecPage",  //$NON-NLS-1$
				new LaunchConfigurationsPreferencePage());
		PreferenceManager manager = new PreferenceManager();
		manager.addToRoot(targetNode);
		final PreferenceDialog dialog = new PreferenceDialog(DebugUIPlugin.getShell(), manager);
		final boolean [] result = new boolean[] { false };
		BusyIndicator.showWhile(DebugUIPlugin.getStandardDisplay(), new Runnable() {
			public void run() {
				dialog.create();
				dialog.setMessage(targetNode.getLabelText());
				result[0]= (dialog.open() == Window.OK);
			}
		});	
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.actions.BaseSelectionListenerAction#updateSelection(org.eclipse.jface.viewers.IStructuredSelection)
	 */
	protected boolean updateSelection(IStructuredSelection selection) {
		return true;
	}

	
}
