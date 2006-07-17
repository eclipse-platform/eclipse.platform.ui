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
 * Tests closing a workspace without save.
 */
public class TestCloseNoSave extends WorkspaceSerializationTest {
	/**
	 * Constructor for TestCloseNoSave.
	 */
	public TestCloseNoSave() {
		super();
	}

	/**
	 * Constructor for TestCloseNoSave.
	 * @param name
	 */
	public TestCloseNoSave(String name) {
		super(name);
	}

	public void test1() throws CoreException {
		/* create some resource handles */
		IProject project = workspace.getRoot().getProject(PROJECT);
		project.create(getMonitor());
		project.open(getMonitor());
		IFolder folder = project.getFolder(FOLDER);
		folder.create(true, true, getMonitor());
		IFile file = folder.getFile(FILE);
		file.create(getRandomContents(), true, getMonitor());
	}

	public void test2() throws CoreException {
		// projects should exist immediately due to snapshot - files may or 
		// may not exist due to snapshot timing. All resources should exist after refresh.
		IResource[] members = workspace.getRoot().members();
		assertEquals("1.0", 1, members.length);
		assertTrue("1.1", members[0].getType() == IResource.PROJECT);
		IProject project = (IProject) members[0];
		assertTrue("1.2", project.exists());
		IFolder folder = project.getFolder(FOLDER);
		IFile file = folder.getFile(FILE);

		//opening the project does an automatic local refresh	
		if (!project.isOpen())
			project.open(null);

		assertEquals("2.0", 2, project.members().length);
		assertTrue("2.1", folder.exists());
		assertTrue("2.2", file.exists());
	}

	public static Test suite() {
		return new WorkspaceSessionTestSuite(AutomatedTests.PI_RESOURCES_TESTS, TestCloseNoSave.class);
	}

}
