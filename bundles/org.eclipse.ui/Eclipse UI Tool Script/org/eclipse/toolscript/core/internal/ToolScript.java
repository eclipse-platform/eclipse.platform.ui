package org.eclipse.toolscript.core.internal;

/**********************************************************************
Copyright (c) 2002 IBM Corp. and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v0.5
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v05.html
 
Contributors:
**********************************************************************/
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.ICommand;

/**
 * This class represents a tool script that can be run. The tool
 * script can be inside or outside the workspace.
 * <p>
 * A tool script consist of a user defined name, a path to the location
 * of the script, optional arguments for the script, and the working
 * directory.
 * </p><p>
 * After the script has run, part or all of the workspace can be
 * refreshed to pickup changes made by the script. This is optional
 * and does nothing by default
 * </p>
 */
public class ToolScript {
	// Internal tags for storing script related information
	private static final String TAG_SCRIPT_TYPE = "!{script_type}"; //$NON-NLS-1$
	private static final String TAG_SCRIPT_NAME = "!{script_name}"; //$NON-NLS-1$
	private static final String TAG_SCRIPT_LOCATION = "!{script_loc}"; //$NON-NLS-1$
	private static final String TAG_SCRIPT_ARGUMENTS = "!{script_args}"; //$NON-NLS-1$
	private static final String TAG_SCRIPT_DIRECTORY = "!{script_dir}"; //$NON-NLS-1$
	private static final String TAG_SCRIPT_REFRESH = "!{script_refresh}"; //$NON-NLS-1$
	
	// Known kind of scripts
	public static final String SCRIPT_TYPE_PROGRAM = "org.eclipse.toolscript.type.program"; //$NON-NLS-1$
	public static final String SCRIPT_TYPE_ANT = "org.eclipse.toolscript.type.ant"; //$NON-NLS-1$
	
	// Variable tag identifiers
	/*package*/ static final String VAR_TAG_START = "${"; //$NON-NLS-1$
	/*package*/ static final String VAR_TAG_END = "}"; //$NON-NLS-1$
	/*package*/ static final String VAR_TAG_SEP = ":"; //$NON-NLS-1$
	
	// Variable names the tool script will expand
	public static final String VAR_DIR_WORKSPACE = "workspace_dir"; //$NON-NLS-1$
	public static final String VAR_DIR_PROJECT = "project_dir"; //$NON-NLS-1$

	// Known refresh scopes
	public static final String REFRESH_SCOPE_NONE = "none"; //$NON-NLS-1$;
	public static final String REFRESH_SCOPE_WORKSPACE = "workspace"; //$NON-NLS-1$;
	public static final String REFRESH_SCOPE_PROJECT = "project"; //$NON-NLS-1$;
	public static final String REFRESH_SCOPE_WORKING_SET = "working_set"; //$NON-NLS-1$;
	
	private static final String EMPTY_VALUE = ""; //$NON-NLS-1$;
	
	private String type = SCRIPT_TYPE_PROGRAM;
	private String name = EMPTY_VALUE;
	private String location = EMPTY_VALUE;
	private String arguments = EMPTY_VALUE;
	private String directory = EMPTY_VALUE;
	private String refreshScope = EMPTY_VALUE;
	
	/**
	 * Creates an empty initialized tool script.
	 */
	public ToolScript() {
		super();
		this.refreshScope = buildVariableTag(REFRESH_SCOPE_NONE, null);
	}

	/**
	 * Creates a fully initialized tool script.
	 */
	public ToolScript(String type, String name, String location, String arguments, String directory, String refreshScope) {
		this();
		if (type != null)
			this.type = type;
		if (name != null)
			this.name = name;
		if (location != null)
			this.location = location;
		if (arguments != null)
			this.arguments = arguments;
		if (directory != null)
			this.directory = directory;
		if (refreshScope != null)
			this.refreshScope = refreshScope;
	}
	
	/**
	 * Creates a tool script based on specified arguments.
	 * Returns null if no corresponding tool script could be created.
	 */
	public static ToolScript fromArgumentMap(Map args) {
		// Validate the critical information.
		String type = (String)args.get(TAG_SCRIPT_TYPE);
		String name = (String)args.get(TAG_SCRIPT_NAME);
		String location = (String)args.get(TAG_SCRIPT_LOCATION);
		if (type == null || name == null || location == null)
			return null;
		if (type.length() == 0 || name.length() == 0 || location.length() == 0)
			return null;

		return new ToolScript(
			type,
			name,
			location,
			(String)args.get(TAG_SCRIPT_ARGUMENTS),
			(String)args.get(TAG_SCRIPT_DIRECTORY),
			(String)args.get(TAG_SCRIPT_REFRESH));
	}

