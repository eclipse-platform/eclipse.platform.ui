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
package org.eclipse.ui.externaltools.internal.registry;


import java.util.ArrayList;
import java.util.Map;
import java.util.StringTokenizer;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.variables.IVariableConstants;
import org.eclipse.debug.ui.variables.VariableUtil;
import org.eclipse.ui.externaltools.internal.model.IExternalToolConstants;

/**
 * Responsible reading an old external tool format and creating
 * and migrating it to create a new external tool.
 */
public final class ExternalToolMigration {
	/*
	 * Ant tags
	 */
	/**
	 * External tool type for Ant build files (value <code>antBuildType</code>).
	 */
	public static final String TOOL_TYPE_ANT_BUILD = "antBuildType"; //$NON-NLS-1$;
		
	public static final String RUN_TARGETS_ATTRIBUTE = TOOL_TYPE_ANT_BUILD + ".runTargets"; //$NON-NLS-1$;

	/**
	* String attribute indicating the Ant targets to execute. Default value is
	 * <code>null</code> which indicates that the default target is to be
	 * executed. Format is a comma separated listing of targets.
	 */
	public static final String ATTR_ANT_TARGETS = IExternalToolConstants.PLUGIN_ID + ".ATTR_ANT_TARGETS"; //$NON-NLS-1$
	
	/*
	 * 2.0 External Tool Tags
	 */
	private static final String TAG_TOOL_TYPE = "!{tool_type}"; //$NON-NLS-1$
	private static final String TAG_TOOL_NAME = "!{tool_name}"; //$NON-NLS-1$
	private static final String TAG_TOOL_LOCATION = "!{tool_loc}"; //$NON-NLS-1$
	private static final String TAG_TOOL_ARGUMENTS = "!{tool_args}"; //$NON-NLS-1$
	private static final String TAG_TOOL_DIRECTORY = "!{tool_dir}"; //$NON-NLS-1$
	private static final String TAG_TOOL_REFRESH = "!{tool_refresh}"; //$NON-NLS-1$
	private static final String TAG_TOOL_SHOW_LOG = "!{tool_show_log}"; //$NON-NLS-1$
	private static final String TAG_TOOL_BUILD_TYPES = "!{tool_build_types}"; //$NON-NLS-1$
	private static final String TAG_TOOL_BLOCK = "!{tool_block}"; //$NON-NLS-1$

	// Known kind of tools
	private static final String TOOL_TYPE_ANT = "org.eclipse.ui.externaltools.type.ant"; //$NON-NLS-1$

	/*
	 * 2.1 External Tool Keys
	 */
	private static final String TAG_TYPE = "type"; //$NON-NLS-1$
	private static final String TAG_NAME = "name"; //$NON-NLS-1$
	private static final String TAG_LOCATION = "location"; //$NON-NLS-1$
	private static final String TAG_WORK_DIR = "workDirectory"; //$NON-NLS-1$
	private static final String TAG_CAPTURE_OUTPUT = "captureOutput"; //$NON-NLS-1$
	private static final String TAG_SHOW_CONSOLE = "showConsole"; //$NON-NLS-1$
	private static final String TAG_RUN_BKGRND = "runInBackground"; //$NON-NLS-1$
	private static final String TAG_PROMPT_ARGS = "promptForArguments"; //$NON-NLS-1$
	private static final String TAG_ARGS = "arguments"; //$NON-NLS-1$
	private static final String TAG_REFRESH_SCOPE = "refreshScope"; //$NON-NLS-1$
	private static final String TAG_REFRESH_RECURSIVE = "refreshRecursive"; //$NON-NLS-1$
	private static final String TAG_RUN_BUILD_KINDS = "runForBuildKinds"; //$NON-NLS-1$
	private static final String TAG_EXTRA_ATTR = "extraAttribute"; //$NON-NLS-1$
	private static final String TAG_VERSION = "version"; //$NON-NLS-1$

	private static final String EXTRA_ATTR_SEPARATOR = "="; //$NON-NLS-1$

	private static final String VERSION_21 = "2.1"; //$NON-NLS-1$;

	private static final String TRUE = "true"; //$NON-NLS-1$
	private static final String FALSE = "false"; //$NON-NLS-1$

	/**
	 * Allows no instances.
	 */
	private ExternalToolMigration() {
		super();
	}

