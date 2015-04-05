/*******************************************************************************
 * Copyright (c) 2008, 2015 Angelo Zerr and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.css.core.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Helper for String.
 */
public class StringUtils {

	/**
	 * Replace <b>oldString</b> occurrences with <b>newString</b> occurrences of
	 * the String <b>line</b> and return the result.
	 *
	 * @param line
	 * @param oldString
	 * @param newString
	 * @return
	 */
	public static final String replace(String line, String oldString,
			String newString) {
		int i = 0;
		if ((i = line.indexOf(oldString, i)) >= 0) {
			char line2[] = line.toCharArray();
			char newString2[] = newString.toCharArray();
			int oLength = oldString.length();
			StringBuffer buf = new StringBuffer(line2.length);
			buf.append(line2, 0, i).append(newString2);
			i += oLength;
			int j;
			for (j = i; (i = line.indexOf(oldString, i)) > 0; j = i) {
				buf.append(line2, j, i - j).append(newString2);
				i += oLength;
			}

			buf.append(line2, j, line2.length - j);
			return buf.toString();
		} else {
			return line;
		}
	}

	/**
	 * Split String <b>line</b> with delimiter <b>delim</b> and return result
	 * inti array of String.
	 *
	 * @param line
	 * @param delim
	 * @return
	 */
	public static String[] split(String line, String delim) {
		List<String> list = new ArrayList<>();
		for (StringTokenizer t = new StringTokenizer(line, delim); t.hasMoreTokens(); list.add(t.nextToken())) {
			;
		}
		return list.toArray(new String[list.size()]);
	}

	/**
	 * Return true if String value is null or empty.
	 *
	 * @param value
	 * @return
	 */
	public static boolean isEmpty(String value) {
		return (value == null || value.length() < 1);
	}

	// Capitalizing
	// -----------------------------------------------------------------------
	/**
	 * <p>
	 * Capitalizes all the whitespace separated words in a String. Only the
	 * first letter of each word is changed. To convert the rest of each word to
	 * lowercase at the same time, use {@link #capitalizeFully(String)}.
	 * </p>
	 *
	 * <p>
	 * Whitespace is defined by {@link Character#isWhitespace(char)}. A
	 * <code>null</code> input String returns <code>null</code>.
	 * Capitalization uses the unicode title case, normally equivalent to upper
	 * case.
	 * </p>
	 *
	 * <pre>
	 * WordUtils.capitalize(null)        = null
	 * WordUtils.capitalize(&quot;&quot;)          = &quot;&quot;
	 * WordUtils.capitalize(&quot;i am FINE&quot;) = &quot;I Am FINE&quot;
	 * </pre>
	 *
	 * @param str
	 *            the String to capitalize, may be null
	 * @return capitalized String, <code>null</code> if null String input
	 * @see #uncapitalize(String)
	 * @see #capitalizeFully(String)
	 */
	public static String capitalize(String str) {
		return capitalize(str, null);
	}

	/**
	 * <p>
	 * Capitalizes all the delimiter separated words in a String. Only the first
	 * letter of each word is changed. To convert the rest of each word to
	 * lowercase at the same time, use {@link #capitalizeFully(String, char[])}.
	 * </p>
	 *
	 * <p>
	 * The delimiters represent a set of characters understood to separate
	 * words. The first string character and the first non-delimiter character
	 * after a delimiter will be capitalized.
	 * </p>
	 *
	 * <p>
	 * A <code>null</code> input String returns <code>null</code>.
	 * Capitalization uses the unicode title case, normally equivalent to upper
	 * case.
	 * </p>
	 *
	 * <pre>
	 * WordUtils.capitalize(null, *)            = null
	 * WordUtils.capitalize(&quot;&quot;, *)              = &quot;&quot;
	 * WordUtils.capitalize(*, new char[0])     = *
	 * WordUtils.capitalize(&quot;i am fine&quot;, null)  = &quot;I Am Fine&quot;
	 * WordUtils.capitalize(&quot;i aM.fine&quot;, {'.'}) = &quot;I aM.Fine&quot;
	 * </pre>
	 *
	 * @param str
	 *            the String to capitalize, may be null
	 * @param delimiters
	 *            set of characters to determine capitalization, null means
	 *            whitespace
	 * @return capitalized String, <code>null</code> if null String input
	 * @see #uncapitalize(String)
	 * @see #capitalizeFully(String)
	 * @since 2.1
	 */
	public static String capitalize(String str, char[] delimiters) {
		int delimLen = (delimiters == null ? -1 : delimiters.length);
		if (str == null || str.length() == 0 || delimLen == 0) {
			return str;
		}
		int strLen = str.length();
		StringBuffer buffer = new StringBuffer(strLen);
		boolean capitalizeNext = true;
		for (int i = 0; i < strLen; i++) {
			char ch = str.charAt(i);

			if (isDelimiter(ch, delimiters)) {
				buffer.append(ch);
				capitalizeNext = true;
			} else if (capitalizeNext) {
				buffer.append(Character.toTitleCase(ch));
				capitalizeNext = false;
			} else {
				buffer.append(ch);
			}
		}
		return buffer.toString();
	}

	/**
	 * Is the character a delimiter.
	 *
	 * @param ch
	 *            the character to check
	 * @param delimiters
	 *            the delimiters
	 * @return true if it is a delimiter
	 */
	private static boolean isDelimiter(char ch, char[] delimiters) {
		if (delimiters == null) {
			return Character.isWhitespace(ch);
		}
		for (char delimiter : delimiters) {
			if (ch == delimiter) {
				return true;
			}
		}
		return false;
	}
}
