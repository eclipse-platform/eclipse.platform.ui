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
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.XMLMemento;
import org.eclipse.ui.externaltools.internal.model.ExternalToolsPlugin;
import org.eclipse.ui.externaltools.internal.model.ToolMessages;
import org.eclipse.ui.externaltools.model.ExternalTool;
import org.eclipse.ui.externaltools.model.IExternalToolConstants;

/**
 * The registry of available external tools.
 */
public class ExternalToolRegistry {
	// Format for a tool looks like:
	// <externalTool
	//		type={string}
	//		name={string}
	//		location={string:path}
	//		workDirectory={string:path}
	//		logMessages={string:true/false}
	//		runInBackground={string:true/false}
	//		promptForArguments={string:true/false}
	//		showInMenu={string:true/false}
	//		openPerspective={string:id}
	//		refreshScope={string}
	//		refreshRecursive={string: true/false}>
	//		<description>{string}</description}
	//		<arguments>{string}</arguments}
	//		<extraAttribute
	//			key={String}>
	//			{String}
	//		</extraAttribute>
	// </externalTool>
	//
	// Element and attribute tags for storing a tool in an XML file.
	private static final String TAG_EXTERNAL_TOOL = "externalTool"; //$NON-NLS-1$
	private static final String TAG_TYPE = "type"; //$NON-NLS-1$
	private static final String TAG_NAME = "name"; //$NON-NLS-1$
	private static final String TAG_LOCATION = "location"; //$NON-NLS-1$
	private static final String TAG_WORK_DIR = "workDirectory"; //$NON-NLS-1$
	private static final String TAG_CAPTURE_OUTPUT = "captureOutput"; //$NON-NLS-1$
	private static final String TAG_SHOW_CONSOLE = "showConsole"; //$NON-NLS-1$
	private static final String TAG_RUN_BKGRND = "runInBackground"; //$NON-NLS-1$
	private static final String TAG_OPEN_PERSP = "openPerspective"; //$NON-NLS-1$
	private static final String TAG_PROMPT_ARGS = "promptForArguments"; //$NON-NLS-1$
	private static final String TAG_SHOW_MENU = "showInMenu"; //$NON-NLS-1$
	private static final String TAG_SAVE_DIRTY = "saveDirtyEditors"; //$NON-NLS-1$
	private static final String TAG_DESC = "description"; //$NON-NLS-1$
	private static final String TAG_ARGS = "arguments"; //$NON-NLS-1$
	private static final String TAG_REFRESH_SCOPE = "refreshScope"; //$NON-NLS-1$
	private static final String TAG_REFRESH_RECURSIVE = "refreshRecursive"; //$NON-NLS-1$
	private static final String TAG_RUN_BUILD_KINDS = "runForBuildKinds"; //$NON-NLS-1$
	private static final String TAG_EXTRA_ATTR = "extraAttribute"; //$NON-NLS-1$
	private static final String TAG_KEY = "key"; //$NON-NLS-1$
	private static final String TAG_VERSION = "version"; //$NON-NLS-1$

	// Possible values for boolean type of attributes	
	private static final String TRUE = "true"; //$NON-NLS-1$
	private static final String FALSE = "false"; //$NON-NLS-1$

	private static final String BUILD_TYPE_SEPARATOR = ","; //$NON-NLS-1$
	private static final String EXTRA_ATTR_SEPARATOR = "="; //$NON-NLS-1$
	
	private static final String VERSION_21 = "2.1"; //$NON-NLS-1$;

	private static final ExternalTool[] EMPTY_TOOLS = new ExternalTool[0];
	
	/**
	 * Path to where the user defined external tools
	 * are stored within the workspace.
	 */
	private static final IPath TOOLS_PATH =
		ExternalToolsPlugin.getDefault().getStateLocation().append(".xtools"); //$NON-NLS-1$

	/**
	 * Extension for external tool files stored within
	 * the workspace
	 */
	private static final String TOOLS_EXTENSION = ".xtool"; //$NON-NLS-1$
	
	/**
	 * Lookup table of external tools where the key is the
	 * type of tool, and the value is a array list of tools of
	 * that type.
	 */
	private HashMap tools = new HashMap();
	
	/**
	 * Lookup table of file names where the key is the external
	 * tool name as <b>lowercase</b>, and the value is the full path
	 * to the file for that tool
	 */
	private HashMap filenames = new HashMap();
	
