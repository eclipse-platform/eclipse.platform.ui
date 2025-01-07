/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
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
 *******************************************************************************/
package org.eclipse.ui.dialogs;

import org.eclipse.core.text.StringMatcher;
import org.eclipse.jface.util.Util;

/**
 * A search pattern defines how search results are found.
 * <p>
 * This class is intended to be subclassed by clients. A default behavior is
 * provided that clients can override if they wish.
 *
 * @since 3.3
 */
public class SearchPattern {

	// Rules for pattern matching:
	/**
	 * Match rule: The search pattern matches exactly the search result, that is,
	 * the source of the search result equals the search pattern. Search pattern
	 * should start from lowerCase char.
	 */
	public static final int RULE_EXACT_MATCH = 0;

	/**
	 * Match rule: The search pattern is a prefix of the search result.
	 */
	public static final int RULE_PREFIX_MATCH = 0x0001;

	/**
	 * Match rule: The search pattern contains one or more wild cards ('*' or '?').
	 * A '*' wild-card can replace 0 or more characters in the search result. A '?'
	 * wild-card replaces exactly 1 character in the search result.
	 * <p>
	 * Unless the pattern ends with ' ' or '>', search is performed as if '*' was
	 * specified at the end of the pattern.
	 * <p>
	 * When {@link #RULE_SUBSTRING_MATCH} is in effect, search is performed as if
	 * '*' was specified at the start of the pattern.
	 */
	public static final int RULE_PATTERN_MATCH = 0x0002;

	/**
	 * Match rule: The search pattern matches the search result only if cases are
	 * the same. Can be combined with previous rules, e.g. {@link #RULE_EXACT_MATCH}
	 * | {@link #RULE_CASE_SENSITIVE}.
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
	 * <code>NullPointerException</code> and <code>NpPermissionException</code>
	 * types,</li>
	 * <li><code>NuPoEx</code> type string pattern will only match
	 * <code>NullPointerException</code> type.</li>
	 * </ul>
	 *
	 * <br>
	 * Can be combined with {@link #RULE_PREFIX_MATCH} match rule. For example, when
	 * prefix match rule is combined with Camel Case match rule, <code>"nPE"</code>
	 * pattern will match <code>nPException</code>. <br>
	 * Match rule {@link #RULE_PATTERN_MATCH} may also be combined but both rules
	 * will not be used simultaneously as they are mutually exclusive. Used match
	 * rule depends on whether string pattern contains specific pattern characters
	 * (e.g. '*' or '?') or not. If it does, then only Pattern match rule will be
	 * used, otherwise only Camel Case match will be used. For example, with
	 * <code>"NPE"</code> string pattern, search will only use Camel Case match
	 * rule, but with <code>N*P*E*</code> string pattern, it will use only Pattern
	 * match rule.
	 * <p>
	 * Unless {@link #RULE_SUBSTRING_MATCH} is in effect, it is required that
	 * the 1st pattern's char must match the very 1st char of the search result.
	 */
	public static final int RULE_CAMELCASE_MATCH = 0x0080;

	/**
	 * Match rule: The search pattern is a string placed anywhere in the search
	 * result. When in effect, this flag also affects some of the other match rules
	 * (see their documentation for details).
	 * <p>
	 * Prefix search may still be enforced by placing '>' at the beginning of the
	 * pattern. Analogically, suffix search may be enforced by placing ' ' or '&lt;'
	 * at the end of the pattern.
	 *
	 * @since 3.128
	 */
	public static final int RULE_SUBSTRING_MATCH = 0x0200;

	/**
	 * The default set of match rules as used by the no-argument constructor.
	 *
	 * @since 3.128
	 */
	public static final int DEFAULT_MATCH_RULES = RULE_EXACT_MATCH | RULE_PREFIX_MATCH | RULE_PATTERN_MATCH
			| RULE_CAMELCASE_MATCH | RULE_BLANK_MATCH;

	private int matchRule;

	private String stringPattern;

	private String initialPattern;

	private StringMatcher stringMatcher;

	private static final char START_SYMBOL = '>';

	private static final char END_SYMBOL = '<';

