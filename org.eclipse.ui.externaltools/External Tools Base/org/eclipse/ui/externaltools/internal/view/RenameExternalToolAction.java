package org.eclipse.ui.externaltools.internal.view;

/**********************************************************************
Copyright (c) 2002 IBM Corp. and others. All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
 
Contributors:
**********************************************************************/

import org.eclipse.jface.action.Action;
import org.eclipse.ui.actions.TextActionHandler;
import org.eclipse.ui.externaltools.internal.model.ExternalToolsPlugin;
import org.eclipse.ui.externaltools.internal.model.IHelpContextIds;
import org.eclipse.ui.externaltools.internal.model.ToolMessages;
import org.eclipse.ui.externaltools.model.ExternalTool;
import org.eclipse.ui.help.WorkbenchHelp;

/**
 * Action that will rename the external tool selected
 * in the the view.
 */
public class RenameExternalToolAction extends Action {
	private ExternalToolView view;
	private TextActionHandler textActionHandler;
	private ExternalTool selectedTool;

	/**
	 * Create an action to rename the external tool.
	 */
	public RenameExternalToolAction(ExternalToolView view) {
		super();
		this.view = view;
		setText(ToolMessages.getString("RenameExternalToolAction.text")); //$NON-NLS-1$
		setToolTipText(ToolMessages.getString("RenameExternalToolAction.toolTip")); //$NON-NLS-1$
		setHoverImageDescriptor(ExternalToolsPlugin.getDefault().getImageDescriptor("icons/full/clcl16/rename_tool.gif")); //$NON-NLS-1$
		setImageDescriptor(ExternalToolsPlugin.getDefault().getImageDescriptor("icons/full/elcl16/rename_tool.gif")); //$NON-NLS-1$
		setDisabledImageDescriptor(ExternalToolsPlugin.getDefault().getImageDescriptor("icons/full/dlcl16/rename_tool.gif")); //$NON-NLS-1$
		WorkbenchHelp.setHelp(this, IHelpContextIds.RENAME_TOOL_ACTION);
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
		org.eclipse.jface.dialogs.MessageDialog.openInformation(view.getSite().getShell(), "Action", "This action is not yet implemented");
	}

	/**
	 * Sets the selected external tool.
	 */
	public void setSelectedTool(ExternalTool tool) {
		selectedTool = tool;
		setEnabled(tool != null);
	}
	
	/**
	 * Sets the text action handler
	 */
	public void setTextActionHandler(TextActionHandler handler) {
		textActionHandler = handler;
	}
}
