/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
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
 *     Bredex GmbH - Creator of this testing class.
 ******************************************************************************/

package org.eclipse.ui.tests.activities;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.regex.Pattern;

import org.eclipse.ui.internal.activities.PatternUtil;
import org.junit.Test;

/**
 * Test for:
 * Utility helper class for regular expression string patterns.
 *
 * @since 3.4
 * @author Jan Diederich
 */
public class PatternUtilTest {

	/**
	 * Tests if the quote function of ActivityPatternBinding works.
	 */
	@Test
	public void testQuotePattern() {
		assertTrue(PatternUtil.quotePattern("abcd").equals("\\Qabcd\\E"));
		assertTrue(PatternUtil.quotePattern("Test\\Q").equals("\\QTest\\Q\\E"));
		assertTrue(PatternUtil.quotePattern("Test\\Q\\E").equals(
				"\\QTest\\Q\\E\\\\E\\Q\\E"));
		assertTrue(PatternUtil.quotePattern("Test\\E\\Q").equals(
				"\\QTest\\E\\\\E\\Q\\Q\\E"));
		assertTrue(PatternUtil.quotePattern("\\ETest\\E\\\\E\\E").equals(
				"\\Q\\E\\\\E\\QTest\\E\\\\E\\Q\\\\E\\\\E\\Q\\E\\\\E\\Q\\E"));
		String searchString = "xy[^a]";
		assertFalse(Pattern.compile(searchString).matcher(searchString)
				.matches());
		assertTrue(Pattern.compile(PatternUtil.quotePattern(searchString))
				.matcher(searchString).matches());
	}

	/**
	 * For quick testing, without JUnit launch.
	 */
	public static void main(String[] args) {
		new PatternUtilTest().testQuotePattern();
	}
}
