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
package org.eclipse.debug.ui.variables;

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

}
