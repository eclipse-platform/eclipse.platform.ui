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
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.toolscript.ui.internal.ToolScriptMessages;

/**
 * Responsible for executing the tool script. Clients
 * must provide a public no-argument constructor
 */
public abstract class ToolScriptRunner {

	/**
	 * Creates an empty tool script runner
	 */
	public ToolScriptRunner() {
		super();
	}

	/**
	 * Execute the tool script
	 */
	public abstract void execute(BuildListener listener, IProgressMonitor monitor, IToolScript script) throws CoreException;
	
	/**
	 * Handles exceptions that may occur while running the script
	 */
	protected final void handleException(Exception e) throws CoreException {
		String msg = e.getMessage();
		if (msg == null)
			msg = ToolScriptMessages.getString("ToolScriptRunner.internalErrorMessage"); //$NON-NLS-1$;
		throw new CoreException(new Status(IStatus.ERROR, ToolScriptPlugin.PLUGIN_ID, 0, msg, e));
	}

	/**
	 * Starts the monitor to show progress while running the script
	 */
	protected final void startMonitor(IProgressMonitor monitor, IToolScript script) {
		String label = ToolScriptMessages.format("ToolScriptRunner.runningScriptLabel", new Object[] {script.getName()}); //$NON-NLS-1$;
		monitor.beginTask(label, IProgressMonitor.UNKNOWN);
	}
}
