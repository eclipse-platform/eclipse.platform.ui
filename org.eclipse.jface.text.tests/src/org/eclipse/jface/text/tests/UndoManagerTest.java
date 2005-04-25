/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.text.tests;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DefaultUndoManager;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.IUndoManager;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.TextViewer;

/**
 * Test for DefaultUndoManager
 */
public class UndoManagerTest extends TestCase {

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
	private static final String staticOriginal= "y/D!=m}@#i4|;=/^::du]3_5g6JnYA>b*%hv#OKZUNkm&5Ujs:";
	/** Replacement string */
	private static final String [] staticStrings= {"0G6", "8o", "+>$", "+P+", "> 5Z", "%+", "\n", "WzVa", "wv", "a", "sDn", "+p;", ")L5", "]aR", "3w0", "%=tVt", "-<p", "{-v", "yM", "(*SD&"};
	/** Position/offset pairs */
	private static final int [] staticPositions= {21, 0, 22, 4, 5, 2, 1, 1, 29, 2, 44, 2, 19, 2, 37, 1, 26, 2, 17, 2, 32, 2, 34, 2, 7, 1, 12, 1, 37, 4, 12, 4, 24, 2, 38, 2, 2, 3, 0, 2, 22, 2, 5, 2, 27, 2, 43, 2, 0, 1, 39, 3, 30, 3, 28, 0, 26, 1, 37, 1, 29, 1, 35, 4, 43, 1, 45, 1, 12, 1, 8, 4, 11, 0, 35, 0, 11, 4, 46, 4};

	public static Test suite() {
		return new TestSuite(UndoManagerTest.class);
	}
	
	/*
	 * @see TestCase#TestCase(String)
	 */
	public UndoManagerTest(final String name) {
		super(name);	
	}
	
	/*
	 *  @see TestCase#setUp()
	 */
	protected void setUp() {
		fShell= new Shell();	
		fUndoManager= new DefaultUndoManager(MAX_UNDO_LEVEL);
		fTextViewer= new TextViewer(fShell, SWT.NONE);
		fTextViewer.setUndoManager(fUndoManager);
		fUndoManager.connect(fTextViewer);
	}
	
