package org.eclipse.jface.text.internal;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class RegExUtils {

	/**
	 * Converts a non-regex string to a pattern
	 * that can be used with the regex search engine.
	 *
	 * @param string the non-regex pattern
	 * @return the string converted to a regex pattern
	 */
	public static String asRegPattern(String string) {
		StringBuilder out= new StringBuilder(string.length());
		boolean quoting= false;

		for (int i= 0, length= string.length(); i < length; i++) {
			char ch= string.charAt(i);
			if (ch == '\\') {
				if (quoting) {
					out.append("\\E"); //$NON-NLS-1$
					quoting= false;
				}
				out.append("\\\\"); //$NON-NLS-1$
				continue;
			}
			if (!quoting) {
				out.append("\\Q"); //$NON-NLS-1$
				quoting= true;
			}
			out.append(ch);
		}
		if (quoting)
			out.append("\\E"); //$NON-NLS-1$

		return out.toString();
	}

	/**
	 * Substitutes \R in a regex find pattern with {@code (?>\r\n?|\n)}
	 *
	 * @param findString the original find pattern
	 * @return the transformed find pattern
	 * @throws PatternSyntaxException if \R is added at an illegal position (e.g. in a character set)
	 * @since 3.4
	 */
	public static String substituteLinebreak(String findString) throws PatternSyntaxException {
		int length= findString.length();
		StringBuilder buf= new StringBuilder(length);

		int inCharGroup= 0;
		int inBraces= 0;
		boolean inQuote= false;
		for (int i= 0; i < length; i++) {
			char ch= findString.charAt(i);
			switch (ch) {
				case '[':
					buf.append(ch);
					if (! inQuote)
						inCharGroup++;
					break;

				case ']':
					buf.append(ch);
					if (! inQuote)
						inCharGroup--;
					break;

				case '{':
					buf.append(ch);
					if (! inQuote && inCharGroup == 0)
						inBraces++;
					break;

				case '}':
					buf.append(ch);
					if (! inQuote && inCharGroup == 0)
						inBraces--;
					break;

				case '\\':
					if (i + 1 < length) {
						char ch1= findString.charAt(i + 1);
						if (inQuote) {
							if (ch1 == 'E')
								inQuote= false;
							buf.append(ch).append(ch1);
							i++;

						} else if (ch1 == 'R') {
							if (inCharGroup > 0 || inBraces > 0) {
								String msg= Messages.RegExUtils_IllegalPositionForRegEx;
								throw new PatternSyntaxException(msg, findString, i);
							}
							buf.append("(?>\\r\\n?|\\n)"); //$NON-NLS-1$
							i++;

						} else {
							if (ch1 == 'Q') {
								inQuote= true;
							}
							buf.append(ch).append(ch1);
							i++;
						}
					} else {
						buf.append(ch);
					}
					break;

				default:
					buf.append(ch);
					break;
			}

		}
		return buf.toString();
	}
	
	/**
	 * Creates the Pattern according to the flags.
	 * @param findString the find string.
	 * @param wholeWord find whole words.
	 * @param caseSensitive search case sensitive.
	 * @param regExSearch is RegEx search
	 * @return a Pattern which can directly be used.
	 */
	public static Pattern createRegexSearchPattern(String findString, boolean wholeWord, boolean caseSensitive, boolean regExSearch) {
		int patternFlags = 0;
		if (regExSearch) {
			patternFlags |= Pattern.MULTILINE;
			findString = RegExUtils.substituteLinebreak(findString);
		}

		if (!caseSensitive)
			patternFlags |= Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE;

		if (!regExSearch)
			findString = RegExUtils.asRegPattern(findString);

		if (wholeWord)
			findString = "\\b" + findString + "\\b"; //$NON-NLS-1$ //$NON-NLS-2$

		return Pattern.compile(findString, patternFlags);
	}

}
