package org.eclipse.ui.externaltools.internal.core;

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

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.dialogs.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;

/**
 * Context to run the external tool in.
 */
public final class DefaultRunnerContext implements IRunnerContext {
	private ExternalTool tool;
	private IProject currentProject;
	private IResource selectedResource;
	private IWorkingSetManager workingSetManager;
	private ArrayList antTargets = new ArrayList();
	private String expandedLocation;
	private String expandedArguments;
	private String expandedDirectory;
	
	/**
	 * Create a new context
	 * 
	 * @param tool the external tool for which the context applies to
	 * @param currentProject the project to run the external tool on, or <code>null</code>
	 * @param manager the working set manager
	 */
	public DefaultRunnerContext(ExternalTool tool, IProject currentProject, IWorkingSetManager manager) {
		this(tool, currentProject, null, manager);
	}

	/**
	 * Create a new context
	 * 
	 * @param tool the external tool for which the context applies to
	 * @param currentProject the project to run the external tool on, or <code>null</code>
	 * @param selectedResource the selected resource to run the external tool on, or <code>null</code>
	 * @param manager the working set manager
	 */
	public DefaultRunnerContext(ExternalTool tool, IProject currentProject, IResource selectedResource, IWorkingSetManager manager) {
		super();
		this.tool = tool;
		this.currentProject = currentProject;
		this.selectedResource = selectedResource;
		this.workingSetManager = manager;
	}

	/* (non-Javadoc)
	 * Method declared on IRunnerContext.
	 */
	public String getName() {
		return tool.getName();
	}
	
	/* (non-Javadoc)
	 * Method declared on IRunnerContext.
	 */
	public String[] getAntTargets() {
		// Required because ant target variable tags
		// are embedded in the tool's arguments and
		// must be expanded beforehand.
		getExpandedArguments();
		
		String[] results = new String[antTargets.size()];	
		antTargets.toArray(results);
		return results;
	}

	/* (non-Javadoc)
	 * Method declared on IRunnerContext.
	 */
	public String getExpandedLocation() {
		if (expandedLocation == null)
			expandedLocation = expandVariables(tool.getLocation());
		return expandedLocation;
	}

	/* (non-Javadoc)
	 * Method declared on IRunnerContext.
	 */
	public String getExpandedArguments() {
		if (expandedArguments == null)
			expandedArguments = expandVariables(tool.getArguments());
		return expandedArguments;
	}

	/* (non-Javadoc)
	 * Method declared on IRunnerContext.
	 */
	public String getExpandedWorkingDirectory() {
		if (expandedDirectory == null)
			expandedDirectory = expandVariables(tool.getWorkingDirectory());
		return expandedDirectory;
	}
	
