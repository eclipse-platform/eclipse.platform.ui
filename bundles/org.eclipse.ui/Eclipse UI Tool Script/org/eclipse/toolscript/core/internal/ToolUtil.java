package org.eclipse.toolscript.core.internal;

/**********************************************************************
Copyright (c) 2002 IBM Corp. and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v0.5
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v05.html
 
Contributors:
**********************************************************************/

/**
 * General utility class dealing with tool scripts
 */
public final class ToolUtil {

	/**
	 * No instances allowed
	 */
	private ToolUtil() {
		super();
	}

	/**
	 * Builds a variable tag that will be auto-expanded before
	 * the script is run.
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
	 * the script is run.
	 * 
	 * @param varName the name of a known variable (one of the VAR_* constants)
	 * @param varArgument an optional argument for the variable, <code>null</code> if none
	 * @param buffer the buffer to write the constructed variable tag
	 */
	public static void buildVariableTag(String varName, String varArgument, StringBuffer buffer) {
		buffer.append(ToolScript.VAR_TAG_START);
		buffer.append(varName);
		if (varArgument != null && varArgument.length() > 0) {
			buffer.append(ToolScript.VAR_TAG_SEP);
			buffer.append(varArgument);
		}
		buffer.append(ToolScript.VAR_TAG_END);
	}
	
	/**
	 * Builds a variable tag for each argument that will be auto-expanded before
	 * the script is run.
	 * 
	 * @param varName the name of a known variable (one of the VAR_* constants)
	 * @param varArguments a list of arguments for each variable
	 * @param buffer the buffer to write the constructed variable tags
	 */
	public static void buildVariableTags(String varName, String[] varArguments, StringBuffer buffer) {
		for (int i = 0; i < varArguments.length; i++) {
			buffer.append(" "); // $NON-NLS-1$
			buffer.append(ToolScript.VAR_TAG_START);
			buffer.append(varName);
			if (varArguments[i] != null && varArguments[i].length() > 0) {
				buffer.append(ToolScript.VAR_TAG_SEP);
				buffer.append(varArguments[i]);
			}
			buffer.append(ToolScript.VAR_TAG_END);
		}
	}
	
	/**
	 * Extracts a variable tag into its name and argument.
	 * 
	 * @param varTag the variable tag to parse
	 * @return an array where the 1st element is the var name and
	 * 		the 2nd element is the var argument. Elements in array
	 * 		can be <code>null</code>
	 */
	public static String[] extractVariableTag(String varTag) {
		String[] result = new String[2];
		int start = 0;
		int end = varTag.indexOf(ToolScript.VAR_TAG_START);
		if (end < 0)
			return result;
		start = end + ToolScript.VAR_TAG_START.length();
		
		end = varTag.indexOf(ToolScript.VAR_TAG_END, start);
		if (end < 0 || end == start)
			return result;

		int mid = varTag.indexOf(ToolScript.VAR_TAG_SEP, start);
		if (mid < 0 || mid > end) {
			result[0] = varTag.substring(start, end);
		} else {
			result[0] = varTag.substring(start, mid);
			result[1] = varTag.substring(mid+1, end);
		}
		
		return result;
	}
}