	/**
	 * Creates the registry and loads the external tools
	 * from storage.
	 * 
	 * @param shell the shell to use for displaying any errors
	 * 		when loading external tool definitions from storage
	 * 		or <code>null</code> to not report these problems.
	 */
	public ExternalToolRegistry(final Shell shell) {
		super();
		final IStatus results = loadTools();
		if (!results.isOK() && shell != null && !shell.isDisposed()) {
			shell.getDisplay().syncExec(new Runnable() {
				public void run() {
					String title = ToolMessages.getString("ExternalToolRegistry.loadErrorTitle"); //$NON-NLS-1$
					String msg = ToolMessages.getString("ExternalToolRegistry.loadErrorMessage"); //$NON-NLS-1$
					ErrorDialog.openError(shell, title, msg, results);
				}
			});
		}
		ExternalTool[] oldTools = ExternalToolMigration.readInOldTools(this);
		if (oldTools != null) {
			for (int i = 0; i < oldTools.length; i++) {
				saveTool(oldTools[i]);
			}
		}
	}

	/**
	 * Converts the build kinds into a built types
	 * string representation.
	 * 
	 * @param buildKinds the array of build kinds to convert
	 * @return the build types string representation
	 */
	protected static String buildKindsToString(int[] buildKinds) {
		if (buildKinds.length == 0)
			return IExternalToolConstants.BUILD_TYPE_NONE;
			
		StringBuffer buffer = new StringBuffer();
		for (int i = 0; i < buildKinds.length; i++) {
			switch (buildKinds[i]) {
				case IncrementalProjectBuilder.INCREMENTAL_BUILD :
					buffer.append(IExternalToolConstants.BUILD_TYPE_INCREMENTAL);
					break;
				case IncrementalProjectBuilder.FULL_BUILD :
					buffer.append(IExternalToolConstants.BUILD_TYPE_FULL);
					break;
				case IncrementalProjectBuilder.AUTO_BUILD :
					buffer.append(IExternalToolConstants.BUILD_TYPE_AUTO);
					break;
				default :
					break;
			}
			buffer.append(BUILD_TYPE_SEPARATOR);
		}
		return buffer.toString();
	}
	
	/**
	 * Converts the build types string into an array of
	 * build kinds.
	 * 
	 * @param buildTypes the string of built types to convert
	 * @return the array of build kinds.
	 */
	protected static int[] buildTypesToArray(String buildTypes) {
		int count = 0;
		boolean incremental = false;
		boolean full = false;
		boolean auto = false;
		
		StringTokenizer tokenizer = new StringTokenizer(buildTypes, BUILD_TYPE_SEPARATOR);
		while (tokenizer.hasMoreTokens()) {
			String token = tokenizer.nextToken();
			if (IExternalToolConstants.BUILD_TYPE_INCREMENTAL.equals(token)) {
				if (!incremental) {
					incremental = true;
					count++;
				}
			}
			else if (IExternalToolConstants.BUILD_TYPE_FULL.equals(token)) {
				if (!full) {
					full = true;
					count++;
				}
			}
			else if (IExternalToolConstants.BUILD_TYPE_AUTO.equals(token)) {
				if (!auto) {
					auto = true;
					count++;
				}
			}
		}
		
		int[] results = new int[count];
		count = 0;
		if (incremental) {
			results[count] = IncrementalProjectBuilder.INCREMENTAL_BUILD;
			count++;
		}
		if (full) {
			results[count] = IncrementalProjectBuilder.FULL_BUILD;
			count++;
		}
		if (auto) {
			results[count] = IncrementalProjectBuilder.AUTO_BUILD;
			count++;
		}
		
		return results;
	}

