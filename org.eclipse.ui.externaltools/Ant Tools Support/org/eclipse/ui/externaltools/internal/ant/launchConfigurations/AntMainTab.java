package org.eclipse.ui.externaltools.internal.ant.launchConfigurations;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.ui.externaltools.internal.ui.FileSelectionDialog;
import org.eclipse.ui.externaltools.internal.launchConfigurations.ExternalToolsMainTab;
import org.eclipse.ui.externaltools.internal.model.IExternalToolConstants;
import org.eclipse.ui.externaltools.internal.model.ToolUtil;

public class AntMainTab extends ExternalToolsMainTab {

	/**
	 * Prompts the user for a workspace location within the workspace and sets
	 * the location as a String containing the workspace_loc variable or
	 * <code>null</code> if no location was obtained from the user.
	 */
	protected void handleWorkspaceLocationButtonSelected() {
		FileSelectionDialog dialog;
		dialog = new FileSelectionDialog(getShell(), ResourcesPlugin.getWorkspace().getRoot(), AntLaunchConfigurationMessages.getString("AntMainTab.&Select_a_build_file__1")); //$NON-NLS-1$
		dialog.setFileFilter("*.xml", true); //$NON-NLS-1$
		dialog.open();
		IFile file = dialog.getResult();
		if (file == null) {
			return;
		}
		StringBuffer buf = new StringBuffer();
		ToolUtil.buildVariableTag(IExternalToolConstants.VAR_WORKSPACE_LOC, file.getFullPath().toString(), buf);
		String text= buf.toString();
		if (text != null) {
			locationField.setText(text);
		}
	}

	/**
	 * @see org.eclipse.ui.externaltools.launchConfigurations.ExternalToolsMainTab#getWorkingDirectoryLabel()
	 */
	protected String getWorkingDirectoryLabel() {
		return AntLaunchConfigurationMessages.getString("AntMainTab.Base_&Directory__3"); //$NON-NLS-1$
	}
}
