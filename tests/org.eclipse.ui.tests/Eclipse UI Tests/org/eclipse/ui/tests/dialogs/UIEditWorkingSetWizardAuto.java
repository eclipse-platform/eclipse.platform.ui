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

import static org.eclipse.core.resources.ResourcesPlugin.getWorkspace;
import static org.eclipse.ui.PlatformUI.getWorkbench;

import java.util.List;

import org.eclipse.core.internal.resources.Workspace;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceDescription;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
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
public class UIEditWorkingSetWizardAuto extends UIWorkingSetWizardsAuto<WorkingSetEditWizard> {

	public UIEditWorkingSetWizardAuto() {
		super(UIEditWorkingSetWizardAuto.class.getSimpleName());
	}

	@Override
	protected WorkingSetEditWizard createWizardToTest() {
		return new WorkingSetEditWizard(getDefaultEditPage());
	}

	private IWorkingSetPage getDefaultEditPage() {
		WorkingSetRegistry registry = WorkbenchPlugin.getDefault().getWorkingSetRegistry();
		return registry.getDefaultWorkingSetPage();
	}

	/**
	 * Blocks the calling thread until autobuild completes.
	 */
	private static void waitForBuild() {
		((Workspace) getWorkspace()).getBuildManager().waitForAutoBuild();
	}

	/**
	 * Enables or disables workspace autobuild. Waits for the build to be finished,
	 * even if the autobuild value did not change and a previous build is still
	 * running.
	 */
	private static void setAutoBuilding(boolean enabled) throws CoreException {
		IWorkspace workspace = getWorkspace();
		if (workspace.isAutoBuilding() != enabled) {
			IWorkspaceDescription description = workspace.getDescription();
			description.setAutoBuilding(enabled);
			workspace.setDescription(description);
		}
		waitForBuild();
	}


	@Test
	public void testEditPage() throws Throwable {
		IWizardPage page = getWizardDialog().getCurrentPage();
		assertTrue(page instanceof IWorkingSetPage);

		/*
		 * Verify that correct working set edit page is displayed
		 */
		assertSame(page.getClass(), getDefaultEditPage().getClass());
		/*
		 * Test initial page state
		 */
		assertFalse(page.canFlipToNextPage());
		assertFalse(getWizard().canFinish());
		assertNull(page.getErrorMessage());
		/*
		 * Test page state with preset page input
		 */
		IWorkingSetManager workingSetManager = getWorkbench().getWorkingSetManager();
		IWorkingSet workingSet = workingSetManager.createWorkingSet(WORKING_SET_NAME_1,
				new IAdaptable[] { getProject1(), getFileInProject2() });
		getWizard().setSelection(workingSet);

		List<Widget> widgets = getWidgets((Composite) page.getControl(), Text.class);
		Text text = (Text) widgets.get(0);
		assertEquals(WORKING_SET_NAME_1, text.getText());
		assertFalse(page.canFlipToNextPage());
		assertTrue(getWizard().canFinish());
		assertNull(page.getErrorMessage());
		widgets = getWidgets((Composite) page.getControl(), Tree.class);
		Tree tree = (Tree) widgets.get(0);
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		assertEquals(workspace.getRoot().getProjects().length, tree.getItemCount());
		setTextWidgetText(WORKING_SET_NAME_2, page);
		assertTrue(getWizard().canFinish());

		/*
		 * Test page state with partial page input
		 */
		setTextWidgetText("", page);
		assertFalse(page.canFlipToNextPage());
		assertFalse(getWizard().canFinish());
		assertNotNull(page.getErrorMessage());

		/*
		 * Test page state with complete page input
		 */
		setTextWidgetText(WORKING_SET_NAME_2, page);
		checkTreeItems();
		assertFalse(page.canFlipToNextPage());
		assertTrue(getWizard().canFinish());
		assertNull(page.getErrorMessage());

		getWizard().performFinish();
		workingSet = getWizard().getSelection();
		IAdaptable[] workingSetItems = workingSet.getElements();
		assertEquals(WORKING_SET_NAME_2, workingSet.getName());
		assertTrue(ArrayUtil.contains(workingSetItems, getProject1()));
		assertTrue(ArrayUtil.contains(workingSetItems, getProject2()));

		DialogCheck.assertDialogTexts(getWizardDialog());
	}

	@Override
	public void doSetUp() throws Exception {
		super.doSetUp();
		setAutoBuilding(false);
	}

	@Override
	public void doTearDown() throws Exception {
		super.doTearDown();
		ResourcesPlugin.getWorkspace().setDescription(Workspace.defaultWorkspaceDescription());
	}

}
