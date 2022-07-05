/*******************************************************************************
 * Copyright (c) 2000, 2019 IBM Corporation and others.
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.dialogs.IWorkingSetPage;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.dialogs.WorkingSetEditWizard;
import org.eclipse.ui.internal.registry.WorkingSetRegistry;
import org.eclipse.ui.tests.harness.util.ArrayUtil;
import org.eclipse.ui.tests.harness.util.DialogCheck;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests the WorkingSetEditWizard
 * Tests input validation, presence of correct edit page and
 * wizard page texts.
 */
@RunWith(JUnit4.class)
public class UIEditWorkingSetWizardAuto extends UIWorkingSetWizardsAuto {
	IWorkingSetPage fDefaultEditPage;

	public UIEditWorkingSetWizardAuto() {
		super(UIEditWorkingSetWizardAuto.class.getSimpleName());
	}

	@Override
	protected void doSetUp() throws Exception {
		WorkingSetRegistry registry = WorkbenchPlugin.getDefault()
				.getWorkingSetRegistry();
		fDefaultEditPage = registry.getDefaultWorkingSetPage();
		fWizard = new WorkingSetEditWizard(fDefaultEditPage);
		super.doSetUp();
	}

	@Test
	public void testEditPage() throws Throwable {
		IWizardPage page = fWizardDialog.getCurrentPage();
		assertTrue(page instanceof IWorkingSetPage);

		/*
		 * Verify that correct working set edit page is displayed
		 */
		assertSame(page.getClass(), fDefaultEditPage.getClass());
		/*
		 * Test initial page state
		 */
		assertFalse(page.canFlipToNextPage());
		assertFalse(fWizard.canFinish());
		assertNull(page.getErrorMessage());
		/*
		 * Test page state with preset page input
		 */
		IWorkingSetManager workingSetManager = fWorkbench
				.getWorkingSetManager();
		IWorkingSet workingSet = workingSetManager.createWorkingSet(
				WORKING_SET_NAME_1, new IAdaptable[] { p1, f2 });
		((WorkingSetEditWizard) fWizard).setSelection(workingSet);

		List<Widget> widgets = getWidgets((Composite) page.getControl(), Text.class);
		Text text = (Text) widgets.get(0);
		assertEquals(WORKING_SET_NAME_1, text.getText());
		assertFalse(page.canFlipToNextPage());
		assertTrue(fWizard.canFinish());
		assertNull(page.getErrorMessage());
		widgets = getWidgets((Composite) page.getControl(), Tree.class);
		Tree tree = (Tree) widgets.get(0);
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		assertEquals(workspace.getRoot().getProjects().length, tree
				.getItemCount());
		setTextWidgetText(WORKING_SET_NAME_2, page);
		assertTrue(fWizard.canFinish());

		/*
		 * Test page state with partial page input
		 */
		setTextWidgetText("", page);
		assertFalse(page.canFlipToNextPage());
		assertFalse(fWizard.canFinish());
		assertNotNull(page.getErrorMessage());

		/*
		 * Test page state with complete page input
		 */
		setTextWidgetText(WORKING_SET_NAME_2, page);
		checkTreeItems();
		assertFalse(page.canFlipToNextPage());
		assertTrue(fWizard.canFinish());
		assertNull(page.getErrorMessage());

		fWizard.performFinish();
		workingSet = ((WorkingSetEditWizard) fWizard).getSelection();
		IAdaptable[] workingSetItems = workingSet.getElements();
		assertEquals(WORKING_SET_NAME_2, workingSet.getName());
		assertTrue(ArrayUtil.contains(workingSetItems, p1));
		assertTrue(ArrayUtil.contains(workingSetItems, p2));

		DialogCheck.assertDialogTexts(fWizardDialog);
	}
}

