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
package org.eclipse.ui.texteditor;



/**
 * Collection of text functions.
 */
class TextUtilities {

	/*
	 * 1GF86V3: ITPUI:WINNT - Internal errors using Find/Replace Dialog
	 * Copied from JFace text
	 */	
	
	
	public final static String[] fgDelimiters= new String[] { "\n", "\r", "\r\n" }; //$NON-NLS-3$ //$NON-NLS-2$ //$NON-NLS-1$
	
	
	/**
	 * Determines which one of fgDelimiters appears first in the list. If none of them the
	 * hint is returned.
	 */
	public static String determineLineDelimiter(String text, String hint) {
		try {
			int[] info= indexOf(fgDelimiters, text, 0);
			return fgDelimiters[info[1]];
		} catch (ArrayIndexOutOfBoundsException x) {
		}
		return hint;
	}
	
	/**
	 * Returns the position in the string greater than offset
	 * of the longest matching search string.
	 */
	public static int[] indexOf(String[] searchStrings, String text, int offset) {
		
		int[] result= { -1, -1 };
		
		for (int i= 0; i < searchStrings.length; i++) {
			int index= text.indexOf(searchStrings[i], offset);
			if (index >= 0) {
				
				if (result[0] == -1) {
					result[0]= index;
					result[1]= i;
				} else if (index < result[0]) {
					result[0]= index;
					result[1]= i;
				} else if (index == result[0] && searchStrings[i].length() > searchStrings[result[1]].length()) {
					result[0]= index;
					result[1]= i;
				}
			}
		}
		
		return result;
		
	}
	
	/**
	 * Returns the longest search string with which the given text ends.
	 */
	public static int endsWith(String[] searchStrings, String text) {
		
		int index= -1;
		
		for (int i= 0; i < searchStrings.length; i++) {
			if (text.endsWith(searchStrings[i])) {
				if (index == -1 || searchStrings[i].length() > searchStrings[index].length())
					index= i;
			}
		}
		
		return index;
	}
	
	/**
	 * Returns the longest search string with which the given text starts.
	 */
	public static int startsWith(String[] searchStrings, String text) {
		
		int index= -1;
		
		for (int i= 0; i < searchStrings.length; i++) {
			if (text.startsWith(searchStrings[i])) {
				if (index == -1 || searchStrings[i].length() > searchStrings[index].length())
					index= i;
			}
		}
		
		return index;
	}
	
	/**
	 * Returns whether the text equals one of the given compare strings.
	 */
	public static int equals(String[] compareStrings, String text) {
		for (int i= 0; i < compareStrings.length; i++) {
			if (text.equals(compareStrings[i]))
				return i;
		}
		return -1;
	}	
}
