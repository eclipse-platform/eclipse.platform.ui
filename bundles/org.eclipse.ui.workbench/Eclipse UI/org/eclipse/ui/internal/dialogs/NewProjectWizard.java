/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.dialogs;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.registry.*;

/**
 * Standard workbench wizard that guides the user to supply
 * the necessary information to create a project.
 */
public class NewProjectWizard extends MultiStepCapabilityWizard implements INewWizard {
	// init method parameters supplied
	private IWorkbench workbench;
	private IStructuredSelection selection;
	
	// Reference to the pages provided by this wizard
	private WizardNewProjectNameAndLocationPage creationPage;
	private WizardNewProjectCapabilityPage capabilityPage;
	
	// Newly created project
	private IProject newProject;

	// initial values for the pages provided by this wizard
	private String initialProjectName;
	private Capability[] initialProjectCapabilities;
	private ICategory[] initialSelectedCategories;
	
	/**
	 * Creates an empty wizard for creating a new project
	 * in the workspace.
	 */
	public NewProjectWizard() {
		super();
		
		WorkbenchPlugin plugin = WorkbenchPlugin.getDefault();
		IDialogSettings workbenchSettings = plugin.getDialogSettings();
		IDialogSettings section = workbenchSettings.getSection("NewProjectWizard");//$NON-NLS-1$
		if (section == null)
			section = workbenchSettings.addNewSection("NewProjectWizard");//$NON-NLS-1$
		setDialogSettings(section);
	}

	/* (non-Javadoc)
	 * Method declared on MultiStepWizard.
	 */
	protected void addCustomPages() {
		creationPage = new WizardNewProjectNameAndLocationPage("newProjectCreationPage");//$NON-NLS-1$
		creationPage.setTitle(WorkbenchMessages.getString("NewProjectWizard.title")); //$NON-NLS-1$
		creationPage.setDescription(WorkbenchMessages.getString("WizardNewProjectCreationPage.description")); //$NON-NLS-1$
		creationPage.setInitialProjectName(initialProjectName);
		this.addPage(creationPage);
		
		capabilityPage = new WizardNewProjectCapabilityPage("newProjectCapabilityPage");//$NON-NLS-1$
		capabilityPage.setTitle(WorkbenchMessages.getString("NewProjectWizard.title")); //$NON-NLS-1$
		capabilityPage.setDescription(WorkbenchMessages.getString("WizardNewProjectCapabilityPage.description")); //$NON-NLS-1$
		capabilityPage.setInitialProjectCapabilities(initialProjectCapabilities);
		capabilityPage.setInitialSelectedCategories(initialSelectedCategories);
		this.addPage(capabilityPage);
	}

	/**
	 * Builds the collection of steps to create and install
	 * the chosen capabilities.
	 * 
	 * @return <code>true</code> if successful, <code>false</code>
	 * 		if a problem was detected.
	 */
	private boolean buildSteps() {
		Capability[] caps = capabilityPage.getSelectedCapabilities();
		CapabilityRegistry reg = WorkbenchPlugin.getDefault().getCapabilityRegistry();
		IStatus status = reg.validateCapabilities(caps);
		if (status.isOK()) {
			Capability[] results = reg.pruneCapabilities(caps);
			WizardStep[] steps = new WizardStep[results.length + 1];
			steps[0] = new CreateProjectStep(1, creationPage, this);
			for (int i = 0; i < results.length; i++)
				steps[i+1] = new InstallCapabilityStep(i+2, results[i], workbench, this);
			setSteps(steps);
			return true;
		} else {
			ErrorDialog.openError(
				getShell(), 
				WorkbenchMessages.getString("NewProjectWizard.errorTitle"),  //$NON-NLS-1$
				WorkbenchMessages.getString("NewProjectWizard.invalidCapabilities"),  //$NON-NLS-1$
		 		status);
			return false;
		}
	}
	
	/* (non-Javadoc)
	 * Method declared on MultiStepWizard.
	 */
	protected boolean canFinishOnReviewPage() {
		// yes if the only step is to create the project.
		return getSteps().length == 1;
	}
	
	/* (non-Javadoc)
	 * Method declared on MultiStepWizard.
	 */
	protected String getConfigurePageTitle() {
		return WorkbenchMessages.getString("NewProjectWizard.title"); //$NON-NLS-1$
	}
	
	/* (non-Javadoc)
	 * Method declared on MultiStepWizard.
	 */
	protected String getConfigurePageDescription() {
		return WorkbenchMessages.getString("WizardProjectConfigurePage.description"); //$NON-NLS-1$
	}
	
