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
package org.eclipse.search.internal.core.text;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 *
 */
public class PatternConstructor {
	

	private PatternConstructor() {
		// don't instanciate
	}

	/**
	 * Creates a pattern element from the pattern string which is either a reg-ex expression or in our old
	 * 'StringMatcher' format.
	 * @param pattern The search pattern
	 * @param isCaseSensitive Set to <code>true</code> to create a case insensitve pattern
	 * @param isRegexSearch <code>true</code> if the passed string already is a reg-ex pattern
	 * @return The created pattern
	 * @throws PatternSyntaxException
	 */
	public static Pattern createPattern(String pattern, boolean isCaseSensitive, boolean isRegexSearch) throws PatternSyntaxException {
		if (!isRegexSearch)
			pattern= asRegEx(pattern);
		
		if (!isCaseSensitive)
			return Pattern.compile(pattern, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE | Pattern.MULTILINE);
		
		return Pattern.compile(pattern, Pattern.MULTILINE);
	}
	
	/*
	 * Converts '*' and '?' to regEx variables.
	 */
	private static String asRegEx(String pattern) {
		
		StringBuffer out= new StringBuffer(pattern.length());
		
		boolean escaped= false;
		boolean quoting= false;
		
		int i= 0;
		while (i < pattern.length()) {
			char ch= pattern.charAt(i++);
			
			if (ch == '*' && !escaped) {
				if (quoting) {
					out.append("\\E"); //$NON-NLS-1$
					quoting= false;
				}
				out.append(".*"); //$NON-NLS-1$
				escaped= false;
				continue;
			} else if (ch == '?' && !escaped) {
				if (quoting) {
					out.append("\\E"); //$NON-NLS-1$
					quoting= false;
				}
				out.append("."); //$NON-NLS-1$
				escaped= false;
				continue;
			} else if (ch == '\\' && !escaped) {
				escaped= true;
				continue;								
				
			} else if (ch == '\\' && escaped) {
				escaped= false;
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
			if (escaped && ch != '*' && ch != '?' && ch != '\\')
				out.append('\\');
			out.append(ch);
			escaped= ch == '\\';
			
		}
		if (quoting)
			out.append("\\E"); //$NON-NLS-1$
		
		return out.toString();
	}

}
