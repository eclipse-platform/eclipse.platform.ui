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
package org.eclipse.ui.dialogs;

import org.eclipse.ui.internal.misc.StringMatcher;

/**
 * 
 * <strong>EXPERIMENTAL</strong> This class or interface has been added as part
 * of a work in progress. This API may change at any given time. Please do not
 * use this API without consulting with the Platform/UI team.
 * 
 * A search pattern defines how search results are found. 
 * 
 * This class is intended to be subclassed by clients. A default behavior is provided for each of the methods above, that
 * clients can ovveride if they wish.
 * </p>
 * 
 * @since 3.3
 */
public class SearchPattern {

	// Rules for pattern matching: (exact, prefix, pattern) [ | case sensitive]
	/**
	 * Match rule: The search pattern matches exactly the search result,
	 * that is, the source of the search result equals the search pattern.
	 */
	public static final int R_EXACT_MATCH = 0;

	/**
	 * Match rule: The search pattern is a prefix of the search result.
	 */
	public static final int R_PREFIX_MATCH = 0x0001;

	/**
	 * Match rule: The search pattern contains one or more wild cards ('*' or '?'). 
	 * A '*' wild-card can replace 0 or more characters in the search result.
	 * A '?' wild-card replaces exactly 1 character in the search result.
	 */
	public static final int R_PATTERN_MATCH = 0x0002;

	/**
	 * Match rule: The search pattern contains a regular expression.
	 */
	public static final int R_REGEXP_MATCH = 0x0004;

	/**
	 * Match rule: The search pattern matches the search result only if cases are the same.
	 * Can be combined to previous rules, e.g. {@link #R_EXACT_MATCH} | {@link #R_CASE_SENSITIVE}
	 */
	public static final int R_CASE_SENSITIVE = 0x0008;

	/**
	 * Match rule: The search pattern matches search results as raw/parameterized types/methods with same erasure.
	 * This mode has no effect on other java elements search.<br>
	 * Type search example:
	 * 	<ul>
	 * 	<li>pattern: <code>List&lt;Exception&gt;</code></li>
	 * 	<li>match: <code>List&lt;Object&gt;</code></li>
	 * 	</ul>
	 * Method search example:
	 * 	<ul>
	 * 	<li>declaration: <code>&lt;T&gt;foo(T t)</code></li>
	 * 	<li>pattern: <code>&lt;Exception&gt;foo(new Exception())</code></li>
	 * 	<li>match: <code>&lt;Object&gt;foo(new Object())</code></li>
	 * 	</ul>
	 * Can be combined to all other match rules, e.g. {@link #R_CASE_SENSITIVE} | {@link #R_ERASURE_MATCH}
	 * This rule is not activated by default, so raw types or parameterized types with same erasure will not be found
	 * for pattern List&lt;String&gt;,
	 * Note that with this pattern, the match selection will be only on the erasure even for parameterized types.
	 * 
	 */
	public static final int R_ERASURE_MATCH = 0x0010;

	/**
	 * Match rule: The search pattern matches search results as raw/parameterized types/methods with equivalent type parameters.
	 * This mode has no effect on other java elements search.<br>
	 * Type search example:
	 * <ul>
	 * 	<li>pattern: <code>List&lt;Exception&gt;</code></li>
	 * 	<li>match:
	 * 		<ul>
	 * 		<li><code>List&lt;? extends Throwable&gt;</code></li>
	 * 		<li><code>List&lt;? super RuntimeException&gt;</code></li>
	 * 		<li><code>List&lt;?&gt;</code></li>
	 *			</ul>
	 * 	</li>
	 * 	</ul>
	 * Method search example:
	 * 	<ul>
	 * 	<li>declaration: <code>&lt;T&gt;foo(T t)</code></li>
	 * 	<li>pattern: <code>&lt;Exception&gt;foo(new Exception())</code></li>
	 * 	<li>match:
	 * 		<ul>
	 * 		<li><code>&lt;? extends Throwable&gt;foo(new Exception())</code></li>
	 * 		<li><code>&lt;? super RuntimeException&gt;foo(new Exception())</code></li>
	 * 		<li><code>foo(new Exception())</code></li>
	 *			</ul>
	 * 	</ul>
	 * Can be combined to all other match rules, e.g. {@link #R_CASE_SENSITIVE} | {@link #R_EQUIVALENT_MATCH}
	 * This rule is not activated by default, so raw types or equivalent parameterized types will not be found
	 * for pattern List&lt;String&gt;,
	 * This mode is overridden by {@link  #R_ERASURE_MATCH} as erasure matches obviously include equivalent ones.
	 * That means that pattern with rule set to {@link #R_EQUIVALENT_MATCH} | {@link  #R_ERASURE_MATCH}
	 * will return same results than rule only set with {@link  #R_ERASURE_MATCH}.
	 * 
	 */
	public static final int R_EQUIVALENT_MATCH = 0x0020;