	private static final char ANY_STRING = '*';

	private static final char BLANK = ' ';

	private int allowedRules;

	private boolean substringSearch;

	private boolean matchPrefix;

	private boolean matchSuffix;

	/**
	 * Creates a new instance of SearchPattern with {@link #DEFAULT_MATCH_RULES
	 * default set of rules} configured.
	 */
	public SearchPattern() {
		this(DEFAULT_MATCH_RULES);
	}

	/**
	 * Creates a search pattern with a rule or rules to apply for matching index keys.
	 *
	 * @param allowedRules one of {@link #RULE_EXACT_MATCH},
	 *                     {@link #RULE_PREFIX_MATCH},
	 *                     {@link #RULE_SUBSTRING_MATCH},
	 *                     {@link #RULE_PATTERN_MATCH},
	 *                     {@link #RULE_CAMELCASE_MATCH},
	 *                     {@link #RULE_CASE_SENSITIVE}, or their combination in
	 *                     order to enable more types of matching. Note that rules
	 *                     {@link #RULE_CASE_SENSITIVE} and
	 *                     {@link #RULE_SUBSTRING_MATCH} are special in that they
	 *                     generally just affect how the other match rules
	 *                     behave.<br>
	 *                     Examples: {@link #RULE_EXACT_MATCH} |
	 *                     {@link #RULE_CASE_SENSITIVE} if an exact and case
	 *                     sensitive match is requested, {@link #RULE_PREFIX_MATCH}
	 *                     if a prefix non case sensitive match is requested, or
	 *                     {@link #RULE_EXACT_MATCH} if a non case sensitive and
	 *                     erasure match is requested.<br>
	 */
	public SearchPattern(int allowedRules) {
		this.allowedRules = allowedRules;
		this.substringSearch = (allowedRules & RULE_SUBSTRING_MATCH) != 0;
	}

	/**
	 * Gets string pattern used by matcher.
	 *
	 * @return pattern
	 */
	public String getPattern() {
		return this.stringPattern;
	}

	/**
	 * Gets the initial (input) string pattern.
	 *
	 * @return pattern
	 * @since 3.128
	 */
	public String getInitialPattern() {
		return this.initialPattern;
	}

	/**
	 * @param stringPattern The stringPattern to set.
	 */
	public void setPattern(String stringPattern) {
		this.initialPattern = stringPattern;
		this.stringPattern = stringPattern;
		initializePatternAndMatchRule(stringPattern);
		matchRule = matchRule & this.allowedRules;
		if (matchRule == RULE_PATTERN_MATCH) {
			stringMatcher = new StringMatcher(this.stringPattern.trim(), true, false);
		}
	}

	/**
	 * Matches text with pattern. The way of matching is determined by the current
	 * pattern setup - see {@link #getMatchRule()} for details.
	 * <p>
	 * The default implementation generally does only case-insensitive searches,
	 * i.e. {@link #RULE_CASE_SENSITIVE} is not considered here.
	 *
	 * @param text the text to match
	 * @return true if search pattern was matched with text
	 */
	public boolean matches(String text) {
		switch (matchRule) {
		case RULE_BLANK_MATCH:
			return true;
		case RULE_PATTERN_MATCH:
			return stringMatcher.matchWords(text);
		case RULE_EXACT_MATCH:
			return stringPattern.equalsIgnoreCase(text);
		case RULE_CAMELCASE_MATCH:
			if (camelCaseMatch(stringPattern, text)) {
				return true;
			}
			//$FALL-THROUGH$
		default:
			// apply RULE_PREFIX_MATCH / RULE_SUBSTRING_MATCH
			boolean doMatchPrefix = matchPrefix || !substringSearch;
			if (doMatchPrefix && !matchSuffix) {
				return startsWithIgnoreCase(text, stringPattern);
			}
			if (!doMatchPrefix && matchSuffix) {
				return endsWithIgnoreCase(text, stringPattern);
			}
			if (doMatchPrefix && matchSuffix) {
				// the same as RULE_EXACT_MATCH
				return stringPattern.equalsIgnoreCase(text);
			}
			return text.toLowerCase().contains(stringPattern.toLowerCase());
		}
	}

