package org.eclipse.toolscript.core.internal;

/**********************************************************************
Copyright (c) 2002 IBM Corp. and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v0.5
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v05.html
 
Contributors:
**********************************************************************/
import org.eclipse.core.resources.IProject;

/**
 * Context to run the tool script in.
 */
public final class ToolScriptContext implements IToolScriptContext {
	private ToolScript script;
	private IProject currentProject;
	private String expandedLocation;
	private String expandedArguments;
	private String expandedDirectory;
	
	/**
	 * Create a new context
	 * 
	 * @param script the tool script for which the context applies to
	 * @param currentProject the project to run the script on, or <code>null</code>
	 */
	public ToolScriptContext(ToolScript script, IProject currentProject) {
		super();
		this.script = script;
		this.currentProject = currentProject;
	}

	/* (non-Javadoc)
	 * Method declared on IToolScriptContext.
	 */
	public String getName() {
		return script.getName();
	}

	/* (non-Javadoc)
	 * Method declared on IToolScriptContext.
	 */
	public String getExpandedLocation() {
		if (expandedLocation == null)
			expandedLocation = expandVariables(script.getLocation());
		return expandedLocation;
	}

	/* (non-Javadoc)
	 * Method declared on IToolScriptContext.
	 */
	public String getExpandedArguments() {
		if (expandedArguments == null)
			expandedArguments = expandVariables(script.getArguments());
		return expandedArguments;
	}

	/* (non-Javadoc)
	 * Method declared on IToolScriptContext.
	 */
	public String getExpandedWorkingDirectory() {
		if (expandedDirectory == null)
			expandedDirectory = expandVariables(script.getWorkingDirectory());
		return expandedDirectory;
	}

	/**
	 * Expands the variables found in the text.
	 */
	private String expandVariables(String text) {
		return text;
	}
}