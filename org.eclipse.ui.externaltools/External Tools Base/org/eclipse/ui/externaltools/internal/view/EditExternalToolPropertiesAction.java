package org.eclipse.ui.externaltools.internal.view;

/**********************************************************************
Copyright (c) 2002 IBM Corp. and others. All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
 
Contributors:
**********************************************************************/

import org.eclipse.jface.action.Action;
import org.eclipse.ui.dialogs.PropertyDialogAction;
import org.eclipse.ui.externaltools.internal.model.ExternalToolsPlugin;
import org.eclipse.ui.externaltools.internal.model.IHelpContextIds;
import org.eclipse.ui.externaltools.internal.model.ToolMessages;
import org.eclipse.ui.externaltools.model.ExternalTool;
import org.eclipse.ui.help.WorkbenchHelp;

/**
 * Action that will open a properties dialog to edit
 * the selected external tool.
 */
public class EditExternalToolPropertiesAction extends Action {
	private ExternalTool selectedTool;
	private PropertyDialogAction propAction;

	/**
	 * Create an action to edit the selected external tool.
	 */
	public EditExternalToolPropertiesAction(ExternalToolView view) {
		super();
		setText(ToolMessages.getString("EditExternalToolPropertiesAction.text")); //$NON-NLS-1$
		setToolTipText(ToolMessages.getString("EditExternalToolPropertiesAction.toolTip")); //$NON-NLS-1$
		setHoverImageDescriptor(ExternalToolsPlugin.getDefault().getImageDescriptor("icons/full/clcl16/prop_tool.gif")); //$NON-NLS-1$
		setImageDescriptor(ExternalToolsPlugin.getDefault().getImageDescriptor("icons/full/elcl16/prop_tool.gif")); //$NON-NLS-1$
		setDisabledImageDescriptor(ExternalToolsPlugin.getDefault().getImageDescriptor("icons/full/dlcl16/prop_tool.gif")); //$NON-NLS-1$
		WorkbenchHelp.setHelp(this, IHelpContextIds.EDIT_TOOL_PROPERTIES_ACTION);
		propAction = new PropertyDialogAction(view.getSite().getShell(), view.getTreeViewer());
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
		if (propAction.isEnabled())
			propAction.run();
	}

	/**
	 * Sets the selected external tool.
	 */
	public void setSelectedTool(ExternalTool tool) {
		selectedTool = tool;
		setEnabled(tool != null);
	}
}
