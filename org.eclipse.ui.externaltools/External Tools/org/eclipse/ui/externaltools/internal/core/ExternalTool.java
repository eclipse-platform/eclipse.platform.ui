package org.eclipse.ui.externaltools.internal.core;

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
 * This class represents an external tool that can be run. The tool
 * can be inside or outside the workspace.
 * <p>
 * An external tool consist of a user defined name, a path to the location
 * of the file, optional arguments for the file, and the working
 * directory.
 * </p><p>
 * After the tool has run, part or all of the workspace can be
 * refreshed to pickup changes made by the tool. This is optional
 * and does nothing by default
 * </p>
 */
public class ExternalTool {
	// Internal tags for storing tool related information
	private static final String TAG_TOOL_TYPE = "!{tool_type}"; //$NON-NLS-1$
	private static final String TAG_TOOL_NAME = "!{tool_name}"; //$NON-NLS-1$
	private static final String TAG_TOOL_LOCATION = "!{tool_loc}"; //$NON-NLS-1$
	private static final String TAG_TOOL_ARGUMENTS = "!{tool_args}"; //$NON-NLS-1$
	private static final String TAG_TOOL_DIRECTORY = "!{tool_dir}"; //$NON-NLS-1$
	private static final String TAG_TOOL_REFRESH = "!{tool_refresh}"; //$NON-NLS-1$
	private static final String TAG_TOOL_SHOW_LOG = "!{tool_show_log}"; //$NON-NLS-1$
	
	// Known kind of tools
	public static final String TOOL_TYPE_PROGRAM = "org.eclipse.ui.externaltools.type.program"; //$NON-NLS-1$
	public static final String TOOL_TYPE_ANT = "org.eclipse.ui.externaltools.type.ant"; //$NON-NLS-1$
	
	// Variable names the tool will expand
	public static final String VAR_WORKSPACE_LOC = "workspace_loc"; //$NON-NLS-1$

	public static final String VAR_PROJECT_LOC = "project_loc"; //$NON-NLS-1$
	public static final String VAR_PROJECT_PATH = "project_path"; //$NON-NLS-1$
	public static final String VAR_PROJECT_NAME = "project_name"; //$NON-NLS-1$

	public static final String VAR_RESOURCE_LOC = "resource_loc"; //$NON-NLS-1$
	public static final String VAR_RESOURCE_PATH = "resource_path"; //$NON-NLS-1$
	public static final String VAR_RESOURCE_NAME = "resource_name"; //$NON-NLS-1$

	public static final String VAR_CONTAINER_LOC = "container_loc"; //$NON-NLS-1$
	public static final String VAR_CONTAINER_PATH = "container_path"; //$NON-NLS-1$
	public static final String VAR_CONTAINER_NAME = "container_name"; //$NON-NLS-1$

	public static final String VAR_EDITOR_CUR_COL = "editor_cur_col"; //$NON-NLS-1$
	public static final String VAR_EDITOR_CUR_LINE = "editor_cur_line"; //$NON-NLS-1$
	public static final String VAR_EDITOR_SEL_TEXT = "editor_sel_text"; //$NON-NLS-1$

	public static final String VAR_ANT_TARGET = "ant_target"; //$NON-NLS-1$
	public static final String VAR_BUILD_TYPE = "build_type"; //$NON-NLS-1$

	// Known refresh scopes
	public static final String REFRESH_SCOPE_NONE = "none"; //$NON-NLS-1$;
	public static final String REFRESH_SCOPE_WORKSPACE = "workspace"; //$NON-NLS-1$;
	public static final String REFRESH_SCOPE_PROJECT = "project"; //$NON-NLS-1$;
	public static final String REFRESH_SCOPE_WORKING_SET = "working_set"; //$NON-NLS-1$;
	
	private static final String EMPTY_VALUE = ""; //$NON-NLS-1$;
	private static final String TRUE = "true"; //$NON-NLS-1$
	private static final String FALSE = "false"; //$NON-NLS-1$
	
	private String type = TOOL_TYPE_PROGRAM;
	private String name = EMPTY_VALUE;
	private String location = EMPTY_VALUE;
	private String arguments = EMPTY_VALUE;
	private String directory = EMPTY_VALUE;
	private String refreshScope = EMPTY_VALUE;
	private boolean showLog = true;
	
	/**
	 * Creates an empty initialized external tool.
	 */
	public ExternalTool() {
		super();
		this.refreshScope = ToolUtil.buildVariableTag(REFRESH_SCOPE_NONE, null);
	}

	/**
	 * Creates a fully initialized external tool.
	 */
	public ExternalTool(String type, String name, String location, String arguments, String directory, String refreshScope, boolean showLog) {
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
		this.showLog = showLog;
	}
	
