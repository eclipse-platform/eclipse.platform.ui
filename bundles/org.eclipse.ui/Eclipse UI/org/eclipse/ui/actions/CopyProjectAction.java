package org.eclipse.ui.actions;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ProjectLocationSelectionDialog;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.IHelpContextIds;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.util.Assert;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

/**
 * The CopyProjectAction is the action designed to copy projects specifically
 * as they have different semantics from other resources.
 * Note that this action assumes that a single project is selected and being
 * manipulated. This should be disabled for multi select or no selection.
 */
public class CopyProjectAction extends SelectionListenerAction {
	private static String COPY_TOOL_TIP = "Copy the project";
	private static String COPY_TITLE = "&Copy";
	private static String COPY_PROGRESS_TITLE = "Copying:";
	private static String COPY_PREFIX = "Copy ";
	private static String OPEN_BRACKET = "(";
	private static String CLOSE_BRACKET = ")";
	private static String EXISTING_NAME_PREFIX = "of ";
	private static String COPY_PROJECT_FAILED_MESSAGE =
		"Problems occurred copying the project.";
	private static String PROBLEMS_TITLE = "Copy Problems";

	/**
	 * The id of this action.
	 */
	public static final String ID = PlatformUI.PLUGIN_ID + ".CopyProjectAction";

	/**
	 * The shell in which to show any dialogs.
	 */
	protected Shell shell;