	/**
	 * Match rule: The search pattern matches exactly the search result,
	 * that is, the source of the search result equals the search pattern.
	 */
	public static final int R_FULL_MATCH = 0x0040;

	/**
	 * Match rule: The search pattern contains a Camel Case expression.
	 * <br>
	 * Examples:
	 * <ul>
	 * 	<li><code>NPE</code> type string pattern will match
	 * 		<code>NullPointerException</code> and <code>NpPermissionException</code> types,</li>
	 * 	<li><code>NuPoEx</code> type string pattern will only match
	 * 		<code>NullPointerException</code> type.</li>
	 * </ul>
	 * 
	 * See {@link #camelCaseMatch(char[], char[])} for a detailed explanation
	 * of Camel Case matching.
	 * 
	 *<br>
	 * Can be combined to {@link #R_PREFIX_MATCH} match rule. For example,
	 * when prefix match rule is combined with Camel Case match rule,
	 * <code>"nPE"</code> pattern will match <code>nPException</code>.
	 *<br>
	 * Match rule {@link #R_PATTERN_MATCH} may also be combined but both rules
	 * will not be used simultaneously as they are mutually exclusive.
	 * Used match rule depends on whether string pattern contains specific pattern 
	 * characters (e.g. '*' or '?') or not. If it does, then only Pattern match rule
	 * will be used, otherwise only Camel Case match will be used.
	 * For example, with <code>"NPE"</code> string pattern, search will only use
	 * Camel Case match rule, but with <code>N*P*E*</code> string pattern, it will 
	 * use only Pattern match rule.
	 * 
	 */
	public static final int R_CAMELCASE_MATCH = 0x0080;

	private static final int MODE_MASK = R_EXACT_MATCH | R_PREFIX_MATCH | R_PATTERN_MATCH | R_REGEXP_MATCH;

	private int matchRule;
	
	private String fPattern;

	private int matchKind;

	private StringMatcher fStringMatcher;

	private static final char END_SYMBOL = '<';

	private static final char ANY_STRING = '*';

	private static final char BLANK = ' ';

	/**
	 * Creates a search pattern with the rule to apply for matching index keys. 
	 * It can be exact match, prefix match, pattern match or regexp match.
	 * Rule can also be combined with a case sensitivity flag.
	 * 
	 * @param matchRule one of {@link #R_EXACT_MATCH}, {@link #R_PREFIX_MATCH}, {@link #R_PATTERN_MATCH},
	 * 	{@link #R_REGEXP_MATCH}, {@link #R_CAMELCASE_MATCH} combined with one of following values:
	 * 	{@link #R_CASE_SENSITIVE}, {@link #R_ERASURE_MATCH} or {@link #R_EQUIVALENT_MATCH}.
	 *		e.g. {@link #R_EXACT_MATCH} | {@link #R_CASE_SENSITIVE} if an exact and case sensitive match is requested, 
	 *		{@link #R_PREFIX_MATCH} if a prefix non case sensitive match is requested or {@link #R_EXACT_MATCH} | {@link #R_ERASURE_MATCH}
	 *		if a non case sensitive and erasure match is requested.<br>
	 * 	Note that {@link #R_ERASURE_MATCH} or {@link #R_EQUIVALENT_MATCH} have no effect
	 * 	on non-generic types/methods search.<br>
	 * 	Note also that default behavior for generic types/methods search is to find exact matches.
	 */
	public SearchPattern(int matchRule) {
		this.matchRule = matchRule;
		// Set full match implicit mode
		if ((matchRule & (R_EQUIVALENT_MATCH | R_ERASURE_MATCH )) == 0) {
			this.matchRule |= R_FULL_MATCH;
		}
	}

	/**
	 * @param pattern of matching
	 */
	public SearchPattern(String pattern) {
		this(pattern, R_EXACT_MATCH | R_PREFIX_MATCH | R_PATTERN_MATCH
				| R_CAMELCASE_MATCH);
	}

