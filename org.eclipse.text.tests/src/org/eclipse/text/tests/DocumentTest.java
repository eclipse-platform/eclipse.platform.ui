/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.BadPositionCategoryException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;


public class DocumentTest {

	private Document fDocument;

	protected void checkPositions(Position[] expected) {

		try {

			Position[] actual= fDocument.getPositions(IDocument.DEFAULT_CATEGORY);
			checkPositions(expected, actual);

		} catch (BadPositionCategoryException x) {
			assertTrue("BadPositionCategoryException thrown", false);
		}

	}

	protected void checkPositions(Position[] expected, Position[] actual) {

		assertTrue("invalid number of positions", expected.length == actual.length);

		for (int i= 0; i < expected.length; i++) {
			assertEquals("Position " + i + " wrong:", expected[i], actual[i]);
		}

	}

	@Before
	public void setUp() {

		fDocument= new Document();

		String text=                          // 0
		"package TestPackage;\n" +            // 21
		"/*\n" +                              // 24
		"* comment\n" +                       // 34
		"*/\n" +                              // 37
		"	public class Class {\n" +         // 59 
		"		// comment1\n" +              // 73
		"		public void method1() {\n" +  // 99
		"		}\n" +                        //103
		"		// comment2\n" +              //117
		"		public void method2() {\n" +  //143
		"		}\n" +                        //147
		"	}\n";                             //150

		fDocument.set(text);

		try {

			fDocument.addPosition(new Position( 0,   20)); // "package TestPackage;"
			fDocument.addPosition(new Position( 21,  15)); // "/*\n* comment\n*/"
			fDocument.addPosition(new Position( 38, 111)); // "public class Class {\n ... }"
			fDocument.addPosition(new Position( 61,  12)); // "// comment1\n"
			fDocument.addPosition(new Position( 75,  27)); // "public void method1() {\n		}"
			fDocument.addPosition(new Position(105,  12)); // "// comment2\n"
			fDocument.addPosition(new Position(119,  27)); // "public void method2() {\n		}"

		} catch (BadLocationException x) {
			assertTrue("initilization failed", false);
		}
	}

	@After
	public void tearDown () {
		fDocument= null;
	}
	
	@Test
	public void testEmptyDocument() {
		IDocument document= new Document();
		int lines= document.getNumberOfLines();
		assertEquals(1, lines);
	}
	
	@Test
	public void testDelete1() {

		try {

			fDocument.replace(21, 16, "");

		} catch (BadLocationException x) {
			assertTrue("BadLocationException thrown", false);
		}

		Position[] positions= new Position[] {
			new Position( 0,   20),
			new Position( 21,  0),
			new Position( 22, 111),
			new Position( 45,  12),
			new Position( 59,  27),
			new Position( 89,  12),
			new Position(103,  27)
		};

		checkPositions(positions);
	}

	@Test
	public void testEditScript1() {

		//	1. step

		try {

			fDocument.replace(0, fDocument.getLength(), "");

		} catch (BadLocationException x) {
			assertTrue("BadLocationException thrown", false);
		}

		Position[] positions= new Position[] {
			new Position( 0, 0)
		};

		checkPositions(positions);


		//	2. step
		try {

			fDocument.replace(0, 0, "\t");

		} catch (BadLocationException x) {
			assertTrue("BadLocationException thrown", false);
		}

		positions= new Position[] {
			new Position( 1, 0)
		};

		checkPositions(positions);

	}

	@Test
	public void testFindPositions() {

		try {

			fDocument.addPosition(new Position( 21,  13));
			fDocument.addPosition(new Position(  0,  19));
			fDocument.addPosition(new Position( 21,  14));
			fDocument.addPosition(new Position( 21,  16));
			fDocument.addPosition(new Position(  0,   0));
			fDocument.addPosition(new Position( 104,  1));
			fDocument.addPosition(new Position( 120,  1));
			fDocument.addPosition(new Position( 119,  1));

		} catch (BadLocationException x) {
			assertTrue("initilization failed", false);
		}


		Position[] positions= new Position[] {
			new Position( 0,    0),
			new Position( 0,   19),
			new Position( 0,   20),
			new Position( 21,  16),
			new Position( 21,  14),
			new Position( 21,  13),
			new Position( 21,  15),
			new Position( 38, 111),
			new Position( 61,  12),
			new Position( 75,  27),
			new Position(104,   1),
			new Position(105,  12),
			new Position(119,   1),
			new Position(119,  27),
			new Position(120,   1)
		};

		checkPositions(positions);

	}

	@Test
	public void testInsert1() {

		try {

			fDocument.replace(0, 0, "//comment\n");

		} catch (BadLocationException x) {
			assertTrue("BadLocationException thrown", false);
		}

		Position[] positions= new Position[] {
			new Position( 10,   20),
			new Position( 31,  15),
			new Position( 48, 111),
			new Position( 71,  12),
			new Position( 85,  27),
			new Position(115,  12),
			new Position(129,  27)
		};

		checkPositions(positions);
	}

