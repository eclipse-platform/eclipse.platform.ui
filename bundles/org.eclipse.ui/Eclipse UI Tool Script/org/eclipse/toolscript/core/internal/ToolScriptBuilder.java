package org.eclipse.toolscript.core.internal;

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

/**
 * The tool script builder can be added to the build spec of a project to run 
 * scripts inside the incremental build process.
 * <p>
 * Note that there is only ever one instance of ToolScriptBuilder per project,
 * and the script to run is specified in the builder's arguments.
 * </p>
 */
public final class ToolScriptBuilder extends IncrementalProjectBuilder {
	public static final String ID = "org.eclipse.toolscript.core.toolScriptBuilder";

	/**
	 * Creates an uninitialized tool script builder.
	 */
	public ToolScriptBuilder() {
		super();
	}
	
	/* (non-Javadoc)
	 * Method declared on IncrementalProjectBuilder.
	 */
	protected IProject[] build(int kind, Map args, IProgressMonitor monitor) throws CoreException {
		ToolScript script = ToolScript.fromArgumentMap(args);
		IToolScriptContext context = new ToolScriptContext(script, getProject());
		if (script != null)
			script.run(null, monitor, context);
		return null;
	}
}
