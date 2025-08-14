/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
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
import org.eclipse.jface.action.CoolBarManager;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.ICoolBarManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.SubCoolBarManager;
import org.eclipse.jface.action.ToolBarContributionItem;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.IActionBars2;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.internal.WorkbenchWindow;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.tests.api.MockAction;
import org.eclipse.ui.tests.api.MockEditorActionBarContributor;
import org.eclipse.ui.tests.api.MockEditorPart;
import org.eclipse.ui.tests.api.MockViewPart;
import org.eclipse.ui.tests.harness.util.FileUtil;
import org.eclipse.ui.tests.harness.util.UITestCase;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * This class contains tests for the editor action bars
 * implementation.
 */
@RunWith(JUnit4.class)
@Ignore
public class EditorActionBarsTest extends UITestCase {

	protected IWorkbenchWindow fWindow;

	protected IWorkbenchPage fPage;

	private final String EDITOR_ID = "org.eclipse.ui.tests.internal.EditorActionBarsTest";

	/**
	 * Constructor for IEditorPartTest
	 */
	public EditorActionBarsTest() {
		super(EditorActionBarsTest.class.getSimpleName());
	}

	@Override
	protected void doSetUp() throws Exception {
		super.doSetUp();
		fWindow = openTestWindow();
		fPage = fWindow.getActivePage();
	}

	/**
	 * Test action enablement / disablement when a
	 * part is active.
	 * <p>
	 * Created for PR 1GJNB52: ToolItems in EditorToolBarManager can get
	 * out of synch with the state of the IAction
	 * </p>
	 */
	@Test
	public void testActionEnablementWhenActive() throws Throwable {
		// Open an editor.
		MockEditorPart editor = openEditor(fPage, "1");
		MockEditorActionBarContributor contributor = (MockEditorActionBarContributor) editor
				.getEditorSite().getActionBarContributor();

		// Enable all actions.
		contributor.enableActions(true);
		verifyToolItemState(contributor, true);

		// Disable all actions.
		contributor.enableActions(false);
		verifyToolItemState(contributor, false);
	}

	/**
	 * Test action enablement / disablement when a
	 * part is inactive.
	 * <p>
	 * Created for PR 1GJNB52: ToolItems in EditorToolBarManager can get
	 * out of synch with the state of the IAction
	 * </p>
	 */
	@Test
	public void testActionEnablementWhenInactive() throws Throwable {
		// Open an editor.
		MockEditorPart editor = openEditor(fPage, "2");
		MockEditorActionBarContributor contributor = (MockEditorActionBarContributor) editor
				.getEditorSite().getActionBarContributor();

		// Enable all actions.
		contributor.enableActions(true);
		verifyToolItemState(contributor, true);

		// Activate some other part.  Disable the actions.
		// Then activate the editor and test tool item state.
		fPage.showView(MockViewPart.ID);
		contributor.enableActions(false);
		fPage.activate(editor);
		verifyToolItemState(contributor, false);

		// Activate some other part.  Enable the actions.
		// Then activate the editor and test tool item state.
		fPage.showView(MockViewPart.ID);
		contributor.enableActions(true);
		fPage.activate(editor);
		verifyToolItemState(contributor, true);
	}

	@Test
	public void testCoolBarContribution() throws Throwable {

		MockEditorPart editor = openEditor(fPage, "3");
		MockEditorActionBarContributor contributor = (MockEditorActionBarContributor) editor
				.getEditorSite().getActionBarContributor();

		assertTrue(contributor.getActionBars() instanceof IActionBars2);
		IActionBars2 actionBars = (IActionBars2) contributor.getActionBars();

		assertTrue(actionBars.getCoolBarManager() instanceof SubCoolBarManager);
		SubCoolBarManager coolBarManager = (SubCoolBarManager) actionBars.getCoolBarManager();
		assertTrue("Coolbar should be visible", coolBarManager.isVisible());
	}



	/**
	 * Open a test editor.
	 */
	protected MockEditorPart openEditor(IWorkbenchPage page, String suffix)
			throws Throwable {
		IProject proj = FileUtil.createProject("IEditorActionBarsTest");
		IFile file = FileUtil.createFile("test" + suffix + ".txt", proj);
		return (MockEditorPart) page.openEditor(new FileEditorInput(file),
				EDITOR_ID);
	}

	/**
	 * Tests whether actions are enabled.
	 */
	protected void verifyToolItemState(MockEditorActionBarContributor ctr,
			boolean enabled) {
		MockAction[] actions = ctr.getActions();
		for (MockAction action : actions) {
			verifyToolItemState(action, enabled);
		}
	}

	/**
	 * Tests whether an action is enabled.
	 */
	protected void verifyToolItemState(IAction action, boolean enabled) {
		String actionText = action.getText();
		ICoolBarManager tbm = ((WorkbenchWindow) fWindow).getCoolBarManager();
		IContributionItem[] coolItems = tbm.getItems();
		for (IContributionItem coolItem2 : coolItems) {
			if (coolItem2 instanceof ToolBarContributionItem coolItem) {
				IToolBarManager citbm = coolItem.getToolBarManager();
				ToolBar tb = ((ToolBarManager) citbm).getControl();
				verifyNullToolbar(tb, actionText, citbm);
				if (tb != null && !tb.isDisposed()) {
					ToolItem[] items = tb.getItems();
					for (ToolItem item : items) {
						String itemText = item.getToolTipText();
						if (actionText.equals(itemText)) {
							assertEquals(enabled, item.getEnabled());
							return;
						}
					}
				}
			}
		}
		fail("Action for " + actionText + " not found");
	}

	/**
	 * Confirms that a ToolBar is not null when you're looking a manager that
	 * is a CoolItemToolBarManager and it has non-separator/non-invisible
	 * contributions.
	 * This is a consequence of the changes made to
	 * CoolItemToolBarManager.update() that hides the a bar if it does not
	 * contain anything as per the above mentioned criteria.  Under this
	 * circumstance, the underlying ToolBar is not created.
	 *
	 * @param tb the ToolBar to check
	 * @param actionText the action text
	 * @param manager the IToolBarManager containing items
	 * @since 3.0
	 */
	private void verifyNullToolbar(ToolBar tb, String actionText,
			IToolBarManager manager) {
		if (tb == null) { // toolbar should only be null if the given manager is
			// a CoolBarManager and it contains only separators or invisible
			// objects.
			IContributionItem[] items = manager.getItems();
			for (IContributionItem item : items) {
				assertTrue("No toolbar for a visible action text \"" + actionText + "\"",
						item instanceof Separator || !item.isVisible());
			}
		}
	}

	/**
	 * Tests an edge case in cool bar updating when the cool bar has a single separator
	 * and no other contents (or multiple separators and no other contents).
	 * See bug 239945 for details.
	 */
	@Test
	public void test239945() throws Throwable {
		// Test a cool bar with a single separator
		CoolBarManager coolBarManager = new CoolBarManager();
		coolBarManager.add(new Separator(CoolBarManager.USER_SEPARATOR));
		coolBarManager.createControl(fWindow.getShell());
		coolBarManager.update(true);

		// Test a cool bar with multiple separators
		CoolBarManager coolBarManager2 = new CoolBarManager();
		coolBarManager2.add(new Separator(CoolBarManager.USER_SEPARATOR));
		coolBarManager2.createControl(fWindow.getShell());
		coolBarManager2.update(true);
	}
}

