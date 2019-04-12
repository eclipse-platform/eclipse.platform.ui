/*******************************************************************************
 * Copyright (c) 2000, 2017 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Lucas Bullen (Red Hat Inc.) - [Bug 203792] filter should support multiple keywords
 *     Mickael Istria (Red Hat Inc.) - [534277] erroneous filtering with multiple words
 *******************************************************************************/
package org.eclipse.ui.internal.misc;

import com.ibm.icu.text.BreakIterator;
import java.util.ArrayList;
import java.util.List;

/**
 * A string pattern matcher, supporting "*" and "?" wildcards.
 */
public class StringMatcher {
	protected String fPattern;

	protected int fLength; // pattern length

	protected boolean fIgnoreWildCards;

	protected boolean fIgnoreCase;

	protected String[] patternWords;

	protected Word wholePatternWord;
	protected Word[] splittedPatternWords;

	protected static final char fSingleWildCard = '\u0000';

	class Word {
		private boolean hasTrailingStar = false;
		private boolean hasLeadingStar = false;
		private int bound = 0;
		private String[] fragments = null;
		private final String pattern;

		Word(String pattern) {
			this.pattern = pattern;
		}

		public Word(String pattern, int fLength, String[] wordsSplitted) {
			this(pattern);
			this.bound = fLength;
			this.fragments = wordsSplitted;
		}

		private void parseWildcards() {
			if (this.pattern.startsWith("*")) { //$NON-NLS-1$
				this.hasLeadingStar = true;
			}
			if (this.pattern.endsWith("*")) {//$NON-NLS-1$
				/* make sure it's not an escaped wildcard */
				if (this.pattern.length() > 1 && this.pattern.charAt(this.pattern.length() - 2) != '\\') {
					this.hasTrailingStar = true;
				}
			}

			ArrayList<String> temp = new ArrayList<>();

			int pos = 0;
			StringBuilder buf = new StringBuilder();
			while (pos < this.pattern.length()) {
				char c = this.pattern.charAt(pos++);
				switch (c) {
				case '\\':
					if (pos >= this.pattern.length()) {
						buf.append(c);
					} else {
						char next = this.pattern.charAt(pos++);
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
				case '*':
					if (buf.length() > 0) {
						/* new segment */
						temp.add(buf.toString());
						this.bound += buf.length();
						buf.setLength(0);
					}
					break;
				case '?':
					/* append special character representing single match wildcard */
					buf.append(fSingleWildCard);
					break;
				default:
					buf.append(c);
				}
			}

			/* add last buffer to segment list */
			if (buf.length() > 0) {
				temp.add(buf.toString());
				this.bound += buf.length();
			}
			this.fragments = temp.toArray(new String[temp.size()]);
		}

		boolean match(String text, int start, int end) {
			boolean found = true;
			if (fIgnoreWildCards) {
				if ((end - start == this.pattern.length())
						&& this.pattern.regionMatches(fIgnoreCase, 0, text, start, this.pattern.length()))
					return true;
				return false;
			}
			String[] segments = null;
			segments = this.fragments;
			int segCount = segments.length;
			if (segCount == 0 && (this.hasLeadingStar || this.hasTrailingStar)) {
				return true;
			}
			if (start == end) {
				if (this.pattern.length() == 0)
					return true;
				return false;
			}
			if (this.pattern.length() == 0) {
				if (start == end)
					return true;
				return false;
			}

			int tCurPos = start;
			int bound = end - this.bound;
			if (bound < 0) {
				return false;
			}
			int i = 0;
			String current = segments[i];
			int segLength = current.length();

			/* process first segment */
			if (!hasLeadingStar) {
				if (!regExpRegionMatches(text, start, current, 0, segLength)) {
					return false;
				}
				++i;
				tCurPos = tCurPos + segLength;
			}
			if ((segments.length == 1) && (!hasLeadingStar) && (!hasTrailingStar)) {
				// only one segment to match, no wildcards specified
				if (tCurPos == end)
					return true;
				return false;
			}
			/* process middle segments */
			while (i < segCount && found) {
				current = segments[i];
				int currentMatch;
				int k = current.indexOf(fSingleWildCard);
				if (k < 0) {
					currentMatch = textPosIn(text, tCurPos, end, current);
					if (currentMatch < 0) {
						found = false;
					}
				} else {
					currentMatch = regExpPosIn(text, tCurPos, end, current);
					if (currentMatch < 0) {
						found = false;
					}
				}
				if (!found)
					return false;
				tCurPos = currentMatch + current.length();
				i++;
			}

			/* process final segment */
			if (!hasTrailingStar && tCurPos != end) {
				int clen = current.length();
				if (regExpRegionMatches(text, end - clen, current, 0, clen))
					return true;
				return false;
			}
			if (i == segCount)
				return true;
			return false;
		}

		/**
		 * @param text
		 * @param start
		 * @param end
		 * @return whether the current pattern word matches at least one word in the
		 *         given text
		 */
		public boolean matchTextWord(String text, int start, int end) {
			String[] textWords = getWords(text.substring(start, end));
			if (textWords.length == 0) {
				return pattern.isEmpty();
			}
			for (String subword : textWords) {
				if (match(subword, 0, subword.length())) {
					return true;
				}
			}
			return false;
		}

	}

