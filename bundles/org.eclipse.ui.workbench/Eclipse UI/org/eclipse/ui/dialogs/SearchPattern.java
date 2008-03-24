/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.dialogs;

import org.eclipse.ui.internal.misc.StringMatcher;

/**
 * A search pattern defines how search results are found.
 * 
 * <p>
 * This class is intended to be subclassed by clients. A default behavior is
 * provided for each of the methods above, that clients can override if they
 * wish.
 * </p>
 * 
 * @since 3.3
 */
public class SearchPattern {

	// Rules for pattern matching: (exact, prefix, pattern) [ | case sensitive]
	/**
	 * Match rule: The search pattern matches exactly the search result, that
	 * is, the source of the search result equals the search pattern. Search pattern
	 * should start from lowerCase char.
	 */
	public static final int RULE_EXACT_MATCH = 0;

	/**
	 * Match rule: The search pattern is a prefix of the search result.
	 */
	public static final int RULE_PREFIX_MATCH = 0x0001;

	/**
	 * Match rule: The search pattern contains one or more wild cards ('*' or
	 * '?'). A '*' wild-card can replace 0 or more characters in the search
	 * result. A '?' wild-card replaces exactly 1 character in the search
	 * result.
	 */
	public static final int RULE_PATTERN_MATCH = 0x0002;

	/**
	 * Match rule: The search pattern matches the search result only if cases
	 * are the same. Can be combined to previous rules, e.g.
	 * {@link #RULE_EXACT_MATCH} | {@link #RULE_CASE_SENSITIVE}
	 */
	public static final int RULE_CASE_SENSITIVE = 0x0008;

	/**
	 * Match rule: The search pattern is blank.
	 */
	public static final int RULE_BLANK_MATCH = 0x0020;

	/**
	 * Match rule: The search pattern contains a Camel Case expression. <br>
	 * Examples:
	 * <ul>
	 * <li><code>NPE</code> type string pattern will match
	 * <code>NullPointerException</code> and
	 * <code>NpPermissionException</code> types,</li>
	 * <li><code>NuPoEx</code> type string pattern will only match
	 * <code>NullPointerException</code> type.</li>
	 * </ul>
	 * 
	 * 
	 * <br>
	 * Can be combined to {@link #RULE_PREFIX_MATCH} match rule. For example,
	 * when prefix match rule is combined with Camel Case match rule,
	 * <code>"nPE"</code> pattern will match <code>nPException</code>. <br>
	 * Match rule {@link #RULE_PATTERN_MATCH} may also be combined but both
	 * rules will not be used simultaneously as they are mutually exclusive.
	 * Used match rule depends on whether string pattern contains specific
	 * pattern characters (e.g. '*' or '?') or not. If it does, then only
	 * Pattern match rule will be used, otherwise only Camel Case match will be
	 * used. For example, with <code>"NPE"</code> string pattern, search will
	 * only use Camel Case match rule, but with <code>N*P*E*</code> string
	 * pattern, it will use only Pattern match rule.
	 * 
	 */
	public static final int RULE_CAMELCASE_MATCH = 0x0080;

	private int matchRule;

	private String stringPattern;

	private String initialPattern;

	private StringMatcher stringMatcher;

	private static final char END_SYMBOL = '<';

	private static final char ANY_STRING = '*';

	private static final char BLANK = ' ';

	private int allowedRules;

	/**
	 * Creates new instance of SearchPattern Default allowedRules for it is
	 * result of belong logic operation: ( RULE_EXACT_MATCH | RULE_PREFIX_MATCH |
	 * RULE_PATTERN_MATCH | RULE_CAMELCASE_MATCH )
	 * 
	 */
	public SearchPattern() {
		this(RULE_EXACT_MATCH | RULE_PREFIX_MATCH | RULE_PATTERN_MATCH
				| RULE_CAMELCASE_MATCH | RULE_BLANK_MATCH);
	}

