/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.session;

import junit.framework.TestCase;

import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.ViewSite;
import org.eclipse.ui.internal.ViewStack;
import org.eclipse.ui.internal.presentations.PresentablePart;
import org.eclipse.ui.internal.presentations.util.TabbedStackPresentation;
import org.eclipse.ui.presentations.IPresentablePart;

/**
 * Bug 108033 Need a test to ensure that view tab order is the same on start up
 * as it was in the last session.
 * 
 * These tests more or less depend on being run in order. The workspace exists
 * from method to method.
 * 
 * @since 3.2
 * 
 */
public class Bug108033Test extends TestCase {

	public static final String PROBLEM_VIEW_ID = "org.eclipse.ui.views.ProblemView";

	public static final String TASK_VIEW_ID = "org.eclipse.ui.views.TaskList";

	public static final String PROGRESS_VIEW_ID = "org.eclipse.ui.views.ProgressView";

	private IWorkbenchWindow fWin;

	private IWorkbenchPage fActivePage;

	private IWorkbench fWorkbench;

	public Bug108033Test(String testName) {
		super(testName);
	}

	protected void setUp() throws Exception {
		fWorkbench = PlatformUI.getWorkbench();

		fWin = fWorkbench.getActiveWorkbenchWindow();

		fActivePage = fWin.getActivePage();
	}

	public void testShowMultipleViews() throws Throwable {
		assertNotNull(fActivePage.showView(PROGRESS_VIEW_ID));
		assertNotNull(fActivePage.showView(PROBLEM_VIEW_ID));
	}

	public void testCheckMultipleViews() throws Throwable {
		IViewPart problemView = instantiateViews();

		ViewSite site = (ViewSite) problemView.getSite();
		ViewStack stack = (ViewStack) site.getPane().getContainer();

		TabbedStackPresentation pres = (TabbedStackPresentation) stack
				.getTestPresentation();

		verifyOrder(pres, new String[] { "Tasks", "Progress", "Problems" });
		IPresentablePart part = getPresentablePart(site);
		assertNotNull(part);

		pres.moveTab(part, 0);

		verifyOrder(pres, new String[] { "Problems", "Tasks", "Progress" });
	}

	public void testMovedMultipleViews() throws Throwable {
		IViewPart problemView = instantiateViews();

		ViewSite site = (ViewSite) problemView.getSite();
		ViewStack stack = (ViewStack) site.getPane().getContainer();

		TabbedStackPresentation pres = (TabbedStackPresentation) stack
				.getTestPresentation();

		verifyOrder(pres, new String[] { "Problems", "Tasks", "Progress" });
	}

	private IViewPart instantiateViews() {
		IViewReference problemRef = fActivePage
				.findViewReference(PROBLEM_VIEW_ID);
		assertNotNull(problemRef);
		IViewPart problemView = (IViewPart) problemRef.getPart(true);

		// make sure all of the views have been instantiated
		IViewReference stateRef = fActivePage
				.findViewReference(PROGRESS_VIEW_ID);
		assertNotNull(stateRef);
		stateRef.getPart(true);

		IViewReference taskRef = fActivePage.findViewReference(TASK_VIEW_ID);
		assertNotNull(taskRef);
		taskRef.getPart(true);
		return problemView;
	}

	private void verifyOrder(TabbedStackPresentation pres, String[] order) {
		IPresentablePart[] tabs = pres.getPartList();
		assertEquals("Different number of tabs", order.length, tabs.length);
		for (int i = 0; i < tabs.length; ++i) {
			assertEquals("Failed on tab " + i, order[i], tabs[i].getName());
		}
	}

	private IPresentablePart getPresentablePart(ViewSite site) {
		IPresentablePart[] partList = (IPresentablePart[]) ((ViewStack) site
				.getPane().getContainer()).getPresentableParts().toArray(
				new IPresentablePart[0]);
		for (int i = 0; i < partList.length; i++) {
			IPresentablePart part = partList[i];
			if (((PresentablePart) part).getPane() == site.getPane()) {
				return part;
			}
		}
		return null;
	}
}
