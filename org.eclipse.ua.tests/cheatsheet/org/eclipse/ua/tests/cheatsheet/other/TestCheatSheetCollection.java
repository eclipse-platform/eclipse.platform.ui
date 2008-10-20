/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ua.tests.cheatsheet.other;

import junit.framework.TestCase;

import org.eclipse.ui.internal.cheatsheets.registry.CheatSheetCollectionElement;
import org.eclipse.ui.internal.cheatsheets.registry.CheatSheetElement;

public class TestCheatSheetCollection extends TestCase {
	
	private CheatSheetCollectionElement root;
	private CheatSheetElement csA;
	private CheatSheetElement csB;
	private CheatSheetElement cs12A;
	private CheatSheetElement cs2A;
	private CheatSheetCollectionElement c1;
	private CheatSheetCollectionElement c2;
	private CheatSheetCollectionElement c11;
	private CheatSheetCollectionElement c12;
	
	protected void setUp() throws Exception {
		root = new CheatSheetCollectionElement("rootPlugin", "rootId", "rootName", root);
		csA = new CheatSheetElement("A");
		csA.setID("idA");
		csB = new CheatSheetElement("B");
		csB.setID("idB");
		cs12A = new CheatSheetElement("12A");
		cs12A.setID("id12A");
		cs2A = new CheatSheetElement("2A");
		cs2A.setID("id2A");
		c1 = new CheatSheetCollectionElement("p1", "c1Id", "c1", root);
		c2 = new CheatSheetCollectionElement("p2", "c2Id", "c2", root);
		c11 = new CheatSheetCollectionElement("p11", "c11Id", "c11", c1);
		c12 = new CheatSheetCollectionElement("p12", "c12Id", "c12", c1);		
	    root.add(c1);
	    root.add(csA);
	    root.add(csB);
	    root.add(c2);
	    c1.add(c11);
	    c1.add(c12);
	    c2.add(cs2A);
	    c12.add(cs12A);
	}

	public void testRoot() {
		assertEquals(2, root.getChildren().length);
		assertEquals(2, root.getCheatSheets().length);
		assertFalse(root.isEmpty());
		assertEquals("rootName", root.getLabel(null));
		assertEquals("rootId", root.getId());
		assertEquals("rootPlugin", root.getPluginId());
	}

	public void testTopLevelChildCategories() {
		Object[] children = root.getChildren();
		assertEquals(c1, children[0]);
		assertEquals(c2, children[1]);
		assertEquals(2, c1.getChildren().length);
		assertEquals(0, c1.getCheatSheets().length);
		assertFalse(c1.isEmpty());
		assertEquals(0, c2.getChildren().length);
		assertEquals(1, c2.getCheatSheets().length);
		assertFalse(c2.isEmpty());
	}
	
	public void testTopLevelCheatsheets() {
		Object[] cheatsheets = root.getCheatSheets();
		assertEquals(csA, cheatsheets[0]);
		assertEquals(csB, cheatsheets[1]);
	}
	
	public void testSecondLevelChildCategories() {
		Object[] children = c1.getChildren();
		assertEquals(c11, children[0]);
		assertEquals(c12, children[1]);
		assertEquals(0, c11.getChildren().length);
		assertEquals(0, c11.getCheatSheets().length);
		assertTrue(c11.isEmpty());
		assertEquals(0, c12.getChildren().length);
		assertEquals(1, c12.getCheatSheets().length);
		assertFalse(c12.isEmpty());
	}
	
	public void testFind() {
		assertEquals(csA, root.findCheatSheet("idA", true));
		assertEquals(csA, root.findCheatSheet("idA", false));
		assertNull(root.findCheatSheet("idC", true));
		assertEquals(cs12A, root.findCheatSheet("id12A", true));
		assertNull(root.findCheatSheet("id12A", false));
	}
}