	/**
	 * Returns a  launch configuration working copy from the argument map or
	 * <code>null</code> if the given map cannot be interpreted as a 2.0 or 2.1
	 * branch external tool. The returned working copy will be unsaved and its
	 * location will be set to the metadata area.
	 */
	public static ILaunchConfigurationWorkingCopy configFromArgumentMap(Map args) {
		String version = (String) args.get(TAG_VERSION);
		if (VERSION_21.equals(version)) {
			return configFrom21ArgumentMap(args);
		}
		return configFrom20ArgumentMap(args);
	}

	public static ILaunchConfigurationWorkingCopy configFrom21ArgumentMap(Map commandArgs) {
		String name = (String) commandArgs.get(TAG_NAME);
		String type = (String) commandArgs.get(TAG_TYPE);
		
		ILaunchConfigurationWorkingCopy config = newConfig(type, name);
		if (config == null) {
			return null;
		}
		
		config.setAttribute(IExternalToolConstants.ATTR_LOCATION, (String) commandArgs.get(TAG_LOCATION));
		config.setAttribute(IExternalToolConstants.ATTR_WORKING_DIRECTORY, (String) commandArgs.get(TAG_WORK_DIR));
		config.setAttribute(IExternalToolConstants.ATTR_CAPTURE_OUTPUT, TRUE.equals((String) commandArgs.get(TAG_CAPTURE_OUTPUT)));
		config.setAttribute(IExternalToolConstants.ATTR_SHOW_CONSOLE, TRUE.equals((String) commandArgs.get(TAG_SHOW_CONSOLE)));
		config.setAttribute(IExternalToolConstants.ATTR_RUN_IN_BACKGROUND, TRUE.equals((String) commandArgs.get(TAG_RUN_BKGRND)));
		config.setAttribute(IExternalToolConstants.ATTR_PROMPT_FOR_ARGUMENTS, TRUE.equals((String) commandArgs.get(TAG_PROMPT_ARGS)));
		config.setAttribute(IExternalToolConstants.ATTR_REFRESH_SCOPE, (String) commandArgs.get(TAG_REFRESH_SCOPE));
		config.setAttribute(IExternalToolConstants.ATTR_REFRESH_RECURSIVE, TRUE.equals((String) commandArgs.get(TAG_REFRESH_RECURSIVE)));

		config.setAttribute(IExternalToolConstants.ATTR_RUN_BUILD_KINDS, (String) commandArgs.get(TAG_RUN_BUILD_KINDS));
		
		String args = (String) commandArgs.get(TAG_ARGS);
		if (args != null) {
			config.setAttribute(IExternalToolConstants.ATTR_TOOL_ARGUMENTS, args);
		}

		String extraAttributes = (String) commandArgs.get(TAG_EXTRA_ATTR);
		if (extraAttributes != null) {
			StringTokenizer tokenizer = new StringTokenizer(extraAttributes, EXTRA_ATTR_SEPARATOR);
			while (tokenizer.hasMoreTokens()) {
				String key = tokenizer.nextToken();
				if (!tokenizer.hasMoreTokens())
					break;
				String value = tokenizer.nextToken();
				if (key.equals(RUN_TARGETS_ATTRIBUTE)) {
					// 2.1 implementation only defined 1 "extra attribute"
					config.setAttribute(ATTR_ANT_TARGETS, value);
				}
			}
		}
		return config;
	}

