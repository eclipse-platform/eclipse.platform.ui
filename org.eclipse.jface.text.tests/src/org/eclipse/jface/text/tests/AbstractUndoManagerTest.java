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
package org.eclipse.jface.text.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.IUndoManager;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.TextViewer;


/**
 * Tests for DefaultUndoManager.
 */
public abstract class AbstractUndoManagerTest {

	/** The maximum undo level. */
	private static final int MAX_UNDO_LEVEL= 256;

	/** The shell. */
	private Shell fShell;
	/** The text viewer. */
	private ITextViewer fTextViewer;
	/** The undo manager. */
	private IUndoManager fUndoManager;

	private static final int LOOP_COUNT= 20;

	//--- Static data sets for comparing scenarios - obtained from capturing random data ---
	/** Original document */
	private static final String INITIAL_DOCUMENT_CONTENT= "+7cyg:/F!T4KnW;0+au$t1G%(`Z|u'7'_!-k?<c\"2Y.]CwsO.r";
	/** Replacement string */
	private static final String [] REPLACEMENTS= { ">", "F", "M/r-*", "-", "bl", "", "}%/#", "", "k&", "f", "\\g", "c!x", "TLG-", "NPO", "Rp9u", "", "X", "W(", ")z", "oe", "", "h*", "t", "I", "X=N>", "2yt", "&Z", "2)W=", ":K", "P9S", "s8t8o", "", "", "5{7", "%", "", "v3", "Wz", "sH", "3c", "8", "ol", ",6$", "94[#", ".~", "n", ">", "9", "W", ",(FW", "Q", "^", "Bq", "$", "re", "", "9", "8[", "Mx", "4b", "$6", "F", "8s]", "o", "-", "E&6", "S\\", "/", "z.a", "4ai", "b", ")", "", "l", "VU", "7M+Ql", "xZ?x", "xx", "lc", "b", "A", "!", "4pSU", "", "{J", "H", "l>_", "n&9", "", "&`", ";igQxq", "", ">", ";\"", "k\\`]G", "o{?", "", "K", "_6", "="};
	/** Position/offset pairs */
	private static final int [] POSITIONS= { 18, 2, 43, 1, 3, 2, 28, 3, 35, 1, 23, 5, 32, 2, 30, 1, 22, 1, 37, 0, 23, 3, 43, 2, 46, 1, 17, 1, 36, 6, 17, 5, 30, 4, 25, 1, 2, 2, 30, 0, 37, 3, 28, 1, 30, 2, 20, 5, 33, 1, 29, 1, 15, 2, 21, 2, 24, 4, 38, 3, 8, 0, 33, 2, 15, 2, 25, 0, 8, 2, 20, 3, 43, 2, 44, 1, 44, 2, 32, 2, 40, 2, 32, 3, 12, 2, 38, 3, 33, 2, 46, 0, 13, 3, 45, 0, 16, 2, 3, 2, 44, 0, 48, 0, 18, 5, 7, 6, 7, 3, 40, 0, 9, 1, 16, 3, 28, 3, 36, 1, 35, 2, 0, 3, 6, 1, 10, 4, 14, 2, 15, 3, 33, 1, 36, 0, 37, 0, 4, 3, 31, 3, 33, 3, 11, 3, 20, 2, 25, 3, 4, 3, 7, 3, 17, 0, 3, 1, 31, 3, 34, 1, 21, 0, 33, 1, 17, 4, 9, 1, 26, 3, 2, 3, 12, 1, 26, 3, 9, 5, 5, 0, 31, 3, 0, 3, 12, 1, 1, 1, 3, 0, 39, 0, 9, 2, 2, 0, 28, 2};

	private static final boolean DEBUG= false;


	@Before
	public void setUp() {
		fShell= new Shell();
		fUndoManager= createUndoManager(MAX_UNDO_LEVEL);
		fTextViewer= new TextViewer(fShell, SWT.NONE);
		fTextViewer.setUndoManager(fUndoManager);
		fUndoManager.connect(fTextViewer);
	}

	abstract protected IUndoManager createUndoManager(int maxUndoLevel);

	@After
	public void tearDown() {
		fUndoManager.disconnect();
		fUndoManager= null;
		fShell.dispose();
		fShell= null;
		fTextViewer= null;
	}

