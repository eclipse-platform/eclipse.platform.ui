package org.eclipse.ui.externaltools.internal.core;

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
 * Responsible for running ant files.
 */
public class AntFileRunner extends ExternalToolsRunner {
	private static final String LOGGER_CLASS = "org.eclipse.ui.externaltools.internal.ui.ant.AntBuildLogger"; //$NON-NLS-1$

	/**
	 * Creates an empty ant file runner
	 */
	public AntFileRunner() {
		super();
	}

	/* (non-Javadoc)
	 * Method declared in ExternalToolsRunner.
	 */
	public void execute(IProgressMonitor monitor, IRunnerContext scriptContext) throws CoreException {
		try {
			String[] targets = scriptContext.getAntTargets();
			startMonitor(monitor, scriptContext, targets.length);
			AntUtil.setCurrentProgressMonitor(monitor);
			AntRunner runner = new AntRunner();
			runner.setArguments(scriptContext.getExpandedArguments());
			runner.setBuildFileLocation(scriptContext.getExpandedLocation());
			if (targets.length > 0)
				runner.setExecutionTargets(targets);
			if (scriptContext.getShowLog())
				runner.addBuildLogger(LOGGER_CLASS);
			runner.run();
		} catch (Exception e) {
			handleException(e);
		} finally {
			monitor.done();
			AntUtil.setCurrentProgressMonitor(null);
		}
	}
}
