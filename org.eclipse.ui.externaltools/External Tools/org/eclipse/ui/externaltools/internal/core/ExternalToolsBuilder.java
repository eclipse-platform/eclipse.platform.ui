package org.eclipse.ui.externaltools.internal.core;

/**********************************************************************
Copyright (c) 2002 IBM Corp. and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v0.5
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v05.html
 
Contributors:
**********************************************************************/
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.PlatformUI;

/**
 * The external tool builder can be added to the build spec of a project to run 
 * external tools inside the incremental build process.
 * <p>
 * Note that there is only ever one instance of ExternalToolsBuilder per project,
 * and the external tool to run is specified in the builder's arguments.
 * </p>
 */
public final class ExternalToolsBuilder extends IncrementalProjectBuilder {
	public static final String ID = "org.eclipse.ui.externaltools.ExternalToolBuilder";

	/**
	 * Creates an uninitialized external tool builder.
	 */
	public ExternalToolsBuilder() {
		super();
	}
	
	/* (non-Javadoc)
	 * Method declared on IncrementalProjectBuilder.
	 */
	protected IProject[] build(int kind, Map args, IProgressMonitor monitor) throws CoreException {
		ExternalTool tool = ExternalTool.fromArgumentMap(args);
		if (tool != null) {
			DefaultRunnerContext context = new DefaultRunnerContext(tool, getProject(), PlatformUI.getWorkbench().getWorkingSetManager());
			context.setBuildType(kind);
			try {
				context.run(monitor);
			} catch (InterruptedException e) {
				// Do nothing, the operation was cancelled by the user
			} finally {
				forgetLastBuiltState();
			}
		}
		
		return null;
	}
}
