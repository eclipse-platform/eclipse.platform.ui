package org.eclipse.ui.externaltools.internal.registry;

/**********************************************************************
Copyright (c) 2002 IBM Corp. and others. All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
 
Contributors:
**********************************************************************/

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.XMLMemento;
import org.eclipse.ui.externaltools.internal.model.ExternalToolsPlugin;
import org.eclipse.ui.externaltools.model.ExternalTool;
import org.eclipse.ui.externaltools.model.IExternalToolConstants;
import org.eclipse.ui.externaltools.model.ToolUtil;

/**
 * Responsible reading an old external tool format and creating
 * and migrating it to create a new external tool.
 */
public final class ExternalToolMigration {
	private static final String SEPERATOR = ";"; //$NON-NLS-1$	
	private static final String STATE_FILE_NAME_OLD = "oldexternaltools.xml"; //$NON-NLS-1$
	private static final String STATE_FILE_NAME = "externaltools.xml"; //$NON-NLS-1$
	private static final String TAG_EXTERNALTOOLS = "externaltools"; //$NON-NLS-1$
	private static final String TAG_TOOL = "tool"; //$NON-NLS-1$
	private static final String TAG_ENTRY = "entry"; //$NON-NLS-1$
	private static final String TAG_KEY = "key"; //$NON-NLS-1$
	private static final String TAG_VALUE = "value"; //$NON-NLS-1$

	// Internal tags for storing tool related information
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
	private static final String TOOL_TYPE_PROGRAM = "org.eclipse.ui.externaltools.type.program"; //$NON-NLS-1$
	private static final String TOOL_TYPE_ANT = "org.eclipse.ui.externaltools.type.ant"; //$NON-NLS-1$
	
	private static final String TRUE = "true"; //$NON-NLS-1$
	private static final String FALSE = "false"; //$NON-NLS-1$

	/**
	 * Allows no instances.
	 */
	private ExternalToolMigration() {
		super();
	}

	/**
	 * Loads the external tools from storage and
	 * adds them to the registry.
	 */
	/*package*/ static ExternalTool[] readInOldTools(ExternalToolRegistry reg) {
		boolean migrationSuccessful = true;
		ArrayList externalTools = null;
		IPath path = ExternalToolsPlugin.getDefault().getStateLocation();
		File file = path.append(STATE_FILE_NAME).toFile();
		if (!file.exists())
			return new ExternalTool[0];
			
		InputStreamReader reader = null;
		try {
			FileInputStream input = new FileInputStream(file);
			reader = new InputStreamReader(input, "utf-8"); //$NON-NLS-1$
			XMLMemento memento = XMLMemento.createReadRoot(reader);
			
			// Get the external tool children element
			IMemento[] tools = memento.getChildren(TAG_TOOL);
			externalTools = new ArrayList(tools.length);
			for (int i = 0; i < tools.length; i++) {
				HashMap args = new HashMap();
				IMemento[] entries = tools[i].getChildren(TAG_ENTRY);
				for (int j = 0; j < entries.length; j++) {
					String key = entries[j].getString(TAG_KEY);
					if (key != null) {
						String value = entries[j].getTextData();
						args.put(key, value);
					}
				}
				ExternalTool tool = toolFromArgumentMap(args, reg, "ExternalToolNewName" + String.valueOf(i)); //$NON-NLS-1$
				if (tool != null)
					externalTools.add(tool);
			}
		}
		catch (FileNotFoundException e) {
			// Silently ignore this...
		}
		catch (IOException e) {
			ExternalToolsPlugin.getDefault().log("File I/O error with reading old external tools.", e); //$NON-NLS-1$
			migrationSuccessful = false;
		}
		catch (WorkbenchException e) {
			ExternalToolsPlugin.getDefault().getLog().log(e.getStatus());
			System.err.println("Error reading old external tools. See .log file for more details"); //$NON-NLS-1$
			migrationSuccessful = false;
		}
		finally {
			if (reader != null) {
				try {
					reader.close();
				} catch(IOException e) {
					ExternalToolsPlugin.getDefault().log("Unable to close external tool old state reader.", e); //$NON-NLS-1$
				}
			}
		}
		
		if (migrationSuccessful) {
			if (!file.renameTo(path.append(STATE_FILE_NAME_OLD).toFile())) {
				ExternalToolsPlugin.getDefault().log("Unable to rename old external tool state file. Please rename externaltools.xml to oldexternaltools.xml manually.", null); //$NON-NLS-1$
				System.err.println("Unable to rename old external tool state file. Please rename externaltools.xml to oldexternaltools.xml manually."); //$NON-NLS-1$
			}
			if (externalTools == null || externalTools.size() == 0) {
				return new ExternalTool[0];
			} else {
				ExternalTool[] results = new ExternalTool[externalTools.size()];
				externalTools.toArray(results);
				return results;
			}
		} else {
			return null;
		}
	}
	
