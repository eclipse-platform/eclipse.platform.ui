/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.text.tests;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class GapTextTest extends AbstractGapTextTest {
	/**
	 * @deprecated tests the legacy constructor of GapTextStore
	 */
	@Deprecated
	@Before
	public void setUp() {
		fText= new GapText(5, 10);
		fText.set("xxxxx");
	}

	@After
	public void tearDown () {
		fText= null;
	}
	
	@Test
	public void testSet() {
		assertGap(5, 10);
	}
	
	@Test
	public void testGetText1() {
		String[] expected= {
			"xyxxxx",
			"xyxyxxx",
			"xyxyxyxx",
			"xyxyxyxyx",
			"xyxyxyxyxy"
		};

		for (int i= 1; i < 5; i++) {
			fText.replace(2 * i - 1, 0, "y");
			assertContents(expected[i - 1]);
		}

	}
	
	@Test
	public void testGetText2() {
		String[] expected= {
			"yxxxxx",
			"yxyxxxx",
			"yxyxyxxx",
			"yxyxyxyxx",
			"yxyxyxyxyx"
		};

		for (int i= 1; i < 5; i++) {
			fText.replace(2 * (i - 1), 0, "y");
			assertContents(expected[i - 1]);
		}

	}
	
	@Test
	public void testInsert() {
		fText.replace(2, 0, "y");
		assertGap(3, 7);


		for (int i= 1; i <= 4; i++) {
			fText.replace(2 + i, 0, "y");
			assertGap(3 + i, 7);
		}

		fText.replace(7, 0, "y");
		assertGap(8, 13);
	}
	
	@Test
	public void testRemoveGapOverlapping() {
		fText.replace(2, 2, null);
		assertGap(2, 9);

		fText.replace(1, 2, null);
		assertGap(1, 10);
	}
	
	@Test
	public void testRemoveGapOverlapping2() {
		fText.replace(0, 0, "aaaaazzzzzyyyyy");
		assertGap(15, 20);
		assertContents("aaaaazzzzzyyyyyxxxxx");


		fText.replace(5, 12, null);
		assertGap(5, 10);
		assertContents("aaaaaxxx");
	}
	
	@Test
	public void testRemoveRemoteFromGap() {
		fText.replace(0, 0, "aaaaazzzzzyyyyy");
		assertGap(15, 20);
		assertContents("aaaaazzzzzyyyyyxxxxx");

		// before gap
		fText.replace(5, 2, null);
		assertGap(5, 12);
		assertContents("aaaaazzzyyyyyxxxxx");

		// after gap
		fText.replace(7, 10, null);
		assertGap(7, 12);
		assertContents("aaaaazzx");

	}
	
	@Test
	public void testRemoveAtLeftGapEdge() {
		fText.replace(4, 0, "xxx");
		assertGap(7, 9);
		fText.replace(6, 0, "x");
		assertGap(7, 8);
		fText.replace(6, 1, null);
		assertGap(6, 8);
	}
	
	@Test
	public void testRemoveAtRightGapEdge() {
		fText.replace(4, 0, "xxx");
		assertGap(7, 9);
		fText.replace(6, 0, "x");
		assertGap(7, 8);
		fText.replace(7, 1, null);
		assertGap(7, 9);
	}
	
	@Test
	public void testReplace() {
		fText.replace(2, 2, "yy");
		assertGap(4, 9);

		fText.replace(2, 1, "yyyyyyyyyyyy");
		assertGap(14, 19);

		fText.replace(14, 0, "yyy");
		assertGap(17, 19);
	}
	
	@Test
	public void testRemoveReallocateBeforeGap() throws Exception {
	    fText.replace(0, 0, "yyyyyzzzzz");
	    assertGap(10, 15);
	    assertContents("yyyyyzzzzzxxxxx");

	    fText.replace(2, 6, null);
	    assertGap(2, 7);
	    assertContents("yyzzxxxxx");
    }
}
