package org.eclipse.toolscript.core.internal;

/**********************************************************************
Copyright (c) 2002 IBM Corp. and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v0.5
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v05.html
 
Contributors:
**********************************************************************/
import org.apache.tools.ant.BuildListener;
import org.eclipse.ant.core.AntRunner;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * Responsible for running ant scripts.
 */
public class AntScriptRunner extends ToolScriptRunner {
	private static final String LOGGER_CLASS = "org.eclipse.toolscript.ui.internal.AntBuildLogger"; //$NON-NLS-1$

	/**
	 * Creates an empty ant script runner
	 */
	public AntScriptRunner() {
		super();
	}

	/* (non-Javadoc)
	 * Method declared in ToolScriptRunner.
	 */
	public void execute(IProgressMonitor monitor, IToolScriptContext scriptContext) throws CoreException {
		try {
			String[] targets = scriptContext.getAntTargets();
			startMonitor(monitor, scriptContext, targets.length);
			AntUtil.setCurrentProgressMonitor(monitor);
			AntRunner runner = new AntRunner();
			runner.setArguments(scriptContext.getExpandedArguments());
			runner.setBuildFileLocation(scriptContext.getExpandedLocation());
			if (targets.length > 0)
				runner.setExecutionTargets(targets);
			//
			// TO DO: This needs to be updated to use the log document support
			//
//			runner.addBuildListener(LOGGER_CLASS);
			runner.run();
		} catch (Exception e) {
			handleException(e);
		} finally {
			monitor.done();
			AntUtil.setCurrentProgressMonitor(null);
		}
	}
}
