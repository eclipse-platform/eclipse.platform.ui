package org.eclipse.ui.externaltools.internal.core;

/**********************************************************************
Copyright (c) 2002 IBM Corp. and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v0.5
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v05.html
 
Contributors:
**********************************************************************/
import org.eclipse.ant.core.AntRunner;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;

/**
 * Responsible for running ant files.
 */
public class AntFileRunner extends ExternalToolsRunner {
	private static final String ANT_LOGGER_CLASS = "org.eclipse.ui.externaltools.internal.ui.ant.AntBuildLogger"; //$NON-NLS-1$
	private static final String NULL_LOGGER_CLASS = "org.eclipse.ui.externaltools.internal.ui.ant.NullBuildLogger"; //$NON-NLS-1$
	
	private static final String INPUT_HANDLER_CLASS = "org.eclipse.ui.externaltools.internal.ui.ant.AntInputHandler"; //$NON-NLS-1$
	
	private static final String BASE_DIR_PREFIX = "-Dbasedir="; //$NON-NLS-1$

	/**
	 * Creates an empty ant file runner
	 */
	public AntFileRunner() {
		super();
	}

	/* (non-Javadoc)
	 * Method declared in ExternalToolsRunner.
	 */
	public void execute(IProgressMonitor monitor, IRunnerContext runnerContext) throws CoreException, InterruptedException {
		try {
			String[] targets = runnerContext.getAntTargets();
			AntRunner runner = new AntRunner();
			String args = runnerContext.getExpandedArguments();
			String baseDir = runnerContext.getExpandedWorkingDirectory();
			if (baseDir.length() > 0) {
				String baseDirArg;
				if (ToolUtil.hasSpace(baseDir)) {
					baseDirArg = BASE_DIR_PREFIX + "\"" + baseDir + "\""; //$NON-NLS-2$ //$NON-NLS-1$
				} else {
					baseDirArg = BASE_DIR_PREFIX + baseDir;
				}
				runner.setArguments(args + " " + baseDirArg ); //$NON-NLS-1$
			} else {
				runner.setArguments(args);	
			}
			runner.setBuildFileLocation(runnerContext.getExpandedLocation());
			if (targets.length > 0) {
				runner.setExecutionTargets(targets);
			}
			if (runnerContext.getShowLog()) {
				runner.addBuildLogger(ANT_LOGGER_CLASS);
			} else {
				runner.addBuildLogger(NULL_LOGGER_CLASS);
			}
			
			runner.setInputHandler(INPUT_HANDLER_CLASS);
			
			runner.run(monitor);
		} catch (CoreException e) {
			Throwable carriedException = e.getStatus().getException();
			if (carriedException instanceof OperationCanceledException) {
				throw new InterruptedException(carriedException.getMessage());
			} else {
				throw e;
			}
		} finally {
			monitor.done();
		}
	}
}
