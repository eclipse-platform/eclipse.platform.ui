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

import java.io.IOException;
import java.io.InputStream;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.sync.IRemoteResource;
import org.eclipse.team.internal.core.target.TargetProvider;

/**
 * A set of test cases for org.eclipse.team.core.sync.IRemoteResource
 */
public class RemoteResourceTests extends TeamTest {
	public RemoteResourceTests() {
		super();
	}
	public RemoteResourceTests(String name) {
		super(name);
	}
	public static Test suite() {
		TestSuite suite = new TestSuite(RemoteResourceTests.class);
		return new TargetTestSetup(suite);
	}
	
	protected IProject createAndPut(String projectPrefix, String[] resourceNames) throws CoreException, TeamException {
		IProject project = getUniqueTestProject(projectPrefix);
		IResource[] resources = buildResources(project, resourceNames, false);
		TargetProvider target = createProvider(project);
		target.put(resources, null);
		return project;
	}
	 
	public void testGetName() throws CoreException, TeamException {
		IProject project = createAndPut("getname", new String[] { "file1.txt", "folder1/", "folder1/b.txt" });
		TargetProvider target = getProvider(project);
		IRemoteResource remote = target.getRemoteResource();
		assertEquals(project.getName(), remote.getName());
	}
	public void testIsContainerSuccess() throws CoreException, TeamException {
		IProject project = getUniqueTestProject("iscontainer");
		IResource[] resources = buildResources(project, new String[] { "file1.txt", "folder1/", "folder1/b.txt" }, false);
		TargetProvider target = createProvider(project);
		target.put(resources, null);
		IRemoteResource remote = target.getRemoteResource();
		assertTrue(remote.isContainer());
	}
	public void testIsContainerFail() throws CoreException, TeamException {
		IProject project = createAndPut("iscontainer", new String[] { "file1.txt", "folder1/", "folder1/b.txt" });
		TargetProvider target = getProvider(project);
		IRemoteResource remote = target.getRemoteResourceFor(project.getFile("file1.txt"));
		assertTrue(!remote.isContainer());
	}
	public void testGetContents() throws CoreException, TeamException {
		IProject project = createAndPut("getname", new String[] { "file1.txt", "folder1/", "folder1/b.txt" });
		TargetProvider target = getProvider(project);
		IRemoteResource remote = target.getRemoteResourceFor(project.getFile("file1.txt"));
		InputStream jin = remote.getContents(DEFAULT_MONITOR);
		try {
			while (jin.available() > 0) {
				jin.read();
			}
		} catch (IOException e) {
			System.out.flush();
			e.printStackTrace(System.err);
			fail("Couldn't read from the input stream.");
		}
	}
	public void testMembers() throws CoreException, TeamException {
		IProject project = createAndPut("getname", new String[] { "file1.txt", "folder1/", "folder1/b.txt" });
		TargetProvider target = getProvider(project);
		IRemoteResource remote = target.getRemoteResource();
		IRemoteResource[] altResources = remote.members(DEFAULT_MONITOR);
		for (int i = 0; i < altResources.length; i++) {
			assertEquals(altResources[i], project.findMember(altResources[i].getName()));
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