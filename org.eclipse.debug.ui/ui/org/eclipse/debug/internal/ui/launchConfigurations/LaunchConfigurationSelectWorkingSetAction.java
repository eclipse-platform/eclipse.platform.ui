package org.eclipse.debug.internal.ui.launchConfigurations;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/
 
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.IDebugHelpContextIds;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.IWorkingSetSelectionDialog;
import org.eclipse.ui.help.WorkbenchHelp;

public class LaunchConfigurationSelectWorkingSetAction extends Action{
	
	private Shell fShell;
	private IWorkingSet fWorkingSet;
	private LaunchConfigurationWorkingSetActionManager fActionMgr;

	public LaunchConfigurationSelectWorkingSetAction(LaunchConfigurationWorkingSetActionManager actionMgr, Shell shell) {
		super(LaunchConfigurationsMessages.getString("LaunchConfigurationSelectWorkingSetAction.Select_working_set_..._1")); //$NON-NLS-1$
		setToolTipText(LaunchConfigurationsMessages.getString("LaunchConfigurationSelectWorkingSetAction.Choose_a_working_set_and_set_it_as_the_filter_for_this_viewer_2")); //$NON-NLS-1$
		
		fShell= shell;
		fActionMgr= actionMgr;
		WorkbenchHelp.setHelp(this, IDebugHelpContextIds.SELECT_WORKING_SET_ACTION);
	}
	
	/*
	 * Overrides method from Action
	 */
	public void run() {
		if (fShell == null) {
			fShell= DebugUIPlugin.getShell();
		}
		IWorkingSetManager manager= PlatformUI.getWorkbench().getWorkingSetManager();
		IWorkingSetSelectionDialog dialog= manager.createWorkingSetSelectionDialog(fShell, false);
		IWorkingSet workingSet= fActionMgr.getWorkingSet();
		if (workingSet != null) {
			dialog.setSelection(new IWorkingSet[]{workingSet});
		}

		if (dialog.open() == Window.OK) {
			IWorkingSet[] result= dialog.getSelection();
			if (result != null && result.length > 0) {
				fActionMgr.setWorkingSet(result[0], true);
				manager.addRecentWorkingSet(result[0]);
			}
			else {
				fActionMgr.setWorkingSet(null, true);
			}
		}
	}

}
