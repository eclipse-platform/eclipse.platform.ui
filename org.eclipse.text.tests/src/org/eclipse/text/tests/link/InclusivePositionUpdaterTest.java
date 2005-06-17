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
package org.eclipse.text.tests.link;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.link.InclusivePositionUpdater;

public class InclusivePositionUpdaterTest extends TestCase {
	
	private InclusivePositionUpdater fUpdater;
	private static final String CATEGORY= "testcategory";
	private Position fPos;
	private IDocument fDoc;
	/*
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		fUpdater= new InclusivePositionUpdater(CATEGORY);
		fDoc= new Document("ccccccccccccccccccccccccccccccccccccccccccccc");
		fPos= new Position(5, 5);
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

	public void testDeleteAfter() throws BadLocationException {
		fDoc.replace(20, 2, null);
		Assert.assertEquals(5, fPos.offset);
		Assert.assertEquals(5, fPos.length);
	}
	
	public void testAddAfter() throws BadLocationException {
		fDoc.replace(20, 0, "yy");
		Assert.assertEquals(5, fPos.offset);
		Assert.assertEquals(5, fPos.length);
	}
	
	public void testDeleteBefore() throws BadLocationException {
		fDoc.replace(2, 2, null);
		Assert.assertEquals(3, fPos.offset);
		Assert.assertEquals(5, fPos.length);
	}

	public void testAddBefore() throws BadLocationException {
		fDoc.replace(2, 0, "yy");
		Assert.assertEquals(7, fPos.offset);
		Assert.assertEquals(5, fPos.length);
	}

	public void testAddRightBefore() throws BadLocationException {
		fDoc.replace(5, 0, "yy");
		Assert.assertEquals(5, fPos.offset);
		Assert.assertEquals(7, fPos.length);
	}

	public void testDeleteAtOffset() throws BadLocationException {
		fDoc.replace(5, 2, "");
		Assert.assertEquals(5, fPos.offset);
		Assert.assertEquals(3, fPos.length);
	}

	public void testDeleteRightBefore() throws BadLocationException {
		fDoc.replace(3, 2, "");
		Assert.assertEquals(3, fPos.offset);
		Assert.assertEquals(5, fPos.length);
	}

	public void testAddRightAfter() throws BadLocationException {
		fDoc.replace(10, 0, "yy");
		Assert.assertEquals(5, fPos.offset);
		Assert.assertEquals(7, fPos.length);
	}

	public void testDeleteRightAfter() throws BadLocationException {
		fDoc.replace(10, 2, "");
		Assert.assertEquals(5, fPos.offset);
		Assert.assertEquals(5, fPos.length);
	}

	public void testAddWithin() throws BadLocationException {
		fDoc.replace(6, 0, "yy");
		Assert.assertEquals(5, fPos.offset);
		Assert.assertEquals(7, fPos.length);
	}

	public void testDeleteWithin() throws BadLocationException {
		fDoc.replace(6, 2, "");
		Assert.assertEquals(5, fPos.offset);
		Assert.assertEquals(3, fPos.length);
	}

	public void testReplaceLeftBorder() throws BadLocationException {
		fDoc.replace(4, 2, "yy");
		Assert.assertEquals(4, fPos.offset);
		Assert.assertEquals(6, fPos.length);
	}

	public void testReplaceRightBorder() throws BadLocationException {
		fDoc.replace(9, 2, "yy");
		Assert.assertEquals(5, fPos.offset);
		Assert.assertEquals(6, fPos.length);
	}

	public void testDeleteOverRightBorder() throws BadLocationException {
		fDoc.replace(9, 2, "");
		Assert.assertEquals(5, fPos.offset);
		Assert.assertEquals(4, fPos.length);
	}
	
	public void testDeleted() throws BadLocationException {
		fDoc.replace(4, 7, null);
		Assert.assertTrue(fPos.isDeleted);
	}

	public void testReplaced() throws BadLocationException {
		fDoc.replace(4, 7, "yyyyyyy");
		Assert.assertTrue(fPos.isDeleted);
	}

}
