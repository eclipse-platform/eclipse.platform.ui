/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.ui.actions;


import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.IDebugHelpContextIds;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.help.WorkbenchHelp;

/**
 * Launches a launch configuration in a specific mode.
 * <p>
 * Clients are not intended to subclass this class; clients may instantiate this
 * class.
 * </p>
 * @since 2.1
 */
public class LaunchAction extends Action {

	/**
	 * The configuration to launch.
	 */
	private ILaunchConfiguration fConfiguration;
	/**
	 * The mode to launch in
	 */
	private String fMode;
	
	/**
	 * Constucts an action that launches the specified launch configuration
	 * in the specified mode.
	 * 
	 * @param configuration launch configuration
	 * @param mode launch mode - one of <code>ILaunchManager.RUN_MODE</code> or
	 * <code>ILaunchManager.DEBUG_MODE</code>
	 */
	public LaunchAction(ILaunchConfiguration configuration, String mode) {
		fConfiguration = configuration;
		fMode = mode;
		setText(configuration.getName());
		setImageDescriptor(DebugUITools.getDefaultImageDescriptor(configuration));
		WorkbenchHelp.setHelp(
			this,
			IDebugHelpContextIds.RELAUNCH_HISTORY_ACTION);
	}

	/**
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	public void run() {
		DebugUITools.launch(fConfiguration, fMode);
	}
	
	/**
	 * If the user has control-clicked the launch history item, open the launch
	 * configuration dialog on the launch configuration, rather than running it.
	 * 
	 * @see org.eclipse.jface.action.IAction#runWithEvent(org.eclipse.swt.widgets.Event)
	 */
	public void runWithEvent(Event event) {
		if ((event.stateMask & SWT.MOD1) > 0) {
			IStructuredSelection selection = new StructuredSelection(fConfiguration);
			String id = DebugUITools.getLaunchGroup(fConfiguration, fMode).getIdentifier();
			DebugUITools.openLaunchConfigurationDialogOnGroup(DebugUIPlugin.getShell(), selection, id); 
		} else {
			run();
		}
	}

}
