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
	private String buildType = ToolUtil.BUILD_TYPE_NONE;
	
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
			expandedLocation = expandVariables(tool.getLocation(), false);
		return expandedLocation;
	}

	/* (non-Javadoc)
	 * Method declared on IRunnerContext.
	 */
	public String getExpandedArguments() {
		if (expandedArguments == null)
			expandedArguments = expandVariables(tool.getArguments(), true);
		return expandedArguments;
	}

	/* (non-Javadoc)
	 * Method declared on IRunnerContext.
	 */
	public String getExpandedWorkingDirectory() {
		if (expandedDirectory == null)
			expandedDirectory = expandVariables(tool.getWorkingDirectory(), false);
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
	private String expandVariables(String text, boolean addQuotes) {
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
				expandVariable(varDef, buffer, addQuotes);
		}
		
		return buffer.toString();
	}

	/**
	 * Expands the variable
	 */
	private void expandVariable(ToolUtil.VariableDefinition varDef, StringBuffer buf, boolean addQuotes) {
		if (ExternalTool.VAR_BUILD_TYPE.equals(varDef.name)) {
			appendVariable(buildType, buf, addQuotes);	
		}

		if (ExternalTool.VAR_ANT_TARGET.equals(varDef.name)) {
			if (varDef.argument != null && varDef.argument.length() > 0)
				antTargets.add(varDef.argument);
			return;
		}
		
		if (ExternalTool.VAR_WORKSPACE_LOC.equals(varDef.name)) {
			String location = null;
			if (varDef.argument != null && varDef.argument.length() > 0)
				location = ToolUtil.getLocationFromFullPath(varDef.argument);
			else
				location = Platform.getLocation().toOSString();
			appendVariable(location, buf, addQuotes);
			return;
		}
		
		if (ExternalTool.VAR_PROJECT_LOC.equals(varDef.name)) {
			String location = null;
			if (varDef.argument != null && varDef.argument.length() > 0) {
				IResource member = ResourcesPlugin.getWorkspace().getRoot().findMember(varDef.argument);
				if (member != null)
					location = member.getProject().getLocation().toOSString();
			} else {
				if (currentProject != null)
					location = currentProject.getLocation().toOSString();
			}
			appendVariable(location, buf, addQuotes);
			return;
		}
		
		if (ExternalTool.VAR_RESOURCE_LOC.equals(varDef.name)) {
			String location = null;
			if (varDef.argument != null && varDef.argument.length() > 0) {
				location = ToolUtil.getLocationFromFullPath(varDef.argument);
			} else {
				if (selectedResource != null)
					location = selectedResource.getLocation().toOSString();
			}
			appendVariable(location, buf, addQuotes);
			return;			
		}
		
		if (ExternalTool.VAR_CONTAINER_LOC.equals(varDef.name)) {
			String location = null;
			if (varDef.argument != null && varDef.argument.length() > 0) {
				IResource member = ResourcesPlugin.getWorkspace().getRoot().findMember(varDef.argument);
				if (member != null)
					location = member.getParent().getLocation().toOSString();
			} else {
				if (selectedResource != null)
					location = selectedResource.getParent().getLocation().toOSString();
			}
			appendVariable(location, buf, addQuotes);
			return;			
		}
		
		if (ExternalTool.VAR_PROJECT_PATH.equals(varDef.name)) {
			String fullPath = null;
			if (varDef.argument != null && varDef.argument.length() > 0) {
				IResource member = ResourcesPlugin.getWorkspace().getRoot().findMember(varDef.argument);
				if (member != null)
					fullPath = member.getProject().getFullPath().toString();
			} else {
				if (currentProject != null)
					fullPath = currentProject.getFullPath().toString();
			}
			appendVariable(fullPath, buf, addQuotes);
			return;
		}
		
		if (ExternalTool.VAR_RESOURCE_PATH.equals(varDef.name)) {
			String fullPath = null;
			if (varDef.argument != null && varDef.argument.length() > 0) {
				IResource member = ResourcesPlugin.getWorkspace().getRoot().findMember(varDef.argument);
				if (member != null)
					fullPath = member.getFullPath().toString();
			} else {
				if (selectedResource != null)
					fullPath = selectedResource.getFullPath().toString();
			}
			appendVariable(fullPath, buf, addQuotes);
			return;			
		}
		
		if (ExternalTool.VAR_CONTAINER_PATH.equals(varDef.name)) {
			String fullPath = null;
			if (varDef.argument != null && varDef.argument.length() > 0) {
				IResource member = ResourcesPlugin.getWorkspace().getRoot().findMember(varDef.argument);
				if (member != null)
					fullPath = member.getParent().getFullPath().toString();
			} else {
				if (selectedResource != null)
					fullPath = selectedResource.getParent().getFullPath().toString();
			}
			appendVariable(fullPath, buf, addQuotes);
			return;			
		}
		
		if (ExternalTool.VAR_PROJECT_NAME.equals(varDef.name)) {
			String name = null;
			if (varDef.argument != null && varDef.argument.length() > 0) {
				IResource member = ResourcesPlugin.getWorkspace().getRoot().findMember(varDef.argument);
				if (member != null)
					name = member.getProject().getName();
			} else {
				if (currentProject != null)
					name = currentProject.getName();
			}
			appendVariable(name, buf, addQuotes);
			return;
		}
		
		if (ExternalTool.VAR_RESOURCE_NAME.equals(varDef.name)) {
			String name = null;
			if (varDef.argument != null && varDef.argument.length() > 0) {
				IResource member = ResourcesPlugin.getWorkspace().getRoot().findMember(varDef.argument);
				if (member != null)
					name = member.getName();
			} else {
				if (selectedResource != null)
					name = selectedResource.getName();
			}
			appendVariable(name, buf, addQuotes);
			return;			
		}
		
		if (ExternalTool.VAR_CONTAINER_NAME.equals(varDef.name)) {
			String name = null;
			if (varDef.argument != null && varDef.argument.length() > 0) {
				IResource member = ResourcesPlugin.getWorkspace().getRoot().findMember(varDef.argument);
				if (member != null)
					name = member.getParent().getName();
			} else {
				if (selectedResource != null)
					name = selectedResource.getParent().getName();
			}
			appendVariable(name, buf, addQuotes);
			return;			
		}
	}

	/**
	 * Helper method to add the given variable string to the given
	 * string buffer if the string is not null. Adds enclosing quotation
	 * marks if addQuotes is true.
	 * 
	 * @param var the variable string to be added
	 * @param buf the string buffer to which the string will be added
	 * @parman addQuotes whether or not to add enclosing quotation marks
	 */
	private void appendVariable(String var, StringBuffer buf, boolean addQuotes) {
		if (var != null) {
			if (addQuotes && ToolUtil.hasSpace(var))
				buf.append("\""); //$NON-NLS-1$
			buf.append(var);
			if (addQuotes && ToolUtil.hasSpace(var))
				buf.append("\""); //$NON-NLS-1$			
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
				if (scope.name == null || ExternalTool.REFRESH_SCOPE_NONE.equals(scope.name)) {
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
		if (ExternalTool.REFRESH_SCOPE_WORKSPACE.equals(scope)) {
			ResourcesPlugin.getWorkspace().getRoot().refreshLocal(IResource.DEPTH_INFINITE, monitor);
			return;
		}
		
		if (ExternalTool.REFRESH_SCOPE_PROJECT.equals(scope)) {
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
		
		if (ExternalTool.REFRESH_SCOPE_WORKING_SET.equals(scope)) {
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
	
	/**
	 * Set the build type for this runner context. The build type is the type 
	 * of build (one of ToolUtil.BUILD_TYPE_INCREMENTAL, ToolUtil.BUILD_TYPE_AUTO,
	 * or ToolUtil.BUILD_TYPE_FULL) if the tool being run is running as a project
	 * builder, or ToolUtil.BUILD_TYPE_NONE otherwise.
	 */
	/*package*/ void setBuildType(int kind) {
		if (kind == IncrementalProjectBuilder.INCREMENTAL_BUILD)
			buildType = ToolUtil.BUILD_TYPE_INCREMENTAL;
		else if (kind == IncrementalProjectBuilder.FULL_BUILD)
			buildType = ToolUtil.BUILD_TYPE_FULL;
		else if (kind == IncrementalProjectBuilder.AUTO_BUILD)
			buildType = ToolUtil.BUILD_TYPE_AUTO;
		else 
			buildType = ToolUtil.BUILD_TYPE_NONE;
	}
}