	/**
	 * Test for line delimiter conversion.
	 */
	@Test
	public void testConvertLineDelimiters() {
		final String original= "a\r\nb\r\n";
		final IDocument document= new Document(original);
		fTextViewer.setDocument(document);

		try {
			document.replace(1, 2, "\n");
			document.replace(3, 2, "\n");
		} catch (BadLocationException e) {
			assertTrue(false);
		}

		assertTrue(fUndoManager.undoable());
		fUndoManager.undo();
		assertTrue(fUndoManager.undoable());
		fUndoManager.undo();

		final String reverted= document.get();

		assertEquals(original, reverted);
	}

	/**
	 * Randomly applies document changes.
	 */
	@Test
	public void testRandomAccess() {
		final int RANDOM_STRING_LENGTH= 50;
		final int RANDOM_REPLACE_COUNT= 100;

		assertTrue(RANDOM_REPLACE_COUNT >= 1);
		assertTrue(RANDOM_REPLACE_COUNT <= MAX_UNDO_LEVEL);

		String original= createRandomString(RANDOM_STRING_LENGTH);
		final IDocument document= new Document(original);
		fTextViewer.setDocument(document);

		doChange(document, RANDOM_REPLACE_COUNT);

		assertTrue(fUndoManager.undoable());
		while (fUndoManager.undoable())
			fUndoManager.undo();

		final String reverted= document.get();
		assertEquals(original, reverted);
	}

	private void doChange(IDocument document, int count) {
		try {
			String before= document.get();

			if (DEBUG)
				System.out.println(before);

			Position [] positions= new Position[count];
			String [] strings= new String[count];
			for (int i= 0; i < count; i++) {
				final Position position= createRandomPositionPoisson(document.getLength());
				final String string= createRandomStringPoisson();
				document.replace(position.getOffset(), position.getLength(), string);
				positions[i]= position;
				strings[i]= string;
			}

			if (DEBUG) {
				System.out.print("{ ");
				for (int i=0; i<count; i++) {
					System.out.print(positions[i].getOffset());
					System.out.print(", ");
					System.out.print(positions[i].getLength());
					System.out.print(", ");
				}
				System.out.println(" }");
				System.out.print("{ ");
				for (int i=0; i<count; i++) {
					System.out.print("\"");
					System.out.print(strings[i]);
					System.out.print("\", ");
				}
				System.out.println(" }");
			}
		} catch (BadLocationException e) {
			assertTrue(false);
		}
	}

	// repeatable test case for comparing success/failure among different tests
	private void doRepeatableChange(IDocument document) {
		assertTrue(POSITIONS.length >= (2 * REPLACEMENTS.length));
		try {
			for (int i= 0; i < REPLACEMENTS.length; i++) {
				int offset= POSITIONS[i*2];
				int length= POSITIONS[i*2+1];
				if (document.getLength() > offset + length)
					document.replace(offset, length, REPLACEMENTS[i]);
				else
					document.replace(0,0, REPLACEMENTS[i]);
			}
		} catch (BadLocationException e) {
			assertTrue(false);
		}
	}

	@Test
	public void testLoopRandomAccessAsCompound() {
		int i= 0;
		while (i < LOOP_COUNT) {
			fUndoManager.reset();
			testRandomAccessAsCompound();
			i++;
		}
	}

	@Test
	public void testLoopRandomAccess() {
		int i= 0;
		while (i < LOOP_COUNT) {
			fUndoManager.reset();
			testRandomAccess();
			i++;
		}
	}

	@Test
	public void testLoopRandomAccessAsUnclosedCompound() {
		int i= 0;
		while (i < LOOP_COUNT) {
			fUndoManager.reset();
			testRandomAccessAsUnclosedCompound();
			i++;
		}
	}

	@Test
	public void testLoopConvertLineDelimiters() {
		int i= 0;
		while (i < LOOP_COUNT) {
			fUndoManager.reset();
			testConvertLineDelimiters();
			i++;
		}
	}

	@Test
	public void testLoopRandomAccessWithMixedCompound() {
		int i= 0;
		while (i < LOOP_COUNT) {
			fUndoManager.reset();
			testRandomAccessWithMixedCompound();
			i++;
		}
	}

