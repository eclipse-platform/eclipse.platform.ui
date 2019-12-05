/*******************************************************************************
 * Copyright (c) 2019 Uenal Akkaya and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Uenal Akkaya - initial API and implementation
 *     Patrik Suzzi <psuzzi@gmail.com> - Bug 552144
 *******************************************************************************/
package org.eclipse.ui.dialogs;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.StyledString.Styler;

/**
 * The default implementation of {@link IStyledStringHighlighter}.
 *
 * Highlights all matching groups of filter regular expression using
 * <code>*</code> and <code>?</code> as placeholder in bold.
 *
 * @since 3.115
 */
public class StyledStringHighlighter implements IStyledStringHighlighter {

	private static final String QUOTE_START = "(\\Q"; //$NON-NLS-1$
	private static final String QUOTE_END = "\\E)"; //$NON-NLS-1$
	private static final String DOT_STAR_LAZY = ".*?"; //$NON-NLS-1$
	private static final String DOT = "."; //$NON-NLS-1$
	private static final String QMARK = "?"; //$NON-NLS-1$
	private static final String STAR = "*"; //$NON-NLS-1$
	private static final char TERMINATOR = '<';

	@Override
	public StyledString highlight(String text, String pattern, Styler styler) {
		if (text == null || text.isEmpty()) {
			return new StyledString();
		}
		StyledString styledString = new StyledString(text);

		if (pattern == null || pattern.isEmpty() //
				|| STAR.equals(pattern) || QMARK.equals(pattern)) {
			return styledString;
		}

		pattern = transformWildcardToRegex(pattern);

		try {
			highlight(text, pattern, styledString, styler);
		} catch (Exception e) {
			// in case of an exception a highlighting of the text won't take place
		}

		return styledString;
	}

	/**
	 * Transform the provided wildcard-based pattern into a corresponding regex.
	 * <p>
	 * The provided pattern must not be empty.
	 * <p>
	 * Each uppercase letter starts its own Group
	 *
	 * @param pattern search text with wildcards.
	 * @return regex
	 */
	private static final String transformWildcardToRegex(String pattern) {
		char[] chars = pattern.toCharArray();
		int len = chars.length;
		StringBuilder sb = new StringBuilder();
		boolean quoting = false;
		boolean prevStar = false;
		boolean prevChar = false;
		for (int i = 0; i < len; i++) {
			char c = chars[i];
			boolean isWild = isWildcard(c);
			if (isWild) {
				if (quoting) {
					sb.append(QUOTE_END);
					quoting = false;
				}
				if (c == '*') {
					if (prevStar) {
						continue;
					}
					sb.append(DOT_STAR_LAZY);
				} else {
					sb.append(DOT);
				}
				if (i < len - 1 && !isWildcard(chars[i + 1])) {
					sb.append(QUOTE_START);
					quoting = true;
				}
			} else {
				if (!quoting) {
					sb.append(QUOTE_START);
					quoting = true;
				}
				if (prevChar && Character.isUpperCase(c)) {
					sb.append(QUOTE_END);
					sb.append(DOT_STAR_LAZY);
					sb.append(QUOTE_START);
				}
				if (c != TERMINATOR) {
					sb.append(c);
				}
				if (i == len - 1) {
					sb.append(QUOTE_END);
					quoting = false;
				}
			}
			prevChar = !isWild;
			prevStar = c == '*';
		}
		return sb.toString();
	}

	private static final boolean isWildcard(char c) {
		return c == '?' || c == '*';
	}

	private void highlight(String text, String filterPattern, StyledString styledString, Styler boldStyler) {
		Pattern pattern = Pattern.compile(filterPattern, Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(text);

		while (matcher.find()) {
			int groupCount = matcher.groupCount();
			if (groupCount == 0) {
				styledString.setStyle(matcher.start(), matcher.end() - matcher.start(), boldStyler);
			} else {
				for (int i = 1; i <= groupCount; i++) {
					styledString.setStyle(matcher.start(i), matcher.end(i) - matcher.start(i), boldStyler);
				}
			}
		}
	}

}
