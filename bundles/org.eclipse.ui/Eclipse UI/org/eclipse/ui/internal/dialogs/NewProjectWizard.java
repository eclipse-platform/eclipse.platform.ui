package org.eclipse.ui.internal.dialogs;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectNatureDescriptor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.registry.Capability;
import org.eclipse.ui.internal.registry.CapabilityRegistry;
import org.eclipse.ui.internal.registry.Category;
import org.eclipse.ui.wizards.newresource.BasicNewResourceWizard;

/**
 * Standard workbench wizard that creates a new project resource in
 * the workspace.
 * <p>
 * Example:
 * <pre>
 * IWizard wizard = new NewProjectWizard();
 * wizard.init(workbench, selection);
 * WizardDialog dialog = new WizardDialog(shell, wizard);
 * dialog.open();
 * </pre>
 * During the call to <code>open</code>, the wizard dialog is presented to the
 * user. When the user hits Finish, a project resource with the user-specified
 * name is created, the dialog closes, and the call to <code>open</code> returns.
 * </p>
 */
public class NewProjectWizard extends BasicNewResourceWizard {
	// Reference to the pages provided by this wizard
	private WizardNewProjectCreationPage creationPage;
	private WizardNewProjectCapabilityPage capabilityPage;
	private WizardProjectReviewPage reviewPage;
	private WizardProjectConfigurePage configPage;
	
	// Newly created project
	private IProject newProject;

	// initial values for the pages provided by this wizard
	private String initialProjectName;
	private Capability[] initialProjectCapabilities;
	private Category[] initialSelectedCategories;
	
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
	 * Method declared on IWizard.
	 */
	public void addPages() {
		super.addPages();
		
		creationPage = new WizardNewProjectCreationPage("newProjectCreationPage");//$NON-NLS-1$
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
		
		reviewPage = new WizardProjectReviewPage("newProjectCapabilityPage");//$NON-NLS-1$
		reviewPage.setTitle(WorkbenchMessages.getString("NewProjectWizard.title")); //$NON-NLS-1$
		reviewPage.setDescription(WorkbenchMessages.getString("WizardProjectReviewPage.description")); //$NON-NLS-1$
		this.addPage(reviewPage);
		
		configPage = new WizardProjectConfigurePage("projectConfigurePage");//$NON-NLS-1$
		configPage.setTitle(WorkbenchMessages.getString("NewProjectWizard.title")); //$NON-NLS-1$
		configPage.setDescription(WorkbenchMessages.getString("WizardProjectConfigurePage.description")); //$NON-NLS-1$
		this.addPage(configPage);
	}

	/**
	 * Builds the collection of steps to create and install
	 * the chosen capabilities
	 */
	private void buildSteps() {
		Capability[] caps = capabilityPage.getSelectedCapabilities();
		CapabilityRegistry reg = WorkbenchPlugin.getDefault().getCapabilityRegistry();
		IStatus status = reg.validateCapabilities(caps);
		if (status.isOK()) {
			Capability[] results = reg.pruneCapabilities(caps);
			WizardStep[] steps = new WizardStep[results.length + 1];
			steps[0] = new CreateProjectStep(1, creationPage);
			for (int i = 0; i < results.length; i++)
				steps[i+1] = new InstallCapabilityStep(i+2, results[i]);
			reviewPage.setSteps(steps);
			configPage.setSteps(steps);
		} else {
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
	 * Method declared on IWizard.
	 */
	public IWizardPage getNextPage(IWizardPage page) {
		if (page == capabilityPage)
			buildSteps();
		return super.getNextPage(page);
	}

	/* (non-Javadoc)
	 * Method declared on IWorkbenchWizard.
	 */
	public void init(IWorkbench workbench, IStructuredSelection currentSelection) {
		super.init(workbench, currentSelection);
		setWindowTitle(WorkbenchMessages.getString("NewProjectWizard.windowTitle")); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * Method declared on BasicNewResourceWizard.
	 */
	protected void initializeDefaultPageImageDescriptor() {
		String iconPath = "icons/full/";//$NON-NLS-1$		
		try {
			URL installURL = WorkbenchPlugin.getDefault().getDescriptor().getInstallURL();
			URL url = new URL(installURL, iconPath + "wizban/newprj_wiz.gif");//$NON-NLS-1$
			ImageDescriptor desc = ImageDescriptor.createFromURL(url);
			setDefaultPageImageDescriptor(desc);
		}
		catch (MalformedURLException e) {
			// Should not happen. Ignore.
		}
	}

	/* (non-Javadoc)
	 * Method declared on IWizard.
	 */
	public boolean performFinish() {
/*		createNewProject();
		
		if (newProject == null)
			return false;
	
		updatePerspective();
		selectAndReveal(newProject);
*/	
		return false;
	}
	
	/**
	 * Sets the initial categories to be selected.
	 * 
	 * @param categories initial categories to select
	 */
	public void setInitialSelectedCategories(Category[] categories) {
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
}
