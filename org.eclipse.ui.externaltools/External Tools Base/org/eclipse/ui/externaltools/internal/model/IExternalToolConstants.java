/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.externaltools.internal.model;


/**
 * Defines the constants available for client use.
 * <p>
 * This interface is not intended to be extended or implemented by clients.
 * </p>
 */
public interface IExternalToolConstants {
	/**
	 * Plugin identifier for external tools (value <code>org.eclipse.ui.externaltools</code>).
	 */
	public static final String PLUGIN_ID = "org.eclipse.ui.externaltools"; //$NON-NLS-1$;

	// ------- Extensions Points -------
	/**
	 * Extension point to declare the launch configuration type that should be
	 * created when duplicating an existing configuration as a project builder.
	 */
	public static final String EXTENSION_POINT_CONFIGURATION_DUPLICATION_MAPS = "configurationDuplicationMaps"; //$NON-NLS-1$
	/**
	 * Extension point to declare external tool variables. 
	 * (value<code>toolVariables</code>).
	 */
	public static final String EXTENSION_POINT_TOOL_VARIABLES = "toolVariables"; //$NON-NLS-1$

	/**
	 * Extension point to declare refresh scope variables. Refresh scope
	 * variables are external tool variables that are only valid for
	 * specifying a refresh scope.
	 * (value <code>refreshVariables</code>).
	 */
	public static final String EXTENSION_POINT_REFRESH_VARIABLES = "refreshVariables"; //$NON-NLS-1$

	// ------- Tool Types -------
	/**
	 * External tool type for programs such as executables, batch files, 
	 * shell scripts, etc (value <code>programType</code>).
	 */
	public static final String TOOL_TYPE_PROGRAM = "programType"; //$NON-NLS-1$;

	// ------- Variables -------

	/**
	 * Variable that expands to the absolute path on the system's hard drive
	 * to the workspace directory (value <code>workspace_loc</code>).
	 */
	public static final String VAR_WORKSPACE_LOC = "workspace_loc"; //$NON-NLS-1$

	/**
	 * Variable that expands to the absolute path on the system's hard drive
	 * to a project's directory (value <code>project_loc</code>).
	 */
	public static final String VAR_PROJECT_LOC = "project_loc"; //$NON-NLS-1$

	/**
	 * Variable that expands to the full path, relative to the workspace root,
	 * of a project (value <code>project_path</code>).
	 */
	public static final String VAR_PROJECT_PATH = "project_path"; //$NON-NLS-1$

	/**
	 * Variable that expands to the name of a project (value <code>project_name</code>).
	 */
	public static final String VAR_PROJECT_NAME = "project_name"; //$NON-NLS-1$

	/**
	 * Variable that expands to the absolute path on the system's hard drive
	 * to a resource's location (value <code>resource_loc</code>).
	 */
	public static final String VAR_RESOURCE_LOC = "resource_loc"; //$NON-NLS-1$

	/**
	 * Variable that expands to the full path, relative to the workspace root,
	 * of a resource (value <code>resource_path</code>).
	 */
	public static final String VAR_RESOURCE_PATH = "resource_path"; //$NON-NLS-1$

	/**
	 * Variable that expands to the name of a resource (value <code>resource_name</code>).
	 */
	public static final String VAR_RESOURCE_NAME = "resource_name"; //$NON-NLS-1$

	/**
	 * Variable that expands to the absolute path on the system's hard drive
	 * to a resource's containing directory (value <code>container_loc</code>).
	 */
	public static final String VAR_CONTAINER_LOC = "container_loc"; //$NON-NLS-1$

	/**
	 * Variable that expands to the full path, relative to the workspace root,
	 * of a resource's parent (value <code>container_path</code>).
	 */
	public static final String VAR_CONTAINER_PATH = "container_path"; //$NON-NLS-1$

	/**
	 * Variable that expands to the name of a resource's parent (value <code>container_name</code>).
	 */
	public static final String VAR_CONTAINER_NAME = "container_name"; //$NON-NLS-1$

