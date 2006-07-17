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
 * Create a project, close it, save, crash, recover.  Recovered project should still be closed.
 */
public class TestSaveWithClosedProject extends WorkspaceSerializationTest {
	/**
	 * Constructor for TestSaveWithClosedProject.
	 */
	public TestSaveWithClosedProject() {
		super();
	}

	/**
	 * Constructor for TestSaveWithClosedProject.
	 * @param name
	 */
	public TestSaveWithClosedProject(String name) {
		super(name);
	}

	public void test1() {
		/* create some resource handles */
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(PROJECT);
		IFile file = project.getFile(FILE);
		try {
			project.create(getMonitor());
			project.open(getMonitor());
			file.create(getRandomContents(), true, null);
			project.close(getMonitor());

			workspace.save(true, getMonitor());
		} catch (CoreException e) {
			fail("1.99", e);
		}
	}

	public void test2() {
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(PROJECT);
		IFile file = project.getFile(FILE);

		assertTrue("1.0", project.exists());
		assertTrue("1.1", !project.isOpen());
		assertTrue("1.2", !file.exists());

		try {
			project.open(getMonitor());
		} catch (CoreException e) {
			fail("1.99", e);
		}

		assertTrue("2.0", project.isOpen());
		assertTrue("2.1", file.exists());
	}

	public static Test suite() {
		return new WorkspaceSessionTestSuite(AutomatedTests.PI_RESOURCES_TESTS, TestSaveWithClosedProject.class);
	}
}
