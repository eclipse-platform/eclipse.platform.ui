package org.eclipse.ui.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.ui.*;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.internal.*;
import org.eclipse.ui.internal.misc.StatusUtil;
import org.eclipse.ui.actions.*;
import org.eclipse.ui.part.*;
import org.eclipse.jface.action.*;
import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.operation.*;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Shell;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * Workbench global action for building all 
 * contained resources. Handles both full and
 * incremental build.
 */
public class GlobalBuildAction extends Action {
	private int buildType;
	private MultiStatus status;
	private IWorkbench workbench;
/**
 * Creates an instance of this class to perform
 * a build on all the resources. Allows the type
 * (full or incremental only) to be specified.
 */
public GlobalBuildAction(IWorkbench aWorkbench, int type) {
	if (type == IncrementalProjectBuilder.INCREMENTAL_BUILD) {
		setText(WorkbenchMessages.getString("GlobalBuildAction.text")); //$NON-NLS-1$
		setToolTipText(WorkbenchMessages.getString("GlobalBuildAction.toolTip")); //$NON-NLS-1$
		setId(IWorkbenchActionConstants.BUILD);
		WorkbenchHelp.setHelp(this, new Object[] {IHelpContextIds.GLOBAL_INCREMENTAL_BUILD_ACTION});
	}
	else {
		setText(WorkbenchMessages.getString("GlobalBuildAction.rebuildText")); //$NON-NLS-1$
		setToolTipText(WorkbenchMessages.getString("GlobalBuildAction.rebuildToolTip")); //$NON-NLS-1$
		setId(IWorkbenchActionConstants.REBUILD_ALL);
		WorkbenchHelp.setHelp(this, new Object[] {IHelpContextIds.GLOBAL_FULL_BUILD_ACTION});
	}
		
	this.workbench = aWorkbench;
	this.buildType = type;
}
/**
 * Builds all specified projects.
 */
protected void build(IProgressMonitor monitor) {
	try {
		ResourcesPlugin.getWorkspace().build(buildType, monitor);
	}
	catch (CoreException e) {
		status.add(e.getStatus());
	}
}
/**
 * Do a global build on all projects within
 * the current workspace depending on the
 * type (full or incremental)
 */
public void doBuild() {
	doBuildOperation();
}
/**
 * Do a global build on all projects within
 * the current workspace depending on the
 * type (full or incremental)
 */
protected void doBuildOperation() {
	// Perform the build on all the projects
	status = new MultiStatus(PlatformUI.PLUGIN_ID, 0, WorkbenchMessages.getString("GlobalBuildAction.buildProblems"), null); //$NON-NLS-1$
	startBuildOperation();
	
	// If errors occurred, open an Error dialog
	if (!status.isOK()) {
		ErrorDialog.openError(
			this.workbench.getActiveWorkbenchWindow().getShell(),
			WorkbenchMessages.getString("GlobalBuildAction.problemTitle"), //$NON-NLS-1$
			null,	// no special message
			status);
	}
}
/**
 * Get all the projects
 */
protected IProject[] getRootProjects() {
	return ResourcesPlugin.getWorkspace().getRoot().getProjects();
}
/**
 * Do a global build on all projects within
 * the current workspace depending on the
 * type (full or incremental)
 */
public void run() {
	// Do nothing if there are no projects...
	IProject[] roots = getRootProjects();
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
 * Cause all editors to save any modified resources
 * depending on the user's preference.
 */
protected void saveAllResources() {
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
 * Start the build on all the projects specified.
 */
protected void startBuildOperation() {
	Shell shell = this.workbench.getActiveWorkbenchWindow().getShell();

	IRunnableWithProgress op = new IRunnableWithProgress() {
		public void run(IProgressMonitor monitor) {
			build(monitor);
		}
	};
	
	try {
		new ProgressMonitorDialog(shell).run(true, true, op);
	}
	catch (InterruptedException e) {
		return;
	}
	catch (InvocationTargetException e) {
		// CoreExceptions are collected in build(), but unexpected runtime exceptions and errors may still occur.
		WorkbenchPlugin.log("Exception in " + getClass().getName() + ".run: " + e.getTargetException());//$NON-NLS-2$//$NON-NLS-1$
		MessageDialog.openError(shell, WorkbenchMessages.getString("GlobalBuildAction.buildProblems"), WorkbenchMessages.format("GlobalBuildAction.internalError", new Object[] {e.getTargetException().getMessage()})); //$NON-NLS-1$ //$NON-NLS-2$
	}
}
/**
 * Verify that there are builders registered on at
 * least one project.
 */
protected boolean verifyBuildersAvailable(IProject[] roots) {
	try {
		for (int i = 0; i < roots.length; i++) {
			if (roots[i].isAccessible())
				if (roots[i].getDescription().getBuildSpec().length > 0)
					return true;
		}
	}
	catch (CoreException e) {
		WorkbenchPlugin.log("Exception in " + getClass().getName() + ".run: " + e);//$NON-NLS-2$//$NON-NLS-1$
		MessageDialog.openError(
			this.workbench.getActiveWorkbenchWindow().getShell(),
			WorkbenchMessages.getString("GlobalBuildAction.buildProblems"), //$NON-NLS-1$
			WorkbenchMessages.format("GlobalBuildAction.internalError", new Object[] {e.getMessage()})); //$NON-NLS-1$
		return false;
	}
	
	MessageDialog.openWarning(
		this.workbench.getActiveWorkbenchWindow().getShell(),
		WorkbenchMessages.getString("GlobalBuildAction.warning"), //$NON-NLS-1$
		WorkbenchMessages.getString("GlobalBuildAction.noBuilders") //$NON-NLS-1$
	);
	
	return false;
}
}
