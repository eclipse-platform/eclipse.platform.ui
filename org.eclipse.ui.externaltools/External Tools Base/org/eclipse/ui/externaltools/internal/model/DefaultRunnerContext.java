package org.eclipse.ui.externaltools.internal.model;

/**********************************************************************
Copyright (c) 2002 IBM Corp. and others. All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
 
Contributors:
**********************************************************************/

import java.io.File;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.ui.externaltools.internal.registry.ExternalToolType;
import org.eclipse.ui.externaltools.internal.registry.ExternalToolTypeRegistry;
import org.eclipse.ui.externaltools.internal.registry.RefreshScopeVariable;
import org.eclipse.ui.externaltools.internal.registry.RefreshScopeVariableRegistry;
import org.eclipse.ui.externaltools.internal.ui.LogConsoleDocument;
import org.eclipse.ui.externaltools.model.ExternalTool;
import org.eclipse.ui.externaltools.model.IExternalToolRunner;
import org.eclipse.ui.externaltools.model.IRunnerContext;
import org.eclipse.ui.externaltools.model.IRunnerLog;
import org.eclipse.ui.externaltools.model.ToolUtil;
import org.eclipse.ui.externaltools.variable.ExpandVariableContext;

/**
 * Context to run the external tool in.
 */
public final class DefaultRunnerContext implements IRunnerContext {
	private ExternalTool tool;
	private ExpandVariableContext expandVarCtx;
	private String expandedLocation;
	private String[] expandedArguments;
	private String expandedDirectory;
	
	/**
	 * Create a new context
	 * 
	 * @param tool the external tool for which the context applies to
	 * @param project the project to run the external tool on, or <code>null</code>
	 * @param buildKind the kind of build being performed
	 * 		(see <code>IncrementalProjectBuilder</code>).
	 */
	public DefaultRunnerContext(ExternalTool tool, IProject project, int buildKind) {
		super();
		this.tool = tool;
		this.expandVarCtx = new ExpandVariableContext(project, buildKind);
	}

	/**
	 * Create a new context. The project will be determine from the
	 * specified selected resource.
	 * 
	 * @param tool the external tool for which the context applies to
	 * @param selectedResource the selected resource to run the external
	 * 		tool on, or <code>null</code>
	 */
	public DefaultRunnerContext(ExternalTool tool, IResource selectedResource) {
		super();
		this.tool = tool;
		this.expandVarCtx = new ExpandVariableContext(selectedResource);
	}

	/**
	 * Executes the runner to launch the external tool. A resource refresh
	 * is done if specified.
	 * 
	 * @param monitor the monitor to report progress or cancellation to
	 * @param status a multi status to report any problems while running tool
	 */
	private void executeRunner(IProgressMonitor monitor, MultiStatus status) {
		try {
			// Lookup the runner based on the tool's type
			ExternalToolTypeRegistry registry = ExternalToolsPlugin.getDefault().getTypeRegistry();
			ExternalToolType toolType = registry.getToolType(tool.getType());
			IExternalToolRunner runner = null;
			if (toolType != null)
				runner = toolType.getRunner();
			if (runner == null) {
				String msg = ToolMessages.format("DefaultRunnerContext.noToolRunner", new Object[] {tool.getName()}); //$NON-NLS-1$
				status.merge(ExternalToolsPlugin.newErrorStatus(msg, null));
				return;
			}
			
			if (monitor.isCanceled())
				return;
				
			// Run the tool
			if (tool.getRefreshScope() == null) {
				runner.run(monitor, this, status);
			} else {
				monitor.beginTask(ToolMessages.getString("DefaultRunnerContext.runningExternalTool"), 100); //$NON-NLS-1$
				runner.run(new SubProgressMonitor(monitor, 70), this, status);
				if (status.isOK() && !monitor.isCanceled())
					refreshResources(new SubProgressMonitor(monitor, 30), status);
			}
		} finally {
			monitor.done();
		}
	}
	
	/* (non-Javadoc)
	 * Method declared on IRunnerContext.
	 */
	 public boolean getCaptureOutput() {
	 	return tool.getCaptureOutput();
	 }
	 
	/* (non-Javadoc)
	 * Method declared on IRunnerContext.
	 */
	public String getExpandedLocation() {
		return expandedLocation;
	}

	/* (non-Javadoc)
	 * Method declared on IRunnerContext.
	 */
	public String[] getExpandedArguments() {
		return expandedArguments;
	}

	/* (non-Javadoc)
	 * Method declared on IRunnerContext.
	 */
	public String getExpandedWorkingDirectory() {
		return expandedDirectory;
	}
	
	/* (non-Javadoc)
	 * Method declared on IRunnerContext.
	 */
	public String getExtraAttribute(String key) {
		return tool.getExtraAttribute(key);
	}

