package org.eclipse.ui.externaltools.model;

/**********************************************************************
Copyright (c) 2002 IBM Corp. and others. All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
 
Contributors:
**********************************************************************/

import java.util.ArrayList;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.ui.externaltools.internal.model.ExternalToolsPlugin;
import org.eclipse.ui.externaltools.internal.model.ToolMessages;
import org.eclipse.ui.externaltools.internal.registry.ArgumentVariable;
import org.eclipse.ui.externaltools.internal.registry.ArgumentVariableRegistry;
import org.eclipse.ui.externaltools.internal.registry.PathLocationVariable;
import org.eclipse.ui.externaltools.internal.registry.PathLocationVariableRegistry;
import org.eclipse.ui.externaltools.variable.ExpandVariableContext;

/**
 * General utility class dealing with external tools
 */
public final class ToolUtil {
	/**
	 * Argument parsing constants
	 */
	private static final char ARG_DELIMITER = ' '; //$NON-NLS-1$
	private static final char ARG_DBL_QUOTE = '"'; //$NON-NLS-1$
	
	/**
	 * Variable tag indentifiers
	 */
	private static final char VAR_TAG_START_CHAR1 = '$'; //$NON-NLS-1$
	private static final char VAR_TAG_START_CHAR2 = '{'; //$NON-NLS-1$
	private static final char VAR_TAG_END_CHAR1 = '}'; //$NON-NLS-1$
	private static final String VAR_TAG_START = "${"; //$NON-NLS-1$
	private static final String VAR_TAG_END = "}"; //$NON-NLS-1$
	private static final String VAR_TAG_SEP = ":"; //$NON-NLS-1$

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
	 * @param varName the name of a known variable (one of the VAR_* constants for instance)
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
	 * @param varName the name of a known variable (one of the VAR_* constants for instance)
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
	 * Expands all the variables found in an individual
	 * argument text.
	 * 
	 * @param argument one of the argument text in the list of arguments
	 * @param context the context to use for expanding variables
	 * @param status multi status to report any problems expanding variables
	 * @return the argument text with all variables expanded, or <code>null</code> if not possible
	 */
	public static String expandArgument(String argument, ExpandVariableContext context, MultiStatus status) {
		StringBuffer buffer = new StringBuffer();
		
		int start = 0;
		while (true) {
			VariableDefinition varDef = extractVariableTag(argument, start);
			
			// No more variables found...
			if (varDef.start == -1) {
				if (start == 0)
					buffer.append(argument);
				else
					buffer.append(argument.substring(start));
				break;
			}

			// Invalid variable format
			if (varDef.end == -1 || varDef.name == null || varDef.name.length() == 0) {
				String msg = ToolMessages.getString("ToolUtil.argumentVarFormatWrong"); //$NON-NLS-1$
				status.merge(ExternalToolsPlugin.newErrorStatus(msg, null));
				return null;
			}

			// Copy text between start and variable.			
			if (varDef.start > start)
				buffer.append(argument.substring(start, varDef.start));
			start = varDef.end;
			
			// Lookup the variable if it exist
			ArgumentVariableRegistry registry;
			registry = ExternalToolsPlugin.getDefault().getArgumentVariableRegistry();
			ArgumentVariable variable = registry.getArgumentVariable(varDef.name);
			if (variable == null) {
				String msg = ToolMessages.format("ToolUtil.argumentVarMissing", new Object[] {varDef.name}); //$NON-NLS-1$
				status.merge(ExternalToolsPlugin.newErrorStatus(msg, null));
				return null;
			}
			
			// Expand the variable as text if possible
			String text = variable.getExpander().getText(varDef.name, varDef.argument, context);
			if (text == null) {
				String msg = ToolMessages.format("ToolUtil.argumentVarExpandFailed", new Object[] {varDef.name}); //$NON-NLS-1$
				status.merge(ExternalToolsPlugin.newErrorStatus(msg, null));
				return null;
			}
			buffer.append(text);
		}
		
		return buffer.toString();
	}
	
