/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.quickaccess;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility methods for camel case style matching.
 *  
 * @since 3.3
 * 
 */
public class CamelUtil {

	/**
	 * Returns a lowercase string consisting of all initials of the words in the
	 * given String. Words are separated by whitespace and other special
	 * characters, or by uppercase letters in a word like CamelCase.
	 * 
	 * @param s
	 *            the string
	 * @return a lowercase string containing the first character of every wordin
	 *         the given string.
	 */
	public static String getCamelCase(String s) {
		StringBuffer result = new StringBuffer();
		if (s.length() > 0) {
			int index = 0;
			while (index != -1) {
				result.append(s.charAt(index));
				index = getNextCamelIndex(s, index + 1);
			}
		}
		return result.toString().toLowerCase();
	}

	/**
	 * Return an array with start/end indices for the characters used for camel
	 * case matching, ignoring the first (start) many camel case characters.
	 * For example, getCamelCaseIndices("some CamelCase", 1, 2) will return
	 * {{5,5},{10,10}}.
	 * 
	 * @param s the source string
	 * @param start how many characters of getCamelCase(s) should be ignored
	 * @param length for how many characters should indices be returned 
	 * @return an array of length start
	 */
	public static int[][] getCamelCaseIndices(String s, int start, int length) {
		List result = new ArrayList();
		int index = 0;
		while (start > 0) {
			index = getNextCamelIndex(s, index + 1);
			start--;
		}
		while (length > 0) {
			result.add(new int[] { index, index });
			index = getNextCamelIndex(s, index + 1);
			length--;
		}
		return (int[][]) result.toArray(new int[result.size()][]);
	}

	/**
	 * Returns the next index to be used for camel case matching.
	 * 
	 * @param s the string
	 * @param index the index
	 * @return the next index, or -1 if not found
	 */
	public static int getNextCamelIndex(String s, int index) {
		char c;
		while (index < s.length()
				&& !(isSeparatorForCamelCase(c = s.charAt(index)))
				&& Character.isLowerCase(c)) {
			index++;
		}
		while (index < s.length() && isSeparatorForCamelCase(c = s.charAt(index))) {
			index++;
		}
		if (index >= s.length()) {
			index = -1;
		}
		return index;
	}

	/**
	 * Returns true if the given character is to be considered a separator
	 * for camel case matching purposes.
	 * 
	 * @param c the character
	 * @return true if the character is a separator
	 */
	public static boolean isSeparatorForCamelCase(char c) {
		return !Character.isLetterOrDigit(c);
	}

}
