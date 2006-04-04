/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tests.resources.session;

import junit.framework.Test;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.tests.resources.AutomatedTests;
import org.eclipse.core.tests.session.WorkspaceSessionTestSuite;

/**
 * Tests recovery after adding a project and not saving
 */
public class TestSaveCreateProject extends WorkspaceSerializationTest {
	/**
	 * Constructor for TestSaveCreateProject.
	 */
	public TestSaveCreateProject() {
		super();
	}

	/**
	 * Constructor for TestSaveCreateProject.
	 * @param name
	 */
	public TestSaveCreateProject(String name) {
		super(name);
	}

	public void test1() {
		/* create some resource handles */
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(PROJECT);
		try {
			workspace.save(true, getMonitor());

			project.create(getMonitor());
			project.open(getMonitor());
		} catch (CoreException e) {
			fail("1.0", e);
		}
	}

	public void test2() {
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		assertTrue("1.0", root.exists());
		try {
			IResource[] children = root.members();
			assertEquals("1.2", 1, children.length);
			IProject project = (IProject) children[0];
			assertTrue("1.3", project.exists());
			assertEquals("1.4", PROJECT, project.getName());
		} catch (CoreException e) {
			fail("1.99", e);
		}
	}

	public static Test suite() {
		return new WorkspaceSessionTestSuite(AutomatedTests.PI_RESOURCES_TESTS, TestSaveCreateProject.class);
	}
}
