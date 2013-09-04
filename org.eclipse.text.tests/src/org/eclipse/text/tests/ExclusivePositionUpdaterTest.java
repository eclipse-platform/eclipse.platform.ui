/*******************************************************************************
 * Copyright (c) 2000, 2013 IBM Corporation and others.
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

import org.junit.Assert;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DefaultPositionUpdater;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IPositionUpdater;
import org.eclipse.jface.text.Position;

/**
 * Tests DefaultPositionUpdater. Does NOT test one of the ExclusivePositionUpdaters.
 * @since 3.3
 */
public class ExclusivePositionUpdaterTest extends TestCase {
	public static Test suite() {
		return new TestSuite(ExclusivePositionUpdaterTest.class);
	}

	private IPositionUpdater fUpdater;
	private static final String CATEGORY= "testcategory";
	private Position fPos;
	private IDocument fDoc;
	/*
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		fUpdater= new DefaultPositionUpdater(CATEGORY);
		fDoc= new Document("ccccccccccccccccccccccccccccccccccccccccccccc");
		fPos= new Position(5, 5);
		// 01234[fPo]0123456789
		fDoc.addPositionUpdater(fUpdater);
		fDoc.addPositionCategory(CATEGORY);
		fDoc.addPosition(CATEGORY, fPos);
	}

	/*
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		fDoc.removePositionUpdater(fUpdater);
		fDoc.removePositionCategory(CATEGORY);
	}
	
	// Delete, ascending by offset, length:

	public void testDeleteBefore() throws BadLocationException {
		fDoc.replace(2, 2, "");
		Assert.assertEquals(3, fPos.offset);
		Assert.assertEquals(5, fPos.length);
		Assert.assertFalse(fPos.isDeleted);
	}
	
	public void testDeleteRightBefore() throws BadLocationException {
		fDoc.replace(3, 2, "");
		Assert.assertEquals(3, fPos.offset);
		Assert.assertEquals(5, fPos.length);
		Assert.assertFalse(fPos.isDeleted);
	}
	
	public void testDeleteOverLeftBorder() throws BadLocationException {
		fDoc.replace(3, 6, "");
		Assert.assertEquals(3, fPos.offset);
		Assert.assertEquals(1, fPos.length);
		Assert.assertFalse(fPos.isDeleted);
	}

	public void testDeleteOverLeftBorderTillRight() throws BadLocationException {
		fDoc.replace(4, 6, "");
		Assert.assertEquals(4, fPos.offset);
		Assert.assertEquals(0, fPos.length);
		Assert.assertFalse(fPos.isDeleted);
	}
	
	public void testDeleted() throws BadLocationException {
		fDoc.replace(4, 7, "");
		Assert.assertTrue(fPos.isDeleted);
	}
	
	public void testDeleteAtOffset() throws BadLocationException {
		fDoc.replace(5, 1, "");
		Assert.assertEquals(5, fPos.offset);
		Assert.assertEquals(4, fPos.length);
		Assert.assertFalse(fPos.isDeleted);
	}
	
	public void testDeleteAtOffset2() throws BadLocationException {
		fDoc.replace(5, 2, "");
		Assert.assertEquals(5, fPos.offset);
		Assert.assertEquals(3, fPos.length);
		Assert.assertFalse(fPos.isDeleted);
	}
	
	public void testDeleteAtOffsetTillRight() throws BadLocationException {
		fDoc.replace(5, 5, "");
		Assert.assertEquals(5, fPos.offset);
		Assert.assertEquals(0, fPos.length);
		Assert.assertFalse(fPos.isDeleted);
	}
	
	public void testDeleteAtOffsetOverRightBorder() throws BadLocationException {
		fDoc.replace(5, 6, "");
		Assert.assertEquals(5, fPos.offset);
		Assert.assertEquals(0, fPos.length);
		Assert.assertFalse(fPos.isDeleted);
	}
	
	public void testDeleteWithin() throws BadLocationException {
		fDoc.replace(6, 2, "");
		Assert.assertEquals(5, fPos.offset);
		Assert.assertEquals(3, fPos.length);
		Assert.assertFalse(fPos.isDeleted);
	}
	
	public void testDeleteAtRight() throws BadLocationException {
		fDoc.replace(8, 2, "");
		Assert.assertEquals(5, fPos.offset);
		Assert.assertEquals(3, fPos.length);
		Assert.assertFalse(fPos.isDeleted);
	}
	
	public void testDeleteOverRightBorder() throws BadLocationException {
		fDoc.replace(9, 2, "");
		Assert.assertEquals(5, fPos.offset);
		Assert.assertEquals(4, fPos.length);
		Assert.assertFalse(fPos.isDeleted);
	}
	
	public void testDeleteRightAfter() throws BadLocationException {
		fDoc.replace(10, 2, "");
		Assert.assertEquals(5, fPos.offset);
		Assert.assertEquals(5, fPos.length);
		Assert.assertFalse(fPos.isDeleted);
	}
	
	public void testDeleteAfter() throws BadLocationException {
		fDoc.replace(20, 2, "");
		Assert.assertEquals(5, fPos.offset);
		Assert.assertEquals(5, fPos.length);
		Assert.assertFalse(fPos.isDeleted);
	}

	// Add, ascending by offset:
	
	public void testAddBefore() throws BadLocationException {
		fDoc.replace(2, 0, "yy");
		Assert.assertEquals(7, fPos.offset);
		Assert.assertEquals(5, fPos.length);
	}
	
	public void testAddRightBefore() throws BadLocationException {
		fDoc.replace(5, 0, "yy");
		Assert.assertEquals(7, fPos.offset);
		Assert.assertEquals(5, fPos.length);
	}

	public void testAddWithin() throws BadLocationException {
		fDoc.replace(6, 0, "yy");
		Assert.assertEquals(5, fPos.offset);
		Assert.assertEquals(7, fPos.length);
	}
	
	public void testAddWithin2() throws BadLocationException {
		fDoc.replace(9, 0, "yy");
		Assert.assertEquals(5, fPos.offset);
		Assert.assertEquals(7, fPos.length);
	}
	
	public void testAddRightAfter() throws BadLocationException {
		fDoc.replace(10, 0, "yy");
		Assert.assertEquals(5, fPos.offset);
		Assert.assertEquals(5, fPos.length);
	}
	
	public void testAddAfter() throws BadLocationException {
		fDoc.replace(20, 0, "yy");
		Assert.assertEquals(5, fPos.offset);
		Assert.assertEquals(5, fPos.length);
	}

	// Replace, ascending by offset, length:
	
	public void testReplaceBefore() throws BadLocationException {
		fDoc.replace(2, 2, "y");
		Assert.assertEquals(4, fPos.offset);
		Assert.assertEquals(5, fPos.length);
	}

	public void testReplaceRightBefore() throws BadLocationException {
		fDoc.replace(2, 3, "y");
		Assert.assertEquals(3, fPos.offset);
		Assert.assertEquals(5, fPos.length);
	}
	
	public void testReplaceLeftBorder() throws BadLocationException {
		fDoc.replace(4, 2, "yy");
		Assert.assertEquals(6, fPos.offset);
		Assert.assertEquals(4, fPos.length);
	}

	public void testReplaceLeftBorderTillRight() throws BadLocationException {
		fDoc.replace(4, 6, "yy");
		Assert.assertEquals(6, fPos.offset);
		Assert.assertEquals(0, fPos.length);
	}
	
	public void testReplaced() throws BadLocationException {
		fDoc.replace(4, 7, "yyyyyyy");
		Assert.assertTrue(fPos.isDeleted);
	}
	
	public void testReplaceAtOffset1() throws BadLocationException {
		fDoc.replace(5, 1, "yy");
		// 01234[fPo]0123456789
		Assert.assertEquals(5, fPos.offset);
		Assert.assertEquals(6, fPos.length);
	}
	
	public void testReplaceAtOffset2() throws BadLocationException {
		fDoc.replace(5, 4, "yy");
		// 01234[fPo]0123456789
		Assert.assertEquals(5, fPos.offset);
		Assert.assertEquals(3, fPos.length);
	}
	
	public void testReplaceAtOffsetTillRight() throws BadLocationException {
		fDoc.replace(5, 5, "yy");
		// 01234[fPo]0123456789
		Assert.assertEquals(5, fPos.offset);
		Assert.assertEquals(2, fPos.length);
		Assert.assertFalse(fPos.isDeleted);
	}
	
	public void testReplaceAtRight() throws BadLocationException {
		fDoc.replace(6, 4, "yy");
		Assert.assertEquals(5, fPos.offset);
		Assert.assertEquals(3, fPos.length);
	}
	
	public void testReplaceRightBorder() throws BadLocationException {
		fDoc.replace(9, 2, "yy");
		Assert.assertEquals(5, fPos.offset);
		Assert.assertEquals(4, fPos.length);
	}

	public void testReplaceRightAfter() throws BadLocationException {
		fDoc.replace(10, 2, "y");
		Assert.assertEquals(5, fPos.offset);
		Assert.assertEquals(5, fPos.length);
	}
	
	public void testReplaceAfter() throws BadLocationException {
		fDoc.replace(20, 2, "y");
		Assert.assertEquals(5, fPos.offset);
		Assert.assertEquals(5, fPos.length);
	}

}
