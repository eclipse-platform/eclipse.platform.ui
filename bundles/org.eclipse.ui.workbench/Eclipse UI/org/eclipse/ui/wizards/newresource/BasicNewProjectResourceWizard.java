/************************************************************************
Copyright (c) 2000, 2003 IBM Corporation and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
    IBM - Initial implementation
************************************************************************/

package org.eclipse.ui.wizards.newresource;

import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.StringTokenizer;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.*;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.dialogs.WizardNewProjectCreationPage;
import org.eclipse.ui.dialogs.WizardNewProjectReferencePage;
import org.eclipse.ui.internal.IPreferenceConstants;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.dialogs.MessageDialogWithToggle;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * Standard workbench wizard that creates a new project resource in
 * the workspace.
 * <p>
 * This class may be instantiated and used without further configuration;
 * this class is not intended to be subclassed.
 * </p>
 * <p>
 * Example:
 * <pre>
 * IWorkbenchWizard wizard = new BasicNewProjectResourceWizard();
 * wizard.init(workbench, selection);
 * WizardDialog dialog = new WizardDialog(shell, wizard);
 * dialog.open();
 * </pre>
 * During the call to <code>open</code>, the wizard dialog is presented to the
 * user. When the user hits Finish, a project resource with the user-specified
 * name is created, the dialog closes, and the call to <code>open</code> returns.
 * </p>
 */