	@Test
	public void testInsert2() {

		try {

			fDocument.replace(61, 0, "//comment\n");

		} catch (BadLocationException x) {
			assertTrue("BadLocationException thrown", false);
		}

		Position[] positions= new Position[] {
			new Position( 0,   20),
			new Position( 21,  15),
			new Position( 38, 121),
			new Position( 71,  12),
			new Position( 85,  27),
			new Position(115,  12),
			new Position(129,  27)
		};

		checkPositions(positions);
	}
	
	@Test
	public void testInsert3() {

		try {

			fDocument.replace(101, 0, "//comment\n");

		} catch (BadLocationException x) {
			assertTrue("BadLocationException thrown", false);
		}

		Position[] positions= new Position[] {
			new Position( 0,   20),
			new Position( 21,  15),
			new Position( 38, 121),
			new Position( 61,  12),
			new Position( 75,  37),
			new Position(115,  12),
			new Position(129,  27)
		};

		checkPositions(positions);
	}
	
	@Test
	public void testInsert4() {

		try {

			fDocument.replace(20, 0, "// comment");

		} catch (BadLocationException x) {
			assertTrue("BadLocationException thrown", false);
		}

		System.out.print(fDocument.get());

		Position[] positions= new Position[] {
			new Position( 0,   20),
			new Position( 31,  15),
			new Position( 48, 111),
			new Position( 71,  12),
			new Position( 85,  27),
			new Position(115,  12),
			new Position(129,  27)
		};

		checkPositions(positions);
	}
	
	@Test
	public void testReplace1() {

		try {

			fDocument.replace(8, 11, "pkg1");

		} catch (BadLocationException x) {
			assertTrue("BadLocationException thrown", false);
		}

		Position[] positions= new Position[] {
			new Position( 0,   13),
			new Position( 14,  15),
			new Position( 31, 111),
			new Position( 54,  12),
			new Position( 68,  27),
			new Position( 98,  12),
			new Position(112,  27)
		};

		checkPositions(positions);
	}
	
	@Test
	public void testReplace2() {

		try {

			fDocument.replace(21, 16, "//comment\n");

		} catch (BadLocationException x) {
			assertTrue("BadLocationException thrown", false);
		}

		Position[] positions= new Position[] {
			new Position( 0,   20),
			new Position( 31,   0),
			new Position( 32, 111),
			new Position( 55,  12),
			new Position( 69,  27),
			new Position( 99,  12),
			new Position(113,  27)
		};

		checkPositions(positions);
	}
	
	@Test
	public void testReplace3() {

		Position[] actual= new Position[] {
			new Position(0, 150),
		};

		try {

			fDocument.addPosition(actual[0]);
			fDocument.replace(0, 150, "xxxxxxxxxx");

		} catch (BadLocationException x) {
			assertTrue("BadLocationException thrown", false);
		}

		Position[] expected= new Position[] {
			new Position(0, 10)
		};

		checkPositions(expected, actual);
	}
	
	@Test
	public void testReplace4() {

		try {

			fDocument.replace(19, 1, "xxxxx;");

		} catch (BadLocationException x) {
			assertTrue("BadLocationException thrown", false);
		}

		Position[] positions= new Position[] {
			new Position( 0,   25),
			new Position( 26,  15),
			new Position( 43, 111),
			new Position( 66,  12),
			new Position( 80,  27),
			new Position(110,  12),
			new Position(124,  27)
		};

		checkPositions(positions);
	}
	
	@Test
	public void testAppend() {

		Position[] actual= new Position[] {
			new Position(0, 2),
		};

		try {

			fDocument.replace(0, 150, "");
			fDocument.replace(fDocument.getLength(), 0, "xx");
			fDocument.addPosition(actual[0]);
			fDocument.replace(fDocument.getLength(), 0, "xxxxxxxx");

		} catch (BadLocationException x) {
			assertTrue("BadLocationException thrown", false);
		}

		Position[] expected= new Position[] {
			new Position(0, 2)
		};

		checkPositions(expected, actual);
	}

	@Test
	public void testShiftLeft() {

		try {

			fDocument.replace(73, 1, "");
			fDocument.replace(98, 1, "");

		} catch (BadLocationException x) {
			assertTrue("BadLocationException thrown", false);
		}

		Position[] positions= new Position[] {
			new Position(  0,  20),
			new Position( 21,  15),
			new Position( 38, 109),
			new Position( 61,  12),
			new Position( 74,  26),
			new Position(103,  12),
			new Position(117,  27)
		};

		checkPositions(positions);
	}
	
	@Test
	public void testShiftRight() {

		try {

			fDocument.replace( 73, 0, "\t");
			fDocument.replace(100, 0, "\t");

		} catch (BadLocationException x) {
			assertTrue("BadLocationException thrown", false);
		}

		Position[] positions= new Position[] {
			new Position(  0,  20),
			new Position( 21,  15),
			new Position( 38, 113),
			new Position( 61,  12),
			new Position( 76,  28),
			new Position(107,  12),
			new Position(121,  27)
		};

		checkPositions(positions);
	}
}
