/*******************************************************************************
 * Copyright (c) 2006, 2017 IBM Corporation and others.
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

package org.eclipse.ui.tests.dialogs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.ui.dialogs.SearchPattern;
import org.junit.Test;

/**
 * Tests of the SearchPattern's match functionality.
 *
 * @since 3.3
 */
public class SearchPatternAuto {

	/**
	 * Items to be filtered.
	 */
	private static List<String> resources = new ArrayList<>();

	static {
		generateResourcesTestCases('A', 'C', 8, "");
		generateResourcesTestCases('A', 'C', 4, "");
	}

	/**
	 * Generates strings data for match test cases.
	 *
	 * @param startChar
	 * @param endChar
	 * @param length
	 * @param resource
	 */
	private static void generateResourcesTestCases(char startChar, char endChar, int length, String resource) {
		for (char ch = startChar; ch <= endChar; ch++) {
			String res = resource + ch;
			if (length == res.length()) {
				resources.add(res);
			} else if ((res.trim().length() % 2) == 0) {
				generateResourcesTestCases(Character.toUpperCase((char)(startChar + 1)), Character.toUpperCase((char)(endChar + 1)), length, res);
			} else {
				generateResourcesTestCases(Character.toLowerCase((char)(startChar + 1)), Character.toLowerCase((char)(endChar + 1)), length, res);
			}
		}
	}

	/**
	 * Tests exact match functionality.
	 * If camelCase rule is enabled, Pattern should start with lowerCase character.
	 * Result for "abcd " pattern should be similar to regexp pattern "abcd" with case insensitive.
	 */
	@Test
	public void testExactMatch() {
		Pattern pattern = Pattern.compile("abcd", Pattern.CASE_INSENSITIVE);
		assertMatches("abcd ", SearchPattern.RULE_EXACT_MATCH, pattern);
		// alternative ending character:
		assertMatches("abcd<", SearchPattern.RULE_EXACT_MATCH, pattern);
	}


	/**
	 * Tests prefix match functionality.
	 * If camelCase rule is enabled, Pattern should start with lowerCase character.
	 * Result for "ab" pattern should be similar to regexp pattern "ab.*" with case insensitive.
	 */
	@Test
	public void testPrefixMatch() {
		Pattern pattern = Pattern.compile("ab.*", Pattern.CASE_INSENSITIVE);
		assertMatches("ab", SearchPattern.RULE_PREFIX_MATCH, pattern);
	}

	/**
	 * Tests pattern match functionality. It's similar to regexp patterns.
	 * Result for "*cDe" pattern should be similar to regexp pattern ".*cde.*" with case insensitive.
	 */
	@Test
	public void testPatternMatch1() {
		Pattern pattern = Pattern.compile(".*cde.*", Pattern.CASE_INSENSITIVE);
		assertMatches("*cDe", SearchPattern.RULE_PATTERN_MATCH, pattern);
		// 1 or more consecutive '*' has to be the same as 1 '*'
		assertMatches("**cDe", SearchPattern.RULE_PATTERN_MATCH, pattern);
	}

	/**
	 * Tests pattern match functionality. It's similar to regexp patterns.
	 * Result for "*c*e*i" pattern should be similar to regexp pattern ".*c.*e.*i.*" with case insensitive.
	 */
	@Test
	public void testPatternMatch2() {
		Pattern pattern = Pattern.compile(".*c.*e.*i.*", Pattern.CASE_INSENSITIVE);
		assertMatches("*c*e*i", SearchPattern.RULE_PATTERN_MATCH, pattern);
	}

	/**
	 * Tests camelCase match functionality.
	 * Every string starting with an upperCase character should be recognized as camelCase pattern match rule.
	 * Result for "CD" SearchPattern should be similar to regexp pattern "C[^A-Z]*D.*".
	 * If pattern contains only upperCase characters, result also contains all prefix match elements.
	 */
	@Test
	public void testCamelCaseMatch() {
		Pattern pattern = Pattern.compile("C[^A-Z]*D.*");
		Pattern pattern2 = Pattern.compile("CD.*", Pattern.CASE_INSENSITIVE);
		assertMatches("CD", SearchPattern.RULE_CAMELCASE_MATCH, pattern, pattern2);
	}

	/**
	 * Tests camelCase match functionality.
	 * Every string starting with an upperCase character should be recognized as camelCase pattern match rule.
	 * Result for "AbCd " SearchPattern should be similar to regexp pattern "Ab[^A-Z]*Cd[^A-Z]*".
	 */
	@Test
	public void testCamelCaseForcedEndMatch() {
		Pattern pattern = Pattern.compile("Ab[^A-Z]*Cd[^A-Z]*");
		assertMatches("AbCd ", SearchPattern.RULE_CAMELCASE_MATCH, pattern);
		// alternative ending character:
		assertMatches("AbCd<", SearchPattern.RULE_CAMELCASE_MATCH, pattern);
	}

	/**
	 * Tests blank match functionality.
	 * Blank string should be recognized as a blank pattern match rule.
	 * It should match with all resources.
	 * Result for SearchPattern should be similar to regexp pattern ".*"
	 */
	@Test
	public void testBlankMatch() {
		Pattern pattern = Pattern.compile(".*", Pattern.CASE_INSENSITIVE);
		assertMatches("", SearchPattern.RULE_BLANK_MATCH, pattern);
	}

	private void assertMatches(String patternText, int expectedMatchRule, Pattern... matchingPatterns) {
		SearchPattern patternMatcher = new SearchPattern();
		patternMatcher.setPattern(patternText);
		assertEquals(expectedMatchRule, patternMatcher.getMatchRule());
		for (String res : resources) {
			boolean anyMatches = anyMatches(res, matchingPatterns);
			boolean patternMatches = patternMatcher.matches(res);
			if (patternMatches) {
				assertTrue("Pattern '" + patternText + "' matches '" + res + "', but it shouldn't.", anyMatches);
			} else {
				assertFalse("Pattern '" + patternText + "' doesn't match '" + res + "', but it should.",
						anyMatches);
			}
		}
	}

	private static boolean anyMatches(String res, Pattern... patterns) {
		for (Pattern pattern : patterns) {
			if (pattern.matcher(res).matches()) {
				return true;
			}
		}
		return false;
	}

}
