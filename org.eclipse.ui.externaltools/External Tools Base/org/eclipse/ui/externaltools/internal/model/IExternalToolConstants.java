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
	 * Extension point to declare refresh scope variables. Refresh scope
	 * variables are external tool variables that are only valid for
	 * specifying a refresh scope.
	 * (value <code>refreshVariables</code>).
	 */
	public static final String EXTENSION_POINT_REFRESH_VARIABLES = "refreshVariables"; //$NON-NLS-1$}
	// ------- Tool Types -------
	/**
	 * External tool type for programs such as executables, batch files, 
	 * shell scripts, etc (value <code>programType</code>).
	 */
	public static final String TOOL_TYPE_PROGRAM = "programType"; //$NON-NLS-1$;

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
