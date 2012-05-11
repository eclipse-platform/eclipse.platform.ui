/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tests.resources;

import java.io.*;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.internal.resources.File;
import org.eclipse.core.internal.resources.Project;
import org.eclipse.core.internal.utils.FileUtil;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.jobs.IJobManager;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.tests.internal.resources.SimpleNature;

/**
 * Tests all aspects of project natures.  These tests only
 * exercise API classes and methods.  Note that the nature-related
 * APIs on IWorkspace are tested by IWorkspaceTest.
 */
public class NatureTest extends ResourceTest {
	/**
	 * Constructor for NatureTest.
	 */
	public NatureTest() {
		super();
	}

	/**
	 * Constructor for NatureTest.
	 * @param name
	 */
	public NatureTest(String name) {
		super(name);
	}

	public static Test suite() {
		return new TestSuite(NatureTest.class);
	}

	/**
	 * Sets the given set of natures for the project.  If success
	 * does not match the "shouldFail" argument, an assertion error
	 * with the given message is thrown.
	 */
	protected void setNatures(String message, IProject project, String[] natures, boolean shouldFail) {
		setNatures(message, project, natures, shouldFail, false);
	}

	/**
	 * Sets the given set of natures for the project.  If success
	 * does not match the "shouldFail" argument, an assertion error
	 * with the given message is thrown.
	 */
	protected void setNatures(String message, IProject project, String[] natures, boolean shouldFail, boolean silent) {
		try {
			IProjectDescription desc = project.getDescription();
			desc.setNatureIds(natures);
			int flags = IResource.KEEP_HISTORY;
			if (silent)
				flags |= IResource.AVOID_NATURE_CONFIG;
			project.setDescription(desc, flags, getMonitor());
			if (shouldFail)
				fail(message);
		} catch (CoreException e) {
			if (!shouldFail)
				fail(message, e);
		}
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		getWorkspace().getRoot().refreshLocal(IResource.DEPTH_INFINITE, null);
		ensureDoesNotExistInWorkspace(getWorkspace().getRoot());
	}

	/**
	 * Tests invalid additions to the set of natures for a project.
	 */
	public void testInvalidAdditions() {
		IWorkspace ws = ResourcesPlugin.getWorkspace();
		IProject project = ws.getRoot().getProject("Project");
		ensureExistsInWorkspace(project, true);
		setNatures("1.0", project, new String[] {NATURE_SIMPLE}, false);

		//Adding a nature that is not available. 
		setNatures("2.0", project, new String[] {NATURE_SIMPLE, NATURE_MISSING}, true);
		try {
			assertTrue("2.1", project.hasNature(NATURE_SIMPLE));
			assertTrue("2.2", !project.hasNature(NATURE_MISSING));
			assertTrue("2.3", project.isNatureEnabled(NATURE_SIMPLE));
			assertTrue("2.4", !project.isNatureEnabled(NATURE_MISSING));
		} catch (CoreException e) {
			fail("2.99", e);
		}
		//Adding a nature that has a missing prerequisite. 
		setNatures("3.0", project, new String[] {NATURE_SIMPLE, NATURE_SNOW}, true);
		try {
			assertTrue("3.1", project.hasNature(NATURE_SIMPLE));
			assertTrue("3.2", !project.hasNature(NATURE_SNOW));
			assertTrue("3.3", project.isNatureEnabled(NATURE_SIMPLE));
			assertTrue("3.4", !project.isNatureEnabled(NATURE_SNOW));
		} catch (CoreException e) {
			fail("3.99", e);
		}
		//Adding a nature that creates a duplicated set member. 
		setNatures("4.0", project, new String[] {NATURE_EARTH}, false);
		setNatures("4.1", project, new String[] {NATURE_EARTH, NATURE_WATER}, true);
		try {
			assertTrue("3.1", project.hasNature(NATURE_EARTH));
			assertTrue("3.2", !project.hasNature(NATURE_WATER));
			assertTrue("3.3", project.isNatureEnabled(NATURE_EARTH));
			assertTrue("3.4", !project.isNatureEnabled(NATURE_WATER));
		} catch (CoreException e) {
			fail("3.99", e);
		}
	}

