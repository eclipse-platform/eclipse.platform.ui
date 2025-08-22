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


import static org.junit.Assert.assertTrue;

import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

public class ShowViewTest extends ZoomTestCase {

	/**
	 * <p>
	 * Test: Zoom a view, create a new view in the same stack using the
	 * IWorkbenchPage.VIEW_VISIBLE flag
	 * </p>
	 * <p>
	 * Expected result: the new view is zoomed and active
	 * </p>
	 */
	@Test
	@Ignore("Commented out until the (possible) ambiguity in bug 91775 is resolved")
	public void testCreateViewAndMakeVisibleInZoomedStack() {
		zoom(stackedView1);
		IViewPart newPart = showRegularView(ZoomPerspectiveFactory.STACK1_PLACEHOLDER1, IWorkbenchPage.VIEW_VISIBLE);

		Assert.assertTrue(page.getActivePart() == newPart);
		Assert.assertTrue(isZoomed(newPart));
	}

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
	 * <p>Test: Zoom a view, create a new view in a different stack using the
	 *    IWorkbenchPage.VIEW_CREATE flag and bring it to front using page.bringToTop</p>
	 * <p>Expected result: no change in zoom or activation. The newly created view is obscured by the zoom,
	 *    but will be the top view in its (currently invisible) stack.</p>
	 */
	@Test
	public void testCreateViewAndBringToTopInOtherStack() {
		zoom(unstackedView);
		IViewPart newPart = showRegularView(ZoomPerspectiveFactory.STACK1_PLACEHOLDER1, IWorkbenchPage.VIEW_CREATE);
		page.bringToTop(newPart);
		Assert.assertTrue(page.getActivePart() == unstackedView);

		// Ensure no change to zoom
		Assert.assertTrue(isZoomed(unstackedView));

		// Ensure that the new part was brought to the top of the stack
		MUIElement partParent = getPartParent(unstackedView);
		assertTrue(partParent instanceof MPartStack);

		MPartStack stack = (MPartStack) partParent;
		Assert.assertTrue(stack.getSelectedElement() == getPartModel(unstackedView));
	}

	/**
	 * <p>Test: Zoom a view, create a new view in a different stack using the
	 *    IWorkbenchPage.VIEW_VISIBLE flag</p>
	 * <p>Expected result: no change in zoom or activation. The newly created view is obscured by the zoom,
	 *    but will be the top view in its (currently invisible) stack.</p>
	 */
	@Test
	public void testCreateViewAndMakeVisibleInOtherStack() {
		zoom(unstackedView);
		IViewPart newPart = showRegularView(ZoomPerspectiveFactory.STACK1_PLACEHOLDER1, IWorkbenchPage.VIEW_VISIBLE);
		Assert.assertTrue(page.getActivePart() == unstackedView);

		// Ensure no change to zoom
		Assert.assertTrue(isZoomed(unstackedView));

		// Ensure that the new part was brought to the top of the stack
		MUIElement partParent = getPartParent(newPart);
		assertTrue(partParent instanceof MPartStack);

		MPartStack stack = (MPartStack) partParent;
		Assert.assertTrue(stack.getSelectedElement() == getPartModel(newPart));
	}
	/**
	 * <p>Test: Zoom an editor, create a new view using the IWorkbenchPage.VIEW_VISIBLE mode</p>
	 * <p>Expected result: No change to zoom or activation. The new view was brought to the top
	 *    of its stack.</p>
	 */
	@Test
	public void testCreateViewAndMakeVisibleWhileEditorZoomed() {
		zoom(editor1);
		IViewPart newPart = showRegularView(ZoomPerspectiveFactory.STACK1_PLACEHOLDER1, IWorkbenchPage.VIEW_VISIBLE);
		Assert.assertTrue(isZoomed());
		Assert.assertTrue(page.getActivePart() == editor1);

		// Ensure that the new part was brought to the top of the stack
		MUIElement partParent = getPartParent(newPart);
		assertTrue(partParent instanceof MPartStack);

		MPartStack stack = (MPartStack) partParent;
		Assert.assertTrue(stack.getSelectedElement() == getPartModel(newPart));
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
	 *    IWorkbenchPage.VIEW_ACTIVATE flag</p>
	 * <p>Expected result: the page is unzoomed and the new view is active</p>
	 */
	@Test
	public void testCreateViewAndActivateInOtherStack() {
		zoom(unstackedView);
		IViewPart newPart = showRegularView(ZoomPerspectiveFactory.STACK1_PLACEHOLDER1, IWorkbenchPage.VIEW_ACTIVATE);

		assertZoomed(null);
		assertActive(newPart);
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
	 * <p>Test: Zoom an editor, create a new view using the IWorkbenchPage.VIEW_ACTIVATE mode</p>
	 * <p>Expected result: the page is unzoomed and the new view is active</p>
	 */
	@Test
	public void testCreateViewAndActivateWhileEditorZoomed() {
		zoom(editor1);
		IViewPart newPart = showRegularView(ZoomPerspectiveFactory.STACK1_PLACEHOLDER1, IWorkbenchPage.VIEW_ACTIVATE);

		assertZoomed(null);
		assertActive(newPart);
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
