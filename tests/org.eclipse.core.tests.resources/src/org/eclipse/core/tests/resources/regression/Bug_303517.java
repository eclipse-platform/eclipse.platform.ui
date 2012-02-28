/*******************************************************************************
 *  Copyright (c) 2011, 2012 Broadcom Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *     James Blackburn (Broadcom Corp.) - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tests.resources.regression;

import java.io.File;
import java.io.InputStream;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.core.tests.resources.ResourceTest;

/**
 * Tests that, when the workspace discovery a resource is out-of-sync
 * it brings the resource back into sync in a timely manner.
 */
public class Bug_303517 extends ResourceTest {

	public static Test suite() {
		return new TestSuite(Bug_303517.class);
	}

	String[] resources = new String[] {"/", "/Bug303517/", "/Bug303517/Folder/", "/Bug303517/Folder/Resource",};

	public String[] defineHierarchy() {
		return resources;
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		InstanceScope.INSTANCE.getNode(ResourcesPlugin.PI_RESOURCES).putBoolean(ResourcesPlugin.PREF_LIGHTWEIGHT_AUTO_REFRESH, true);
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		InstanceScope.INSTANCE.getNode(ResourcesPlugin.PI_RESOURCES).putBoolean(ResourcesPlugin.PREF_LIGHTWEIGHT_AUTO_REFRESH, false);
	}

	/**
	 * Tests that file deleted is udpated after #getContents
	 * @throws Exception
	 */
	public void testExists() throws Exception {
		createHierarchy();
		IFile f = getWorkspace().getRoot().getFile(new Path(resources[resources.length - 1]));
		assertTrue("1.0", f.exists());
		assertTrue("1.1", f.isSynchronized(IResource.DEPTH_ONE));

		// Touch on file-system
		f.getLocation().toFile().delete();
		// Core.resources still thinks the file exists
		assertTrue("1.2", f.exists());
		try {
			InputStream in = f.getContents();
			in.close();
			assertTrue("1.3", false);
		} catch (CoreException e) {
			// File doesn't exist - expected
		}

		// Wait for auto-refresh to happen
		Job.getJobManager().join(ResourcesPlugin.FAMILY_AUTO_REFRESH, getMonitor());

		// Core.resources should be aware that the file no longer exists...
		assertFalse("1.4", f.exists());
	}

	/**
	 * Tests that file discovered out-of-sync during #getContents is updated
	 * @throws Exception
	 */
	public void testGetContents() throws Exception {
		createHierarchy();
		IFile f = getWorkspace().getRoot().getFile(new Path(resources[resources.length - 1]));
		assertTrue("1.0", f.exists());
		assertTrue("1.1", f.isSynchronized(IResource.DEPTH_ONE));

		// Touch on file-system
		touchInFilesystem(f);
		try {
			InputStream in = f.getContents(false);
			in.close();
			assertTrue("1.2", false);
		} catch (CoreException e) {
			// File is out-of-sync, so this is good
		}

		// Wait for auto-refresh to happen
		Job.getJobManager().join(ResourcesPlugin.FAMILY_AUTO_REFRESH, getMonitor());

		// File is now in sync.
		try {
			InputStream in = f.getContents(false);
			in.close();
		} catch (CoreException e) {
			// Bad, file shouldn't be out-of-sync
			fail("1.3", e);
		}
	}

	/**
	 * Tests that file discovered out-of-sync during #getContents is updated
	 * @throws Exception
	 */
	public void testGetContentsTrue() throws Exception {
		createHierarchy();
		IFile f = getWorkspace().getRoot().getFile(new Path(resources[resources.length - 1]));
		assertTrue("1.0", f.exists());
		assertTrue("1.1", f.isSynchronized(IResource.DEPTH_ONE));

		// Touch on file-system
		touchInFilesystem(f);
		try {
			InputStream in = f.getContents(true);
			in.close();
		} catch (CoreException e) {
			// File is out-of-sync, so this is good
			fail("1.2", e);
		}

		// Wait for auto-refresh to happen
		Job.getJobManager().join(ResourcesPlugin.FAMILY_AUTO_REFRESH, getMonitor());

		// File is now in sync.
		try {
			InputStream in = f.getContents();
			in.close();
		} catch (CoreException e) {
			// Bad, file shouldn't be out-of-sync
			fail("1.3", e);
		}
	}

	/**
	 * Tests that resource discovered out-of-sync during #isSynchronized is updated
	 * @throws Exception
	 */
	public void testIsSynchronized() throws Exception {
		createHierarchy();
		IFile f = getWorkspace().getRoot().getFile(new Path(resources[resources.length - 1]));
		assertTrue("1.0", f.exists());
		assertTrue("1.1", f.isSynchronized(IResource.DEPTH_ONE));

		// Touch on file-system
		touchInFilesystem(f);
		assertFalse("1.2", f.isSynchronized(IResource.DEPTH_ONE));

		// Wait for auto-refresh to happen
		Job.getJobManager().join(ResourcesPlugin.FAMILY_AUTO_REFRESH, getMonitor());

		// File is now in sync.
		assertTrue("1.3", f.isSynchronized(IResource.DEPTH_ONE));
	}

	/**
	 * Tests that when changing resource gender is correctly picked up.
	 * @throws Exception
	 */
	public void testChangeResourceGender() throws Exception {
		createHierarchy();
		IResource f = getWorkspace().getRoot().getFile(new Path(resources[resources.length - 1]));
		assertTrue("1.0", f.exists());
		assertTrue("1.1", f.isSynchronized(IResource.DEPTH_ONE));

		// Replace the file with a folder
		File osResource = f.getLocation().toFile();
		osResource.delete();
		osResource.mkdir();
		assertTrue(osResource.exists());
		File osChild = new File(osResource, "child");
		osChild.createNewFile();
		assertTrue(osChild.exists());

		assertFalse("1.2", f.isSynchronized(IResource.DEPTH_ONE));

		// Wait for auto-refresh to happen
		Job.getJobManager().join(ResourcesPlugin.FAMILY_AUTO_REFRESH, getMonitor());

		// File is no longer a file - i.e. still out-of-sync
		assertFalse("1.3", f.exists());
		assertFalse("1.4", f.isSynchronized(IResource.DEPTH_ONE));
		// Folder + child are now in-sync
		f = getWorkspace().getRoot().getFolder(new Path(resources[resources.length - 1]));
		assertTrue("1.5", f.exists());
		assertTrue("1.6", f.isSynchronized(IResource.DEPTH_INFINITE));
	}
}