	/**
	 * Creates an external tool from the map.
	 * 
	 * @param commandArgs the builder ICommand arguments
	 * @param newName a new name for the tool if the one in the command is invalid
	 * @return the new external tool or <code>null</code> if not possible.
	 */
	public static ExternalTool toolFromBuildCommandArgs(Map commandArgs, String newName) {
		String version = (String) commandArgs.get(TAG_VERSION);
		if (VERSION_21.equals(version)) {
			String name = (String)commandArgs.get(TAG_NAME);
			if (ExternalTool.validateToolName(name) != null)
				name = newName;
			String type = (String)commandArgs.get(TAG_TYPE);
	
			try {
				ExternalTool tool = new ExternalTool(type, name);
				tool.setLocation((String)commandArgs.get(TAG_LOCATION));
				tool.setWorkingDirectory((String)commandArgs.get(TAG_WORK_DIR));
				tool.setCaptureOutput(TRUE.equals((String)commandArgs.get(TAG_CAPTURE_OUTPUT)));
				tool.setShowConsole(TRUE.equals((String)commandArgs.get(TAG_SHOW_CONSOLE)));
				tool.setRunInBackground(TRUE.equals((String)commandArgs.get(TAG_RUN_BKGRND)));
				tool.setPromptForArguments(TRUE.equals((String)commandArgs.get(TAG_PROMPT_ARGS)));
				tool.setShowInMenu(TRUE.equals((String)commandArgs.get(TAG_SHOW_MENU)));
				tool.setOpenPerspective((String)commandArgs.get(TAG_OPEN_PERSP));
				tool.setRefreshScope((String)commandArgs.get(TAG_REFRESH_SCOPE));
				tool.setRefreshRecursive(TRUE.equals((String)commandArgs.get(TAG_REFRESH_RECURSIVE)));
				tool.setSaveDirtyEditors(TRUE.equals((String)commandArgs.get(TAG_SAVE_DIRTY)));
				
				String types = (String)commandArgs.get(TAG_RUN_BUILD_KINDS);
				if (types != null && types.length() > 0)
					tool.setRunForBuildKinds(buildTypesToArray(types));
				
				String desc = (String)commandArgs.get(TAG_DESC);
				if (desc != null)
					tool.setDescription(desc);
	
				String args = (String)commandArgs.get(TAG_ARGS);
				if (args != null)
					tool.setArguments(args);
				
				String extraAttributes = (String)commandArgs.get(TAG_EXTRA_ATTR);
				if (extraAttributes != null) {
					StringTokenizer tokenizer = new StringTokenizer(extraAttributes, EXTRA_ATTR_SEPARATOR);
					while (tokenizer.hasMoreTokens()) {
						String key = tokenizer.nextToken();
						if (!tokenizer.hasMoreTokens())
							break;
						String value = tokenizer.nextToken();
						tool.setExtraAttribute(key, value);
					}
				}
				
				return tool;
			} catch (CoreException e) {
				return null;
			}
		} else {
			return ExternalToolMigration.toolFromArgumentMap(commandArgs, null, newName);
		}
	}
	
	/**
	 * Creates an builder ICommand argument map for the external tool.
	 * 
	 * @param tool the external tool to use
	 * @return the map of arguments representing the external tool.
	 */
	public static Map toolToBuildCommandArgs(ExternalTool tool) {
		Map commandArgs = new HashMap();
		commandArgs.put(TAG_VERSION, VERSION_21);
		commandArgs.put(TAG_TYPE, tool.getType());
		commandArgs.put(TAG_NAME, tool.getName());
		commandArgs.put(TAG_LOCATION, tool.getLocation());
		commandArgs.put(TAG_WORK_DIR, tool.getWorkingDirectory());
		commandArgs.put(TAG_CAPTURE_OUTPUT, tool.getCaptureOutput() ? TRUE : FALSE);
		commandArgs.put(TAG_SHOW_CONSOLE, tool.getShowConsole() ? TRUE : FALSE);
		commandArgs.put(TAG_RUN_BKGRND, tool.getRunInBackground() ? TRUE : FALSE);
		commandArgs.put(TAG_PROMPT_ARGS, tool.getPromptForArguments() ? TRUE : FALSE);
		commandArgs.put(TAG_SHOW_MENU, tool.getShowInMenu() ? TRUE : FALSE);
		commandArgs.put(TAG_OPEN_PERSP, tool.getOpenPerspective());
		commandArgs.put(TAG_REFRESH_SCOPE, tool.getRefreshScope());
		commandArgs.put(TAG_REFRESH_RECURSIVE, tool.getRefreshRecursive() ? TRUE : FALSE);
		commandArgs.put(TAG_SAVE_DIRTY, tool.getSaveDirtyEditors() ? TRUE : FALSE);
		commandArgs.put(TAG_RUN_BUILD_KINDS, buildKindsToString(tool.getRunForBuildKinds()));
		commandArgs.put(TAG_DESC, tool.getDescription());
		commandArgs.put(TAG_ARGS, tool.getArguments());

		String[] keys = tool.getExtraAttributeKeys();
		if (keys.length > 0) {
			StringBuffer buffer = new StringBuffer();
			String[] values = tool.getExtraAttributeValues();
			for (int i = 0; i < keys.length; i++) {
				buffer.append(keys[i]);
				buffer.append(EXTRA_ATTR_SEPARATOR);
				buffer.append(values[i]);
			}
			commandArgs.put(TAG_EXTRA_ATTR, buffer.toString());
		}

		return commandArgs;
	}
	
