package org.eclipse.ui.externaltools.internal.core;

/**********************************************************************
Copyright (c) 2002 IBM Corp. and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v0.5
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v05.html
 
Contributors:
**********************************************************************/
import java.util.ArrayList;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.*;
import org.eclipse.ui.externaltools.internal.ui.*;

/**
 * General utility class dealing with external tools
 */
public final class ToolUtil {
	/**
	 * Variable tag indentifiers
	 */
	/*package*/ static final String VAR_TAG_START = "${"; //$NON-NLS-1$
	/*package*/ static final String VAR_TAG_END = "}"; //$NON-NLS-1$
	/*package*/ static final String VAR_TAG_SEP = ":"; //$NON-NLS-1$

	/**
	 * Build types (what type of build is occuring when a tool is run)
	 */
	public static final String BUILD_TYPE_INCREMENTAL = "incremental"; //$NON-NLS-1$
	public static final String BUILD_TYPE_FULL = "full"; //$NON-NLS-1$
	public static final String BUILD_TYPE_AUTO = "auto"; //$NON-NLS-1$
	public static final String BUILD_TYPE_NONE = "none"; //$NON-NLS-1$
	
	private static final ToolUtil instance = new ToolUtil();
	
	/**
	 * No instances allowed
	 */
	private ToolUtil() {
		super();
	}

	/**
	 * Builds a variable tag that will be auto-expanded before
	 * the tool is run.
	 * 
	 * @param varName the name of a known variable (one of the VAR_* constants)
	 * @param varArgument an optional argument for the variable, <code>null</code> if none
	 */
	public static String buildVariableTag(String varName, String varArgument) {
		StringBuffer buf = new StringBuffer();
		buildVariableTag(varName,varArgument, buf);
		return buf.toString();
	}
	
	/**
	 * Builds a variable tag that will be auto-expanded before
	 * the tool is run.
	 * 
	 * @param varName the name of a known variable (one of the VAR_* constants)
	 * @param varArgument an optional argument for the variable, <code>null</code> if none
	 * @param buffer the buffer to write the constructed variable tag
	 */
	public static void buildVariableTag(String varName, String varArgument, StringBuffer buffer) {
		buffer.append(VAR_TAG_START);
		buffer.append(varName);
		if (varArgument != null && varArgument.length() > 0) {
			buffer.append(VAR_TAG_SEP);
			buffer.append(varArgument);
		}
		buffer.append(VAR_TAG_END);
	}
	
	/**
	 * Builds a variable tag for each argument that will be auto-expanded before
	 * the tool is run.
	 * 
	 * @param varName the name of a known variable (one of the VAR_* constants)
	 * @param varArguments a list of arguments for each variable
	 * @param buffer the buffer to write the constructed variable tags
	 */
	public static void buildVariableTags(String varName, String[] varArguments, StringBuffer buffer) {
		for (int i = 0; i < varArguments.length; i++) {
			buffer.append(" "); // $NON-NLS-1$
			buildVariableTag(varName, varArguments[i], buffer);
		}
	}
	
	/**
	 * Extracts from the source text the variable tag's name
	 * and argument.
	 * 
	 * @param text the source text to parse for a variable tag
	 * @param start the index in the string to start the search
	 * @return the variable definition
	 */
	public static VariableDefinition extractVariableTag(String text, int start) {
		VariableDefinition varDef = instance.new VariableDefinition();
		
		varDef.start = text.indexOf(VAR_TAG_START, start);
		if (varDef.start < 0)
			return varDef;
		start = varDef.start + VAR_TAG_START.length();
		
		int end = text.indexOf(VAR_TAG_END, start);
		if (end < 0)
			return varDef;
		varDef.end = end + VAR_TAG_END.length();
		if (end == start)
			return varDef;
	
		int mid = text.indexOf(VAR_TAG_SEP, start);
		if (mid < 0 || mid > end) {
			varDef.name = text.substring(start, end);
		} else {
			if (mid > start)
				varDef.name = text.substring(start, mid);
			mid = mid + VAR_TAG_SEP.length();
			if (mid < end)
				varDef.argument = text.substring(mid, end);
		}
		
		return varDef;
	}
	
	/**
	 * Extracts all arguments of the specified variable tag name.
	 * Places the remaining text on the buffer.
	 * 
	 * @param text the text to parse for variable tags of the specified name
	 * @param varName the name of the variable tag to extract its argument
	 * @param buffer the buffer to write the rest of the text
	 * @return an array of arguments for the variable tag name specified
	 */
	public static String[] extractVariableArguments(String text, String varName, StringBuffer buffer) {
		ArrayList results = new ArrayList();

		int start = 0;
		while (true) {
			VariableDefinition varDef = extractVariableTag(text, start);
			
			if (varDef.start == -1) {
				if (start == 0)
					buffer.append(text);
				else
					buffer.append(text.substring(start));
				break;
			} else if (varDef.start > start) {
				buffer.append(text.substring(start, varDef.start));
			}

			if (varDef.end == -1) {
				buffer.append(text.substring(varDef.start));
				break;
			}

			if (varName.equals(varDef.name)) {
				if (varDef.argument != null)
					results.add(varDef.argument);
			} else {
				buffer.append(text.substring(varDef.start, varDef.end));
			}
			
			start = varDef.end;
		}
		
		String[] args = new String[results.size()];
		results.toArray(args);
		return args;
	}
	