	/**
	 * Returns a list of individual arguments where all
	 * variables have been expanded.
	 * 
	 * @param arguments the arguments with leading and trailing
	 * 		spaces already removed.
	 * @param context the context used to expand the variable(s)
	 * @param status multi status to report any problems expanding variables
	 * @return the list of individual arguments where some elements in the
	 * 		list maybe <code>null</code> if problems expanding variable(s).
	 */
	public static String[] expandArguments(String arguments, ExpandVariableContext context, MultiStatus status) {
		if (arguments == null || arguments.length() == 0)
			return new String[0];

		String[] argList = parseArgumentsIntoList(arguments);
		for (int i = 0; i < argList.length; i++)
			argList[i] = expandArgument(argList[i], context, status);
		
		return argList;
	}
	
	/**
	 * Returns the expanded directory location if represented by a
	 * directory variable. Otherwise, the directory location given is
	 * return unless an unknown variable was detected.
	 * 
	 * @param dirLocation a directory location either as a path or a variable
	 * 		with leading and trailing spaces already removed.
	 * @param context the context used to expand the variable
	 * @param status multi status to report any problems expanding variables
	 * @return the directory location as a string or <code>null</code> if not possible
	 */
	public static String expandDirectoryLocation(String dirLocation, ExpandVariableContext context, MultiStatus status) {
		if (dirLocation == null || dirLocation.length() == 0)
			return ""; //$NON-NLS-1$

		VariableDefinition varDef = extractVariableTag(dirLocation, 0);
		// Return if no variable found
		if (varDef.start < 0)
			return dirLocation;
		
		// Disallow text before/after variable
		if (varDef.start != 0 || (varDef.end < dirLocation.length() && varDef.end != -1)) {
			String msg = ToolMessages.getString("ToolUtil.dirLocVarBetweenText"); //$NON-NLS-1$
			status.merge(ExternalToolsPlugin.newErrorStatus(msg, null));
			return null;
		}
		
		// Invalid variable format
		if (varDef.name == null || varDef.name.length() == 0 || varDef.end == -1) {
			String msg = ToolMessages.getString("ToolUtil.dirLocVarFormatWrong"); //$NON-NLS-1$
			status.merge(ExternalToolsPlugin.newErrorStatus(msg, null));
			return null;
		}
		
		// Lookup the variable if it exist
		PathLocationVariableRegistry registry;
		registry = ExternalToolsPlugin.getDefault().getDirectoryLocationVariableRegistry();
		PathLocationVariable variable = registry.getPathLocationVariable(varDef.name);
		if (variable == null) {
			String msg = ToolMessages.format("ToolUtil.dirLocVarMissing", new Object[] {varDef.name}); //$NON-NLS-1$
			status.merge(ExternalToolsPlugin.newErrorStatus(msg, null));
			return null;
		}
		
		// Expand the variable into a IPath if possible
		IPath path = variable.getExpander().getPath(varDef.name, varDef.argument, context);
		if (path == null) {
			String msg = ToolMessages.format("ToolUtil.dirLocVarExpandFailed", new Object[] {varDef.name}); //$NON-NLS-1$
			status.merge(ExternalToolsPlugin.newErrorStatus(msg, null));
			return null;
		}
		
		return path.toOSString();
	}
	
