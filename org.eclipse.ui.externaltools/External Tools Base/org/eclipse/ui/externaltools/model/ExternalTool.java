package org.eclipse.ui.externaltools.model;

/**********************************************************************
Copyright (c) 2002 IBM Corp. and others. All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
 
Contributors:
**********************************************************************/

import java.text.MessageFormat;
import java.util.ArrayList;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IActionFilter;
import org.eclipse.ui.externaltools.internal.model.ExternalToolsPlugin;
import org.eclipse.ui.model.IWorkbenchAdapter;

/**
 * This class represents an external tool that can be run. The tool
 * can be inside or outside the workspace.
 * <p>
 * An external tool consist of a user defined name, the location
 * of the tool, optional arguments for the tool, and the working
 * directory.
 * </p><p>
 * After the tool has run, part or all of the workspace can be
 * refreshed to pickup changes made by the tool. This is optional
 * and does nothing by default
 * </p><p>
 * This class is not intended to be extended by clients
 * </p>
 */
public final class ExternalTool implements IAdaptable {
	private static final String EMPTY_STRING = ""; //$NON-NLS-1$
	private static final int[] DEFAULT_BUILD_KINDS = 
		{IncrementalProjectBuilder.INCREMENTAL_BUILD,
		IncrementalProjectBuilder.FULL_BUILD,
		IncrementalProjectBuilder.AUTO_BUILD};
	
	private static final ToolWorkbenchAdapter workbenchAdapter = new ToolWorkbenchAdapter();
	private static final ToolFilterAdapter filterAdapter = new ToolFilterAdapter();
	
	private String type = EMPTY_STRING;
	private String name = EMPTY_STRING;
	private String location = EMPTY_STRING;
	private String arguments = EMPTY_STRING;
	private String workDirectory = EMPTY_STRING;
	private String description = EMPTY_STRING;
	private String openPerspective = null;
	private String refreshScope = null;
	private boolean refreshRecursive = true;
	private boolean captureOutput = true;
	private boolean showConsole = true;
	private boolean runInBackground = true;
	private boolean promptForArguments = false;
	private boolean showInMenu = false;
	private boolean saveDirtyEditors = false;
	private int[] runForBuildKinds = DEFAULT_BUILD_KINDS;
	private ArrayList extraAttributes = null;
	
	/**
	 * Creates a fully initialized external tool.
	 * 
	 * @param type the type of external tool.
	 * @param name the name given to the external tool. Must only
	 * 		contain letters, numbers, hyphens, and spaces.
	 */
	public ExternalTool(String type, String name) throws CoreException {
		super();

		if (type != null)
			this.type = type;

		String errorText = validateToolName(name);
		if (errorText == null)
			this.name = name.trim();
		else
			throw ExternalToolsPlugin.newError(errorText, null);
	}

	/**
	 * Validates the specified tool name only includes letters,
	 * numbers, hyphens, and spaces. Must contain at least one
	 * letter or number.
	 * 
	 * @param name the proposed name for the external tool
	 * @return a string indicating the invalid format or <code>null</code> if valid.
	 */
	public static String validateToolName(String name) {
		IStatus status = ResourcesPlugin.getWorkspace().validateName(name, IResource.FILE);
		if (status.getCode() != IStatus.OK) {
			return status.getMessage();								
		}
		return null;
	}

	/* (non-Javadoc)
	 * Method declared on IAdaptable.
	 */
	public Object getAdapter(Class adapter) {
		if (adapter == IWorkbenchAdapter.class)
			return workbenchAdapter;
		
		if (adapter == IActionFilter.class)
			return filterAdapter;
		
		return null;
	}

	/**
	 * Returns the extra attribute value
	 * 
	 * @param key the unique attribute name
	 * @return the value of the attribute, or <code>null</code>
	 * 		if not such attribute name.
	 */
	public String getExtraAttribute(String key) {
		if (key == null || key.length() == 0)
			return null;
		if (extraAttributes == null)
			return null;

		for (int i = 0; i < extraAttributes.size(); i++) {
			Attribute attr = (Attribute)extraAttributes.get(i);
			if (attr.key.equals(key))
				return attr.value;
		}

		return null;
	}
	
	/**
	 * Returns all the extra attribute keys
	 */
	public String[] getExtraAttributeKeys() {
		if (extraAttributes == null)
			return new String[0];

		String[] results = new String[extraAttributes.size()];
		for (int i = 0; i < extraAttributes.size(); i++) {
			Attribute attr = (Attribute)extraAttributes.get(i);
			results[i] = attr.key;
		}

		return results;
	}
	
