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
package org.eclipse.debug.core.variables;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.boot.BootLoader;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.internal.core.DebugCoreMessages;

/**
 * Utility for dealing with launch variables.
 * 
 * @since 3.0
 */
public class LaunchVariableUtil {
	/**
	 * Variable tag indentifiers
	 */
	private static final char VAR_TAG_START_CHAR1 = '$';
	private static final char VAR_TAG_START_CHAR2 = '{';
	private static final char VAR_TAG_END_CHAR1 = '}';
	private static final String VAR_TAG_START = "${"; //$NON-NLS-1$
	private static final String VAR_TAG_END = "}"; //$NON-NLS-1$
	private static final String VAR_TAG_SEP = ":"; //$NON-NLS-1$
	/**
	 * Argument parsing constants
	 */
	private static final char ARG_DELIMITER = ' ';
	private static final char ARG_DBL_QUOTE = '"';
	private static final char ARG_BACKSLASH = '\\';
	/**
	 * Launch configuration attribute - a map of variables passed into
	 * Runtime.exec(...) when a launch configuration is launched.
	 * 
	 * @since 3.0
	 */
	public static final String ATTR_ENVIRONMENT_VARIABLES = DebugPlugin.getUniqueIdentifier() + ".environmentVariables"; //$NON-NLS-1$

	/**
	 * Boolean attribute indicating if a refresh scope is recursive. Default
	 * value is <code>false</code>.
	 * 
	 * @since 3.0
	 */
	public static final String ATTR_REFRESH_RECURSIVE = DebugPlugin.getUniqueIdentifier() + ".ATTR_REFRESH_RECURSIVE"; //$NON-NLS-1$