	private void initializePatternAndMatchRule(String pattern) {
		if (pattern.length() == 0) {
			matchRule = RULE_BLANK_MATCH;
			stringPattern = pattern;
			return;
		}

		// pre-process the string pattern
		char first = pattern.charAt(0);
		char last = pattern.charAt(pattern.length() - 1);
		// note: a file name might start with a space => we can't use it for enforcing prefix match
		matchPrefix = pattern.length() > 1 && first == START_SYMBOL;
		matchSuffix = pattern.length() > (matchPrefix ? 2 : 1) && (last == END_SYMBOL || last == BLANK);
		stringPattern = pattern;
		if (matchPrefix) {
			stringPattern = stringPattern.substring(1);
		}
		if (matchSuffix) {
			stringPattern = stringPattern.substring(0, stringPattern.length() - 1);
		}

		if (pattern.indexOf('*') != -1 || pattern.indexOf('?') != -1) {
			matchRule = RULE_PATTERN_MATCH;
			if (substringSearch && !matchPrefix && first != ANY_STRING) {
				stringPattern = ANY_STRING + stringPattern;
			}
			if (!matchSuffix && last != ANY_STRING) {
				stringPattern += ANY_STRING;
			}
			return;
		}

		if (validateMatchRule(stringPattern, RULE_CAMELCASE_MATCH) == RULE_CAMELCASE_MATCH) {
			matchRule = RULE_CAMELCASE_MATCH;
			return;
		}

		if ((!substringSearch || matchPrefix) && matchSuffix) {
			matchRule = RULE_EXACT_MATCH;
			return;
		}

		matchRule = RULE_PREFIX_MATCH;
	}

	/**
	 * @return true if text starts with given prefix, ignoring case
	 */
	private boolean startsWithIgnoreCase(String text, String prefix) {
		int textLength = text.length();
		int prefixLength = prefix.length();
		if (textLength < prefixLength)
			return false;
		for (int i = prefixLength - 1; i >= 0; i--) {
			if (Character.toLowerCase(prefix.charAt(i)) != Character.toLowerCase(text.charAt(i)))
				return false;
		}
		return true;
	}

	/**
	 * @return true if text ends with given suffix, ignoring case; false in other
	 *         way
	 */
	private boolean endsWithIgnoreCase(String text, String suffix) {
		int textLength = text.length();
		int suffixLength = suffix.length();
		if (textLength < suffixLength)
			return false;
		return startsWithIgnoreCase(text.substring(textLength - suffixLength), suffix);
	}

	/**
	 * Answers true if the pattern matches the given name using CamelCase rules, or
	 * false otherwise. CamelCase matching does NOT accept explicit wild-cards '*'
	 * and '?' and is inherently case sensitive. <br>
	 * CamelCase denotes the convention of writing compound names without spaces,
	 * and capitalizing every term. This function recognizes both upper and lower
	 * CamelCase, depending whether the leading character is capitalized or not. The
	 * leading part of an upper CamelCase pattern is assumed to contain a sequence
	 * of capitals which are appearing in the matching name; e.g. 'NPE' will match
	 * 'NullPointerException', but not 'NewPerfData'. A lower CamelCase pattern uses
	 * a lowercase first character. In Java, type names follow the upper CamelCase
	 * convention, whereas method or field names follow the lower CamelCase
	 * convention. <br>
	 * The pattern may contain lowercase characters, which will be match in a case
	 * sensitive way. These characters must appear in sequence in the name. For
	 * instance, 'NPExcep' will match 'NullPointerException', but not
	 * 'NullPointerExCEPTION' or 'NuPoEx' will match 'NullPointerException', but not
	 * 'NoPointerException'. <br>
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
	 * @param pattern the given pattern
	 * @param name    the given name
	 * @return true if the pattern matches the given name, false otherwise
	 */
	private boolean camelCaseMatch(String pattern, String name) {
		if (pattern == null)
			return true; // null pattern is equivalent to '*'
		if (name == null)
			return false; // null name cannot match

		return camelCaseMatch(pattern, 0, pattern.length(), name, 0, name.length());
	}

