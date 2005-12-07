/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.propertytester;

import java.util.ArrayList;

/**
 * A string pattern matcher, supporting "*" and "?" wild cards.
 * 
 * @since 3.2
 */
public class StringMatcher {
	protected static final char fSingleWildCard = '\u0000';

	/* boundary value beyond which we don't need to search in the text */
	protected int fBound = 0;

	protected boolean fHasLeadingStar;

	protected boolean fHasTrailingStar;

	protected final int fLength; // pattern length

	protected final String fPattern;

	protected String fSegments[]; //the given pattern is split into * separated segments

	/**
	 * StringMatcher constructor takes in a String object that is a simple 
	 * pattern which may contain '*' for 0 and many characters and
	 * '?' for exactly one character.  
	 *
	 * Literal '*' and '?' characters must be escaped in the pattern 
	 * e.g., "\*" means literal "*", etc.
	 *
	 * Escaping any other character (including the escape character itself), 
	 * just results in that character in the pattern.
	 * e.g., "\a" means "a" and "\\" means "\"
	 *
	 * If invoking the StringMatcher with string literals in Java, don't forget
	 * escape characters are represented by "\\".
	 *
	 * @param pattern the pattern to match text against
	 */
	public StringMatcher(String pattern) {
		if (pattern == null)
			throw new IllegalArgumentException();
		fPattern = pattern;
		fLength = pattern.length();
		parseWildCards();
	}

	/** 
	 * @param text a simple regular expression that may only contain '?'(s)
	 * @param start the starting index in the text for search, inclusive
	 * @param end the stopping point of search, exclusive
	 * @param p a simple regular expression that may contain '?'
	 * @return the starting index in the text of the pattern , or -1 if not found 
	 */
	private int findPosition(String text, int start, int end, String p) {
		boolean hasWildCard = p.indexOf(fSingleWildCard) >= 0;
		int plen = p.length();
		for (int i = start, max = end - plen; i <= max; ++i) {
			if (hasWildCard) {
				if (regExpRegionMatches(text, i, p, 0, plen))
					return i;
			} else {
				if (text.regionMatches(true, i, p, 0, plen))
					return i;
			}
		}
		return -1;
	}

	/**
	 * Given the starting (inclusive) and the ending (exclusive) positions in the   
	 * <code>text</code>, determine if the given substring matches with aPattern  
	 * @return true if the specified portion of the text matches the pattern
	 * @param text a String object that contains the substring to match 
	 * @param start marks the starting position (inclusive) of the substring
	 * @param end marks the ending index (exclusive) of the substring 
	 */
	public boolean match(String text) {
		if (text == null)
			return false;
		final int start = 0;
		final int end = text.length();

		int segCount = fSegments.length;
		if (segCount == 0 && (fHasLeadingStar || fHasTrailingStar)) // pattern contains only '*'(s)
			return true;
		if (start == end)
			return fLength == 0;
		if (fLength == 0)
			return false;

		int tCurPos = start;
		if ((end - fBound) < 0)
			return false;
		int i = 0;
		String current = fSegments[i];
		int segLength = current.length();

		/* process first segment */
		if (!fHasLeadingStar) {
			if (!regExpRegionMatches(text, start, current, 0, segLength))
				return false;
			++i;
			tCurPos = tCurPos + segLength;
		}
		if ((fSegments.length == 1) && (!fHasLeadingStar) && (!fHasTrailingStar)) {
			// only one segment to match, no wild cards specified
			return tCurPos == end;
		}
		/* process middle segments */
		while (i < segCount) {
			current = fSegments[i];
			int currentMatch = findPosition(text, tCurPos, end, current);
			if (currentMatch < 0)
				return false;
			tCurPos = currentMatch + current.length();
			i++;
		}

		/* process final segment */
		if (!fHasTrailingStar && tCurPos != end) {
			int clen = current.length();
			return regExpRegionMatches(text, end - clen, current, 0, clen);
		}
		return i == segCount;
	}

	/**
	 * Parses the pattern into segments separated by wildcard '*' characters.
	 */
	private void parseWildCards() {
		if (fPattern.startsWith("*"))//$NON-NLS-1$
			fHasLeadingStar = true;
		if (fPattern.endsWith("*")) {//$NON-NLS-1$
			/* make sure it's not an escaped wildcard */
			if (fLength > 1 && fPattern.charAt(fLength - 2) != '\\') {
				fHasTrailingStar = true;
			}
		}

		ArrayList temp = new ArrayList();

		int pos = 0;
		StringBuffer buf = new StringBuffer();
		while (pos < fLength) {
			char c = fPattern.charAt(pos++);
			switch (c) {
				case '\\' :
					if (pos >= fLength) {
						buf.append(c);
					} else {
						char next = fPattern.charAt(pos++);
						/* if it's an escape sequence */
						if (next == '*' || next == '?' || next == '\\') {
							buf.append(next);
						} else {
							/* not an escape sequence, just insert literally */
							buf.append(c);
							buf.append(next);
						}
					}
					break;
				case '*' :
					if (buf.length() > 0) {
						/* new segment */
						temp.add(buf.toString());
						fBound += buf.length();
						buf.setLength(0);
					}
					break;
				case '?' :
					/* append special character representing single match wildcard */
					buf.append(fSingleWildCard);
					break;
				default :
					buf.append(c);
			}
		}

		/* add last buffer to segment list */
		if (buf.length() > 0) {
			temp.add(buf.toString());
			fBound += buf.length();
		}
		fSegments = (String[]) temp.toArray(new String[temp.size()]);
	}

	/**
	 * 
	 * @return boolean
	 * @param text a String to match
	 * @param start int that indicates the starting index of match, inclusive
	 * @param end int that indicates the ending index of match, exclusive
	 * @param p String,  String, a simple regular expression that may contain '?'
	 */
	private boolean regExpRegionMatches(String text, int tStart, String p, int pStart, int plen) {
		while (plen-- > 0) {
			char tchar = text.charAt(tStart++);
			char pchar = p.charAt(pStart++);

			// process wild cards, skipping single wild cards
			if (pchar == fSingleWildCard)
				continue;
			if (pchar == tchar)
				continue;
			if (Character.toUpperCase(tchar) == Character.toUpperCase(pchar))
				continue;
			// comparing after converting to upper case doesn't handle all cases;
			// also compare after converting to lower case
			if (Character.toLowerCase(tchar) == Character.toLowerCase(pchar))
				continue;
			return false;
		}
		return true;
	}
}