	/**
	 * String attribute identifying the scope of resources that should be
	 * refreshed after an external tool is run. The value is either a refresh
	 * variable or the default value, <code>null</code>, indicating no refresh.
	 * 
	 * @since 3.0
	 */
	public static final String ATTR_REFRESH_SCOPE = DebugPlugin.getUniqueIdentifier() + ".ATTR_REFRESH_SCOPE"; //$NON-NLS-1$
	
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
	 * Extracts a variable name and argument from the given string.
	 * 
	 * @param text the source text to parse for a variable tag
	 * @param start the index in the string to start the search
	 * @return the variable definition
	 */
	public static VariableDefinition extractVariableDefinition(String text, int start) {
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
	 * Returns an expression referencing the given variable and argument.
	 * 
	 * @param varName the name of a variable
	 * @param argument an optional argument for the variable, <code>null</code> if none
	 * @return an expression referencing the given variable and argument
	 */
	public static String newVariableExpression(String varName, String argument) {
		StringBuffer buf = new StringBuffer();
		appendVariableExpression(varName,argument, buf);
		return buf.toString();
	}
	
	/**
	 * Builds and appends a variable expression referencing the given variable and
	 * argument, to the given String buffer.
	 * 
	 * @param varName the name of a variable
	 * @param argument an optional argument for the variable, <code>null</code> if none
	 * @param buffer the buffer to append the expression to
	 */
	private static void appendVariableExpression(String varName, String argument, StringBuffer buffer) {
		buffer.append(VAR_TAG_START);
		buffer.append(varName);
		if (argument != null && argument.length() > 0) {
			buffer.append(VAR_TAG_SEP);
			buffer.append(argument);
		}
		buffer.append(VAR_TAG_END);
	}

	/**
	 * Expands all the variables found in the given string.
	 * 
	 * @param argument the string whose variables should be expanded
	 * @param context the context to use for expanding variables or
	 * <code>null</code> if none.
	 * @param status multi status to report any problems expanding variables or
	 * <code>null</code> if none.
	 * @return the argument text with all variables expanded, or <code>null</code> if not possible
	 */
	public static String expandVariables(String argument, MultiStatus status, ExpandVariableContext context) {
		StringBuffer buffer = new StringBuffer();
		int start = 0;
		VariableDefinition varDef= extractVariableDefinition(argument, start);
		while (varDef.start > -1) {
			if (varDef.end == -1 || varDef.name == null || varDef.name.length() == 0) {
				// Invalid variable format
				if (status != null) {
					status.merge(newErrorStatus(MessageFormat.format(DebugCoreMessages.getString("VariableUtil.4"), new String[] {argument.substring(varDef.start)}), null)); //$NON-NLS-1$
				}
				return null;
			}
			// Copy text between start and variable.			
			if (varDef.start > start) {
				buffer.append(argument.substring(start, varDef.start));
			}
			start = varDef.end;
			// Look up the context variable if it exists
			ILaunchVariableManager manager = DebugPlugin.getDefault().getLaunchVariableManager();
			IContextLaunchVariable contextVariable = manager.getContextVariable(varDef.name);
			if (contextVariable != null) {
				String text = null;
				if (context == null) {
					context= new ExpandVariableContext(null);
				}
				try {
					text= contextVariable.getExpander().getText(varDef.name, varDef.argument, context);
				} catch (CoreException exception) {
					if (status != null) {
						status.merge(exception.getStatus());
					}
					return null;
				}
				buffer.append(text);
			} else {
				// If no context variable found, look up a simple variable
				ISimpleLaunchVariable simpleVariable= manager.getSimpleVariable(varDef.name);
				if (simpleVariable == null) {
					if (status != null) {
						status.merge(newErrorStatus(MessageFormat.format(DebugCoreMessages.getString("VariableUtil.5"), new Object[] {varDef.name}), null)); //$NON-NLS-1$
					}
					return null;
				}
				buffer.append(simpleVariable.getValue());
			}
			varDef = extractVariableDefinition(argument, start);
		}
		// No more variables
		if (start == 0) {
			buffer.append(argument);
		} else {
			buffer.append(argument.substring(start));
		}
		return buffer.toString();
	}

	/**
	 * Return a list of all the environment variables (suitable for passing to
	 * Process.exec) in which variable expansion has been performed.
	 * 
	 * @param envMap Map of all the environment variables (key=name,value=value)
	 * @param context the context used to expand the variable or <code>null</code>
	 * if none.
	 * @param status multi status to report any problems expanding variables
	 * @return String[] the list of variables in "variable=value" form
	 */
	public static String[] expandEnvironment(Map envMap, MultiStatus status, ExpandVariableContext context) {
		String[] vars = null;
		if (envMap != null && envMap.size() > 0) {
			Map.Entry e;
			Iterator iter = envMap.entrySet().iterator();
			vars = new String[envMap.size()];
			int i = 0;
			while (iter.hasNext()) {
				e = (Map.Entry) iter.next();
				vars[i++] = (String) e.getKey() + '=' + expandVariables((String) e.getValue(), status, context);
			}
		}
		return vars;
	}

	/**
	 * Returns a list of individual strings where all
	 * variables have been expanded. The given string
	 * is separated into individual strings based on whitespace
	 * deliniation.
	 * 
	 * @param sourceString the source string with leading and trailing
	 * 		spaces already removed.
	 * @param context the context used to expand the variable(s)
	 * @param status multi status to report any problems expanding variables or
	 * 		<code>null</code> if none.
	 * @return the list of individual arguments where some elements in the
	 * 		list maybe <code>null</code> if problems expanding variable(s).
	 */
	public static String[] expandStrings(String sourceString, MultiStatus status, ExpandVariableContext context) {
		if (sourceString == null || sourceString.length() == 0) {
			return new String[0];
		}
	
		String[] argList = parseStringIntoList(sourceString);
		for (int i = 0; i < argList.length; i++) {
			argList[i] = expandVariables(argList[i], status, context);
		}
		
		return argList;
	}

	/**
	 * Parses the argument text into an array of individual
	 * strings using the space character as the delimiter.
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
	public static String[] parseStringIntoList(String arguments) {
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
					
				case ARG_BACKSLASH:
					if (start < end && (arguments.charAt(start) == ARG_DBL_QUOTE)) {
						// Escaped double-quote
						buffer.append('\"');
						start++;
					} else {
						buffer.append(ch);
					}
					break;
	
				case ARG_DBL_QUOTE :
					if (inVar) {
						buffer.append(ch);
					} else {
						inQuotes = !inQuotes;
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
		
		if (buffer.length() > 0) {
			list.add(buffer.toString());
		}
			
		String[] results = new String[list.size()];
		list.toArray(results);
		return results;
	}

	/** 
	 * Returns an array of (expanded) environment variables to be used when
	 * running the launch configuration or <code>null</code> if unspecified
	 * 
	 * @param configuration launch configuration
	 * @param context context used to expand environment variable values
	 * or <code>null</code> if none
	 * @return String[] the array of "variable=value" pairs, suitable for
	 * passing to Runtime.exec(...)
	 * @throws CoreException if unable to access associated attribute or if
	 * unable to resolve a variable in an environment variable's value
	 */
	public static String[] getEnvironment(ILaunchConfiguration configuration, ExpandVariableContext context) throws CoreException {
		Map envMap = configuration.getAttribute(ATTR_ENVIRONMENT_VARIABLES, (Map) null);
		if (envMap == null) {
			return null;
		}
		MultiStatus status = new MultiStatus(DebugPlugin.getUniqueIdentifier(), 0, DebugCoreMessages.getString("VariableUtil.6"), null); //$NON-NLS-1$
		HashMap env= getNativeEnvironment();
		if (status.isOK()) {
			if (envMap != null) {
				Iterator iter= envMap.entrySet().iterator();
				// Overwrite system environment with locally defined variables
				while (iter.hasNext()) {
					Map.Entry entry = (Map.Entry) iter.next();
					env.put(entry.getKey(), expandVariables((String) entry.getValue(), status, context));
				}
			}
			Iterator iter= env.entrySet().iterator();
			List strings= new ArrayList(env.size());
			while (iter.hasNext()) {
				Map.Entry entry = (Map.Entry) iter.next();
				StringBuffer buffer= new StringBuffer((String) entry.getKey());
				buffer.append('=').append((String) entry.getValue());
				strings.add(buffer.toString());
			}
			return (String[]) strings.toArray(new String[strings.size()]);
		} else {
			throw new CoreException(status);
		}
	}
	
	/**
	 * Returns the native system environment variables.
	 * 
	 * @return the native system environment variables
	 */
	private static HashMap getNativeEnvironment() {
		HashMap env= new HashMap();
		try {
			String nativeCommand= null;
			if (BootLoader.getOS().equals(BootLoader.OS_WIN32)) {
				nativeCommand= "set";
			} else if (!BootLoader.getOS().equals(BootLoader.OS_UNKNOWN)){
				nativeCommand= "printenv";				
			}
			if (nativeCommand == null) {
				return env;
			}
			Process process= Runtime.getRuntime().exec(nativeCommand);
			BufferedReader reader= new BufferedReader(new InputStreamReader(process.getInputStream()));
			String line= reader.readLine();
			while (line != null) {
				int separator= line.indexOf('=');
				if (separator > 0) {
					String key= line.substring(0, separator);
					String value= line.substring(separator + 1);
					env.put(key, value);
				}
				line= reader.readLine();
			}
			reader.close();
		} catch (IOException e) {
			DebugPlugin.log(e);
		}
		return env;
	}

	private static IStatus newErrorStatus(String message, Throwable exception) {
		return new Status(IStatus.ERROR, DebugPlugin.getUniqueIdentifier(), DebugPlugin.INTERNAL_ERROR, message, exception);
	}

	/**
	 * Returns the refresh scope specified by the given launch configuration or
	 * <code>null</code> if none.
	 * 
	 * @param configuration
	 * @return refresh scope
	 * @throws CoreException if unable to access the associated attribute
	 */
	public static String getRefreshScope(ILaunchConfiguration configuration) throws CoreException {
		return configuration.getAttribute(LaunchVariableUtil.ATTR_REFRESH_SCOPE, (String) null);
	}

	/**
	 * Returns whether the refresh scope specified by the given launch
	 * configuration is recursive.
	 * 
	 * @param configuration
	 * @return whether the refresh scope is recursive
	 * @throws CoreException if unable to access the associated attribute
	 */
	public static boolean isRefreshRecursive(ILaunchConfiguration configuration) throws CoreException {
		return configuration.getAttribute(LaunchVariableUtil.ATTR_REFRESH_RECURSIVE, true);
	}

}