	/**
	 * Creates an external tool based on specified arguments.
	 * Returns null if no corresponding external tool could be created.
	 */
	public static ExternalTool fromArgumentMap(Map args) {
		// Validate the critical information.
		String type = (String)args.get(TAG_TOOL_TYPE);
		String name = (String)args.get(TAG_TOOL_NAME);
		String location = (String)args.get(TAG_TOOL_LOCATION);
		if (type == null || name == null || location == null)
			return null;
		if (type.length() == 0 || name.length() == 0 || location.length() == 0)
			return null;
		String sShowLog = (String)args.get(TAG_TOOL_SHOW_LOG);
		boolean showLog;
		if (FALSE.equals(sShowLog))
			showLog = false;
		else
			showLog = true;
			
		return new ExternalTool(
			type,
			name,
			location,
			(String)args.get(TAG_TOOL_ARGUMENTS),
			(String)args.get(TAG_TOOL_DIRECTORY),
			(String)args.get(TAG_TOOL_REFRESH),
			showLog);
	}

	/**
	 * Returns the type of external tool.
	 */
	public String getType() {
		return type;
	}
	
	/**
	 * Returns the name of the external tool.
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Returns the path where the external tool is located.
	 */
	public String getLocation() {
		return location;
	}
	
	/**
	 * Returns the arguments for the external tool.
	 */
	public String getArguments() {
		return arguments;
	}
	
	/**
	 * Returns the working directory to run the external tool in.
	 */
	public String getWorkingDirectory() {
		return directory;
	}
	
	/**
	 * Returns the scope of resources to refresh after
	 * the external tool is run
	 */
	public String getRefreshScope() {
		return refreshScope;
	}
	
	/**
	 * Returns whether or not the execution log of the external tool
	 * will be shown on the log console.
	 */
	public boolean getShowLog() {
		return showLog;	
	}
	
	/**
	 * Sets the type of external tool.
	 */
	public void setType(String type) {
		if (type == null)
			this.type = EMPTY_VALUE;
		else
			this.type = type;
	}
	
	/**
	 * Sets the name of the external tool.
	 */
	public void setName(String name) {
		if (name == null)
			this.name = EMPTY_VALUE;
		else
			this.name = name;
	}
	
	/**
	 * Sets the path where the external tool is located.
	 */
	public void setLocation(String location) {
		if (location == null)
			this.location = EMPTY_VALUE;
		else
			this.location = location;
	}
	
	/**
	 * Sets the arguments for the external tool.
	 */
	public void setArguments(String arguments) {
		if (arguments == null)
			this.arguments = EMPTY_VALUE;
		else
			this.arguments = arguments;
	}
	
	/**
	 * Sets the working directory to run the external tool in.
	 */
	public void setWorkingDirectory(String directory) {
		if (directory == null)
			this.directory = EMPTY_VALUE;
		else
			this.directory = directory;
	}
	
	/**
	 * Sets the scope of resources to refresh after
	 * the external tool is run
	 */
	public void setRefreshScope(String refreshScope) {
		if (refreshScope == null || refreshScope.length() < 1)
			this.refreshScope = ToolUtil.buildVariableTag(REFRESH_SCOPE_NONE, null);
		else
			this.refreshScope = refreshScope;
	}
	
	/**
	 * Sets whether or not the execution log of the external tool should
	 * be shown on the log console.
	 */
	public void setShowLog(boolean showLog) {
		this.showLog = showLog;	
	}
	
	/**
	 * Stores the external tool as an argument map that can be
	 * used later on to recreate this external tool.
	 * 
	 * @return the argument map
	 */
	public Map toArgumentMap() {
		HashMap args = new HashMap();
		args.put(TAG_TOOL_TYPE, type);
		args.put(TAG_TOOL_NAME, name);
		args.put(TAG_TOOL_LOCATION, location);
		args.put(TAG_TOOL_ARGUMENTS, arguments);
		args.put(TAG_TOOL_DIRECTORY, directory);
		args.put(TAG_TOOL_REFRESH, refreshScope);
		if (showLog)
			args.put(TAG_TOOL_SHOW_LOG, TRUE);
		else
			args.put(TAG_TOOL_SHOW_LOG, FALSE);
		
		return args;
	}

	/**
	 * Configures the given build command to invoke this
	 * external tool.
	 * 
	 * @param command the build command to configure
	 * @return the configured command.
	 */
	public ICommand toBuildCommand(ICommand command) {
		Map args = toArgumentMap();
		command.setBuilderName(ExternalToolsBuilder.ID);
		command.setArguments(args);
		
		return command;
	}
}
