/*******************************************************************************
 * Copyright (c) 2008, 2009 webtekie@gmail.com, IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     webtekie@gmail.com - initial API and implementation
 *.....IBM Corporation - fixed dead code warning
 *******************************************************************************/
package org.eclipse.ui.tests.navigator;

import java.io.ByteArrayInputStream;
import java.text.DecimalFormat;

import junit.framework.TestCase;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.navigator.CommonNavigator;

/**
 * A test to see if created projects are reflected in Project Explorer
 * 
 * @author akravets
 * 
 */
public class CreateProjectTest extends TestCase {

	private static final int NUM_OF_PROJECTS = 300; // number of projects to
	private static final boolean DEBUG = false;

	// created

	public void testCreateMultipleProjects() throws Exception {
		CommonNavigator view = null;
		try {
			// initialize Project Explorer view and assign in it to view
			// variable
			PlatformUI.getWorkbench().getActiveWorkbenchWindow()
					.getActivePage().showView(
							"org.eclipse.ui.navigator.ProjectExplorer");
			IViewReference[] viewReferences = PlatformUI.getWorkbench()
					.getActiveWorkbenchWindow().getActivePage()
					.getViewReferences();
			for (int j = 0; j < viewReferences.length; j++) {
				if (viewReferences[j].getId().equals(
						"org.eclipse.ui.navigator.ProjectExplorer")) {
					view = (CommonNavigator) viewReferences[j].getView(true);
				}
			}
		} catch (PartInitException e1) {
			fail("Couldn't not initialize CommonNavigator");
		}

		Job createJob = new Job("Create projects") {

			protected IStatus run(IProgressMonitor monitor) {
				try {
					DecimalFormat df = new DecimalFormat("000");
					for (int i = 0; i < NUM_OF_PROJECTS; i++) {
						String name = df.format(i);
						IProject p1 = ResourcesPlugin.getWorkspace().getRoot()
								.getProject("p" + name);
						p1.create(null);
						p1.open(null);
						IFile f1 = p1.getFile("f" + name);
						f1.create(new ByteArrayInputStream(new byte[] {}),
								true, null);
					}
				} catch (Exception ex) {
					ex.printStackTrace();
					fail("Unexpected exception: " + ex);
				}
				return Status.OK_STATUS;
			}
		};

		createJob.schedule();
		createJob.join();
		
		assertEquals(createJob.getResult(), Status.OK_STATUS);

		DisplayHelper.runEventLoop(Display.getCurrent(), 10);
		
		int numOfProjects = view.getCommonViewer().getTree().getItemCount();
		System.out.println("Projects created: " + numOfProjects);

		if (DEBUG)
			DisplayHelper.sleep(Display.getCurrent(), 10000000);

		// as a test compare number of created projects with NUM_OF_PROJECTS
		// constant
		assertEquals(NUM_OF_PROJECTS, numOfProjects);

	}
}