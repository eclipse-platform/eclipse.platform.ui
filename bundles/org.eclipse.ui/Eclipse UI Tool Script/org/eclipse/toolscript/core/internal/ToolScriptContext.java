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

import org.apache.tools.ant.BuildListener;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SubProgressMonitor;
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
	 * Expands the variables found in the text.
	 */
	private String expandVariables(String text) {
		StringBuffer buf = new StringBuffer();
		
		int start = 0;
		while (true) {
			int end = text.indexOf(script.VAR_TAG_START, start);
			if (end < 0) {
				if (start == 0)
					return text;
				buf.append(text.substring(start));
				break;
			}
			if (end > start)
				buf.append(text.substring(start, end));
			start = end + script.VAR_TAG_START.length();
			
			end = text.indexOf(script.VAR_TAG_END, start);
			if (end < 0)
				break;
			if (end > start) {
				String var = text.substring(start, end);
				expandVariable(var, buf);
			}
			
			start = end + script.VAR_TAG_END.length();
		}
		
		return buf.toString();
	}

	/**
	 * Expands the variable
	 */
	private void expandVariable(String text, StringBuffer buf) {
		String varName = null;
		String varArg = null;
		
		int i = text.indexOf(script.VAR_TAG_SEP);
		if (i < 0) {
			varName = text;
		} else {
			varName = text.substring(0, i);
			varArg = text.substring(i+1);
		}
		
		if (script.VAR_DIR_WORKSPACE.equals(varName)) {
			buf.append(Platform.getLocation().toString());
			return;
		}
		
		if (script.VAR_DIR_PROJECT.equals(varName)) {
			IPath location = null;
			if (varArg != null) {
				IProject namedProject = ResourcesPlugin.getWorkspace().getRoot().getProject(varArg);
				location = namedProject.getLocation();
			} else {
				if (currentProject != null)
					location = currentProject.getLocation();
			}
			if (location != null)
				buf.append(location.toString());
			return;
		}
	}
	
	/**
	 * Runs the tool script and does a resource refresh if specified. 
	 * An additional listener may be provided for logging more feedback.
	 * 
	 * @param listener the listener to provide feedback to, or null.
	 * @param monitor the monitor to report progress to, or null.
	 */
	public void run(BuildListener listener, IProgressMonitor monitor) throws CoreException {
		if (monitor == null)
			monitor = new NullProgressMonitor();
		try {
			String[] scope = script.extractVariableTag(script.getRefreshScope());
			ToolScriptRunner runner = ToolScriptPlugin.getDefault().getToolScriptRunner(script.getType());
			if (runner != null) {
				if (scope[0] == null || script.REFRESH_SCOPE_NONE.equals(scope[0])) {
					runner.execute(listener, monitor, this);
				} else {
					monitor.beginTask(ToolScriptMessages.getString("ToolScriptContext.runningToolScript"), 100); //$NON-NLS-1$
					runner.execute(listener, new SubProgressMonitor(monitor, 70), this);
					refreshResources(new SubProgressMonitor(monitor, 30), scope[0], scope[1]);
				}
			}
		} finally {
			monitor.done();
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
	public String validateScriptInContext() {
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