	/**
	 * Returns the expanded file location if represented by a
	 * file variable. Otherwise, the file location given is
	 * return unless an unknown variable was detected.
	 * 
	 * @param fileLocation a file location either as a path or a variable
	 * 		with leading and trailing spaces already removed.
	 * @param context the context used to expand the variable
	 * @param status multi status to report any problems expanding variables
	 * @return the file location as a string or <code>null</code> if not possible
	 */
	public static String expandFileLocation(String fileLocation, ExpandVariableContext context, MultiStatus status) {
		if (fileLocation == null || fileLocation.length() == 0)
			return ""; //$NON-NLS-1$

		VariableDefinition varDef = extractVariableTag(fileLocation, 0);
		// Return if no variable found
		if (varDef.start < 0)
			return fileLocation;
		
		// Disallow text before/after variable
		if (varDef.start != 0 || (varDef.end < fileLocation.length() && varDef.end != -1)) {
			String msg = ToolMessages.getString("ToolUtil.fileLocVarBetweenText"); //$NON-NLS-1$
			status.merge(ExternalToolsPlugin.newErrorStatus(msg, null));
			return null;
		}
		
		// Invalid variable format
		if (varDef.name == null || varDef.name.length() == 0 || varDef.end == -1) {
			String msg = ToolMessages.getString("ToolUtil.fileLocVarFormatWrong"); //$NON-NLS-1$
			status.merge(ExternalToolsPlugin.newErrorStatus(msg, null));
			return null;
		}
		
		// Lookup the variable if it exist
		PathLocationVariableRegistry registry;
		registry = ExternalToolsPlugin.getDefault().getFileLocationVariableRegistry();
		PathLocationVariable variable = registry.getPathLocationVariable(varDef.name);
		if (variable == null) {
			String msg = ToolMessages.format("ToolUtil.fileLocVarMissing", new Object[] {varDef.name}); //$NON-NLS-1$
			status.merge(ExternalToolsPlugin.newErrorStatus(msg, null));
			return null;
		}
		
		// Expand the variable into a IPath if possible
		IPath path = variable.getExpander().getPath(varDef.name, varDef.argument, context);
		if (path == null) {
			String msg = ToolMessages.format("The variable {0} with argument {1} could not be expanded to a valid path.", new Object[] {varDef.name, varDef.argument});
			status.merge(ExternalToolsPlugin.newErrorStatus(msg, null));
			return null;
		}
		
		return path.toString();
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
		VariableDefinition varDef = new VariableDefinition();
		
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
	 * Parses the argument text into an array of individual
	 * arguments using the space character as the delimiter.
	 * An individual argument containing spaces must have a
	 * double quote (") at the start and end. Two double 
	 * quotes together is taken to mean an embedded double
	 * quote in the argument text. Variables are treated as
	 * a single unit and therefore spaces and double quotes
	 * inside a variable are copied as is and not parsed.
	 * 
	 * @param arguments the arguments as one string
	 * @return the array of arguments
	 */
	public static String[] parseArgumentsIntoList(String arguments) {
		if (arguments == null || arguments.length() == 0)
			return new String[0];
		
		ArrayList list = new ArrayList(10);
		boolean inQuotes = false;
		boolean inVar = false;
		int start = 0;
		int end = arguments.length();
		StringBuffer buffer = new StringBuffer(end);
		
		while (start < end) {
			char ch = arguments.charAt(start);
			start++;
			
			switch (ch) {
				case ARG_DELIMITER :
					if (inQuotes || inVar) {
						buffer.append(ch);
					} else {
						if (buffer.length() > 0) {
							list.add(buffer.toString());
							buffer.setLength(0);
						}
					}
					break;

				case ARG_DBL_QUOTE :
					if (inVar) {
						buffer.append(ch);
					} else {
						if (start < end) {
							if (arguments.charAt(start) == ARG_DBL_QUOTE) {
								// Two quotes together represents one quote
								buffer.append(ch);
								start++;
							} else {
								inQuotes = !inQuotes;
							}
						} else {
							// A lone quote at the end, just drop it.
							inQuotes = false;
						}
					}
					break;
					
				case VAR_TAG_START_CHAR1 :
					buffer.append(ch);
					if (!inVar && start < end) {
						if (arguments.charAt(start) == VAR_TAG_START_CHAR2) {
							buffer.append(VAR_TAG_START_CHAR2);
							inVar = true;
							start++;
						}
					}
					break;

				case VAR_TAG_END_CHAR1 :
					buffer.append(ch);
					inVar = false;
					break;

				default :
					buffer.append(ch);
					break;
			}
			
		}
		
		if (buffer.length() > 0)
			list.add(buffer.toString());
			
		String[] results = new String[list.size()];
		list.toArray(results);
		return results;
	}


	/**
	 * Structure to represent a variable definition within a
	 * source string.
	 */
	public static final class VariableDefinition {
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
