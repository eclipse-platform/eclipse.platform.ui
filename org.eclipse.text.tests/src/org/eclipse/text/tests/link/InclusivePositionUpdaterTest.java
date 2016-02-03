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
package org.eclipse.text.tests.link;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IPositionUpdater;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.link.InclusivePositionUpdater;

public class InclusivePositionUpdaterTest {

	private IPositionUpdater fUpdater;
	private static final String CATEGORY= "testcategory";
	private Position fPos;
	private IDocument fDoc;
	
	@Before
	public void setUp() throws Exception {
		fUpdater= new InclusivePositionUpdater(CATEGORY);
		fDoc= new Document("ccccccccccccccccccccccccccccccccccccccccccccc");
		fPos= new Position(5, 5);
		fDoc.addPositionUpdater(fUpdater);
		fDoc.addPositionCategory(CATEGORY);
		fDoc.addPosition(CATEGORY, fPos);
	}

	@After
	public void tearDown() throws Exception{
		fDoc.removePositionUpdater(fUpdater);
		fDoc.removePositionCategory(CATEGORY);
	}
	
	@Test
	public void testDeleteAfter() throws BadLocationException {
		fDoc.replace(20, 2, "");
		Assert.assertEquals(5, fPos.offset);
		Assert.assertEquals(5, fPos.length);
	}
	
	@Test
	public void testAddAfter() throws BadLocationException {
		fDoc.replace(20, 0, "yy");
		Assert.assertEquals(5, fPos.offset);
		Assert.assertEquals(5, fPos.length);
	}
	
	@Test
	public void testDeleteBefore() throws BadLocationException {
		fDoc.replace(2, 2, "");
		Assert.assertEquals(3, fPos.offset);
		Assert.assertEquals(5, fPos.length);
	}
	
	@Test
	public void testAddBefore() throws BadLocationException {
		fDoc.replace(2, 0, "yy");
		Assert.assertEquals(7, fPos.offset);
		Assert.assertEquals(5, fPos.length);
	}
	
	@Test
	public void testAddRightBefore() throws BadLocationException {
		fDoc.replace(5, 0, "yy");
		Assert.assertEquals(5, fPos.offset);
		Assert.assertEquals(7, fPos.length);
	}
	
	@Test
	public void testDeleteAtOffset() throws BadLocationException {
		fDoc.replace(5, 2, "");
		Assert.assertEquals(5, fPos.offset);
		Assert.assertEquals(3, fPos.length);
	}
	
	@Test
	public void testDeleteRightBefore() throws BadLocationException {
		fDoc.replace(3, 2, "");
		Assert.assertEquals(3, fPos.offset);
		Assert.assertEquals(5, fPos.length);
	}
	
	@Test
	public void testAddRightAfter() throws BadLocationException {
		fDoc.replace(10, 0, "yy");
		Assert.assertEquals(5, fPos.offset);
		Assert.assertEquals(7, fPos.length);
	}
	
	@Test
	public void testDeleteRightAfter() throws BadLocationException {
		fDoc.replace(10, 2, "");
		Assert.assertEquals(5, fPos.offset);
		Assert.assertEquals(5, fPos.length);
	}
	
	@Test
	public void testAddWithin() throws BadLocationException {
		fDoc.replace(6, 0, "yy");
		Assert.assertEquals(5, fPos.offset);
		Assert.assertEquals(7, fPos.length);
	}
	
	@Test
	public void testDeleteWithin() throws BadLocationException {
		fDoc.replace(6, 2, "");
		Assert.assertEquals(5, fPos.offset);
		Assert.assertEquals(3, fPos.length);
	}
	
	@Test
	public void testReplaceLeftBorder() throws BadLocationException {
		fDoc.replace(4, 2, "yy");
		Assert.assertEquals(4, fPos.offset);
		Assert.assertEquals(6, fPos.length);
	}
	
	@Test
	public void testReplaceRightBorder() throws BadLocationException {
		fDoc.replace(9, 2, "yy");
		Assert.assertEquals(5, fPos.offset);
		Assert.assertEquals(6, fPos.length);
	}
	
	@Test
	public void testDeleteOverRightBorder() throws BadLocationException {
		fDoc.replace(9, 2, "");
		Assert.assertEquals(5, fPos.offset);
		Assert.assertEquals(4, fPos.length);
	}
	
	@Test
	public void testDeleted() throws BadLocationException {
		fDoc.replace(4, 7, "");
		Assert.assertTrue(fPos.isDeleted);
	}
	
	@Test
	public void testReplaced() throws BadLocationException {
		fDoc.replace(4, 7, "yyyyyyy");
		Assert.assertTrue(fPos.isDeleted);
	}

}