	/**
	 * Clears the log messages recorded so far.
	 */
	public static void clearLogDocument() {
		LogConsoleDocument.getInstance().clearOutput();
	}

	/**
	 * Returns the physical location of a resource, given a string of the form
	 * VAR_TAG_START + ExternalTool.VAR_WORKSPACE_LOC + VAR_TAG_SEP + 
	 * full path of a resource + VAR_TAG_END, or a string that is already
	 * a resource location.
	 * 
	 * @param contents the contents of a EditDialog location field.
	 * @return the location of the resource, or null if the resource
	 * is not found.
	 */
	public static String getLocationFromText(String contents) {
		VariableDefinition varDef = extractVariableTag(contents, 0);
		if (varDef.start >= 0 && ExternalTool.VAR_WORKSPACE_LOC.equals(varDef.name))
			if (varDef.argument != null && varDef.argument.length() > 0) {
				return getLocationFromFullPath(varDef.argument);			
			} else {
				return Platform.getLocation().toOSString() + contents.substring(varDef.end);
			}
		else
			return contents;
	}
	
	/**
	 * Returns the physical location of a resource, given the full path of the
	 * resource (with respect to the workspace).
	 * 
	 * @param fullPath the full path of the resource.
	 * @return the location of the resource, or null if the resource is not found.
	 */
	public static String getLocationFromFullPath(String fullPath) {
		IResource member = ResourcesPlugin.getWorkspace().getRoot().findMember(fullPath);
		if (member != null)
			return member.getLocation().toOSString();
		else
			return null;
	}
	
	/**
	 * Returns the tool runner for the specified
	 * type, or <code>null</code> if none registered.
	 */
	public static ExternalToolsRunner getRunner(String type) {
		if (ExternalTool.TOOL_TYPE_PROGRAM.equals(type))
			return new ProgramRunner();
		if (ExternalTool.TOOL_TYPE_ANT.equals(type))
			return new AntFileRunner();
		return null;
	}
	
	/**
	 * Saves any dirty editors if user preference
	 */
	public static void saveDirtyEditors(IWorkbenchWindow window) {
		IPreferenceStore store = ExternalToolsPlugin.getDefault().getPreferenceStore();
		boolean autoSave = store.getBoolean(IPreferenceConstants.AUTO_SAVE);
		if (autoSave) {
			IWorkbenchWindow[] windows = window.getWorkbench().getWorkbenchWindows();
			for (int i=0; i < windows.length; i++) {
				IWorkbenchPage[] pages = windows[i].getPages();
				for (int j = 0; j < pages.length; j++) {
					pages[j].saveAllEditors(false);
				}
			}
		}
	}
	
	/**
	 * Forces the log console view to open. Returns the view
	 * part if successful, otherwise <code>null</code>.
	 */
	public static LogConsoleView showLogConsole(IWorkbenchWindow window) {
		IWorkbenchPage page = window.getActivePage();
		LogConsoleView console = null;
		try {
			if (page != null) {
				console= (LogConsoleView)page.findView(ExternalToolsPlugin.LOG_CONSOLE_ID);
				if(console == null) {
					IWorkbenchPart activePart= page.getActivePart();
					page.showView(ExternalToolsPlugin.LOG_CONSOLE_ID);
					//restore focus stolen by the creation of the console
					page.activate(activePart);
				} else {
					page.bringToTop(console);
				}
			}
		} catch (PartInitException e) {
			ExternalToolsPlugin.getDefault().getLog().log(e.getStatus());
		}
		return console;
	}
	
	/**
	 * Returns whether or not the given string contains at least one space.
	 * 
	 * @return true if the given string contains at least one space, false otherwise
	 */
	public static boolean hasSpace(String var) {
		int index = var.indexOf(' ');
		if (index >= 0)
			return true;
		else
			return false;
	}
	
	/**
	 * Structure to represent a variable definition within a
	 * source string.
	 */
	public final class VariableDefinition {
		/**
		 * Index in the source text where the variable started
		 * or <code>-1</code> if no valid variable start tag 
		 * identifier found.
		 */
		public int start = -1;
		
		/**
		 * Index in the source text of the character following
		 * the end of the variable or <code>-1</code> if no 
		 * valid variable end tag found.
		 */
		public int end = -1;
		
		/**
		 * The variable's name found in the source text, or
		 * <code>null</code> if no valid variable found.
		 */
		public String name = null;
		
		/**
		 * The variable's argument found in the source text, or
		 * <code>null</code> if no valid variable found or if
		 * the variable did not specify an argument
		 */
		public String argument = null;
		
		/**
		 * Create an initialized variable definition.
		 */
		private VariableDefinition() {
			super();
		}
		
		/**
		 * Create an initialized variable definition.
		 */
		private VariableDefinition(int start, int end) {
			super();
			this.start = start;
			this.end = end;
		}
	}
}