	public static class Position {
		int start; // inclusive

		int end; // exclusive

		public Position(int start, int end) {
			this.start = start;
			this.end = end;
		}

		public int getStart() {
			return start;
		}

		public int getEnd() {
			return end;
		}
	}

	/**
	 * StringMatcher constructor takes in a String object that is a simple pattern
	 * which may contain '*' for 0 and many characters and '?' for exactly one
	 * character.
	 *
	 * Literal '*' and '?' characters must be escaped in the pattern e.g., "\*"
	 * means literal "*", etc.
	 *
	 * Escaping any other character (including the escape character itself), just
	 * results in that character in the pattern. e.g., "\a" means "a" and "\\" means
	 * "\"
	 *
	 * If invoking the StringMatcher with string literals in Java, don't forget
	 * escape characters are represented by "\\".
	 *
	 * @param pattern         the pattern to match text against
	 * @param ignoreCase      if true, case is ignored
	 * @param ignoreWildCards if true, wild cards and their escape sequences are
	 *                        ignored (everything is taken literally).
	 */
	public StringMatcher(String pattern, boolean ignoreCase, boolean ignoreWildCards) {
		if (pattern == null) {
			throw new IllegalArgumentException();
		}
		fIgnoreCase = ignoreCase;
		fIgnoreWildCards = ignoreWildCards;
		fPattern = pattern;
		fLength = pattern.length();

		parsePatternIntoWords();

		if (fIgnoreWildCards) {
			parseNoWildCards();
		} else {
			if (wholePatternWord != null) {
				wholePatternWord.parseWildcards();
			}
			if (splittedPatternWords != null && splittedPatternWords.length > 1) {
				for (Word word : splittedPatternWords) {
					word.parseWildcards();
				}
			}
		}
	}

	/**
	 * match the given <code>text</code> with the pattern
	 * 
	 * @return true if matched otherwise false
	 * @param text a String object
	 */
	public boolean match(String text) {
		if (text == null) {
			return false;
		}
		return match(text, 0, text.length());
	}

	/**
	 * Given the starting (inclusive) and the ending (exclusive) positions in the
	 * <code>text</code>, determine if the given substring matches with aPattern
	 * 
	 * @return true if the specified portion of the text matches the pattern
	 * @param text  a String object that contains the substring to match
	 * @param start marks the starting position (inclusive) of the substring
	 * @param end   marks the ending index (exclusive) of the substring
	 */
	public boolean match(String text, int start, int end) {
		if (null == text) {
			throw new IllegalArgumentException();
		}
		if (start > end) {
			return false;
		}
		int tlen = text.length();
		start = Math.max(0, start);
		end = Math.min(end, tlen);

		if (wholePatternWord != null
				&& (wholePatternWord.match(text, start, end) || wholePatternWord.matchTextWord(text, start, end))) {
			return true;
		}
		if (splittedPatternWords != null && splittedPatternWords.length > 0) {
			for (Word word : splittedPatternWords) {
				if (!word.match(text, start, end) && !word.matchTextWord(text, start, end)) {
					return false;
				}
			}
			return true;
		}
		return false;
	}

	/**
	 * This method parses the given pattern into words separated by spaces
	 * characters. Since wildcards are not being used in this case, the pattern
	 * consists of a single segment.
	 */
	private void parsePatternIntoWords() {
		String trimedPattern = fPattern.trim();
		if (!trimedPattern.isEmpty()) {
			this.wholePatternWord = new Word(trimedPattern);
			patternWords = trimedPattern.split("\\s+"); //$NON-NLS-1$
			if (patternWords.length > 1) {
				this.splittedPatternWords = new Word[patternWords.length];
				for (int i = 0; i < patternWords.length; i++) {
					String patternWord = patternWords[i];
					if (!patternWord.endsWith("*")) { //$NON-NLS-1$
						patternWord += '*';
					}
					this.splittedPatternWords[i] = new Word(patternWord);
					// words may be found anywhere in the line
				}
			}
		}
	}

