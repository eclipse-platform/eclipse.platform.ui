package org.eclipse.toolscript.core.internal;

/**********************************************************************
Copyright (c) 2002 IBM Corp. and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v0.5
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v05.html
 
Contributors:
**********************************************************************/
import java.io.File;
import java.util.ArrayList;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.toolscript.ui.internal.ToolScriptMessages;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;

/**
 * Context to run the tool script in.
 */
public final class ToolScriptContext implements IToolScriptContext {
	private ToolScript script;
	private IProject currentProject;
	private IWorkingSetManager workingSetManager;
	private ArrayList antTargets = new ArrayList();
	private String expandedLocation;
	private String expandedArguments;
	private String expandedDirectory;
	
	/**
	 * Create a new context
	 * 
	 * @param script the tool script for which the context applies to
	 * @param currentProject the project to run the script on, or <code>null</code>
	 */
	public ToolScriptContext(ToolScript script, IProject currentProject, IWorkingSetManager manager) {
		super();
		this.script = script;
		this.currentProject = currentProject;
		this.workingSetManager = manager;
	}

	/* (non-Javadoc)
	 * Method declared on IToolScriptContext.
	 */
	public String getName() {
		return script.getName();
	}
	
	/* (non-Javadoc)
	 * Method declared on IToolScriptContext.
	 */
	public String[] getAntTargets() {
		// Required because ant target variable tags
		// are embedded in the script's arguments and
		// must be expanded beforehand.
		getExpandedArguments();
		
		String[] results = new String[antTargets.size()];	
		antTargets.toArray(results);
		return results;
	}

	/* (non-Javadoc)
	 * Method declared on IToolScriptContext.
	 */
	public String getExpandedLocation() {
		if (expandedLocation == null)
			expandedLocation = expandVariables(script.getLocation());
		return expandedLocation;
	}

	/* (non-Javadoc)
	 * Method declared on IToolScriptContext.
	 */
	public String getExpandedArguments() {
		if (expandedArguments == null)
			expandedArguments = expandVariables(script.getArguments());
		return expandedArguments;
	}

	/* (non-Javadoc)
	 * Method declared on IToolScriptContext.
	 */
	public String getExpandedWorkingDirectory() {
		if (expandedDirectory == null)
			expandedDirectory = expandVariables(script.getWorkingDirectory());
		return expandedDirectory;
	}
	
	/**
	 * Returns whether or not the script's execution log should appear
	 * on the log console.
	 */
	public boolean getShowLog() {
		return script.getShowLog();	
	}

	/**
	 * Expands the variables found in the text.
	 */
	private String expandVariables(String text) {
		StringBuffer buffer = new StringBuffer();
		
		int start = 0;
		while (true) {
			ToolUtil.VariableDefinition varDef = ToolUtil.extractVariableTag(text, start);
			
			if (varDef.start == -1) {
				if (start == 0)
					buffer.append(text);
				else
					buffer.append(text.substring(start));
				break;
			} else if (varDef.start > start) {
				buffer.append(text.substring(start, varDef.start));
			}

			if (varDef.end == -1) {
				buffer.append(text.substring(varDef.start));
				break;
			} else {
				start = varDef.end;
			}

			if (varDef.name != null)			
				expandVariable(varDef, buffer);
		}
		
		return buffer.toString();
	}

	/**
	 * Expands the variable
	 */
	private void expandVariable(ToolUtil.VariableDefinition varDef, StringBuffer buf) {
		if (script.VAR_DIR_WORKSPACE.equals(varDef.name)) {
			buf.append(Platform.getLocation().toString());
			return;
		}
		
		if (script.VAR_DIR_PROJECT.equals(varDef.name)) {
			IPath location = null;
			if (varDef.argument != null && varDef.argument.length() > 0) {
				IProject namedProject = ResourcesPlugin.getWorkspace().getRoot().getProject(varDef.argument);
				location = namedProject.getLocation();
			} else {
				if (currentProject != null)
					location = currentProject.getLocation();
			}
			if (location != null)
				buf.append(location.toString());
			return;
		}
		
		if (script.VAR_ANT_TARGET.equals(varDef.name)) {
			if (varDef.argument != null && varDef.argument.length() > 0)
				antTargets.add(varDef.argument);
			return;
		}
	}
	
	/**
	 * Executes the runner to launch the script. A resource refresh
	 * is done if specified.
	 * 
	 * @param monitor the monitor to report progress to, or <code>null</code>.
	 */
	private void executeRunner(IProgressMonitor monitor) throws CoreException {
		if (monitor == null)
			monitor = new NullProgressMonitor();
		try {
			ToolUtil.VariableDefinition scope = ToolUtil.extractVariableTag(script.getRefreshScope(), 0);
			ToolScriptRunner runner = ToolScriptPlugin.getDefault().getToolScriptRunner(script.getType());
			if (runner != null) {
				if (scope.name == null || script.REFRESH_SCOPE_NONE.equals(scope.name)) {
					runner.execute(monitor, this);
				} else {
					monitor.beginTask(ToolScriptMessages.getString("ToolScriptContext.runningToolScript"), 100); //$NON-NLS-1$
					runner.execute(new SubProgressMonitor(monitor, 70), this);
					refreshResources(new SubProgressMonitor(monitor, 30), scope.name, scope.argument);
				}
			}
		} finally {
			monitor.done();
		}
	}
	