	/**
	 * Returns all the extra attribute values
	 */
	public String[] getExtraAttributeValues() {
		if (extraAttributes == null)
			return new String[0];

		String[] results = new String[extraAttributes.size()];
		for (int i = 0; i < extraAttributes.size(); i++) {
			Attribute attr = (Attribute)extraAttributes.get(i);
			results[i] = attr.value;
		}

		return results;
	}
	
	/**
	 * Sets an extra attribute to the tool.
	 * 
	 * @param key the unique attribute name
	 * @param value the value for the attribute. If <code>null</code>,
	 * 		then the existing attribute is removed.
	 */
	public void setExtraAttribute(String key, String value) {
		// Exit on invalid key
		if (key == null || key.length() == 0)
			return;
		
		// Init the list but only if not attempting to remove
		// an extra attribute
		if (extraAttributes == null) {
			if (value == null)
				return;
			else
				extraAttributes = new ArrayList(4);
		}
		
		// If the extra attribute exist, update it with the
		// new value, or remove it if the value is null	
		for (int i = 0; i < extraAttributes.size(); i++) {
			Attribute attr = (Attribute)extraAttributes.get(i);
			if (attr.key.equals(key)) {
				if (value == null)
					extraAttributes.remove(i);
				else
					attr.value = value.trim();
				return;
			}
		}

		// Otherwise add the new extra attribute
		if (value != null)
			extraAttributes.add(new Attribute(key, value.trim()));
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
	 * Returns the location of the external tool.
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
		return workDirectory;
	}
	
	/**
	 * Returns whether the refresh will be recursive.
	 */
	public boolean getRefreshRecursive() {
		return refreshRecursive;
	}
	
	/**
	 * Returns the scope of resources to refresh after
	 * the external tool is run, or <code>null</code> if
	 * not specified. The value is in an variable
	 * format.
	 */
	public String getRefreshScope() {
		return refreshScope;
	}
	
	/**
	 * Returns whether to capture output messages from the 
	 * running tool.
	 */
	public boolean getCaptureOutput() {
		return captureOutput;	
	}

	/**
	 * Returns whether to show the log console when
	 * the tool is run.
	 */
	public boolean getShowConsole() {
		return showConsole;	
	}

	/**
	 * Returns whether to run the external tool in the
	 * background so as not to block the UI.
	 */
	public boolean getRunInBackground() {
		return runInBackground;
	}
	
	/**
	 * Returns whether to prompt for arguments before
	 * the tool is run.
	 */
	public boolean getPromptForArguments() {
		return promptForArguments;
	}
	
	/**
	 * Returns whether to show this tool in the 
	 * Run > External Tools menu.
	 */
	public boolean getShowInMenu() {
		return showInMenu;	
	}
	
	/**
	 * Returns whether to save all dirty editors before
	 * running this tool.
	 */
	public boolean getSaveDirtyEditors() {
		return saveDirtyEditors;	
	}
	
	/**
	 * Returns the perspective ID to open when this
	 * tool is run, or <code>null</code> if not specified.
	 */
	public String getOpenPerspective() {
		return openPerspective;
	}

	/**
	 * Returns a description of this external tool
	 */
	public String getDescription() {
		return description;
	}
	
	/**
	 * Returns the list of build kinds that this
	 * tool wants to run when used as a builder.
	 * The list of valid build kinds is defined
	 * on <code>IncrementalProjectBuilder</code>.
	 */
	public int[] getRunForBuildKinds() {
		return runForBuildKinds;
	}
	
	/**
	 * Sets the name of the external tool.
	 */
	public void rename(String name) throws CoreException {
		IStatus status= ExternalToolsPlugin.getDefault().getToolRegistry(null).renameTool(this, name);
		if (!status.isOK()) {
			// Throw an exception
			ExternalToolsPlugin.newError(MessageFormat.format("An exception occurred attempting to rename the tool: ", new Object[] {this.getName()}), null);
		}
		if (name == null) {
			this.name = EMPTY_STRING;
		} else {
			this.name = name;
		}
	}
	
	/**
	 * Sets the location of the external tool.
	 */
	public void setLocation(String location) {
		if (location == null)
			this.location = EMPTY_STRING;
		else
			this.location = location.trim();
	}
	
	/**
	 * Sets the arguments for the external tool.
	 */
	public void setArguments(String arguments) {
		if (arguments == null)
			this.arguments = EMPTY_STRING;
		else
			this.arguments = arguments.trim();
	}
	
