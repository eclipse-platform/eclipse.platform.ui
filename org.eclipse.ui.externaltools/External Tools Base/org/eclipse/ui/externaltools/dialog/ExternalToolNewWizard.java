package org.eclipse.ui.externaltools.dialog;

/**********************************************************************
Copyright (c) 2002 IBM Corp. and others. All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
 
Contributors:
**********************************************************************/

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.externaltools.group.ExternalToolMainGroup;
import org.eclipse.ui.externaltools.group.ExternalToolOptionGroup;
import org.eclipse.ui.externaltools.group.ExternalToolRefreshGroup;
import org.eclipse.ui.externaltools.group.IExternalToolGroup;
import org.eclipse.ui.externaltools.internal.model.ExternalToolsPlugin;
import org.eclipse.ui.externaltools.internal.model.IHelpContextIds;
import org.eclipse.ui.externaltools.internal.model.ToolMessages;
import org.eclipse.ui.externaltools.internal.registry.ExternalToolType;
import org.eclipse.ui.externaltools.model.ExternalTool;
import org.eclipse.ui.externaltools.model.ExternalToolStorage;
import org.eclipse.ui.externaltools.model.IExternalToolConstants;
import org.eclipse.ui.externaltools.model.ToolUtil;

/**
 

/**
 * Abstract wizard to create new external tools of a specified type.
 * <p>
 * This class can be extended by clients.
 * </p>
 */
public abstract class ExternalToolNewWizard extends Wizard implements INewWizard {
	private ExternalToolType toolType;
	private IResource selectedResource;
	private IWorkbench workbench;
	protected ExternalToolMainGroup mainGroup;
	protected ExternalToolOptionGroup optionGroup;
	protected ExternalToolRefreshGroup refreshGroup;
	
	/**
	 * Creates the wizard for a new external tool
	 */
	public ExternalToolNewWizard(String toolTypeId) {
		super();
		setWindowTitle(ToolMessages.getString("ExternalToolNewWizard.shellTitle")); //$NON-NLS-1$
		setDefaultPageImageDescriptor(getDefaultImageDescriptor());
		setNeedsProgressMonitor(true);
		toolType = ExternalToolsPlugin.getDefault().getTypeRegistry().getToolType(toolTypeId);
	}
	
	/**
	 * Creates a wizard page to contain the external tool
	 * main group component and adds it to the wizard page
	 * list.
	 */
	protected void addMainPage() {
		createMainGroup();
		if (mainGroup == null)
			return;
		ExternalToolGroupWizardPage page;
		page = new ExternalToolGroupWizardPage("mainGroupPage", mainGroup, IHelpContextIds.TOOL_MAIN_WIZARD_PAGE);  //$NON-NLS-1$
		if (toolType != null) {
			page.setTitle(toolType.getName());
			page.setDescription(toolType.getDescription());
		}
		addPage(page);
	}
	
	/**
	 * Creates a wizard page to contain the external tool
	 * option group component and adds it to the wizard page
	 * list.
	 */
	protected void addOptionPage() {
		createOptionGroup();
		if (optionGroup == null)
			return;
		ExternalToolGroupWizardPage page;
		page = new ExternalToolGroupWizardPage("optionGroupPage", optionGroup, IHelpContextIds.TOOL_OPTION_WIZARD_PAGE);  //$NON-NLS-1$
		page.setTitle(ToolMessages.getString("ExternalToolNewWizard.optionPageTitle")); //$NON-NLS-1$
		page.setDescription(ToolMessages.getString("ExternalToolNewWizard.optionPageDescription")); //$NON-NLS-1$
		addPage(page);
	}
	
	/**
	 * Creates a wizard page to contain the external tool
	 * refresh scope group component and adds it to the wizard page
	 * list.
	 */
	protected void addRefreshPage() {
		createRefreshGroup();
		if (refreshGroup == null)
			return;
		ExternalToolGroupWizardPage page;
		page = new ExternalToolGroupWizardPage("refreshGroupPage", refreshGroup, IHelpContextIds.TOOL_REFRESH_WIZARD_PAGE);  //$NON-NLS-1$
		page.setTitle(ToolMessages.getString("ExternalToolNewWizard.refreshPageTitle")); //$NON-NLS-1$
		page.setDescription(ToolMessages.getString("ExternalToolNewWizard.refreshPageDescription")); //$NON-NLS-1$
		addPage(page);
	}
	
