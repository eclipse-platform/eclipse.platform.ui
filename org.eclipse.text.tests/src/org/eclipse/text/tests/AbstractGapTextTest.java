/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.text.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.eclipse.jface.text.GapTextStore;

/**
 * A test specifically testing the gap property of a gap text store.
 *
 * @since 3.3
 */
public class AbstractGapTextTest {
	protected static class GapText extends GapTextStore {

		/**
		 * Creates a new empty text store using the specified low and high watermarks.
		 *
		 * @param lowWatermark unused - at the lower bound, the array is only resized when the
		 *            content does not fit
		 * @param highWatermark if the gap is ever larger than this, it will automatically be
		 *            shrunken (&gt;=&nbsp;0)
		 * @deprecated use {@link GapTextStore#GapTextStore(int, int, float)} instead
		 */
		@Deprecated
		public GapText(int lowWatermark, int highWatermark) {
			super(lowWatermark, highWatermark);
		}

		public GapText(int min, int max, float maxGapFactor) {
			super(min, max, maxGapFactor);
		}

		String getText() {
			return super.getContentAsString();
		}

		int getGapStart() {
			return super.getGapStartIndex();
		}

		int getGapEnd() {
			return super.getGapEndIndex();
		}

		int getRawLength() {
			return super.getContentAsString().length();
		}
	}

	protected GapText fText;

	private String printGap() {
		return printGap(fText.getGapStart(), fText.getGapEnd());
	}

	private String printGap(int start, int end) {
		return "[" + start + "," + end + "]";
	}

	protected void assertGap(int start, int end) {
		assertTrue("Invalid gap. Expected: " + printGap(start, end) + " actual:" + printGap() , fText.getGapStart() == start && fText.getGapEnd() == end);
	}

	protected void assertContents(String expected) {
		assertEquals(expected, fText.get(0, fText.getLength()));
	}
}