	/**
	 * Builds a variable tag that will be auto-expanded before
	 * the script is run.
	 * 
	 * @param varName the name of a known variable (one of the VAR_* constants)
	 * @param varArgument an optional argument for the variable, <code>null</code> if none
	 */
	public static String buildVariableTag(String varName, String varArgument) {
		StringBuffer buf = new StringBuffer();
		buf.append(VAR_TAG_START);
		buf.append(varName);
		if (varArgument != null && varArgument.length() > 0) {
			buf.append(VAR_TAG_SEP);
			buf.append(varArgument);
		}
		buf.append(VAR_TAG_END);
		return buf.toString();
	}
	
	/**
	 * Extracts a variable tag into its name and argument.
	 * 
	 * @param varTag the variable tag to parse
	 * @return an array where the 1st element is the var name and
	 * 		the 2nd element is the var argument. Elements in array
	 * 		can be <code>null</code>
	 */
	public static String[] extractVariableTag(String varTag) {
		String[] result = new String[2];
		int start = 0;
		int end = varTag.indexOf(VAR_TAG_START);
		if (end < 0)
			return result;
		start = end + VAR_TAG_START.length();
		
		end = varTag.indexOf(VAR_TAG_END, start);
		if (end < 0 || end == start)
			return result;

		int mid = varTag.indexOf(VAR_TAG_SEP, start);
		if (mid < 0 || mid > end) {
			result[0] = varTag.substring(start, end);
		} else {
			result[0] = varTag.substring(start, mid);
			result[1] = varTag.substring(mid+1, end);
		}
		
		return result;
	}

	/**
	 * Returns the type of script.
	 */
	public String getType() {
		return type;
	}
	
	/**
	 * Returns the name of the script.
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Returns the path where the script is located.
	 */
	public String getLocation() {
		return location;
	}
	
	/**
	 * Returns the arguments for the script.
	 */
	public String getArguments() {
		return arguments;
	}
	
	/**
	 * Returns the working directory to run the script in.
	 */
	public String getWorkingDirectory() {
		return directory;
	}
	
	/**
	 * Returns the scope of resources to refresh after
	 * the script is run
	 */
	public String getRefreshScope() {
		return refreshScope;
	}
	
	/**
	 * Sets the type of script.
	 */
	public void setType(String type) {
		if (type == null)
			this.type = EMPTY_VALUE;
		else
			this.type = type;
	}
	
	/**
	 * Sets the name of the script.
	 */
	public void setName(String name) {
		if (name == null)
			this.name = EMPTY_VALUE;
		else
			this.name = name;
	}
	
	/**
	 * Sets the path where the script is located.
	 */
	public void setLocation(String location) {
		if (location == null)
			this.location = EMPTY_VALUE;
		else
			this.location = location;
	}
	
	/**
	 * Sets the arguments for the script.
	 */
	public void setArguments(String arguments) {
		if (arguments == null)
			this.arguments = EMPTY_VALUE;
		else
			this.arguments = arguments;
	}
	
	/**
	 * Sets the working directory to run the script in.
	 */
	public void setWorkingDirectory(String directory) {
		if (directory == null)
			this.directory = EMPTY_VALUE;
		else
			this.directory = directory;
	}
	
	/**
	 * Sets the scope of resources to refresh after
	 * the script is run
	 */
	public void setRefreshScope(String refreshScope) {
		if (refreshScope == null || refreshScope.length() < 1)
			this.refreshScope = buildVariableTag(REFRESH_SCOPE_NONE, null);
		else
			this.refreshScope = refreshScope;
	}
	
	/**
	 * Stores the script as an argument map that can be
	 * used later on to recreate this script.
	 * 
	 * @return the argument map
	 */
	public Map toArgumentMap() {
		HashMap args = new HashMap();
		args.put(TAG_SCRIPT_TYPE, type);
		args.put(TAG_SCRIPT_NAME, name);
		args.put(TAG_SCRIPT_LOCATION, location);
		args.put(TAG_SCRIPT_ARGUMENTS, arguments);
		args.put(TAG_SCRIPT_DIRECTORY, directory);
		args.put(TAG_SCRIPT_REFRESH, refreshScope);
		
		return args;
	}

	/**
	 * Configures the given build command to invoke this
	 * tool script.
	 * 
	 * @param command the build command to configure
	 * @return the configured command.
	 */
	public ICommand toBuildCommand(ICommand command) {
		Map args = toArgumentMap();
		command.setBuilderName(ToolScriptBuilder.ID);
		command.setArguments(args);
		
		return command;
	}
}