	/**
	 * Creates a search pattern with the rule to apply for matching index keys.
	 * It can be exact match, prefix match, pattern match or camelCase match.
	 * Rule can also be combined with a case sensitivity flag.
	 * 
	 * @param allowedRules
	 *            one of {@link #RULE_EXACT_MATCH}, {@link #RULE_PREFIX_MATCH},
	 *            {@link #RULE_PATTERN_MATCH}, {@link #RULE_CASE_SENSITIVE},
	 *            {@link #RULE_CAMELCASE_MATCH} combined with one of following
	 *            values: {@link #RULE_EXACT_MATCH}, {@link #RULE_PREFIX_MATCH},
	 *            {@link #RULE_PATTERN_MATCH} or {@link #RULE_CAMELCASE_MATCH}.
	 *            e.g. {@link #RULE_EXACT_MATCH} | {@link #RULE_CASE_SENSITIVE}
	 *            if an exact and case sensitive match is requested,
	 *            {@link #RULE_PREFIX_MATCH} if a prefix non case sensitive
	 *            match is requested or {@link #RULE_EXACT_MATCH} if a non case
	 *            sensitive and erasure match is requested.<br>
	 *            Note also that default behavior for generic types/methods
	 *            search is to find exact matches.
	 */
	public SearchPattern(int allowedRules) {
		this.allowedRules = allowedRules;
	}

	/**
	 * Gets string pattern used by matcher
	 * 
	 * @return pattern
	 */
	public String getPattern() {
		return this.stringPattern;
	}

	/**
	 * @param stringPattern
	 *            The stringPattern to set.
	 */
	public void setPattern(String stringPattern) {
		this.initialPattern = stringPattern;
		this.stringPattern = stringPattern;
		initializePatternAndMatchRule(stringPattern);
		matchRule = matchRule & this.allowedRules;
		if (matchRule == RULE_PATTERN_MATCH) {
			stringMatcher = new StringMatcher(this.stringPattern, true, false);
		}
	}

	/**
	 * Matches text with pattern. matching is determine by matchKind.
	 * 
	 * @param text
	 * @return true if search pattern was matched with text false in other way
	 */
	public boolean matches(String text) {
		switch (matchRule) {
		case RULE_BLANK_MATCH:
			return true;
		case RULE_PATTERN_MATCH:
			return stringMatcher.match(text);
		case RULE_EXACT_MATCH:
			return stringPattern.equalsIgnoreCase(text);
		case RULE_CAMELCASE_MATCH:
			if (camelCaseMatch(stringPattern, text)) {
				return true;
			}
		default:
			return startsWithIgnoreCase(text, stringPattern);
		}
	}

	private void initializePatternAndMatchRule(String pattern) {
		int length = pattern.length();
		if (length == 0) {
			matchRule = RULE_BLANK_MATCH;
			stringPattern = pattern;
			return;
		}
		char last = pattern.charAt(length - 1);

		if (pattern.indexOf('*') != -1 || pattern.indexOf('?') != -1) {
			matchRule = RULE_PATTERN_MATCH;
			switch (last) {
			case END_SYMBOL:
			case BLANK:
				stringPattern = pattern.substring(0, length - 1);
				break;
			case ANY_STRING:
				stringPattern = pattern;
				break;
			default:
				stringPattern = pattern + ANY_STRING;
			}
			return;
		}

		if (validateMatchRule(pattern, RULE_CAMELCASE_MATCH) == RULE_CAMELCASE_MATCH) {
			matchRule = RULE_CAMELCASE_MATCH;
			stringPattern = pattern;
			return;
		}
		
		if (last == END_SYMBOL || last == BLANK) {
			matchRule = RULE_EXACT_MATCH;
			stringPattern = pattern.substring(0, length - 1);
			return;
		}

		matchRule = RULE_PREFIX_MATCH;
		stringPattern = pattern;

	}