	/**
	 * Adds an external tool to the in-memory registry.
	 * Note that no check for an existing tool with the
	 * same name is done.
	 */
	private void addTool(ExternalTool tool, IPath filePath) {
		ArrayList list = (ArrayList) tools.get(tool.getType());
		if (list == null) {
			list = new ArrayList(10);
			tools.put(tool.getType(), list);
		}
		list.add(tool);
		
		filenames.put(tool.getName().toLowerCase(), filePath);
	}
	
	/**
	 * Performs the necessary work to rename the given external tool
	 * in this tools registry. The tool's storage location is updated
	 * to a new location based on the given tool name and the registry's
	 * cache is updated. Note that this method does NOT update the "name"
	 * attribute of the given tool.
	 * 
	 * This method is intended to be called only by ExternalTool.rename(String)	 */
	public IStatus renameTool(ExternalTool tool, String newName) {
		IPath filename= (IPath) filenames.get(tool.getName().toLowerCase());
		if (filename == null) {
			String msg = MessageFormat.format("The file for tool {0} could not be found", new Object[] {tool.getName()});
			return ExternalToolsPlugin.newErrorStatus(msg, null);
		}
		IFile file= ResourcesPlugin.getWorkspace().getRoot().getFile(filename);
		IPath newPath= generateToolFilename(newName);
		try {
			file.move(newPath, false, true, null);
		} catch (CoreException exception) {
			String msg = MessageFormat.format("An exception occurred creating file {0}", new Object[] {newPath.toString()});
			return ExternalToolsPlugin.newErrorStatus(msg, null);
		}
		filenames.put(tool.getName().toLowerCase(), newPath);
		return ExternalToolsPlugin.OK_STATUS;
	}

	/**
	 * Deletes the external tool from storage and
	 * registry.
	 */
	public IStatus deleteTool(ExternalTool tool) {
		IPath filename = (IPath) filenames.get(tool.getName().toLowerCase());
		if (filename == null) {
			String msg = ToolMessages.getString("ExternalToolRegistry.noToolFilename"); //$NON-NLS-1$
			return ExternalToolsPlugin.newErrorStatus(msg, null);
		}
		
		if (!filename.toFile().delete()) {
			String msg = ToolMessages.format("ExternalToolRegistry.deleteToolFileFailed", new Object[] {filename.toOSString()}); //$NON-NLS-1$
			return ExternalToolsPlugin.newErrorStatus(msg, null);
		}
		
		filenames.remove(tool.getName().toLowerCase());
		
		ArrayList list = (ArrayList) tools.get(tool.getType());
		if (list != null)
			list.remove(tool);
			
		return ExternalToolsPlugin.OK_STATUS;
	}


	/**
	 * Generate a filename path to store the contents
	 * of the external tool of the specified name.
	 * 
	 * @param toolName a valid external tool name
	 * @return the <code>IPath</code> for the filename
	 */
	private IPath generateToolFilename(String toolName) {
		String filename = toolName.replace(' ', '_');
		return TOOLS_PATH.append(filename + TOOLS_EXTENSION);
	}

	/**
	 * Returns the number of external tools of the specified
	 * type.
	 */
	public int getToolCountOfType(String toolTypeId) {
		ArrayList list = (ArrayList) tools.get(toolTypeId);
		if (list == null)
			return 0;
		else
			return list.size();
	}

	/**
	 * Returns the external tool with the specified name.
	 * 
	 * @return the external tool with the specified name or
	 * 		<code>null</code> if none exist with that name.
	 */
	public ExternalTool getToolNamed(String name) {
		Iterator typeEnum = tools.values().iterator();
		while (typeEnum.hasNext()) {
			ArrayList list = (ArrayList) typeEnum.next();
			if (list != null && !list.isEmpty()) {
				Iterator toolEnum = list.iterator();
				while (toolEnum.hasNext()) {
					ExternalTool tool = (ExternalTool)toolEnum.next();
					if (tool.getName().equalsIgnoreCase(name))
						return tool;
				}
			}
		}
		
		return null;
	}
	
	/**
	 * Returns the external tools of the specified
	 * type.
	 */
	public ExternalTool[] getToolsOfType(String toolTypeId) {
		ArrayList list = (ArrayList) tools.get(toolTypeId);
		if (list == null)
			return EMPTY_TOOLS;
		ExternalTool[] results = new ExternalTool[list.size()];
		list.toArray(results);
		return results;
	}

