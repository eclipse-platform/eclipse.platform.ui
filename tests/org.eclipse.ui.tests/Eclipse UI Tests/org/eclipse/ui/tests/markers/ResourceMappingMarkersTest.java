/*******************************************************************************
 * Copyright (c) 2005, 2024 IBM Corporation and others.
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
package org.eclipse.ui.tests.markers;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.tests.navigator.AbstractNavigatorTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class ResourceMappingMarkersTest extends AbstractNavigatorTest {

	/**
	 * Create an instance of the receiver.
	 */
	public ResourceMappingMarkersTest() {
		super(ResourceMappingMarkersTest.class.getSimpleName());
	}

	/**
	 * Set up the receiver.
	 */
	@Override
	protected void doSetUp() throws Exception {
		super.doSetUp();
		createTestFile();
	}

	@Test
	public void testResourceMappings() throws PartInitException {
		IWorkbenchWindow window = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow();
		IWorkbenchPage page = window.getActivePage();
		ResourceMappingTestView view = (ResourceMappingTestView) page
				.showView("org.eclipse.ui.tests.resourceMappingView");

		final MarkersTestMarkersView problemView = (MarkersTestMarkersView) page
				.showView("org.eclipse.ui.tests.markerTests");

		IMarker marker=view.addMarkerToFirstProject();
		assertNotNull("Marker creation failed", marker);
		try {
			Job.getJobManager().join(
					problemView.MARKERSVIEW_UPDATE_JOB_FAMILY,
					new NullProgressMonitor());
		} catch (OperationCanceledException | InterruptedException e) {
		}

		IMarker[] markers=problemView.getCurrentMarkers();
		boolean markerFound = false;
		for (IMarker marker2 : markers) {
			if(marker2.equals(marker)){
				markerFound = true;
				break;
			}
		}
		assertTrue("No markers generated",markerFound);
	}
}
