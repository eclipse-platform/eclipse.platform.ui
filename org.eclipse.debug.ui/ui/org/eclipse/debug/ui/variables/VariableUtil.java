/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Tomasz Stanczak - Fix for Bug 29504
 *     Keith Seitz (keiths@redhat.com) - environment variables contribution (Bug 27243)
 *******************************************************************************/
package org.eclipse.debug.ui.variables;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.debug.internal.ui.DebugUIPlugin;

/**
 * Utility for dealing with variables
 */
public class VariableUtil {
	/**
	 * Variable tag indentifiers
	 */
	public static final char VAR_TAG_START_CHAR1 = '$'; //$NON-NLS-1$
	public static final char VAR_TAG_START_CHAR2 = '{'; //$NON-NLS-1$
	public static final char VAR_TAG_END_CHAR1 = '}'; //$NON-NLS-1$
	private static final String VAR_TAG_START = "${"; //$NON-NLS-1$
	private static final String VAR_TAG_END = "}"; //$NON-NLS-1$
	private static final String VAR_TAG_SEP = ":"; //$NON-NLS-1$
	/**
	 * Argument parsing constants
	 */
	private static final char ARG_DELIMITER = ' '; //$NON-NLS-1$
	private static final char ARG_DBL_QUOTE = '"'; //$NON-NLS-1$
	
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
		if (varDef.start < 0){
			return varDef;
		}
		start = varDef.start + VAR_TAG_START.length();
		
		int end = text.indexOf(VAR_TAG_END, start);
		if (end < 0) {
			return varDef;
		}
		varDef.end = end + VAR_TAG_END.length();
		if (end == start) {
			return varDef;
		}
	
		int mid = text.indexOf(VAR_TAG_SEP, start);
		if (mid < 0 || mid > end) {
			varDef.name = text.substring(start, end);
		} else {
			if (mid > start) {
				varDef.name = text.substring(start, mid);
			}
			mid = mid + VAR_TAG_SEP.length();
			if (mid < end) {
				varDef.argument = text.substring(mid, end);
			}
		}
		
