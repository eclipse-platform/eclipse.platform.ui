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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.navigator.resources.ProjectExplorer;
import org.eclipse.ui.tests.harness.util.DisplayHelper;

/**
 * A test to see if created projects are reflected in Project Explorer
 */
public class CreateProjectTest extends NavigatorTestBase {

	private static final int NUM_OF_PROJECTS = 50; // number of projects to
	private static final int NUM_OF_FILES = 1000; 
	private static final boolean DEBUG = false;

	DecimalFormat _df = new DecimalFormat("000");

	public CreateProjectTest() {
		_navigatorInstanceId = ProjectExplorer.VIEW_ID;
		_initTestData = false;
	}

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	protected void createProjects() throws Exception {
		Job createJob = new Job("Create projects") {

			protected IStatus run(IProgressMonitor monitor) {
				try {
					for (int i = 0; i < NUM_OF_PROJECTS; i++) {
						String name = _df.format(i);
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

		int numOfProjects = _viewer.getTree().getItemCount();
		System.out.println("Projects created: " + numOfProjects);

		if (DEBUG)
			DisplayHelper.sleep(Display.getCurrent(), 10000000);

		assertEquals(NUM_OF_PROJECTS, numOfProjects);
	}
	
	// bug 159828 deleting large number of projects takes too long
	public void testCreateAndDeleteProjects() throws Exception {
		createProjects();
		
		long start = System.currentTimeMillis();

		Job deleteJob = new Job("Delete Projects") {

			protected IStatus run(IProgressMonitor monitor) {
				try {
					DecimalFormat df = new DecimalFormat("000");
					for (int i = 0; i < NUM_OF_PROJECTS; i++) {
						String name = df.format(i);
						IProject p1 = ResourcesPlugin.getWorkspace().getRoot()
								.getProject("p" + name);
						p1.delete(true, null);
					}
				} catch (Exception ex) {
					ex.printStackTrace();
					fail("Unexpected exception: " + ex);
				}
				return Status.OK_STATUS;
			}
		};

		deleteJob.schedule();
		deleteJob.join();

		assertEquals(deleteJob.getResult(), Status.OK_STATUS);

		System.out.println("Delete " + NUM_OF_PROJECTS + " Time: " + (System.currentTimeMillis() - start));

		DisplayHelper.runEventLoop(Display.getCurrent(), 10);

		int numOfProjects = _viewer.getTree().getItemCount();

		if (DEBUG)
			DisplayHelper.sleep(Display.getCurrent(), 10000000);

		assertEquals(0, numOfProjects);
	}

	// bug 194209 updating lots of label providers does not scale well
	public void testCreateAndTouchFiles() throws Exception {

		final IProject p1 = ResourcesPlugin.getWorkspace().getRoot()
		.getProject("p1");
		p1.create(null);
		p1.open(null);
		
		Job createJob = new Job("Create Files") {

			protected IStatus run(IProgressMonitor monitor) {
				try {
					for (int i = 0; i < NUM_OF_FILES; i++) {
						String name = _df.format(i);
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

		_viewer.collapseAll();
		
		if (DEBUG)
			DisplayHelper.sleep(Display.getCurrent(), 10000000);
		
		long start = System.currentTimeMillis();
		
		Job touchJob = new Job("Touch Files") {

			protected IStatus run(IProgressMonitor monitor) {
				try {
					for (int i = 0; i < NUM_OF_FILES; i++) {
						String name = _df.format(i);
						IFile f1 = p1.getFile("f" + name);
						f1.touch(null);
					}
				} catch (Exception ex) {
					ex.printStackTrace();
					fail("Unexpected exception: " + ex);
				}
				return Status.OK_STATUS;
			}
		};

		touchJob.schedule();
		touchJob.join();
		assertEquals(touchJob.getResult(), Status.OK_STATUS);

		System.out.println("Touch " + NUM_OF_FILES + " Time: " + (System.currentTimeMillis() - start));

	}
	
	
	
}