	/**
	 * Status containing the errors detected when running the operation or
	 * <code>null</code> if no errors detected.
	 */
	protected IStatus errorStatus;
/**
 * Creates a new project copy action with the default text.
 *
 * @param shell the shell for any dialogs
 * @param text the string used as the text for the action, 
 *   or <code>null</code> if these is no text
 */
public CopyProjectAction(Shell shell) {
	this(shell,COPY_TITLE);
	WorkbenchHelp.setHelp(this, new Object[] {IHelpContextIds.COPY_PROJECT_ACTION});
}
/**
 * Creates a new project copy action with the given text.
 *
 * @param shell the shell for any dialogs
 * @param text the string used as the text for the action, 
 *   or <code>null</code> if these is no text
 */
CopyProjectAction(Shell shell, String name) {
	super(name);
	setToolTipText(COPY_TOOL_TIP);
	setId(CopyProjectAction.ID);
	Assert.isNotNull(shell);
	this.shell = shell;
}
/**
 * Create a new IProjectDescription for the copy using the name and path selected
 * from the dialog.
 * @return IProjectDescription
 * @param project the source project
 * @param projectName the name for the new project
 * @param rootLocation the path the new project will be stored under.
 */
protected IProjectDescription createDescription(
	IProject project,
	String projectName,
	IPath rootLocation)
	throws CoreException {
	//Get a copy of the current description and modify it
	IProjectDescription newDescription = project.getDescription();
	newDescription.setName(projectName);

	//If the location is the default then set the location to null
	if(rootLocation.equals(Platform.getLocation()))
		newDescription.setLocation(null);
	else
		newDescription.setLocation(rootLocation);
		
	return newDescription;
}
/**
 * Opens an error dialog to display the given message.
 * <p>
 * Note that this method must be called from UI thread.
 * </p>
 *
 * @param message the message
 */
void displayError(String message) {
	MessageDialog.openError(this.shell,getErrorsTitle(), message);
}
/**
 * Generate a new name for the project that does not have any collisions.
 * @return String
 * @param projectName String
 * @param workspace IWorkspace
 */
private String getCopyNameFor(String projectName, IWorkspace workspace) {

	IWorkspaceRoot root = workspace.getRoot();
	if (root.getProject(projectName) != null)
		return projectName;

	int counter = 1;
	while (true) {
		String nameSegment = COPY_PREFIX;

		if (counter > 1)
			nameSegment += OPEN_BRACKET + counter + CLOSE_BRACKET;

		nameSegment += EXISTING_NAME_PREFIX + projectName;

		if (workspace.getRoot().getProject(nameSegment) == null)
			return nameSegment;

		counter++;
	}

}
/**
 * Return the title of the errors dialog.
 * @return java.lang.String
 */
protected String getErrorsTitle() {
	return PROBLEMS_TITLE;
}
/**
 * Get the plugin used by a copy action
 * @return AbstractUIPlugin
 */
protected org.eclipse.ui.plugin.AbstractUIPlugin getPlugin() {
	return (AbstractUIPlugin) Platform.getPlugin(PlatformUI.PLUGIN_ID);
}
/**
 * Copies the project to the new values.
 *
 * @param project the project to copy
 * @param projectName the name of the copy
 * @param newLocation IPath
 * @return <code>true</code> if the copy operation completed, and 
 *   <code>false</code> if it was abandoned part way
 */
boolean performCopy(
	final IProject project,
	final String projectName,
	final IPath newLocation) {
	WorkspaceModifyOperation op = new WorkspaceModifyOperation() {
		public void execute(IProgressMonitor monitor) {

			monitor.beginTask(COPY_PROGRESS_TITLE, 100);
			try {
				if (monitor.isCanceled())
					throw new OperationCanceledException();

				//Get a copy of the current description and modify it
				IProjectDescription newDescription =
					createDescription(project, projectName, newLocation);
				monitor.worked(50);

				project.copy(newDescription, true, monitor);

				monitor.worked(50);

			} catch (CoreException e) {
				recordError(e); // log error
			} finally {
				monitor.done();
			}
		}
	};

	try {
		new ProgressMonitorDialog(shell).run(true, true, op);
	} catch (InterruptedException e) {
		return false;
	} catch (InvocationTargetException e) {
		displayError("Internal error: " + e.getTargetException().getMessage());
		return false;
	}

	return true;
}
/**
 * Query for a new project name and destination using the parameters in the existing
 * project.
 * @return Object []  or null if the selection is cancelled
 * @param IProject - the project we are going to copy.
 */
protected Object [] queryDestinationParameters(IProject project) {
	ProjectLocationSelectionDialog dialog =
		new ProjectLocationSelectionDialog(shell, project);
	dialog.open();
	return dialog.getResult();
}
/**
 * Records the core exception to be displayed to the user
 * once the action is finished.
 *
 * @param error a <code>CoreException</code>
 */
final void recordError(CoreException error) {
	this.errorStatus = error.getStatus();
}
/**
 * Implementation of method defined on <code>IAction</code>.
 */
public void run() {

	IProject project = (IProject) getSelectedResources().get(0);

	//Get the project name and location in a two element list
	Object[] destinationPaths = queryDestinationParameters(project);
	if (destinationPaths == null)
		return;

	String projectName = (String) destinationPaths[0];
	IPath newLocation = new Path((String) destinationPaths[1]);

	String copyName =
		getCopyNameFor(projectName, WorkbenchPlugin.getPluginWorkspace());

	boolean completed = performCopy(project, copyName, newLocation);

	if (!completed) // ie.- canceled
		return; // not appropriate to show errors

	// If errors occurred, open an Error dialog
	if (errorStatus != null) {
		ErrorDialog.openError(this.shell, getErrorsTitle(), null, errorStatus);
	}
}
/**
 * The <code>CopyResourceAction</code> implementation of this
 * <code>SelectionListenerAction</code> method enables this action only if 
 * there is a single selection which is a project.
 */
protected boolean updateSelection(IStructuredSelection selection) {
	if (!super.updateSelection(selection)) {
		return false;
	}
	if (getSelectedNonResources().size() > 0) {
		return false;
	}

	// to enable this command there must be one project selected and nothing else
	List selectedResources = getSelectedResources();
	if (selectedResources.size() != 1)
		return false;
	if ((selectedResources.get(0)) instanceof IProject)
		return true;
	return false;
}
}
