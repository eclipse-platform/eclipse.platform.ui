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
package org.eclipse.ui.actions;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.internal.ide.IDEInternalWorkbenchImages;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.internal.ide.IHelpContextIds;

/**
 * Standard action for full and incremental builds of all projects
 * within the workspace.
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 */
public class GlobalBuildAction
		extends Action 
		implements ActionFactory.IWorkbenchAction {

	/**
	 * The type of build performed by this action. Can be either
	 * <code>IncrementalProjectBuilder.INCREMENTAL_BUILD</code> or
	 * <code>IncrementalProjectBuilder.FULL_BUILD</code>.
	 */
	private int buildType;
		
	/**
	 * The workbench window; or <code>null</code> if this
	 * action has been <code>dispose</code>d.
	 */
	private IWorkbenchWindow workbenchWindow;
			
/**
 * Creates a new action of the appropriate type. The action id is 
 * <code>IWorkbenchActionConstants.BUILD</code> for incremental builds
 * and <code>IWorkbenchActionConstants.REBUILD_ALL</code> for full builds.
 *
 * @param workbench the active workbench
 * @param shell the shell for any dialogs
 * @param type the type of build; one of
 *  <code>IncrementalProjectBuilder.INCREMENTAL_BUILD</code> or 
 *  <code>IncrementalProjectBuilder.FULL_BUILD</code>
 * 
 * @deprecated use GlobalBuildAction(IWorkbenchWindow, type) instead
 */
public GlobalBuildAction(IWorkbench workbench, Shell shell, int type) {
	// always use active window; ignore shell
	this(workbench.getActiveWorkbenchWindow(), type);
	if (shell == null) {
		throw new IllegalArgumentException();
	}
}

/**
 * Creates a new action of the appropriate type. The action id is 
 * <code>IWorkbenchActionConstants.BUILD</code> for incremental builds
 * and <code>IWorkbenchActionConstants.REBUILD_ALL</code> for full builds.
 *
 * @param window the window in which this action appears
 * @param type the type of build; one of
 *  <code>IncrementalProjectBuilder.INCREMENTAL_BUILD</code> or 
 *  <code>IncrementalProjectBuilder.FULL_BUILD</code>
 */
public GlobalBuildAction(IWorkbenchWindow window, int type) {
	if (window == null) {
		throw new IllegalArgumentException();
	}
	this.workbenchWindow = window;
	setBuildType(type);
}

/**
 * Sets the build type.
 * 
 * @param type the type of build; one of
 *  <code>IncrementalProjectBuilder.INCREMENTAL_BUILD</code> or 
 *  <code>IncrementalProjectBuilder.FULL_BUILD</code>
 */
private void setBuildType(int type) {
	// allow AUTO_BUILD as well for backwards compatibility, but treat it the same as INCREMENTAL_BUILD
	switch (type) {
		case IncrementalProjectBuilder.INCREMENTAL_BUILD:
		case IncrementalProjectBuilder.AUTO_BUILD:
			setText(IDEWorkbenchMessages.getString("GlobalBuildAction.text")); //$NON-NLS-1$
			setToolTipText(IDEWorkbenchMessages.getString("GlobalBuildAction.toolTip")); //$NON-NLS-1$
			setId(IWorkbenchActionConstants.BUILD);
			WorkbenchHelp.setHelp(this, IHelpContextIds.GLOBAL_INCREMENTAL_BUILD_ACTION);
			setImageDescriptor(
				IDEInternalWorkbenchImages.getImageDescriptor(
					IDEInternalWorkbenchImages.IMG_CTOOL_BUILD_EXEC));
			setHoverImageDescriptor(
				IDEInternalWorkbenchImages.getImageDescriptor(
					IDEInternalWorkbenchImages.IMG_CTOOL_BUILD_EXEC_HOVER));
			setDisabledImageDescriptor(
				IDEInternalWorkbenchImages.getImageDescriptor(
					IDEInternalWorkbenchImages.IMG_CTOOL_BUILD_EXEC_DISABLED));
			setActionDefinitionId("org.eclipse.ui.project.buildAll"); //$NON-NLS-1$
			break;
		case IncrementalProjectBuilder.FULL_BUILD:
			setText(IDEWorkbenchMessages.getString("GlobalBuildAction.rebuildText")); //$NON-NLS-1$
			setToolTipText(IDEWorkbenchMessages.getString("GlobalBuildAction.rebuildToolTip")); //$NON-NLS-1$
			setId(IWorkbenchActionConstants.REBUILD_ALL);
			WorkbenchHelp.setHelp(this, IHelpContextIds.GLOBAL_FULL_BUILD_ACTION);
			setActionDefinitionId("org.eclipse.ui.project.rebuildAll"); //$NON-NLS-1$
			break;
		default:
			throw new IllegalArgumentException("Invalid build type"); //$NON-NLS-1$
	}
	this.buildType = type;
}

/**
 * Returns the shell to use.
 */
private Shell getShell() {
	return workbenchWindow.getShell();
}
/**
 * Returns the operation name to use
 */
private String getOperationMessage() {
	if (buildType == IncrementalProjectBuilder.INCREMENTAL_BUILD)
		return IDEWorkbenchMessages.getString("GlobalBuildAction.buildOperationTitle"); //$NON-NLS-1$
	else
		return IDEWorkbenchMessages.getString("GlobalBuildAction.rebuildAllOperationTitle"); //$NON-NLS-1$
}
/**
 * Builds all projects within the workspace. Does
 * not save any open editors.
 */
public void doBuild() {
	doBuildOperation();
}
/**
 * Invokes a build on all projects within the workspace.
 * Reports any errors with the build to the user.
 */
/* package */ void doBuildOperation() {
	
	final MultiStatus status = new MultiStatus(
		PlatformUI.PLUGIN_ID,
		0,
		IDEWorkbenchMessages.getString("GlobalBuildAction.buildProblems"), //$NON-NLS-1$
		null);

	IRunnableWithProgress op = new IRunnableWithProgress() {
		public void run(IProgressMonitor monitor) {
			monitor.beginTask("", 1); //$NON-NLS-1$
			// Fix for bug 31768 - Don't provide a task name in beginTask
			// as it will be appended to each subTask message. Need to
			// call setTaskName as its the only was to assure the task name is
			// set in the monitor (see bug 31824)
			monitor.setTaskName(getOperationMessage());
			try {
				ResourcesPlugin.getWorkspace().build(buildType, new SubProgressMonitor(monitor,1));
			}
			catch (CoreException e) {
				status.add(e.getStatus());
			}
			finally {
				monitor.done();
			}
		}
	};
	
	try {
		new ProgressMonitorDialog(getShell()).run(true, true, op);
	}
	catch (InterruptedException e) {
		// do nothing
	}
	catch (InvocationTargetException e) {
		// Unexpected runtime exceptions
		IDEWorkbenchPlugin.log("Exception in " + getClass().getName() + ".run: " + e.getTargetException());//$NON-NLS-2$//$NON-NLS-1$
		MessageDialog.openError(
			getShell(), 
			IDEWorkbenchMessages.getString("GlobalBuildAction.buildProblems"), //$NON-NLS-1$
			IDEWorkbenchMessages.format(
				"GlobalBuildAction.internalError", //$NON-NLS-1$
				new Object[] {e.getTargetException().getMessage()}));
		return;
	}

	// If errors occurred, open an error dialog
	if (!status.isOK()) {
		ErrorDialog.openError(
			getShell(),
			IDEWorkbenchMessages.getString("GlobalBuildAction.problemTitle"), //$NON-NLS-1$
			null, // no special message
			status);
	}
}
/**
 * Returns an array of all projects in the workspace
 */
/* package */ IProject[] getWorkspaceProjects() {
	return ResourcesPlugin.getWorkspace().getRoot().getProjects();
}
/* (non-Javadoc)
 * Method declared on IAction.
 * 
 * Builds all projects within the workspace. Saves
 * all editors prior to build depending on user's
 * preference.
 */
public void run() {
	if (workbenchWindow == null) {
		// action has been disposed
		return;
	}
	// Do nothing if there are no projects...
	IProject[] roots = getWorkspaceProjects();
	if (roots.length < 1)
		return;

	// Verify that there are builders registered on at
	// least one project
	if (!verifyBuildersAvailable(roots))
		return;
		
	// Save all resources prior to doing build
	saveAllResources();

	// Perform the build on all the projects
	doBuildOperation();
}
/**
 * Causes all editors to save any modified resources
 * depending on the user's preference.
 */
/* package */ void saveAllResources() {
	if (!BuildAction.isSaveAllSet())
		return;
		
	IWorkbenchWindow[] windows = PlatformUI.getWorkbench().getWorkbenchWindows();
	for (int i = 0; i < windows.length; i++) {
		IWorkbenchPage[] perspectives = windows[i].getPages();
		for (int j = 0; j < perspectives.length; j++)
			perspectives[j].saveAllEditors(false);
	}
}
/**
 * Checks that there is at least one project with a
 * builder registered on it.
 */
/* package */ boolean verifyBuildersAvailable(IProject[] roots) {
	try {
		for (int i = 0; i < roots.length; i++) {
			if (roots[i].isAccessible())
				if (roots[i].getDescription().getBuildSpec().length > 0)
					return true;
		}
	}
	catch (CoreException e) {
		IDEWorkbenchPlugin.log("Exception in " + getClass().getName() + ".run: " + e);//$NON-NLS-2$//$NON-NLS-1$
		ErrorDialog.openError(
			getShell(),
			IDEWorkbenchMessages.getString("GlobalBuildAction.buildProblems"), //$NON-NLS-1$
			IDEWorkbenchMessages.format("GlobalBuildAction.internalError", new Object[] {e.getMessage()}), //$NON-NLS-1$
			e.getStatus());
		return false;
	}
	
	return false;
}

/* (non-Javadoc)
 * Method declared on ActionFactory.IWorkbenchAction.
 * @since 3.0
 */
public void dispose() {
	if (workbenchWindow == null) {
		// action has already been disposed
		return;
	}
	workbenchWindow = null;
}

}
