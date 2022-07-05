/*******************************************************************************
 * Copyright (c) 2016 msg systems ag and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     msg systems ag - initial API and implementation
 ******************************************************************************/
package org.eclipse.ui.tests.markers;

import static org.junit.Assert.assertEquals;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.tests.harness.util.UITestCase;
import org.eclipse.ui.views.markers.MarkerViewUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * MarkerViewUtilTest are the test for the marker view util.
 *
 * @since 3.13
 */
@RunWith(JUnit4.class)
public class MarkerViewUtilTest extends UITestCase {

	private IProject project;

	/**
	 * Create a new instance of the receiver.
	 */
	public MarkerViewUtilTest() {
		super(MarkerViewUtilTest.class.getSimpleName());
	}

	@Override
	protected void doSetUp() throws Exception {
		super.doSetUp();
		project = ResourcesPlugin.getWorkspace().getRoot().getProject("tests");
		if (!project.exists()) {
			project.create(null);
		}

		if (!project.isOpen()) {
			project.open(null);
		}
	}

	@Override
	protected void doTearDown() throws Exception {
		if (project.exists()) {
			project.delete(true, null);
		}
		super.doTearDown();
	}

	@Test
	public void testShowMarkers() throws CoreException {
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		IWorkbenchPage page = window.getActivePage();

		boolean result = MarkerViewUtil.showMarkers(page, null, true);
		assertEquals(false, result);

		result = MarkerViewUtil.showMarkers(page, new IMarker[] {}, true);
		assertEquals(false, result);

		result = MarkerViewUtil.showMarkers(page, new IMarker[] { null }, true);
		assertEquals(false, result);

		IMarker someTaskMarker = project.createMarker(IMarker.TASK);
		IMarker someBookmarkMarker = project.createMarker(IMarker.BOOKMARK);
		IMarker someProblemMarker = project.createMarker(IMarker.PROBLEM);
		IMarker someTextMarker = project.createMarker(IMarker.TEXT);

		result = MarkerViewUtil.showMarkers(page,
				new IMarker[] { someTaskMarker, someBookmarkMarker, someProblemMarker }, true);
		IViewPart part = page.findView(IPageLayout.ID_TASK_LIST);
		boolean visible = page.isPartVisible(part);
		assertEquals(true, result);
		assertEquals(true, visible);

		result = MarkerViewUtil.showMarkers(page,
				new IMarker[] { someBookmarkMarker, someProblemMarker, someTaskMarker }, true);
		part = page.findView(IPageLayout.ID_BOOKMARKS);
		visible = page.isPartVisible(part);
		assertEquals(true, result);
		assertEquals(true, visible);

		result = MarkerViewUtil.showMarkers(page,
				new IMarker[] { someProblemMarker, someTaskMarker, someBookmarkMarker }, true);
		part = page.findView(IPageLayout.ID_PROBLEM_VIEW);
		visible = page.isPartVisible(part);
		assertEquals(true, result);
		assertEquals(true, visible);
		page.hideView(part);

		result = MarkerViewUtil.showMarkers(page, new IMarker[] { someTextMarker, someProblemMarker }, true);
		part = page.findView(IPageLayout.ID_PROBLEM_VIEW);
		visible = page.isPartVisible(part);
		assertEquals(true, result);
		assertEquals(true, visible);
	}
}