	@Test
	public void testRandomAccessAsCompound() {
		final int RANDOM_STRING_LENGTH= 50;
		final int RANDOM_REPLACE_COUNT= 100;

		assertTrue(RANDOM_REPLACE_COUNT >= 1);
		assertTrue(RANDOM_REPLACE_COUNT <= MAX_UNDO_LEVEL);

		String original= createRandomString(RANDOM_STRING_LENGTH);
		final IDocument document= new Document(original);
		fTextViewer.setDocument(document);

		fUndoManager.beginCompoundChange();
		doChange(document, RANDOM_REPLACE_COUNT);
		fUndoManager.endCompoundChange();

		assertTrue(fUndoManager.undoable());
		while (fUndoManager.undoable())
			fUndoManager.undo();
		assertTrue(!fUndoManager.undoable());

		final String reverted= document.get();

		assertEquals(original, reverted);
	}

	/**
	 * Test case for https://bugs.eclipse.org/bugs/show_bug.cgi?id=88172
	 */
	@Test
	public void testRandomAccessAsUnclosedCompound() {

		final int RANDOM_STRING_LENGTH= 50;
		final int RANDOM_REPLACE_COUNT= 100;

		assertTrue(RANDOM_REPLACE_COUNT >= 1);
		assertTrue(RANDOM_REPLACE_COUNT <= MAX_UNDO_LEVEL);

		String original= createRandomString(RANDOM_STRING_LENGTH);
		final IDocument document= new Document(original);
		fTextViewer.setDocument(document);

		fUndoManager.beginCompoundChange();
		doChange(document, RANDOM_REPLACE_COUNT);
		// do not close the compound.
		// fUndoManager.endCompoundChange();

		assertTrue(fUndoManager.undoable());
		while (fUndoManager.undoable())
			fUndoManager.undo();
		assertTrue(!fUndoManager.undoable());

		final String reverted= document.get();

		assertEquals(original, reverted);
	}

	@Test
	public void testRandomAccessWithMixedCompound() {

		final int RANDOM_STRING_LENGTH= 50;
		final int RANDOM_REPLACE_COUNT= 10;
		final int NUMBER_COMPOUNDS= 5;
		final int NUMBER_ATOMIC_PER_COMPOUND= 3;

		assertTrue(RANDOM_REPLACE_COUNT >= 1);
		assertTrue(NUMBER_COMPOUNDS * (1 + NUMBER_ATOMIC_PER_COMPOUND) * RANDOM_REPLACE_COUNT <= MAX_UNDO_LEVEL);

		String original= createRandomString(RANDOM_STRING_LENGTH);
		final IDocument document= new Document(original);
		fTextViewer.setDocument(document);

		for (int i= 0; i < NUMBER_COMPOUNDS; i++) {
			fUndoManager.beginCompoundChange();
			doChange(document, RANDOM_REPLACE_COUNT);
			fUndoManager.endCompoundChange();
			assertTrue(fUndoManager.undoable());
			for (int j= 0; j < NUMBER_ATOMIC_PER_COMPOUND; j++) {
				doChange(document, RANDOM_REPLACE_COUNT);
				assertTrue(fUndoManager.undoable());
			}
		}

		assertTrue(fUndoManager.undoable());
		while (fUndoManager.undoable())
			fUndoManager.undo();
		assertTrue(!fUndoManager.undoable());

		final String reverted= document.get();

		assertEquals(original, reverted);
	}

	@Test
	public void testRepeatableAccess() {
		assertTrue(REPLACEMENTS.length <= MAX_UNDO_LEVEL);

		final IDocument document= new Document(INITIAL_DOCUMENT_CONTENT);
		fTextViewer.setDocument(document);

		doRepeatableChange(document);

		assertTrue(fUndoManager.undoable());
		while (fUndoManager.undoable())
			fUndoManager.undo();

		final String reverted= document.get();

		assertEquals(INITIAL_DOCUMENT_CONTENT, reverted);
	}

	@Test
	public void testRepeatableAccessAsCompound() {
		assertTrue(REPLACEMENTS.length <= MAX_UNDO_LEVEL);

		final IDocument document= new Document(INITIAL_DOCUMENT_CONTENT);
		fTextViewer.setDocument(document);

		fUndoManager.beginCompoundChange();
		doRepeatableChange(document);
		fUndoManager.endCompoundChange();

		assertTrue(fUndoManager.undoable());
		fUndoManager.undo();
		// with a single compound, there should be only one undo
		assertFalse(fUndoManager.undoable());

		final String reverted= document.get();

		assertEquals(INITIAL_DOCUMENT_CONTENT, reverted);
	}

