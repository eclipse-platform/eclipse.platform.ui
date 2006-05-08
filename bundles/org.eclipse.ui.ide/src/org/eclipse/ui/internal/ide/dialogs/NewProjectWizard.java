/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.ide.dialogs;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;

/**
 * Standard workbench wizard that guides the user to supply
 * the necessary information to create a project.
 */
public class NewProjectWizard extends MultiStepWizard implements
        INewWizard {

    // Reference to the pages provided by this wizard
    private WizardNewProjectNameAndLocationPage creationPage;


    // Newly created project
    private IProject newProject;

    // initial values for the pages provided by this wizard
    private String initialProjectName;


    /**
     * Creates an empty wizard for creating a new project
     * in the workspace.
     */
    public NewProjectWizard() {
        super();

        IDEWorkbenchPlugin plugin = IDEWorkbenchPlugin.getDefault();
        IDialogSettings workbenchSettings = plugin.getDialogSettings();
        IDialogSettings section = workbenchSettings
                .getSection("NewProjectWizard");//$NON-NLS-1$
        if (section == null) {
			section = workbenchSettings.addNewSection("NewProjectWizard");//$NON-NLS-1$
		}
        setDialogSettings(section);
    }

    /* (non-Javadoc)
     * Method declared on MultiStepWizard.
     */
    protected void addCustomPages() {
        creationPage = new WizardNewProjectNameAndLocationPage(
                "newProjectCreationPage");//$NON-NLS-1$
        creationPage.setTitle(IDEWorkbenchMessages.NewProjectWizard_title);
        creationPage.setDescription(IDEWorkbenchMessages.WizardNewProjectCreationPage_description);
        creationPage.setInitialProjectName(initialProjectName);
        this.addPage(creationPage);

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
        return IDEWorkbenchMessages.NewProjectWizard_title;
    }

    /* (non-Javadoc)
     * Method declared on MultiStepWizard.
     */
    protected String getConfigurePageDescription() {
        return IDEWorkbenchMessages.WizardProjectConfigurePage_description;
    }

    /* (non-Javadoc)
     * Method declared on MultiStepWizard.
     */
    protected String getReviewPageTitle() {
        return IDEWorkbenchMessages.NewProjectWizard_title;
    }

    /* (non-Javadoc)
     * Method declared on MultiStepWizard.
     */
    protected String getReviewPageDescription() {
        return IDEWorkbenchMessages.WizardProjectReviewPage_description;
    }

    /* (non-Javadoc)
     * Method declared on MultiStepWizard.
     */
    protected String getFinishStepLabel(WizardStep[] steps) {
        // The first step is the project creation which has no wizard
        // pages, so ignore it. If there is only one step after that,
        // then it needs the "Finish" label. So the "Finish Step" label
        // is only needed if more than 2 steps in the list.
        if (steps.length > 2) {
			return super.getFinishStepLabel(steps);
		} else {
			return null;
		}
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
     * Method declared on IProjectProvider.
     */
    public IProject getProject() {
        return newProject;
    }

    /* (non-Javadoc)
     * Method declared on MultiStepWizard.
     */
    /* package */boolean handleMissingStepWizard(WizardStep step) {
        MessageDialog
                .openError(
                        getShell(),
                        IDEWorkbenchMessages.NewProjectWizard_errorTitle,
                        NLS.bind(IDEWorkbenchMessages.NewProjectWizard_noWizard, step.getLabel()));
        return false;
    }

    /* (non-Javadoc)
     * Method declared on IWorkbenchWizard.
     */
    public void init(IWorkbench workbench, IStructuredSelection currentSelection) {
        initializeDefaultPageImageDescriptor();
        setWindowTitle(IDEWorkbenchMessages.NewProjectWizard_windowTitle);
    }

    /**
     * Sets the image banner for the wizard
     */
    protected void initializeDefaultPageImageDescriptor() {
        ImageDescriptor desc = IDEWorkbenchPlugin.getIDEImageDescriptor("wizban/newprj_wiz.png");//$NON-NLS-1$
        setDefaultPageImageDescriptor(desc);
       
    }   

    /**
     * Sets the initial project name. Leading and trailing
     * spaces in the name are ignored.
     * 
     * @param name initial project name
     */
    public void setInitialProjectName(String name) {
        if (name == null) {
			initialProjectName = null;
		} else {
			initialProjectName = name.trim();
		}
    }

    /**
     * Sets the newly created project resource
     */
    /* package */void setNewProject(IProject project) {
        newProject = project;
    }
}