	/**
	 * Creates an external tool from the map.
	 */
	public static ILaunchConfigurationWorkingCopy configFrom20ArgumentMap(Map args) {
		// Update the type...
		String type = (String) args.get(TAG_TOOL_TYPE);
		if (TOOL_TYPE_ANT.equals(type)) {
			type = TOOL_TYPE_ANT_BUILD;
		} else {
			type = IExternalToolConstants.TOOL_TYPE_PROGRAM;
		}

		String name = (String) args.get(TAG_TOOL_NAME);
		
		ILaunchConfigurationWorkingCopy config = newConfig(type, name);
		if (config == null) {
			return null;
		}

		// Update the location...
		String location = (String) args.get(TAG_TOOL_LOCATION);
		if (location != null) {
			VariableUtil.VariableDefinition varDef = VariableUtil.extractVariableTag(location, 0);
			if (IVariableConstants.VAR_WORKSPACE_LOC.equals(varDef.name)) {
				location = VariableUtil.buildVariableTag(IVariableConstants.VAR_RESOURCE_LOC, varDef.argument);
			}
			config.setAttribute(IExternalToolConstants.ATTR_LOCATION, location);
		}

		// Update the refresh scope...
		String refresh = (String) args.get(TAG_TOOL_REFRESH);
		if (refresh != null) {
			VariableUtil.VariableDefinition varDef = VariableUtil.extractVariableTag(refresh, 0);
			if ("none".equals(varDef.name)) { //$NON-NLS-1$
				refresh = null;
			}
			config.setAttribute(IExternalToolConstants.ATTR_REFRESH_SCOPE, refresh);
		}

		// Update the arguments
		String arguments = (String) args.get(TAG_TOOL_ARGUMENTS);
		String targetNames = null;
		if (arguments != null) {
			int start = 0;
			ArrayList targets = new ArrayList();
			StringBuffer buffer = new StringBuffer();
			VariableUtil.VariableDefinition varDef = VariableUtil.extractVariableTag(arguments, start);
			while (varDef.end != -1) {
				if ("ant_target".equals(varDef.name) && varDef.argument != null) { //$NON-NLS-1$
					targets.add(varDef.argument);
					buffer.append(arguments.substring(start, varDef.start));
				} else {
					buffer.append(arguments.substring(start, varDef.end));
				}
				start = varDef.end;
				varDef = VariableUtil.extractVariableTag(arguments, start);
			}
			buffer.append(arguments.substring(start, arguments.length()));
			arguments = buffer.toString();

			buffer.setLength(0);
			for (int i = 0; i < targets.size(); i++) {
				String target = (String) targets.get(i);
				if (target != null && target.length() > 0) {
					buffer.append(target);
					buffer.append(","); //$NON-NLS-1$
				}
			}
			targetNames = buffer.toString();
		}
		if (targetNames != null && targetNames.length() > 0) {
			config.setAttribute(ATTR_ANT_TARGETS, targetNames);
		}

		// Collect the rest of the information
		config.setAttribute(IExternalToolConstants.ATTR_SHOW_CONSOLE, TRUE.equals((String) args.get(TAG_TOOL_SHOW_LOG)));
		config.setAttribute(IExternalToolConstants.ATTR_CAPTURE_OUTPUT, TRUE.equals((String) args.get(TAG_TOOL_SHOW_LOG)));
		config.setAttribute(IExternalToolConstants.ATTR_RUN_IN_BACKGROUND, FALSE.equals((String) args.get(TAG_TOOL_BLOCK)));
		config.setAttribute(IExternalToolConstants.ATTR_RUN_BUILD_KINDS, (String) args.get(TAG_TOOL_BUILD_TYPES));
		config.setAttribute(IExternalToolConstants.ATTR_TOOL_ARGUMENTS, arguments);
		config.setAttribute(IExternalToolConstants.ATTR_WORKING_DIRECTORY, (String) args.get(TAG_TOOL_DIRECTORY));
		return config;
	}

	/**
	 * Returns a new working copy with the given external tool name and external
	 * tool type or <code>null</code> if no config could be created.
	 */
	private static ILaunchConfigurationWorkingCopy newConfig(String type, String name) {
		if (type == null || name == null) {
			return null;
		}
		ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager();
		ILaunchConfigurationType configType;
		//if (IExternalToolConstants.TOOL_TYPE_ANT_BUILD.equals(type)) {
			//configType = manager.getLaunchConfigurationType(IExternalToolConstants.ID_ANT_BUILDER_LAUNCH_CONFIGURATION_TYPE);
		//} else
		 if (IExternalToolConstants.TOOL_TYPE_PROGRAM.equals(type)) {
			configType = manager.getLaunchConfigurationType(IExternalToolConstants.ID_PROGRAM_BUILDER_LAUNCH_CONFIGURATION_TYPE);
		} else {
			return null;
		}
		try {
			return configType.newInstance(null, name);
		} catch (CoreException e) {
			return null;
		}
	}
	
	/**
	 * Returns the tool name extracted from the given command argument map.
	 * Extraction is attempted using 2.0 and 2.1 external tool formats.
	 */
	public static String getNameFromCommandArgs(Map commandArgs) {
		String name= (String) commandArgs.get(TAG_NAME);
		if (name == null) {
			name= (String) commandArgs.get(TAG_TOOL_NAME);
		}
		return name;
	}	
}