	/**
	 * @param pattern of matching
	 * @param allowedModes determine pattern matching mode
	 */
	public SearchPattern(String pattern, int allowedModes) {
		initializePatternAndMatchKind(pattern);
		matchKind = matchKind & allowedModes;
		if (matchKind == R_PATTERN_MATCH) {
			fStringMatcher = new StringMatcher(fPattern, true, false);
		}
	}

	/**
	 * @return pattern
	 */
	public String getPattern() {
		return fPattern;
	}

	/**
	 * @return kind of matching
	 */
	public int getMatchKind() {
		return matchKind;
	}

	/**
	 * @param text
	 * @return true if search pattern was mached with text
	 * 			false in other way
	 */
	public boolean matches(String text) {
		switch (matchKind) {
		case R_PATTERN_MATCH:
			return fStringMatcher.match(text);
		case R_EXACT_MATCH:
			return fPattern.equalsIgnoreCase(text);
		case R_CAMELCASE_MATCH:
			if (camelCaseMatch(fPattern, text)) {
				return true;
			}
			// fall through to prefix match if camel case failed (bug
			// 137244)
		default:
			return startsWithIgnoreCase(text, fPattern);
		}
	}

	private void initializePatternAndMatchKind(String pattern) {
		int length = pattern.length();
		if (length == 0) {
			matchKind = R_EXACT_MATCH;
			fPattern = pattern;
			return;
		}
		char last = pattern.charAt(length - 1);

		if (pattern.indexOf('*') != -1 || pattern.indexOf('?') != -1) {
			matchKind = R_PATTERN_MATCH;
			switch (last) {
			case END_SYMBOL:
				fPattern = pattern.substring(0, length - 1);
				break;
			case BLANK:
				fPattern = pattern.trim();
				break;
			case ANY_STRING:
				fPattern = pattern;
				break;
			default:
				fPattern = pattern + ANY_STRING;
			}
			return;
		}

		if (last == END_SYMBOL) {
			matchKind = R_EXACT_MATCH;
			fPattern = pattern.substring(0, length - 1);
			return;
		}

		if (last == BLANK) {
			matchKind = R_EXACT_MATCH;
			fPattern = pattern.trim();
			return;
		}

		if (validateMatchRule(pattern, R_CAMELCASE_MATCH) == R_CAMELCASE_MATCH) {
			matchKind = R_CAMELCASE_MATCH;
			fPattern = pattern;
			return;
		}

		matchKind = R_PREFIX_MATCH;
		fPattern = pattern;
	}