	/**
	 * @param text
	 * @param prefix
	 * @return true if text starts with given prefix, ignoring case false in
	 *         other way
	 */
	private boolean startsWithIgnoreCase(String text, String prefix) {
		int textLength = text.length();
		int prefixLength = prefix.length();
		if (textLength < prefixLength)
			return false;
		for (int i = prefixLength - 1; i >= 0; i--) {
			if (Character.toLowerCase(prefix.charAt(i)) != Character
					.toLowerCase(text.charAt(i)))
				return false;
		}
		return true;
	}

	/**
	 * Answers true if the pattern matches the given name using CamelCase rules,
	 * or false otherwise. CamelCase matching does NOT accept explicit
	 * wild-cards '*' and '?' and is inherently case sensitive. <br>
	 * CamelCase denotes the convention of writing compound names without
	 * spaces, and capitalizing every term. This function recognizes both upper
	 * and lower CamelCase, depending whether the leading character is
	 * capitalized or not. The leading part of an upper CamelCase pattern is
	 * assumed to contain a sequence of capitals which are appearing in the
	 * matching name; e.g. 'NPE' will match 'NullPointerException', but not
	 * 'NewPerfData'. A lower CamelCase pattern uses a lowercase first
	 * character. In Java, type names follow the upper CamelCase convention,
	 * whereas method or field names follow the lower CamelCase convention. <br>
	 * The pattern may contain lowercase characters, which will be match in a
	 * case sensitive way. These characters must appear in sequence in the name.
	 * For instance, 'NPExcep' will match 'NullPointerException', but not
	 * 'NullPointerExCEPTION' or 'NuPoEx' will match 'NullPointerException', but
	 * not 'NoPointerException'. <br>
	 * <br>
	 * Examples:
	 * <ol>
	 * <li>
	 * 
	 * <pre>
	 *                 pattern = &quot;NPE&quot;
	 *                 name = NullPointerException / NoPermissionException
	 *                 result =&gt; true
	 * </pre>
	 * 
	 * </li>
	 * <li>
	 * 
	 * <pre>
	 *                 pattern = &quot;NuPoEx&quot;
	 *                 name = NullPointerException
	 *                 result =&gt; true
	 * </pre>
	 * 
	 * </li>
	 * <li>
	 * 
	 * <pre>
	 *                 pattern = &quot;npe&quot;
	 *                 name = NullPointerException
	 *                 result =&gt; false
	 * </pre>
	 * 
	 * </li>
	 * </ol>
	 * 
	 * @param pattern
	 *            the given pattern
	 * @param name
	 *            the given name
	 * @return true if the pattern matches the given name, false otherwise
	 * 
	 */
	private boolean camelCaseMatch(String pattern, String name) {
		if (pattern == null)
			return true; // null pattern is equivalent to '*'
		if (name == null)
			return false; // null name cannot match

		return camelCaseMatch(pattern, 0, pattern.length(), name, 0, name
				.length());
	}

