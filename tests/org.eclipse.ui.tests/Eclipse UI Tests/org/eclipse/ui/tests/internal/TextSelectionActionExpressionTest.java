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
package org.eclipse.ui.tests.internal;

import static org.eclipse.ui.tests.harness.util.UITestUtil.openTestWindow;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.SubContributionItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.internal.WorkbenchWindow;
import org.eclipse.ui.tests.harness.util.ActionUtil;
import org.eclipse.ui.tests.harness.util.FileUtil;
import org.eclipse.ui.tests.harness.util.UITestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * This class contains tests for text selection action enablement
 */
@RunWith(JUnit4.class)
public class TextSelectionActionExpressionTest extends UITestCase {
	protected IWorkbenchWindow fWindow;

	protected IWorkbenchPage fPage;

	public TextSelectionActionExpressionTest() {
		super(TextSelectionActionExpressionTest.class.getSimpleName());
	}

	@Override
	protected void doSetUp() throws Exception {
		super.doSetUp();
		fWindow = openTestWindow();
		fPage = fWindow.getActivePage();
	}

	@Test
	public void testAnyTextAction() throws Throwable {
		// Setup.
		ExtendedTextEditor editor = showTextEditor("anyText.exttxt");
		MenuManager mgr = getActionMenuManager();

		// Test empty selection.
		selectAndUpdateMenu(editor, "", mgr);
		testAction(mgr, "anyText", true);

		// Test full selection.
		selectAndUpdateMenu(editor, "bob", mgr);
		testAction(mgr, "anyText", true);

		// Activate another view.
		fPage.showView(IPageLayout.ID_BOOKMARKS);
		testAction(mgr, "anyText", false);

		// Activate editor.
		// Test old selection.
		fPage.activate(editor);
		testAction(mgr, "anyText", true);

		// Test empty selection.
		selectAndUpdateMenu(editor, "", mgr);
		testAction(mgr, "anyText", true);
	}

	@Test
	public void testEmptyTextAction() throws Throwable {
		// Setup.
		ExtendedTextEditor editor = showTextEditor("emptyText.exttxt");
		MenuManager mgr = getActionMenuManager();

		// Test empty selection.
		selectAndUpdateMenu(editor, "", mgr);
		testAction(mgr, "emptyText", true);

		// Test full selection.
		selectAndUpdateMenu(editor, "bob", mgr);
		testAction(mgr, "emptyText", false);

		// Activate another view.
		fPage.showView(IPageLayout.ID_BOOKMARKS);
		testAction(mgr, "emptyText", false);

		// Activate editor.
		// Test old selection.
		fPage.activate(editor);
		testAction(mgr, "emptyText", false);

		// Test empty selection.
		selectAndUpdateMenu(editor, "", mgr);
		testAction(mgr, "emptyText", true);
	}

	@Test
	public void testHelloTextAction() throws Throwable {
		// Setup.
		ExtendedTextEditor editor = showTextEditor("helloText.exttxt");
		MenuManager mgr = getActionMenuManager();

		// Test empty selection.
		selectAndUpdateMenu(editor, "", mgr);
		testAction(mgr, "helloText", false);

		// Test wrong selection.
		selectAndUpdateMenu(editor, "bob", mgr);
		testAction(mgr, "helloText", false);

		// Test right selection.
		selectAndUpdateMenu(editor, "Hello", mgr);
		testAction(mgr, "helloText", true);

		// Activate another view.
		fPage.showView(IPageLayout.ID_BOOKMARKS);
		testAction(mgr, "helloText", false);

		// Activate editor.
		// Test old selection.
		fPage.activate(editor);
		testAction(mgr, "helloText", true);

		// Test wrong selection.
		selectAndUpdateMenu(editor, "bob", mgr);
		testAction(mgr, "helloText", false);
	}

	/**
	 * Creates the list view.
	 */
	private ExtendedTextEditor showTextEditor(String fileName) throws Throwable {
		IProject proj = FileUtil
				.createProject("TextSelectionActionExpressionTest");
		IFile file = FileUtil.createFile(fileName, proj);
		return (ExtendedTextEditor) IDE.openEditor(fPage, file, true);
	}

	/**
	 * Select an object and simulate show.
	 */
	private void selectAndUpdateMenu(ExtendedTextEditor editor, String str,
			MenuManager mgr) throws Throwable {
		editor.setText(str);
		fPage.saveEditor(editor, false);
		mgr.getMenu().notifyListeners(SWT.Show, new Event());
	}

	/**
	 * Returns the menu manager containing the actions.
	 */
	private MenuManager getActionMenuManager() throws Throwable {
		fPage.showActionSet("org.eclipse.ui.tests.internal.TextSelectionActions");
		WorkbenchWindow win = (WorkbenchWindow) fWindow;
		IContributionItem item = win.getMenuBarManager().find(
				"org.eclipse.ui.tests.internal.TextSelectionMenu");
		while (item instanceof SubContributionItem) {
			item = ((SubContributionItem) item).getInnerItem();
			if (item instanceof MenuManager mm) {
				return mm;
			}
		}
		fail("Unable to find menu manager");
		return null;
	}

	/**
	 * Tests the enablement / visibility of an action.
	 */
	private void testAction(MenuManager mgr, String action, boolean expected)
			throws Throwable {
		assertEquals(action, expected, ActionUtil.getActionWithLabel(mgr,
				action).isEnabled());
	}
}
