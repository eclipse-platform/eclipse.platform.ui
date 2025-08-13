/*******************************************************************************
 * Copyright (c) 2008, 2015 webtekie@gmail.com, IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     webtekie@gmail.com - initial API and implementation
 *     IBM Corporation - fixed dead code warning
 *     Thibault Le Ouay <thibaultleouay@gmail.com> - Bug 457870
 *******************************************************************************/
package org.eclipse.ui.tests.navigator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

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
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.navigator.ICommonViewerMapper;
import org.eclipse.ui.navigator.resources.ProjectExplorer;
import org.eclipse.ui.tests.harness.util.DisplayHelper;
import org.eclipse.ui.tests.harness.util.EditorTestHelper;
import org.junit.Test;

/**
 * A test to see if created projects are reflected in Project Explorer
 */
public class PerformanceTest extends NavigatorTestBase {

	private int _numProjects = 50; // number of projects to
	private int _numFiles = 1000;
	private static final boolean DEBUG = false;

	DecimalFormat _df = new DecimalFormat("000");

	public PerformanceTest() {
		_navigatorInstanceId = ProjectExplorer.VIEW_ID;
		_initTestData = false;
	}

	protected void createProjects() throws InterruptedException {
		Job createJob = new Job("Create projects") {

			@Override
			protected IStatus run(IProgressMonitor monitor) {
				try {
					for (int i = 0; i < _numProjects; i++) {
						String name = _df.format(i);
						IProject p1 = ResourcesPlugin.getWorkspace().getRoot()
								.getProject("p" + name);
						p1.create(null);
						p1.open(null);
						IFile f1 = p1.getFile("f" + _df.format(0));
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

		createJob.setRule(ResourcesPlugin.getWorkspace().getRoot());
		createJob.schedule();
		createJob.join();

		assertEquals(createJob.getResult(), Status.OK_STATUS);

		DisplayHelper.runEventLoop(Display.getCurrent(), 10);

		int numOfProjects = _viewer.getTree().getItemCount();
		System.out.println("Projects created: " + numOfProjects);

		if (DEBUG)
			DisplayHelper.sleep(Display.getCurrent(), 10000000);

		assertEquals(_numProjects, numOfProjects);
	}

	protected void deleteProjects() throws InterruptedException {
		Job deleteJob = new Job("Delete Projects") {

			@Override
			protected IStatus run(IProgressMonitor monitor) {
				try {
					for (int i = 0; i < _numProjects; i++) {
						String name = _df.format(i);
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

		deleteJob.setRule(ResourcesPlugin.getWorkspace().getRoot());
		deleteJob.schedule();
		deleteJob.join();

		assertEquals(deleteJob.getResult(), Status.OK_STATUS);

		DisplayHelper.runEventLoop(Display.getCurrent(), 10);
	}

	protected void createFiles(final IProject project, final int startNumber) throws InterruptedException
 {
		Job createJob = new Job("Create Files") {

			@Override
			protected IStatus run(IProgressMonitor monitor) {
				try {
					for (int i = startNumber; i < _numFiles; i++) {
						String name = _df.format(i);
						IFile f1 = project.getFile("f" + name);
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

		createJob.setRule(ResourcesPlugin.getWorkspace().getRoot());
		createJob.schedule();
		createJob.join();

		assertEquals(createJob.getResult(), Status.OK_STATUS);
	}

	protected void touchFiles(final IProject p1) throws Exception {
		Job touchJob = new Job("Touch Files") {

			@Override
			protected IStatus run(IProgressMonitor monitor) {
				try {
					for (int i = 0; i < _numFiles; i++) {
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

		touchJob.setRule(ResourcesPlugin.getWorkspace().getRoot());
		touchJob.schedule();
		touchJob.join();
		assertEquals(touchJob.getResult(), Status.OK_STATUS);
	}

	// bug 159828 deleting large number of projects takes too long
	@Test
	public void testCreateAndDeleteProjects() throws PartInitException, InterruptedException {

		_numProjects = 100;

		createProjects();

		// Hide it
		EditorTestHelper.showView(_navigatorInstanceId, false);

		long start = System.currentTimeMillis();
		deleteProjects();
		DisplayHelper.sleep(500);
		System.out.println("No project explorer delete " + _numProjects + " Time: "
				+ (System.currentTimeMillis() - start));

		showNavigator();
		DisplayHelper.sleep(100);
		createProjects();
		DisplayHelper.sleep(200);

		start = System.currentTimeMillis();
		deleteProjects();
		DisplayHelper.sleep(500);
		System.out.println("Project explorer " + _numProjects + " Time: "
				+ (System.currentTimeMillis() - start));

		DisplayHelper.sleep(500);

	}

	public void XXXtestCreateAndTouchFiles() throws Exception {

		final IProject p1 = ResourcesPlugin.getWorkspace().getRoot()
				.getProject("p1");
		p1.create(null);
		p1.open(null);

		createFiles(p1, 0);

		_viewer.collapseAll();

		if (DEBUG)
			DisplayHelper.sleep(Display.getCurrent(), 10000000);

		long start = System.currentTimeMillis();

		Job touchJob = new Job("Touch Files") {

			@Override
			protected IStatus run(IProgressMonitor monitor) {
				try {
					for (int i = 0; i < _numFiles; i++) {
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

		touchJob.setRule(ResourcesPlugin.getWorkspace().getRoot());
		touchJob.schedule();
		touchJob.join();
		assertEquals(touchJob.getResult(), Status.OK_STATUS);

		System.out.println("Touch " + _numFiles + " Time: "
				+ (System.currentTimeMillis() - start));
	}

	protected void createFilesForProjects() throws InterruptedException {
		for (int i = 0; i < _numProjects; i++) {
			String name = _df.format(i);
			IProject p1 = ResourcesPlugin.getWorkspace().getRoot().getProject(
					"p" + name);
			createFiles(p1, 1);
		}
	}

	// bug 194209 updating lots of label providers does not scale well
	@Test
	public void testLabelProviderMapping() throws Exception {

		ICommonViewerMapper mapper = _viewer.getMapper();

		_numProjects = 1;
		_numFiles = 2000;

		createProjects();
		createFilesForProjects();

		// Warm up
		final IProject p1 = ResourcesPlugin.getWorkspace().getRoot()
				.getProject("p000");

		p1.close(null);

		long start = System.currentTimeMillis();
		_viewer.setMapper(null);
		p1.open(null);
		// Let the updates run
		DisplayHelper.sleep(200);

		long createUnMappedTime = System.currentTimeMillis() - start;
		System.out.println("Unmapped Time: " + createUnMappedTime);

		p1.close(null);
		DisplayHelper.sleep(200);
		_viewer.setMapper(mapper);

		start = System.currentTimeMillis();
		p1.open(null);
		// Let the updates run
		DisplayHelper.sleep(200);
		long createMappedTime = System.currentTimeMillis() - start;
		System.out.println("Mapped Time: " + createMappedTime);

		assertTrue(createMappedTime < createUnMappedTime);
	}

}