	/**
	 * @param text
	 * @param prefix
	 * @return true if text starts with given prefix, ignoring case
	 * 			false in other way
	 */
	public static boolean startsWithIgnoreCase(String text, String prefix) {
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
	 * Answers true if the pattern matches the given name using CamelCase rules, or false otherwise. 
	 * CamelCase matching does NOT accept explicit wild-cards '*' and '?' and is inherently case sensitive.
	 * <br>
	 * CamelCase denotes the convention of writing compound names without spaces, and capitalizing every term.
	 * This function recognizes both upper and lower CamelCase, depending whether the leading character is capitalized
	 * or not. The leading part of an upper CamelCase pattern is assumed to contain a sequence of capitals which are appearing
	 * in the matching name; e.g. 'NPE' will match 'NullPointerException', but not 'NewPerfData'. A lower CamelCase pattern
	 * uses a lowercase first character. In Java, type names follow the upper CamelCase convention, whereas method or field
	 * names follow the lower CamelCase convention.
	 * <br>
	 * The pattern may contain lowercase characters, which will be match in a case sensitive way. These characters must
	 * appear in sequence in the name. For instance, 'NPExcep' will match 'NullPointerException', but not 'NullPointerExCEPTION'
	 * or 'NuPoEx' will match 'NullPointerException', but not 'NoPointerException'.
	 * <br><br>
	 * Examples:
	 * <ol>
	 * <li><pre>
	 *    pattern = "NPE"
	 *    name = NullPointerException / NoPermissionException
	 *    result => true
	 * </pre>
	 * </li>
	 * <li><pre>
	 *    pattern = "NuPoEx"
	 *    name = NullPointerException
	 *    result => true
	 * </pre>
	 * </li>
	 * <li><pre>
	 *    pattern = "npe"
	 *    name = NullPointerException
	 *    result => false
	 * </pre>
	 * </li>
	 * </ol>
	 * 
	 * @param pattern the given pattern
	 * @param name the given name
	 * @return true if the pattern matches the given name, false otherwise
	 * 
	 */
	public static final boolean camelCaseMatch(String pattern, String name) {
		if (pattern == null)
			return true; // null pattern is equivalent to '*'
		if (name == null)
			return false; // null name cannot match

		return camelCaseMatch(pattern, 0, pattern.length(), name, 0, name.length());
	}

	/**
	 * Answers true if a sub-pattern matches the subpart of the given name using CamelCase rules, or false otherwise.  
	 * CamelCase matching does NOT accept explicit wild-cards '*' and '?' and is inherently case sensitive. 
	 * Can match only subset of name/pattern, considering end positions as non-inclusive.
	 * The subpattern is defined by the patternStart and patternEnd positions.
	 * <br>
	 * CamelCase denotes the convention of writing compound names without spaces, and capitalizing every term.
	 * This function recognizes both upper and lower CamelCase, depending whether the leading character is capitalized
	 * or not. The leading part of an upper CamelCase pattern is assumed to contain a sequence of capitals which are appearing
	 * in the matching name; e.g. 'NPE' will match 'NullPointerException', but not 'NewPerfData'. A lower CamelCase pattern
	 * uses a lowercase first character. In Java, type names follow the upper CamelCase convention, whereas method or field
	 * names follow the lower CamelCase convention.
	 * <br>
	 * The pattern may contain lowercase characters, which will be match in a case sensitive way. These characters must
	 * appear in sequence in the name. For instance, 'NPExcep' will match 'NullPointerException', but not 'NullPointerExCEPTION'
	 * or 'NuPoEx' will match 'NullPointerException', but not 'NoPointerException'.
	 * <br><br>
	 * Examples:
	 * <ol>
	 * <li><pre>
	 *    pattern = "NPE"
	 *    patternStart = 0
	 *    patternEnd = 3
	 *    name = NullPointerException
	 *    nameStart = 0
	 *    nameEnd = 20
	 *    result => true
	 * </pre>
	 * </li>
	 * <li><pre>
	 *    pattern = "NPE"
	 *    patternStart = 0
	 *    patternEnd = 3
	 *    name = NoPermissionException
	 *    nameStart = 0
	 *    nameEnd = 21
	 *    result => true
	 * </pre>
	 * </li>
	 * <li><pre>
	 *    pattern = "NuPoEx"
	 *    patternStart = 0
	 *    patternEnd = 6
	 *    name = NullPointerException
	 *    nameStart = 0
	 *    nameEnd = 20
	 *    result => true
	 * </pre>
	 * </li>
	 * <li><pre>
	 *    pattern = "NuPoEx"
	 *    patternStart = 0
	 *    patternEnd = 6
	 *    name = NoPermissionException
	 *    nameStart = 0
	 *    nameEnd = 21
	 *    result => false
	 * </pre>
	 * </li>
	 * <li><pre>
	 *    pattern = "npe"
	 *    patternStart = 0
	 *    patternEnd = 3
	 *    name = NullPointerException
	 *    nameStart = 0
	 *    nameEnd = 20
	 *    result => false
	 * </pre>
	 * </li>
	 * </ol>
	 * 
	 * @param pattern the given pattern
	 * @param patternStart the start index of the pattern, inclusive
	 * @param patternEnd the end index of the pattern, exclusive
	 * @param name the given name
	 * @param nameStart the start index of the name, inclusive
	 * @param nameEnd the end index of the name, exclusive
	 * @return true if a sub-pattern matches the subpart of the given name, false otherwise
	 */
	public static final boolean camelCaseMatch(String pattern, int patternStart, int patternEnd, String name, int nameStart, int nameEnd) {
		if (name == null)
			return false; // null name cannot match
		if (pattern == null)
			return true; // null pattern is equivalent to '*'
		if (patternEnd < 0) 	patternEnd = pattern.length();
		if (nameEnd < 0) nameEnd = name.length();

		if (patternEnd <= patternStart) return nameEnd <= nameStart;
		if (nameEnd <= nameStart) return false;
		// check first pattern char
		if (name.charAt(nameStart) != pattern.charAt(patternStart)) {
			// first char must strictly match (upper/lower)
			return false;
		}

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

			if (iName == nameEnd){
				// We have exhausted name (and not pattern), so it's not a match 
				return false;
			}

			// For as long as we're exactly matching, bring it on (even if it's a lower case character)
			if ((patternChar = pattern.charAt(iPattern)) == name.charAt(iName)) {
				continue;
			}

			// If characters are not equals, then it's not a match if patternChar is lowercase
			if (!isPatternCharAllowed(patternChar))
					return false;
			
			// patternChar is uppercase, so let's find the next uppercase in name
			while (true) {
				if (iName == nameEnd){
		            //	We have exhausted name (and not pattern), so it's not a match
					return false;
				}

				nameChar = name.charAt(iName);

				if (!isNameCharAllowed(nameChar)) {
						// nameChar is lowercase    
						iName++;
					// nameChar is uppercase...
					} else  if (patternChar != nameChar) {
						//.. and it does not match patternChar, so it's not a match
						return false;
					} else {
						//.. and it matched patternChar. Back to the big loop
						break;
					}
			}
			// At this point, either name has been exhausted, or it is at an uppercase letter.
			// Since pattern is also at an uppercase letter
		}
	}	

