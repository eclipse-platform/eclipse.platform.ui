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

	/**
	 * Creates an empty ant script runner
	 */
	public AntScriptRunner() {
		super();
	}

	/* (non-Javadoc)
	 * Method declared in ToolScriptRunner.
	 */
	public void execute(BuildListener listener, IProgressMonitor monitor, IToolScriptContext scriptContext) throws CoreException {
		try {
			startMonitor(monitor, scriptContext);
			AntRunner runner = new AntRunner();
			runner.setArguments(scriptContext.getExpandedArguments());
			runner.setBuildFileLocation(scriptContext.getExpandedLocation());
			if (listener != null)
				runner.addBuildListener(listener.getClass().getName());
			runner.run();
		} catch (Exception e) {
			handleException(e);
		} finally {
			monitor.done();
		}
	}
}
