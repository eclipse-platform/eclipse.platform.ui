package org.eclipse.ui.externaltools.internal.ant.dialog;

/**********************************************************************
Copyright (c) 2002 IBM Corp. and others. All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
 
Contributors:
**********************************************************************/

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.externaltools.dialog.ExternalToolGroupWizardPage;
import org.eclipse.ui.externaltools.dialog.ExternalToolNewWizard;
import org.eclipse.ui.externaltools.internal.model.ExternalToolsPlugin;
import org.eclipse.ui.externaltools.internal.model.IHelpContextIds;
import org.eclipse.ui.externaltools.internal.model.ToolMessages;
import org.eclipse.ui.externaltools.model.ExternalTool;
import org.eclipse.ui.externaltools.model.IExternalToolConstants;
import org.eclipse.ui.externaltools.model.ToolUtil;

/**
 * Wizard that will create a new external tool of type Ant build.
 */
public class AntExternalToolNewWizard extends ExternalToolNewWizard {
	private AntTargetsGroup antTargetsGroup;
	private IFile xmlFile;

	/**
	 * Creates the wizard for a new external tool
	 */
	public AntExternalToolNewWizard() {
		super(IExternalToolConstants.TOOL_TYPE_ANT_BUILD);
	}
	
	public AntExternalToolNewWizard(IFile file) {
		super(IExternalToolConstants.TOOL_TYPE_ANT_BUILD);
		xmlFile= validateXMLFile(file);
	}

	/* (non-Javadoc)
	 * Method declared on IWizard.
	 */
	public void addPages() {
		addMainPage();
		addAntTargetsPage();
		addOptionPage();
		addRefreshPage();
		
		optionGroup.setPromptForArgumentLabel(ToolMessages.getString("AntExternalToolNewWizard.promptForArgumentLabel")); //$NON-NLS-1$
	}
	
	/**
	 * Returns whether the given resource is an XML file
	 * based on the resource's file extension
	 * 	 * @param resource the resource to examine	 * @return whether the given resource is an XML file	 */
	private IFile validateXMLFile(IFile file) {
		if ("xml".equals(file.getFileExtension().toLowerCase())) {
			return file;
		}
		return null;
	}

	/* (non-Javadoc)
	 * Method declared on ExternalToolNewWizard.
	 */
	protected ImageDescriptor getDefaultImageDescriptor() {
		return ExternalToolsPlugin.getDefault().getImageDescriptor("icons/full/wizban/ant_wiz.gif"); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * Method declared on ExternalToolNewWizard.
	 */
	protected boolean updateTool(ExternalTool tool) {
		if (super.updateTool(tool))
			return updateToolFromGroup(tool, antTargetsGroup);
		return false;
	}

	/**
	 * Creates a wizard page to contain the ant build tool
	 * targets component and adds it to the wizard page
	 * list.
	 */
	private void addAntTargetsPage() {
		createAntTargetsGroup();
		if (antTargetsGroup == null)
			return;
		ExternalToolGroupWizardPage page;
		page = new AntTargetsGroupWizardPage("antGroupPage", antTargetsGroup, mainGroup, IHelpContextIds.ANT_TARGETS_WIZARD_PAGE); //$NON-NLS-1$
		page.setTitle(ToolMessages.getString("AntExternalToolNewWizard.antTargetsPageTitle")); //$NON-NLS-1$
		page.setDescription(ToolMessages.getString("AntExternalToolNewWizard.antTargetsPageDescription")); //$NON-NLS-1$
		addPage(page);
	}

	/**
	 * Creates and initializes the group for selecting
	 * which Ant targets to run.
	 */
	private void createAntTargetsGroup() {
		if (antTargetsGroup != null)
			return;
		antTargetsGroup = new AntTargetsGroup();
	}
	public IWizardPage getStartingPage() {
		return super.getStartingPage();
	}

	public void createPageControls(Composite pageContainer) {
		String fileLocation= null;
		if (xmlFile != null) {
			StringBuffer buffer = new StringBuffer();
			ToolUtil.buildVariableTag(IExternalToolConstants.VAR_WORKSPACE_LOC, xmlFile.getFullPath().toString(), buffer);
			fileLocation= buffer.toString();
			String baseName= xmlFile.getName() + " [" + xmlFile.getProject().getName() + "]" ;
			String name= baseName;
			int index= 0;
			while (ExternalToolsPlugin.getDefault().getToolRegistry(getShell()).hasToolNamed(name)) {
				name= baseName +  "_" + index;
			}
			mainGroup.setInitialName(name);
			mainGroup.setInitialLocation(fileLocation);
		}
		super.createPageControls(pageContainer);
		if (fileLocation != null) {
			antTargetsGroup.setFileLocation(fileLocation);
		}
	}

}
