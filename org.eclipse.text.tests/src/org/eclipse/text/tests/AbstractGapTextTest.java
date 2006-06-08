/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.text.tests;

import junit.framework.TestCase;

import org.eclipse.jface.text.GapTextStore;

/**
 * A test specifically testing the gap property of a gap text store.
 * 
 * @since 3.3
 */
public class AbstractGapTextTest extends TestCase {
	protected static class GapText extends GapTextStore {
		/**
		 * @deprecated
		 */
		public GapText(int lowWaterMark, int highWaterMark) {
			super(lowWaterMark, highWaterMark);
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
