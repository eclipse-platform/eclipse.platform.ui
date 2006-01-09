/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tests.resources;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;

public class ResourceURLTest extends ResourceTest {
	static boolean noSideEffects = false;
	protected static IResource[] interestingResources;
	protected static Set nonExistingResources = new HashSet();
	protected static Map unsynchronizedResources = new HashMap();
	protected static IPath[] interestingPaths;

	/**
	 * Need a zero argument constructor to satisfy the test harness.
	 * This constructor should not do any real work nor should it be
	 * called by user code.
	 */
	public ResourceURLTest() {
		super();
	}

	public ResourceURLTest(String name) {
		super(name);
	}

	public void doCleanup() throws Throwable {
		getWorkspace().getRoot().delete(true, true, null);
	}

	/**
	 * Returns a collection of string paths describing the standard 
	 * resource hierarchy for this test.  In the string forms, folders are
	 * represented as having trailing separators ('/').  All other resources
	 * are files.  It is generally assumed that this hierarchy will be 
	 * inserted under some solution and project structure.
	 */
	public String[] defineHierarchy() {
		return new String[] {"/", "/1/", "/1/1", "/1/2", "/1/3", "/2/", "/2/1", "/2/2", "/2/3", "/3/", "/3/1", "/3/2", "/3/3", "/4/", "/5"};
	}

	protected IProject getTestProject() {
		return getWorkspace().getRoot().getProject("testProject");
	}

	protected IProject getTestProject2() {
		return getWorkspace().getRoot().getProject("testProject2");
	}

	public static Test suite() {
		TestSuite suite = new TestSuite(ResourceURLTest.class.getName());
		suite.addTest(new ResourceURLTest("testNonExistantURLs"));
		suite.addTest(new ResourceURLTest("testBasicURLs"));
		suite.addTest(new ResourceURLTest("testExternalURLs"));
		suite.addTest(new ResourceURLTest("doCleanup"));
		return suite;
	}

	protected void tearDown() throws Exception {
		// overwrite the superclass and do nothing since our test methods build on each other
	}

	private URL getURL(IPath path) throws Throwable {
		return new URL("platform:/resource" + path.makeAbsolute().toString());
	}

	private URL getURL(IResource resource) throws Throwable {
		return getURL(resource.getFullPath());
	}

	private void checkURL(IResource resource) throws Throwable {
		URL url = getURL(resource);
		IPath file = new Path(Platform.resolve(url).getFile());
		IPath metric = resource.getLocation();
		assertEquals(metric, file);
	}

	public void testBasicURLs() throws Throwable {
		IResource[] resources = buildResources();
		ensureExistsInWorkspace(resources, true);
		for (int i = 0; i < resources.length; i++) {
			checkURL(resources[i]);
		}
	}

	public void testNonExistantURLs() throws Throwable {
		IResource[] resources = buildResources();
		for (int i = 1; i < resources.length; i++) {
			try {
				checkURL(resources[i]);
				fail("1.0");
			} catch (IOException e) {
				// expected
			}
		}
	}

	public void testExternalURLs() throws Throwable {
		IProject project = getWorkspace().getRoot().getProject("test");
		IProjectDescription desc = getWorkspace().newProjectDescription("test");
		desc.setLocation(Platform.getLocation().append("../testproject"));
		project.create(desc, null);
		project.open(null);
		IResource[] resources = buildResources(project, defineHierarchy());
		ensureExistsInWorkspace(resources, true);
		for (int i = 0; i < resources.length; i++) {
			checkURL(resources[i]);
		}
	}
}