	/**
	 * Variable that expands to the type of build (value <code>build_type</code>). See
	 * <code>BUILD_TYPE_*</code> constants for possible values.
	 */
	public static final String VAR_BUILD_TYPE = "build_type"; //$NON-NLS-1$

	/**
	 * Variable that expands to the current editor cursor column (value <code>editor_cur_col</code>).
	 */
	public static final String VAR_EDITOR_CUR_COL = "editor_cur_col"; //$NON-NLS-1$

	/**
	 * Variable that expands to the current editor cursor line (value <code>editor_cur_line</code>).
	 */
	public static final String VAR_EDITOR_CUR_LINE = "editor_cur_line"; //$NON-NLS-1$

	/**
	 * Variable that expands to the current editor selected text (value <code>editor_sel_text</code>).
	 */
	public static final String VAR_EDITOR_SEL_TEXT = "editor_sel_text"; //$NON-NLS-1$

	// ------- Refresh Variables -------
	/**
	 * Variable that expands to the workspace root object (value <code>workspace</code>).
	 */
	public static final String VAR_WORKSPACE = "workspace"; //$NON-NLS-1$

	/**
	 * Variable that expands to the project resource (value <code>project</code>).
	 */
	public static final String VAR_PROJECT = "project"; //$NON-NLS-1$

	/**
	 * Variable that expands to the container resource (value <code>container</code>).
	 */
	public static final String VAR_CONTAINER = "container"; //$NON-NLS-1$

	/**
	 * Variable that expands to a resource (value <code>resource</code>).
	 */
	public static final String VAR_RESOURCE = "resource"; //$NON-NLS-1$

	/**
	 * Variable that expands to the working set object (value <code>working_set</code>).
	 */
	public static final String VAR_WORKING_SET = "working_set"; //$NON-NLS-1$

	// ------- Build Types -------
	/**
	 * Build type indicating an incremental project build request for
	 * the external tool running as a builder (value <code>incremental</code>).
	 */
	public static final String BUILD_TYPE_INCREMENTAL = "incremental"; //$NON-NLS-1$

	/**
	 * Build type indicating a full project build request for
	 * the external tool running as a builder (value <code>full</code>).
	 */
	public static final String BUILD_TYPE_FULL = "full"; //$NON-NLS-1$

	/**
	 * Build type indicating an automatic project build request for
	 * the external tool running as a builder (value <code>incremental</code>).
	 */
	public static final String BUILD_TYPE_AUTO = "auto"; //$NON-NLS-1$

	/**
	 * Build type indicating an no project build request for
	 * the external tool running as a builder (value <code>none</code>).
	 */
	public static final String BUILD_TYPE_NONE = "none"; //$NON-NLS-1$

	// ------- Images -------
	/**
	 * Refresh tab image
	 */
	public static final String IMG_TAB_REFRESH = PLUGIN_ID + ".IMG_TAB_REFRESH"; //$NON-NLS-1$

	/**
	 * Main tab image.
	 */
	public static final String IMG_TAB_MAIN = PLUGIN_ID + ".IMG_TAB_MAIN"; //$NON-NLS-1$
	
	// ------- Launch configuration types --------
	/**
	 * Program launch configuration type identifier.
	 */
	public static final String ID_PROGRAM_LAUNCH_CONFIGURATION_TYPE = "org.eclipse.ui.externaltools.ProgramLaunchConfigurationType"; //$NON-NLS-1$
	
	/**
	 * Program builder launch configuration type identifier. Program project
	 * builders are of this type.
	 */
	public static final String ID_PROGRAM_BUILDER_LAUNCH_CONFIGURATION_TYPE = "org.eclipse.ui.externaltools.ProgramBuilderLaunchConfigurationType"; //$NON-NLS-1$	

	// ------- Launch configuration category --------
	/**
	 * Identifier for external tools launch configuration category. Launch
	 * configuration types for external tools that appear in the external tools
	 * launch configuration dialog should belong to this category.
	 */
	public static final String ID_EXTERNAL_TOOLS_LAUNCH_CATEGORY = "org.eclipse.ui.externaltools"; //$NON-NLS-1$
	/**
	 * Identifier for external tools launch configuration builders category.
	 * Launch configuration types that can be added as project builders should
	 * belong to this category.
	 */
	public static final String ID_EXTERNAL_TOOLS_BUILDER_LAUNCH_CATEGORY = "org.eclipse.ui.externaltools.builder"; //$NON-NLS-1$

