/*******************************************************************************
 * Copyright (c) 2000, 2025 IBM Corporation and others.
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

import static org.eclipse.ui.tests.harness.util.UITestUtil.closeAllPages;
import static org.eclipse.ui.tests.harness.util.UITestUtil.openTestPage;
import static org.eclipse.ui.tests.harness.util.UITestUtil.openTestWindow;
import static org.junit.Assert.assertThrows;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.action.StatusLineManager;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.internal.WorkbenchWindow;
import org.eclipse.ui.tests.harness.util.ArrayUtil;
import org.eclipse.ui.tests.harness.util.EmptyPerspective;
import org.eclipse.ui.tests.harness.util.UITestCase;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class IWorkbenchWindowTest extends UITestCase {

	private IWorkbench fWorkbench;

	private IWorkbenchWindow fWin;

	public IWorkbenchWindowTest() {
		super(IWorkbenchWindowTest.class.getSimpleName());
	}

	@Override
	protected void doSetUp() throws Exception {
		super.doSetUp();
		fWorkbench = PlatformUI.getWorkbench();
		fWin = openTestWindow();
	}

	@Test
	public void testClose() throws Throwable {
		assertEquals(fWin.close(), true);
		assertEquals(
				ArrayUtil.contains(fWorkbench.getWorkbenchWindows(), fWin),
				false);
	}

	@Test
	@Ignore
	public void testGetActivePage() throws Throwable {
		/*
		 * Commented out because until test case can be updated to work
		 * with new window/page/perspective implementation
		 */
		IWorkbenchPage page1, page2;
		page1 = openTestPage(fWin);
		assertEquals(fWin.getActivePage(), page1);

		page2 = openTestPage(fWin);
		assertEquals(fWin.getActivePage(), page2);

		fWin.setActivePage(page1);
		assertEquals(fWin.getActivePage(), page1);

		fWin.setActivePage(page2);
		assertEquals(fWin.getActivePage(), page2);

		// no pages
		closeAllPages(fWin);
		assertNull(fWin.getActivePage());
	}

	@Test
	@Ignore
	public void XXXtestSetActivePage() throws Throwable {
		openTestPage(fWin, 5);
		IWorkbenchPage[] pages = fWin.getPages();

		for (IWorkbenchPage page : pages) {
			fWin.setActivePage(page);
			assertEquals(page, fWin.getActivePage());
		}

		fWin.setActivePage(null);
		assertNull(fWin.getActivePage());
	}

	@Test
	@Ignore
	public void testGetPages() throws Throwable {
		/*
		 * Commented out because until test case can be updated to work
		 * with new window/page/perspective implementation
		 */
		int totalBefore;
		IWorkbenchPage[] pages, domainPages;

		totalBefore = fWin.getPages().length;
		int num = 5;
		pages = openTestPage(fWin, num);
		assertEquals(fWin.getPages().length, totalBefore + num);

		domainPages = fWin.getPages();
		for (IWorkbenchPage page : pages) {
			assertEquals(ArrayUtil.contains(domainPages, page), true);
		}

		closeAllPages(fWin);
		assertEquals(fWin.getPages().length, 0);
	}

	@Test
	public void testGetShell() {
		Shell sh = fWin.getShell();
		assertNotNull(sh);
	}

	@Test
	public void testGetWorkbench() {
		IWorkbenchWindow win = fWorkbench.getActiveWorkbenchWindow();
		assertEquals(win.getWorkbench(), fWorkbench);
	}

	/**
	 * tests openPage(String)
	 */
	@Test
	@Ignore
	public void testOpenPage() throws Throwable {
		/*
		 * Commented out because until test case can be updated to work
		 * with new window/page/perspective implementation
		 */
		IWorkbenchPage page = null;
		try {
			page = fWin.openPage(ResourcesPlugin.getWorkspace());
			assertNotNull(page);
			assertEquals(fWin.getActivePage(), page);
		} finally {
			if (page != null) {
				page.close();
			}
		}
	}

	/**
	 * tests openPage(String, IAdaptable)
	 */
	@Test
	@Ignore
	public void testOpenPage2() throws Throwable {
		/*
		 * Commented out because until test case can be updated to work
		 * with new window/page/perspective implementation
		 */
		IWorkbenchPage page = null;
		try {
			page = fWin.openPage(EmptyPerspective.PERSP_ID, ResourcesPlugin.getWorkspace());
			assertNotNull(page);
			assertEquals(fWin.getActivePage(), page);
			assertEquals(fWin.getActivePage().getPerspective().getId(), EmptyPerspective.PERSP_ID);
		} finally {
			if (page != null) {
				page.close();
			}
		}

		// test openPage() fails
		assertThrows(WorkbenchException.class, () -> {
			IWorkbenchPage p = fWin.openPage("*************", ResourcesPlugin.getWorkspace());
			p.close();
		});
	}

	@Test
	public void testIsApplicationMenu() {
		String[] ids = { IWorkbenchActionConstants.M_FILE,
				IWorkbenchActionConstants.M_WINDOW, };

		for (String id : ids) {
			assertEquals(fWin.isApplicationMenu(id), true);
		}

		ids = new String[] { IWorkbenchActionConstants.M_EDIT,
				IWorkbenchActionConstants.M_HELP,
				IWorkbenchActionConstants.M_LAUNCH };

		for (String id : ids) {
			assertEquals(fWin.isApplicationMenu(id), false);
		}
	}

	@Test
	public void testRunJobInStatusLine() throws Throwable {
		fWin.run(false, false, monitor -> {
			monitor.beginTask("Task", 1);
			assertStatusText("Task");
		});
		assertStatusText("");
	}

	@Test
	public void testRunJobInStatusLineWithSubtasks() throws Throwable{
		fWin.run(false, false, monitor -> {
			monitor.beginTask("Task", 1);
			assertStatusText("Task");
			monitor.subTask("SubTask");
			assertStatusText("Task: SubTask");
			monitor.subTask("OtherSubTask");
			assertStatusText("Task: OtherSubTask");
		});
		assertStatusText("");
	}

	private void assertStatusText(String text) {
		StatusLineManager statusManager = ((WorkbenchWindow) fWin).getStatusLineManager();
		Composite statusLine = (Composite) statusManager.getControl();
		CLabel statusLabel = (CLabel) statusLine.getChildren()[0];
		assertEquals("Status line was not updated.", text, statusLabel.getText());
	}
}