	/**
	 * Runs the tool script and does a resource refresh if specified.
	 * The script is validated within this context before being
	 * runned. Any problems will cause an exception to be thrown.
	 * 
	 * @param monitor the monitor to report progress to, or <code>null</code>.
	 */
	public void run(IProgressMonitor monitor) throws CoreException {
		String problem = validateScriptInContext();
		if (problem != null) {
			IStatus status = new Status(IStatus.WARNING, ToolScriptPlugin.PLUGIN_ID, 0, problem, null);
			throw new CoreException(status);
		}
		
		executeRunner(monitor);
	}
	
	/**
	 * Runs the tool script and does a resource refresh if specified.
	 * The script is validated within this context before being
	 * runned. Any problems are displayed to the user in a dialog box.
	 * <p>
	 * <b>Note:</b> Only call this method if running within the UI thread
	 * </p>
	 * 
	 * @param monitor the monitor to report progress to, or <code>null</code>.
	 * @param shell the shell to parent the error message dialog
	 */
	public void run(IProgressMonitor monitor, Shell shell) {
		try {
			String problem = validateScriptInContext();
			if (problem != null) {
				MessageDialog.openWarning(
					shell, 
					ToolScriptMessages.getString("ToolScriptContext.errorShellTitle"), //$NON-NLS-1$
					problem);
			}
			
			executeRunner(monitor);
		} catch(CoreException e) {
			ErrorDialog.openError(
				shell,
				ToolScriptMessages.getString("ToolScriptContext.errorShellTitle"), //$NON-NLS-1$
				ToolScriptMessages.getString("ToolScriptContext.errorMessage"), //$NON-NLS-1$
				e.getStatus());
		}
	}

	/**
	 * Causes the specified resources to be refreshed.
	 */
	private void refreshResources(IProgressMonitor monitor, String scope, String argument) throws CoreException {
		if (script.REFRESH_SCOPE_WORKSPACE.equals(scope)) {
			ResourcesPlugin.getWorkspace().getRoot().refreshLocal(IResource.DEPTH_INFINITE, monitor);
			return;
		}
		
		if (script.REFRESH_SCOPE_PROJECT.equals(scope)) {
			IProject container = null;
			if (argument == null) {
				container = currentProject;
			} else {
				container = ResourcesPlugin.getWorkspace().getRoot().getProject(argument);
			}
			if (container != null && container.isAccessible())
				container.refreshLocal(IResource.DEPTH_INFINITE, monitor);
			return;
		}
		
		if (script.REFRESH_SCOPE_WORKING_SET.equals(scope)) {
			if (argument == null)
				return;
			IWorkingSet set = workingSetManager.getWorkingSet(argument);
			if (set == null)
				return;
			try {
				IAdaptable[] elements = set.getElements();
				monitor.beginTask(
					ToolScriptMessages.getString("ToolScriptContext.refreshWorkingSet"), //$NON-NLS-1$
					elements.length);
				for (int i = 0; i < elements.length; i++) {
					IAdaptable adaptable = elements[i];
					IResource resource;
					
					if (adaptable instanceof IResource)
						resource = (IResource) adaptable;
					else
						resource = (IResource) adaptable.getAdapter(IResource.class);
					if (resource != null)
						resource.refreshLocal(IResource.DEPTH_INFINITE, null);

					monitor.worked(1);
				}
			}
			finally {
				monitor.done();
			}
			
			return;
		}
	}
	
	/**
	 * Validates the script to ensure the script location and
	 * working directory exist in the file system.
	 * 
	 * @return the problem text is validate fails, or <code>null</code>
	 * 		if all seems valid.
	 */
	private String validateScriptInContext() {
		String loc = getExpandedLocation();
		if (loc == null || loc.length() == 0)
			return ToolScriptMessages.format("ToolScriptContext.invalidLocation", new Object[] {script.getName()}); //$NON-NLS-1$
		File file = new File(loc);
		if (!file.isFile())
			return  ToolScriptMessages.format("ToolScriptContext.invalidLocation", new Object[] {script.getName()}); //$NON-NLS-1$
		
		String dir = getExpandedWorkingDirectory();
		if (dir != null && dir.length() > 0) {
			File path = new File(dir);
			if (!path.isDirectory())
				return ToolScriptMessages.format("ToolScriptContext.invalidDirectory", new Object[] {script.getName()}); //$NON-NLS-1$
		}
		
		return null;
	}
}