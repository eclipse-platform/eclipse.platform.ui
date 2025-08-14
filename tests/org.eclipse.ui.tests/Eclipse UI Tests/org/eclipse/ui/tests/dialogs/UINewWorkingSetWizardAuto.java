/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
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
package org.eclipse.ui.tests.dialogs;

import static org.eclipse.ui.PlatformUI.getWorkbench;

import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.dialogs.IWorkingSetNewWizard;
import org.eclipse.ui.dialogs.IWorkingSetPage;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.dialogs.WorkingSetNewWizard;
import org.eclipse.ui.internal.dialogs.WorkingSetTypePage;
import org.eclipse.ui.internal.registry.WorkingSetDescriptor;
import org.eclipse.ui.internal.registry.WorkingSetRegistry;
import org.eclipse.ui.tests.harness.util.ArrayUtil;
import org.eclipse.ui.tests.harness.util.DialogCheck;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests the WorkingSetNewWizard.
 * Tests input validation, presence of type page and correct edit page
 * and wizard page texts.
 */
@RunWith(JUnit4.class)
public class UINewWorkingSetWizardAuto extends UIWorkingSetWizardsAuto<IWorkingSetNewWizard> {

	public UINewWorkingSetWizardAuto() {
		super(UINewWorkingSetWizardAuto.class.getSimpleName());
	}

	@Override
	protected IWorkingSetNewWizard createWizardToTest() {
		return getWorkbench().getWorkingSetManager().createWorkingSetNewWizard(null);
	}

	@Test
	public void testTypePage() throws Throwable {
		IWizardPage page = getWizardDialog().getCurrentPage();
		WorkingSetDescriptor[] descriptors = getEditableWorkingSetDescriptors();

		// the first page must be the type selection page iff there is more than one working set type
		assertEquals(descriptors.length > 1, (page instanceof WorkingSetTypePage));

		/*
		 * Should have at least resourceWorkingSetPage and MockWorkingSet
		 */
		assertTrue(descriptors.length >= 2);
		if (page instanceof WorkingSetTypePage typePage) {
			List<Widget> widgets = getWidgets((Composite) page.getControl(),
					Table.class);
			Table table = (Table) widgets.get(0);
			/*
			 * Test initial page state
			 */
			assertEquals(descriptors.length, table.getItemCount());
			assertFalse(typePage.canFlipToNextPage());
			assertFalse(getWizard().canFinish());
			/*
			 * Test page state with page complete input
			 */
			table.setSelection(descriptors.length - 1);
			table.notifyListeners(SWT.Selection, new Event());
			assertTrue(typePage.canFlipToNextPage());
			assertFalse(getWizard().canFinish());

			/*
			 * Check page texts
			 */
			DialogCheck.assertDialogTexts(getWizardDialog());
		}
	}

	@Test
	public void testEditPage() throws Throwable {
		WorkingSetRegistry registry = WorkbenchPlugin.getDefault()
				.getWorkingSetRegistry();
		IWizardPage page = getWizardDialog().getCurrentPage();
		IWizardPage defaultEditPage = registry.getDefaultWorkingSetPage();
		String defaultEditPageClassName = defaultEditPage.getClass().getName();
		WorkingSetDescriptor[] descriptors = getEditableWorkingSetDescriptors();

		// the first page must be the type selection page iff there is more than one working set type
		assertEquals(descriptors.length > 1, (page instanceof WorkingSetTypePage));

		if (page instanceof WorkingSetTypePage) {
			/*
			 * Select the default (Resource) working set type
			 * and advance to edit page.
			 */
			List<Widget> widgets = getWidgets((Composite) page.getControl(),
					Table.class);
			Table table = (Table) widgets.get(0);
			TableItem[] items = table.getItems();
			String workingSetName = null;
			for (WorkingSetDescriptor descriptor : descriptors) {
				if (defaultEditPageClassName.equals(descriptor
						.getPageClassName())) {
					workingSetName = descriptor.getName();
					break;
				}
			}
			assertNotNull(workingSetName);
			boolean found = false;
			for (int i = 0; i < items.length; i++) {
				if (items[i].getText().equals(workingSetName)) {
					table.setSelection(i);
					found = true;
					break;
				}
			}
			assertTrue(found);
			getWizardDialog().showPage(getWizard().getNextPage(page));
		}
		page = getWizardDialog().getCurrentPage();
		assertTrue(page instanceof IWorkingSetPage);

		/*
		 * Verify that correct working set edit page is displayed
		 */
		assertTrue(page.getClass() == defaultEditPage.getClass());
		/*
		 * Test initial page state
		 */
		assertFalse(page.canFlipToNextPage());
		assertFalse(getWizard().canFinish());
		assertNull(page.getErrorMessage());
		assertNull(page.getMessage());

		/*
		 * Test page state with partial page input
		 */
		setTextWidgetText(WORKING_SET_NAME_1, page);
		assertFalse(page.canFlipToNextPage());
		assertTrue(getWizard().canFinish());  // allow for empty sets
		assertNull(page.getErrorMessage());
		assertNotNull(page.getMessage());

		/*
		 * Test page state with page complete input
		 */
		checkTreeItems();
		assertFalse(page.canFlipToNextPage());
		assertTrue(getWizard().canFinish());
		assertNull(page.getErrorMessage());
		assertNull(page.getMessage());

		getWizard().performFinish();
		IWorkingSet workingSet = ((WorkingSetNewWizard) getWizard()).getSelection();
		IAdaptable[] workingSetItems = workingSet.getElements();
		assertEquals(WORKING_SET_NAME_1, workingSet.getName());

		List<Widget> widgets = getWidgets((Composite) page.getControl(), Tree.class);
		Tree tree = (Tree) widgets.get(0);
		assertEquals(workingSetItems.length, tree.getItemCount());
		assertTrue(ArrayUtil.contains(workingSetItems, getProject1()));
		assertTrue(ArrayUtil.contains(workingSetItems, getProject2()));

		/*
		 * Check page texts
		 */
		DialogCheck.assertDialogTexts(getWizardDialog());
	}
}