	/**
	 * Answers true if the pattern matches the given name using CamelCase rules, or false otherwise. 
	 * char[] CamelCase matching does NOT accept explicit wild-cards '*' and '?' and is inherently case sensitive.
	 * <br>
	 * CamelCase denotes the convention of writing compound names without spaces, and capitalizing every term.
	 * This function recognizes both upper and lower CamelCase, depending whether the leading character is capitalized
	 * or not. The leading part of an upper CamelCase pattern is assumed to contain a sequence of capitals which are appearing
	 * in the matching name; e.g. 'NPE' will match 'NullPointerException', but not 'NewPerfData'. A lower CamelCase pattern
	 * uses a lowercase first character. In Java, type names follow the upper CamelCase convention, whereas method or field
	 * names follow the lower CamelCase convention.
	 * <br>
	 * The pattern may contain lowercase characters, which will be match in a case sensitive way. These characters must
	 * appear in sequence in the name. For instance, 'NPExcep' will match 'NullPointerException', but not 'NullPointerExCEPTION'
	 * or 'NuPoEx' will match 'NullPointerException', but not 'NoPointerException'.
	 * <br><br>
	 * Examples:
	 * <ol>
	 * <li><pre>
	 *    pattern = { 'N', 'P', 'E' }
	 *    name = { 'N', 'u','l', 'l', 'P', 'o', 'i', 'n', 't', 'e', 'r', 'E', 'x', 'c', 'e', 'p', 't', 'i', 'o', 'n' }
	 *    result => true
	 * </pre>
	 * </li>
	 * <li><pre>
	 *    pattern = { 'N', 'P', 'E' }
	 *    name = { 'N', 'o', 'P', 'e', 'r', 'm', 'i', 's', 's', 'i', 'o', 'n', 'E', 'x', 'c', 'e', 'p', 't', 'i', 'o', 'n' }
	 *    result => true
	 * </pre>
	 * </li>
	 * <li><pre>
	 *    pattern = { 'N', 'u', 'P', 'o', 'E', 'x' }
	 *    name = { 'N', 'u','l', 'l', 'P', 'o', 'i', 'n', 't', 'e', 'r', 'E', 'x', 'c', 'e', 'p', 't', 'i', 'o', 'n' }
	 *    result => true
	 * </pre>
	 * </li>
	 * <li><pre>
	 *    pattern = { 'N', 'u', 'P', 'o', 'E', 'x' }
	 *    name = { 'N', 'o', 'P', 'e', 'r', 'm', 'i', 's', 's', 'i', 'o', 'n', 'E', 'x', 'c', 'e', 'p', 't', 'i', 'o', 'n' }
	 *    result => false
	 * </pre>
	 * </li>
	 * <li><pre>
	 *    pattern = { 'n', p', 'e' }
	 *    name = { 'N', 'u','l', 'l', 'P', 'o', 'i', 'n', 't', 'e', 'r', 'E', 'x', 'c', 'e', 'p', 't', 'i', 'o', 'n' }
	 *    result => false
	 * </pre>
	 * </li>
	 * </ol>
	 * 
	 * @param pattern the given pattern
	 * @param name the given name
	 * @return true if the pattern matches the given name, false otherwise
	 */
	public static final boolean camelCaseMatch(char[] pattern, char[] name) {
		if (pattern == null)
			return true; // null pattern is equivalent to '*'
		if (name == null)
			return false; // null name cannot match

		return camelCaseMatch(pattern, 0, pattern.length, name, 0, name.length);
	}