	/**
	 * Creates the main group and initializes it using
	 * the information from the selected resource.
	 */
	protected void createMainGroup() {
		if (mainGroup != null)
			return;
		mainGroup = new ExternalToolMainGroup();
		if (selectedResource != null) {
			String path = selectedResource.getFullPath().toString();
			String loc = ToolUtil.buildVariableTag(
				IExternalToolConstants.VAR_RESOURCE_LOC, 
				path);
			mainGroup.setInitialLocation(loc);

			String name = path.replace(IPath.SEPARATOR, '-');
			int start = 0;
			while (name.charAt(start) == '-' && start < name.length())
				start++;
			int end = name.lastIndexOf('.');
			if (end == -1)
				end = name.length();
			name = name.substring(start, end);
			mainGroup.setInitialName(name);
		}
	}

	/**
	 * Creates the option group and initializes it.
	 */
	protected void createOptionGroup() {
		if (optionGroup != null)
			return;
		optionGroup = new ExternalToolOptionGroup();
	}

	/**
	 * Creates the refresh scope group and initializes it.
	 */
	protected void createRefreshGroup() {
		if (refreshGroup != null)
			return;
		refreshGroup = new ExternalToolRefreshGroup();
	}

	/* (non-Javadoc)
	 * Method declared on IWizard.
	 */
	public void dispose() {
		super.dispose();
		selectedResource = null;
	}

	/**
	 * Returns the selected resource.
	 */
	protected final IResource getSelectedResource() {
		return selectedResource;
	}

	/**
	 * Returns the default image descriptor for this wizard.
	 * 
	 * @return the image descriptor or <code>null</code> if
	 * 		none required.
	 */
	protected abstract ImageDescriptor getDefaultImageDescriptor();
	
	/**
	 * Returns the workbench.
	 */
	protected final IWorkbench getWorkbench() {
		return workbench;
	}

	/* (non-Javadoc)
	 * Method declared on IWorkbenchWizard.
	 */
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		this.workbench = workbench;
		
		Object sel = selection.getFirstElement();
		if (sel != null) {
			if (sel instanceof IResource)
				selectedResource = (IResource) sel;
			else if (sel instanceof IAdaptable)
				selectedResource = (IResource)((IAdaptable)sel).getAdapter(IResource.class);
		}
	}

	/**
	 * Returns a new external tool object that is not yet
	 * initialized with the information collected by the
	 * wizard.
	 * <p>
	 * By default, return a new external tool using the name
	 * specified in the main page and the tool type of the wizard.
	 * </p>
	 * 
	 * @return a new external tool or <code>null</code> if not possible
	 */
	protected ExternalTool newTool() {
		if (mainGroup == null)
			return null;
		String name = mainGroup.getNameFieldValue();
		if (name == null || name.length() == 0)
			return null;
		if (toolType == null)
			return null;
		try {
			return new ExternalTool(toolType.getId(), name);
		} catch (CoreException e) {
			return null;
		}
	}
		
	/* (non-Javadoc)
	 * Method declared on IWizard.
	 */
	public final boolean performFinish() {
		ExternalTool tool = newTool();
		if (tool == null)
			return false;
		if (!updateTool(tool))
			return false;
		return ExternalToolStorage.saveTool(tool, getShell());
	}
	
	/**
	 * Update the new external tool with the information
	 * collected from the user.
	 * <p>
	 * By default, ask each group to update the tool.
	 * </p>
	 * 
	 * @param tool the new external tool to update
	 * @return <code>true</code> if the tool was updated properly,
	 * 		or <code>false</code> if the tool could not be updated
	 * 		and the wiard should remain open.
	 */
	protected boolean updateTool(ExternalTool tool) {
		if (!updateToolFromGroup(tool, mainGroup))
			return false;
		if (!updateToolFromGroup(tool, optionGroup))
			return false;
		if (!updateToolFromGroup(tool, refreshGroup))
			return false;
		return true;
	}

	/**
	 * Update the new external tool with the information
	 * collected from the user via an external tool group.
	 * 
	 * @param tool the new external tool to update
	 * @param group the external tool group of visual component (<code>null</code> is ignored)
	 * @return <code>true</code> if the tool was updated properly,
	 * 		or <code>false</code> if the tool could not be updated
	 * 		because the group was in an invalid state.
	 */
	protected final boolean updateToolFromGroup(ExternalTool tool, IExternalToolGroup group) {
		if (group == null)
			return true;
		if (!group.isValid())
			return false;
		group.updateTool(tool);
		return true;
	}
}
