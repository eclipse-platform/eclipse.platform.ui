package org.eclipse.ui.actions;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.internal.*;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.*;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Shell;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Standard actions for full and incremental builds of the selected project(s).
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 */
public class BuildAction extends WorkspaceAction {
	
	/**
	 * The id of an incremental build action.
	 */
	public static final String ID_BUILD = PlatformUI.PLUGIN_ID + ".BuildAction";//$NON-NLS-1$
	
	/**
	 * The id of a rebuild all action.
	 */
	public static final String ID_REBUILD_ALL = PlatformUI.PLUGIN_ID + ".RebuildAllAction";//$NON-NLS-1$

	private int	buildType;
/**
 * Creates a new action of the appropriate type. The action id is 
 * <code>ID_BUILD</code> for incremental builds and <code>ID_REBUILD_ALL</code>
 * for full builds.
 *
 * @param shell the shell for any dialogs
 * @param type the type of build; one of
 *  <code>BaseBuilder.INCREMENTAL_BUILD</code> or 
 *  <code>BaseBuilder.FULL_BUILD</code>
 */
public BuildAction(Shell shell, int type) {
	super(shell, "");//$NON-NLS-1$

	if (type == IncrementalProjectBuilder.INCREMENTAL_BUILD) {
		setText(WorkbenchMessages.getString("BuildAction.text")); //$NON-NLS-1$
		setToolTipText(WorkbenchMessages.getString("BuildAction.toolTip")); //$NON-NLS-1$
		setId(ID_BUILD);
		WorkbenchHelp.setHelp(this, new Object[] {IHelpContextIds.INCREMENTAL_BUILD_ACTION});
	}
	else {
		setText(WorkbenchMessages.getString("RebuildAction.text")); //$NON-NLS-1$
		setToolTipText(WorkbenchMessages.getString("RebuildAction.tooltip")); //$NON-NLS-1$
		setId(ID_REBUILD_ALL);
		WorkbenchHelp.setHelp(this, new Object[] {IHelpContextIds.FULL_BUILD_ACTION});
	}
		
	this.buildType = type;
}
/* (non-Javadoc)
 * Method declared on WorkspaceAction.
 */
String getOperationMessage() {
	return WorkbenchMessages.getString("BuildAction.operationMessage"); //$NON-NLS-1$
}
/* (non-Javadoc)
 * Method declared on WorkspaceAction.
 */
String getProblemsMessage() {
	return WorkbenchMessages.getString("BuildAction.problemMessage"); //$NON-NLS-1$
}
/* (non-Javadoc)
 * Method declared on WorkspaceAction.
 */
String getProblemsTitle() {
	return WorkbenchMessages.getString("BuildAction.problemTitle"); //$NON-NLS-1$
}
/* (non-Javadoc)
 * Method declared on WorkspaceAction.
 */
void invokeOperation(IResource resource, IProgressMonitor monitor) throws CoreException {
	((IProject)resource).build(buildType,monitor);
}
/**
 * Returns whether the user's preference is set to automatically save modified
 * resources before a manual build is done.
 *
 * @return <code>true</code> if Save All Before Build is enabled
 */
public static boolean isSaveAllSet() {
	IPreferenceStore store = WorkbenchPlugin.getDefault().getPreferenceStore();
	return store.getBoolean(IPreferenceConstants.SAVE_ALL_BEFORE_BUILD);
}
/* (non-Javadoc)
 * Method declared on WorkspaceAction.
 *
 * Change the order of the resources so that
 * it matches the build order. Closed and
 * non existant projects are eliminated. Also,
 * any projects in cycles are eliminated.
 */
List pruneResources(List resourceCollection) {
	// Optimize...
	if (resourceCollection.size() < 2)
		return resourceCollection;

	// Try the workspace's description build order if specified
	String[] orderedNames = ResourcesPlugin.getWorkspace().getDescription().getBuildOrder();
	if (orderedNames != null) {
		List orderedProjects = new ArrayList(resourceCollection.size());
		for (int i = 0; i < orderedNames.length; i++) {
			String projectName = orderedNames[i];
			for (int j = 0; j < resourceCollection.size(); j++) {
				IProject project = (IProject) resourceCollection.get(j);
				if (project.getName().equals(projectName)) {
					orderedProjects.add(project);
					break;
				}
			}
		}
		return orderedProjects;
	}

	// Try the project prerequisite order then
	IProject[] projects = new IProject[resourceCollection.size()];
	projects = (IProject[]) resourceCollection.toArray(projects);
	IProject[][] prereqs = ResourcesPlugin.getWorkspace().computePrerequisiteOrder(projects);
	List ordered = Arrays.asList(prereqs[0]);
	ordered.addAll(Arrays.asList(prereqs[1]));
	return ordered;
}
/* (non-Javadoc)
 * Method declared on IAction; overrides method on WorkspaceAction.
 * This override allows the user to save the contents of selected
 * open editors so that the updated contents will be used for building.
 */
public void run() {
	// Verify that there are builders registered on at
	// least one project
	if (!verifyBuildersAvailable())
		return;

	// Save all resources prior to doing build
	saveAllResources();

	super.run();
}
/**
 * Causes all editors to save any modified resources depending on the user's
 * preference.
 */
void saveAllResources() {
	List projects = getSelectedResources();
	if (projects == null || projects.isEmpty())
		return;
		
	if (!isSaveAllSet())
		return;
		
	IWorkbenchWindow[] windows = PlatformUI.getWorkbench().getWorkbenchWindows();
	for (int i = 0; i < windows.length; i++) {
		IWorkbenchPage [] pages = windows[i].getPages();
		for (int j = 0; j < pages.length; j++) {
			IWorkbenchPage page = pages[j];
			IEditorPart[] editors = page.getEditors();
			for (int k = 0; k < editors.length; k++) {
				IEditorPart editor = editors[k];
				if (editor.isDirty()) {
					IEditorInput input = editor.getEditorInput();
					if (input instanceof IFileEditorInput) {
						IFile inputFile = ((IFileEditorInput)input).getFile();
						if (projects.contains(inputFile.getProject())) {
							page.saveEditor(editor, false);
						}
					}
				}
			}
		}
	}
}
/* (non-Javadoc)
 * Method declared on WorkspaceAction.
 */
boolean shouldPerformResourcePruning() {
	return true;
}
/**
 * The <code>BuildAction</code> implementation of this
 * <code>SelectionListenerAction</code> method ensures that this action is
 * enabled only if all of the selected resources are projects.
 */
protected boolean updateSelection(IStructuredSelection s) {
	return super.updateSelection(s) && selectionIsOfType(IProject.PROJECT);
}
/**
 * Returns whether there are builders registered on at least one selected
 * project.
 *
 * @return <code>true</code> if there is something that could be built, and 
 *   <code>false</code> if there is nothing buildable selected
 */
boolean verifyBuildersAvailable() {
	List projects = getSelectedResources();
	if (projects == null || projects.isEmpty())
		return false;
	
	try {
		for (int i = 0; i < projects.size(); i++) {
			IProject project = (IProject) projects.get(i);
			ICommand[] commands = project.getDescription().getBuildSpec();
			if (commands.length > 0)
				return true;
		}
	}
	catch (CoreException e) {
		WorkbenchPlugin.log(WorkbenchMessages.format("BuildAction.verifyExceptionMessage", new Object[] {getClass().getName(), e}));//$NON-NLS-1$
		MessageDialog.openError(
			getShell(),
			WorkbenchMessages.getString("BuildAction.buildProblems"), //$NON-NLS-1$
			WorkbenchMessages.format("BuildAction.internalError", new Object[] {e.getMessage()})); //$NON-NLS-1$
		return false;
	}
	
	MessageDialog.openWarning(
		getShell(),
		WorkbenchMessages.getString("BuildAction.warning"), //$NON-NLS-1$
		WorkbenchMessages.getString("BuildAction.noBuilders") //$NON-NLS-1$
	);
	
	return false;
}
}
