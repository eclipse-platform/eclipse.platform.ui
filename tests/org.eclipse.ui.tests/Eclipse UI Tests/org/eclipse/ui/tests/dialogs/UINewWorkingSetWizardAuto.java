/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.dialogs;

import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.dialogs.IWorkingSetPage;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.dialogs.WorkingSetNewWizard;
import org.eclipse.ui.internal.dialogs.WorkingSetTypePage;
import org.eclipse.ui.internal.registry.WorkingSetDescriptor;
import org.eclipse.ui.internal.registry.WorkingSetRegistry;
import org.eclipse.ui.tests.harness.util.ArrayUtil;
import org.eclipse.ui.tests.harness.util.DialogCheck;

/**
 * Tests the WorkingSetNewWizard.
 * Tests input validation, presence of type page and correct edit page
 * and wizard page texts.
 */
public class UINewWorkingSetWizardAuto extends UIWorkingSetWizardsAuto {

    public UINewWorkingSetWizardAuto(String name) {
        super(name);
    }

    @Override
	protected void doSetUp() throws Exception {
        fWizard = getWorkbench().getWorkingSetManager().createWorkingSetNewWizard(null);
        super.doSetUp();
    }

    public void testTypePage() throws Throwable {
        IWizardPage page = fWizardDialog.getCurrentPage();
        WorkingSetDescriptor[] descriptors = getEditableWorkingSetDescriptors();
        
        // the first page must be the type selection page iff there is more than one working set type
        assertEquals(descriptors.length > 1, (page instanceof WorkingSetTypePage));

        /*
         * Should have at least resourceWorkingSetPage and MockWorkingSet
         */
        assertTrue(descriptors.length >= 2);
        if (page instanceof WorkingSetTypePage) {
            WorkingSetTypePage typePage = (WorkingSetTypePage) page;
            List widgets = getWidgets((Composite) page.getControl(),
                    Table.class);
            Table table = (Table) widgets.get(0);
            /*
             * Test initial page state
             */
            assertEquals(descriptors.length, table.getItemCount());
            assertTrue(typePage.canFlipToNextPage() == false);
            assertTrue(fWizard.canFinish() == false);
            /*
             * Test page state with page complete input
             */
            table.setSelection(descriptors.length - 1);
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
        WorkingSetRegistry registry = WorkbenchPlugin.getDefault()
                .getWorkingSetRegistry();
        IWizardPage page = fWizardDialog.getCurrentPage();
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
            List widgets = getWidgets((Composite) page.getControl(),
                    Table.class);
            Table table = (Table) widgets.get(0);
            TableItem[] items = table.getItems();
            String workingSetName = null;
            for (int descriptorIndex = 0; descriptorIndex < descriptors.length; descriptorIndex++) {
                WorkingSetDescriptor descriptor = descriptors[descriptorIndex];
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
        assertFalse(page.canFlipToNextPage());
        assertFalse(fWizard.canFinish());
        assertNull(page.getErrorMessage());
        assertNull(page.getMessage());
        
        /*
         * Test page state with partial page input
         */
        setTextWidgetText(WORKING_SET_NAME_1, page);
        assertFalse(page.canFlipToNextPage());
        assertTrue(fWizard.canFinish());  // allow for empty sets
        assertNull(page.getErrorMessage());
        assertNotNull(page.getMessage());

        /*
         * Test page state with page complete input
         */
        checkTreeItems();
        assertFalse(page.canFlipToNextPage());
        assertTrue(fWizard.canFinish());
        assertNull(page.getErrorMessage());
        assertNull(page.getMessage());

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