	/**
	 * Answers true if a sub-pattern matches the subpart of the given name using
	 * CamelCase rules, or false otherwise. CamelCase matching does NOT accept
	 * explicit wild-cards '*' and '?' and is inherently case sensitive. Can
	 * match only subset of name/pattern, considering end positions as
	 * non-inclusive. The subpattern is defined by the patternStart and
	 * patternEnd positions. <br>
	 * CamelCase denotes the convention of writing compound names without
	 * spaces, and capitalizing every term. This function recognizes both upper
	 * and lower CamelCase, depending whether the leading character is
	 * capitalized or not. The leading part of an upper CamelCase pattern is
	 * assumed to contain a sequence of capitals which are appearing in the
	 * matching name; e.g. 'NPE' will match 'NullPointerException', but not
	 * 'NewPerfData'. A lower CamelCase pattern uses a lowercase first
	 * character. In Java, type names follow the upper CamelCase convention,
	 * whereas method or field names follow the lower CamelCase convention. <br>
	 * The pattern may contain lowercase characters, which will be match in a
	 * case sensitive way. These characters must appear in sequence in the name.
	 * For instance, 'NPExcep' will match 'NullPointerException', but not
	 * 'NullPointerExCEPTION' or 'NuPoEx' will match 'NullPointerException', but
	 * not 'NoPointerException'. <br>
	 * <br>
	 * Examples:
	 * <ol>
	 * <li>
	 * 
	 * <pre>
	 *                 pattern = &quot;NPE&quot;
	 *                 patternStart = 0
	 *                 patternEnd = 3
	 *                 name = NullPointerException
	 *                 nameStart = 0
	 *                 nameEnd = 20
	 *                 result =&gt; true
	 * </pre>
	 * 
	 * </li>
	 * <li>
	 * 
	 * <pre>
	 *                 pattern = &quot;NPE&quot;
	 *                 patternStart = 0
	 *                 patternEnd = 3
	 *                 name = NoPermissionException
	 *                 nameStart = 0
	 *                 nameEnd = 21
	 *                 result =&gt; true
	 * </pre>
	 * 
	 * </li>
	 * <li>
	 * 
	 * <pre>
	 *                 pattern = &quot;NuPoEx&quot;
	 *                 patternStart = 0
	 *                 patternEnd = 6
	 *                 name = NullPointerException
	 *                 nameStart = 0
	 *                 nameEnd = 20
	 *                 result =&gt; true
	 * </pre>
	 * 
	 * </li>
	 * <li>
	 * 
	 * <pre>
	 *                 pattern = &quot;NuPoEx&quot;
	 *                 patternStart = 0
	 *                 patternEnd = 6
	 *                 name = NoPermissionException
	 *                 nameStart = 0
	 *                 nameEnd = 21
	 *                 result =&gt; false
	 * </pre>
	 * 
	 * </li>
	 * <li>
	 * 
	 * <pre>
	 *                 pattern = &quot;npe&quot;
	 *                 patternStart = 0
	 *                 patternEnd = 3
	 *                 name = NullPointerException
	 *                 nameStart = 0
	 *                 nameEnd = 20
	 *                 result =&gt; false
	 * </pre>
	 * 
	 * </li>
	 * </ol>
	 * 
	 * @param pattern
	 *            the given pattern
	 * @param patternStart
	 *            the start index of the pattern, inclusive
	 * @param patternEnd
	 *            the end index of the pattern, exclusive
	 * @param name
	 *            the given name
	 * @param nameStart
	 *            the start index of the name, inclusive
	 * @param nameEnd
	 *            the end index of the name, exclusive
	 * @return true if a sub-pattern matches the subpart of the given name,
	 *         false otherwise
	 */
	private boolean camelCaseMatch(String pattern, int patternStart,
			int patternEnd, String name, int nameStart, int nameEnd) {
		if (name == null)
			return false; // null name cannot match
		if (pattern == null)
			return true; // null pattern is equivalent to '*'
		if (patternEnd < 0)
			patternEnd = pattern.length();
		if (nameEnd < 0)
			nameEnd = name.length();

		if (patternEnd <= patternStart)
			return nameEnd <= nameStart;
		if (nameEnd <= nameStart)
			return false;
		// check first pattern char
		if (name.charAt(nameStart) != pattern.charAt(patternStart)) {
			// first char must strictly match (upper/lower)
			return false;
		}

		int patternLength = patternEnd;
		
		if (pattern.charAt(patternEnd - 1) == END_SYMBOL || pattern.charAt(patternEnd - 1) == BLANK )
			patternLength = patternEnd - 1;


		char patternChar, nameChar;
		int iPattern = patternStart;
		int iName = nameStart;

		// Main loop is on pattern characters
		while (true) {

			iPattern++;
			iName++;

			if (iPattern == patternEnd) {
				// We have exhausted pattern, so it's a match
				return true;
			}

			if (iName == nameEnd) {
				if (iPattern == patternLength) 
					return true;
				// We have exhausted name (and not pattern), so it's not a match
				return false;
			}

			// For as long as we're exactly matching, bring it on (even if it's
			// a lower case character)
			if ((patternChar = pattern.charAt(iPattern)) == name.charAt(iName)) {
				continue;
			}

			// If characters are not equals, then it's not a match if
			// patternChar is lowercase
			if (!isPatternCharAllowed(patternChar))
				return false;

			// patternChar is uppercase, so let's find the next uppercase in
			// name
			while (true) {
				if (iName == nameEnd) {
					if ((iPattern == patternLength) && (patternChar == END_SYMBOL || patternChar == BLANK))
						return true;
					return false;
				}

				nameChar = name.charAt(iName);

				if ((iPattern == patternLength) && (patternChar == END_SYMBOL || patternChar == BLANK)) {
					if (isNameCharAllowed(nameChar)) {
						return false;
					}
					iName++;
					continue;
				}

				if (!isNameCharAllowed(nameChar)) {
					// nameChar is lowercase
					iName++;
					// nameChar is uppercase...
				} else if (patternChar != nameChar) {
					// .. and it does not match patternChar, so it's not a match
					return false;
				} else {
					// .. and it matched patternChar. Back to the big loop
					break;
				}
			}
			// At this point, either name has been exhausted, or it is at an
			// uppercase letter.
			// Since pattern is also at an uppercase letter
		}
	}