	/* (non-Javadoc)
	 * Method declared on IRunnerContext.
	 */
	public IRunnerLog getLog() {
		return LogConsoleDocument.getInstance();
	}

	/* (non-Javadoc)
	 * Method declared on IRunnerContext.
	 */
	public String getName() {
		return tool.getName();
	}
	
	/**
	 * Runs the external tool is the context is valid. Once the tool
	 * has run, resources are refreshed based on the scope. Any problems
	 * while validating or running the tool will cause an exception
	 * to be thrown.
	 * 
	 * @param monitor the monitor to report progress or cancellation to
	 * @param status a multi status to report any problems while running tool
	 */
	public void run(IProgressMonitor monitor, MultiStatus status) {
		validateContext(status);
		if (status.isOK() && !monitor.isCanceled())
			executeRunner(monitor, status);
	}

	/**
	 * Refreshes the resources specified by the tool.
	 * 
	 * @param monitor the monitor to report progress or cancellation to
	 * @param status a multi status to report any problems while running tool
	 */
	private void refreshResources(IProgressMonitor monitor, MultiStatus status) {
		if (tool.getRefreshScope() == null)
			return;
		
		ToolUtil.VariableDefinition varDef = ToolUtil.extractVariableTag(tool.getRefreshScope(), 0);
		if (varDef.start == -1 || varDef.end == -1 || varDef.name == null) {
			String msg = ToolMessages.format("DefaultRunnerContext.invalidRefreshVarFormat", new Object[] {tool.getName()}); //$NON-NLS-1$
			status.merge(ExternalToolsPlugin.newErrorStatus(msg, null));
			return;
		}
		
		RefreshScopeVariableRegistry registry = ExternalToolsPlugin.getDefault().getRefreshVariableRegistry();
		RefreshScopeVariable variable = registry.getRefreshVariable(varDef.name);
		if (variable == null) {
			String msg = ToolMessages.format("DefaultRunnerContext.noRefreshVarNamed", new Object[] {tool.getName(), varDef.name}); //$NON-NLS-1$
			status.merge(ExternalToolsPlugin.newErrorStatus(msg, null));
			return;
		}

		int depth = IResource.DEPTH_ZERO;
		if (tool.getRefreshRecursive())
			depth = IResource.DEPTH_INFINITE;

		if (monitor.isCanceled())
			return;
					
		IResource[] resources = variable.getExpander().getResources(varDef.name, varDef.argument, expandVarCtx);
		if (resources == null || resources.length == 0)
			return;
			
		monitor.beginTask(
			ToolMessages.getString("DefaultRunnerContext.refreshResources"), //$NON-NLS-1$
			resources.length);
			
		for (int i = 0; i < resources.length; i++) {
			if (monitor.isCanceled())
				break;
			if (resources[i] != null && resources[i].isAccessible()) {
				try {
					resources[i].refreshLocal(depth, null);
				} catch (CoreException e) {
					status.merge(e.getStatus());
				}
			}
			monitor.worked(1);
		}
		
		monitor.done();
	}
	
	/**
	 * Validates the context in which to run the external tool.
	 * This will cause the location, arguments, and working
	 * directory to be expanded and verified.
	 * 
	 * @param status a multi status to report any problems while running tool
	 */
	private void validateContext(MultiStatus status) {
		expandedLocation = ToolUtil.expandFileLocation(tool.getLocation(), expandVarCtx, status);
		if (status.isOK()) {
			if (expandedLocation == null || expandedLocation.length() == 0) {
				String msg = ToolMessages.format("DefaultRunnerContext.invalidLocation", new Object[] {tool.getName()}); //$NON-NLS-1$
				status.merge(ExternalToolsPlugin.newErrorStatus(msg, null));
			} else {
				File file = new File(expandedLocation);
				if (!file.isFile()) {
					String msg = ToolMessages.format("DefaultRunnerContext.invalidLocation", new Object[] {tool.getName()}); //$NON-NLS-1$
					status.merge(ExternalToolsPlugin.newErrorStatus(msg, null));
				}
			}
		}
		
		expandedDirectory = ToolUtil.expandDirectoryLocation(tool.getWorkingDirectory(), expandVarCtx, status);
		if (status.isOK()) {
			if (expandedDirectory != null && expandedDirectory.length() > 0) {
				File path = new File(expandedDirectory);
				if (!path.isDirectory()) {
					String msg = ToolMessages.format("DefaultRunnerContext.invalidDirectory", new Object[] {tool.getName()}); //$NON-NLS-1$
					status.merge(ExternalToolsPlugin.newErrorStatus(msg, null));
				}
			}
		}
		
		expandedArguments = ToolUtil.expandArguments(tool.getArguments(), expandVarCtx, status);
	}
}