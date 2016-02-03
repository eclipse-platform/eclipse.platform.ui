/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.text.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;

import org.junit.Before;
import org.junit.Test;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;

import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextPresentation;

public class TextPresentationTest {

	private static final int NORMAL= SWT.NORMAL;
	private static final int BOLD= SWT.BOLD;
//	private static final int ITALIC= SWT.ITALIC;

	private TextPresentation fTextPresentation;
	private StyleRange[] fAllRanges;
	private StyleRange[] fNonDefaultRanges;

	@Before
	public void setUp() {
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
		for (int i= 0; i < fAllRanges.length; i++)
			fTextPresentation.addStyleRange(fAllRanges[i]);
	}

	private StyleRange createStyleRange(int start, int end, int style) {
		return createStyleRange(start, end, null, null, style);
	}

	private StyleRange createStyleRange(int start, int end, Color foreground, Color background, int style) {
		return new StyleRange(start, end - start, foreground, background, style);
	}

	private StyleRange createStyleRange(int start, int end, int foreground, int background, int style) {
		return createStyleRange(start, end, createColor(foreground, foreground, foreground), createColor(background, background, background), style);
	}

	private Display fDisplay= Display.getDefault();

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
		return new Color(fDisplay, red, green, blue);
	}

	private void checkRegions(StyleRange[] expectedAllRanges, StyleRange[] expectedNonDefaultRanges) {
		Iterator<StyleRange> e= fTextPresentation.getAllStyleRangeIterator();
		for (int i= 0; i < expectedAllRanges.length; i++) {
			assertTrue(e.hasNext());
			assertEquals(expectedAllRanges[i], e.next());
		}
		assertTrue(!e.hasNext());

		e= fTextPresentation.getNonDefaultStyleRangeIterator();
		for (int i= 0; i < expectedNonDefaultRanges.length; i++) {
			assertTrue(e.hasNext());
			assertEquals(expectedNonDefaultRanges[i], e.next());
		}
		assertTrue(!e.hasNext());
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
		StyleRange range= createStyleRange(0, 2, 1, -1, NORMAL);
		fTextPresentation.mergeStyleRange(range);

		StyleRange[] expectedAllRanges= new StyleRange[] {
				createStyleRange(0, 2, 1, -1, NORMAL),
				createStyleRange(  2,   4, NORMAL),
				createStyleRange(  4,  20, BOLD),
				createStyleRange( 20,  47, NORMAL),
				createStyleRange( 47,  54, BOLD),
				createStyleRange( 54,  96, NORMAL),
				createStyleRange( 96, 102, BOLD),
				createStyleRange(102, 140, NORMAL),
		};

		StyleRange[] expectedNonDefaultRanges= new StyleRange[] {
				createStyleRange(0, 2, 1, -1, NORMAL),
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
		StyleRange range= createStyleRange(138, 140, 1, -1, NORMAL);
		fTextPresentation.mergeStyleRange(range);

		StyleRange[] expectedAllRanges= new StyleRange[] {
				createStyleRange(  0,   4, NORMAL),
				createStyleRange(  4,  20, BOLD),
				createStyleRange( 20,  47, NORMAL),
				createStyleRange( 47,  54, BOLD),
				createStyleRange( 54,  96, NORMAL),
				createStyleRange( 96, 102, BOLD),
				createStyleRange(102, 138, NORMAL),
				createStyleRange(138, 140, 1, -1, NORMAL),
		};

		StyleRange[] expectedNonDefaultRanges= new StyleRange[] {
				createStyleRange(  4,  20, BOLD),
				createStyleRange( 47,  54, BOLD),
				createStyleRange( 96, 102, BOLD),
				createStyleRange(138, 140, 1, -1, NORMAL),
		};

		checkRegions(expectedAllRanges, expectedNonDefaultRanges);
	}

	/**
	 * Merge range at start of existing default range.
	 */
	@Test
	public void testMergeStyleRange3() {
		StyleRange range= createStyleRange(20, 22, 1, -1, NORMAL);
		fTextPresentation.mergeStyleRange(range);

		StyleRange[] expectedAllRanges= new StyleRange[] {
				createStyleRange(  0,   4, NORMAL),
				createStyleRange(  4,  20, BOLD),
				createStyleRange(20, 22, 1, -1, NORMAL),
				createStyleRange( 22,  47, NORMAL),
				createStyleRange( 47,  54, BOLD),
				createStyleRange( 54,  96, NORMAL),
				createStyleRange( 96, 102, BOLD),
				createStyleRange(102, 140, NORMAL),
		};

		StyleRange[] expectedNonDefaultRanges= new StyleRange[] {
				createStyleRange(  4,  20, BOLD),
				createStyleRange(20, 22, 1, -1, NORMAL),
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
		StyleRange range= createStyleRange(22, 24, 1, -1, NORMAL);
		fTextPresentation.mergeStyleRange(range);

		StyleRange[] expectedAllRanges= new StyleRange[] {
				createStyleRange(  0,   4, NORMAL),
				createStyleRange(  4,  20, BOLD),
				createStyleRange( 20,  22, NORMAL),
				createStyleRange(22, 24, 1, -1, NORMAL),
				createStyleRange( 24,  47, NORMAL),
				createStyleRange( 47,  54, BOLD),
				createStyleRange( 54,  96, NORMAL),
				createStyleRange( 96, 102, BOLD),
				createStyleRange(102, 140, NORMAL),
		};

		StyleRange[] expectedNonDefaultRanges= new StyleRange[] {
				createStyleRange(  4,  20, BOLD),
				createStyleRange(22, 24, 1, -1, NORMAL),
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
		StyleRange range= createStyleRange(45, 47, 1, -1, NORMAL);
		fTextPresentation.mergeStyleRange(range);

		StyleRange[] expectedAllRanges= new StyleRange[] {
				createStyleRange(  0,   4, NORMAL),
				createStyleRange(  4,  20, BOLD),
				createStyleRange( 20,  45, NORMAL),
				createStyleRange(45, 47, 1, -1, NORMAL),
				createStyleRange( 47,  54, BOLD),
				createStyleRange( 54,  96, NORMAL),
				createStyleRange( 96, 102, BOLD),
				createStyleRange(102, 140, NORMAL),
		};

		StyleRange[] expectedNonDefaultRanges= new StyleRange[] {
				createStyleRange(  4,  20, BOLD),
				createStyleRange(45, 47, 1, -1, NORMAL),
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
		StyleRange range= createStyleRange(47, 49, 1, -1, NORMAL);
		fTextPresentation.mergeStyleRange(range);

		StyleRange[] expectedAllRanges= new StyleRange[] {
				createStyleRange(  0,   4, NORMAL),
				createStyleRange(  4,  20, BOLD),
				createStyleRange( 20,  47, NORMAL),
				createStyleRange(47, 49, 1, -1, BOLD),
				createStyleRange( 49,  54, BOLD),
				createStyleRange( 54,  96, NORMAL),
				createStyleRange( 96, 102, BOLD),
				createStyleRange(102, 140, NORMAL),
		};

		StyleRange[] expectedNonDefaultRanges= new StyleRange[] {
				createStyleRange(  4,  20, BOLD),
				createStyleRange(47, 49, 1, -1, BOLD),
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
		StyleRange range= createStyleRange(49, 51, 1, -1, NORMAL);
		fTextPresentation.mergeStyleRange(range);

		StyleRange[] expectedAllRanges= new StyleRange[] {
				createStyleRange(  0,   4, NORMAL),
				createStyleRange(  4,  20, BOLD),
				createStyleRange( 20,  47, NORMAL),
				createStyleRange( 47,  49, BOLD),
				createStyleRange(49, 51, 1, -1, BOLD),
				createStyleRange( 51,  54, BOLD),
				createStyleRange( 54,  96, NORMAL),
				createStyleRange( 96, 102, BOLD),
				createStyleRange(102, 140, NORMAL),
		};

		StyleRange[] expectedNonDefaultRanges= new StyleRange[] {
				createStyleRange(  4,  20, BOLD),
				createStyleRange( 47,  49, BOLD),
				createStyleRange(49, 51, 1, -1, BOLD),
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
		StyleRange range= createStyleRange(52, 54, 1, -1, NORMAL);
		fTextPresentation.mergeStyleRange(range);

		StyleRange[] expectedAllRanges= new StyleRange[] {
				createStyleRange(  0,   4, NORMAL),
				createStyleRange(  4,  20, BOLD),
				createStyleRange( 20,  47, NORMAL),
				createStyleRange( 47,  52, BOLD),
				createStyleRange(52, 54, 1, -1, BOLD),
				createStyleRange( 54,  96, NORMAL),
				createStyleRange( 96, 102, BOLD),
				createStyleRange(102, 140, NORMAL),
		};

		StyleRange[] expectedNonDefaultRanges= new StyleRange[] {
				createStyleRange(  4,  20, BOLD),
				createStyleRange( 47,  52, BOLD),
				createStyleRange(52, 54, 1, -1, BOLD),
				createStyleRange( 96, 102, BOLD),
		};

		checkRegions(expectedAllRanges, expectedNonDefaultRanges);
	}

	/**
	 * Merge range from existing default to non-default range.
	 */
	@Test
	public void testMergeStyleRange9() {
		StyleRange range= createStyleRange(45, 49, 1, -1, NORMAL);
		fTextPresentation.mergeStyleRange(range);

		StyleRange[] expectedAllRanges= new StyleRange[] {
				createStyleRange(  0,   4, NORMAL),
				createStyleRange(  4,  20, BOLD),
				createStyleRange( 20,  45, NORMAL),
				createStyleRange(45, 47, 1, -1, NORMAL),
				createStyleRange(47, 49, 1, -1, BOLD),
				createStyleRange( 49,  54, BOLD),
				createStyleRange( 54,  96, NORMAL),
				createStyleRange( 96, 102, BOLD),
				createStyleRange(102, 140, NORMAL),
		};

		StyleRange[] expectedNonDefaultRanges= new StyleRange[] {
				createStyleRange(  4,  20, BOLD),
				createStyleRange(45, 47, 1, -1, NORMAL),
				createStyleRange(47, 49, 1, -1, BOLD),
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
		StyleRange range= createStyleRange(52, 56, 1, -1, NORMAL);
		fTextPresentation.mergeStyleRange(range);

		StyleRange[] expectedAllRanges= new StyleRange[] {
				createStyleRange(  0,   4, NORMAL),
				createStyleRange(  4,  20, BOLD),
				createStyleRange( 20,  47, NORMAL),
				createStyleRange( 47,  52, BOLD),
				createStyleRange(52, 54, 1, -1, BOLD),
				createStyleRange(54, 56, 1, -1, NORMAL),
				createStyleRange( 56,  96, NORMAL),
				createStyleRange( 96, 102, BOLD),
				createStyleRange(102, 140, NORMAL),
		};

		StyleRange[] expectedNonDefaultRanges= new StyleRange[] {
				createStyleRange(  4,  20, BOLD),
				createStyleRange( 47,  52, BOLD),
				createStyleRange(52, 54, 1, -1, BOLD),
				createStyleRange(54, 56, 1, -1, NORMAL),
				createStyleRange( 96, 102, BOLD),
		};

		checkRegions(expectedAllRanges, expectedNonDefaultRanges);
	}

	/**
	 * Merge range from existing default over non-default to default range.
	 */
	@Test
	public void testMergeStyleRange11() {
		StyleRange range= createStyleRange(45, 56, 1, -1, NORMAL);
		fTextPresentation.mergeStyleRange(range);

		StyleRange[] expectedAllRanges= new StyleRange[] {
				createStyleRange(  0,   4, NORMAL),
				createStyleRange(  4,  20, BOLD),
				createStyleRange( 20,  45, NORMAL),
				createStyleRange(45, 47, 1, -1, NORMAL),
				createStyleRange(47, 54, 1, -1, BOLD),
				createStyleRange(54, 56, 1, -1, NORMAL),
				createStyleRange( 56,  96, NORMAL),
				createStyleRange( 96, 102, BOLD),
				createStyleRange(102, 140, NORMAL),
		};

		StyleRange[] expectedNonDefaultRanges= new StyleRange[] {
				createStyleRange(  4,  20, BOLD),
				createStyleRange(45, 47, 1, -1, NORMAL),
				createStyleRange(47, 54, 1, -1, BOLD),
				createStyleRange(54, 56, 1, -1, NORMAL),
				createStyleRange( 96, 102, BOLD),
		};

		checkRegions(expectedAllRanges, expectedNonDefaultRanges);
	}

	/**
	 * Merge range from existing non-default over default to non-default range.
	 */
	@Test
	public void testMergeStyleRange12() {
		StyleRange range= createStyleRange(52, 98, 1, -1, NORMAL);
		fTextPresentation.mergeStyleRange(range);

		StyleRange[] expectedAllRanges= new StyleRange[] {
				createStyleRange(  0,   4, NORMAL),
				createStyleRange(  4,  20, BOLD),
				createStyleRange( 20,  47, NORMAL),
				createStyleRange( 47,  52, BOLD),
				createStyleRange(52, 54, 1, -1, BOLD),
				createStyleRange(54, 96, 1, -1, NORMAL),
				createStyleRange(96, 98, 1, -1, BOLD),
				createStyleRange( 98, 102, BOLD),
				createStyleRange(102, 140, NORMAL),
		};

		StyleRange[] expectedNonDefaultRanges= new StyleRange[] {
				createStyleRange(  4,  20, BOLD),
				createStyleRange( 47,  52, BOLD),
				createStyleRange(52, 54, 1, -1, BOLD),
				createStyleRange(54, 96, 1, -1, NORMAL),
				createStyleRange(96, 98, 1, -1, BOLD),
				createStyleRange( 98, 102, BOLD),
		};

		checkRegions(expectedAllRanges, expectedNonDefaultRanges);
	}

	/**
	 * Merge range over existing default range.
	 */
	@Test
	public void testMergeStyleRange13() {
		StyleRange range= createStyleRange(20, 47, 1, -1, NORMAL);
		fTextPresentation.mergeStyleRange(range);

		StyleRange[] expectedAllRanges= new StyleRange[] {
				createStyleRange(  0,   4, NORMAL),
				createStyleRange(  4,  20, BOLD),
				createStyleRange(20, 47, 1, -1, NORMAL),
				createStyleRange( 47,  54, BOLD),
				createStyleRange( 54,  96, NORMAL),
				createStyleRange( 96, 102, BOLD),
				createStyleRange(102, 140, NORMAL),
		};

		StyleRange[] expectedNonDefaultRanges= new StyleRange[] {
				createStyleRange(  4,  20, BOLD),
				createStyleRange(20, 47, 1, -1, NORMAL),
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
		StyleRange range= createStyleRange(47, 54, 1, -1, NORMAL);
		fTextPresentation.mergeStyleRange(range);

		StyleRange[] expectedAllRanges= new StyleRange[] {
				createStyleRange(  0,   4, NORMAL),
				createStyleRange(  4,  20, BOLD),
				createStyleRange( 20,  47, NORMAL),
				createStyleRange(47, 54, 1, -1, BOLD),
				createStyleRange( 54,  96, NORMAL),
				createStyleRange( 96, 102, BOLD),
				createStyleRange(102, 140, NORMAL),
		};

		StyleRange[] expectedNonDefaultRanges= new StyleRange[] {
				createStyleRange(  4,  20, BOLD),
				createStyleRange(47, 54, 1, -1, BOLD),
				createStyleRange( 96, 102, BOLD),
		};

		checkRegions(expectedAllRanges, expectedNonDefaultRanges);
	}

	@Test
	public void testMergeStyleRanges1() {
		StyleRange[] ranges= new StyleRange[] {
				createStyleRange(0, 2, 1, -1, NORMAL), // at start of first existing
				createStyleRange(20, 22, 1, -1, NORMAL), // at start of existing default
				createStyleRange(24, 26, 1, -1, NORMAL), // within existing default
				createStyleRange(45, 47, 1, -1, NORMAL), // at end of existing default
				createStyleRange(47, 49, 1, -1, NORMAL), // at start of existing non-default
				createStyleRange(50, 51, 1, -1, NORMAL), // within existing non-default
				createStyleRange(52, 54, 1, -1, NORMAL), // at end of existing non-default
				createStyleRange(138, 140, 1, -1, NORMAL), // at end of last existing
		};
		fTextPresentation.mergeStyleRanges(ranges);

		StyleRange[] expectedAllRanges= new StyleRange[] {
				createStyleRange(0, 2, 1, -1, NORMAL),
				createStyleRange(  2,   4, NORMAL),
				createStyleRange(  4,  20, BOLD),
				createStyleRange(20, 22, 1, -1, NORMAL),
				createStyleRange( 22,  24, NORMAL),
				createStyleRange(24, 26, 1, -1, NORMAL),
				createStyleRange( 26,  45, NORMAL),
				createStyleRange(45, 47, 1, -1, NORMAL),
				createStyleRange(47, 49, 1, -1, BOLD),
				createStyleRange( 49,  50, BOLD),
				createStyleRange(50, 51, 1, -1, BOLD),
				createStyleRange( 51,  52, BOLD),
				createStyleRange(52, 54, 1, -1, BOLD),
				createStyleRange( 54,  96, NORMAL),
				createStyleRange( 96, 102, BOLD),
				createStyleRange(102, 138, NORMAL),
				createStyleRange(138, 140, 1, -1, NORMAL),
		};

		StyleRange[] expectedNonDefaultRanges= new StyleRange[] {
				createStyleRange(0, 2, 1, -1, NORMAL),
				createStyleRange(  4,  20, BOLD),
				createStyleRange(20, 22, 1, -1, NORMAL),
				createStyleRange(24, 26, 1, -1, NORMAL),
				createStyleRange(45, 47, 1, -1, NORMAL),
				createStyleRange(47, 49, 1, -1, BOLD),
				createStyleRange( 49,  50, BOLD),
				createStyleRange(50, 51, 1, -1, BOLD),
				createStyleRange( 51,  52, BOLD),
				createStyleRange(52, 54, 1, -1, BOLD),
				createStyleRange( 96, 102, BOLD),
				createStyleRange(138, 140, 1, -1, NORMAL),
		};

		checkRegions(expectedAllRanges, expectedNonDefaultRanges);
	}

}