	/**
	 * Answers true if a sub-pattern matches the subpart of the given name using
	 * CamelCase rules, or false otherwise. CamelCase matching does NOT accept
	 * explicit wild-cards '*' and '?' and is inherently case sensitive. Can match
	 * only subset of name/pattern, considering end positions as non-inclusive. The
	 * subpattern is defined by the patternStart and patternEnd positions. <br>
	 * CamelCase denotes the convention of writing compound names without spaces,
	 * and capitalizing every term. This function recognizes both upper and lower
	 * CamelCase, depending whether the leading character is capitalized or not. The
	 * leading part of an upper CamelCase pattern is assumed to contain a sequence
	 * of capitals which are appearing in the matching name; e.g. 'NPE' will match
	 * 'NullPointerException', but not 'NewPerfData'. A lower CamelCase pattern uses
	 * a lowercase first character. In Java, type names follow the upper CamelCase
	 * convention, whereas method or field names follow the lower CamelCase
	 * convention. <br>
	 * The pattern may contain lowercase characters, which will be match in a case
	 * sensitive way. These characters must appear in sequence in the name. For
	 * instance, 'NPExcep' will match 'NullPointerException', but not
	 * 'NullPointerExCEPTION' or 'NuPoEx' will match 'NullPointerException', but not
	 * 'NoPointerException'. <br>
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
	 * @param pattern      the given pattern
	 * @param patternStart the start index of the pattern, inclusive
	 * @param patternEnd   the end index of the pattern, exclusive
	 * @param name         the given name
	 * @param nameStart    the start index of the name, inclusive
	 * @param nameEnd      the end index of the name, exclusive
	 * @return true if a sub-pattern matches the subpart of the given name, false
	 *         otherwise
	 */
	private boolean camelCaseMatch(String pattern, int patternStart, int patternEnd, String name, int nameStart,
			int nameEnd) {
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
			if (!this.substringSearch || this.matchPrefix) {
				return false;
			}
			nameStart = name.indexOf(pattern.charAt(patternStart), nameStart + 1);
			if (nameStart < 0) {
				return false;
			}
		}

		char patternChar, nameChar;
		int iPattern = patternStart;
		int iName = nameStart;

