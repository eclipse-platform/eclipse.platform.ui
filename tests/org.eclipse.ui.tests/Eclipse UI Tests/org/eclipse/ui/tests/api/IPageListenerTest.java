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
package org.eclipse.ui.tests.api;

import static org.eclipse.ui.tests.harness.util.UITestUtil.openTestWindow;
import static org.junit.Assert.assertEquals;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.ui.IPageListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.tests.harness.util.CloseTestWindowsRule;
import org.eclipse.ui.tests.harness.util.EmptyPerspective;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

/**
 * Tests the IPageListener class.
 */
public class IPageListenerTest implements IPageListener {

	@Rule
	public final CloseTestWindowsRule closeTestWindows = new CloseTestWindowsRule();

	private IWorkbenchWindow fWindow;
	private IWorkspace fWorkspace;

	private int eventsReceived = 0;

	final private int OPEN = 0x01;

	final private int CLOSE = 0x02;

	final private int ACTIVATE = 0x04;

	private IWorkbenchPage pageMask;

	@Before
	public final void setUp() throws Exception {
		fWindow = openTestWindow();
		fWorkspace = ResourcesPlugin.getWorkspace();
		fWindow.addPageListener(this);
	}

	@After
	public final void tearDown() throws Exception {
		fWindow.removePageListener(this);
	}

	/**
	 * Tests the pageOpened method.
	 */
	@Test
	@Ignore
	public void testPageOpened() throws Throwable {
		/*
		 * Commented out because until test case can be updated to work with new
		 * window/page/perspective implementation
		 */
		 // From Javadoc: "Notifies this listener that the given page has been opened."

		 // Test open page.
		 eventsReceived = 0;
		 IWorkbenchPage page = fWindow.openPage(EmptyPerspective.PERSP_ID,
		 fWorkspace);
		 assertEquals(eventsReceived, OPEN|ACTIVATE);

		 // Close page.
		 page.close();
	}

	/**
	 * Tests the pageClosed method.
	 */
	@Test
	@Ignore
	public void testPageClosed() throws Throwable {
		/*
		 * Commented out because until test case can be updated to work
		 * with new window/page/perspective implementation
		 */
		 // From Javadoc: "Notifies this listener that the given page has been closed."

		 // Open page.
		 IWorkbenchPage page = fWindow.openPage(EmptyPerspective.PERSP_ID,
		 fWorkspace);

		 // Test close page.
		 eventsReceived = 0;
		 pageMask = page;
		 page.close();
		 assertEquals(eventsReceived, CLOSE);
	}

	/**
	 * Tests the pageActivated method.
	 */
	@Test
	@Ignore
	public void testPageActivate() throws Throwable {
		/*
		 * Commented out because until test case can be updated to work
		 * with new window/page/perspective implementation
		 */
		 // From Javadoc: "Notifies this listener that the given page has been activated."

		 // Add pages.
		 IWorkbenchPage page1 = fWindow.openPage(EmptyPerspective.PERSP_ID,
		 fWorkspace);
		 IWorkbenchPage page2 = fWindow.openPage(EmptyPerspective.PERSP_ID,
		 fWorkspace);

		 // Test activation of page 1.
		 eventsReceived = 0;
		 pageMask = page1;
		 fWindow.setActivePage(page1);
		 assertEquals(eventsReceived, ACTIVATE);

		 // Test activation of page 2.
		 eventsReceived = 0;
		 pageMask = page2;
		 fWindow.setActivePage(page2);
		 assertEquals(eventsReceived, ACTIVATE);

		 // Cleanup.
		 page1.close();
		 page2.close();
	}

	/**
	 * @see IPageListener#pageActivated(IWorkbenchPage)
	 */
	@Override
	public void pageActivated(IWorkbenchPage page) {
		if (pageMask == null || page == pageMask) {
			eventsReceived = eventsReceived | ACTIVATE;
		}
	}

	/**
	 * @see IPageListener#pageClosed(IWorkbenchPage)
	 */
	@Override
	public void pageClosed(IWorkbenchPage page) {
		if (pageMask == null || page == pageMask) {
			eventsReceived = eventsReceived | CLOSE;
		}
	}

	/**
	 * @see IPageListener#pageOpened(IWorkbenchPage)
	 */
	@Override
	public void pageOpened(IWorkbenchPage page) {
		if (pageMask == null || page == pageMask) {
			eventsReceived |= OPEN;
		}
	}

}
