/*******************************************************************************
 * Copyright (c) 2006, 2017 IBM Corporation and others.
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
 ******************************************************************************/

package org.eclipse.ui.tests.performance;

import static org.eclipse.ui.tests.harness.util.UITestUtil.openTestWindow;
import static org.eclipse.ui.tests.harness.util.UITestUtil.processEvents;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.test.performance.Dimension;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.tests.performance.parts.PerformanceProblemsView;
import org.eclipse.ui.texteditor.MarkerUtilities;

/**
 * The ProblemsViewPerformanceTest is a test of population of the problems view.
 *
 * @since 3.2
 */
public class ProblemsViewPerformanceTest extends BasicPerformanceTest {

	private final String EMPTY_PERSPECTIVE_ID = "org.eclipse.ui.tests.harness.util.EmptyPerspective";

	private final String PROBLEMS_VIEW_ID = "org.eclipse.ui.tests.performance.problemsView";

	/**
	 * Create a new instance of the receiver.
	 */
	public ProblemsViewPerformanceTest(String testName) {
		super(testName);
	}

	/**
	 * Test the population of the problems view.
	 */
	public void testPopulation() {
		IWorkbenchWindow window = openTestWindow(EMPTY_PERSPECTIVE_ID);
		final IWorkbenchPage page = window.getActivePage();

		PerformanceProblemsView view;

		try {
			view = (PerformanceProblemsView) page.showView(PROBLEMS_VIEW_ID);
		} catch (PartInitException e) {
			e.printStackTrace();
			fail();
			return;
		}

		tagIfNecessary("UI - Problems View population",
				Dimension.ELAPSED_PROCESS);

		for (int i = 0; i < 100; i++) {
			createMarkers();
			processEvents();
			startMeasuring();
			while (view.getTreeWidget().getItemCount() == 0)
				processEvents();
			stopMeasuring();
			removeMarkers();
			while (view.getTreeWidget().getItemCount() > 0)
				processEvents();
		}
		commitMeasurements();
		assertPerformance();

	}

	/**
	 * Remove the created markers
	 */
	private void removeMarkers() {

		try {
			IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();

			IMarker[] markers = root.findMarkers(IMarker.PROBLEM, false,
					IResource.DEPTH_ZERO);

			for (IMarker marker : markers) {
				String message = (String) marker.getAttribute(IMarker.MESSAGE);
				if (message != null && message.startsWith("this is a test")) {
					marker.delete();
				}
			}
		} catch (CoreException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Create the markers for the receiver.
	 */
	private void createMarkers() {
		try {
			IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
			Map<String, Object> attribs = new HashMap<>();
			for (int i = 0; i < 1000; i++) {
				attribs.put(IMarker.SEVERITY, Integer.valueOf(
						IMarker.SEVERITY_ERROR));
				attribs.put(IMarker.MESSAGE, "this is a test " + i);
				MarkerUtilities.createMarker(root, attribs, IMarker.PROBLEM);
			}
		} catch (CoreException e) {
			e.printStackTrace();
		}

	}

}