		// Main loop starts from the 2nd characters...
		while (true) {

			iPattern++;
			iName++;

			if (iPattern == patternEnd) {
				// We have exhausted pattern, so it might be a match
				if (!this.matchSuffix) {
					return true;
				}
				for (int i = iName; i < nameEnd; i++) {
					if (isNameCharAllowed(name.charAt(i))) {
						// There is another uppercase further in the name, but the pattern doesn't allow
						// this
						return false;
					}
				}
				return true;
			}

			if (iName == nameEnd) {
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

			// patternChar is uppercase, so let's find the next patternChar-matching uppercase in
			// name
			while (true) {
				if (iName == nameEnd) {
					// We have exhausted name (and not pattern), so it's not a match
					return false;
				}

				nameChar = name.charAt(iName);

				if (Character.isDigit(nameChar)) {
					// nameChar is digit => break if the digit is current pattern character
					// otherwise consume it
					if (patternChar == nameChar)
						break;
					iName++;
				} else if (!isNameCharAllowed(nameChar)) {
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
	 * Checks pattern's character is allowed for specified set. It could be overridden
	 * if you want to change logic of camelCaseMatch methods.
	 *
	 * @param patternChar the char to check
	 * @return true if patternChar is in set of allowed characters for pattern
	 */
	protected boolean isPatternCharAllowed(char patternChar) {
		return patternChar == END_SYMBOL || patternChar == BLANK || Character.isUpperCase(patternChar)
				|| Character.isDigit(patternChar);
	}

	/**
	 * Checks character of element's name is allowed for specified set. It could be
	 * overridden if you want to change logic of camelCaseMatch methods.
	 *
	 * @param nameChar - name of searched element
	 * @return if nameChar is in set of allowed characters for name of element
	 */
	protected boolean isNameCharAllowed(char nameChar) {
		return Character.isUpperCase(nameChar);
	}

	/**
	 * Returns the active rule to apply for matching keys, based on the currently
	 * set {@link #setPattern(String) pattern} and allowed rules passed to the
	 * {@link #SearchPattern(int) constructor}.
	 *
	 * @return one of {@link #RULE_BLANK_MATCH}, {@link #RULE_EXACT_MATCH},
	 *         {@link #RULE_PREFIX_MATCH}, {@link #RULE_PATTERN_MATCH},
	 *         {@link #RULE_CAMELCASE_MATCH}
	 */
	public final int getMatchRule() {
		return this.matchRule;
	}

	/**
	 * Validate compatibility between given string pattern and match rule. <br>
	 * Optimized (ie. returned match rule is modified) combinations are:
	 * <ul>
	 * <li>{@link #RULE_PATTERN_MATCH} without any '*' or '?' in string pattern:
	 * pattern match bit is unset,</li>
	 * <li>{@link #RULE_PATTERN_MATCH} and {@link #RULE_PREFIX_MATCH} bits
	 * simultaneously set: prefix match bit is unset,</li>
	 * <li>{@link #RULE_PATTERN_MATCH} and {@link #RULE_CAMELCASE_MATCH} bits
	 * simultaneously set: camel case match bit is unset,</li>
	 * <li>{@link #RULE_CAMELCASE_MATCH} with invalid combination of uppercase and
	 * lowercase characters: camel case match bit is unset and replaced with prefix
	 * match pattern,</li>
	 * <li>{@link #RULE_CAMELCASE_MATCH} combined with {@link #RULE_PREFIX_MATCH}
	 * and {@link #RULE_CASE_SENSITIVE} bits is reduced to only
	 * {@link #RULE_CAMELCASE_MATCH} as Camel Case search is already prefix and case
	 * sensitive,</li>
	 * </ul>
	 * <br>
	 * Rejected (ie. returned match rule -1) combinations are:
	 * <ul>
	 * <li>{@link #RULE_PATTERN_MATCH} with any other match mode bit set,</li>
	 * </ul>
	 *
	 * @param stringPattern The string pattern
	 * @param matchRule     The match rule
	 * @return Optimized valid match rule or -1 if an incompatibility was detected.
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
	 * Checks if character is a valid camelCase character.
	 *
	 * @param ch character to be validated
	 * @return true if character is valid
	 */
	protected boolean isValidCamelCaseChar(char ch) {
		return true;
	}

	/**
	 * Tells whether the given <code>SearchPattern</code> equals this pattern.
	 *
	 * @param pattern pattern to be checked
	 * @return true if the given pattern equals this search pattern
	 */
	public boolean equalsPattern(SearchPattern pattern) {
		return trimWildcardCharacters(pattern.initialPattern).equals(trimWildcardCharacters(this.initialPattern));
	}

	/**
	 * Tells whether the given <code>SearchPattern</code> is a sub-pattern of this
	 * pattern.
	 * <p>
	 * <i>WARNING: This method is <b>not</b> defined in reading order, i.e.
	 * <code>a.isSubPattern(b)</code> is <code>true</code> iff <code>b</code> is a
	 * sub-pattern of <code>a</code>, and not vice-versa. </i>
	 *
	 * @param pattern pattern to be checked
	 * @return true if the given pattern is a sub pattern of this search pattern
	 */
	public boolean isSubPattern(SearchPattern pattern) {
		if (this.initialPattern.length() == 1 && this.initialPattern.charAt(0) == START_SYMBOL) {
			return false;
		}
		return trimWildcardCharacters(pattern.initialPattern).startsWith(trimWildcardCharacters(this.initialPattern));
	}

	/**
	 * Replaces sequences of '*' characters by just one '*'. It doesn't do any
	 * trimming actually.
	 *
	 * @param pattern string to be normalized
	 * @return normalized pattern
	 */
	private String trimWildcardCharacters(String pattern) {
		return Util.replaceAll(pattern, "\\*+", "\\*"); //$NON-NLS-1$ //$NON-NLS-2$ }
	}

}