public class BasicNewProjectResourceWizard extends BasicNewResourceWizard
	implements IExecutableExtension
{
	private WizardNewProjectCreationPage mainPage;
	private WizardNewProjectReferencePage referencePage;
	
	// cache of newly-created project
	private IProject newProject;
	
	/**
	 * The config element which declares this wizard.
	 */
	private IConfigurationElement configElement;

	private static String WINDOW_PROBLEMS_TITLE = ResourceMessages.getString("NewProject.errorOpeningWindow"); //$NON-NLS-1$

	/**
	 * Extension attribute name for final perspective.
	 */
	private static final String FINAL_PERSPECTIVE = "finalPerspective";  //$NON-NLS-1$

	/**
	 * Extension attribute name for preferred perspectives.
	 */
	private static final String PREFERRED_PERSPECTIVES = "preferredPerspectives";  //$NON-NLS-1$
		
/**
 * Creates a wizard for creating a new project resource in the workspace.
 */
public BasicNewProjectResourceWizard() {
	AbstractUIPlugin plugin = (AbstractUIPlugin) Platform.getPlugin(PlatformUI.PLUGIN_ID);
	IDialogSettings workbenchSettings = plugin.getDialogSettings();
	IDialogSettings section = workbenchSettings.getSection("BasicNewProjectResourceWizard");//$NON-NLS-1$
	if(section == null)
		section = workbenchSettings.addNewSection("BasicNewProjectResourceWizard");//$NON-NLS-1$
	setDialogSettings(section);
}
/* (non-Javadoc)
 * Method declared on IWizard.
 */
public void addPages() {
	super.addPages();
	
	mainPage = new WizardNewProjectCreationPage("basicNewProjectPage");//$NON-NLS-1$
	mainPage.setTitle(ResourceMessages.getString("NewProject.title")); //$NON-NLS-1$
	mainPage.setDescription(ResourceMessages.getString("NewProject.description")); //$NON-NLS-1$
	this.addPage(mainPage);
	
	// only add page if there are already projects in the workspace
	if (ResourcesPlugin.getWorkspace().getRoot().getProjects().length > 0) {
		referencePage = new WizardNewProjectReferencePage("basicReferenceProjectPage");//$NON-NLS-1$
		referencePage.setTitle(ResourceMessages.getString("NewProject.referenceTitle")); //$NON-NLS-1$
		referencePage.setDescription(ResourceMessages.getString("NewProject.referenceDescription")); //$NON-NLS-1$
		this.addPage(referencePage);
	}
}
/**
 * Creates a new project resource with the selected name.
 * <p>
 * In normal usage, this method is invoked after the user has pressed Finish on
 * the wizard; the enablement of the Finish button implies that all controls
 * on the pages currently contain valid values.
 * </p>
 * <p>
 * Note that this wizard caches the new project once it has been successfully
 * created; subsequent invocations of this method will answer the same
 * project resource without attempting to create it again.
 * </p>
 *
 * @return the created project resource, or <code>null</code> if the project
 *    was not created
 */
private IProject createNewProject() {
	if (newProject != null)
		return newProject;

	// get a project handle
	final IProject newProjectHandle = mainPage.getProjectHandle();

	// get a project descriptor
	IPath newPath = null;
	if (!mainPage.useDefaults())
		newPath = mainPage.getLocationPath();
		
	IWorkspace workspace = ResourcesPlugin.getWorkspace();
	final IProjectDescription description = workspace.newProjectDescription(newProjectHandle.getName());
	description.setLocation(newPath);

	// update the referenced project if provided
	if (referencePage != null) {
		IProject[] refProjects = referencePage.getReferencedProjects();
		if (refProjects.length > 0)
			description.setReferencedProjects(refProjects);
	}
	
	// create the new project operation
	WorkspaceModifyOperation op = new WorkspaceModifyOperation() {
		protected void execute(IProgressMonitor monitor) throws CoreException {
			createProject(description, newProjectHandle, monitor);
		}
	};

	// run the new project creation operation
	try {
		getContainer().run(true, true, op);
	}
	catch (InterruptedException e) {
		return null;
	}
	catch (InvocationTargetException e) {
		// ie.- one of the steps resulted in a core exception	
		Throwable t = e.getTargetException();
		if (t instanceof CoreException) {
			if (((CoreException)t).getStatus().getCode() == IResourceStatus.CASE_VARIANT_EXISTS) {
				MessageDialog.openError(
					getShell(), 
					ResourceMessages.getString("NewProject.errorMessage"),  //$NON-NLS-1$
					ResourceMessages.format("NewProject.caseVariantExistsError", new String[] { newProjectHandle.getName()} )  //$NON-NLS-1$,
					);	
			} else {
				ErrorDialog.openError(
					getShell(), 
					ResourceMessages.getString("NewProject.errorMessage"),  //$NON-NLS-1$
					null, // no special message
			 		((CoreException) t).getStatus());
			}
		} else {
			// CoreExceptions are handled above, but unexpected runtime exceptions and errors may still occur.
			Platform.getPlugin(PlatformUI.PLUGIN_ID).getLog().log(
				new Status(
					Status.ERROR, 
					PlatformUI.PLUGIN_ID, 
					0, 
					t.toString(),
					t));
			MessageDialog.openError(
				getShell(),
				ResourceMessages.getString("NewProject.errorMessage"),  //$NON-NLS-1$
				ResourceMessages.format("NewProject.internalError", new Object[] {t.getMessage()})); //$NON-NLS-1$
		}
		return null;
	}

	newProject = newProjectHandle;

	return newProject;
}
/**
 * Creates a project resource given the project handle and description.
 *
 * @param description the project description to create a project resource for
 * @param projectHandle the project handle to create a project resource for
 * @param monitor the progress monitor to show visual progress with
 *
 * @exception CoreException if the operation fails
 * @exception OperationCanceledException if the operation is canceled
 */
private void createProject(IProjectDescription description, IProject projectHandle, IProgressMonitor monitor) throws CoreException, OperationCanceledException {
	try {
		monitor.beginTask("",2000);//$NON-NLS-1$

		projectHandle.create(description, new SubProgressMonitor(monitor,1000));

		if (monitor.isCanceled())
			throw new OperationCanceledException();

		projectHandle.open(new SubProgressMonitor(monitor,1000));

	} finally {
		monitor.done();
	}
}
/**
 * Returns the newly created project.
 *
 * @return the created project, or <code>null</code>
 *   if project not created
 */
public IProject getNewProject() {
	return newProject;
}
/* (non-Javadoc)
 * Method declared on IWorkbenchWizard.
 */
public void init(IWorkbench workbench, IStructuredSelection currentSelection) {
	super.init(workbench, currentSelection);
	setNeedsProgressMonitor(true);
	setWindowTitle(ResourceMessages.getString("NewProject.windowTitle")); //$NON-NLS-1$
}
/* (non-Javadoc)
 * Method declared on BasicNewResourceWizard.
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
		// Should not happen.  Ignore.
	}
}
/* (non-Javadoc)
 * Opens a new window with a particular perspective and input.
 */
private static void openInNewWindow(IPerspectiveDescriptor desc) {

	// Open the page.
	try {
		PlatformUI.getWorkbench().openWorkbenchWindow(
			desc.getId(),
			ResourcesPlugin.getWorkspace().getRoot());
	} catch (WorkbenchException e) {
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (window != null) {
			ErrorDialog.openError(
				window.getShell(),
				WINDOW_PROBLEMS_TITLE,
				e.getMessage(),
				e.getStatus());
		}
	}
}
/* (non-Javadoc)
 * Method declared on IWizard.
 */
public boolean performFinish() {
	createNewProject();
	
	if (newProject == null)
		return false;

	updatePerspective();
	selectAndReveal(newProject);
	
	return true;
}
/* (non-Javadoc)
 * Replaces the current perspective with the new one.
 */
private static void replaceCurrentPerspective(IPerspectiveDescriptor persp) {

	//Get the active page.
	IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
	if (window == null)
		return;
	IWorkbenchPage page = window.getActivePage();
	if (page == null)
		return;

	// Set the perspective.
	page.setPerspective(persp);
}
/**
 * Stores the configuration element for the wizard.  The config element will be used
 * in <code>performFinish</code> to set the result perspective.
 */
public void setInitializationData(IConfigurationElement cfig, String propertyName, Object data) {
	configElement = cfig;
}
/**
 * Updates the perspective for the active page within the window.
 */
protected void updatePerspective() {
	updatePerspective(configElement);
}
/**
 * Updates the perspective based on the current settings
 * in the Workbench/Perspectives preference page.
 * <p>
 * A new project wizard class will need to implement the
 * <code>IExecutableExtension</code> interface so as to gain
 * access to the wizard's <code>IConfigurationElement</code>.
 * That is the configuration element to pass into this method.
 * </p>
 *
 * @see IWorkbenchPreferenceConstants#OPEN_PERSPECTIVE_WINDOW
 * @see IWorkbenchPreferenceConstants#OPEN_PERSPECTIVE_PAGE
 * @see IWorkbenchPreferenceConstants#OPEN_PERSPECTIVE_REPLACE
 * @see IWorkbenchPreferenceConstants#NO_NEW_PERSPECTIVE
 */
public static void updatePerspective(IConfigurationElement configElement) {
	// Do not change perspective if the configuration element is
	// not specified.
	if (configElement == null)
		return;
		
	AbstractUIPlugin uiPlugin =
		(AbstractUIPlugin) Platform.getPlugin(PlatformUI.PLUGIN_ID);
		
	// Retrieve the new project open perspective preference setting
	String perspSetting =
		uiPlugin.getPreferenceStore().getString(
			IWorkbenchPreferenceConstants.PROJECT_OPEN_NEW_PERSPECTIVE);

	// Return if do not switch perspective setting
	if (perspSetting.equals(IWorkbenchPreferenceConstants.NO_NEW_PERSPECTIVE))
		return;

	// Read the requested perspective id to be opened.
	String finalPerspId = configElement.getAttribute(FINAL_PERSPECTIVE);
	if (finalPerspId == null)
		return;
	
	// Map perspective id to descriptor.
	IPerspectiveRegistry reg = PlatformUI.getWorkbench().getPerspectiveRegistry();
	IPerspectiveDescriptor finalPersp = reg.findPerspectiveWithId(finalPerspId);
	if (finalPersp == null) {
		WorkbenchPlugin.log(
			"Unable to find persective " //$NON-NLS-1$
				+ finalPerspId
				+ " in BasicNewProjectResourceWizard.updatePerspective"); //$NON-NLS-1$
		return;
	}

	// gather the preferred perspectives
	// always consider the final perspective to be preferred
	ArrayList preferredPerspIds = new ArrayList();
	preferredPerspIds.add(finalPerspId);
	String preferred = configElement.getAttribute(PREFERRED_PERSPECTIVES);
	if (preferred != null) {
		StringTokenizer tok = new StringTokenizer(preferred, " \t\n\r\f,"); //$NON-NLS-1$
		while (tok.hasMoreTokens()) {
			preferredPerspIds.add(tok.nextToken());
		}
	}
	
	IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
	if (window != null) {
		IWorkbenchPage page = window.getActivePage();
		if (page != null) {
			IPerspectiveDescriptor currentPersp = page.getPerspective();
			
			// don't switch if the current perspective is a preferred perspective	
			if (currentPersp != null && preferredPerspIds.contains(currentPersp.getId())) {
				return;
			}
		}
		
		// prompt the user to switch
		if (!confirmPerspectiveSwitch(window, finalPersp)) {
			return;
		}
	}

	// open perspective in new window setting
	if (perspSetting.equals(IWorkbenchPreferenceConstants.OPEN_PERSPECTIVE_WINDOW)) {
		openInNewWindow(finalPersp);
		return;
	}

	// replace active perspective	setting
	if (perspSetting.equals(IWorkbenchPreferenceConstants.OPEN_PERSPECTIVE_REPLACE)) {
		replaceCurrentPerspective(finalPersp);
		return;
	}
}

/**
 * Prompts the user for whether to switch perspectives.
 * 
 * @return <code>true</code> if it's OK to switch, <code>false</code> otherwise
 */
private static boolean confirmPerspectiveSwitch(IWorkbenchWindow window, IPerspectiveDescriptor finalPersp) {
	IPreferenceStore store = WorkbenchPlugin.getDefault().getPreferenceStore();
	String pspm = store.getString(IPreferenceConstants.PROJECT_SWITCH_PERSP_MODE);
	if (!IPreferenceConstants.PSPM_PROMPT.equals(pspm)) {
		return true;
	}
	MessageDialogWithToggle dialog = MessageDialogWithToggle.openQuestion(
		window.getShell(),
		ResourceMessages.getString("NewProject.perspSwitchTitle"), //$NON-NLS-1$
		ResourceMessages.format(
			"NewProject.perspSwitchMessage", //$NON-NLS-1$
			new Object[] { finalPersp.getLabel() }),
		null,	// use the default message for the toggle
		false); // toggle is initially unchecked
	int result = dialog.getReturnCode();
	if (result >= 0 && dialog.getToggleState()) {
		if (result == 0) {
			// User chose Yes/Don't ask again, so always switch
			store.setValue(IPreferenceConstants.PROJECT_SWITCH_PERSP_MODE, IPreferenceConstants.PSPM_ALWAYS);
			// leave PROJECT_OPEN_NEW_PERSPECTIVE as is
		}
		else {
			// User chose No/Don't ask again, so never switch
			store.setValue(IPreferenceConstants.PROJECT_SWITCH_PERSP_MODE, IPreferenceConstants.PSPM_NEVER);
			// update PROJECT_OPEN_NEW_PERSPECTIVE to correspond
			AbstractUIPlugin uiPlugin =
				(AbstractUIPlugin) Platform.getPlugin(PlatformUI.PLUGIN_ID);
			uiPlugin.getPreferenceStore().setValue(
				IWorkbenchPreferenceConstants.PROJECT_OPEN_NEW_PERSPECTIVE,
				IWorkbenchPreferenceConstants.NO_NEW_PERSPECTIVE);
		}
	}
	return result == 0;
}
}
