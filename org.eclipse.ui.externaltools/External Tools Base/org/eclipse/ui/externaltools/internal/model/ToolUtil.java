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

package org.eclipse.ui.externaltools.internal.model;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.ui.variables.ExpandVariableContext;
import org.eclipse.debug.ui.variables.ExternalToolVariable;
import org.eclipse.debug.ui.variables.ExternalToolVariableRegistry;
import org.eclipse.debug.ui.variables.VariableUtil;

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
	 * No instances allowed
	 */
	private ToolUtil() {
		super();
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
			Entry e;
			Iterator iter = envMap.entrySet().iterator();
			vars = new String[envMap.size()];
			int i = 0;
			while (iter.hasNext()) {
				e = (Entry) iter.next();
				vars[i++] = (String) e.getKey() + '=' + expandArgument((String) e.getValue(), context, status);
			}
		}
		
		return vars;
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
				String msg = ExternalToolsModelMessages.getString("ToolUtil.argumentVarFormatWrong"); //$NON-NLS-1$
				status.merge(ExternalToolsPlugin.newErrorStatus(msg, null));
				return null;
			}

			// Copy text between start and variable.			
			if (varDef.start > start) {
				buffer.append(argument.substring(start, varDef.start));
			}
			start = varDef.end;
			
			// Lookup the variable if it exist
			ExternalToolVariableRegistry registry = DebugUIPlugin.getDefault().getToolVariableRegistry();
			ExternalToolVariable variable = registry.getVariable(varDef.name);
			if (variable == null) {
				String msg = MessageFormat.format(ExternalToolsModelMessages.getString("ToolUtil.argumentVarMissing"), new Object[] {varDef.name}); //$NON-NLS-1$
				status.merge(ExternalToolsPlugin.newErrorStatus(msg, null));
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
			argList[i] = expandArgument(argList[i], context, status);
		}
		
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
		if (dirLocation == null || dirLocation.length() == 0) {
			return ""; //$NON-NLS-1$
		}

		VariableUtil.VariableDefinition varDef = VariableUtil.extractVariableTag(dirLocation, 0);
		// Return if no variable found
		if (varDef.start < 0) {
			return dirLocation;
		}
		
		StringBuffer buffer= new StringBuffer();
		int start= 0;
		while (varDef.start >= 0) {
			// Invalid variable format
			if (varDef.name == null || varDef.name.length() == 0 || varDef.end == -1) {
				String msg = ExternalToolsModelMessages.getString("ToolUtil.dirLocVarFormatWrong"); //$NON-NLS-1$
				status.merge(ExternalToolsPlugin.newErrorStatus(msg, null));
				return null;
			}
			
			// Append text before the variable
			buffer.append(dirLocation.substring(start, varDef.start));
			
			// Lookup the variable if it exist
			ExternalToolVariableRegistry registry;
			registry = DebugUIPlugin.getDefault().getToolVariableRegistry();
			ExternalToolVariable variable = registry.getVariable(varDef.name);
			if (variable == null) {
				String msg = MessageFormat.format(ExternalToolsModelMessages.getString("ToolUtil.dirLocVarMissing"), new Object[] {varDef.name}); //$NON-NLS-1$
				status.merge(ExternalToolsPlugin.newErrorStatus(msg, null));
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
			varDef= VariableUtil.extractVariableTag(dirLocation, start);
		}
		// Append text remaining after the variables
		buffer.append(dirLocation.substring(start));
		return buffer.toString();
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
		if (fileLocation == null || fileLocation.length() == 0) {
			return ""; //$NON-NLS-1$
		}

		VariableUtil.VariableDefinition varDef = VariableUtil.extractVariableTag(fileLocation, 0);
		// Return if no variable found
		if (varDef.start < 0) {
			return fileLocation;
		}
		
		StringBuffer buffer= new StringBuffer();
		int start= 0;
		while (varDef.start >= 0) {
			// Invalid variable format
			if (varDef.name == null || varDef.name.length() == 0 || varDef.end == -1) {
				String msg = ExternalToolsModelMessages.getString("ToolUtil.fileLocVarFormatWrong"); //$NON-NLS-1$
				status.merge(ExternalToolsPlugin.newErrorStatus(msg, null));
				return null;
			}
			
			// Append text before the variable
			buffer.append(fileLocation.substring(start, varDef.start));
		
			// Lookup the variable if it exist
			ExternalToolVariableRegistry registry;
			registry = DebugUIPlugin.getDefault().getToolVariableRegistry();
			ExternalToolVariable variable = registry.getVariable(varDef.name);
			if (variable == null) {
				String msg = MessageFormat.format(ExternalToolsModelMessages.getString("ToolUtil.fileLocVarMissing"), new Object[] {varDef.name}); //$NON-NLS-1$
				status.merge(ExternalToolsPlugin.newErrorStatus(msg, null));
				return null;
			}
		
			// Expand the variable into a IPath if possible
			IPath path = null;
			try {
				path= variable.getExpander().getPath(varDef.name, varDef.argument, context);
			} catch (CoreException exception) {
				status.merge(exception.getStatus());
				return null;
			}
			buffer.append(path.toOSString());
			start= varDef.end;
			varDef= VariableUtil.extractVariableTag(fileLocation, start);
		}
		// Append text remaining after the variables
		buffer.append(fileLocation.substring(start));
		return buffer.toString();
		

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