	@Test
	public void testRepeatableAccessAsUnclosedCompound() {
		assertTrue(REPLACEMENTS.length <= MAX_UNDO_LEVEL);

		final IDocument document= new Document(INITIAL_DOCUMENT_CONTENT);
		fTextViewer.setDocument(document);

		fUndoManager.beginCompoundChange();
		doRepeatableChange(document);

		assertTrue(fUndoManager.undoable());
		while (fUndoManager.undoable())
			fUndoManager.undo();

		final String reverted= document.get();

		assertEquals(INITIAL_DOCUMENT_CONTENT, reverted);
	}

	@Test
	public void testRepeatableAccessWithMixedAndEmptyCompound() {
		assertTrue(REPLACEMENTS.length + 2 <= MAX_UNDO_LEVEL);

		final IDocument document= new Document(INITIAL_DOCUMENT_CONTENT);
		fTextViewer.setDocument(document);

		fUndoManager.beginCompoundChange();
		doRepeatableChange(document);
		fUndoManager.endCompoundChange();
		assertTrue(fUndoManager.undoable());

		// insert an empty compound
		fUndoManager.beginCompoundChange();
		fUndoManager.endCompoundChange();

	    // insert the atomic changes
		doRepeatableChange(document);

		assertTrue(fUndoManager.undoable());
		while (fUndoManager.undoable())
			fUndoManager.undo();
		assertTrue(!fUndoManager.undoable());

		final String reverted= document.get();

		assertEquals(INITIAL_DOCUMENT_CONTENT, reverted);
	}

	@Test
	public void testDocumentStamp() {
		final Document document= new Document(INITIAL_DOCUMENT_CONTENT);
		fTextViewer.setDocument(document);
		long stamp= document.getModificationStamp();
		doChange(document, 1);
		fUndoManager.undo();
		assertEquals(stamp, document.getModificationStamp());

	}

	// See https://bugs.eclipse.org/bugs/show_bug.cgi?id=109104
	@Test
	public void testDocumentStamp2() throws BadLocationException {
		final Document document= new Document("");
		final int stringLength= 13;
		fTextViewer.setDocument(document);
		document.replace(0, 0, createRandomString(stringLength));
		long stamp= document.getModificationStamp();
		fUndoManager.undo();
		document.replace(0, 0, createRandomString(stringLength));
		assertFalse(stamp == document.getModificationStamp());

	}

	private static String createRandomString(int length) {
		final StringBuffer buffer= new StringBuffer();

		for (int i= 0; i < length; i++)
			buffer.append(getRandomCharacter());

		return buffer.toString();
	}

	private static final char getRandomCharacter() {
		// XXX should include \t
		return (char) (32 + 95 * Math.random());
	}

	private static String createRandomStringPoisson() {
		final int length= getRandomPoissonValue(2);
		return createRandomString(length);
	}

	private static Position createRandomPositionPoisson(int documentLength) {

		float random= (float) Math.random();
		int offset= (int) (random * (documentLength + 1));

		// Catch potential rounding issue
		if (offset == documentLength + 1)
			offset= documentLength;

		int length= getRandomPoissonValue(2);
		if (offset + length > documentLength)
			length= documentLength - offset;

		return new Position(offset, length);
	}

	private static int getRandomPoissonValue(int mean) {
		final int MAX_VALUE= 10;

		final float random= (float) Math.random();
		float probability= 0;
		int i= 0;
		while (probability < 1 && i < MAX_VALUE) {
			probability += getPoissonDistribution(mean, i);
			if (random <= probability)
				break;
			i++;
		}
		return i;
	}

	private static float getPoissonDistribution(float lambda, int k) {
		return (float) (Math.exp(-lambda) * Math.pow(lambda, k) / faculty(k));
	}

	/**
	 * Returns the faculty of k.
	 *
	 * @param k the <code>int</code> for which to get the faculty
	 * @return the faculty
	 */
	private static final int faculty(int k) {
		return k == 0
			? 1
			: k * faculty(k - 1);
	}

}