	/**
	 * Returns whether or not the execution log should appear
	 * on the log console.
	 */
	public boolean getShowLog() {
		return tool.getShowLog();	
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
		if (tool.VAR_ANT_TARGET.equals(varDef.name)) {
			if (varDef.argument != null && varDef.argument.length() > 0)
				antTargets.add(varDef.argument);
			return;
		}
		
		if (tool.VAR_WORKSPACE_LOC.equals(varDef.name)) {
			String location = null;
			if (varDef.argument != null && varDef.argument.length() > 0)
				location = ToolUtil.getLocationFromFullPath(varDef.argument);
			else
				location = Platform.getLocation().toString();
			if (location != null)
				buf.append(location);
			return;
		}
		
		if (tool.VAR_PROJECT_LOC.equals(varDef.name)) {
			IPath location = null;
			if (varDef.argument != null && varDef.argument.length() > 0) {
				IResource member = ResourcesPlugin.getWorkspace().getRoot().findMember(varDef.argument);
				if (member != null)
					location = member.getProject().getLocation();
			} else {
				if (currentProject != null)
					location = currentProject.getLocation();
			}
			if (location != null)
				buf.append(location.toString());
			return;
		}
		
		if (tool.VAR_RESOURCE_LOC.equals(varDef.name)) {
			String location = null;
			if (varDef.argument != null && varDef.argument.length() > 0) {
				location = ToolUtil.getLocationFromFullPath(varDef.argument);
			} else {
				if (selectedResource != null)
					location = selectedResource.getLocation().toString();
			}
			if (location != null)
				buf.append(location);
			return;			
		}
		
		if (tool.VAR_CONTAINER_LOC.equals(varDef.name)) {
			String location = null;
			if (varDef.argument != null && varDef.argument.length() > 0) {
				IResource member = ResourcesPlugin.getWorkspace().getRoot().findMember(varDef.argument);
				if (member != null)
					location = member.getParent().getLocation().toString();
			} else {
				if (selectedResource != null)
					location = selectedResource.getParent().getLocation().toString();
			}
			if (location != null)
				buf.append(location);
			return;			
		}
		
		if (tool.VAR_PROJECT_PATH.equals(varDef.name)) {
			IPath fullPath = null;
			if (varDef.argument != null && varDef.argument.length() > 0) {
				IResource member = ResourcesPlugin.getWorkspace().getRoot().findMember(varDef.argument);
				if (member != null)
					fullPath = member.getProject().getFullPath();
			} else {
				if (currentProject != null)
					fullPath = currentProject.getFullPath();
			}
			if (fullPath != null)
				buf.append(fullPath.toString());
			return;
		}
		
		if (tool.VAR_RESOURCE_PATH.equals(varDef.name)) {
			String fullPath = null;
			if (varDef.argument != null && varDef.argument.length() > 0) {
				IResource member = ResourcesPlugin.getWorkspace().getRoot().findMember(varDef.argument);
				if (member != null)
					fullPath = member.getFullPath().toString();
			} else {
				if (selectedResource != null)
					fullPath = selectedResource.getFullPath().toString();
			}
			if (fullPath != null)
				buf.append(fullPath);
			return;			
		}
		
		if (tool.VAR_CONTAINER_PATH.equals(varDef.name)) {
			String fullPath = null;
			if (varDef.argument != null && varDef.argument.length() > 0) {
				IResource member = ResourcesPlugin.getWorkspace().getRoot().findMember(varDef.argument);
				if (member != null)
					fullPath = member.getParent().getFullPath().toString();
			} else {
				if (selectedResource != null)
					fullPath = selectedResource.getParent().getFullPath().toString();
			}
			if (fullPath != null)
				buf.append(fullPath);
			return;			
		}
		
		if (tool.VAR_PROJECT_NAME.equals(varDef.name)) {
			String name = null;
			if (varDef.argument != null && varDef.argument.length() > 0) {
				IResource member = ResourcesPlugin.getWorkspace().getRoot().findMember(varDef.argument);
				if (member != null)
					name = member.getProject().getName();
			} else {
				if (currentProject != null)
					name = currentProject.getName();
			}
			if (name != null)
				buf.append(name);
			return;
		}
		
		if (tool.VAR_RESOURCE_NAME.equals(varDef.name)) {
			String name = null;
			if (varDef.argument != null && varDef.argument.length() > 0) {
				IResource member = ResourcesPlugin.getWorkspace().getRoot().findMember(varDef.argument);
				if (member != null)
					name = member.getName();
			} else {
				if (selectedResource != null)
					name = selectedResource.getName();
			}
			if (name != null)
				buf.append(name);
			return;			
		}
		
		if (tool.VAR_CONTAINER_NAME.equals(varDef.name)) {
			String name = null;
			if (varDef.argument != null && varDef.argument.length() > 0) {
				IResource member = ResourcesPlugin.getWorkspace().getRoot().findMember(varDef.argument);
				if (member != null)
					name = member.getParent().getName();
			} else {
				if (selectedResource != null)
					name = selectedResource.getParent().getName();
			}
			if (name != null)
				buf.append(name);
			return;			
		}
	}
	
	/**
	 * Executes the runner to launch the external tool. A resource refresh
	 * is done if specified.
	 * 
	 * @param monitor the monitor to report progress to, or <code>null</code>.
	 */
	private void executeRunner(IProgressMonitor monitor) throws CoreException, InterruptedException {
		if (monitor == null)
			monitor = new NullProgressMonitor();
		try {
			ToolUtil.VariableDefinition scope = ToolUtil.extractVariableTag(tool.getRefreshScope(), 0);
			ExternalToolsRunner runner = ToolUtil.getRunner(tool.getType());
			if (runner != null) {
				if (scope.name == null || tool.REFRESH_SCOPE_NONE.equals(scope.name)) {
					runner.execute(monitor, this);
				} else {
					monitor.beginTask(ToolMessages.getString("DefaultRunnerContext.runningExternalTool"), 100); //$NON-NLS-1$
					runner.execute(new SubProgressMonitor(monitor, 70), this);
					refreshResources(new SubProgressMonitor(monitor, 30), scope.name, scope.argument);
				}
			}
		} finally {
			monitor.done();
		}
	}
	
