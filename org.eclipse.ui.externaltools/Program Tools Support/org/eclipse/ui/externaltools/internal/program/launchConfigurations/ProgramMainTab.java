package org.eclipse.ui.externaltools.internal.program.launchConfigurations;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.ui.externaltools.internal.ui.FileSelectionDialog;
import org.eclipse.ui.externaltools.launchConfigurations.ExternalToolsMainTab;
import org.eclipse.ui.externaltools.model.IExternalToolConstants;
import org.eclipse.ui.externaltools.model.ToolUtil;

public class ProgramMainTab extends ExternalToolsMainTab {

	/**
	 * Prompts the user for a program location within the workspace and sets the
	 * location as a String containing the workspace_loc variable or
	 * <code>null</code> if no location was obtained from the user.
	 */
	protected void handleWorkspaceLocationButtonSelected() {
		FileSelectionDialog dialog;
		dialog = new FileSelectionDialog(getShell(), ResourcesPlugin.getWorkspace().getRoot(), "&Select a program");
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

}
