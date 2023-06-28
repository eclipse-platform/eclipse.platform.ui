/*******************************************************************************
 * Copyright (c) 2000, 2020 IBM Corporation and others.
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
package org.eclipse.jface.text.tests;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GlyphMetrics;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextPresentation;

public class TextPresentationTest {

	private static final int NORMAL= SWT.NORMAL;
	private static final int BOLD= SWT.BOLD;

	private TextPresentation fTextPresentation;
	private StyleRange[] fAllRanges;
	private StyleRange[] fNonDefaultRanges;

	// collect colors for disposal
	private ArrayList<Color> fColors= new ArrayList<>();

	private Display fDisplay;

	@Before
	public void setUp() {
		fDisplay= Display.getDefault();
		setUpStyleRanges();
		setUpTextPresentation();
	}

	private void setUpStyleRanges() {
		fAllRanges= new StyleRange[] {
			createStyleRange(  0,   4, NORMAL),
			createStyleRange(  4,  20, BOLD),
			createStyleRange( 20,  47, NORMAL),
			createStyleRange( 47,  54, BOLD),
			createStyleRange( 54,  96, NORMAL),
			createStyleRange( 96, 102, BOLD),
			createStyleRange(102, 140, NORMAL)
		};

		fNonDefaultRanges= new StyleRange[] {
			createStyleRange(  4,  20, BOLD),
			createStyleRange( 47,  54, BOLD),
			createStyleRange( 96, 102, BOLD)
		};
	}

	private void setUpTextPresentation() {
		fTextPresentation= new TextPresentation();
		fTextPresentation.setDefaultStyleRange(createStyleRange(0, 140, NORMAL));
		for (StyleRange range : fAllRanges) {
			fTextPresentation.addStyleRange(range);
		}
	}

	@After
	public void tearDown() {
		fColors.clear();
		if (!fDisplay.isDisposed()) {
			for (Shell shell : fDisplay.getShells()) {
				shell.dispose();
			}
		}
	}

	private StyleRange createStyleRange(int start, int end, int style) {
		return createStyleRange(start, end, style, null, null);
	}

	private StyleRange createStyleRange(int start, int end, int style, Color foreground, Color background) {
		return new StyleRange(start, end - start, foreground, background, style);
	}

	private StyleRange createStyleRange(int start, int end, int style, int foreground, int background) {
		return createStyleRange(start, end, style, createColor(foreground, foreground, foreground), createColor(background, background, background));
	}

	/**
	 * Creates a new color.
	 *
	 * @param red the amount of red in the color
	 * @param green the amount of green in the color
	 * @param blue the amount of blue in the color
	 * @return <code>null</code> if any of the parameters is smaller than 0 or greater than 255
	 */
	private Color createColor(int red, int green, int blue) {
		if (red < 0 || red > 255 || green < 0 || green > 255 || blue < 0 || blue > 255)
			return null;
		Color c= new Color(fDisplay, red, green, blue);
		fColors.add(c);
		return c;
	}

	/**
	 * Check that the presentation contains the expected style ranges and other presentation methods
	 * return compatible values.
	 *
	 * @param expectedAllRanges all styles expected for the whole presentation
	 * @param expectedNonDefaultRanges all styles which are not the default for the whole
	 *            presentation
	 */
	protected void checkRegions(StyleRange[] expectedAllRanges, StyleRange[] expectedNonDefaultRanges) {
		checkRegions(expectedAllRanges, fTextPresentation.getAllStyleRangeIterator(), true);
		checkRegions(expectedNonDefaultRanges, fTextPresentation.getNonDefaultStyleRangeIterator(), false);
		// now test the same but remove the default style
		StyleRange defaultRange= fTextPresentation.getDefaultStyleRange();
		fTextPresentation.setDefaultStyleRange(null);
		checkRegions(expectedAllRanges, fTextPresentation.getAllStyleRangeIterator(), true);
		checkRegions(expectedAllRanges, fTextPresentation.getNonDefaultStyleRangeIterator(), true);
		fTextPresentation.setDefaultStyleRange(defaultRange);
	}

	/**
	 * Check expected regions against existing regions from presentation. Also validates that
	 * iterator returns ordered and disjoint styles and most other presentation methods.
	 *
	 * @param expectedRanges the expected regions
	 * @param rangeIterator iterator from presentation to get the actual regions
	 * @param withDefaults if <code>true</code> the expected ranges also contains the ranges which
	 *            are equal to default style
	 */
	private void checkRegions(StyleRange[] expectedRanges, Iterator<StyleRange> rangeIterator, boolean withDefaults) {
		StyleRange defaultRange= fTextPresentation.getDefaultStyleRange();
		int start= -1, end= -1;
		int lastEnd= defaultRange != null ? defaultRange.start : Integer.MIN_VALUE;
		for (int i= 0; i < expectedRanges.length; i++) {
			StyleRange expectedRange= expectedRanges[i];
			assertTrue("Presentation has less ranges than expected.", rangeIterator.hasNext());
			StyleRange actualRange= rangeIterator.next();
			assertEquals(expectedRange, actualRange);
			assertTrue("Unexpected default style.", withDefaults || !actualRange.similarTo(defaultRange));
			assertTrue("Overlapping or wrong ordered style.", lastEnd <= actualRange.start);
			lastEnd= actualRange.start + actualRange.length;

			// test first and last range methods
			if (i == 0) {
				start= actualRange.start;
				StyleRange first= fTextPresentation.getFirstStyleRange();
				if (withDefaults) {
					assertEquals("getFirstStyleRange() failed", expectedRange, first);
				} else {
					assertTrue("getFirstStyleRange() failed", first.equals(expectedRange) || first.similarTo(defaultRange));
				}
			} else if (i == expectedRanges.length - 1) {
				end= actualRange.start + actualRange.length;
				StyleRange last= fTextPresentation.getLastStyleRange();
				if (withDefaults) {
					assertEquals("getLastStyleRange() failed", expectedRange, last);
				} else {
					assertTrue("getLastStyleRange() failed", last.equals(expectedRange) || last.similarTo(defaultRange));
				}
			}
		}
		assertTrue("Presentation has more ranges than expected.", !rangeIterator.hasNext());
		if (withDefaults) {
			assertEquals("getDenumerableRanges() failed", expectedRanges.length, fTextPresentation.getDenumerableRanges());
		}
		assertEquals("isEmpty() failed", Boolean.valueOf(expectedRanges.length == 0 && defaultRange == null), Boolean.valueOf(fTextPresentation.isEmpty()));
		IRegion expectedCover;
		if (defaultRange == null) {
			expectedCover= (start == -1 && end == -1 ? null : new Region(start, end - start));
		} else {
			expectedCover= new Region(defaultRange.start, defaultRange.length);
		}
		assertEquals("Wrong coverage", expectedCover, fTextPresentation.getCoverage());
	}

	@Test
	public void testUnclippedRegions() {
		checkRegions(fAllRanges, fNonDefaultRanges);
	}

	@Test
	public void testClippedRegions1() {
		fTextPresentation.setResultWindow(new Region(0, 140));
		checkRegions(fAllRanges, fNonDefaultRanges);
	}

	@Test
	public void testClippedRegions2() {

		fTextPresentation.setResultWindow(new Region(30, 70));

		StyleRange[] expectedAllRanges= new StyleRange[] {
			createStyleRange(  0, 17, NORMAL),
			createStyleRange( 17, 24, BOLD),
			createStyleRange( 24, 66, NORMAL),
			createStyleRange( 66, 70, BOLD)
		};

		StyleRange[] expectedNonDefaultRanges= new StyleRange[] {
			createStyleRange( 17, 24, BOLD),
			createStyleRange( 66, 70, BOLD)
		};

		checkRegions(expectedAllRanges, expectedNonDefaultRanges);
	}

	/**
	 * Merge range at start of first existing range.
	 */
	@Test
	public void testMergeStyleRange1() {
		StyleRange range= createStyleRange(0, 2, NORMAL, 1, -1);
		fTextPresentation.mergeStyleRange(range);

		StyleRange[] expectedAllRanges= new StyleRange[] {
				createStyleRange(  0,   2, NORMAL, 1, -1),
				createStyleRange(  2,   4, NORMAL),
				createStyleRange(  4,  20, BOLD),
				createStyleRange( 20,  47, NORMAL),
				createStyleRange( 47,  54, BOLD),
				createStyleRange( 54,  96, NORMAL),
				createStyleRange( 96, 102, BOLD),
				createStyleRange(102, 140, NORMAL),
		};

		StyleRange[] expectedNonDefaultRanges= new StyleRange[] {
				createStyleRange(  0,   2, NORMAL, 1, -1),
				createStyleRange(  4,  20, BOLD),
				createStyleRange( 47,  54, BOLD),
				createStyleRange( 96, 102, BOLD),
		};

		checkRegions(expectedAllRanges, expectedNonDefaultRanges);
	}

	/**
	 * Merge range at end of last existing range.
	 */
	@Test
	public void testMergeStyleRange2() {
		StyleRange range= createStyleRange(138, 140, NORMAL, 1, -1);
		fTextPresentation.mergeStyleRange(range);

		StyleRange[] expectedAllRanges= new StyleRange[] {
				createStyleRange(  0,   4, NORMAL),
				createStyleRange(  4,  20, BOLD),
				createStyleRange( 20,  47, NORMAL),
				createStyleRange( 47,  54, BOLD),
				createStyleRange( 54,  96, NORMAL),
				createStyleRange( 96, 102, BOLD),
				createStyleRange(102, 138, NORMAL),
				createStyleRange(138, 140, NORMAL, 1, -1),
		};

		StyleRange[] expectedNonDefaultRanges= new StyleRange[] {
				createStyleRange(  4,  20, BOLD),
				createStyleRange( 47,  54, BOLD),
				createStyleRange( 96, 102, BOLD),
				createStyleRange(138, 140, NORMAL, 1, -1),
		};

		checkRegions(expectedAllRanges, expectedNonDefaultRanges);
	}

	/**
	 * Merge range at start of existing default range.
	 */
	@Test
	public void testMergeStyleRange3() {
		StyleRange range= createStyleRange(20, 22, NORMAL, 1, -1);
		fTextPresentation.mergeStyleRange(range);

		StyleRange[] expectedAllRanges= new StyleRange[] {
				createStyleRange(  0,   4, NORMAL),
				createStyleRange(  4,  20, BOLD),
				createStyleRange( 20,  22, NORMAL, 1, -1),
				createStyleRange( 22,  47, NORMAL),
				createStyleRange( 47,  54, BOLD),
				createStyleRange( 54,  96, NORMAL),
				createStyleRange( 96, 102, BOLD),
				createStyleRange(102, 140, NORMAL),
		};

		StyleRange[] expectedNonDefaultRanges= new StyleRange[] {
				createStyleRange(  4,  20, BOLD),
				createStyleRange( 20,  22, NORMAL, 1, -1),
				createStyleRange( 47,  54, BOLD),
				createStyleRange( 96, 102, BOLD),
		};

		checkRegions(expectedAllRanges, expectedNonDefaultRanges);
	}

	/**
	 * Merge range within existing default range.
	 */
	@Test
	public void testMergeStyleRange4() {
		StyleRange range= createStyleRange(22, 24, NORMAL, 1, -1);
		fTextPresentation.mergeStyleRange(range);

		StyleRange[] expectedAllRanges= new StyleRange[] {
				createStyleRange(  0,   4, NORMAL),
				createStyleRange(  4,  20, BOLD),
				createStyleRange( 20,  22, NORMAL),
				createStyleRange( 22,  24, NORMAL, 1, -1),
				createStyleRange( 24,  47, NORMAL),
				createStyleRange( 47,  54, BOLD),
				createStyleRange( 54,  96, NORMAL),
				createStyleRange( 96, 102, BOLD),
				createStyleRange(102, 140, NORMAL),
		};

		StyleRange[] expectedNonDefaultRanges= new StyleRange[] {
				createStyleRange(  4,  20, BOLD),
				createStyleRange( 22,  24, NORMAL, 1, -1),
				createStyleRange( 47,  54, BOLD),
				createStyleRange( 96, 102, BOLD),
		};

		checkRegions(expectedAllRanges, expectedNonDefaultRanges);
	}

	/**
	 * Merge range at end of existing default range.
	 */
	@Test
	public void testMergeStyleRange5() {
		StyleRange range= createStyleRange(45, 47, NORMAL, 1, -1);
		fTextPresentation.mergeStyleRange(range);

		StyleRange[] expectedAllRanges= new StyleRange[] {
				createStyleRange(  0,   4, NORMAL),
				createStyleRange(  4,  20, BOLD),
				createStyleRange( 20,  45, NORMAL),
				createStyleRange( 45,  47, NORMAL, 1, -1),
				createStyleRange( 47,  54, BOLD),
				createStyleRange( 54,  96, NORMAL),
				createStyleRange( 96, 102, BOLD),
				createStyleRange(102, 140, NORMAL),
		};

		StyleRange[] expectedNonDefaultRanges= new StyleRange[] {
				createStyleRange(  4,  20, BOLD),
				createStyleRange( 45,  47, NORMAL, 1, -1),
				createStyleRange( 47,  54, BOLD),
				createStyleRange( 96, 102, BOLD),
		};

		checkRegions(expectedAllRanges, expectedNonDefaultRanges);
	}

	/**
	 * Merge range at start of existing non-default range.
	 */
	@Test
	public void testMergeStyleRange6() {
		StyleRange range= createStyleRange(47, 49, NORMAL, 1, -1);
		fTextPresentation.mergeStyleRange(range);

		StyleRange[] expectedAllRanges= new StyleRange[] {
				createStyleRange(  0,   4, NORMAL),
				createStyleRange(  4,  20, BOLD),
				createStyleRange( 20,  47, NORMAL),
				createStyleRange( 47,  49, BOLD, 1, -1),
				createStyleRange( 49,  54, BOLD),
				createStyleRange( 54,  96, NORMAL),
				createStyleRange( 96, 102, BOLD),
				createStyleRange(102, 140, NORMAL),
		};

		StyleRange[] expectedNonDefaultRanges= new StyleRange[] {
				createStyleRange(  4,  20, BOLD),
				createStyleRange( 47,  49, BOLD, 1, -1),
				createStyleRange( 49,  54, BOLD),
				createStyleRange( 96, 102, BOLD),
		};

		checkRegions(expectedAllRanges, expectedNonDefaultRanges);
	}

	/**
	 * Merge range within existing non-default range.
	 */
	@Test
	public void testMergeStyleRange7() {
		StyleRange range= createStyleRange(49, 51, NORMAL, 1, -1);
		fTextPresentation.mergeStyleRange(range);

		StyleRange[] expectedAllRanges= new StyleRange[] {
				createStyleRange(  0,   4, NORMAL),
				createStyleRange(  4,  20, BOLD),
				createStyleRange( 20,  47, NORMAL),
				createStyleRange( 47,  49, BOLD),
				createStyleRange( 49,  51, BOLD, 1, -1),
				createStyleRange( 51,  54, BOLD),
				createStyleRange( 54,  96, NORMAL),
				createStyleRange( 96, 102, BOLD),
				createStyleRange(102, 140, NORMAL),
		};

		StyleRange[] expectedNonDefaultRanges= new StyleRange[] {
				createStyleRange(  4,  20, BOLD),
				createStyleRange( 47,  49, BOLD),
				createStyleRange( 49,  51, BOLD, 1, -1),
				createStyleRange( 51,  54, BOLD),
				createStyleRange( 96, 102, BOLD),
		};

		checkRegions(expectedAllRanges, expectedNonDefaultRanges);
	}

	/**
	 * Merge range at end of existing non-default range.
	 */
	@Test
	public void testMergeStyleRange8() {
		StyleRange range= createStyleRange(52, 54, NORMAL, 1, -1);
		fTextPresentation.mergeStyleRange(range);

		StyleRange[] expectedAllRanges= new StyleRange[] {
				createStyleRange(  0,   4, NORMAL),
				createStyleRange(  4,  20, BOLD),
				createStyleRange( 20,  47, NORMAL),
				createStyleRange( 47,  52, BOLD),
				createStyleRange( 52,  54, BOLD, 1, -1),
				createStyleRange( 54,  96, NORMAL),
				createStyleRange( 96, 102, BOLD),
				createStyleRange(102, 140, NORMAL),
		};

		StyleRange[] expectedNonDefaultRanges= new StyleRange[] {
				createStyleRange(  4,  20, BOLD),
				createStyleRange( 47,  52, BOLD),
				createStyleRange( 52,  54, BOLD, 1, -1),
				createStyleRange( 96, 102, BOLD),
		};

		checkRegions(expectedAllRanges, expectedNonDefaultRanges);
	}

	/**
	 * Merge range from existing default to non-default range.
	 */
	@Test
	public void testMergeStyleRange9() {
		StyleRange range= createStyleRange(45, 49, NORMAL, 1, -1);
		fTextPresentation.mergeStyleRange(range);

		StyleRange[] expectedAllRanges= new StyleRange[] {
				createStyleRange(  0,   4, NORMAL),
				createStyleRange(  4,  20, BOLD),
				createStyleRange( 20,  45, NORMAL),
				createStyleRange( 45,  47, NORMAL, 1, -1),
				createStyleRange( 47,  49, BOLD,   1, -1),
				createStyleRange( 49,  54, BOLD),
				createStyleRange( 54,  96, NORMAL),
				createStyleRange( 96, 102, BOLD),
				createStyleRange(102, 140, NORMAL),
		};

		StyleRange[] expectedNonDefaultRanges= new StyleRange[] {
				createStyleRange(  4,  20, BOLD),
				createStyleRange( 45,  47, NORMAL, 1, -1),
				createStyleRange( 47,  49, BOLD,   1, -1),
				createStyleRange( 49,  54, BOLD),
				createStyleRange( 96, 102, BOLD),
		};

		checkRegions(expectedAllRanges, expectedNonDefaultRanges);
	}

	/**
	 * Merge range from existing non-default to default range.
	 */
	@Test
	public void testMergeStyleRange10() {
		StyleRange range= createStyleRange(52, 56, NORMAL, 1, -1);
		fTextPresentation.mergeStyleRange(range);

		StyleRange[] expectedAllRanges= new StyleRange[] {
				createStyleRange(  0,   4, NORMAL),
				createStyleRange(  4,  20, BOLD),
				createStyleRange( 20,  47, NORMAL),
				createStyleRange( 47,  52, BOLD),
				createStyleRange( 52,  54, BOLD,   1, -1),
				createStyleRange( 54,  56, NORMAL, 1, -1),
				createStyleRange( 56,  96, NORMAL),
				createStyleRange( 96, 102, BOLD),
				createStyleRange(102, 140, NORMAL),
		};

		StyleRange[] expectedNonDefaultRanges= new StyleRange[] {
				createStyleRange(  4,  20, BOLD),
				createStyleRange( 47,  52, BOLD),
				createStyleRange( 52,  54, BOLD,   1, -1),
				createStyleRange( 54,  56, NORMAL, 1, -1),
				createStyleRange( 96, 102, BOLD),
		};

		checkRegions(expectedAllRanges, expectedNonDefaultRanges);
	}

	/**
	 * Merge range from existing default over non-default to default range.
	 */
	@Test
	public void testMergeStyleRange11() {
		StyleRange range= createStyleRange(45, 56, NORMAL, 1, -1);
		fTextPresentation.mergeStyleRange(range);

		StyleRange[] expectedAllRanges= new StyleRange[] {
				createStyleRange(  0,   4, NORMAL),
				createStyleRange(  4,  20, BOLD),
				createStyleRange( 20,  45, NORMAL),
				createStyleRange( 45,  47, NORMAL, 1, -1),
				createStyleRange( 47,  54, BOLD,   1, -1),
				createStyleRange( 54,  56, NORMAL, 1, -1),
				createStyleRange( 56,  96, NORMAL),
				createStyleRange( 96, 102, BOLD),
				createStyleRange(102, 140, NORMAL),
		};

		StyleRange[] expectedNonDefaultRanges= new StyleRange[] {
				createStyleRange(  4,  20, BOLD),
				createStyleRange( 45,  47, NORMAL, 1, -1),
				createStyleRange( 47,  54, BOLD,   1, -1),
				createStyleRange( 54,  56, NORMAL, 1, -1),
				createStyleRange( 96, 102, BOLD),
		};

		checkRegions(expectedAllRanges, expectedNonDefaultRanges);
	}

	/**
	 * Merge range from existing non-default over default to non-default range.
	 */
	@Test
	public void testMergeStyleRange12() {
		StyleRange range= createStyleRange(52, 98, NORMAL, 1, -1);
		fTextPresentation.mergeStyleRange(range);

		StyleRange[] expectedAllRanges= new StyleRange[] {
				createStyleRange(  0,   4, NORMAL),
				createStyleRange(  4,  20, BOLD),
				createStyleRange( 20,  47, NORMAL),
				createStyleRange( 47,  52, BOLD),
				createStyleRange( 52,  54, BOLD,   1, -1),
				createStyleRange( 54,  96, NORMAL, 1, -1),
				createStyleRange( 96,  98, BOLD,   1, -1),
				createStyleRange( 98, 102, BOLD),
				createStyleRange(102, 140, NORMAL),
		};

		StyleRange[] expectedNonDefaultRanges= new StyleRange[] {
				createStyleRange(  4,  20, BOLD),
				createStyleRange( 47,  52, BOLD),
				createStyleRange( 52,  54, BOLD,   1, -1),
				createStyleRange( 54,  96, NORMAL, 1, -1),
				createStyleRange( 96,  98, BOLD,   1, -1),
				createStyleRange( 98, 102, BOLD),
		};

		checkRegions(expectedAllRanges, expectedNonDefaultRanges);
	}

	/**
	 * Merge range over existing default range.
	 */
	@Test
	public void testMergeStyleRange13() {
		StyleRange range= createStyleRange(20, 47, NORMAL, 1, -1);
		fTextPresentation.mergeStyleRange(range);

		StyleRange[] expectedAllRanges= new StyleRange[] {
				createStyleRange(  0,   4, NORMAL),
				createStyleRange(  4,  20, BOLD),
				createStyleRange( 20,  47, NORMAL, 1, -1),
				createStyleRange( 47,  54, BOLD),
				createStyleRange( 54,  96, NORMAL),
				createStyleRange( 96, 102, BOLD),
				createStyleRange(102, 140, NORMAL),
		};

		StyleRange[] expectedNonDefaultRanges= new StyleRange[] {
				createStyleRange(  4,  20, BOLD),
				createStyleRange( 20,  47, NORMAL, 1, -1),
				createStyleRange( 47,  54, BOLD),
				createStyleRange( 96, 102, BOLD),
		};

		checkRegions(expectedAllRanges, expectedNonDefaultRanges);
	}

	/**
	 * Merge range over existing non-default range.
	 */
	@Test
	public void testMergeStyleRange14() {
		StyleRange range= createStyleRange(47, 54, NORMAL, 1, -1);
		fTextPresentation.mergeStyleRange(range);

		StyleRange[] expectedAllRanges= new StyleRange[] {
				createStyleRange(  0,   4, NORMAL),
				createStyleRange(  4,  20, BOLD),
				createStyleRange( 20,  47, NORMAL),
				createStyleRange( 47,  54, BOLD, 1, -1),
				createStyleRange( 54,  96, NORMAL),
				createStyleRange( 96, 102, BOLD),
				createStyleRange(102, 140, NORMAL),
		};

		StyleRange[] expectedNonDefaultRanges= new StyleRange[] {
				createStyleRange(  4,  20, BOLD),
				createStyleRange( 47,  54, BOLD, 1, -1),
				createStyleRange( 96, 102, BOLD),
		};

		checkRegions(expectedAllRanges, expectedNonDefaultRanges);
	}

	/**
	 * Merge range over whole presentation.
	 */
	@Test
	public void testMergeStyleRange15() {
		StyleRange range= createStyleRange(0, 140, NORMAL, 1, -1);

		Color expectedColor= createColor(1, 1, 1);
		StyleRange[] expectedAllRanges= new StyleRange[fAllRanges.length];
		for (int i= 0; i < fAllRanges.length; i++) {
			expectedAllRanges[i]= (StyleRange) fAllRanges[i].clone();
			expectedAllRanges[i].foreground= expectedColor;
		}

		StyleRange[] expectedNonDefaultRanges= expectedAllRanges;

		fTextPresentation.mergeStyleRange(range);
		checkRegions(expectedAllRanges, expectedNonDefaultRanges);


		range= createStyleRange(-100, 200, BOLD);

		for (int i= 0; i < expectedAllRanges.length; i++) {
			expectedAllRanges[i].fontStyle= BOLD;
		}

		fTextPresentation.mergeStyleRange(range);
		checkRegions(expectedAllRanges, expectedNonDefaultRanges);

		fTextPresentation.setDefaultStyleRange(null);
		range= createStyleRange(-150, 250, NORMAL, -1, 2);

		expectedColor= createColor(2, 2, 2);
		for (int i= 0; i < expectedAllRanges.length; i++) {
			expectedAllRanges[i].background= expectedColor;
		}
		StyleRange[] newExpectedRanges= new StyleRange[expectedAllRanges.length + 2];
		System.arraycopy(expectedAllRanges, 0, newExpectedRanges, 1, expectedAllRanges.length);
		newExpectedRanges[0]= createStyleRange(-150, 0, NORMAL, -1, 2);
		newExpectedRanges[newExpectedRanges.length - 1]= createStyleRange(140, 250, NORMAL, -1, 2);

		fTextPresentation.mergeStyleRange(range);
		checkRegions(newExpectedRanges, newExpectedRanges);
	}

	/**
	 * Merge range covering existing default range.
	 */
	@Test
	public void testMergeStyleRange16() {
		StyleRange range= createStyleRange(20, 47, NORMAL, 1, -1);
		fTextPresentation.mergeStyleRange(range);

		StyleRange[] expectedAllRanges= new StyleRange[] {
				createStyleRange(  0,   4, NORMAL),
				createStyleRange(  4,  20, BOLD),
				createStyleRange( 20,  47, NORMAL, 1, -1),
				createStyleRange( 47,  54, BOLD),
				createStyleRange( 54,  96, NORMAL),
				createStyleRange( 96, 102, BOLD),
				createStyleRange(102, 140, NORMAL),
		};

		StyleRange[] expectedNonDefaultRanges= new StyleRange[] {
				createStyleRange(  4,  20, BOLD),
				createStyleRange( 20,  47, NORMAL, 1, -1),
				createStyleRange( 47,  54, BOLD),
				createStyleRange( 96, 102, BOLD),
		};

		checkRegions(expectedAllRanges, expectedNonDefaultRanges);
	}

	/**
	 * Merge range covering existing non-default range.
	 */
	@Test
	public void testMergeStyleRange17() {
		StyleRange range= createStyleRange(47, 54, NORMAL, 1, -1);
		fTextPresentation.mergeStyleRange(range);

		StyleRange[] expectedAllRanges= new StyleRange[] {
				createStyleRange(  0,   4, NORMAL),
				createStyleRange(  4,  20, BOLD),
				createStyleRange( 20,  47, NORMAL),
				createStyleRange( 47,  54, BOLD, 1, -1),
				createStyleRange( 54,  96, NORMAL),
				createStyleRange( 96, 102, BOLD),
				createStyleRange(102, 140, NORMAL),
		};

		StyleRange[] expectedNonDefaultRanges= new StyleRange[] {
				createStyleRange(  4,  20, BOLD),
				createStyleRange( 47,  54, BOLD, 1, -1),
				createStyleRange( 96, 102, BOLD),
		};

		checkRegions(expectedAllRanges, expectedNonDefaultRanges);
	}

	/**
	 * Merge range covering three ranges.
	 */
	@Test
	public void testMergeStyleRange18() {
		StyleRange range= createStyleRange(20, 96, NORMAL, 1, -1);
		fTextPresentation.mergeStyleRange(range);

		StyleRange[] expectedAllRanges= new StyleRange[] {
				createStyleRange(  0,   4, NORMAL),
				createStyleRange(  4,  20, BOLD),
				createStyleRange( 20,  47, NORMAL, 1, -1),
				createStyleRange( 47,  54, BOLD,   1, -1),
				createStyleRange( 54,  96, NORMAL, 1, -1),
				createStyleRange( 96, 102, BOLD),
				createStyleRange(102, 140, NORMAL),
		};

		StyleRange[] expectedNonDefaultRanges= new StyleRange[] {
				createStyleRange(  4,  20, BOLD),
				createStyleRange( 20,  47, NORMAL, 1, -1),
				createStyleRange( 47,  54, BOLD,   1, -1),
				createStyleRange( 54,  96, NORMAL, 1, -1),
				createStyleRange( 96, 102, BOLD),
		};

		checkRegions(expectedAllRanges, expectedNonDefaultRanges);
	}

	/**
	 * Merge range starting and ending outside.
	 */
	@Test
	public void testMergeStyleRange22() {
		StyleRange range= createStyleRange(-5, 145, BOLD, 1, -1);

		Color expectedColor= createColor(1, 1, 1);
		StyleRange[] expectedAllRanges= new StyleRange[fAllRanges.length];
		for (int i= 0; i < fAllRanges.length; i++) {
			expectedAllRanges[i]= (StyleRange) fAllRanges[i].clone();
			expectedAllRanges[i].fontStyle= BOLD;
			expectedAllRanges[i].foreground= expectedColor;
		}
		StyleRange[] expectedNonDefaultRanges= expectedAllRanges;

		fTextPresentation.mergeStyleRange(range);
		checkRegions(expectedAllRanges, expectedNonDefaultRanges);
	}

	/**
	 * Merge range starting and ending outside without default style.
	 */
	@Test
	public void testMergeStyleRange23() {
		fTextPresentation.setDefaultStyleRange(null);
		StyleRange range= createStyleRange(-5, 145, BOLD, 1, -1);

		Color expectedColor= createColor(1, 1, 1);
		StyleRange[] expectedAllRanges= new StyleRange[fAllRanges.length + 2];
		expectedAllRanges[0]= createStyleRange(-5, 0, BOLD, 1, -1);
		for (int i= 0; i < fAllRanges.length; i++) {
			expectedAllRanges[i + 1]= (StyleRange) fAllRanges[i].clone();
			expectedAllRanges[i + 1].fontStyle= BOLD;
			expectedAllRanges[i + 1].foreground= expectedColor;
		}
		expectedAllRanges[expectedAllRanges.length - 1]= createStyleRange(140, 145, BOLD, 1, -1);

		StyleRange[] expectedNonDefaultRanges= expectedAllRanges;

		fTextPresentation.mergeStyleRange(range);
		checkRegions(expectedAllRanges, expectedNonDefaultRanges);
	}

	/**
	 * Merge/replace styles on default only presentation.
	 */
	@Test
	public void testApplyStyleRange1() {
		fTextPresentation.clear();
		fTextPresentation.setDefaultStyleRange(createStyleRange(0, 140, NORMAL));
		for (StyleRange range : deepClone(fAllRanges)) {
			fTextPresentation.mergeStyleRange(range);
		}
		checkRegions(fAllRanges, fNonDefaultRanges);

		fTextPresentation.clear();
		fTextPresentation.setDefaultStyleRange(createStyleRange(0, 140, NORMAL));
		for (StyleRange range : deepClone(fAllRanges)) {
			fTextPresentation.replaceStyleRange(range);
		}
		checkRegions(fAllRanges, fNonDefaultRanges);
	}

	/**
	 * Merge/replace styles without default on empty presentation.
	 */
	@Test
	public void testApplyStyleRange2() {
		fTextPresentation.clear();
		for (StyleRange range : deepClone(fAllRanges)) {
			fTextPresentation.mergeStyleRange(range);
		}
		checkRegions(fAllRanges, fAllRanges);

		fTextPresentation.clear();
		for (StyleRange range : deepClone(fAllRanges)) {
			fTextPresentation.replaceStyleRange(range);
		}
		checkRegions(fAllRanges, fAllRanges);
	}

	/**
	 * Merge/replace styles without default on existing styles.
	 */
	@Test
	public void testApplyStyleRange3() {
		fTextPresentation.clear();
		fTextPresentation.mergeStyleRange(createStyleRange(0, 140, NORMAL));
		for (StyleRange range : deepClone(fAllRanges)) {
			fTextPresentation.mergeStyleRange(range);
		}
		checkRegions(fAllRanges, fAllRanges);

		fTextPresentation.clear();
		fTextPresentation.mergeStyleRange(createStyleRange(0, 140, NORMAL));
		for (StyleRange range : deepClone(fAllRanges)) {
			fTextPresentation.replaceStyleRange(range);
		}
		checkRegions(fAllRanges, fAllRanges);
	}

	/**
	 * Merge/replace empty range.
	 */
	@Test
	public void testApplyStyleRange4() {
		StyleRange empty= createStyleRange(0, 0, BOLD);
		fTextPresentation.mergeStyleRange(empty);
		checkRegions(fAllRanges, fNonDefaultRanges);
		fTextPresentation.replaceStyleRange(empty);
		checkRegions(fAllRanges, fNonDefaultRanges);
	}

	/**
	 * Test merge operation involving link styling.
	 */
	@Test
	public void testLinkMerge() {
		Color linkColor= createColor(0, 0, 255);
		StyleRange[] ranges= new StyleRange[] {
				createStyleRange(  4,  20, NORMAL),        // default underline
				createStyleRange( 20,  47, NORMAL),        // explicit default underline
				createStyleRange( 47,  54, NORMAL),        // double underline
				createStyleRange( 96, 102, NORMAL, 1, -1), // set color for later test
				createStyleRange(102, 140, NORMAL),        // link
		};

		ranges[0].underline= true;

		ranges[1].underline= true;
		ranges[1].underlineStyle= SWT.UNDERLINE_SINGLE;

		ranges[2].underline= true;
		ranges[2].underlineStyle= SWT.UNDERLINE_DOUBLE;

		ranges[4].underline= true;
		ranges[4].underlineStyle= SWT.UNDERLINE_LINK;
		ranges[4].foreground= linkColor;

		for (StyleRange range : deepClone(ranges)) {
			fTextPresentation.mergeStyleRange(range);
		}

		StyleRange[] expectedAllRanges= new StyleRange[] {
				createStyleRange(  0,   4, NORMAL),
				createStyleRange(  4,  20, BOLD),
				createStyleRange( 20,  47, NORMAL),
				createStyleRange( 47,  54, BOLD),
				createStyleRange( 54,  96, NORMAL),
				createStyleRange( 96, 102, BOLD, 1, -1),
				createStyleRange(102, 140, NORMAL),
		};

		StyleRange[] expectedNonDefaultRanges= new StyleRange[] {
				expectedAllRanges[1],
				expectedAllRanges[2],
				expectedAllRanges[3],
				expectedAllRanges[5],
				expectedAllRanges[6],
		};

		expectedNonDefaultRanges[0].underline= true;

		expectedNonDefaultRanges[1].underline= true;
		expectedNonDefaultRanges[1].underlineStyle= SWT.UNDERLINE_SINGLE;

		expectedNonDefaultRanges[2].underline= true;
		expectedNonDefaultRanges[2].underlineStyle= SWT.UNDERLINE_DOUBLE;

		expectedNonDefaultRanges[4].underline= true;
		expectedNonDefaultRanges[4].underlineStyle= SWT.UNDERLINE_LINK;
		expectedNonDefaultRanges[4].foreground= linkColor;

		checkRegions(expectedAllRanges, expectedNonDefaultRanges);


		for (int i= 0; i < 3; i++) {
			ranges[i].underlineStyle= SWT.UNDERLINE_LINK;
			ranges[i].foreground= linkColor;
			expectedNonDefaultRanges[i].underlineStyle= SWT.UNDERLINE_LINK;
			expectedNonDefaultRanges[i].foreground= linkColor;
		}

		ranges[3].underlineStyle= SWT.UNDERLINE_LINK;
		ranges[3].foreground= null;
		expectedNonDefaultRanges[3].underlineStyle= SWT.UNDERLINE_LINK;
		expectedNonDefaultRanges[3].foreground= null; // merging underline_link force set the foreground

		ranges[4].underlineStyle= SWT.UNDERLINE_SQUIGGLE;
		// expect no change for expectedNonDefaultRanges[4]. underline_link cannot removed using merge

		for (StyleRange range : ranges) {
			fTextPresentation.mergeStyleRange(range);
		}
		checkRegions(expectedAllRanges, expectedNonDefaultRanges);
	}

	/**
	 * Test merge operation with all style parts.
	 */
	@Test
	public void testMergeAll() {
		StyleRange range= createStyleRange(4, 20, BOLD, 1, 2);
		range.borderColor= createColor(3, 3, 3);
		range.borderStyle= SWT.BORDER_DASH;
		range.font= fDisplay.getSystemFont();
		range.metrics= new GlyphMetrics(0, 0, 10);
		range.strikeout= true;
		range.strikeoutColor= createColor(4, 4, 4);
		range.underline= true;
		range.underlineColor= createColor(5, 5, 5);
		range.underlineStyle= SWT.UNDERLINE_SQUIGGLE;
		fTextPresentation.mergeStyleRange((StyleRange) range.clone());

		StyleRange[] expectedAllRanges= new StyleRange[] {
				createStyleRange(  0,   4, NORMAL),
				(StyleRange) range.clone(),
				createStyleRange( 20,  47, NORMAL),
				createStyleRange( 47,  54, BOLD),
				createStyleRange( 54,  96, NORMAL),
				createStyleRange( 96, 102, BOLD),
				createStyleRange(102, 140, NORMAL),
		};

		StyleRange[] expectedNonDefaultRanges= new StyleRange[] {
				(StyleRange) range.clone(),
				createStyleRange( 47,  54, BOLD),
				createStyleRange( 96, 102, BOLD),
		};

		checkRegions(expectedAllRanges, expectedNonDefaultRanges);

		range.background= null;
		range.borderColor= null;
		range.borderStyle= SWT.NONE;
		range.font= null;
		range.fontStyle= SWT.NORMAL;
		range.foreground= null;
		range.metrics= null;
		range.strikeout= false;
		range.strikeoutColor= null;
		range.underline= false;
		range.underlineColor= null;
		range.underlineStyle= SWT.UNDERLINE_SINGLE;
		fTextPresentation.mergeStyleRange((StyleRange) range.clone());

		checkRegions(expectedAllRanges, expectedNonDefaultRanges);

		fTextPresentation.replaceStyleRange((StyleRange) range.clone());

		expectedAllRanges[1]= range;
		expectedNonDefaultRanges= new StyleRange[] {
				createStyleRange( 47,  54, BOLD),
				createStyleRange( 96, 102, BOLD),
		};

		checkRegions(expectedAllRanges, expectedNonDefaultRanges);
	}

	@Test
	public void testMergeStyleRanges1() {
		StyleRange[] ranges= new StyleRange[] {
				createStyleRange(  0,   2, NORMAL, 1, -1), // at start of first existing
				createStyleRange( 20,  22, NORMAL, 1, -1), // at start of existing default
				createStyleRange( 24,  26, NORMAL, 1, -1), // within existing default
				createStyleRange( 45,  47, NORMAL, 1, -1), // at end of existing default
				createStyleRange( 47,  49, NORMAL, 1, -1), // at start of existing non-default
				createStyleRange( 50,  51, NORMAL, 1, -1), // within existing non-default
				createStyleRange( 52,  54, NORMAL, 1, -1), // at end of existing non-default
				createStyleRange(138, 140, NORMAL, 1, -1), // at end of last existing
		};
		fTextPresentation.mergeStyleRanges(deepClone(ranges));

		StyleRange[] expectedAllRanges= new StyleRange[] {
				createStyleRange(  0,   2, NORMAL, 1, -1),
				createStyleRange(  2,   4, NORMAL),
				createStyleRange(  4,  20, BOLD),
				createStyleRange( 20,  22, NORMAL, 1, -1),
				createStyleRange( 22,  24, NORMAL),
				createStyleRange( 24,  26, NORMAL, 1, -1),
				createStyleRange( 26,  45, NORMAL),
				createStyleRange( 45,  47, NORMAL, 1, -1),
				createStyleRange( 47,  49, BOLD,   1, -1),
				createStyleRange( 49,  50, BOLD),
				createStyleRange( 50,  51, BOLD,   1, -1),
				createStyleRange( 51,  52, BOLD),
				createStyleRange( 52,  54, BOLD,   1, -1),
				createStyleRange( 54,  96, NORMAL),
				createStyleRange( 96, 102, BOLD),
				createStyleRange(102, 138, NORMAL),
				createStyleRange(138, 140, NORMAL, 1, -1),
		};

		StyleRange[] expectedNonDefaultRanges= new StyleRange[] {
				createStyleRange(  0,   2, NORMAL, 1, -1),
				createStyleRange(  4,  20, BOLD),
				createStyleRange( 20,  22, NORMAL, 1, -1),
				createStyleRange( 24,  26, NORMAL, 1, -1),
				createStyleRange( 45,  47, NORMAL, 1, -1),
				createStyleRange( 47,  49, BOLD,   1, -1),
				createStyleRange( 49,  50, BOLD),
				createStyleRange( 50,  51, BOLD,   1, -1),
				createStyleRange( 51,  52, BOLD),
				createStyleRange( 52,  54, BOLD,   1, -1),
				createStyleRange( 96, 102, BOLD),
				createStyleRange(138, 140, NORMAL, 1, -1),
		};

		checkRegions(expectedAllRanges, expectedNonDefaultRanges);

		// now swap fore- and background colors for an easy additional test
		for (StyleRange r : ranges) {
			Color c= r.foreground;
			r.foreground= r.background;
			r.background= c;
		}
		for (StyleRange[] rs : new StyleRange[][] { expectedAllRanges, expectedNonDefaultRanges }) {
			for (StyleRange r : rs) {
				r.background= r.foreground;
			}
		}
		fTextPresentation.mergeStyleRanges(ranges);
		checkRegions(expectedAllRanges, expectedNonDefaultRanges);
	}

	@Test
	public void testReplaceStyleRanges1() {
		StyleRange[] ranges= new StyleRange[] {
				createStyleRange(  0,   2, NORMAL, 1, -1), // at start of first existing
				createStyleRange( 20,  22, NORMAL, 1, -1), // at start of existing default
				createStyleRange( 24,  26, NORMAL, 1, -1), // within existing default
				createStyleRange( 45,  47, NORMAL, 1, -1), // at end of existing default
				createStyleRange( 47,  49, NORMAL, 1, -1), // at start of existing non-default
				createStyleRange( 50,  51, NORMAL, 1, -1), // within existing non-default
				createStyleRange( 52,  54, NORMAL, 1, -1), // at end of existing non-default
				createStyleRange(138, 140, NORMAL, 1, -1), // at end of last existing
		};
		fTextPresentation.replaceStyleRanges(deepClone(ranges));

		StyleRange[] expectedAllRanges= new StyleRange[] {
				createStyleRange(  0,   2, NORMAL, 1, -1),
				createStyleRange(  2,   4, NORMAL),
				createStyleRange(  4,  20, BOLD),
				createStyleRange( 20,  22, NORMAL, 1, -1),
				createStyleRange( 22,  24, NORMAL),
				createStyleRange( 24,  26, NORMAL, 1, -1),
				createStyleRange( 26,  45, NORMAL),
				createStyleRange( 45,  47, NORMAL, 1, -1),
				createStyleRange( 47,  49, NORMAL, 1, -1),
				createStyleRange( 49,  50, BOLD),
				createStyleRange( 50,  51, NORMAL, 1, -1),
				createStyleRange( 51,  52, BOLD),
				createStyleRange( 52,  54, NORMAL, 1, -1),
				createStyleRange( 54,  96, NORMAL),
				createStyleRange( 96, 102, BOLD),
				createStyleRange(102, 138, NORMAL),
				createStyleRange(138, 140, NORMAL, 1, -1),
		};

		StyleRange[] expectedNonDefaultRanges= new StyleRange[] {
				createStyleRange(  0,   2, NORMAL, 1, -1),
				createStyleRange(  4,  20, BOLD),
				createStyleRange( 20,  22, NORMAL, 1, -1),
				createStyleRange( 24,  26, NORMAL, 1, -1),
				createStyleRange( 45,  47, NORMAL, 1, -1),
				createStyleRange( 47,  49, NORMAL, 1, -1),
				createStyleRange( 49,  50, BOLD),
				createStyleRange( 50,  51, NORMAL, 1, -1),
				createStyleRange( 51,  52, BOLD),
				createStyleRange( 52,  54, NORMAL, 1, -1),
				createStyleRange( 96, 102, BOLD),
				createStyleRange(138, 140, NORMAL, 1, -1),
		};

		checkRegions(expectedAllRanges, expectedNonDefaultRanges);

		// now swap all fore- and background colors for an easy additional test
		for (StyleRange[] rs : new StyleRange[][] { ranges, expectedAllRanges, expectedNonDefaultRanges }) {
			for (StyleRange r : rs) {
				Color c= r.foreground;
				r.foreground= r.background;
				r.background= c;
			}
		}
		fTextPresentation.replaceStyleRanges(ranges);
		checkRegions(expectedAllRanges, expectedNonDefaultRanges);
	}

	/**
	 * Replace whole presentation.
	 */
	@Test
	public void testReplaceStyleRange2() {
		StyleRange range= createStyleRange(0, 140, BOLD, 1, -1);
		fTextPresentation.replaceStyleRange(range);

		Color expectedColor= createColor(1, 1, 1);
		StyleRange[] expectedAllRanges= new StyleRange[fAllRanges.length];
		for (int i= 0; i < fAllRanges.length; i++) {
			expectedAllRanges[i]= (StyleRange) fAllRanges[i].clone();
			expectedAllRanges[i].fontStyle= BOLD;
			expectedAllRanges[i].foreground= expectedColor;
		}

		StyleRange[] expectedNonDefaultRanges= expectedAllRanges;

		checkRegions(expectedAllRanges, expectedNonDefaultRanges);
	}

	@Test
	public void testExtend() {
		fTextPresentation.setDefaultStyleRange(null);
		assertEquals(new Region(0, 140), fTextPresentation.getExtent());
		fTextPresentation.setDefaultStyleRange(createStyleRange(0, 150, NORMAL));
		assertEquals(new Region(0, 150), fTextPresentation.getExtent());
		fTextPresentation.setDefaultStyleRange(null);
		fTextPresentation.setResultWindow(new Region(100, 10));
		assertEquals(new Region(0, 10), fTextPresentation.getExtent());
		fTextPresentation.setDefaultStyleRange(createStyleRange(0, 150, NORMAL));
		assertEquals(new Region(0, 10), fTextPresentation.getExtent());

		fTextPresentation= new TextPresentation(new Region(0, 160), 10);
		assertEquals(new Region(0, 160), fTextPresentation.getExtent());
		fTextPresentation.setDefaultStyleRange(createStyleRange(0, 150, NORMAL));
		assertEquals(new Region(0, 160), fTextPresentation.getExtent());
		fTextPresentation.setDefaultStyleRange(null);
		fTextPresentation.setResultWindow(new Region(100, 10));
		assertEquals(new Region(0, 10), fTextPresentation.getExtent());
		fTextPresentation.setDefaultStyleRange(createStyleRange(0, 150, NORMAL));
		assertEquals(new Region(0, 10), fTextPresentation.getExtent());
	}

	@Test
	public void testExtendUndefined() {
		fTextPresentation= new TextPresentation(new Region(0, 160), 10);
		fTextPresentation.setResultWindow(new Region(-10, 5));
		assertEquals(new Region(10, -5), fTextPresentation.getExtent());
		fTextPresentation.setResultWindow(new Region(-10, 30));
		assertEquals(new Region(10, 20), fTextPresentation.getExtent());
		fTextPresentation.setResultWindow(new Region(150, 30));
		assertEquals(new Region(0, 10), fTextPresentation.getExtent());
		fTextPresentation.setResultWindow(new Region(200, 30));
		assertEquals(new Region(0, -40), fTextPresentation.getExtent());
	}

	@Test
	public void testEmptyPresentation() {
		fTextPresentation.clear();
		checkRegions(new StyleRange[0], new StyleRange[0]);
		fTextPresentation.setResultWindow(new Region(4, 10));
		checkRegions(new StyleRange[0], new StyleRange[0]);
		fTextPresentation.setResultWindow(null);

		StyleRange defaultRange= createStyleRange(0, 50, BOLD);
		fTextPresentation.setDefaultStyleRange(defaultRange);
		checkRegions(new StyleRange[0], new StyleRange[0]);
		fTextPresentation.setResultWindow(new Region(4, 10));
		checkRegions(new StyleRange[0], new StyleRange[0]);

		fTextPresentation= new TextPresentation(new Region(0, 100), 1);
		assertEquals(new Region(0, 100), fTextPresentation.getExtent());
		fTextPresentation.setResultWindow(new Region(4, 10));
		assertEquals(new Region(0, 10), fTextPresentation.getExtent());
	}

	@Test
	public void testApplyTextPresentation() {
		Shell shell= new Shell(fDisplay);
		try {
			StyledText text= new StyledText(shell, SWT.NONE);
			text.setText(String.join("", Collections.nCopies(fTextPresentation.getCoverage().getLength(), ".")));
			TextPresentation.applyTextPresentation(fTextPresentation, text);
			assertArrayEquals(fAllRanges, text.getStyleRanges());
		} finally {
			shell.dispose();
		}
	}

	@Test
	public void testIterator() {
		// Test read over iterator end
		Iterator<StyleRange> e= fTextPresentation.getAllStyleRangeIterator();
		try {
			for (int i= 0; i < 1000; i++) {
				e.next();
			}
			fail("Iterator has no end.");
		} catch (NoSuchElementException ex) {
			// expected
		}
		e= fTextPresentation.getNonDefaultStyleRangeIterator();
		try {
			for (int i= 0; i < 1000; i++) {
				e.next();
			}
			fail("Iterator has no end.");
		} catch (NoSuchElementException ex) {
			// expected
		}
	}

	// helper method required as long as TextPresentation methods manipulate given arguments
	private static StyleRange[] deepClone(StyleRange[] original) {
		StyleRange[] clone= new StyleRange[original.length];
		for (int i= 0; i < original.length; i++) {
			clone[i]= (StyleRange) original[i].clone();
		}
		return clone;
	}
}