	/**
	 * Runs the external tool and does a resource refresh if specified.
	 * The tool is validated within this context before being
	 * runned. Any problems will cause an exception to be thrown.
	 * 
	 * @param monitor the monitor to report progress to, or <code>null</code>.
	 */
	public void run(IProgressMonitor monitor) throws CoreException, InterruptedException {
		String problem = validateInContext();
		if (problem != null) {
			IStatus status = new Status(IStatus.WARNING, ExternalToolsPlugin.PLUGIN_ID, 0, problem, null);
			throw new CoreException(status);
		}
		
		executeRunner(monitor);
	}

	/**
	 * Runs the external tool and does a resource refresh if specified.
	 * The tool is validated within this context before being
	 * runned. Any problems are displayed to the user in a dialog box.
	 * 
	 * @param monitor the monitor to report progress to, or <code>null</code>.
	 * @param shell the shell to parent the error message dialog
	 */
	public void run(IProgressMonitor monitor, final Shell shell) throws InterruptedException {
		try {
			final String problem = validateInContext();
			if (problem != null) {
				shell.getDisplay().syncExec(new Runnable() { 
					public void run() {
						MessageDialog.openError(
							shell, 
							ToolMessages.getString("DefaultRunnerContext.errorShellTitle"), //$NON-NLS-1$
							problem);
					}
				});
			} else {
				executeRunner(monitor);
			}
		} catch (final CoreException e) {
			shell.getDisplay().syncExec(new Runnable() { 
				public void run() {
					ErrorDialog.openError(
						shell,
						ToolMessages.getString("DefaultRunnerContext.errorShellTitle"), //$NON-NLS-1$
						ToolMessages.getString("DefaultRunnerContext.errorMessage"), //$NON-NLS-1$
						e.getStatus());
				}
			});
		}
	}

	/**
	 * Causes the specified resources to be refreshed.
	 */
	private void refreshResources(IProgressMonitor monitor, String scope, String argument) throws CoreException {
		if (tool.REFRESH_SCOPE_WORKSPACE.equals(scope)) {
			ResourcesPlugin.getWorkspace().getRoot().refreshLocal(IResource.DEPTH_INFINITE, monitor);
			return;
		}
		
		if (tool.REFRESH_SCOPE_PROJECT.equals(scope)) {
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
		
		if (tool.REFRESH_SCOPE_WORKING_SET.equals(scope)) {
			if (argument == null)
				return;
			IWorkingSet set = workingSetManager.getWorkingSet(argument);
			if (set == null)
				return;
			try {
				IAdaptable[] elements = set.getElements();
				monitor.beginTask(
					ToolMessages.getString("DefaultRunnerContext.refreshWorkingSet"), //$NON-NLS-1$
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
	 * Validates the external tool to ensure the external tool location and
	 * working directory exist in the file system.
	 * 
	 * @return the problem text is validate fails, or <code>null</code>
	 * 		if all seems valid.
	 */
	private String validateInContext() {
		String loc = getExpandedLocation();
		if (loc == null || loc.length() == 0)
			return ToolMessages.format("DefaultRunnerContext.invalidLocation", new Object[] {tool.getName()}); //$NON-NLS-1$
		File file = new File(loc);
		if (!file.isFile())
			return  ToolMessages.format("DefaultRunnerContext.invalidLocation", new Object[] {tool.getName()}); //$NON-NLS-1$
		
		String dir = getExpandedWorkingDirectory();
		if (dir != null && dir.length() > 0) {
			File path = new File(dir);
			if (!path.isDirectory())
				return ToolMessages.format("DefaultRunnerContext.invalidDirectory", new Object[] {tool.getName()}); //$NON-NLS-1$
		}
		
		return null;
	}
}