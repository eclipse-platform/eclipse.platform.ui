package org.eclipse.ui.actions;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.*;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.internal.*;
import org.eclipse.ui.internal.misc.Assert;

/**
 * Standard action for full and incremental builds of all projects
 * within the workspace.
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 */
public class GlobalBuildAction extends Action {
	
	/**
	 * The type of build performed by this action. Can be either
	 * <code>IncrementalProjectBuilder.INCREMENTAL_BUILD</code> or
	 * <code>IncrementalProjectBuilder.FULL_BUILD</code>.
	 */
	private int buildType;
	
	/**
	 * The workbench this action applies to.
	 */
	private IWorkbench workbench;
	
	/**
	 * The window this action appears in.
	 */
	private IWorkbenchWindow window;
	
	/**
	 * The shell used to display message dialogs to the user
	 */
	private Shell shell;
	
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
	Assert.isNotNull(workbench);
	Assert.isNotNull(shell);
	this.workbench = workbench;
	this.shell = shell;
	setBuildType(type);
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
	Assert.isNotNull(window);
	this.workbench = window.getWorkbench();
	this.window = window;
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
			setText(WorkbenchMessages.getString("GlobalBuildAction.text")); //$NON-NLS-1$
			setToolTipText(WorkbenchMessages.getString("GlobalBuildAction.toolTip")); //$NON-NLS-1$
			setId(IWorkbenchActionConstants.BUILD);
			WorkbenchHelp.setHelp(this, IHelpContextIds.GLOBAL_INCREMENTAL_BUILD_ACTION);
			break;
		case IncrementalProjectBuilder.FULL_BUILD:
			setText(WorkbenchMessages.getString("GlobalBuildAction.rebuildText")); //$NON-NLS-1$
			setToolTipText(WorkbenchMessages.getString("GlobalBuildAction.rebuildToolTip")); //$NON-NLS-1$
			setId(IWorkbenchActionConstants.REBUILD_ALL);
			WorkbenchHelp.setHelp(this, IHelpContextIds.GLOBAL_FULL_BUILD_ACTION);
			break;
		default:
			Assert.isTrue(false, "Invalid build type"); //$NON-NLS-1$
			break;
	}
	this.buildType = type;
}

/**
 * Returns the shell to use.  Uses the window's shell if a window
 * has been set, otherwise it uses the shell passed to the deprecated constructor.
 */
private Shell getShell() {
	if (window != null) {
		return window.getShell();
	}
	else {
		return shell;
	}
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
		WorkbenchMessages.getString("GlobalBuildAction.buildProblems"), //$NON-NLS-1$
		null);

	IRunnableWithProgress op = new IRunnableWithProgress() {
		public void run(IProgressMonitor monitor) {
			try {
				ResourcesPlugin.getWorkspace().build(buildType, monitor);
			}
			catch (CoreException e) {
				status.add(e.getStatus());
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
		WorkbenchPlugin.log("Exception in " + getClass().getName() + ".run: " + e.getTargetException());//$NON-NLS-2$//$NON-NLS-1$
		MessageDialog.openError(
			shell, 
			WorkbenchMessages.getString("GlobalBuildAction.buildProblems"), //$NON-NLS-1$
			WorkbenchMessages.format(
				"GlobalBuildAction.internalError", //$NON-NLS-1$
				new Object[] {e.getTargetException().getMessage()}));
		return;
	}

	// If errors occurred, open an error dialog
	if (!status.isOK()) {
		ErrorDialog.openError(
			shell,
			WorkbenchMessages.getString("GlobalBuildAction.problemTitle"), //$NON-NLS-1$
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
		
	IWorkbenchWindow[] windows = this.workbench.getWorkbenchWindows();
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
		WorkbenchPlugin.log("Exception in " + getClass().getName() + ".run: " + e);//$NON-NLS-2$//$NON-NLS-1$
		ErrorDialog.openError(
			getShell(),
			WorkbenchMessages.getString("GlobalBuildAction.buildProblems"), //$NON-NLS-1$
			WorkbenchMessages.format("GlobalBuildAction.internalError", new Object[] {e.getMessage()}), //$NON-NLS-1$
			e.getStatus());
		return false;
	}
	
	return false;
}
}
