package org.eclipse.ui.wizards.newresource;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.ui.*;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.dialogs.WizardNewProjectCreationPage;
import org.eclipse.ui.dialogs.WizardNewProjectReferencePage;
import org.eclipse.ui.internal.IWorkbenchPreferenceConstants;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Display;
import java.lang.reflect.InvocationTargetException;
import java.net.*;


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
 * IWizard wizard = new BasicNewProjectResourceWizard();
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

	private static String PAGE_PROBLEMS_TITLE = "Problems Opening Page";
	private static String WINDOW_PROBLEMS_TITLE = "Problems Opening Window";
/**
 * Creates a wizard for creating a new project resource in the workspace.
 */
public BasicNewProjectResourceWizard() {
	AbstractUIPlugin plugin = (AbstractUIPlugin) Platform.getPlugin(PlatformUI.PLUGIN_ID);
	IDialogSettings workbenchSettings = plugin.getDialogSettings();
	IDialogSettings section = workbenchSettings.getSection("BasicNewProjectResourceWizard");
	if(section == null)
		section = workbenchSettings.addNewSection("BasicNewProjectResourceWizard");
	setDialogSettings(section);
}
/* (non-Javadoc)
 * Method declared on IWizard.
 */
public void addPages() {
	super.addPages();
	
	mainPage = new WizardNewProjectCreationPage("basicNewProjectPage");
	mainPage.setTitle("Project");
	mainPage.setDescription("Create a new project resource.");
	this.addPage(mainPage);
	
	// only add page if there are already projects in the workspace
	if (ResourcesPlugin.getWorkspace().getRoot().getProjects().length > 0) {
		referencePage = new WizardNewProjectReferencePage("basicReferenceProjectPage");
		referencePage.setTitle("Project");
		referencePage.setDescription("Select referenced projects.");
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
	IPath defaultPath = Platform.getLocation();
	IPath newPath = mainPage.getLocationPath();
	if (defaultPath.equals(newPath))
		newPath = null;
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
			ErrorDialog.openError(
				getShell(), 
				"Creation Problems", 
				null, // no special message
			 	((CoreException) t).getStatus());
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
				"Creation problems",
				"Internal error: " + t.getMessage());
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
		monitor.beginTask("",2000);

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
	setWindowTitle("New Project");
}
/* (non-Javadoc)
 * Method declared on BasicNewResourceWizard.
 */
protected void initializeDefaultPageImageDescriptor() {
	String iconPath;
	if(Display.getCurrent().getIconDepth() > 4)
		iconPath = "icons/full/";
	else
		iconPath = "icons/basic/";
		
	try {
		URL installURL = Platform.getPlugin(PlatformUI.PLUGIN_ID).getDescriptor().getInstallURL();
		URL url = new URL(installURL, iconPath + "wizban/newprj_wiz.gif");
		ImageDescriptor desc = ImageDescriptor.createFromURL(url);
		setDefaultPageImageDescriptor(desc);
	}
	catch (MalformedURLException e) {
		// Should not happen.  Ignore.
	}
}
/* (non-Javadoc)
 * Opens a new page with a particular perspective and input.
 */
private void openInNewPage(IPerspectiveDescriptor desc) {

	IWorkbenchWindow window = getWorkbench().getActiveWorkbenchWindow();
	if (window == null)
		return;

	IWorkbenchPage page = window.getActivePage();
	if (page != null) {
		//Do not open the perspective if it is already there
		if (page.getPerspective().getId().equals(desc.getId()))
			return;
	}

	// Open the page.
	try {
		window.openPage(desc.getId(), newProject.getWorkspace().getRoot());
	} catch (WorkbenchException e) {
		MessageDialog.openError(window.getShell(), PAGE_PROBLEMS_TITLE, e.getMessage());
	}
}
/* (non-Javadoc)
 * Opens a new window with a particular perspective and input.
 */
private void openInNewWindow(IPerspectiveDescriptor desc) {
	IWorkbenchWindow window = getWorkbench().getActiveWorkbenchWindow();

	// Open the page.
	try {
		window.getWorkbench().openWorkbenchWindow(
			desc.getId(),
			newProject.getWorkspace().getRoot());
	} catch (WorkbenchException e) {
		MessageDialog.openError(
			window.getShell(),
			WINDOW_PROBLEMS_TITLE,
			e.getMessage());
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
/**
 * Replace the current perspective with the new one.
 */
private void replaceCurrentPerspective(IPerspectiveDescriptor persp) {

	//Get the active page.
	IWorkbenchWindow dw = getWorkbench().getActiveWorkbenchWindow();
	if (dw == null)
		return;
	IWorkbenchPage page = dw.getActivePage();
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

	AbstractUIPlugin plugin =
		(AbstractUIPlugin) Platform.getPlugin(PlatformUI.PLUGIN_ID);
	String perspectiveSetting =
		plugin.getPreferenceStore().getString(
			IWorkbenchPreferenceConstants.PROJECT_OPEN_NEW_PERSPECTIVE);

	//Do not switch perspectives if that is the setting
	if (perspectiveSetting.equals(IWorkbenchPreferenceConstants.NO_NEW_PERSPECTIVE))
		return;

	// Read final persp from config.
	String perspID = configElement.getAttribute("finalPerspective");
	if (perspID == null)
		return;
	// Map persp id to descriptor.
	IPerspectiveRegistry reg = PlatformUI.getWorkbench().getPerspectiveRegistry();
	IPerspectiveDescriptor persp = reg.findPerspectiveWithId(perspID);
	if (persp == null)
		return;

	if (perspectiveSetting
		.equals(IWorkbenchPreferenceConstants.OPEN_PERSPECTIVE_WINDOW))
		openInNewWindow(persp);
	if (perspectiveSetting
		.equals(IWorkbenchPreferenceConstants.OPEN_PERSPECTIVE_PAGE))
		openInNewPage(persp);
	if (perspectiveSetting
		.equals(IWorkbenchPreferenceConstants.OPEN_PERSPECTIVE_REPLACE))
		replaceCurrentPerspective(persp);

}
}