	/**
	 * This method parses the given pattern into segments seperated by wildcard '*'
	 * characters. Since wildcards are not being used in this case, the pattern
	 * consists of a single segment.
	 */
	private void parseNoWildCards() {
		this.wholePatternWord = new Word(fPattern, fLength, patternWords);
		this.wholePatternWord.bound = fLength;
		this.wholePatternWord.fragments = patternWords;
	}

	/**
	 * @param text  a string which contains no wildcard
	 * @param start the starting index in the text for search, inclusive
	 * @param end   the stopping point of search, exclusive
	 * @return the starting index in the text of the pattern , or -1 if not found
	 */
	protected int posIn(String text, int start, int end) {// no wild card in pattern
		int max = end - fLength;

		if (!fIgnoreCase) {
			int i = text.indexOf(fPattern, start);
			if (i == -1 || i > max) {
				return -1;
			}
			return i;
		}

		for (int i = start; i <= max; ++i) {
			if (text.regionMatches(true, i, fPattern, 0, fLength)) {
				return i;
			}
		}

		return -1;
	}

	/**
	 * @param text  a simple regular expression that may only contain '?'(s)
	 * @param start the starting index in the text for search, inclusive
	 * @param end   the stopping point of search, exclusive
	 * @param p     a simple regular expression that may contains '?'
	 * @return the starting index in the text of the pattern , or -1 if not found
	 */
	protected int regExpPosIn(String text, int start, int end, String p) {
		int plen = p.length();

		int max = end - plen;
		for (int i = start; i <= max; ++i) {
			if (regExpRegionMatches(text, i, p, 0, plen)) {
				return i;
			}
		}
		return -1;
	}

	/**
	 *
	 * @return boolean
	 * @param text       a String to match
	 * @param start      int that indicates the starting index of match, inclusive
	 * @param end</code> int that indicates the ending index of match, exclusive
	 * @param p          String, String, a simple regular expression that may
	 *                   contain '?'
	 * @param ignoreCase boolean indicating whether code>p</code> is case sensitive
	 */
	protected boolean regExpRegionMatches(String text, int tStart, String p, int pStart, int plen) {
		while (plen-- > 0) {
			char tchar = text.charAt(tStart++);
			char pchar = p.charAt(pStart++);

			/* process wild cards */
			if (!fIgnoreWildCards) {
				/* skip single wild cards */
				if (pchar == fSingleWildCard) {
					continue;
				}
			}
			if (pchar == tchar) {
				continue;
			}
			if (fIgnoreCase) {
				if (Character.toUpperCase(tchar) == Character.toUpperCase(pchar)) {
					continue;
				}
				// comparing after converting to upper case doesn't handle all cases;
				// also compare after converting to lower case
				if (Character.toLowerCase(tchar) == Character.toLowerCase(pchar)) {
					continue;
				}
			}
			return false;
		}
		return true;
	}

	/**
	 * @param text  the string to match
	 * @param start the starting index in the text for search, inclusive
	 * @param end   the stopping point of search, exclusive
	 * @param p     a pattern string that has no wildcard
	 * @return the starting index in the text of the pattern , or -1 if not found
	 */
	protected int textPosIn(String text, int start, int end, String p) {

		int plen = p.length();
		int max = end - plen;

		if (!fIgnoreCase) {
			int i = text.indexOf(p, start);
			if (i == -1 || i > max) {
				return -1;
			}
			return i;
		}

		for (int i = start; i <= max; ++i) {
			if (text.regionMatches(true, i, p, 0, plen)) {
				return i;
			}
		}

		return -1;
	}

	/**
	 * Take the given filter text and break it down into words using a
	 * BreakIterator.
	 *
	 * @param text
	 * @return an array of words
	 */
	public static String[] getWords(String text) {
		List<String> words = new ArrayList<>();
		// Break the text up into words, separating based on whitespace and
		// common punctuation.
		// Previously used String.split(..., "\\W"), where "\W" is a regular
		// expression (see the Javadoc for class Pattern).
		// Need to avoid both String.split and regular expressions, in order to
		// compile against JCL Foundation (bug 80053).
		// Also need to do this in an NL-sensitive way. The use of BreakIterator
		// was suggested in bug 90579.
		BreakIterator iter = BreakIterator.getWordInstance();
		iter.setText(text);
		int i = iter.first();
		while (i != java.text.BreakIterator.DONE && i < text.length()) {
			int j = iter.following(i);
			if (j == java.text.BreakIterator.DONE) {
				j = text.length();
			}
			// match the word
			if (Character.isLetterOrDigit(text.charAt(i))) {
				String word = text.substring(i, j);
				words.add(word);
			}
			i = j;
		}
		return words.toArray(new String[words.size()]);
	}

}
