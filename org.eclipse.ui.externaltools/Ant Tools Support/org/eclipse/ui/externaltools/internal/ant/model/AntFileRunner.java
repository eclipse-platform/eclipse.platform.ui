package org.eclipse.ui.externaltools.internal.ant.model;

/**********************************************************************
Copyright (c) 2002 IBM Corp. and others. All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
 
Contributors:
**********************************************************************/

import org.eclipse.ant.core.AntRunner;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.ui.externaltools.internal.model.ToolMessages;
import org.eclipse.ui.externaltools.model.IExternalToolRunner;
import org.eclipse.ui.externaltools.model.IRunnerContext;
import org.eclipse.ui.externaltools.model.IRunnerLog;

/**
 * Responsible for running ant build files.
 */
public class AntFileRunner implements IExternalToolRunner {
	private static final String ANT_LOGGER_CLASS = "org.eclipse.ui.externaltools.internal.ant.logger.AntBuildLogger"; //$NON-NLS-1$
	private static final String BASE_DIR_PREFIX = "-Dbasedir="; //$NON-NLS-1$

	/**
	 * Creates an ant build file runner
	 */
	public AntFileRunner() {
		super();
	}

	/* (non-Javadoc)
	 * Method declared in IExternalToolsRunner.
	 */
	public void run(IProgressMonitor monitor, IRunnerContext runnerContext, MultiStatus status) {
		try {
			// Determine the targets to run.
			String value = runnerContext.getExtraAttribute(AntUtil.RUN_TARGETS_ATTRIBUTE);
			String[] targets = AntUtil.parseRunTargets(value);

			AntRunner runner = new AntRunner();
			
			// Setup the arguments
			String[] args = runnerContext.getExpandedArguments();
			String[] runnerArgs = args;
			String baseDir = runnerContext.getExpandedWorkingDirectory();
			if (baseDir.length() > 0) {
				// Ant requires the working directory to be specified
				// as one of the arguments, so it needs to be appended.
				runnerArgs = new String[args.length + 1];
				System.arraycopy(args, 0, runnerArgs, 0, args.length);
				runnerArgs[args.length] = BASE_DIR_PREFIX + baseDir;
			}
			runner.setArguments(runnerArgs);
			
			runner.setBuildFileLocation(runnerContext.getExpandedLocation());
			if (targets.length > 0) {
				runner.setExecutionTargets(targets);
			}
			if (runnerContext.getCaptureOutput()) {
				runner.addBuildLogger(ANT_LOGGER_CLASS);
			}
			
			// Print out the command used to run the ant build file.
			if (IRunnerLog.LEVEL_VERBOSE <= runnerContext.getLog().getFilterLevel()) {
				runnerContext.getLog().append(
					ToolMessages.getString("AntFileRunner.callingAntRunner"), //$NON-NLS-1$;
					IRunnerLog.LEVEL_VERBOSE);
				runnerContext.getLog().append(
					ToolMessages.format("AntFileRunner.antFile", new Object[] {runnerContext.getExpandedLocation()}), //$NON-NLS-1$;
					IRunnerLog.LEVEL_VERBOSE);
				for (int i = 1; i < runnerArgs.length; i++) {
					runnerContext.getLog().append(
						ToolMessages.format("AntFileRunner.argument", new Object[] {runnerArgs[i]}), //$NON-NLS-1$;
						IRunnerLog.LEVEL_VERBOSE);
				}
				for (int i = 0; i < targets.length; i++) {
					runnerContext.getLog().append(
						ToolMessages.format("AntFileRunner.target", new Object[] {targets[i]}), //$NON-NLS-1$;
						IRunnerLog.LEVEL_VERBOSE);
				}
			}

			if (!monitor.isCanceled()) {
				runner.run(monitor);
			}
		} catch (CoreException e) {
			Throwable carriedException = e.getStatus().getException();
			if (carriedException instanceof OperationCanceledException) {
				monitor.setCanceled(true);
			} else {
				status.merge(e.getStatus());
			}
		} finally {
			monitor.done();
		}
	}
}
