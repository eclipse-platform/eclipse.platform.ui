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
 *     Petr Bodnar - common ancestor for SearchPattern test classes
 *******************************************************************************/

package org.eclipse.ui.tests.dialogs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.ui.dialogs.SearchPattern;
import org.junit.Test;

/**
 * Common ancestor for tests of the SearchPattern's match functionality.
 */
public abstract class AbstractSearchPatternAuto {

	/**
	 * Items to be filtered.
	 */
	protected static List<String> resources = new ArrayList<>();

	static {
		generateResourcesTestCases('A', 'C', 8, "");
		generateResourcesTestCases('A', 'C', 4, "");
		// add some explicit testing samples:
		resources.addAll(
				Arrays.asList("BC", "BCD", "AiBiCiDi", "BiCiDi", "BijCiDi", "BICI", "abc", "bcd", "abcd", "ab cd"));
		// in practice, there should be no resources with such names, but for
		// completeness:
		resources.addAll(Arrays.asList(" ", "AB ", ">", "<"));
		// ... while this might exist:
		resources.add(" BC");
	}

	/**
	 * Generates strings data for match test cases.
	 *
	 * @param startChar
	 * @param endChar
	 * @param length
	 * @param resource
	 */
	protected static void generateResourcesTestCases(char startChar, char endChar, int length, String resource) {
		for (char ch = startChar; ch <= endChar; ch++) {
			String res = resource + ch;
			if (length == res.length()) {
				resources.add(res);
			} else if ((res.trim().length() % 2) == 0) {
				generateResourcesTestCases(Character.toUpperCase((char) (startChar + 1)),
						Character.toUpperCase((char) (endChar + 1)), length, res);
			} else {
				generateResourcesTestCases(Character.toLowerCase((char) (startChar + 1)),
						Character.toLowerCase((char) (endChar + 1)), length, res);
			}
		}
	}

	/**
	 * Empty pattern matches everything. In practice, no search is done in this case
	 * though.
	 */
	@Test
	public void testBlankMatch() {
		Pattern pattern = Pattern.compile(".*", Pattern.CASE_INSENSITIVE);
		assertMatches("", SearchPattern.RULE_BLANK_MATCH, pattern);
	}

	/**
	 * Tests that a single "&lt;" is taken as a plain character. For simplicity, we
	 * emulate just a perfect match here.
	 */
	@Test
	public void testJustEndCharPattern() {
		Pattern pattern = Pattern.compile("<", Pattern.CASE_INSENSITIVE);
		assertMatches("<", SearchPattern.RULE_PREFIX_MATCH, pattern);
	}

	/**
	 * Tests that a single "&gt;" is taken as a plain character. For simplicity, we
	 * emulate just a perfect match here.
	 */
	@Test
	public void testJustStartCharPattern() {
		Pattern pattern = Pattern.compile(">", Pattern.CASE_INSENSITIVE);
		assertMatches(">", SearchPattern.RULE_PREFIX_MATCH, pattern);
	}

	@Test
	public void testIsSubPattern_BasicCases() {
		SearchPattern prevPattern = createSearchPattern("a");
		SearchPattern nextPattern = createSearchPattern("ab");
		assertTrue("[ab] has to be a sub-pattern of [a]", prevPattern.isSubPattern(nextPattern));
		assertFalse("[a] must not be a sub-pattern of [ab]", nextPattern.isSubPattern(prevPattern));
	}

	/**
	 * Tests this scenario: user types in ">" and then "a" - the resulting ">a"
	 * pattern must NOT be evaluated as a sub-pattern of ">", because searching just
	 * by ">" should normally return an empty set - and filtering just that empty
	 * set would find nothing.
	 */
	@Test
	public void testIsSubPattern_WhenPrevIsJustStartChar_ReturnFalse() {
		SearchPattern prevPattern = createSearchPattern(">");
		SearchPattern nextPattern = createSearchPattern(">a");
		assertFalse(prevPattern.isSubPattern(nextPattern));
	}

	protected abstract SearchPattern createSearchPattern();

	protected SearchPattern createSearchPattern(String inputPattern) {
		SearchPattern pattern = createSearchPattern();
		pattern.setPattern(inputPattern);
		return pattern;
	}

	protected void assertMatches(String patternText, int expectedMatchRule, Pattern... matchingPatterns) {
		SearchPattern patternMatcher = createSearchPattern();
		patternMatcher.setPattern(patternText);
		assertEquals("Inferred match rule must match", expectedMatchRule, patternMatcher.getMatchRule());
		boolean someMatch = false;
		for (String res : resources) {
			boolean anyMatches = anyMatches(res, matchingPatterns);
			boolean patternMatches = patternMatcher.matches(res);
			if (patternMatches) {
				assertTrue("Pattern '" + patternText + "' matches '" + res + "', but it shouldn't.", anyMatches);
			} else {
				assertFalse("Pattern '" + patternText + "' doesn't match '" + res + "', but it should.", anyMatches);
			}
			if (anyMatches) {
				someMatch = true;
			}
		}
		if (!someMatch) {
			fail("Invalid test setup: no item matches any of the supplied matchingPatterns");
		}
	}

	protected static boolean anyMatches(String res, Pattern... patterns) {
		for (Pattern pattern : patterns) {
			if (pattern.matcher(res).matches()) {
				return true;
			}
		}
		return false;
	}

}
