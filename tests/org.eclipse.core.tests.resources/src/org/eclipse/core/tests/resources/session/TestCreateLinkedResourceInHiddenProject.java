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
import org.eclipse.core.internal.resources.ProjectDescription;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.tests.resources.AutomatedTests;
import org.eclipse.core.tests.session.WorkspaceSessionTestSuite;

/**
 * Tests for the bug 219568.
 */
public class TestCreateLinkedResourceInHiddenProject extends WorkspaceSerializationTest {
	/**
	 * Constructor for TestCreateLinkedResourceInHiddenProject.
	 */
	public TestCreateLinkedResourceInHiddenProject() {
		super();
	}

	/**
	 * Constructor for TestCreateLinkedResourceInHiddenProject.
	 * @param name
	 */
	public TestCreateLinkedResourceInHiddenProject(String name) {
		super(name);
	}

	public void test1() {
		/* create some resource handles */
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(PROJECT);
		try {
			IProjectDescription desc = new ProjectDescription();
			desc.setName(PROJECT);
			project.create(desc, IResource.HIDDEN, getMonitor());
			project.open(getMonitor());

			workspace.save(true, getMonitor());
		} catch (CoreException e) {
			fail("1.0", e);
		}
	}

	public void test2() {
		IPath path = getTempDir().addTrailingSeparator().append(getUniqueString());
		path.toFile().mkdir();

		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(PROJECT);
		IFolder folder = project.getFolder(getUniqueString());
		
		try {
			folder.createLink(path, IResource.NONE, getMonitor());
		} catch (CoreException e) {
			fail("2.0", e);
		}
	}

	public static Test suite() {
		return new WorkspaceSessionTestSuite(AutomatedTests.PI_RESOURCES_TESTS, TestCreateLinkedResourceInHiddenProject.class);
	}
}
