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
package org.eclipse.ui.tests.api;

import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.tests.harness.util.ArrayUtil;
import org.eclipse.ui.tests.harness.util.UITestCase;

public class IWorkbenchWindowTest extends UITestCase {

    private IWorkbenchWindow fWin;

    public IWorkbenchWindowTest(String testName) {
        super(testName);
    }

    protected void doSetUp() throws Exception {
        super.doSetUp();
        fWin = openTestWindow();
    }

    public void testClose() throws Throwable {
        assertEquals(fWin.close(), true);
        assertEquals(
                ArrayUtil.contains(fWorkbench.getWorkbenchWindows(), fWin),
                false);
    }

    public void testGetActivePage() throws Throwable {
        /*
         * Commented out because until test case can be updated to work
         * with new window/page/perspective implementation
         * 
         IWorkbenchPage page1, page2;
         page1 = openTestPage(fWin);
         assertEquals(fWin.getActivePage(), page1);

         page2 = openTestPage(fWin);
         assertEquals(fWin.getActivePage(), page2);

         fWin.setActivePage(page1);
         assertEquals(fWin.getActivePage(), page1);

         fWin.setActivePage(page2);
         assertEquals(fWin.getActivePage(), page2);

         //no pages
         closeAllPages(fWin);
         assertNull(fWin.getActivePage());
         */
    }

    public void testSetActivePage() throws Throwable {
        openTestPage(fWin, 5);
        IWorkbenchPage[] pages = fWin.getPages();

        for (int i = 0; i < pages.length; i++) {
            fWin.setActivePage(pages[i]);
            assertEquals(pages[i], fWin.getActivePage());
        }

        fWin.setActivePage(null);
        assertNull(fWin.getActivePage());
    }

    public void testGetPages() throws Throwable {
        /*
         * Commented out because until test case can be updated to work
         * with new window/page/perspective implementation
         * 
         int totalBefore;
         IWorkbenchPage[] pages, domainPages;

         totalBefore = fWin.getPages().length;
         int num = 5;
         pages = openTestPage(fWin, num);
         assertEquals(fWin.getPages().length, totalBefore + num);

         domainPages = fWin.getPages();
         for (int i = 0; i < pages.length; i++)
         assertEquals(ArrayUtil.contains(domainPages, pages[i]), true);

         closeAllPages(fWin);
         assertEquals(fWin.getPages().length, 0);
         */
    }

    public void testGetShell() {
        Shell sh = fWin.getShell();
        assertNotNull(sh);
    }

    public void testGetWorkbench() {
        IWorkbenchWindow win = fWorkbench.getActiveWorkbenchWindow();
        assertEquals(win.getWorkbench(), fWorkbench);
    }

    /**
     * tests openPage(String)
     */
    public void testOpenPage() throws Throwable {
        /*
         * Commented out because until test case can be updated to work
         * with new window/page/perspective implementation
         * 
         IWorkbenchPage page = null;
         try {
         page = fWin.openPage(ResourcesPlugin.getWorkspace());
         assertNotNull(page);
         assertEquals(fWin.getActivePage(), page);
         } finally {
         if (page != null)
         page.close();
         }
         */
    }

    /**
     * tests openPage(String, IAdaptable)
     */
    public void testOpenPage2() throws Throwable {
        /*
         * Commented out because until test case can be updated to work
         * with new window/page/perspective implementation
         * 
         IWorkbenchPage page = null;
         try {
         page = fWin.openPage(EmptyPerspective.PERSP_ID, ResourcesPlugin.getWorkspace());
         assertNotNull(page);
         assertEquals(fWin.getActivePage(), page);
         assertEquals(
         fWin.getActivePage().getPerspective().getId(),
         EmptyPerspective.PERSP_ID);
         } finally {
         if (page != null)
         page.close();
         }

         //test openPage() fails
         try {
         page = fWin.openPage("*************", ResourcesPlugin.getWorkspace());
         fail();
         } catch (WorkbenchException ex) {
         }

         page.close();
         */
    }

    public void testIsApplicationMenu() {
        String[] ids = { IWorkbenchActionConstants.M_FILE,
                IWorkbenchActionConstants.M_WINDOW, };

        for (int i = 0; i < ids.length; i++)
            assertEquals(fWin.isApplicationMenu(ids[i]), true);

        ids = new String[] { IWorkbenchActionConstants.M_EDIT,
                IWorkbenchActionConstants.M_HELP,
                IWorkbenchActionConstants.M_LAUNCH };

        for (int i = 0; i < ids.length; i++)
            assertEquals(fWin.isApplicationMenu(ids[i]), false);
    }
}
