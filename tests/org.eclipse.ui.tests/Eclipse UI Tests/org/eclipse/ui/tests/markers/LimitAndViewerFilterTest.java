/*******************************************************************************
 * Copyright (c) 2022 Enda O'Brien and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which accompanies this distribution,
 * and is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors: IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.tests.markers;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.ide.IDEInternalPreferences;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.tests.harness.util.UITestCase;
import org.eclipse.ui.views.markers.MarkerItem;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 *
 * @since 3.5
 *
 *        Test that both the Content provider and TreeViewer for
 *        ExtendedMarkersView are giving consistent results when applying a
 *        ViewerFilter and marker limit.
 *
 */
@RunWith(JUnit4.class)
public class LimitAndViewerFilterTest extends UITestCase {

	/**
	 * @param testName
	 */
	public LimitAndViewerFilterTest() {
		super(LimitAndViewerFilterTest.class.getSimpleName());
	}

	final static int MARKER_LIMIT = 5, RED_MARKER_COUNT = 4, BLUE_MARKER_COUNT = 4;
	public static final String MARKER_COLOR_BLUE = "BLUE";

	/**
	 * Test set up with - A marker limit set to 5. - creates 4 red and 4 blue
	 * markers in the workspace.
	 *
	 */
	@Before
	public void before() {
		// Enable Marker limit and set to 5
		IPreferenceStore preferenceStore = IDEWorkbenchPlugin.getDefault().getPreferenceStore();
		preferenceStore.setValue(IDEInternalPreferences.USE_MARKER_LIMITS, true);
		preferenceStore.setValue(IDEInternalPreferences.MARKER_LIMITS_VALUE, MARKER_LIMIT);

		IWorkspace workspace = ResourcesPlugin.getWorkspace();

		// create 4 red and 4 blue markers
		try {
			createMarkers(workspace, RED_MARKER_COUNT, RedProblemMarkerViewView.MARKER_COLOR_RED);
			createMarkers(workspace, BLUE_MARKER_COUNT, MARKER_COLOR_BLUE);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@After
	public void after() {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		deleteMarkers(workspace);
	}

	/**
	 *
	 * Test using a view that only displays red markers. The expected result is that
	 * the TreeViewer should contain the 4 red markers and no blue markers.
	 *
	 * @throws Exception
	 */

	@Test
	public void expectTreeViewerToContain4Markers() throws Exception {

		// Open the Red marker view and expand the view
		openView();
		expandView();
		// As the limit is 5, Expect that 4 red markers makes it into the tree
		// viewer
		validateViewerMarkerCounts(RED_MARKER_COUNT);
	}

	/**
	 * Test using a view that only displays red markers. The expected result is that
	 * the Content provider should display the 4 red markers and no blue markers.
	 *
	 * @throws Exception
	 */

	@Test
	public void expectContentProviderToContainOnlyRedMarkers() throws Exception {
		// Open the Red marker view and expand the view
		openView();
		expandView();
		// Expect that only red markers makes it into the tree viewer
		validateContentProviderMarkerColors(RedProblemMarkerViewView.MARKER_COLOR_RED);
	}

	void openView() {
		IWorkbenchWindow ww = fWorkbench.getActiveWorkbenchWindow();
		// Get current page

		try {
			IWorkbenchPage wp = ww.getActivePage();

			wp.showView(RedProblemMarkerViewView.ID);

			processEventsUntil(() -> ww.getShell() != null, 10000);
		} catch (Exception e) {

		}
	}

	void expandView() {

		// Get current page
		IWorkbenchPage wp = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();

		RedProblemMarkerViewView view;
		try {
			view = (RedProblemMarkerViewView) wp.showView(RedProblemMarkerViewView.ID);

			ISelectionProvider provider = view.getSite().getSelectionProvider();
			TreeViewer viewer = (TreeViewer) provider;

			viewer.expandAll();
			processEventsUntil(() -> viewer.getExpandedElements().length > 0, 10000);
			waitForJobs(100, 1000);
		} catch (Exception e) {
			throw new RuntimeException();
		}
	}

	void validateViewerMarkerCounts(int expectedCount) {
		// Get current page
		IWorkbenchPage wp = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();

		RedProblemMarkerViewView view;

		view = (RedProblemMarkerViewView) wp.findView(RedProblemMarkerViewView.ID);

		ISelectionProvider provider = view.getSite().getSelectionProvider();
		TreeViewer viewer = (TreeViewer) provider;

		TreeItem[] viewerGroupRows = viewer.getTree().getItems();
		TreeItem[] viewerMarkers = viewerGroupRows[0].getItems();

		assertEquals(expectedCount, viewerMarkers.length);
	}

	void validateContentProviderMarkerColors(String expectedColor) {

		// Get current page
		IWorkbenchPage wp = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();

		RedProblemMarkerViewView view;

		view = (RedProblemMarkerViewView) wp.findView(RedProblemMarkerViewView.ID);

		ISelectionProvider provider = view.getSite().getSelectionProvider();
		TreeViewer viewer = (TreeViewer) provider;

		ITreeContentProvider contentProvider = (ITreeContentProvider) viewer.getContentProvider();
		Object[] contentProvGroupRows = contentProvider.getElements(null);
		Object[] contentProvMarkers = contentProvider.getChildren(contentProvGroupRows[0]);

		for (Object item : contentProvMarkers) {
			MarkerItem marker = (MarkerItem) item;
			String markerColor = marker.getAttributeValue(RedProblemMarkerViewView.MARKER_COLOR_ATTRIBUTE, "");
			assertEquals(expectedColor, markerColor);
		}

	}

	/**
	 * Create the given number of problem markers in the workspace and set the color
	 * attribute using the color argument.
	 *
	 * @param workspace
	 * @param count
	 * @param color
	 * @throws Exception
	 */
	void createMarkers(IWorkspace workspace, int count, String color) throws Exception {
		for (int i = 0; i < count; i++) {
			IMarker marker = workspace.getRoot().createMarker(IMarker.PROBLEM);
			marker.setAttribute(RedProblemMarkerViewView.MARKER_COLOR_ATTRIBUTE, color);
			marker.setAttribute(IMarker.MESSAGE, color + ": " + i);
			marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
		}
	}

	/**
	 * Delete all problem markers from the workspace.
	 *
	 * @param workspace
	 */

	void deleteMarkers(IWorkspace workspace) {
		try {
			workspace.getRoot().deleteMarkers(IMarker.PROBLEM, true, IResource.DEPTH_INFINITE);
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}
}
