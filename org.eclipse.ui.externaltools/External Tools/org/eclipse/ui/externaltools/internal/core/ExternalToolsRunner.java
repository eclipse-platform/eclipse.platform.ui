package org.eclipse.ui.externaltools.internal.core;

/**********************************************************************
Copyright (c) 2002 IBM Corp. and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v0.5
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v05.html
 
Contributors:
**********************************************************************/
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * Responsible for executing the external tool. Clients
 * must provide a public no-argument constructor
 */
public abstract class ExternalToolsRunner {

	/**
	 * Creates an empty external tool runner
	 */
	public ExternalToolsRunner() {
		super();
	}

	/**
	 * Execute the external tool within the given context. Subclasses
	 * are responsible for showing the execution log if
	 * specified in the context.
	 */
	public abstract void execute(IProgressMonitor monitor, IRunnerContext runnerContext) throws CoreException, InterruptedException;
	
	/**
	 * Handles exceptions that may occur while running.
	 */
	protected final void handleException(Exception e) throws CoreException {
		String msg = e.getMessage();
		if (msg == null)
			msg = ToolMessages.getString("ExternalToolsRunner.internalErrorMessage"); //$NON-NLS-1$;
		throw new CoreException(new Status(IStatus.ERROR, ExternalToolsPlugin.PLUGIN_ID, 0, msg, e));
	}

	/**
	 * Starts the monitor to show progress while running.
	 */
	protected final void startMonitor(IProgressMonitor monitor, IRunnerContext runnerContext, int workAmount) {
		String label = ToolMessages.format("ExternalToolsRunner.runningToolLabel", new Object[] {runnerContext.getName()}); //$NON-NLS-1$;
		monitor.beginTask(label, workAmount);
	}
}
