/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.dialogs;

import java.util.List;

import org.eclipse.core.runtime.*;
import org.eclipse.jface.wizard.*;
import org.eclipse.swt.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;
import org.eclipse.ui.dialogs.*;
import org.eclipse.ui.internal.*;
import org.eclipse.ui.internal.dialogs.*;
import org.eclipse.ui.internal.registry.*;
import org.eclipse.ui.tests.util.*;

/**
 * Tests the WorkingSetNewWizard.
 * Tests input validation, presence of type page and correct edit page
 * and wizard page texts.
 */
public class UINewWorkingSetWizardAuto extends UIWorkingSetWizardsAuto {

	public UINewWorkingSetWizardAuto(String name) {
		super(name);
	}
	/**
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		fWizard = new WorkingSetNewWizard();
		super.setUp();
	}
	public void testTypePage() throws Throwable {
		IWizardPage page = fWizardDialog.getCurrentPage();
		assertTrue((page instanceof WorkingSetTypePage) == fWorkingSetDescriptors.length > 1);
		
		/*
		 * Should have at least resourceWorkingSetPage and MockWorkingSet
		 */
		assertTrue(fWorkingSetDescriptors.length >= 2);		
		if (page instanceof WorkingSetTypePage) {
			WorkingSetTypePage typePage = (WorkingSetTypePage) page;
			List widgets = getWidgets((Composite) page.getControl(), Table.class);
			Table table = (Table) widgets.get(0);
			/*
			 * Test initial page state
			 */
			assertEquals(fWorkingSetDescriptors.length, table.getItemCount());
			assertTrue(typePage.canFlipToNextPage() == false);
			assertTrue(fWizard.canFinish() == false);						
			/*
			 * Test page state with page complete input
			 */
			table.setSelection(fWorkingSetDescriptors.length - 1);
			table.notifyListeners(SWT.Selection, new Event());
			assertTrue(typePage.canFlipToNextPage());
			assertTrue(fWizard.canFinish() == false);
			
			/*
			 * Check page texts 
			 */
			DialogCheck.assertDialogTexts(fWizardDialog, this);
		}
	}
	public void testEditPage() throws Throwable {
		WorkingSetRegistry registry = WorkbenchPlugin.getDefault().getWorkingSetRegistry();
		IWizardPage page = fWizardDialog.getCurrentPage();
		IWizardPage defaultEditPage = registry.getDefaultWorkingSetPage();
		String defaultEditPageClassName = defaultEditPage.getClass().getName(); 
		assertTrue((page instanceof WorkingSetTypePage) == fWorkingSetDescriptors.length > 1);

		if (page instanceof WorkingSetTypePage) {
			/*
			 * Select the default (Resource) working set type
			 * and advance to edit page.
			 */
			List widgets = getWidgets((Composite) page.getControl(), Table.class);
			Table table = (Table) widgets.get(0);
			TableItem[] items = table.getItems();
			String workingSetName = null;
			for (int descriptorIndex = 0; descriptorIndex < fWorkingSetDescriptors.length; descriptorIndex++) {
				WorkingSetDescriptor descriptor = fWorkingSetDescriptors[descriptorIndex]; 
				if (defaultEditPageClassName.equals(descriptor.getPageClassName())) {
					workingSetName = descriptor.getName();
					break;
				}
			}
			assertNotNull(workingSetName);
			boolean found  = false;
			for (int i = 0; i < items.length; i++) {
				if (items[i].getText().equals(workingSetName)) {
					table.setSelection(i);
					found = true;
					break;
				}
			}
			assertTrue(found);
			fWizardDialog.showPage(fWizard.getNextPage(page));
		}
		page = fWizardDialog.getCurrentPage();
		assertTrue(page instanceof IWorkingSetPage);

		/*
		 * Verify that correct working set edit page is displayed
		 */
		assertTrue(page.getClass() == defaultEditPage.getClass());
		/*
		 * Test initial page state
		 */
		assertTrue(page.canFlipToNextPage() == false);
		assertTrue(fWizard.canFinish() == false);						
		assertNull(page.getErrorMessage());
		/*
		 * Test page state with partial page input
		 */
		setTextWidgetText(WORKING_SET_NAME_1,page);
		assertTrue(page.canFlipToNextPage() == false);
		assertTrue(fWizard.canFinish() == false);		
		assertNotNull(page.getErrorMessage());		

		/*
		 * Test page state with page complete input
		 */
		checkTreeItems();
		assertTrue(page.canFlipToNextPage() == false);
		assertTrue(fWizard.canFinish());
		assertNull(page.getErrorMessage());
		
		fWizard.performFinish();
		IWorkingSet workingSet = ((WorkingSetNewWizard) fWizard).getSelection();
		IAdaptable[] workingSetItems = workingSet.getElements();
		assertEquals(WORKING_SET_NAME_1, workingSet.getName());
		
		List widgets = getWidgets((Composite) page.getControl(), Tree.class);
		Tree tree = (Tree) widgets.get(0);
		assertEquals(workingSetItems.length, tree.getItemCount());
		assertTrue(ArrayUtil.contains(workingSetItems, p1));
		assertTrue(ArrayUtil.contains(workingSetItems, p2));

		/*
		 * Check page texts 
		 */
		DialogCheck.assertDialogTexts(fWizardDialog, this);
	}
}