	/**
	 * Tests invalid removals from the set of natures for a project.
	 */
	public void testInvalidRemovals() {
		IWorkspace ws = ResourcesPlugin.getWorkspace();
		IProject project = ws.getRoot().getProject("Project");
		ensureExistsInWorkspace(project, true);

		//Removing a nature that still has dependents.
		setNatures("1.0", project, new String[] {NATURE_WATER, NATURE_SNOW}, false);
		setNatures("2.0", project, new String[] {NATURE_SNOW}, true);
		try {
			assertTrue("2.1", project.hasNature(NATURE_WATER));
			assertTrue("2.2", project.hasNature(NATURE_SNOW));
			assertTrue("2.3", project.isNatureEnabled(NATURE_WATER));
			assertTrue("2.4", project.isNatureEnabled(NATURE_SNOW));
		} catch (CoreException e) {
			fail("2.99", e);
		}
	}

	public void testNatureLifecyle() {
		IWorkspace ws = ResourcesPlugin.getWorkspace();
		IProject project = ws.getRoot().getProject("Project");
		ensureExistsInWorkspace(project, true);

		//add simple nature
		setNatures("1.0", project, new String[] {NATURE_SIMPLE}, false);
		SimpleNature instance = SimpleNature.getInstance();
		assertTrue("1.1", instance.wasConfigured);
		assertTrue("1.2", !instance.wasDeconfigured);
		instance.reset();

		//remove simple nature
		setNatures("1.3", project, new String[0], false);
		instance = SimpleNature.getInstance();
		assertTrue("1.4", !instance.wasConfigured);
		assertTrue("1.5", instance.wasDeconfigured);

		//add with AVOID_NATURE_CONFIG
		instance.reset();
		setNatures("2.0", project, new String[] {NATURE_SIMPLE}, false, true);
		instance = SimpleNature.getInstance();
		assertTrue("2.1", !instance.wasConfigured);
		assertTrue("2.2", !instance.wasDeconfigured);
		try {
			assertTrue("2.3", project.hasNature(NATURE_SIMPLE));
		} catch (CoreException e) {
			fail("1.99", e);
		}

		//remove with AVOID_NATURE_CONFIG
		instance.reset();
		setNatures("2.3", project, new String[0], false, true);
		instance = SimpleNature.getInstance();
		assertTrue("2.4", !instance.wasConfigured);
		assertTrue("2.5", !instance.wasDeconfigured);
		try {
			assertTrue("2.6", !project.hasNature(NATURE_SIMPLE));
		} catch (CoreException e) {
			fail("2.99", e);
		}
	}

	/**
	 * Test simple addition and removal of natures.
	 */
	public void testSimpleNature() {
		IWorkspace ws = ResourcesPlugin.getWorkspace();
		IProject project = ws.getRoot().getProject("Project");
		ensureExistsInWorkspace(project, true);

		String[][] valid = getValidNatureSets();
		for (int i = 0; i < valid.length; i++) {
			setNatures("valid: " + i, project, valid[i], false);
		}
		//configure a valid nature before starting invalid tests
		String[] currentSet = new String[] {NATURE_SIMPLE};
		setNatures("1.0", project, currentSet, false);

		//now do invalid tests and ensure simple nature is still configured	
		String[][] invalid = getInvalidNatureSets();
		for (int i = 0; i < invalid.length; i++) {
			setNatures("invalid: " + i, project, invalid[i], true);
			try {
				assertTrue("2.0", project.hasNature(NATURE_SIMPLE));
				assertTrue("2.1", !project.hasNature(NATURE_EARTH));
				assertTrue("2.2", project.isNatureEnabled(NATURE_SIMPLE));
				assertTrue("2.3", !project.isNatureEnabled(NATURE_EARTH));
				assertEquals("2.4", project.getDescription().getNatureIds(), currentSet);
			} catch (CoreException e) {
				fail("2.99", e);
			}
		}
	}

	/**
	 * Test addition of nature that requires the workspace root.
	 * See bugs 127562 and  128709.
	 */
	public void testBug127562Nature() {
		IWorkspace ws = ResourcesPlugin.getWorkspace();
		IProject project = ws.getRoot().getProject("Project");
		ensureExistsInWorkspace(project, true);

		String[][] valid = getValidNatureSets();
		for (int i = 0; i < valid.length; i++) {
			setNatures("valid: " + i, project, valid[i], false);
		}

		// add with AVOID_NATURE_CONFIG
		String[] currentSet = new String[] {NATURE_127562};
		setNatures("1.0", project, currentSet, false, true);

		// configure the nature using a conflicting scheduling rule
		IJobManager manager = Job.getJobManager();
		try {
			manager.beginRule(ws.getRuleFactory().modifyRule(project), null);
			project.getNature(NATURE_127562).configure();
			fail("2.0");
		} catch (CoreException ex) {
			fail("2.1");
		} catch (IllegalArgumentException ex) {
			// should throw this kind of exception
		} finally {
			manager.endRule(ws.getRuleFactory().modifyRule(project));
		}

		// configure the nature using a non-conflicting scheduling rule
		try {
			manager.beginRule(ws.getRoot(), null);
			project.getNature(NATURE_127562).configure();
		} catch (CoreException ex) {
			fail("3.0");
		} finally {
			manager.endRule(ws.getRoot());
		}
	}

