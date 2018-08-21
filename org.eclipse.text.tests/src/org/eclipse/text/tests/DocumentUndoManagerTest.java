/*******************************************************************************
 * Copyright (c) 2009, 2010 IBM Corporation and others.
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

import junit.framework.TestCase;

import org.eclipse.core.commands.ExecutionException;

import org.eclipse.text.edits.DeleteEdit;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.text.undo.DocumentUndoManager;
import org.eclipse.text.undo.IDocumentUndoManager;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;


/**
 * Tests for DefaultUndoManager.
 * 
 * @since 3.5
 */
public class DocumentUndoManagerTest extends TestCase {


	/** The maximum undo level. */
	private static final int MAX_UNDO_LEVEL= 256;


	//--- Static data sets for comparing scenarios - obtained from capturing random data ---
	/** Original document */
	private static final String INITIAL_DOCUMENT_CONTENT= "+7cyg:/F!T4KnW;0+au$t1G%(`Z|u'7'_!-k?<c\"2Y.]CwsO.r";

	/** Replacement string */
	private static final String[] REPLACEMENTS= { ">", "F", "M/r-*", "-", "bl", "", "}%/#", "", "k&", "f", "\\g", "c!x", "TLG-", "NPO", "Rp9u", "", "X", "W(", ")z", "oe", "", "h*", "t", "I", "X=N>",
			"2yt", "&Z", "2)W=", ":K", "P9S", "s8t8o", "", "", "5{7", "%", "", "v3", "Wz", "sH", "3c", "8", "ol", ",6$", "94[#", ".~", "n", ">", "9", "W", ",(FW", "Q", "^", "Bq", "$", "re", "", "9",
			"8[", "Mx", "4b", "$6", "F", "8s]", "o", "-", "E&6", "S\\", "/", "z.a", "4ai", "b", ")", "", "l", "VU", "7M+Ql", "xZ?x", "xx", "lc", "b", "A", "!", "4pSU", "", "{J", "H", "l>_", "n&9",
			"", "&`", ";igQxq", "", ">", ";\"", "k\\`]G", "o{?", "", "K", "_6", "=" };

	/** Position/offset pairs */
	private static final int[] POSITIONS= { 18, 2, 43, 1, 3, 2, 28, 3, 35, 1, 23, 5, 32, 2, 30, 1, 22, 1, 37, 0, 23, 3, 43, 2, 46, 1, 17, 1, 36, 6, 17, 5, 30, 4, 25, 1, 2, 2, 30, 0, 37, 3, 28, 1, 30,
			2, 20, 5, 33, 1, 29, 1, 15, 2, 21, 2, 24, 4, 38, 3, 8, 0, 33, 2, 15, 2, 25, 0, 8, 2, 20, 3, 43, 2, 44, 1, 44, 2, 32, 2, 40, 2, 32, 3, 12, 2, 38, 3, 33, 2, 46, 0, 13, 3, 45, 0, 16, 2, 3,
			2, 44, 0, 48, 0, 18, 5, 7, 6, 7, 3, 40, 0, 9, 1, 16, 3, 28, 3, 36, 1, 35, 2, 0, 3, 6, 1, 10, 4, 14, 2, 15, 3, 33, 1, 36, 0, 37, 0, 4, 3, 31, 3, 33, 3, 11, 3, 20, 2, 25, 3, 4, 3, 7, 3, 17,
			0, 3, 1, 31, 3, 34, 1, 21, 0, 33, 1, 17, 4, 9, 1, 26, 3, 2, 3, 12, 1, 26, 3, 9, 5, 5, 0, 31, 3, 0, 3, 12, 1, 1, 1, 3, 0, 39, 0, 9, 2, 2, 0, 28, 2 };

	private static final boolean DEBUG= false;

	/** The undo manager. */
	private IDocumentUndoManager fUndoManager;

	@Override
	protected void setUp() {
		fUndoManager= null;
	}


	@Override
	protected void tearDown() {
		fUndoManager.disconnect(this);
		fUndoManager= null;
	}