	/**
	 * Returns whether the external tool with the specified name
	 * already exist in the in-memory registry.
	 * 
	 * @return <code>true</code> if found, <code>false</code> otherwise
	 */
	public boolean hasToolNamed(String name) {
		return filenames.get(name.toLowerCase()) != null;
	}
	
	/**
	 * Loads the external tools from storage and
	 * adds them to the registry.
	 * 
	 * @return a status containing any problems encountered.
	 */
	private IStatus loadTools() {
		String msg = ToolMessages.getString("ExternalToolRegistry.loadToolFailure"); //$NON-NLS-1$
		MultiStatus results = new MultiStatus(IExternalToolConstants.PLUGIN_ID, 0, msg, null);

		final File toolsPath = TOOLS_PATH.toFile();
		if (toolsPath.isDirectory()) {
			FilenameFilter filter = new FilenameFilter() {
				public boolean accept(File dir, String name) {
					return dir.equals(toolsPath) && name.endsWith(TOOLS_EXTENSION);
				}
			};
			
			String[] files = toolsPath.list(filter);
			for (int i = 0; i < files.length; i++) {
				IStatus status = loadTool(TOOLS_PATH.append(files[i]));
				if (status != null)
					results.add(status);
			}
		}
		
		return results;
	}
	
	/**
	 * Loads an external tool from storage.
	 */
	private IStatus loadTool(IPath filePath) {
		IStatus result = null;
		InputStreamReader reader = null;
		
		try {
			FileInputStream input = new FileInputStream(filePath.toFile());
			reader = new InputStreamReader(input, "utf-8"); //$NON-NLS-1$
			IPath basePath = filePath.removeLastSegments(1).addTrailingSeparator();
			XMLMemento memento = XMLMemento.createReadRoot(reader, basePath.toOSString());

			String type = memento.getString(TAG_TYPE);
			String name = memento.getString(TAG_NAME);
			ExternalTool tool = new ExternalTool(type, name);
			
			tool.setLocation(memento.getString(TAG_LOCATION));
			tool.setWorkingDirectory(memento.getString(TAG_WORK_DIR));
			tool.setCaptureOutput(TRUE.equals(memento.getString(TAG_CAPTURE_OUTPUT)));
			tool.setShowConsole(TRUE.equals(memento.getString(TAG_SHOW_CONSOLE)));
			tool.setRunInBackground(TRUE.equals(memento.getString(TAG_RUN_BKGRND)));
			tool.setPromptForArguments(TRUE.equals(memento.getString(TAG_PROMPT_ARGS)));
			tool.setShowInMenu(TRUE.equals(memento.getString(TAG_SHOW_MENU)));
			tool.setOpenPerspective(memento.getString(TAG_OPEN_PERSP));
			tool.setRefreshScope(memento.getString(TAG_REFRESH_SCOPE));
			tool.setRefreshRecursive(TRUE.equals(memento.getString(TAG_REFRESH_RECURSIVE)));
			tool.setSaveDirtyEditors(TRUE.equals(memento.getString(TAG_SAVE_DIRTY)));
			
			String types = memento.getString(TAG_RUN_BUILD_KINDS);
			if (types != null && types.length() > 0)
				tool.setRunForBuildKinds(buildTypesToArray(types));
			
			IMemento child = memento.getChild(TAG_DESC);
			if (child != null)
				tool.setDescription(child.getTextData());

			child = memento.getChild(TAG_ARGS);
			if (child != null)
				tool.setArguments(child.getTextData());
			
			IMemento[] attributes = memento.getChildren(TAG_EXTRA_ATTR);
			for (int i = 0; i < attributes.length; i++) {
				String key = attributes[i].getString(TAG_KEY);
				String value = attributes[i].getTextData();
				tool.setExtraAttribute(key, value);
			}
			
			addTool(tool, filePath);
		} catch (FileNotFoundException e) {
			String msg = e.getMessage();
			if (msg == null)
				msg = ToolMessages.getString("ExternalToolRegistry.fileNotFoundError"); //$NON-NLS-1$
			result = ExternalToolsPlugin.newErrorStatus(msg, e);
		} catch (IOException e) {
			String msg = e.getMessage();
			if (msg == null)
				msg = ToolMessages.getString("ExternalToolRegistry.ioLoadError"); //$NON-NLS-1$
			result = ExternalToolsPlugin.newErrorStatus(msg, e);
		} catch (CoreException e) {
			result = e.getStatus();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch(IOException e) {
					ExternalToolsPlugin.getDefault().log("Unable to close external tool storage reader.", e); //$NON-NLS-1$
				}
			}
		}
		
