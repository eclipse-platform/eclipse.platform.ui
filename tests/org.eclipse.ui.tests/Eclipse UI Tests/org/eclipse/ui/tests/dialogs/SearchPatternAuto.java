/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.tests.dialogs;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.regex.Pattern;

import junit.framework.TestCase;

import org.eclipse.ui.dialogs.SearchPattern;

/**
 * Test case for tests SearchPattern match functionality 
 * 
 * @since 3.3
 *
 */
public class SearchPatternAuto extends TestCase {
	
	private static ArrayList resources = new ArrayList();
	
	
	static {
		
		generateRescourcesTestCases('A', 'C', 8, "");
		
		generateRescourcesTestCases('A', 'C', 4, "");
		
	}
	/**
	 * @param name
	 */
	public SearchPatternAuto(String name) {
		super(name);
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
	}
	
	/**
	 * Genarates strings data for match test cases.
	 * 
	 * @param startChar
	 * @param endChar
	 * @param lenght
	 * @param resource
	 */
	private static void generateRescourcesTestCases(char startChar, char endChar, int lenght, String resource){
		for (char ch = startChar; ch <= endChar; ch++) {
			String res = resource + String.valueOf(ch);
			if (lenght == res.length()) 
				resources.add(res);
			else if ((res.trim().length() % 2) == 0)
					generateRescourcesTestCases(Character.toUpperCase((char)(startChar + 1)), Character.toUpperCase((char)(endChar + 1)), lenght, res + " ");
				else 
					generateRescourcesTestCases(Character.toLowerCase((char)(startChar + 1)), Character.toLowerCase((char)(endChar + 1)), lenght, res);
		}
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	/**
	 * Tests exact match funtionality
	 */
	public void testExactMatch1() {
		String patternText = "aBcD ";
		Pattern pattern = Pattern.compile("aBcD", Pattern.CASE_INSENSITIVE);
		SearchPattern patternMatcher = new SearchPattern();
		patternMatcher.setPattern(patternText);
		assertEquals(patternMatcher.getMatchRule(), SearchPattern.RULE_EXACT_MATCH);
		for (Iterator iter = resources.iterator(); iter.hasNext();) {
			String res = (String) iter.next();
			assertEquals(patternMatcher.matches(res), pattern.matcher(res).matches());
		}
	}
	
	/**
	 * Tests exact match funtionality
	 */
	public void testExactMatch2() {
		String patternText = "AbCdEfGh<";
		Pattern pattern = Pattern.compile("AbCdEfGh", Pattern.CASE_INSENSITIVE);
		SearchPattern patternMatcher = new SearchPattern();
		patternMatcher.setPattern(patternText);
		assertEquals(patternMatcher.getMatchRule(), SearchPattern.RULE_EXACT_MATCH);
		for (Iterator iter = resources.iterator(); iter.hasNext();) {
			String res = (String) iter.next();
			assertEquals(patternMatcher.matches(res), pattern.matcher(res).matches());
		}
	}
	
	/**
	 * Tests prefix match funtionality
	 */
	public void testPrefixMatch() {
		String patternText = "ab";
		Pattern pattern = Pattern.compile("ab.*", Pattern.CASE_INSENSITIVE);
		SearchPattern patternMatcher = new SearchPattern();
		patternMatcher.setPattern(patternText);
		assertEquals(patternMatcher.getMatchRule(), SearchPattern.RULE_PREFIX_MATCH);
		for (Iterator iter = resources.iterator(); iter.hasNext();) {
			String res = (String) iter.next();
			assertEquals(patternMatcher.matches(res), pattern.matcher(res).matches());
		}
	}
	
	/**
	 * Tests pattern match funtionality
	 */
	public void testPatternMatch1() {
		String patternText = "**cDe";
		Pattern pattern = Pattern.compile(".*cde.*", Pattern.CASE_INSENSITIVE);
		SearchPattern patternMatcher = new SearchPattern();
		patternMatcher.setPattern(patternText);
		assertEquals(patternMatcher.getMatchRule(), SearchPattern.RULE_PATTERN_MATCH);
		for (Iterator iter = resources.iterator(); iter.hasNext();) {
			String res = (String) iter.next();
			assertEquals(patternMatcher.matches(res), pattern.matcher(res).matches());
		}
	}
	
	/**
	 * Tests pattern match funtionality
	 */
	public void testPatternMatch2() {
		String patternText = "**c*e*i";
		Pattern pattern = Pattern.compile(".*c.*e.*i.*", Pattern.CASE_INSENSITIVE);
		SearchPattern patternMatcher = new SearchPattern();
		patternMatcher.setPattern(patternText);
		assertEquals(patternMatcher.getMatchRule(), SearchPattern.RULE_PATTERN_MATCH);
		for (Iterator iter = resources.iterator(); iter.hasNext();) {
			String res = (String) iter.next();
			assertEquals(patternMatcher.matches(res), pattern.matcher(res).matches());
		}
	}
	
	/**
	 * Tests camelCase match funtionality.
	 * Every string contains only upperCase characters should be racognize as camelCase pattern match rule.
	 * Result for "CD" SearchPattern should be similar to regexp pattern "C[^A-Z]*D.*" or "CD.*"
	 */
	public void testCamelCaseMatch() {
		String patternText = "CD";
		Pattern pattern = Pattern.compile("C[^A-Z]*D.*");
		Pattern pattern2 = Pattern.compile("CD.*", Pattern.CASE_INSENSITIVE);
		SearchPattern patternMatcher = new SearchPattern();
		patternMatcher.setPattern(patternText);
		assertEquals(patternMatcher.getMatchRule(), SearchPattern.RULE_CAMELCASE_MATCH);
		for (Iterator iter = resources.iterator(); iter.hasNext();) {
			String res = (String) iter.next();
			if (patternMatcher.matches(res) != pattern.matcher(res).matches()) {
				assertEquals(patternMatcher.matches(res), pattern2.matcher(res).matches());
			}
		}
	}
	
	/**
	 * Tests blank match funtionality. 
	 * Blank string should be racognize as blank pattern match rule.
	 * It should match with all resources.
	 * Result for SearchPattern should be similar to regexp pattern ".*"
	 */
	public void testBlankMatch() {
		String patternText = "";
		Pattern pattern = Pattern.compile(".*", Pattern.CASE_INSENSITIVE);
		SearchPattern patternMatcher = new SearchPattern();
		patternMatcher.setPattern(patternText);
		assertEquals(patternMatcher.getMatchRule(), SearchPattern.RULE_BLANK_MATCH);
		for (Iterator iter = resources.iterator(); iter.hasNext();) {
			String res = (String) iter.next();
			assertEquals(patternMatcher.matches(res), pattern.matcher(res).matches());
		}
	}
	
}
