/*******************************************************************************
 * Copyright (c) 2007, 2012 IBM Corporation and others.
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

package org.eclipse.ui.tests;

import org.eclipse.jface.text.contentassist.BoldStylerProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.dialogs.StyledStringHighlighter;
import org.eclipse.ui.tests.harness.util.UITestCase;

public class StyledStringHighlighterTest extends UITestCase {

	public StyledStringHighlighterTest(String testName) {
		super(testName);
	}

	private StyledStringHighlighter cut;
	private static Display display;
	private static Font font;
	private static BoldStylerProvider boldStyler;

	@Override
	public void doSetUp() {
		display = Display.getCurrent();
		font = new Font(display, "Arial", 14, SWT.BOLD);
		boldStyler = new BoldStylerProvider(font);
		cut = new StyledStringHighlighter();
	}

	@Override
	protected void doTearDown() {
		if (boldStyler != null) {
			boldStyler.dispose();
			boldStyler = null;
		}
		if (font != null && !font.isDisposed()) {
			font.dispose();
			font = null;
		}
		if (display != null && !display.isDisposed()) {
			display.dispose();
			display = null;
		}
	}

	public void testNullAndEmptyParameter() {
		// no exceptions expected
		cut.highlight(null, "", boldStyler.getBoldStyler());
		cut.highlight("", null, boldStyler.getBoldStyler());
		cut.highlight("", "", null);
		cut.highlight("", "", boldStyler.getBoldStyler());
	}

	public void testFullHighlighting() {
		assertHighlightedRegions("abcd", "abcd", new int[] { 0, 4 });
	}

	public void testManyAsterisks() {
		assertHighlightedRegions("abcd", "**a**b****c***d****", new int[] { 0, 4 });
	}

	public void testEndTerminator() {
		assertHighlightedRegions("abcd", "abcd<", new int[] { 0, 4 });
	}

	public void testMultipleEndTerminators() {
		assertHighlightedRegions("abcd", "abcd<<<<<<<", new int[] { 0, 4 });
	}

	public void testAstersisksAndEndTerminator() {
		assertHighlightedRegions("abcd", "**a**b**c**d<", new int[] { 0, 4 });
	}

	public void testAstersisksForOneGap() {
		assertHighlightedRegions("abcd", "a*d", new int[] { 0, 1, 3, 4 });
	}

	public void testAstersisksForGaps() {
		assertHighlightedRegions("abcdefgh", "a*d*f*h<", new int[] { 0, 1, 3, 4, 5, 6, 7, 8 });
	}

	public void testQuestionMarks() {
		assertHighlightedRegions("abcdef", "a??d?f", new int[] { 0, 1, 3, 4, 5, 6 });
	}

	public void testStartsWithQuestionMark() {
		assertHighlightedRegions("abcdef", "?b?d?f", new int[] { 1, 2, 3, 4, 5, 6 });
	}

	public void testEndsWithQuestionMark() {
		assertHighlightedRegions("abcdef", "a??de?", new int[] { 0, 1, 3, 5 });
	}

	public void testStartsWithAsteriskAndQuestionMark() {
		assertHighlightedRegions("abcdef", "*?b?def", new int[] { 1, 2, 3, 6 });
	}

	public void testEndsWithQuestionMarkAndEndTerminator() {
		assertHighlightedRegions("abcdef", "a??de?<", new int[] { 0, 1, 3, 5 });
	}

	public void testCaseInsensitivity() {
		assertHighlightedRegions("abc123def", "aBC123dEf", new int[] { 0, 9 });
	}

	public void testJustAsteriskNothingFound() {
		assertHighlightedRegions("abcdef", "*");
	}

	public void testJustQuestionMark() {
		assertHighlightedRegions("a", "?");
		assertHighlightedRegions("abcd", "?");
	}

	public void testTextShorterThanPattern() {
		assertHighlightedRegions("abcd", "abcdefgh");
	}

	public void testNothingFound() {
		assertHighlightedRegions("abcdefg", "*nothing*");
	}

	public void testAll() {
		assertHighlightedRegions("abcdef", "*a??d*f<", new int[] { 0, 1, 3, 4, 5, 6 });
	}

	private void assertHighlightedRegions(String text, String filterPattern) {
		assertHighlightedRegions(text, filterPattern, new int[] {});

	}

	private void assertHighlightedRegions(String text, String filterPattern, int[] expRanges) {
		StyledString styledString = cut.highlight(text, filterPattern, boldStyler.getBoldStyler());

		StyleRange[] actRanges = styledString.getStyleRanges();
		assertEquals("Number of ranges differs.", expRanges.length / 2, actRanges.length);

		int expIndex = 0;
		for (StyleRange styleRange : actRanges) {
			int actStart = styleRange.start;
			assertEquals("Start positions differ.", expRanges[expIndex], actStart);
			expIndex++;

			int actEnd = actStart + styleRange.length;
			assertEquals("End positions differ.", expRanges[expIndex], actEnd);
			expIndex++;
		}
	}

}
