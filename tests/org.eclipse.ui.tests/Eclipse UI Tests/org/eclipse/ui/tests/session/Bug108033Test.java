/*******************************************************************************
 * Copyright (c) 2004, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.session;

import java.util.List;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.e4.ui.model.application.ui.MElementContainer;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.MUILabel;
import org.eclipse.e4.ui.model.application.ui.advanced.MPlaceholder;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.ViewSite;

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

	public static TestSuite suite() {
		TestSuite ts = new TestSuite("org.eclipse.ui.tests.session.Bug108033Test");
		ts.addTest(new Bug108033Test("testShowMultipleViews"));
		ts.addTest(new Bug108033Test("testCheckMultipleViews"));
		ts.addTest(new Bug108033Test("testMovedMultipleViews"));
		return ts;
	}

	public static final String PROBLEM_VIEW_ID = "org.eclipse.ui.views.ProblemView";

	public static final String TASK_VIEW_ID = "org.eclipse.ui.views.TaskList";

	public static final String PROGRESS_VIEW_ID = "org.eclipse.ui.views.ProgressView";

	private static String RESOURCE_ID = "org.eclipse.ui.resourcePerspective";

	private IWorkbenchWindow fWin;

	private IWorkbenchPage fActivePage;

	private IWorkbench fWorkbench;

	public Bug108033Test(String testName) {
		super(testName);
	}

	@Override
	protected void setUp() throws Exception {
		fWorkbench = PlatformUI.getWorkbench();

		fWin = fWorkbench.getActiveWorkbenchWindow();

		fActivePage = fWin.getActivePage();
	}

	/**
	 * Make sure the perspective has been reset, and then show the views in the
	 * expected order. These tests depend on being run in order in the same
	 * environment, so we can't use the standard openWindow() to protect
	 * ourselves from side effects.
	 *
	 * @throws Throwable
	 *             an error
	 */
	public void testShowMultipleViews() throws Throwable {
		IPerspectiveDescriptor desc = fActivePage.getWorkbenchWindow()
				.getWorkbench().getPerspectiveRegistry().findPerspectiveWithId(
						RESOURCE_ID);
		fActivePage.setPerspective(desc);
		fActivePage.resetPerspective();
		assertNotNull(fActivePage.showView(TASK_VIEW_ID));
		assertNotNull(fActivePage.showView(PROGRESS_VIEW_ID));
		assertNotNull(fActivePage.showView(PROBLEM_VIEW_ID));
	}

	/**
	 * Check the views are still in the correct order, then move the problems
	 * view to the first tab.
	 *
	 * @throws Throwable
	 *             an error
	 */
	public void testCheckMultipleViews() throws Throwable {
		IViewPart problemView = instantiateViews();

		ViewSite site = (ViewSite) problemView.getSite();
		MElementContainer<MUIElement> stack = getParent(site.getModel());

		verifyOrder(stack, new String[] { "Tasks", "Progress", "Problems" });
		moveTab(stack, problemView, 0);

		verifyOrder(stack, new String[] { "Problems", "Tasks", "Progress" });
	}

	// TBD should this be in the ModelService or PartService?
	private MElementContainer<MUIElement> getParent(MUIElement element) {
		MElementContainer<MUIElement> parent = element.getParent();
		if (parent != null) {
			return parent;
		}
		MPlaceholder placeholder = element.getCurSharedRef();
		if (placeholder != null) {
			return placeholder.getParent();
		}
		return null;
	}

	private void moveTab(MElementContainer<MUIElement> stack, IViewPart viewPart, int indexTo) {
		ViewSite site = (ViewSite) viewPart.getSite();
		MPart part = site.getModel();
		List<MUIElement> children = stack.getChildren();
		int indexFrom = children.indexOf(part);
		assertTrue(indexFrom >= 0);
		children.remove(part);
		children.add(indexTo, part);
		stack.setSelectedElement(part);
	}

	/**
	 * Verify the views are ordered with the problems view first after the
	 * restart.
	 *
	 * @throws Throwable
	 *             an error
	 */
	public void testMovedMultipleViews() throws Throwable {
		IViewPart problemView = instantiateViews();

		ViewSite site = (ViewSite) problemView.getSite();
		MElementContainer<MUIElement> stack = getParent(site.getModel());

		verifyOrder(stack, new String[] { "Problems", "Tasks", "Progress" });
	}

	/**
	 * Removes any NPEs.
	 *
	 * @return the problem view.
	 * @throws PartInitException
	 *             if a view fails to instantiate.
	 */
	private IViewPart instantiateViews() throws PartInitException {
		IViewPart problemView = fActivePage.showView(PROBLEM_VIEW_ID);
		assertNotNull(problemView);

		// make sure all of the views have been instantiated
		assertNotNull(fActivePage.showView(PROGRESS_VIEW_ID));
		assertNotNull(fActivePage.showView(TASK_VIEW_ID));
		return problemView;
	}

	/**
	 * Verify the tabs are in the correct order.
	 *
	 * @param pres
	 *            the stack presentation
	 * @param order
	 *            the expected order
	 */
	private void verifyOrder(MElementContainer<MUIElement> stack, String[] order) {
		List<MUIElement> children = stack.getChildren();
		assertEquals("Different number of tabs", order.length, children.size());
		for (int i = 0; i < children.size(); ++i) {
			MUIElement child = children.get(i);
			if (child instanceof MPlaceholder) {
				child = ((MPlaceholder) child).getRef();
			}
			assertEquals("Failed on tab " + i, order[i], ((MUILabel)child).getLabel());
		}
	}

}
