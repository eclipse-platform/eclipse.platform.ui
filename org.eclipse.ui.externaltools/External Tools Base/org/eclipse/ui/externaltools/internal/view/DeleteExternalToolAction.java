package org.eclipse.ui.externaltools.internal.view;

/**********************************************************************
Copyright (c) 2002 IBM Corp. and others. All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
 
Contributors:
**********************************************************************/

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.externaltools.internal.model.ExternalToolsPlugin;
import org.eclipse.ui.externaltools.internal.model.IHelpContextIds;
import org.eclipse.ui.externaltools.internal.model.ToolMessages;
import org.eclipse.ui.externaltools.model.ExternalTool;
import org.eclipse.ui.externaltools.model.ExternalToolStorage;
import org.eclipse.ui.help.WorkbenchHelp;

/**
 * Action that will delete the currently selected
 * external tool in the view.
 */
public class DeleteExternalToolAction extends Action {
	private IWorkbenchPage page;
	private ExternalTool selectedTool;

	/**
	 * Create an action to delete the selected external tool
	 */
	public DeleteExternalToolAction(IWorkbenchPage page) {
		super();
		this.page = page;
		setText(ToolMessages.getString("DeleteExternalToolAction.text")); //$NON-NLS-1$
		setToolTipText(ToolMessages.getString("DeleteExternalToolAction.toolTip")); //$NON-NLS-1$
		setHoverImageDescriptor(ExternalToolsPlugin.getDefault().getImageDescriptor("icons/full/clcl16/del_tool.gif")); //$NON-NLS-1$
		setImageDescriptor(ExternalToolsPlugin.getDefault().getImageDescriptor("icons/full/elcl16/del_tool.gif")); //$NON-NLS-1$
		setDisabledImageDescriptor(ExternalToolsPlugin.getDefault().getImageDescriptor("icons/full/dlcl16/del_tool.gif")); //$NON-NLS-1$
		WorkbenchHelp.setHelp(this, IHelpContextIds.DELETE_TOOL_ACTION);
	}
	
	/**
	 * Returns the selected external tool.
	 */
	public ExternalTool getSelectedTool() {
		return selectedTool;
	}
	
	/* (non-Javadoc)
	 * Method declared on Action.
	 */
	public void run() {
		if (selectedTool == null)
			return;
		
		// Get user confirmation first
		Shell shell = page.getWorkbenchWindow().getShell();
		String title = ToolMessages.getString("DeleteExternalToolAction.confirmToolDeleteTitle"); //$NON-NLS-1$
		String msg = ToolMessages.format("DeleteExternalToolAction.confirmToolDeleteMsg", new Object[] {selectedTool.getName()}); //$NON-NLS-1$
		boolean deleteOk = MessageDialog.openQuestion(shell, title, msg);
		if (!deleteOk)
			return;
		
		// Delete the external tool
		ExternalToolStorage.deleteTool(selectedTool, shell);
	}

	/**
	 * Sets the selected external tool.
	 */
	public void setSelectedTool(ExternalTool tool) {
		selectedTool = tool;
		setEnabled(tool != null);
	}
}
