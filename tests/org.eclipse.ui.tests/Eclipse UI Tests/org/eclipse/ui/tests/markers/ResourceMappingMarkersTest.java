/*******************************************************************************
 * Copyright (c) 2005, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.markers;

import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.tests.navigator.AbstractNavigatorTest;

public class ResourceMappingMarkersTest extends AbstractNavigatorTest {

	/**
	 * Create an instance of the receiver.
	 * 
	 * @param testName
	 */
	public ResourceMappingMarkersTest(String testName) {
		super(testName);
	}

	/**
	 * Set up the receiver.
	 * 
	 * @throws Exception
	 */
	protected void doSetUp() throws Exception {
		super.doSetUp();
		createTestFile();
	}

	public void testResourceMappings() {
		IWorkbenchWindow window = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow();
		IWorkbenchPage page = window.getActivePage();
		ResourceMappingTestView view;

		try {
			view = (ResourceMappingTestView) page
					.showView("org.eclipse.ui.tests.resourceMappingView");
		} catch (PartInitException e) {
			assertTrue(e.getLocalizedMessage(), false);
			return;
		}
		final boolean[] waiting = new boolean[] { true };

		final MarkersTestMarkersView problemView;
		try {
			problemView = (MarkersTestMarkersView) page
					.showView("org.eclipse.ui.tests.markerTests");
		} catch (PartInitException e) {
			assertTrue(e.getLocalizedMessage(), false);
			return;
		}

		IJobChangeListener doneListener = new JobChangeAdapter() {
			public void done(IJobChangeEvent event) {
				if (problemView.getCurrentMarkers().length > 0)
					waiting[0] = false;
			}
		};

		problemView.addUpdateFinishListener(doneListener);
		view.addMarkerToFirstProject();
		long timeOut = System.currentTimeMillis() + 2000;
		waiting[0] = problemView.getCurrentMarkers().length == 0;

		Display display = view.getSite().getShell().getDisplay();
		while (waiting[0] && System.currentTimeMillis() < timeOut) {
            // Spin the loop until empty
			while (display.readAndDispatch()) {}

			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}

		assertTrue("No markers generated",
				problemView.getCurrentMarkers().length > 0);
		problemView.removeUpdateFinishListener(doneListener);

	}

}