	/**
	 * Sets the working directory to run the external tool in.
	 */
	public void setWorkingDirectory(String workDirectory) {
		if (workDirectory == null)
			this.workDirectory = EMPTY_STRING;
		else
			this.workDirectory = workDirectory.trim();
	}
	
	/**
	 * Sets whether the refresh will be recursive.
	 */
	public void setRefreshRecursive(boolean refreshRecursive) {
		this.refreshRecursive = refreshRecursive;
	}
	
	/**
	 * Sets the scope of resources to refresh after
	 * the external tool is run, or <code>null</code>
	 * if none. The value is in a variable format.
	 */
	public void setRefreshScope(String refreshScope) {
		if (refreshScope == null)
			this.refreshScope = null;
		else {
			this.refreshScope = refreshScope.trim();
			if (this.refreshScope.length() == 0)
				this.refreshScope = null;
		}
	}
	
	/**
	 * Sets whether to capture output messages from the 
	 * running tool.
	 */
	public void setCaptureOutput(boolean captureOutput) {
		this.captureOutput = captureOutput;	
	}

	/**
	 * Sets whether to show the log console when
	 * the tool is run.
	 */
	public void setShowConsole(boolean showConsole) {
		this.showConsole = showConsole;	
	}

	/**
	 * Sets whether to run the external tool in the
	 * background so as not to block the UI.
	 */
	public void setRunInBackground(boolean runInBackground) {
		this.runInBackground = runInBackground;
	}
	
	/**
	 * Sets whether to prompt for arguments before
	 * the tool is run.
	 */
	public void setPromptForArguments(boolean promptForArguments) {
		this.promptForArguments = promptForArguments;
	}
	
	/**
	 * Sets whether to show this tool in the 
	 * Run > External Tools menu.
	 */
	public void setShowInMenu(boolean showInMenu) {
		this.showInMenu = showInMenu;
	}
	
	/**
	 * Sets whether all dirty editors will be saved
	 * before running this tool.
	 */
	public void setSaveDirtyEditors(boolean saveDirtyEditors) {
		this.saveDirtyEditors = saveDirtyEditors;	
	}
	
	/**
	 * Sets the perspective ID to open when this
	 * tool is run, or <code>null</code> if not specified.
	 */
	public void setOpenPerspective(String openPerspective) {
		if (openPerspective == null)
			this.openPerspective = null;
		else {
			this.openPerspective = openPerspective.trim();
			if (this.openPerspective.length() == 0)
				this.openPerspective = null;
		}
	}

	/**
	 * Sets a description of this external tool
	 */
	public void setDescription(String description) {
		if (description == null)
			this.description = EMPTY_STRING;
		else
			this.description = description.trim();
	}
	
	/**
	 * Sets the list of build kinds that this
	 * tool wants to run when used as a builder.
	 * The list of valid build kinds is defined
	 * on <code>IncrementalProjectBuilder</code>.
	 */
	public void setRunForBuildKinds(int[] kinds) {
		if (kinds == null)
			this.runForBuildKinds = DEFAULT_BUILD_KINDS;
		else
			this.runForBuildKinds = kinds;
	}

	/**
	 * Internal representation of extra attributes.
	 */
	private static class Attribute {
		public String key;
		public String value;
		
		public Attribute(String key, String value) {
			super();
			this.key = key;
			this.value = value;
		}
	}
	
	/**
	 * Internal workbench adapter implementation.
	 */
	private static class ToolWorkbenchAdapter implements IWorkbenchAdapter {
		public Object[] getChildren(Object o) {
			return new Object[0];
		}
		
		public ImageDescriptor getImageDescriptor(Object o) {
			String type = ((ExternalTool)o).getType();
			return ExternalToolsPlugin.getDefault().getTypeRegistry().getToolTypeImageDescriptor(type);
		}

		public String getLabel(Object o) {
			return ((ExternalTool)o).getName();
		}
		
		public Object getParent(Object o) {
			String type = ((ExternalTool)o).getType();
			return ExternalToolsPlugin.getDefault().getTypeRegistry().getToolType(type);
		}
	}
	
	/**
	 * Internal action filter adapter implementation.
	 */
	private static class ToolFilterAdapter implements IExternalToolFilter {
		public boolean testAttribute(Object target, String name, String value) {
			ExternalTool tool = (ExternalTool) target;

			if (IExternalToolFilter.TYPE.equals(name))
				return tool.getType().equals(value);

			String attrValue = tool.getExtraAttribute(name);
			if (attrValue != null)
				return attrValue.equals(value);

			return false;
		}
	}
}