	/**
	 * Checks pattern's character is allowed for specified set. It could be
	 * override if you want change logic of camelCaseMatch methods.
	 * 
	 * @param patternChar
	 * @return true if patternChar is in set of allowed characters for pattern
	 */
	protected boolean isPatternCharAllowed(char patternChar) {
		return Character.isUpperCase(patternChar) || patternChar == END_SYMBOL
				|| patternChar == BLANK;
	}

	/**
	 * Checks character of element's name is allowed for specified set. It could
	 * be override if you want change logic of camelCaseMatch methods.
	 * 
	 * @param nameChar -
	 *            name of searched element
	 * @return if nameChar is in set of allowed characters for name of element
	 */
	protected boolean isNameCharAllowed(char nameChar) {
		return Character.isUpperCase(nameChar);
	}

	/**
	 * Returns the rule to apply for matching keys. Can be exact match, prefix
	 * match, pattern match or camelcase match. Rule can also be combined with a
	 * case sensitivity flag.
	 * 
	 * @return one of RULE_EXACT_MATCH, RULE_PREFIX_MATCH, RULE_PATTERN_MATCH,
	 *         RULE_CAMELCASE_MATCH, combined with RULE_CASE_SENSITIVE, e.g.
	 *         RULE_EXACT_MATCH | RULE_CASE_SENSITIVE if an exact and case
	 *         sensitive match is requested, or RULE_PREFIX_MATCH if a prefix
	 *         non case sensitive match is requested.
	 */
	public final int getMatchRule() {
		return this.matchRule;
	}