		return varDef;
	}
	
	/**
	 * Returns a variable tag from the given variable name and argument.
	 * 
	 * @param varName the name of a variable (one of the VAR_* constants for instance)
	 * @param varArgument an optional argument for the variable, <code>null</code> if none
	 * @return a variable tag, built from the given variable name and argument.
	 */
	public static String buildVariableTag(String varName, String varArgument) {
		StringBuffer buf = new StringBuffer();
		buildVariableTag(varName,varArgument, buf);
		return buf.toString();
	}
	
	/**
	 * Builds a variable tag in the given buffer from the given
	 * variable name and argument.
	 * 
	 * @param varName the name of a variable (one of the VAR_* constants for instance)
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
	 * Expands all the variables found in the given string.
	 * 
	 * @param argument the string whose variables should be expanded
	 * @param context the context to use for expanding variables
	 * @param status multi status to report any problems expanding variables
	 * @return the argument text with all variables expanded, or <code>null</code> if not possible
	 */
	public static String expandTextVariables(String argument, ExpandVariableContext context, MultiStatus status) {
		StringBuffer buffer = new StringBuffer();
		
		int start = 0;
		while (true) {
			VariableUtil.VariableDefinition varDef = VariableUtil.extractVariableTag(argument, start);
			// No more variables found...
			if (varDef.start == -1) {
				if (start == 0) {
					buffer.append(argument);
				} else {
					buffer.append(argument.substring(start));
				}
				break;
			}
	
			// Invalid variable format
			if (varDef.end == -1 || varDef.name == null || varDef.name.length() == 0) {
				status.merge(DebugUIPlugin.newErrorStatus(MessageFormat.format("Invalid variable format: {0}.", new String[] {argument.substring(varDef.start)}), null));
				return null;
			}
	
			// Copy text between start and variable.			
			if (varDef.start > start) {
				buffer.append(argument.substring(start, varDef.start));
			}
			start = varDef.end;
			
			// Lookup the variable if it exists
			LaunchConfigurationVariableRegistry registry = DebugUIPlugin.getDefault().getToolVariableRegistry();
			LaunchConfigurationVariable variable = registry.getVariable(varDef.name);
			if (variable == null) {
				status.merge(DebugUIPlugin.newErrorStatus(MessageFormat.format("The variable named \''{0}\'' does not exist.", new Object[] {varDef.name}), null));
				return null;
			}
			
			// Expand the variable as text if possible
			String text = null;
			try {
				text= variable.getExpander().getText(varDef.name, varDef.argument, context);
			} catch (CoreException exception) {
				status.merge(exception.getStatus());
				return null;
			}
			buffer.append(text);
		}
		return buffer.toString();
	}

	/**
	 * Returns the expanded location if represented by a
	 * location variable. Otherwise, the location given is
	 * returned unless an unknown variable was detected.
	 * 
	 * @param locationText a location either as a path or a variable
	 * 		with leading and trailing spaces already removed.
	 * @param context the context used to expand the variable
	 * @param status multi status to report any problems expanding variables
	 * @return the location as a string or <code>null</code> if not possible
	 */
	public static String expandLocationText(String locationText, ExpandVariableContext context, MultiStatus status) {
		if (locationText == null || locationText.length() == 0) {
			return ""; //$NON-NLS-1$
		}
	
		VariableUtil.VariableDefinition varDef = VariableUtil.extractVariableTag(locationText, 0);
		// Return if no variable found
		if (varDef.start < 0) {
			return locationText;
		}
		
		StringBuffer buffer= new StringBuffer();
		int start= 0;
		while (varDef.start >= 0) {
			// Invalid variable format
			if (varDef.name == null || varDef.name.length() == 0 || varDef.end == -1) {
				status.merge(DebugUIPlugin.newErrorStatus(MessageFormat.format("Invalid variable format: {0}.", new String[] {locationText.substring(varDef.start)}), null));
				return null;
			}
			
			// Append text before the variable
			buffer.append(locationText.substring(start, varDef.start));
			
			// Lookup the variable if it exist
			LaunchConfigurationVariableRegistry registry;
			registry = DebugUIPlugin.getDefault().getToolVariableRegistry();
			LaunchConfigurationVariable variable = registry.getVariable(varDef.name);
			if (variable == null) {
				status.merge(DebugUIPlugin.newErrorStatus(MessageFormat.format("The variable named ''{0}'' does not exist.", new Object[] {varDef.name}), null));
				return null;
			}
			
			// Expand the variable into a IPath if possible
			IPath path= null;
			try {
				path= variable.getExpander().getPath(varDef.name, varDef.argument, context);
			} catch (CoreException exception) {
				status.merge(exception.getStatus());
				return null;
			}
			buffer.append(path.toOSString());
			start= varDef.end;
			varDef= VariableUtil.extractVariableTag(locationText, start);
		}
		// Append text remaining after the variables
		buffer.append(locationText.substring(start));
		return buffer.toString();
	}

	/**
	 * Return a list of all the environment variables (suitable for passing to
	 * Process.exec) in which variable expansion has been performed.
	 * 
	 * @param envMap Map of all the environment variables (key=name,value=value)
	 * @param context the context used to expand the variable
	 * @param status multi status to report any problems expanding variables
	 * @return String[] the list of variables in "variable=value" form
	 */
	public static String[] expandEnvironment(Map envMap, ExpandVariableContext context, MultiStatus status) {
		String[] vars = null;
		
		if (envMap != null && envMap.size() > 0) {
			Map.Entry e;
			Iterator iter = envMap.entrySet().iterator();
			vars = new String[envMap.size()];
			int i = 0;
			while (iter.hasNext()) {
				e = (Map.Entry) iter.next();
				vars[i++] = (String) e.getKey() + '=' + VariableUtil.expandTextVariables((String) e.getValue(), context, status);
			}
		}
		
		return vars;
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
		if (arguments == null || arguments.length() == 0) {
			return new String[0];
		}
	
		String[] argList = parseArgumentsIntoList(arguments);
		for (int i = 0; i < argList.length; i++) {
			argList[i] = VariableUtil.expandTextVariables(argList[i], context, status);
		}
		
		return argList;
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
		if (arguments == null || arguments.length() == 0) {
			return new String[0];
		}
		
		List list = new ArrayList(10);
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
					
				case VariableUtil.VAR_TAG_START_CHAR1 :
					buffer.append(ch);
					if (!inVar && start < end) {
						if (arguments.charAt(start) == VariableUtil.VAR_TAG_START_CHAR2) {
							buffer.append(VariableUtil.VAR_TAG_START_CHAR2);
							inVar = true;
							start++;
						}
					}
					break;
	
				case VariableUtil.VAR_TAG_END_CHAR1 :
					buffer.append(ch);
					inVar = false;
					break;
	
				default :
					buffer.append(ch);
					break;
			}
			
		}
		
		if (buffer.length() > 0) {
			list.add(buffer.toString());
		}
			
		String[] results = new String[list.size()];
		list.toArray(results);
		return results;
	}

}
