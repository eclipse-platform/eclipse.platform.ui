/*******************************************************************************
 * Copyright (c) 2000, 2013 IBM Corporation and others.
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


import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
public class ExclusivePositionUpdaterTest {

	private IPositionUpdater fUpdater;
	private static final String CATEGORY= "testcategory";
	private Position fPos;
	private IDocument fDoc;

	@BeforeEach
	public void setUp() throws Exception {
		fUpdater= new DefaultPositionUpdater(CATEGORY);
		fDoc= new Document("ccccccccccccccccccccccccccccccccccccccccccccc");
		fPos= new Position(5, 5);
		// 01234[fPo]0123456789
		fDoc.addPositionUpdater(fUpdater);
		fDoc.addPositionCategory(CATEGORY);
		fDoc.addPosition(CATEGORY, fPos);
	}

	@AfterEach
	public void tearDown() throws Exception {
		fDoc.removePositionUpdater(fUpdater);
		fDoc.removePositionCategory(CATEGORY);
	}

	// Delete, ascending by offset, length:

	@Test
	public void testDeleteBefore() throws BadLocationException {
		fDoc.replace(2, 2, "");
		Assertions.assertEquals(3, fPos.offset);
		Assertions.assertEquals(5, fPos.length);
		Assertions.assertFalse(fPos.isDeleted);
	}

	@Test
	public void testDeleteRightBefore() throws BadLocationException {
		fDoc.replace(3, 2, "");
		Assertions.assertEquals(3, fPos.offset);
		Assertions.assertEquals(5, fPos.length);
		Assertions.assertFalse(fPos.isDeleted);
	}

	@Test
	public void testDeleteOverLeftBorder() throws BadLocationException {
		fDoc.replace(3, 6, "");
		Assertions.assertEquals(3, fPos.offset);
		Assertions.assertEquals(1, fPos.length);
		Assertions.assertFalse(fPos.isDeleted);
	}

	@Test
	public void testDeleteOverLeftBorderTillRight() throws BadLocationException {
		fDoc.replace(4, 6, "");
		Assertions.assertEquals(4, fPos.offset);
		Assertions.assertEquals(0, fPos.length);
		Assertions.assertFalse(fPos.isDeleted);
	}

	@Test
	public void testDeleted() throws BadLocationException {
		fDoc.replace(4, 7, "");
		Assertions.assertTrue(fPos.isDeleted);
	}

	@Test
	public void testDeleteAtOffset() throws BadLocationException {
		fDoc.replace(5, 1, "");
		Assertions.assertEquals(5, fPos.offset);
		Assertions.assertEquals(4, fPos.length);
		Assertions.assertFalse(fPos.isDeleted);
	}

	@Test
	public void testDeleteAtOffset2() throws BadLocationException {
		fDoc.replace(5, 2, "");
		Assertions.assertEquals(5, fPos.offset);
		Assertions.assertEquals(3, fPos.length);
		Assertions.assertFalse(fPos.isDeleted);
	}

	@Test
	public void testDeleteAtOffsetTillRight() throws BadLocationException {
		fDoc.replace(5, 5, "");
		Assertions.assertEquals(5, fPos.offset);
		Assertions.assertEquals(0, fPos.length);
		Assertions.assertFalse(fPos.isDeleted);
	}

	@Test
	public void testDeleteAtOffsetOverRightBorder() throws BadLocationException {
		fDoc.replace(5, 6, "");
		Assertions.assertEquals(5, fPos.offset);
		Assertions.assertEquals(0, fPos.length);
		Assertions.assertFalse(fPos.isDeleted);
	}

	@Test
	public void testDeleteWithin() throws BadLocationException {
		fDoc.replace(6, 2, "");
		Assertions.assertEquals(5, fPos.offset);
		Assertions.assertEquals(3, fPos.length);
		Assertions.assertFalse(fPos.isDeleted);
	}

	@Test
	public void testDeleteAtRight() throws BadLocationException {
		fDoc.replace(8, 2, "");
		Assertions.assertEquals(5, fPos.offset);
		Assertions.assertEquals(3, fPos.length);
		Assertions.assertFalse(fPos.isDeleted);
	}

	@Test
	public void testDeleteOverRightBorder() throws BadLocationException {
		fDoc.replace(9, 2, "");
		Assertions.assertEquals(5, fPos.offset);
		Assertions.assertEquals(4, fPos.length);
		Assertions.assertFalse(fPos.isDeleted);
	}

	@Test
	public void testDeleteRightAfter() throws BadLocationException {
		fDoc.replace(10, 2, "");
		Assertions.assertEquals(5, fPos.offset);
		Assertions.assertEquals(5, fPos.length);
		Assertions.assertFalse(fPos.isDeleted);
	}

	@Test
	public void testDeleteAfter() throws BadLocationException {
		fDoc.replace(20, 2, "");
		Assertions.assertEquals(5, fPos.offset);
		Assertions.assertEquals(5, fPos.length);
		Assertions.assertFalse(fPos.isDeleted);
	}

	// Add, ascending by offset:

	@Test
	public void testAddBefore() throws BadLocationException {
		fDoc.replace(2, 0, "yy");
		Assertions.assertEquals(7, fPos.offset);
		Assertions.assertEquals(5, fPos.length);
	}

	@Test
	public void testAddRightBefore() throws BadLocationException {
		fDoc.replace(5, 0, "yy");
		Assertions.assertEquals(7, fPos.offset);
		Assertions.assertEquals(5, fPos.length);
	}

	@Test
	public void testAddWithin() throws BadLocationException {
		fDoc.replace(6, 0, "yy");
		Assertions.assertEquals(5, fPos.offset);
		Assertions.assertEquals(7, fPos.length);
	}

	@Test
	public void testAddWithin2() throws BadLocationException {
		fDoc.replace(9, 0, "yy");
		Assertions.assertEquals(5, fPos.offset);
		Assertions.assertEquals(7, fPos.length);
	}

	@Test
	public void testAddRightAfter() throws BadLocationException {
		fDoc.replace(10, 0, "yy");
		Assertions.assertEquals(5, fPos.offset);
		Assertions.assertEquals(5, fPos.length);
	}

	@Test
	public void testAddAfter() throws BadLocationException {
		fDoc.replace(20, 0, "yy");
		Assertions.assertEquals(5, fPos.offset);
		Assertions.assertEquals(5, fPos.length);
	}

	// Replace, ascending by offset, length:

	@Test
	public void testReplaceBefore() throws BadLocationException {
		fDoc.replace(2, 2, "y");
		Assertions.assertEquals(4, fPos.offset);
		Assertions.assertEquals(5, fPos.length);
	}

	@Test
	public void testReplaceRightBefore() throws BadLocationException {
		fDoc.replace(2, 3, "y");
		Assertions.assertEquals(3, fPos.offset);
		Assertions.assertEquals(5, fPos.length);
	}

	@Test
	public void testReplaceLeftBorder() throws BadLocationException {
		fDoc.replace(4, 2, "yy");
		Assertions.assertEquals(6, fPos.offset);
		Assertions.assertEquals(4, fPos.length);
	}

	@Test
	public void testReplaceLeftBorderTillRight() throws BadLocationException {
		fDoc.replace(4, 6, "yy");
		Assertions.assertEquals(6, fPos.offset);
		Assertions.assertEquals(0, fPos.length);
	}

	@Test
	public void testReplaced() throws BadLocationException {
		fDoc.replace(4, 7, "yyyyyyy");
		Assertions.assertTrue(fPos.isDeleted);
	}

	@Test
	public void testReplaceAtOffset1() throws BadLocationException {
		fDoc.replace(5, 1, "yy");
		// 01234[fPo]0123456789
		Assertions.assertEquals(5, fPos.offset);
		Assertions.assertEquals(6, fPos.length);
	}

	@Test
	public void testReplaceAtOffset2() throws BadLocationException {
		fDoc.replace(5, 4, "yy");
		// 01234[fPo]0123456789
		Assertions.assertEquals(5, fPos.offset);
		Assertions.assertEquals(3, fPos.length);
	}

	@Test
	public void testReplaceAtOffsetTillRight() throws BadLocationException {
		fDoc.replace(5, 5, "yy");
		// 01234[fPo]0123456789
		Assertions.assertEquals(5, fPos.offset);
		Assertions.assertEquals(2, fPos.length);
		Assertions.assertFalse(fPos.isDeleted);
	}

	@Test
	public void testReplaceAtRight() throws BadLocationException {
		fDoc.replace(6, 4, "yy");
		Assertions.assertEquals(5, fPos.offset);
		Assertions.assertEquals(3, fPos.length);
	}

	@Test
	public void testReplaceRightBorder() throws BadLocationException {
		fDoc.replace(9, 2, "yy");
		Assertions.assertEquals(5, fPos.offset);
		Assertions.assertEquals(4, fPos.length);
	}

	@Test
	public void testReplaceRightAfter() throws BadLocationException {
		fDoc.replace(10, 2, "y");
		Assertions.assertEquals(5, fPos.offset);
		Assertions.assertEquals(5, fPos.length);
	}

	@Test
	public void testReplaceAfter() throws BadLocationException {
		fDoc.replace(20, 2, "y");
		Assertions.assertEquals(5, fPos.offset);
		Assertions.assertEquals(5, fPos.length);
	}

}
