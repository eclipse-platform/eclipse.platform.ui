package org.eclipse.ui.actions;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.internal.IHelpContextIds;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.IWorkbenchPreferenceConstants;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.*;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Shell;
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
	public static final String ID_BUILD = PlatformUI.PLUGIN_ID + ".BuildAction";
	
	/**
	 * The id of a rebuild all action.
	 */
	public static final String ID_REBUILD_ALL = PlatformUI.PLUGIN_ID + ".RebuildAllAction";

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
	super(shell, "");

	if (type == IncrementalProjectBuilder.INCREMENTAL_BUILD) {
		setText("&Build");
		setToolTipText("Incremental build of modified selected resources");
		setId(ID_BUILD);
		WorkbenchHelp.setHelp(this, new Object[] {IHelpContextIds.INCREMENTAL_BUILD_ACTION});
	}
	else {
		setText("Rebuild &All");
		setToolTipText("Full build of all selected resources");
		setId(ID_REBUILD_ALL);
		WorkbenchHelp.setHelp(this, new Object[] {IHelpContextIds.FULL_BUILD_ACTION});
	}
		
	this.buildType = type;
}
/* (non-Javadoc)
 * Method declared on WorkspaceAction.
 */
String getOperationMessage() {
	return "Building:";
}
/* (non-Javadoc)
 * Method declared on WorkspaceAction.
 */
String getProblemsMessage() {
	return "Problems occurred building the selected resources.";
}
/* (non-Javadoc)
 * Method declared on WorkspaceAction.
 */
String getProblemsTitle() {
	return "Build Problems";
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
	return store.getBoolean(IWorkbenchPreferenceConstants.SAVE_ALL_BEFORE_BUILD);
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
	return false;
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
		WorkbenchPlugin.log("Exception in " + getClass().getName() + ".run: " + e);
		MessageDialog.openError(
			getShell(),
			"Build problems",
			"Internal error: " + e.getMessage());
		return false;
	}
	
	MessageDialog.openWarning(
		getShell(),
		"Warning",
		"None of the selected projects have registered builders."
	);
	
	return false;
}
}