	/**
	 * Answers true if a sub-pattern matches the subpart of the given name using CamelCase rules, or false otherwise.  
	 * char[] CamelCase matching does NOT accept explicit wild-cards '*' and '?' and is inherently case sensitive. 
	 * Can match only subset of name/pattern, considering end positions as non-inclusive.
	 * The subpattern is defined by the patternStart and patternEnd positions.
	 * <br>
	 * CamelCase denotes the convention of writing compound names without spaces, and capitalizing every term.
	 * This function recognizes both upper and lower CamelCase, depending whether the leading character is capitalized
	 * or not. The leading part of an upper CamelCase pattern is assumed to contain a sequence of capitals which are appearing
	 * in the matching name; e.g. 'NPE' will match 'NullPointerException', but not 'NewPerfData'. A lower CamelCase pattern
	 * uses a lowercase first character. In Java, type names follow the upper CamelCase convention, whereas method or field
	 * names follow the lower CamelCase convention.
	 * <br>
	 * The pattern may contain lowercase characters, which will be match in a case sensitive way. These characters must
	 * appear in sequence in the name. For instance, 'NPExcep' will match 'NullPointerException', but not 'NullPointerExCEPTION'
	 * or 'NuPoEx' will match 'NullPointerException', but not 'NoPointerException'.
	 * <br><br>
	 * Examples:
	 * <ol>
	 * <li><pre>
	 *    pattern = { 'N', 'P', 'E' }
	 *    patternStart = 0
	 *    patternEnd = 3
	 *    name = { 'N', 'u','l', 'l', 'P', 'o', 'i', 'n', 't', 'e', 'r', 'E', 'x', 'c', 'e', 'p', 't', 'i', 'o', 'n' }
	 *    nameStart = 0
	 *    nameEnd = 20
	 *    result => true
	 * </pre>
	 * </li>
	 * <li><pre>
	 *    pattern = { 'N', 'P', 'E' }
	 *    patternStart = 0
	 *    patternEnd = 3
	 *    name = { 'N', 'o', 'P', 'e', 'r', 'm', 'i', 's', 's', 'i', 'o', 'n', 'E', 'x', 'c', 'e', 'p', 't', 'i', 'o', 'n' }
	 *    nameStart = 0
	 *    nameEnd = 21
	 *    result => true
	 * </pre>
	 * </li>
	 * <li><pre>
	 *    pattern = { 'N', 'u', 'P', 'o', 'E', 'x' }
	 *    patternStart = 0
	 *    patternEnd = 6
	 *    name = { 'N', 'u','l', 'l', 'P', 'o', 'i', 'n', 't', 'e', 'r', 'E', 'x', 'c', 'e', 'p', 't', 'i', 'o', 'n' }
	 *    nameStart = 0
	 *    nameEnd = 20
	 *    result => true
	 * </pre>
	 * </li>
	 * <li><pre>
	 *    pattern = { 'N', 'u', 'P', 'o', 'E', 'x' }
	 *    patternStart = 0
	 *    patternEnd = 6
	 *    name = { 'N', 'o', 'P', 'e', 'r', 'm', 'i', 's', 's', 'i', 'o', 'n', 'E', 'x', 'c', 'e', 'p', 't', 'i', 'o', 'n' }
	 *    nameStart = 0
	 *    nameEnd = 21
	 *    result => false
	 * </pre>
	 * </li>
	 * <li><pre>
	 *    pattern = { 'n', p', 'e' }
	 *    patternStart = 0
	 *    patternEnd = 3
	 *    name = { 'N', 'u','l', 'l', 'P', 'o', 'i', 'n', 't', 'e', 'r', 'E', 'x', 'c', 'e', 'p', 't', 'i', 'o', 'n' }
	 *    nameStart = 0
	 *    nameEnd = 20
	 *    result => false
	 * </pre>
	 * </li>
	 * </ol>
	 * 
	 * @param pattern the given pattern
	 * @param patternStart the start index of the pattern, inclusive
	 * @param patternEnd the end index of the pattern, exclusive
	 * @param name the given name
	 * @param nameStart the start index of the name, inclusive
	 * @param nameEnd the end index of the name, exclusive
	 * @return true if a sub-pattern matches the subpart of the given name, false otherwise
	 */
	public static final boolean camelCaseMatch(char[] pattern, int patternStart, int patternEnd, char[] name, int nameStart, int nameEnd) {
		if (name == null)
			return false; // null name cannot match
		if (pattern == null)
			return true; // null pattern is equivalent to '*'
		if (patternEnd < 0) 	patternEnd = pattern.length;
		if (nameEnd < 0) nameEnd = name.length;

		if (patternEnd <= patternStart) return nameEnd <= nameStart;
		if (nameEnd <= nameStart) return false;
		// check first pattern char
		if (name[nameStart] != pattern[patternStart]) {
			// first char must strictly match (upper/lower)
			return false;
		}

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

			if (iName == nameEnd){
				// We have exhausted name (and not pattern), so it's not a match 
				return false;
			}

			// For as long as we're exactly matching, bring it on (even if it's a lower case character)
			if ((patternChar = pattern[iPattern]) == name[iName]) {
				continue;
			}

			// If characters are not equals, then it's not a match if patternChar is lowercase
			if (!isPatternCharAllowed(patternChar))
				return false;

			// patternChar is uppercase, so let's find the next uppercase in name
			while (true) {
				if (iName == nameEnd){
		            //	We have exhausted name (and not pattern), so it's not a match
					return false;
				}

				nameChar = name[iName];
				if (!isNameCharAllowed(nameChar)) {
					// nameChar is lowercase    
						iName++;
					// nameChar is uppercase...
					} else  if (patternChar != nameChar) {
						//.. and it does not match patternChar, so it's not a match
						return false;
					} else {
						//.. and it matched patternChar. Back to the big loop
						break;
					}
			}
			// At this point, either name has been exhausted, or it is at an uppercase letter.
			// Since pattern is also at an uppercase letter
		}
	}	

	/**
	 * It's a method for checking character of pattern.
	 * It's check character allowed for specified set.
	 * EveryOne can override
	 * @param patternChar
	 * @return true if patternChar is in set of allowed characters for pattern
	 */
	protected static boolean isPatternCharAllowed(char patternChar){
		return Character.isUpperCase(patternChar);
	}

	/**
	 * It's a method for checking character of element's name.
	 * It's check character allowed for specified set. 
	 * @param nameChar - name of searched lement
	 * @return if nameChar is in set of allowed characters for name of element
	 */
	protected static boolean isNameCharAllowed(char nameChar){
		return Character.isUpperCase(nameChar);
	}

	/**
	 * Returns the rule to apply for matching index keys. Can be exact match, prefix match, pattern match or regexp match.
	 * Rule can also be combined with a case sensitivity flag.
	 * 
	 * @return one of R_EXACT_MATCH, R_PREFIX_MATCH, R_PATTERN_MATCH, R_REGEXP_MATCH combined with R_CASE_SENSITIVE,
	 *   e.g. R_EXACT_MATCH | R_CASE_SENSITIVE if an exact and case sensitive match is requested, 
	 *   or R_PREFIX_MATCH if a prefix non case sensitive match is requested.
	 * [TODO (frederic) I hope R_ERASURE_MATCH doesn't need to be on this list. Because it would be a breaking API change.]
	 */	
	public final int getMatchRule() {
		return this.matchRule;
	}

	/**
	 * Returns whether the given name matches the given pattern.
	 * <p>
	 * This method should be re-implemented in subclasses that need to define how
	 * a name matches a pattern.
	 * </p>
	 * 
	 * @param pattern the given pattern, or <code>null</code> to represent "*"
	 * @param name the given name
	 * @return whether the given name matches the given pattern
	 */
	public boolean matchesName(char[] pattern, char[] name) {
		if (pattern == null) return true; // null is as if it was "*"
		if (name != null) {
			boolean isCaseSensitive = (this.matchRule & R_CASE_SENSITIVE) != 0;
			boolean isCamelCase = (this.matchRule & R_CAMELCASE_MATCH) != 0;
			int matchMode = this.matchRule & MODE_MASK;
			boolean emptyPattern = pattern.length == 0;
			if (matchMode == R_PREFIX_MATCH && emptyPattern) return true;
			boolean sameLength = pattern.length == name.length;
			boolean canBePrefix = name.length >= pattern.length;
			boolean matchFirstChar = !isCaseSensitive || emptyPattern || (name.length > 0 &&  pattern[0] == name[0]);
			if (isCamelCase && matchFirstChar && SearchPattern.camelCaseMatch(pattern, name)) {
				return true;
			}
			switch (matchMode) {
				case R_EXACT_MATCH :
				case R_FULL_MATCH :
					if (!isCamelCase) {
						if (sameLength && matchFirstChar) {
							return CharOperation.equals(pattern, name, isCaseSensitive);
						}
						break;
					}
					// fall through next case to match as prefix if camel case failed
				case R_PREFIX_MATCH :
					if (canBePrefix && matchFirstChar) {
						return CharOperation.prefixEquals(pattern, name, isCaseSensitive);
					}
					break;

				case R_PATTERN_MATCH :
					if (!isCaseSensitive)
						pattern = CharOperation.toLowerCase(pattern);
					return CharOperation.match(pattern, name, isCaseSensitive);

				case R_REGEXP_MATCH :
					// TODO (frederic) implement regular expression match
					return true;
			}
		}
		return false;
	}

	/**
	 * Validate compatibility between given string pattern and match rule.
	 *<br>
	 * Optimized (ie. returned match rule is modified) combinations are:
	 * <ul>
	 * 	<li>{@link #R_PATTERN_MATCH} without any '*' or '?' in string pattern:
	 * 		pattern match bit is unset,
	 * 	</li>
	 * 	<li>{@link #R_PATTERN_MATCH} and {@link #R_PREFIX_MATCH}  bits simultaneously set:
	 * 		prefix match bit is unset,
	 * 	</li>
	 * 	<li>{@link #R_PATTERN_MATCH} and {@link #R_CAMELCASE_MATCH}  bits simultaneously set:
	 * 		camel case match bit is unset,
	 * 	</li>
	 * 	<li>{@link #R_CAMELCASE_MATCH} with invalid combination of uppercase and lowercase characters:
	 * 		camel case match bit is unset and replaced with prefix match pattern,
	 * 	</li>
	 * 	<li>{@link #R_CAMELCASE_MATCH} combined with {@link #R_PREFIX_MATCH} and {@link #R_CASE_SENSITIVE}
	 * 		bits is reduced to only {@link #R_CAMELCASE_MATCH} as Camel Case search is already prefix and case sensitive,
	 * 	</li>
	 * </ul>
	 *<br>
	 * Rejected (ie. returned match rule -1) combinations are:
	 * <ul>
	 * 	<li>{@link #R_REGEXP_MATCH} with any other match mode bit set,
	 * 	</li>
	 * </ul>
	 *
	 * @param stringPattern The string pattern
	 * @param matchRule The match rule
	 * @return Optimized valid match rule or -1 if an incompatibility was detected.
	 */
	public static int validateMatchRule(String stringPattern, int matchRule) {

		// Verify Regexp match rule
		if ((matchRule & R_REGEXP_MATCH) != 0) {
			if ((matchRule & R_PATTERN_MATCH) != 0 || (matchRule & R_PREFIX_MATCH) != 0 || (matchRule & R_CAMELCASE_MATCH) != 0) {
				return -1;
			}
		}

		// Verify Pattern match rule
		int starIndex = stringPattern.indexOf('*');
		int questionIndex = stringPattern.indexOf('?');
		if (starIndex < 0 && questionIndex < 0) {
			// reset pattern match bit if any
			matchRule &= ~R_PATTERN_MATCH;
		} else {
			// force Pattern rule
			matchRule |= R_PATTERN_MATCH;
		}
		if ((matchRule & R_PATTERN_MATCH) != 0) {
			// remove Camel Case and Prefix match bits if any
			matchRule &= ~R_CAMELCASE_MATCH;
			matchRule &= ~R_PREFIX_MATCH;
		}

		// Verify Camel Case match rule
		if ((matchRule & R_CAMELCASE_MATCH) != 0) {
			// Verify sting pattern validity
			int length = stringPattern.length();
			boolean validCamelCase = true;
			boolean uppercase = false;
			for (int i=0; i<length && validCamelCase; i++) {
				char ch = stringPattern.charAt(i);
				validCamelCase = isValidCamelCaseChar(ch);
				// at least one uppercase character is need in CamelCase pattern
				// (see bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=136313)
				if (!uppercase) uppercase = Character.isUpperCase(ch);
			}
			validCamelCase = validCamelCase && uppercase;
			// Verify bits compatibility
			if (validCamelCase) {
				if ((matchRule & R_PREFIX_MATCH) != 0) {
					if ((matchRule & R_CASE_SENSITIVE) != 0) {
						// This is equivalent to Camel Case match rule
						matchRule &= ~R_PREFIX_MATCH;
						matchRule &= ~R_CASE_SENSITIVE;
					}
				}
			} else {
				matchRule &= ~R_CAMELCASE_MATCH;
				if ((matchRule & R_PREFIX_MATCH) == 0) {
					matchRule |= R_PREFIX_MATCH;
					matchRule |= R_CASE_SENSITIVE;
				}
			}
		}
		return matchRule;
	}

	protected static boolean isValidCamelCaseChar(char ch) {
		return true;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "SearchPattern"; //$NON-NLS-1$
	}
}

