/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.search.internal.core.text;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 *
 */
public class PatternConstructor {
	

	private PatternConstructor() {
		// don't instantiate
	}
	
	public static Pattern createPattern(String pattern, boolean isCaseSensitive, boolean isRegex) throws PatternSyntaxException {
		return createPattern(pattern, isRegex, true, isCaseSensitive, false);
	}

	/**
	 * Creates a pattern element from the pattern string which is either a reg-ex expression or in our old
	 * 'StringMatcher' format.
	 * @param pattern The search pattern
	 * @param isRegex <code>true</code> if the passed string already is a reg-ex pattern
	 * @param isStringMatcher <code>true</code> if the passed string is in the StringMatcher format.
	 * @param isCaseSensitive Set to <code>true</code> to create a case insensitive pattern
	 * @param isWholeWord <code>true</code> to create a pattern that requires a word boundary at the beginning and the end.
	 * @return The created pattern
	 * @throws PatternSyntaxException
	 */
	public static Pattern createPattern(String pattern, boolean isRegex, boolean isStringMatcher, boolean isCaseSensitive, boolean isWholeWord) throws PatternSyntaxException {
		if (isRegex) {
			if (isWholeWord) {
				StringBuffer buffer= new StringBuffer(pattern.length() + 10);
				buffer.append("\\b(?:").append(pattern).append(")\\b"); //$NON-NLS-1$ //$NON-NLS-2$
				pattern= buffer.toString();
			}
		} else {
			int len= pattern.length();
			StringBuffer buffer= new StringBuffer(len + 10);
			// don't add a word boundary if the search text does not start with
			// a word char. (this works around a user input error).
			if (isWholeWord && len > 0 && isWordChar(pattern.charAt(0))) {
				buffer.append("\\b"); //$NON-NLS-1$
			}
			appendAsRegEx(isStringMatcher, pattern, buffer);
			if (isWholeWord && len > 0 && isWordChar(pattern.charAt(len - 1))) {
				buffer.append("\\b"); //$NON-NLS-1$
			}
			pattern= buffer.toString();
		}

		int regexOptions= Pattern.MULTILINE;
		if (!isCaseSensitive) {
			regexOptions|= Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE;
		}
		return Pattern.compile(pattern, regexOptions);
	}
	
    private static boolean isWordChar(char c) {
        return Character.isLetterOrDigit(c);
    }

    /**
	 * Creates a pattern element from an array of patterns in the old
	 * 'StringMatcher' format.
	 * @param patterns The search patterns
	 * @param isCaseSensitive Set to <code>true</code> to create a case insensitive pattern
	 * @return The created pattern
	 * @throws PatternSyntaxException
	 */
	public static Pattern createPattern(String[] patterns, boolean isCaseSensitive) throws PatternSyntaxException {
		StringBuffer pattern= new StringBuffer();
		for (int i= 0; i < patterns.length; i++) {
			if (i > 0) {
                // note that this works only as we know that the operands of the
                // or expression will be simple and need no brackets.
				pattern.append('|');
			}
			appendAsRegEx(true, patterns[i], pattern);
		}
		return createPattern(pattern.toString(), true, true, isCaseSensitive, false);
	}
	
	
	public static StringBuffer appendAsRegEx(boolean isStringMatcher, String pattern, StringBuffer buffer) {
        boolean isEscaped= false;
        for (int i = 0; i < pattern.length(); i++) {
            char c = pattern.charAt(i);
            switch(c) {
            // the backslash
            case '\\':
                // the backslash is escape char in string matcher
                if (isStringMatcher && !isEscaped) {
                    isEscaped= true;
                }
                else {
                    buffer.append("\\\\");  //$NON-NLS-1$
                    isEscaped= false;
                }
                break;
            // characters that need to be escaped in the regex.
            case '(':
            case ')':
            case '{':
            case '}':
            case '.':
            case '[':
            case ']':
            case '$':
            case '^':
            case '+':
            case '|':
                if (isEscaped) {
                    buffer.append("\\\\");  //$NON-NLS-1$
                    isEscaped= false;
                }
                buffer.append('\\');
                buffer.append(c);
                break;
            case '?':
                if (isStringMatcher && !isEscaped) {
                    buffer.append('.');
                }
                else {
                    buffer.append('\\');
                    buffer.append(c);
                    isEscaped= false;
                }
                break;
            case '*':
                if (isStringMatcher && !isEscaped) {
                    buffer.append(".*"); //$NON-NLS-1$
                }
                else {
                    buffer.append('\\');
                    buffer.append(c);
                    isEscaped= false;
                }
                break;
            default:
                if (isEscaped) {
                    buffer.append("\\\\");  //$NON-NLS-1$
                    isEscaped= false;
                }
                buffer.append(c);
                break;
            }
        }
        if (isEscaped) {
            buffer.append("\\\\");  //$NON-NLS-1$
            isEscaped= false;
        }
        return buffer;
    }
}