	public void testBug297871() {
		IWorkspace ws = ResourcesPlugin.getWorkspace();
		Project project = (Project) ws.getRoot().getProject("Project");
		ensureExistsInWorkspace(project, true);

		java.io.File desc = null;
		try {
			IFileStore descStore = ((File) project.getFile(IProjectDescription.DESCRIPTION_FILE_NAME)).getStore();
			desc = descStore.toLocalFile(EFS.NONE, getMonitor());
		} catch (CoreException e) {
			fail("1.0");
		}

		java.io.File descTmp = new java.io.File(desc.getPath() + ".tmp");
		try {
			copy(desc, descTmp);
		} catch (IOException e) {
			fail("2.0", e);
		}

		setNatures("valid ", project, new String[] {NATURE_EARTH}, false);

		try {
			assertNotNull(project.getNature(NATURE_EARTH));
		} catch (CoreException e) {
			fail("3.0", e);
		}

		try {
			assertTrue(project.hasNature(NATURE_EARTH));
		} catch (CoreException e) {
			fail("4.0", e);
		}

		try {
			// Make sure enough time has past to bump file's
			// timestamp during the copy  
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			fail("5.0", e);
		}

		try {
			copy(descTmp, desc);
		} catch (IOException e) {
			fail("6.0", e);
		}

		try {
			project.refreshLocal(IResource.DEPTH_INFINITE, getMonitor());
		} catch (CoreException e) {
			fail("7.0", e);
		}

		try {
			assertNull(project.getNature(NATURE_EARTH));
		} catch (CoreException e) {
			fail("8.0", e);
		}

		try {
			assertFalse(project.hasNature(NATURE_EARTH));
		} catch (CoreException e) {
			fail("9.0", e);
		}
	}

	private void copy(java.io.File src, java.io.File dst) throws IOException {
		InputStream in = new FileInputStream(src);
		OutputStream out = new FileOutputStream(dst);

		byte[] buffer = new byte[1024];
		int read;
		while ((read = in.read(buffer)) > 0) {
			out.write(buffer, 0, read);
		}
		in.close();
		out.close();
	}

	/**
	 * Changes project description and parallel checks {@link IProject#isNatureEnabled(String)},
	 * to check if natures value is cached properly.
	 * 
	 * See Bug 338055.
	 * @throws Exception
	 */
	public void testBug338055() throws Exception {
		final boolean finished[] = new boolean[] {false};
		final Project project = (Project) ResourcesPlugin.getWorkspace().getRoot().getProject(getUniqueString());

		ensureExistsInWorkspace(project, true);

		new Job("CheckNatureJob") {
			protected IStatus run(IProgressMonitor monitor) {
				try {
					if (finished[0] == false) {
						if (project.exists() && project.isOpen())
							project.isNatureEnabled(NATURE_SIMPLE);
						schedule();
					}
				} catch (CoreException e) {
					fail("CheckNatureJob failed", e);
				}
				return Status.OK_STATUS;
			}
		}.schedule();

		try {
			// Make sure enough time has past to bump file's
			// timestamp during the copy  
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			fail("2.0", e);
		}

		IFileStore descStore = ((File) project.getFile(IProjectDescription.DESCRIPTION_FILE_NAME)).getStore();

		// create a description with many natures, this will make updating description longer
		StringBuffer description = new StringBuffer();
		description.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?><projectDescription><name></name><comment></comment><projects></projects><buildSpec></buildSpec><natures>");
		description.append("<nature>" + NATURE_SIMPLE + "</nature>");
		for (int i = 0; i < 100; i++) {
			description.append("<nature>nature" + i + "</nature>");
		}
		description.append("</natures></projectDescription>\n");

		// write the description
		OutputStream output = null;
		try {
			output = descStore.openOutputStream(EFS.NONE, getMonitor());
			output.write(description.toString().getBytes());
		} catch (CoreException e) {
			fail("1.0");
		} finally {
			FileUtil.safeClose(output);
		}

		try {
			project.refreshLocal(IResource.DEPTH_INFINITE, getMonitor());
		} catch (CoreException e) {
			fail("3.0", e);
		}

		finished[0] = true;

		assertTrue("4.0", project.hasNature(NATURE_SIMPLE));
		assertTrue("5.0", project.isNatureEnabled(NATURE_SIMPLE));
	}
}
