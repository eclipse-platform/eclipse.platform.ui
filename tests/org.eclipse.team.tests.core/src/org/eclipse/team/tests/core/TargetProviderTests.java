/*******************************************************************************
 * Copyright (c) 2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 * IBM - Initial implementation
 ******************************************************************************/
package org.eclipse.team.tests.core;

import java.net.MalformedURLException;
import java.net.URL;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.target.TargetManager;
import org.eclipse.team.core.target.TargetProvider;

/**
 * A set of test cases for org.eclipse.team.core.target.TargetProvider
 */
public class TargetProviderTests extends TeamTest {
	

	public TargetProviderTests() {
		super();
	}
	public TargetProviderTests(String name) {
		super(name);
	}
	public static Test suite() {
		TestSuite suite = new TestSuite(TargetProviderTests.class);
		return new TargetTestSetup(suite);
		//return new TargetTestSetup(new TargetProviderTests("testPutAndGet"));
	}
	/**
	 * Tests the link between the target & the site for consistency.
	 * @throws CoreException
	 * @throws TeamException
	 */
	public void testProjectMapping() throws CoreException, TeamException {
		IProject project = getUniqueTestProject("projectmapping");
		TargetProvider target = createProvider(project);
		assertTrue(getSite().equals(target.getSite()));
		TargetManager.unmap(project);
		assertNull(TargetManager.getProvider(project));
	}
	/**
	 * Tests the getURL() method on TargetProvider.
	 * @throws CoreException
	 * @throws TeamException
	 */
	public void testUrlRetrieval() throws CoreException, TeamException, MalformedURLException {
		IProject project = getNamedTestProject("urlretrieval");
		TargetManager.map(project, getSite(), new Path(properties.getProperty("test_dir")));
		TargetProvider target = TargetManager.getProvider(project);
		String goodurl =new URL(new URL(properties.getProperty("location")), properties.getProperty("test_dir")).toString();
		assertEquals(goodurl, target.getURL().toString()); 
	}
	/**
	 * Verifies that the get() and put() methods on the TargetProvider works as expected.
	 * @throws CoreException
	 * @throws TeamException
	 */
	public void testPutAndGet() throws CoreException, TeamException {
		// test put
		IProject project = getUniqueTestProject("get");
		IResource[] resources = buildResources(project, new String[] { "file1.txt", "folder1/", "folder1/b.txt" }, false);
		TargetProvider target = createProvider(project);
		target.put(resources, null);
		assertLocalEqualsRemote(project);
		// test get on a new project
		project.delete(true, true, null);
		project.create(null);
		project.open(null);
		target = createProvider(project);
		target.get(new IResource[] { project }, null);
		assertLocalEqualsRemote(project);
	}
	/**
	 * Verifies that canGet() and canPut() returns accurate values.
	 * @throws CoreException
	 * @throws TeamException
	 */
	public void testCanGetAndCanPut() throws CoreException, TeamException {
		IProject project = getUniqueTestProject("canget");
		IResource[] resources = buildResources(project, new String[] { "file1.txt", "folder1/", "folder1/b.txt" }, false);
		TargetProvider target = createProvider(project);
		for (int i = 0; i < resources.length; i++) {
			assertTrue(target.canPut(resources[i]));
		}
		target.put(resources, null);
		project.delete(true, true, null);
		project.create(null);
		project.open(null);
		target = createProvider(project);
		for (int i = 0; i < resources.length; i++) {
			assertTrue(target.canGet(resources[i]));
		}
		target.get(new IResource[] { project }, null);
	}
	/**
	 * Tests bahavior of isOutOfDate() for correctness
	 * @throws CoreException
	 * @throws TeamException
	 */
	public void testIsOutOfDate() throws CoreException, TeamException {
		IProject project = getUniqueTestProject("outdated");
		IResource[] resources = buildResources(project, new String[] { "file1.txt", "folder1/", "folder1/b.txt" }, false);
		TargetProvider target = createProvider(project);
		target.put(resources, null);
		sleep(1501);
		IProject dupeProject = getUniqueTestProject("outdated");
		IResource[] freshResources = buildResources(dupeProject, new String[] { "file1.txt", "folder1/", "folder1/b.txt" }, false);
		TargetManager.map(dupeProject, getSite(), new Path(properties.getProperty("test_dir")).append(project.getName()));
		TargetProvider dupeTarget = TargetManager.getProvider(dupeProject);
		dupeTarget.put(freshResources, null);
		for (int i = 0; i < resources.length; i++) {
			if (resources[i].getType() == IResource.FILE)
				assertTrue(target.isOutOfDate(resources[i], DEFAULT_PROGRESS_MONITOR));
		}
	}
	/**
	 * Tests bahavior of isDirty() for correctness
	 * @throws CoreException
	 * @throws TeamException
	 */
	public void testIsDirty() throws CoreException, TeamException {
		IProject project = getUniqueTestProject("dirty");
		IResource[] resources = buildResources(project, new String[] { "file1.txt", "folder1/", "folder1/b.txt" }, false);
		TargetProvider target = createProvider(project);
		target.put(resources, null);
		sleep(1501);
		resources = buildResources(project, new String[] { "file1.txt", "folder1/", "folder1/b.txt" }, false);
		for (int i = 0; i < resources.length; i++) {
			if (resources[i].getType()==IResource.FILE) assertTrue(resources[i].getName(),target.isDirty(resources[i]));
		}
	}
	public void testOverwrite() throws CoreException, TeamException {
		IProject project = getUniqueTestProject("overwrite");
		IResource[] resources = buildResources(project, new String[] { "file1.txt", "folder1/", "folder1/b.txt" }, false);
		TargetProvider target = createProvider(project);
		target.put(resources, null);
		//Create resources with the same names but different content & upload them in the same spot:
		resources = buildResources(project, new String[] { "file1.txt", "folder1/", "folder1/b.txt" }, false);
		target.put(resources, null);

		IProject dupeProject = getUniqueTestProject("overwrite");
		TargetManager.map(dupeProject, getSite(), new Path(properties.getProperty("test_dir")).append(project.getName()));
		IResource[] freshResources = buildEmptyResources(project, new String[] { "file1.txt", "folder1/", "folder1/b.txt" }, false);
		TargetProvider dupeTarget = TargetManager.getProvider(dupeProject);
		dupeTarget.get(new IResource[] { dupeProject }, null);
		for (int i = 0; i < resources.length; i++) {
			assertEquals(resources[i],freshResources[i]);
		}
	}
	public void testIsDirtyWhenDeleted() throws CoreException, TeamException {
		IProject project = getUniqueTestProject("dirty");
		IResource[] resources = buildResources(project, new String[] { "file1.txt", "folder1/", "folder1/b.txt" }, false);
		TargetProvider target = createProvider(project);
		target.put(resources, null);
		for (int i = 0; i < resources.length; i++) {
			resources[i].delete(true, null);
		}
		sleep(1501);
		resources = buildResources(project, new String[] { "file1.txt", "folder1/", "folder1/b.txt" }, false);
		for (int i = 0; i < resources.length; i++) {
			if (resources[i].getType()==IResource.FILE) assertTrue(resources[i].getName(),target.isDirty(resources[i]));
		}
	}
	public void testIsOutOfDateWhenDeleted() throws CoreException, TeamException {
		IProject project = getUniqueTestProject("outdated");
		IResource[] resources = buildResources(project, new String[] { "file1.txt", "folder1/", "folder1/b.txt" }, false);
		TargetProvider target = createProvider(project);
		target.put(resources, null);
		for (int i = 0; i < resources.length; i++) {
			resources[i].delete(true, null);
		}
		sleep(1501);
		IProject dupeProject = getUniqueTestProject("outdated");
		IResource[] freshResources = buildResources(dupeProject, new String[] { "file1.txt", "folder1/", "folder1/b.txt" }, false);
		TargetManager.map(dupeProject, getSite(), new Path(properties.getProperty("test_dir")).append(project.getName()));
		TargetProvider dupeTarget = TargetManager.getProvider(dupeProject);
		dupeTarget.put(freshResources, null);
		for (int i = 0; i < resources.length; i++) {
			if (resources[i].getType() == IResource.FILE)
				assertTrue(target.isOutOfDate(resources[i], DEFAULT_PROGRESS_MONITOR));
		}
	}
	public void testPutWithPhantoms() throws CoreException, TeamException {
		IProject project = getUniqueTestProject("put");
		IResource[] resources = buildEmptyResources(project, new String[] { "file1.txt", "folder1/", "folder1/b.txt" }, false);
		TargetProvider target = createProvider(project);
		for (int i = 0; i < resources.length; i++) {
			resources[i].delete(true, null);
		}
		try {
			target.put(resources, null);
			fail("Shouldn't be able to put files that don't exist locally.");
		} catch (TeamException e) {} catch (RuntimeException e) {}
	}
	public void testGetWithPhantoms() throws CoreException, TeamException {
		IProject project = getUniqueTestProject("get");
		IResource[] resources = buildEmptyResources(project, new String[] { "file1.txt", "folder1/", "folder1/b.txt" }, false);
		TargetProvider target = createProvider(project);
		try {
			target.get(new IResource[] { project }, null);
			fail("Shouldn't be able to get files that don't exist");
		} catch (TeamException e) {}
	}
	public void testCanGetWithPhantoms() throws CoreException, TeamException {
		IProject project = getUniqueTestProject("canget");
		IResource[] resources = buildEmptyResources(project, new String[] { "file1.txt", "folder1/", "folder1/b.txt" }, false);
		TargetProvider target = createProvider(project);
		for (int i = 0; i < resources.length; i++) {
			assertTrue("Shouldn't be able to retrieve phantom resources.",!target.canGet(resources[i]));
		}
	}
	public void testCanPutWithPhantoms() throws CoreException, TeamException {
		IProject project = getUniqueTestProject("canput");
		IResource[] resources = buildEmptyResources(project, new String[] { "file1.txt", "folder1/", "folder1/b.txt" }, false);
		TargetProvider target = createProvider(project);
		for (int i = 0; i < resources.length; i++) {
			assertTrue("Shouldn't be able to upload phantom resources.",!target.canPut(resources[i]));
		}
	}
	/**
	 * @see TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		properties=TargetTestSetup.properties;
	}
}