	/**
	 * Creates an external tool from the map.
	 */
	public static ExternalTool toolFromArgumentMap(Map args, ExternalToolRegistry reg, String newName) {
		// Update the type...
		String type = (String)args.get(TAG_TOOL_TYPE);
		if (TOOL_TYPE_ANT.equals(type))
			type = IExternalToolConstants.TOOL_TYPE_ANT_BUILD;
		else
			type = IExternalToolConstants.TOOL_TYPE_PROGRAM;
		
		// Update the name...
		String name = (String)args.get(TAG_TOOL_NAME);
		name= name.replace('/', '.');
		if (name.charAt(0) == ('.')) {
			name= name.substring(1);
		}
		if (ExternalTool.validateToolName(name) != null)
			name = newName;
		if (reg != null && reg.hasToolNamed(name))
			name = newName;

		// Update the location...
		String location = (String)args.get(TAG_TOOL_LOCATION);
		if (location != null) {
			ToolUtil.VariableDefinition varDef = ToolUtil.extractVariableTag(location, 0);
			if (IExternalToolConstants.VAR_WORKSPACE_LOC.equals(varDef.name))
				location = ToolUtil.buildVariableTag(IExternalToolConstants.VAR_RESOURCE_LOC, varDef.argument);
		}

		// Update the refresh scope...
		String refresh = (String)args.get(TAG_TOOL_REFRESH);
		if (refresh != null) {
			ToolUtil.VariableDefinition varDef = ToolUtil.extractVariableTag(refresh,  0);
			if ("none".equals(varDef.name)) //$NON-NLS-1$
				refresh = null;
		}
		
		// Update the arguments
		String arguments = (String)args.get(TAG_TOOL_ARGUMENTS);
		String targetNames = null;
		if (arguments != null) {
			int start = 0;
			ArrayList targets = new ArrayList();
			StringBuffer buffer = new StringBuffer();
			ToolUtil.VariableDefinition varDef = ToolUtil.extractVariableTag(arguments, start);
			while (varDef.end != -1) {
				if ("ant_target".equals(varDef.name) && varDef.argument != null) { //$NON-NLS-1$
					targets.add(varDef.argument);
					buffer.append(arguments.substring(start, varDef.start));
				} else {
					buffer.append(arguments.substring(start, varDef.end));
				}
				start = varDef.end;
				varDef = ToolUtil.extractVariableTag(arguments, start);
			}
			buffer.append(arguments.substring(start, arguments.length()));
			arguments = buffer.toString();
			
			buffer.setLength(0);
			for (int i = 0; i < targets.size(); i++) {
				String target = (String)targets.get(i);
				if (target != null && target.length() > 0) {
					buffer.append(target);
					buffer.append(","); //$NON-NLS-1$
				}
			}
			targetNames = buffer.toString();
		}
		
		// Collect the rest of the information
		String sShowLog = (String)args.get(TAG_TOOL_SHOW_LOG);
		boolean showLog = TRUE.equals(sShowLog);

		String sBlock = (String)args.get(TAG_TOOL_BLOCK);
		boolean block = !(FALSE.equals(sBlock));
			
		try {
			ExternalTool tool = new ExternalTool(type, name);
			tool.setLocation(location);
			tool.setArguments(arguments);
			tool.setWorkingDirectory((String)args.get(TAG_TOOL_DIRECTORY));
			tool.setRefreshScope(refresh);
			tool.setCaptureOutput(showLog);
			tool.setShowConsole(showLog);
			tool.setRunInBackground(!block);
			tool.setRunForBuildKinds(toBuildTypesArray((String)args.get(TAG_TOOL_BUILD_TYPES)));
			if (targetNames != null && targetNames.length() > 0) {
				String key = IExternalToolConstants.TOOL_TYPE_ANT_BUILD + ".runTargets"; //$NON-NLS-1$;
				tool.setExtraAttribute(key, targetNames);
			}
			return tool;
		} catch (CoreException e) {
			return null;
		}
	}

	/**
	 * Translates a single String representation of build types
	 * used for storage in an argument map to an array of build 
	 * types.
	 */
	private static int[] toBuildTypesArray(String string) {
		if (string == null)
			return null;
		return ExternalToolRegistry.buildTypesToArray(string);
	}
}
