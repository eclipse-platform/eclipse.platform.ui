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
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;

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
		StringBuffer buf = new StringBuffer();
		
		int start = 0;
		while (true) {
			int end = text.indexOf(script.VAR_TAG_START, start);
			if (end < 0) {
				if (start == 0)
					return text;
				buf.append(text.substring(start));
				break;
			}
			if (end > start)
				buf.append(text.substring(start, end));
			start = end + script.VAR_TAG_START.length();
			
			end = text.indexOf(script.VAR_TAG_END, start);
			if (end < 0)
				break;
			if (end > start) {
				String var = text.substring(start, end);
				expandVariable(var, buf);
			}
			
			start = end + script.VAR_TAG_END.length();
		}
		
		return buf.toString();
	}

	/**
	 * Expands the variable
	 */
	private void expandVariable(String text, StringBuffer buf) {
		String varName = null;
		String varArg = null;
		
		int i = text.indexOf(script.VAR_TAG_SEP);
		if (i < 0) {
			varName = text;
		} else {
			varName = text.substring(0, i);
			varArg = text.substring(i+1);
		}
		
		if (script.VAR_DIR_WORKSPACE.equals(varName)) {
			buf.append(Platform.getLocation().toString());
			return;
		}
		
		if (script.VAR_DIR_PROJECT.equals(varName)) {
			IPath location = null;
			if (varArg != null) {
				IProject namedProject = ResourcesPlugin.getWorkspace().getRoot().getProject(varArg);
				location = namedProject.getLocation();
			} else {
				if (currentProject != null)
					location = currentProject.getLocation();
			}
			if (location != null)
				buf.append(location.toString());
			return;
		}
	}
}