	/**
	 * Test for line delimiter conversion.
	 * 
	 * @throws ExecutionException if undo fails
	 */
	public void testConvertLineDelimiters() throws ExecutionException {
		final String original= "a\r\nb\r\n";
		final IDocument document= new Document(original);
		createUndoManager(document);

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


	private void createUndoManager(final IDocument document) {
		fUndoManager= new DocumentUndoManager(document);
		fUndoManager.connect(this);
		fUndoManager.setMaximalUndoLevel(MAX_UNDO_LEVEL);
	}

	/**
	 * Randomly applies document changes.
	 * 
	 * @throws ExecutionException if undo fails
	 */
	public void testRandomAccess() throws ExecutionException {
		final int RANDOM_STRING_LENGTH= 50;
		final int RANDOM_REPLACE_COUNT= 100;

		assertTrue(RANDOM_REPLACE_COUNT >= 1);
		assertTrue(RANDOM_REPLACE_COUNT <= MAX_UNDO_LEVEL);

		String original= createRandomString(RANDOM_STRING_LENGTH);
		final IDocument document= new Document(original);
		createUndoManager(document);


		doChange(document, RANDOM_REPLACE_COUNT);

		assertTrue(fUndoManager.undoable());
		while (fUndoManager.undoable())
			fUndoManager.undo();

		final String reverted= document.get();
		assertEquals(original, reverted);
	}

	private void doChange(IDocument document, int count) {
		try {

			if (DEBUG)
				System.out.println(document.get());

			Position[] positions= new Position[count];
			String[] strings= new String[count];
			for (int i= 0; i < count; i++) {
				final Position position= createRandomPositionPoisson(document.getLength());
				final String string= createRandomStringPoisson();
				document.replace(position.getOffset(), position.getLength(), string);
				positions[i]= position;
				strings[i]= string;
			}

			if (DEBUG) {
				System.out.print("{ ");
				for (int i= 0; i < count; i++) {
					System.out.print(positions[i].getOffset());
					System.out.print(", ");
					System.out.print(positions[i].getLength());
					System.out.print(", ");
				}
				System.out.println(" }");
				System.out.print("{ ");
				for (int i= 0; i < count; i++) {
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
				int offset= POSITIONS[i * 2];
				int length= POSITIONS[i * 2 + 1];
				if (document.getLength() > offset + length)
					document.replace(offset, length, REPLACEMENTS[i]);
				else
					document.replace(0, 0, REPLACEMENTS[i]);
			}
		} catch (BadLocationException e) {
			assertTrue(false);
		}
	}

	public void testCompoundTextEdit() throws ExecutionException, BadLocationException {
		final int RANDOM_STRING_LENGTH= 50;
		final int RANDOM_REPLACE_COUNT= 100;

		assertTrue(RANDOM_REPLACE_COUNT >= 1);
		assertTrue(RANDOM_REPLACE_COUNT <= MAX_UNDO_LEVEL);

		String original= createRandomString(RANDOM_STRING_LENGTH);
		final IDocument document= new Document(original);
		createUndoManager(document);

//		fUndoManager.beginCompoundChange();

		MultiTextEdit fRoot= new MultiTextEdit();
		TextEdit e1= new DeleteEdit(3, 1);
		fRoot.addChild(e1);
		fRoot.apply(document);

		fRoot= new MultiTextEdit();
		TextEdit e2= new DeleteEdit(3, 1);
		fRoot.addChild(e2);
		fRoot.apply(document);

//		fUndoManager.endCompoundChange();

		assertTrue(fUndoManager.undoable());
//		while (fUndoManager.undoable())
			fUndoManager.undo();
		assertTrue(!fUndoManager.undoable());

		final String reverted= document.get();

		assertEquals(original, reverted);
	}

	public void testRandomAccessAsCompound() throws ExecutionException {
		final int RANDOM_STRING_LENGTH= 50;
		final int RANDOM_REPLACE_COUNT= 100;

		assertTrue(RANDOM_REPLACE_COUNT >= 1);
		assertTrue(RANDOM_REPLACE_COUNT <= MAX_UNDO_LEVEL);

		String original= createRandomString(RANDOM_STRING_LENGTH);
		final IDocument document= new Document(original);
		createUndoManager(document);

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
	 * 
	 * @throws ExecutionException if undo fails
	 */
	public void testRandomAccessAsUnclosedCompound() throws ExecutionException {

		final int RANDOM_STRING_LENGTH= 50;
		final int RANDOM_REPLACE_COUNT= 100;

		assertTrue(RANDOM_REPLACE_COUNT >= 1);
		assertTrue(RANDOM_REPLACE_COUNT <= MAX_UNDO_LEVEL);

		String original= createRandomString(RANDOM_STRING_LENGTH);
		final IDocument document= new Document(original);
		createUndoManager(document);


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

	public void testRandomAccessWithMixedCompound() throws ExecutionException {

		final int RANDOM_STRING_LENGTH= 50;
		final int RANDOM_REPLACE_COUNT= 10;
		final int NUMBER_COMPOUNDS= 5;
		final int NUMBER_ATOMIC_PER_COMPOUND= 3;

		assertTrue(RANDOM_REPLACE_COUNT >= 1);
		assertTrue(NUMBER_COMPOUNDS * (1 + NUMBER_ATOMIC_PER_COMPOUND) * RANDOM_REPLACE_COUNT <= MAX_UNDO_LEVEL);

		String original= createRandomString(RANDOM_STRING_LENGTH);
		final IDocument document= new Document(original);
		createUndoManager(document);


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

	public void testRepeatableAccess() throws ExecutionException {
		assertTrue(REPLACEMENTS.length <= MAX_UNDO_LEVEL);

		final IDocument document= new Document(INITIAL_DOCUMENT_CONTENT);
		createUndoManager(document);

		doRepeatableChange(document);

		assertTrue(fUndoManager.undoable());
		while (fUndoManager.undoable())
			fUndoManager.undo();

		final String reverted= document.get();

		assertEquals(INITIAL_DOCUMENT_CONTENT, reverted);
	}

	public void testRepeatableAccessAsCompound() throws ExecutionException {
		assertTrue(REPLACEMENTS.length <= MAX_UNDO_LEVEL);

		final IDocument document= new Document(INITIAL_DOCUMENT_CONTENT);
		createUndoManager(document);

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

	public void testRepeatableAccessAsUnclosedCompound() throws ExecutionException {
		assertTrue(REPLACEMENTS.length <= MAX_UNDO_LEVEL);

		final IDocument document= new Document(INITIAL_DOCUMENT_CONTENT);
		createUndoManager(document);


		fUndoManager.beginCompoundChange();
		doRepeatableChange(document);

		assertTrue(fUndoManager.undoable());
		while (fUndoManager.undoable())
			fUndoManager.undo();

		final String reverted= document.get();

		assertEquals(INITIAL_DOCUMENT_CONTENT, reverted);
	}

	public void testDocumentStamp() throws ExecutionException {
		final Document document= new Document(INITIAL_DOCUMENT_CONTENT);
		fUndoManager= new DocumentUndoManager(document);
		fUndoManager.connect(this);

		long stamp= document.getModificationStamp();
		doChange(document, 1);
		fUndoManager.undo();
		assertEquals(stamp, document.getModificationStamp());

	}

	// See https://bugs.eclipse.org/bugs/show_bug.cgi?id=109104
	public void testDocumentStamp2() throws BadLocationException, ExecutionException {
		final Document document= new Document("");
		fUndoManager= new DocumentUndoManager(document);
		fUndoManager.connect(this);

		final int stringLength= 13;
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
		return (char)(32 + 95 * Math.random());
	}

	private static String createRandomStringPoisson() {
		final int length= getRandomPoissonValue(2);
		return createRandomString(length);
	}

	private static Position createRandomPositionPoisson(int documentLength) {

		float random= (float)Math.random();
		int offset= (int)(random * (documentLength + 1));

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

		final float random= (float)Math.random();
		float probability= 0;
		int i= 0;
		while (probability < 1 && i < MAX_VALUE) {
			probability+= getPoissonDistribution(mean, i);
			if (random <= probability)
				break;
			i++;
		}
		return i;
	}

	private static float getPoissonDistribution(float lambda, int k) {
		return (float)(Math.exp(-lambda) * Math.pow(lambda, k) / faculty(k));
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

