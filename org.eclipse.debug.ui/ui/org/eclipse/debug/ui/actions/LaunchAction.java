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
package org.eclipse.debug.ui.actions;


import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.IDebugHelpContextIds;
import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.ILaunchGroup;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.PlatformUI;
import org.eclipse.debug.internal.ui.actions.ActionMessages;
import org.eclipse.debug.internal.ui.launchConfigurations.LaunchHistory;

import com.ibm.icu.text.MessageFormat;

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
	 * Constructs an action that launches the specified launch configuration
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
		PlatformUI.getWorkbench().getHelpSystem().setHelp(
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
		if ((event.stateMask & SWT.MOD1) > 0 && (event.stateMask & SWT.MOD2) > 0){
			String id = DebugUITools.getLaunchGroup(fConfiguration, fMode).getIdentifier();
			LaunchHistory history = DebugUIPlugin.getDefault().getLaunchConfigurationManager().getLaunchHistory(id);
			if (history != null){
				//prompt based on pref
				IPreferenceStore store = DebugUIPlugin.getDefault().getPreferenceStore();
				if(store.getBoolean(IInternalDebugUIConstants.PREF_REMOVE_FROM_LAUNCH_HISTORY)) {
					MessageDialogWithToggle mdwt = MessageDialogWithToggle.openYesNoQuestion(DebugUIPlugin.getShell(), 
							ActionMessages.LaunchAction_0, 
							MessageFormat.format(ActionMessages.LaunchAction_1, new String[] {fConfiguration.getName()}), 
							ActionMessages.LaunchAction_2, 
							false, 
							null, 
							null);
					int ret = mdwt.getReturnCode();
					if(ret == IDialogConstants.YES_ID) {
						history.removeFromHistory(fConfiguration);
						store.setValue(IInternalDebugUIConstants.PREF_REMOVE_FROM_LAUNCH_HISTORY, !mdwt.getToggleState());
					}
				}
				else {
					history.removeFromHistory(fConfiguration);
				}
			} else {
				DebugUIPlugin.logErrorMessage("Unable to remove configuration from launch history.  Launch history could not be retrieved."); //$NON-NLS-1$
			}
		}
		else if ((event.stateMask & SWT.MOD1) > 0) {
			ILaunchGroup group = DebugUITools.getLaunchGroup(fConfiguration, fMode);
			if(group != null) {
				DebugUITools.openLaunchConfigurationDialogOnGroup(DebugUIPlugin.getShell(), new StructuredSelection(fConfiguration), group.getIdentifier());
			}
			else {
				run();
			}
		} 
		else {
			run();
		}
	}

}
