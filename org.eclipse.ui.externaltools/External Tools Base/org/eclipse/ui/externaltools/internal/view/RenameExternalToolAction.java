package org.eclipse.ui.externaltools.internal.view;

/**********************************************************************
Copyright (c) 2002 IBM Corp. and others. All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
 
Contributors:
**********************************************************************/

import java.text.MessageFormat;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.TextActionHandler;
import org.eclipse.ui.externaltools.internal.model.ExternalToolsPlugin;
import org.eclipse.ui.externaltools.internal.model.IHelpContextIds;
import org.eclipse.ui.externaltools.internal.model.ToolMessages;
import org.eclipse.ui.externaltools.model.ExternalTool;
import org.eclipse.ui.externaltools.model.ExternalToolStorage;
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
	
	private Shell getShell() {
		return Display.getCurrent().getActiveShell();
	}
	
	/* (non-Javadoc)
	 * Method declared on Action.
	 */
	public void run() {
		if (selectedTool != null) {
			InputDialog dialog= new InputDialog(getShell(), "Rename Tool", "Enter a new name for the tool", selectedTool.getName(), new IInputValidator() {
				public String isValid(String newText) {
					if (newText.equals(selectedTool.getName())) {
						return null;
					} else if (ExternalToolsPlugin.getDefault().getToolRegistry(getShell()).hasToolNamed(newText)) {
						return "An external tool of that name already exists";
					}
					return ExternalTool.validateToolName(newText);
				}
			});
			if (dialog.open() == Dialog.OK) {
				String newName= dialog.getValue();
				if (newName.equals(selectedTool.getName())) {
					return;
				}
				try {
					selectedTool.rename(newName);
					ExternalToolStorage.saveTool(selectedTool, getShell());
				} catch (CoreException exception) {
					ErrorDialog.openError(getShell(), "External Tool Error", MessageFormat.format("An exception occurred while renaming {0}", new String[] {selectedTool.getName()}), exception.getStatus());
				}
			}
		}
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
