/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.text.tests;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.jface.text.GapTextStore;


public class GapTextTest extends TestCase {

	static class GapText extends GapTextStore {
		
		public GapText() {
			super(5, 10);
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
		
	private GapText fText;
	
	
	public GapTextTest(String name) {
		super(name);
	}
	
	protected String printGap() {
		return "[" + fText.getGapStart() + "," + fText.getGapEnd() + "]";
	}
	
	protected void setUp() {
	
		fText= new GapText();
		fText.set("xxxxx");
	}
	
	public static Test suite() {
		return new TestSuite(GapTextTest.class); 
	}
	
	protected void tearDown () {
		fText= null;
	}
		
	public void testGetText1() {
	
		assertTrue("invalid gap", fText.getGapStart() == -1 && fText.getGapEnd() == -1);
	
		String[] expected= {
			"xyxxxx",
			"xyxyxxx",
			"xyxyxyxx",
			"xyxyxyxyx",
			"xyxyxyxyxy"
		};
		
		for (int i= 1; i < 5; i++) {
			fText.replace(2 * i - 1, 0, "y");
			String txt= fText.get(0, fText.getLength());
			assertEquals("invalid text \'" + txt + "\' returned, should be \'" + expected[i - 1] + "\'", expected[i - 1],  txt);
		}
	
	}
	
	public void testGetText2() {
	
		assertTrue("invalid gap", fText.getGapStart() == -1 && fText.getGapEnd() == -1);
	
		String[] expected= {
			"yxxxxx",
			"yxyxxxx",
			"yxyxyxxx",
			"yxyxyxyxx",
			"yxyxyxyxyx"
		};
		
		for (int i= 1; i < 5; i++) {
			fText.replace(2 * (i - 1), 0, "y");
			String txt= fText.get(0, fText.getLength());
			assertEquals("invalid text \'" + txt + "\' returned, should be \'" + expected[i - 1] + "\'", expected[i - 1], txt);
		}
	
	}
	
	public void testInsert() {
	
		assertTrue("invalid gap", fText.getGapStart() == -1 && fText.getGapEnd() == -1);
		fText.replace(2, 0, "y");
		assertTrue("invalid gap:" + printGap(), fText.getGapStart() == 3 && fText.getGapEnd() == 13);
	
		
		for (int i= 1; i <= 5; i++) {
			fText.replace(2 + i, 0, "y");
			assertTrue("invalid gap:" + printGap(), fText.getGapStart() == (3 + i) && fText.getGapEnd() == 13);
		}
	
		fText.replace(8, 0, "y");
		assertTrue("invalid gap:" + printGap(), fText.getGapStart() == 9 && fText.getGapEnd() == 19);
	}
	
	public void testRemoveGapOverlapping() {
	
		assertTrue("invalid gap", fText.getGapStart() == -1 && fText.getGapEnd() == -1);
		fText.replace(2, 2, null);
		assertTrue("invalid gap: " + printGap(), fText.getGapStart() == 2 && fText.getGapEnd() == 12);
	
		fText.replace(1, 2, null);
		assertTrue("invalid gap: " + printGap(), fText.getGapStart() == 1 && fText.getGapEnd() == 13);
	}
	
	public void testRemoveGapOverlapping2() {
	
		assertTrue("invalid gap", fText.getGapStart() == -1 && fText.getGapEnd() == -1);
		fText.replace(0, 0, "aaaaazzzzzyyyyy");
		assertTrue("invalid gap:" + printGap(), fText.getGapStart() == 15 && fText.getGapEnd() == 25);
		assertEquals("aaaaazzzzzyyyyyxxxxx", fText.get(0, fText.getLength()));
	
	
		fText.replace(5, 12, null);
		assertTrue("invalid gap:" + printGap(), fText.getGapStart() == 5 && fText.getGapEnd() == 27);
		assertEquals("aaaaaxxx", fText.get(0, fText.getLength()));
	}
	
	public void testRemoveRemoteFromGap() {
	
		assertTrue("invalid gap", fText.getGapStart() == -1 && fText.getGapEnd() == -1);
		fText.replace(0, 0, "aaaaazzzzzyyyyy");
		assertTrue("invalid gap:" + printGap(), fText.getGapStart() == 15 && fText.getGapEnd() == 25);
		assertEquals("aaaaazzzzzyyyyyxxxxx", fText.get(0, fText.getLength()));
	
		// before gap
		fText.replace(5, 2, null);
		assertTrue("invalid gap:" + printGap(), fText.getGapStart() == 5 && fText.getGapEnd() == 15);
		assertEquals("aaaaazzzyyyyyxxxxx", fText.get(0, fText.getLength()));
		
		// after gap
		fText.replace(7, 10, null);
		assertTrue("invalid gap:" + printGap(), fText.getGapStart() == 7 && fText.getGapEnd() == 17);
		assertEquals("aaaaazzx", fText.get(0, fText.getLength()));
	
	}
	
	public void testRemoveAtLeftGapEdge() {
		assertTrue("invalid gap", fText.getGapStart() == -1 && fText.getGapEnd() == -1);
		fText.replace(2, 0, "xxxxx");
		assertTrue("invalid gap: " + printGap(), fText.getGapStart() == 7 && fText.getGapEnd() == 17);
		fText.replace(6, 1, null);
		assertTrue("invalid gap: " + printGap(), fText.getGapStart() == 6 && fText.getGapEnd() == 17);
	}
	
	public void testRemoveAtRightGapEdge() {
		assertTrue("invalid gap", fText.getGapStart() == -1 && fText.getGapEnd() == -1);
		fText.replace(2, 0, "xxxxx");
		assertTrue("invalid gap: " + printGap(), fText.getGapStart() == 7 && fText.getGapEnd() == 17);
		fText.replace(7, 1, null);
		assertTrue("invalid gap: " + printGap(), fText.getGapStart() == 7 && fText.getGapEnd() == 18);
	}
	
	public void testReplace() {
	
		assertTrue("invalid gap", fText.getGapStart() == -1 && fText.getGapEnd() == -1);
		fText.replace(2, 2, "yy");
		assertTrue("invalid gap:" + printGap(), fText.getGapStart() == 4 && fText.getGapEnd() == 14);
	
		fText.replace(4, 1, "yyyyyyyyyy");
		assertTrue("invalid gap:" + printGap(), fText.getGapStart() == 14 && fText.getGapEnd() == 24);
	
		fText.replace(14, 0, "yyy");
		assertTrue("invalid gap:" + printGap(), fText.getGapStart() == 17 && fText.getGapEnd() == 24);
	}
}
