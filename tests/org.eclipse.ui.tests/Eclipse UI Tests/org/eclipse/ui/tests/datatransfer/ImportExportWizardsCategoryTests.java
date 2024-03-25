/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
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
package org.eclipse.ui.tests.datatransfer;

import org.eclipse.core.runtime.IPath;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.registry.WizardsRegistryReader;
import org.eclipse.ui.tests.harness.util.UITestCase;
import org.eclipse.ui.wizards.IWizardCategory;
import org.eclipse.ui.wizards.IWizardDescriptor;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class ImportExportWizardsCategoryTests extends UITestCase {

	private static String WIZARD_ID_IMPORT_NO_CATEGORY = "org.eclipse.ui.tests.import.NoCategory";
	private static String WIZARD_ID_IMPORT_INVALID_CATEGORY = "org.eclipse.ui.tests.import.InvalidCategory";
	private static String WIZARD_IMPORT_NEW_CATEGORY = "org.eclipse.ui.tests.TestImport";
	private static String WIZARD_ID_IMPORT_NEW_CATEGORY = "org.eclipse.ui.tests.import.NewCategory";
	private static String WIZARD_IMPORT_NEW_PARENTED_CATEGORY = "org.eclipse.ui.Basic/org.eclipse.ui.tests.TestImportParented";
	private static String WIZARD_ID_IMPORT_PARENTED_CATEGORY = "org.eclipse.ui.tests.import.NewParentedCategory";
	private static String WIZARD_IMPORT_DUPLICATE_CATEGORY = "org.eclipse.ui.tests.TestImportDup";
	private static String WIZARD_ID_IMPORT_DUPLICATE_CATEGORY = "org.eclipse.ui.tests.import.DuplicateCategory";

	private static String WIZARD_ID_EXPORT_NO_CATEGORY = "org.eclipse.ui.tests.export.NoCategory";
	private static String WIZARD_ID_EXPORT_INVALID_CATEGORY = "org.eclipse.ui.tests.export.InvalidCategory";
	private static String WIZARD_EXPORT_NEW_CATEGORY = "org.eclipse.ui.tests.TestExport";
	private static String WIZARD_ID_EXPORT_NEW_CATEGORY = "org.eclipse.ui.tests.export.NewCategory";
	private static String WIZARD_EXPORT_NEW_PARENTED_CATEGORY = "org.eclipse.ui.Basic/org.eclipse.ui.tests.TestExportParented";
	private static String WIZARD_ID_EXPORT_PARENTED_CATEGORY = "org.eclipse.ui.tests.export.NewParentedCategory";
	private static String WIZARD_EXPORT_DUPLICATE_CATEGORY = "org.eclipse.ui.tests.TestExportDup";
	private static String WIZARD_ID_EXPORT_DUPLICATE_CATEGORY = "org.eclipse.ui.tests.export.DuplicateCategory";

	IWizardCategory exportRoot;
	IWizardCategory importRoot;

	public ImportExportWizardsCategoryTests() {
		super(ImportExportWizardsCategoryTests.class.getSimpleName());
		exportRoot = WorkbenchPlugin.getDefault()
			.getExportWizardRegistry().getRootCategory();
		importRoot = WorkbenchPlugin.getDefault()
			.getImportWizardRegistry().getRootCategory();
	}

	/* Import */
	@Test
	public void testImportNoCategoryProvided(){
		IWizardCategory otherCategory = importRoot.findCategory(IPath.fromOSString(
				WizardsRegistryReader.UNCATEGORIZED_WIZARD_CATEGORY));
		if (otherCategory != null){
			IWizardDescriptor wizardDesc =
				otherCategory.findWizard(WIZARD_ID_IMPORT_NO_CATEGORY);
			assertTrue(
				"Could not find wizard with id" + WIZARD_ID_IMPORT_NO_CATEGORY+ "in Other category.",
				wizardDesc != null);
			return;
		}
		assertTrue("Could not find Other category", false);
	}

	@Test
	public void testImportCategoryDoesNotExist(){
		IWizardCategory otherCategory = importRoot.findCategory(IPath.fromOSString(
				WizardsRegistryReader.UNCATEGORIZED_WIZARD_CATEGORY));
		if (otherCategory != null){
			IWizardDescriptor wizardDesc =
				otherCategory.findWizard(WIZARD_ID_IMPORT_INVALID_CATEGORY);
			assertTrue(
				"Could not find wizard with id" + WIZARD_ID_IMPORT_INVALID_CATEGORY+ "in Other category.",
				wizardDesc != null);
			return;
		}
		assertTrue("Could not find Other category", false);
	}

	@Test
	public void testImportAddToNewCategory(){
		IWizardCategory newCategory = importRoot.findCategory(
				IPath.fromOSString(WIZARD_IMPORT_NEW_CATEGORY));
		if (newCategory != null){
			IWizardDescriptor wizardDesc =
				newCategory.findWizard(WIZARD_ID_IMPORT_NEW_CATEGORY);
			assertTrue(
				"Could not find wizard with id" + WIZARD_ID_IMPORT_NEW_CATEGORY+ "in category " + WIZARD_IMPORT_NEW_CATEGORY,
				wizardDesc != null);
			return;
		}
		assertTrue("Could not find category named " + WIZARD_IMPORT_NEW_CATEGORY, false);
	}

	@Test
	public void testImportAddToParentedCategory(){
		IWizardCategory newCategory = importRoot.findCategory(
				IPath.fromOSString(WIZARD_IMPORT_NEW_PARENTED_CATEGORY));
		if (newCategory != null){
			IWizardDescriptor wizardDesc =
				newCategory.findWizard(WIZARD_ID_IMPORT_PARENTED_CATEGORY);
			assertTrue(
				"Could not find wizard with id" + WIZARD_ID_IMPORT_PARENTED_CATEGORY+ "in category " + WIZARD_IMPORT_NEW_PARENTED_CATEGORY,
				wizardDesc != null);
			return;
		}
		assertTrue("Could not find category named " + WIZARD_IMPORT_NEW_PARENTED_CATEGORY, false);
	}

	@Test
	public void testImportDuplicateCategory(){
		IWizardCategory newCategory = importRoot.findCategory(
				IPath.fromOSString(WIZARD_IMPORT_DUPLICATE_CATEGORY));
		if (newCategory != null){
			IWizardDescriptor wizardDesc =
				newCategory.findWizard(WIZARD_ID_IMPORT_DUPLICATE_CATEGORY);
			assertTrue(
				"Could not find wizard with id" + WIZARD_ID_IMPORT_DUPLICATE_CATEGORY+ "in category " + WIZARD_IMPORT_DUPLICATE_CATEGORY,
				wizardDesc != null);
			return;
		}
		assertTrue("Could not find category named " + WIZARD_IMPORT_DUPLICATE_CATEGORY, false);
	}

	@Test
	public void testImportUsingExportCategory(){
		IWizardCategory category = importRoot.findCategory(
				IPath.fromOSString(WIZARD_EXPORT_NEW_CATEGORY));
		assertTrue(
				"Import wizards should not have category named " + WIZARD_EXPORT_NEW_CATEGORY,
				category == null);
	}

	/* Export */
	@Test
	public void testExportNoCategoryProvided(){
		IWizardCategory otherCategory = exportRoot.findCategory(IPath.fromOSString(
				WizardsRegistryReader.UNCATEGORIZED_WIZARD_CATEGORY));
		if (otherCategory != null){
			IWizardDescriptor wizardDesc =
				otherCategory.findWizard(WIZARD_ID_EXPORT_NO_CATEGORY);
			assertTrue(
				"Could not find wizard with id" + WIZARD_ID_EXPORT_NO_CATEGORY+ "in Other category.",
				wizardDesc != null);
			return;
		}
		assertTrue("Could not find Other category", false);
	}

	@Test
	public void testExportCategoryDoesNotExist(){
		IWizardCategory otherCategory = exportRoot.findCategory(IPath.fromOSString(
				WizardsRegistryReader.UNCATEGORIZED_WIZARD_CATEGORY));
		if (otherCategory != null){
			IWizardDescriptor wizardDesc =
				otherCategory.findWizard(WIZARD_ID_EXPORT_INVALID_CATEGORY);
			assertTrue(
				"Could not find wizard with id" + WIZARD_ID_EXPORT_INVALID_CATEGORY+ "in Other category.",
				wizardDesc != null);
			return;
		}
		assertTrue("Could not find Other category", false);
	}

	@Test
	public void testExportAddToNewCategory(){
		IWizardCategory newCategory = exportRoot.findCategory(
				IPath.fromOSString(WIZARD_EXPORT_NEW_CATEGORY));
		if (newCategory != null){
			IWizardDescriptor wizardDesc =
				newCategory.findWizard(WIZARD_ID_EXPORT_NEW_CATEGORY);
			assertTrue(
				"Could not find wizard with id" + WIZARD_ID_EXPORT_NEW_CATEGORY+ "in category " + WIZARD_EXPORT_NEW_CATEGORY,
				wizardDesc != null);
			return;
		}
		assertTrue("Could not find category named " + WIZARD_EXPORT_NEW_CATEGORY, false);
	}

	@Test
	public void testExportAddToParentedCategory(){
		IWizardCategory newCategory = exportRoot.findCategory(
				IPath.fromOSString(WIZARD_EXPORT_NEW_PARENTED_CATEGORY));
		if (newCategory != null){
			IWizardDescriptor wizardDesc =
				newCategory.findWizard(WIZARD_ID_EXPORT_PARENTED_CATEGORY);
			assertTrue(
				"Could not find wizard with id" + WIZARD_ID_EXPORT_PARENTED_CATEGORY+ "in category " + WIZARD_EXPORT_NEW_PARENTED_CATEGORY,
				wizardDesc != null);
			return;
		}
		assertTrue("Could not find category named " + WIZARD_EXPORT_NEW_PARENTED_CATEGORY, false);
	}

	@Test
	public void testExportDuplicateCategory(){
		IWizardCategory newCategory = exportRoot.findCategory(
				IPath.fromOSString(WIZARD_EXPORT_DUPLICATE_CATEGORY));
		if (newCategory != null){
			IWizardDescriptor wizardDesc =
				newCategory.findWizard(WIZARD_ID_EXPORT_DUPLICATE_CATEGORY);
			assertTrue(
				"Could not find wizard with id" + WIZARD_ID_EXPORT_DUPLICATE_CATEGORY+ "in category " + WIZARD_EXPORT_DUPLICATE_CATEGORY,
				wizardDesc != null);
			return;
		}
		assertTrue("Could not find category named " + WIZARD_EXPORT_DUPLICATE_CATEGORY, false);
	}

	@Test
	public void testExportUsingImportCategory(){
		IWizardCategory category = exportRoot.findCategory(
				IPath.fromOSString(WIZARD_IMPORT_NEW_CATEGORY));
		assertTrue(
				"Export wizards should not have category named " + WIZARD_IMPORT_NEW_CATEGORY,
				category == null);
	}

	@Override
	protected void doTearDown() throws Exception {
		super.doTearDown();
		importRoot = null;
		exportRoot = null;
	}


}