	/**
	 * Validate compatibility between given string pattern and match rule. <br>
	 * Optimized (ie. returned match rule is modified) combinations are:
	 * <ul>
	 * <li>{@link #RULE_PATTERN_MATCH} without any '*' or '?' in string
	 * pattern: pattern match bit is unset, </li>
	 * <li>{@link #RULE_PATTERN_MATCH} and {@link #RULE_PREFIX_MATCH} bits
	 * simultaneously set: prefix match bit is unset, </li>
	 * <li>{@link #RULE_PATTERN_MATCH} and {@link #RULE_CAMELCASE_MATCH} bits
	 * simultaneously set: camel case match bit is unset, </li>
	 * <li>{@link #RULE_CAMELCASE_MATCH} with invalid combination of uppercase
	 * and lowercase characters: camel case match bit is unset and replaced with
	 * prefix match pattern, </li>
	 * <li>{@link #RULE_CAMELCASE_MATCH} combined with
	 * {@link #RULE_PREFIX_MATCH} and {@link #RULE_CASE_SENSITIVE} bits is
	 * reduced to only {@link #RULE_CAMELCASE_MATCH} as Camel Case search is
	 * already prefix and case sensitive, </li>
	 * </ul>
	 * <br>
	 * Rejected (ie. returned match rule -1) combinations are:
	 * <ul>
	 * <li>{@link #RULE_REGEXP_MATCH} with any other match mode bit set, </li>
	 * </ul>
	 * 
	 * @param stringPattern
	 *            The string pattern
	 * @param matchRule
	 *            The match rule
	 * @return Optimized valid match rule or -1 if an incompatibility was
	 *         detected.
	 */
	private int validateMatchRule(String stringPattern, int matchRule) {

		// Verify Pattern match rule
		int starIndex = stringPattern.indexOf('*');
		int questionIndex = stringPattern.indexOf('?');
		if (starIndex < 0 && questionIndex < 0) {
			// reset pattern match bit if any
			matchRule &= ~RULE_PATTERN_MATCH;
		} else {
			// force Pattern rule
			matchRule |= RULE_PATTERN_MATCH;
		}
		if ((matchRule & RULE_PATTERN_MATCH) != 0) {
			// remove Camel Case and Prefix match bits if any
			matchRule &= ~RULE_CAMELCASE_MATCH;
			matchRule &= ~RULE_PREFIX_MATCH;
		}

		// Verify Camel Case match rule
		if ((matchRule & RULE_CAMELCASE_MATCH) != 0) {
			// Verify sting pattern validity
			int length = stringPattern.length();
			boolean validCamelCase = true;
			for (int i = 0; i < length && validCamelCase; i++) {
				char ch = stringPattern.charAt(i);
				validCamelCase = isValidCamelCaseChar(ch);
			}
			validCamelCase = validCamelCase && Character.isUpperCase(stringPattern.charAt(0));
			// Verify bits compatibility
			if (validCamelCase) {
				if ((matchRule & RULE_PREFIX_MATCH) != 0) {
					if ((matchRule & RULE_CASE_SENSITIVE) != 0) {
						// This is equivalent to Camel Case match rule
						matchRule &= ~RULE_PREFIX_MATCH;
						matchRule &= ~RULE_CASE_SENSITIVE;
					}
				}
			} else {
				matchRule &= ~RULE_CAMELCASE_MATCH;
				if ((matchRule & RULE_PREFIX_MATCH) == 0) {
					matchRule |= RULE_PREFIX_MATCH;
					matchRule |= RULE_CASE_SENSITIVE;
				}
			}
		}
		return matchRule;
	}

	/**
	 * Check if character is valid camelCase character
	 * 
	 * @param ch
	 *            character to be validated
	 * @return true if character is valid
	 */
	protected boolean isValidCamelCaseChar(char ch) {
		return true;
	}

	/**
	 * Tells whether the given <code>SearchPattern</code> equals this pattern.
	 * 
	 * @param pattern
	 *            pattern to be checked
	 * @return true if the given pattern equals this search pattern
	 */
	public boolean equalsPattern(SearchPattern pattern) {
		return trimWildcardCharacters(pattern.initialPattern).equals(
				trimWildcardCharacters(this.initialPattern));
	}

	/**
	 * Tells whether the given <code>SearchPattern</code> is a sub-pattern of
	 * this pattern.
	 * <p>
	 * <i>WARNING: This method is <b>not</b> defined in reading order, i.e.
	 * <code>a.isSubPattern(b)</code> is <code>true</code> iff
	 * <code>b</code> is a sub-pattern of <code>a</code>, and not vice-versa.
	 * </i>
	 * </p>
	 * 
	 * @param pattern
	 *            pattern to be checked
	 * @return true if the given pattern is a sub pattern of this search pattern
	 */
	public boolean isSubPattern(SearchPattern pattern) {
		return trimWildcardCharacters(pattern.initialPattern).startsWith(
				trimWildcardCharacters(this.initialPattern));
	}

	/**
	 * Trims sequences of '*' characters
	 * 
	 * @param pattern
	 *            string to be trimmed
	 * @return trimmed pattern
	 */
	private String trimWildcardCharacters(String pattern) {
		return pattern.replaceAll("\\*+", "\\*"); //$NON-NLS-1$ //$NON-NLS-2$		}
	}

}
