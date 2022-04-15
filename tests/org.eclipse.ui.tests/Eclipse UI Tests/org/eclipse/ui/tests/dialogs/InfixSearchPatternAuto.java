/*******************************************************************************
 * Copyright (c) 2022 Petr Bodnar and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Petr Bodnar - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.tests.dialogs;

import java.util.regex.Pattern;

import org.eclipse.ui.dialogs.SearchPattern;
import org.junit.Test;

/**
 * Tests of the SearchPattern's match functionality. Similar to
 * {@link SearchPatternAuto}, but this one tests the SearchPattern with
 * <var>autoInfixSearch</var> activated.
 */
public class InfixSearchPatternAuto extends AbstractSearchPatternAuto {

	/**
	 * Tests that a single space is taken as a plain character.
	 */
	@Test
	public void testJustSpaceCharPattern() {
		Pattern pattern = Pattern.compile(".* .*", Pattern.CASE_INSENSITIVE);
		assertMatches(" ", SearchPattern.RULE_PREFIX_MATCH, pattern);
	}

	/**
	 * Similar to {@link #testJustSpaceCharPattern()}, but enforcing just suffix match
	 * this time.
	 */
	@Test
	public void testJustSpaceAndEndCharPattern() {
		Pattern pattern = Pattern.compile(".* ", Pattern.CASE_INSENSITIVE);
		assertMatches("  ", SearchPattern.RULE_PREFIX_MATCH, pattern);
		// alternative end character:
		assertMatches(" <", SearchPattern.RULE_PREFIX_MATCH, pattern);
	}

	/**
	 * Tests exact match functionality. If camelCase rule is enabled, Pattern should
	 * start with lowerCase character.
	 */
	@Test
	public void testExactMatch() {
		Pattern pattern = Pattern.compile("bcd", Pattern.CASE_INSENSITIVE);
		assertMatches(">bcd ", SearchPattern.RULE_EXACT_MATCH, pattern);
		// alternative end character:
		assertMatches(">bcd<", SearchPattern.RULE_EXACT_MATCH, pattern);
	}

	/**
	 * Tests infix match functionality. If camelCase rule is enabled, Pattern should
	 * start with lowerCase character.
	 */
	@Test
	public void testInfixMatch() {
		Pattern pattern = Pattern.compile(".*bcd.*", Pattern.CASE_INSENSITIVE);
		assertMatches("bcd", SearchPattern.RULE_PREFIX_MATCH, pattern);
	}

	/**
	 * Tests prefix match functionality. If camelCase rule is enabled, Pattern
	 * should start with lowerCase character.
	 */
	@Test
	public void testPrefixMatch() {
		Pattern pattern = Pattern.compile("bcd.*", Pattern.CASE_INSENSITIVE);
		assertMatches(">bcd", SearchPattern.RULE_PREFIX_MATCH, pattern);
	}

	/**
	 * Tests suffix match functionality. If camelCase rule is enabled, Pattern
	 * should start with lowerCase character.
	 */
	@Test
	public void testSuffixMatch() {
		Pattern pattern = Pattern.compile(".*bcd", Pattern.CASE_INSENSITIVE);
		assertMatches("bcd ", SearchPattern.RULE_PREFIX_MATCH, pattern);
		// alternative end character:
		assertMatches("bcd<", SearchPattern.RULE_PREFIX_MATCH, pattern);
	}

	/**
	 * Tests pattern match functionality. It's similar to regexp patterns.
	 */
	@Test
	public void testPatternMatch1() {
		Pattern pattern = Pattern.compile(".*cde.*", Pattern.CASE_INSENSITIVE);
		assertMatches("*cDe", SearchPattern.RULE_PATTERN_MATCH, pattern);
		// 1 or more consecutive '*' has to be the same as 1 '*'
		assertMatches("**cDe", SearchPattern.RULE_PATTERN_MATCH, pattern);
		// starting '*' is automatically expected
		assertMatches("cDe*", SearchPattern.RULE_PATTERN_MATCH, pattern);
	}

	/**
	 * Tests pattern match functionality.
	 */
	@Test
	public void testPatternMatch2() {
		Pattern pattern = Pattern.compile(".*c.*e.*i.*", Pattern.CASE_INSENSITIVE);
		assertMatches("*c*e*i", SearchPattern.RULE_PATTERN_MATCH, pattern);
		// starting '*' is automatically expected
		assertMatches("c*e*i", SearchPattern.RULE_PATTERN_MATCH, pattern);
	}

	/**
	 * Tests pattern match functionality.
	 */
	@Test
	public void testPatternForcedPrefixMatch() {
		Pattern pattern = Pattern.compile("c.*e.*i.*", Pattern.CASE_INSENSITIVE);
		assertMatches(">c*e*i", SearchPattern.RULE_PATTERN_MATCH, pattern);
	}

	/**
	 * Tests camelCase match functionality. Every string starting with an upperCase
	 * character should be recognized as camelCase pattern match rule. If there is
	 * no camelCase match, a simple substring match is tried as a fallback.
	 */
	@Test
	public void testCamelCaseMatch() {
		Pattern pattern = Pattern.compile(".*B[^A-Z]*C.*");
		Pattern fallbackPattern = Pattern.compile(".*BC.*", Pattern.CASE_INSENSITIVE);
		assertMatches("BC", SearchPattern.RULE_CAMELCASE_MATCH, pattern, fallbackPattern);
	}

	/**
	 * Tests camelCase match functionality.
	 */
	@Test
	public void testCamelCaseChangingCaseMatch() {
		Pattern pattern = Pattern.compile(".*Bi[^A-Z]*Ci.*");
		Pattern fallbackPattern = Pattern.compile(".*BiCi.*", Pattern.CASE_INSENSITIVE);
		assertMatches("BiCi", SearchPattern.RULE_CAMELCASE_MATCH, pattern, fallbackPattern);
	}

	/**
	 * Tests camelCase match functionality.
	 */
	@Test
	public void testCamelCaseForcedPrefixMatch() {
		Pattern pattern = Pattern.compile("B[^A-Z]*C.*");
		Pattern fallbackPattern = Pattern.compile("BC.*", Pattern.CASE_INSENSITIVE);
		assertMatches(">BC", SearchPattern.RULE_CAMELCASE_MATCH, pattern, fallbackPattern);
	}

	/**
	 * Tests camelCase match functionality.
	 */
	@Test
	public void testCamelCaseForcedEndMatch() {
		Pattern pattern = Pattern.compile(".*B[^A-Z]*C[^A-Z]*");
		Pattern fallbackPattern = Pattern.compile(".*BC", Pattern.CASE_INSENSITIVE);
		assertMatches("BC ", SearchPattern.RULE_CAMELCASE_MATCH, pattern, fallbackPattern);
		// alternative end character:
		assertMatches("BC<", SearchPattern.RULE_CAMELCASE_MATCH, pattern, fallbackPattern);
	}

	@Override
	protected SearchPattern createSearchPattern() {
		return new SearchPattern(SearchPattern.DEFAULT_MATCH_RULES | SearchPattern.RULE_SUBSTRING_MATCH);
	}

}
