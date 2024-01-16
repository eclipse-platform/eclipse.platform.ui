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
package org.eclipse.ui.tests.api;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.tests.harness.util.ActionUtil;
import org.eclipse.ui.tests.harness.util.FileUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests the lifecycle for an editor action delegate.
 */
@RunWith(JUnit4.class)
public class IEditorActionDelegateTest extends IActionDelegateTest {

	public static String EDITOR_ID = "org.eclipse.ui.tests.api.IEditorActionDelegateTest";

	private MockEditorPart editor;

	/**
	 * Constructor for IWorkbenchWindowActionDelegateTest
	 */
	public IEditorActionDelegateTest() {
		super(IEditorActionDelegateTest.class.getSimpleName());
	}

	@Test
	public void testSetActiveEditor() throws Throwable {
		// When an action delegate is run the
		// setActiveEditor, selectionChanged, and run methods should
		// be called, in that order.

		// Run the action.
		testRun();

		// Verify lifecycle.
		MockActionDelegate delegate = getDelegate();
		assertNotNull(delegate);
		assertTrue(delegate.callHistory.verifyOrder(new String[] {
				"setActiveEditor", "selectionChanged", "run" }));
	}

	@Override
	protected Object createActionWidget() throws Throwable {
		editor = openEditor(fPage, "X");
		return editor;
	}

	@Override
	protected void runAction(Object widget) throws Throwable {
		MockEditorPart editor = (MockEditorPart) widget;
		MockEditorActionBarContributor contributor = (MockEditorActionBarContributor) editor
				.getEditorSite().getActionBarContributor();
		IMenuManager mgr = contributor.getActionBars().getMenuManager();
		ActionUtil.runActionWithLabel(this, mgr, "Mock Action");
	}

	@Override
	protected void fireSelection(Object widget) throws Throwable {
		MockEditorPart editor = (MockEditorPart) widget;
		editor.fireSelection();
	}

	protected MockEditorPart openEditor(IWorkbenchPage page, String suffix)
			throws Throwable {
		IProject proj = FileUtil.createProject("IEditorActionDelegateTest");
		IFile file = FileUtil.createFile("test" + suffix + ".txt", proj);
		return (MockEditorPart) page.openEditor(new FileEditorInput(file),
				EDITOR_ID);
	}

}

