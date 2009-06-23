/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
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
import org.eclipse.ui.internal.cheatsheets.registry.CheatSheetRegistryReader;

public class TestCheatSheetCategories extends TestCase {

	private static final String TEST_CATEGORY = "org.eclipse.ua.tests.cheatsheet.cheatSheetsTestCat";

	public void testForCollection() {
		CheatSheetCollectionElement cheatSheets = 
			(CheatSheetCollectionElement)CheatSheetRegistryReader.getInstance().getCheatSheets();
		Object[] subCategories = cheatSheets.getChildren();
		for (int i = 0; i < subCategories.length; i++) {
			assertTrue(subCategories[i] instanceof CheatSheetCollectionElement);
		}
	}
	
	public void testFindTestCategory() {
		CheatSheetCollectionElement cheatSheets = 
			(CheatSheetCollectionElement)CheatSheetRegistryReader.getInstance().getCheatSheets();
		CheatSheetCollectionElement testCat = findChildCategory(cheatSheets, TEST_CATEGORY);
		assertNotNull("Cannot find category org.eclipse.ua.tests.cheatsheet.cheatSheetsTestCat",
					       testCat);
	}

	public void testFindQualifiedSubcategory() {
		CheatSheetCollectionElement cheatSheets = 
			(CheatSheetCollectionElement)CheatSheetRegistryReader.getInstance().getCheatSheets();
		CheatSheetCollectionElement testCat = findChildCategory(cheatSheets, TEST_CATEGORY);
		CheatSheetCollectionElement subCat = findChildCategory(testCat, 
				"org.eclipse.ua.tests.subcategory");
		assertNotNull(subCat);
	}

	public void testFindCsInUnqualifiedSubcategory() {
		CheatSheetCollectionElement cheatSheets = 
			(CheatSheetCollectionElement)CheatSheetRegistryReader.getInstance().getCheatSheets();
		CheatSheetCollectionElement testCat = findChildCategory(cheatSheets, TEST_CATEGORY);
		CheatSheetCollectionElement subCat = findChildCategory(testCat, 
				"org.eclipse.ua.tests.subcategory");
		CheatSheetElement unqual = findCheatsheet(subCat, 
				"org.eclipse.ua.tests.cheatsheet.subcategory.simple");
		assertNotNull(unqual);
	}
	
	public void testFindCsInQualifiedSubcategory() {
		CheatSheetCollectionElement cheatSheets = 
			(CheatSheetCollectionElement)CheatSheetRegistryReader.getInstance().getCheatSheets();
		CheatSheetCollectionElement testCat = findChildCategory(cheatSheets, TEST_CATEGORY);
		CheatSheetCollectionElement subCat = findChildCategory(testCat, 
				"org.eclipse.ua.tests.subcategory");
		CheatSheetElement qual = findCheatsheet(subCat, 
				"org.eclipse.ua.tests.cheatsheet.subcategory.qualified");
		assertNotNull(qual);
	}

	private CheatSheetCollectionElement findChildCategory(
			CheatSheetCollectionElement collection, String id) {
		Object[] subCategories = collection.getChildren();
		for (int i = 0; i < subCategories.length; i++) {
			 CheatSheetCollectionElement child = (CheatSheetCollectionElement)subCategories[i];
			 if (child.getId().equals(id)) {
				 return child;
			 }
		}
		return null;
	}
	private CheatSheetElement findCheatsheet(
			CheatSheetCollectionElement collection, String id) {
		Object[] cheatSheets = collection.getCheatSheets();
		for (int i = 0; i < cheatSheets.length; i++) {
			 CheatSheetElement child = (CheatSheetElement)cheatSheets[i];
			 if (child.getID().equals(id)) {
				 return child;
			 }
		}
		return null;
	}

}