	/**
	 * Test for line delimiter conversion.
	 */	
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
			for (int i= 0; i < count; i++) {
				final Position position= createRandomPositionPoisson(document.getLength());
				final String string= createRandomStringPoisson(4);
				document.replace(position.getOffset(), position.getLength(), string);
			}
		} catch (BadLocationException e) {
			assertTrue(false);
		}
	}
	
	// repeatable test case for comparing success/failure among different tests
	private void doRepeatableChange(IDocument document) {
		assertTrue(staticPositions.length >= (2 * staticStrings.length));
		try {
			for (int i= 0; i < staticStrings.length; i++) {
				document.replace(staticPositions[i*2], staticPositions[i*2+1], staticStrings[i]);
			}
		} catch (BadLocationException e) {
			assertTrue(false);
		}
	}
	
	public void testLoopRandomAccessAsCompound() {
		int i= 0;
		while (i < LOOP_COUNT) {
			fUndoManager.reset();
			testRandomAccessAsCompound();
			i++;
		}
	}
	
	public void testLoopRandomAccess() {
		int i= 0;
		while (i < LOOP_COUNT) {
			fUndoManager.reset();
			testRandomAccess();
			i++;
		}
	}
	
	public void testLoopRandomAccessAsUnclosedCompound() {
		int i= 0;
		while (i < LOOP_COUNT) {
			fUndoManager.reset();
			testRandomAccessAsUnclosedCompound();
			i++;
		}
	}
	
	public void testLoopConvertLineDelimiters() {
		int i= 0;
		while (i < LOOP_COUNT) {
			fUndoManager.reset();
			testConvertLineDelimiters();
			i++;
		}
	}
	
	public void testLoopRandomAccessWithMixedCompound() {
		int i= 0;
		while (i < LOOP_COUNT) {
			fUndoManager.reset();
			testRandomAccessWithMixedCompound();
			i++;
		}
	}
	
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
			for (int j= 0; j < NUMBER_ATOMIC_PER_COMPOUND; j++)
				doChange(document, RANDOM_REPLACE_COUNT);
		}

		assertTrue(fUndoManager.undoable());
		while (fUndoManager.undoable())
			fUndoManager.undo();
		assertTrue(!fUndoManager.undoable());
			
		final String reverted= document.get();

		assertEquals(original, reverted);		
	}

	public void testRepeatableAccess() {
		
		final IDocument document= new Document(staticOriginal);
		fTextViewer.setDocument(document);
	
		doRepeatableChange(document);
		
		assertTrue(fUndoManager.undoable());
		while (fUndoManager.undoable())
			fUndoManager.undo();
			
		final String reverted= document.get();

		assertEquals(staticOriginal, reverted);
	}
	
	public void testRepeatableAccessAsCompound() {
		
		final IDocument document= new Document(staticOriginal);
		fTextViewer.setDocument(document);
	
		fUndoManager.beginCompoundChange();
		doRepeatableChange(document);
		fUndoManager.endCompoundChange();
		
		assertTrue(fUndoManager.undoable());
		fUndoManager.undo();
		// with a single compound, there should be only one undo
		assertFalse(fUndoManager.undoable());
			
		final String reverted= document.get();

		assertEquals(staticOriginal, reverted);
	}
	
	public void testRepeatableAccessAsUnclosedCompound() {
		
		final IDocument document= new Document(staticOriginal);
		fTextViewer.setDocument(document);
	
		fUndoManager.beginCompoundChange();
		doRepeatableChange(document);
		
		assertTrue(fUndoManager.undoable());
		while (fUndoManager.undoable())
			fUndoManager.undo();
			
		final String reverted= document.get();

		assertEquals(staticOriginal, reverted);
	}
	
	public void testRepeatableAccessWithMixedAndEmptyCompound() {
		
		final int NUMBER_ATOMIC_PER_COMPOUND= 3;

		final IDocument document= new Document(staticOriginal);
		fTextViewer.setDocument(document);

		fUndoManager.beginCompoundChange();		
		doRepeatableChange(document);
		fUndoManager.endCompoundChange();
		assertTrue(fUndoManager.undoable());
		
		// insert an empty compound
		fUndoManager.beginCompoundChange();
		fUndoManager.endCompoundChange();
			
	    // insert the atomic changes
		for (int j=0; j<NUMBER_ATOMIC_PER_COMPOUND; j++)
				doRepeatableChange(document);

		assertTrue(fUndoManager.undoable());
		while (fUndoManager.undoable())
			fUndoManager.undo();
		assertTrue(!fUndoManager.undoable());
			
		final String reverted= document.get();

		assertEquals(staticOriginal, reverted);		
	}
	
	public void testDocumentStamp() {
		final Document document= new Document(staticOriginal);
		fTextViewer.setDocument(document);
		long stamp= document.getModificationStamp();
		doChange(document, 1);
		fUndoManager.undo();
		assertEquals(stamp, document.getModificationStamp());

	}
	
	private static String createRandomString(int length) {
		final StringBuffer buffer= new StringBuffer();
		
		for (int i= 0; i < length; i++)
			buffer.append(getRandomCharacter());

		return buffer.toString();
	}
	
	private static final char getRandomCharacter() {
//		return Math.random() < 0.5
//			? '\r'
//			: '\n';
					
		// XXX must include \r, \n, \t
		return (char) (32 + 95 * Math.random());
	}
	
	private static String createRandomStringPoisson(int mean) {
		final int length= getRandomPoissonValue(2);
		return createRandomString(length);
	}
	
	private static Position createRandomPositionPoisson(int documentLength) {

		final float random= (float) Math.random();
		final int offset= (int) (random * (documentLength + 1));

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
