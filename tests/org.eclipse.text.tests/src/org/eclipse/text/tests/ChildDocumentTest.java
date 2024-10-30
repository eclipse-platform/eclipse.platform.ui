/*******************************************************************************
 * Copyright (c) 2000, 2024 IBM Corporation and others.
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
package org.eclipse.text.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.BadPositionCategoryException;
import org.eclipse.jface.text.DefaultLineTracker;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.projection.ChildDocument;
import org.eclipse.jface.text.projection.ChildDocumentManager;

public class ChildDocumentTest {

	private IDocument fDocument;
	private Document fParent;
	private ChildDocumentManager fManager;


	protected void checkPositions(Position[] positions) throws BadPositionCategoryException {

		Position[] v = fDocument.getPositions(IDocument.DEFAULT_CATEGORY);
		assertTrue("invalid number of positions", v.length == positions.length);

		for (int i = 0; i < positions.length; i++) {
			assertEquals(print(v[i]) + " != " + print(positions[i]), positions[i], v[i]);
		}

	}

	protected void checkPositions(Position[] expected, Position[] actual) {

		assertTrue("invalid number of positions", expected.length == actual.length);

		for (int i= 0; i < expected.length; i++) {
			assertEquals(print(actual[i]) + " != " + print(expected[i]), expected[i], actual[i]);
		}

	}

	protected String print(Position p) {
		return "[" + p.getOffset() + "," + p.getLength() + "]";
	}

	protected void checkLineInformationConsistency() throws BadLocationException {
		DefaultLineTracker textTracker= new DefaultLineTracker();
		textTracker.set(fDocument.get());

		int textLines= textTracker.getNumberOfLines();
		int trackerLines= fDocument.getNumberOfLines();

		assertEquals("Child document store and child line tracker are inconsistent", trackerLines, textLines);

		for (int i = 0; i < trackerLines; i++) {
			IRegion trackerLine = fDocument.getLineInformation(i);
			IRegion textLine = textTracker.getLineInformation(i);
			assertEquals("Child document store and child line tracker are inconsistent", trackerLine.getOffset(),
					textLine.getOffset());
			assertEquals("Child document store and child line tracker are inconsistent", trackerLine.getLength(),
					textLine.getLength());
		}
	}

	@Before
	public void setUp() throws BadLocationException {

		fParent= new Document();

		String text=
		"package TestPackage;\n" +
		"/*\n" +
		"* comment\n" +
		"*/\n" +
		"	public class Class {\n" +
		"		// comment1\n" +
		"		public void method1() {\n" +
		"		}\n" +
		"		// comment2\n" +
		"		public void method2() {\n" +
		"		}\n" +
		"	}\n";

		fParent.set(text);
		fManager = new ChildDocumentManager();
		fDocument = fManager.createSlaveDocument(fParent);
		if (fDocument instanceof ChildDocument child) {
			child.setParentDocumentRange(0, fParent.getLength());
		}

		fDocument.addPosition(new Position(0, 20));
		fDocument.addPosition(new Position(21, 15));
		fDocument.addPosition(new Position(38, 111));
		fDocument.addPosition(new Position(61, 12));
		fDocument.addPosition(new Position(75, 27));
		fDocument.addPosition(new Position(105, 12));
		fDocument.addPosition(new Position(119, 27));

	}

	@After
	public void tearDown () {
		fDocument= null;
	}

	@Test
	public void testDelete1() throws BadLocationException, BadPositionCategoryException {

		fDocument.replace(21, 16, "");

		Position[] positions = {
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
	public void testEditScript1() throws BadPositionCategoryException, BadLocationException {

		//	1. step
		fDocument.replace(0, fDocument.getLength(), "");

		Position[] positions = { new Position(0, 0) };

		checkPositions(positions);

		//	2. step
		fDocument.replace(0, 0, "\t");

		positions = new Position[] {
			new Position( 1, 0)
		};

		checkPositions(positions);
	}

	@Test
	public void testFindPositions() throws BadLocationException, BadPositionCategoryException {

		fDocument.addPosition(new Position(21, 13));
		fDocument.addPosition(new Position(0, 19));
		fDocument.addPosition(new Position(21, 14));
		fDocument.addPosition(new Position(21, 16));
		fDocument.addPosition(new Position(0, 0));
		fDocument.addPosition(new Position(104, 1));
		fDocument.addPosition(new Position(120, 1));
		fDocument.addPosition(new Position(119, 1));

		Position[] positions = {
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
	public void testInsert1() throws BadLocationException, BadPositionCategoryException {

		fDocument.replace(0, 0, "//comment\n");

		Position[] positions = {
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
	public void testInsert2() throws BadLocationException, BadPositionCategoryException {

		fDocument.replace(61, 0, "//comment\n");

		Position[] positions = {
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
	public void testInsert3() throws BadLocationException, BadPositionCategoryException {

		fDocument.replace(101, 0, "//comment\n");

		Position[] positions = {
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
	public void testInsert4() throws BadPositionCategoryException, BadLocationException {

		fDocument.replace(20, 0, "// comment");

		Position[] positions = {
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
	public void testReplace1() throws BadLocationException, BadPositionCategoryException {

		fDocument.replace(8, 11, "pkg1");

		Position[] positions = {
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
	public void testReplace2() throws BadLocationException, BadPositionCategoryException {
		fDocument.replace(21, 16, "//comment\n");

		Position[] positions = {
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
	public void testReplace3() throws BadLocationException {

		Position[] actual = { new Position(0, 150), };

		fDocument.addPosition(actual[0]);
		fDocument.replace(0, 150, "xxxxxxxxxx");

		Position[] expected = { new Position(0, 10) };

		checkPositions(expected, actual);
	}

	/*
	 * Replace in the parent document at the end offset of the child document
	 *
	 * [formatting] IllegalArgumentException when formatting comment code snippet in segmented mode
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=51594
	 */
	@Test
	public void testReplace4() throws BadLocationException {
		int start = fParent.getLineOffset(5);
		int end = fParent.getLineOffset(8);
		ChildDocument child = (ChildDocument) fDocument;
		child.setParentDocumentRange(start, end - start);
		fParent.replace(end, 1, "x");
		checkLineInformationConsistency();
	}

	@Test
	public void testAppend() throws BadLocationException {

		Position[] actual = { new Position(0, 2), };

		fDocument.replace(0, 150, "");
		fDocument.replace(fDocument.getLength(), 0, "xx");
		fDocument.addPosition(actual[0]);
		fDocument.replace(fDocument.getLength(), 0, "xxxxxxxx");

		Position[] expected = { new Position(0, 2) };

		checkPositions(expected, actual);
	}

	@Test
	public void testShiftLeft() throws BadLocationException, BadPositionCategoryException {

		fDocument.replace(73, 1, "");
		fDocument.replace(98, 1, "");


		Position[] positions = {
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
	public void testShiftRight() throws BadLocationException, BadPositionCategoryException {
		fDocument.replace(73, 0, "\t");
		fDocument.replace(100, 0, "\t");

		Position[] positions = {
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