	// ------- Launch configuration groups --------
	/**
	 * Identifier for external tools launch configuration group. The external
	 * tools launch configuration group corresponds to the external tools
	 * category in run mode.
	 */
	public static final String ID_EXTERNAL_TOOLS_LAUNCH_GROUP = "org.eclipse.ui.externaltools.launchGroup"; //$NON-NLS-1$
	/**
	 * Identifier for external tools launch configuration group
	 */
	public static final String ID_EXTERNAL_TOOLS_BUILDER_LAUNCH_GROUP = "org.eclipse.ui.externaltools.launchGroup.builder"; //$NON-NLS-1$

	// ------- Common External Tool Launch Configuration Attributes -------

	/**
	 * Boolean attribute indicating if external tool output should be captured.
	 * Default value is <code>false</code>.
	 */
	public static final String ATTR_CAPTURE_OUTPUT = PLUGIN_ID + ".ATTR_CAPTURE_OUTPUT"; //$NON-NLS-1$
	/**
	 * String attribute identifying the location of an external. Default value
	 * is <code>null</code>. Encoding is tool specific.
	 */
	public static final String ATTR_LOCATION = PLUGIN_ID + ".ATTR_LOCATION"; //$NON-NLS-1$

	/**
	 * Boolean attribute indicating if the user should be prompted for
	 * arguments before running a tool. Default value is <code>false</code>.
	 */
	public static final String ATTR_PROMPT_FOR_ARGUMENTS = PLUGIN_ID + ".ATTR_PROMPT_FOR_ARGUMENTS"; //$NON-NLS-1$

	/**
	 * Boolean attribute indicating if a refresh scope is recursive. Default
	 * value is <code>false</code>.
	 */
	public static final String ATTR_REFRESH_RECURSIVE = PLUGIN_ID + ".ATTR_REFRESH_RECURSIVE"; //$NON-NLS-1$

	/**
	 * String attribute identifying the scope of resources that should be
	 * refreshed after an external tool is run. Default value is
	 * <code>null</code>, indicating no refresh. Format is ???
	 */
	public static final String ATTR_REFRESH_SCOPE = PLUGIN_ID + ".ATTR_REFRESH_SCOPE"; //$NON-NLS-1$
	
	/**
	 * String attribute containing an array of build kinds for which an
	 * external tool builder should be run.
	 */
	public static final String ATTR_RUN_BUILD_KINDS = PLUGIN_ID + ".ATTR_RUN_BUILD_KINDS"; //$NON-NLS-1$
	
	/**
	 * Boolean attribute indicating if an external tool should be run in the
	 * background. Default value is <code>false</code>.
	 */
	public static final String ATTR_RUN_IN_BACKGROUND = PLUGIN_ID + ".ATTR_RUN_IN_BACKGROUND"; //$NON-NLS-1$
	
	/**
	 * Boolean attribute indicating if the console should be shown on external
	 * tool output. Default value is <code>false</code>.
	 */
	public static final String ATTR_SHOW_CONSOLE = PLUGIN_ID + ".ATTR_SHOW_CONSOLE"; //$NON-NLS-1$

	/**
	 * String attribute containing the arguments that should be passed to the
	 * tool. Default value is <code>null</code>, and encoding is tool specific.
	 */
	public static final String ATTR_TOOL_ARGUMENTS = PLUGIN_ID + ".ATTR_TOOL_ARGUMENTS"; //$NON-NLS-1$

	/**
	 * String attribute identifying the working directory of an external tool.
	 * Default value is <code>null</code>, which indicates a default working
	 * directory, which is tool specific.
	 */
	public static final String ATTR_WORKING_DIRECTORY = PLUGIN_ID + ".ATTR_WORKING_DIRECTORY"; //$NON-NLS-1$	
}
