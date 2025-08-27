/*******************************************************************************
 * Copyright (c) 2000, 2017 IBM Corporation and others.
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
package org.eclipse.ui.tests.zoom;


import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.junit.Assert;
import org.junit.Test;

public class ShowViewTest extends ZoomTestCase {

	/**
	 * <p>Test: Zoom a view, create a new view in the same stack using the
	 *    IWorkbenchPage.VIEW_CREATE flag, then bring it to top using </p>
	 * <p>Expected result: the new view is zoomed and active</p>
	 */
	@Test
	public void testCreateViewAndBringToTop() {
		zoom(stackedView1);
		IViewPart newPart = showRegularView(ZoomPerspectiveFactory.STACK1_PLACEHOLDER1,
				IWorkbenchPage.VIEW_CREATE);

		page.bringToTop(newPart);

		Assert.assertTrue(page.getActivePart() == newPart);
		Assert.assertTrue(isZoomed(newPart));
	}

	/**
	 * <p>Test: Zoom a view, create a new view in the same stack using the
	 *    IWorkbenchPage.VIEW_ACTIVATE flag</p>
	 * <p>Expected result: the new view is zoomed and active</p>
	 */
	@Test
	public void testCreateViewAndActivateInZoomedStack() {
		zoom(stackedView1);
		IViewPart newPart = showRegularView(ZoomPerspectiveFactory.STACK1_PLACEHOLDER1, IWorkbenchPage.VIEW_ACTIVATE);

		assertZoomed(newPart);
		assertActive(newPart);
	}

	/**
	 * <p>Test: Zoom a view, create a new view in the same stack using the
	 *    IWorkbenchPage.VIEW_CREATE flag</p>
	 * <p>Expected result: no change in activation or zoom</p>
	 */
	@Test
	public void testCreateViewInZoomedStack() {
		zoom(stackedView1);
		showRegularView(ZoomPerspectiveFactory.STACK1_PLACEHOLDER1,
				IWorkbenchPage.VIEW_CREATE);

		assertZoomed(stackedView1);
		assertActive(stackedView1);
	}

	/**
	 * <p>Test: Zoom a view, create a new view in a different stack using the
	 *    IWorkbenchPage.VIEW_CREATE flag</p>
	 * <p>Expected result: No change to zoom or activation. The newly created view is hidden</p>
	 */
	@Test
	public void testCreateViewInOtherStack() {
		zoom(unstackedView);
		showRegularView(ZoomPerspectiveFactory.STACK1_PLACEHOLDER1, IWorkbenchPage.VIEW_CREATE);

		assertZoomed(unstackedView);
		assertActive(unstackedView);
	}

	/**
	 * <p>Test: Zoom an editor, create a new view using the IWorkbenchPage.VIEW_CREATE mode</p>
	 * <p>Expected result: The editor remains zoomed and active.</p>
	 */
	@Test
	public void testCreateViewWhileEditorZoomed() {
		zoom(editor1);
		showRegularView(ZoomPerspectiveFactory.STACK1_PLACEHOLDER1, IWorkbenchPage.VIEW_CREATE);

		assertZoomed(editor1);
		assertActive(editor1);
	}

}