		return result;
	}
	
	/**
	 * Cause the registry to reload all the tools from storage.
	 * 
	 * @return a status containing any problems encountered.
	 */
	public IStatus reloadTools() {
		tools = new HashMap();
		return loadTools();
	}
	
	/**
	 * Save an external tool to storage. Adds it to the
	 * in-memory registry if new.
	 */
	public IStatus saveTool(ExternalTool tool) {
		boolean isNew = false;
		
		IPath filename = (IPath) filenames.get(tool.getName().toLowerCase());
		if (filename == null) {
			filename = generateToolFilename(tool.getName());
			isNew = true;
		}
		
		// Ensure the extenal tool storage directory exist
		TOOLS_PATH.toFile().mkdirs();
		
		IStatus results = storeTool(tool, filename);
		if (results != null)
			return results;

		if (isNew)
			addTool(tool, filename);
			
		return ExternalToolsPlugin.OK_STATUS;
	}

	/**
	 * Stores an external tool to storage.
	 */
	private IStatus storeTool(ExternalTool tool, IPath filePath) {
		IStatus result = null;

		// Populate the memento
		XMLMemento memento = XMLMemento.createWriteRoot(TAG_EXTERNAL_TOOL);
		memento.putString(TAG_VERSION, VERSION_21);
		memento.putString(TAG_TYPE, tool.getType());
		memento.putString(TAG_NAME, tool.getName());
		memento.putString(TAG_LOCATION, tool.getLocation());
		memento.putString(TAG_WORK_DIR, tool.getWorkingDirectory());
		memento.putString(TAG_CAPTURE_OUTPUT, tool.getCaptureOutput() ? TRUE : FALSE);
		memento.putString(TAG_SHOW_CONSOLE, tool.getShowConsole() ? TRUE : FALSE);
		memento.putString(TAG_RUN_BKGRND, tool.getRunInBackground() ? TRUE : FALSE);
		memento.putString(TAG_PROMPT_ARGS, tool.getPromptForArguments() ? TRUE : FALSE);
		memento.putString(TAG_SHOW_MENU, tool.getShowInMenu() ? TRUE : FALSE);
		memento.putString(TAG_OPEN_PERSP, tool.getOpenPerspective());
		memento.putString(TAG_REFRESH_SCOPE, tool.getRefreshScope());
		memento.putString(TAG_REFRESH_RECURSIVE, tool.getRefreshRecursive() ? TRUE : FALSE);
		memento.putString(TAG_SAVE_DIRTY, tool.getSaveDirtyEditors() ? TRUE : FALSE);
		memento.putString(TAG_RUN_BUILD_KINDS, buildKindsToString(tool.getRunForBuildKinds()));
		
		IMemento child = memento.createChild(TAG_DESC);
		if (child != null)
			child.putTextData(tool.getDescription());

		child = memento.createChild(TAG_ARGS);
		if (child != null)
			child.putTextData(tool.getArguments());

		String[] keys = tool.getExtraAttributeKeys();
		if (keys.length > 0) {
			String[] values = tool.getExtraAttributeValues();
			for (int i = 0; i < keys.length; i++) {
				child = memento.createChild(TAG_EXTRA_ATTR);
				if (child != null) {
					child.putString(TAG_KEY, keys[i]);
					child.putTextData(values[i]);
				}
			}
		}

		// Write the memento to the file	
		File toolFile = filePath.toFile();
		OutputStreamWriter writer = null;
		try {
			FileOutputStream stream = new FileOutputStream(toolFile);
			writer = new OutputStreamWriter(stream, "utf-8"); //$NON-NLS-1$
			memento.save(writer);
		} catch (IOException e) {
			toolFile.delete();
			String msg = e.getMessage();
			if (msg == null)
				msg = ToolMessages.getString("ExternalToolRegistry.ioSaveError"); //$NON-NLS-1$
			result = ExternalToolsPlugin.newErrorStatus(msg, e);
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch(IOException e) {
					ExternalToolsPlugin.getDefault().log("Unable to close external tool storage writer.", e); //$NON-NLS-1$
				}
			}
			if (result != null)
				toolFile.delete();
		}

		return result;
	}
}