	/* (non-Javadoc)
	 * Method declared on MultiStepWizard.
	 */
	protected  String getReviewPageTitle() {
		return WorkbenchMessages.getString("NewProjectWizard.title"); //$NON-NLS-1$
	}
	
	/* (non-Javadoc)
	 * Method declared on MultiStepWizard.
	 */
	protected String getReviewPageDescription() {
		return WorkbenchMessages.getString("WizardProjectReviewPage.description"); //$NON-NLS-1$
	}
		
	/* (non-Javadoc)
	 * Method declared on MultiStepWizard.
	 */
	protected String getFinishStepLabel(WizardStep[] steps) {
		// The first step is the project creation which has no wizard
		// pages, so ignore it. If there is only one step after that,
		// then it needs the "Finish" label. So the "Finish Step" label
		// is only needed if more than 2 steps in the list.
		if (steps.length > 2)
			return super.getFinishStepLabel(steps);
		else
			return null;
	}
	
	/**
	 * Returns the newly created project.
	 *
	 * @return the created project, or <code>null</code>
	 *   if project is not created yet.
	 */
	public IProject getNewProject() {
		return newProject;
	}

	/* (non-Javadoc)
	 * Method declared on MultiStepWizard.
	 */
	protected String[] getPerspectiveChoices() {
		ArrayList results = new ArrayList();
		Capability[] caps = capabilityPage.getSelectedCapabilities();
		for (int i = 0; i < caps.length; i++) {
			ArrayList ids = caps[i].getPerspectiveChoices();
			if (ids != null) {
				Iterator enum = ids.iterator();
				while (enum.hasNext()) {
					String id = (String)enum.next();
					if (!results.contains(id))
						results.add(id);
				}
			}
		}
		String[] ids = new String[results.size()];
		results.toArray(ids);
		return ids;
	}
	
	/* (non-Javadoc)
	 * Method declared on IProjectProvider.
	 */
	 public IProject getProject() {
	 	return newProject;
	 }
	 
	/* (non-Javadoc)
	 * Method declared on IWizard.
	 */
	public IWizardPage getNextPage(IWizardPage page) {
		if (page == capabilityPage) {
			if (!buildSteps())
				return capabilityPage;
		}
		return super.getNextPage(page);
	}

	/* (non-Javadoc)
	 * Method declared on MultiStepWizard.
	 */
	/* package */ boolean handleMissingStepWizard(WizardStep step) {
		MessageDialog.openError(
			getShell(),
			WorkbenchMessages.getString("NewProjectWizard.errorTitle"), //$NON-NLS-1$
			WorkbenchMessages.format("NewProjectWizard.noWizard", new Object[] {step.getLabel()})); //$NON-NLS-1$
		return false;
	}

	/* (non-Javadoc)
	 * Method declared on IWorkbenchWizard.
	 */
	public void init(IWorkbench workbench, IStructuredSelection currentSelection) {
		this.workbench = workbench;
		this.selection = currentSelection;
		initializeDefaultPageImageDescriptor();
		setWindowTitle(WorkbenchMessages.getString("NewProjectWizard.windowTitle")); //$NON-NLS-1$
	}

	/**
	 * Sets the image banner for the wizard
	 */
	protected void initializeDefaultPageImageDescriptor() {
		String iconPath = "icons/full/";//$NON-NLS-1$		
		try {
			URL installURL = Platform.getPlugin(PlatformUI.PLUGIN_ID).getDescriptor().getInstallURL();
			URL url = new URL(installURL, iconPath + "wizban/newprj_wiz.gif");//$NON-NLS-1$
			ImageDescriptor desc = ImageDescriptor.createFromURL(url);
			setDefaultPageImageDescriptor(desc);
		}
		catch (MalformedURLException e) {
			// Should not happen. Ignore.
		}
	}

	/**
	 * Sets the initial categories to be selected.
	 * 
	 * @param categories initial categories to select
	 */
	public void setInitialSelectedCategories(ICategory[] categories) {
		initialSelectedCategories = categories;
	}
	
	/**
	 * Sets the initial project capabilities to be selected.
	 * 
	 * @param capabilities initial project capabilities to select
	 */
	public void setInitialProjectCapabilities(Capability[] capabilities) {
		initialProjectCapabilities = capabilities;
	}
	
	/**
	 * Sets the initial project name. Leading and trailing
	 * spaces in the name are ignored.
	 * 
	 * @param name initial project name
	 */
	public void setInitialProjectName(String name) {
		if (name == null)
			initialProjectName = null;
		else
			initialProjectName = name.trim();
	}
	
	/**
	 * Sets the newly created project resource
	 */
	/* package */ void setNewProject(IProject project) {
		newProject = project;
	}
}
