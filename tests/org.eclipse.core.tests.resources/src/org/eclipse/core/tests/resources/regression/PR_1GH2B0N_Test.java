/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tests.resources.regression;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.*;
import org.eclipse.core.tests.resources.ResourceTest;

public class PR_1GH2B0N_Test extends ResourceTest {
	/**
	 * Constructor for PR_1GH2B0N_Test
	 */
	public PR_1GH2B0N_Test() {
		super();
	}

	/**
	 * Constructor for PR_1GH2B0N_Test
	 */
	public PR_1GH2B0N_Test(String name) {
		super(name);
	}

	public static Test suite() {
		return new TestSuite(PR_1GH2B0N_Test.class);
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		ensureDoesNotExistInWorkspace(getWorkspace().getRoot());
	}

	public void test_1GH2B0N() {
		IPath path = getTempDir().append("1GH2B0N");
		IProject project = getWorkspace().getRoot().getProject("MyProject");
		IProjectDescription description = getWorkspace().newProjectDescription("MyProject");
		IPath projectLocation = path.append(project.getName());
		description.setLocation(projectLocation);
		try {
			try {
				project.create(description, getMonitor());
			} catch (CoreException e) {
				fail("1.0", e);
			}
			try {
				project.open(getMonitor());
			} catch (CoreException e) {
				fail("1.1", e);
			}

			IProject project2 = getWorkspace().getRoot().getProject("MyProject2");
			IStatus status = getWorkspace().validateProjectLocation(project2, project.getLocation().append(project2.getName()));
			assertTrue("2.0", !status.isOK());
		} finally {
			ensureDoesNotExistInWorkspace(project);
			ensureDoesNotExistInFileSystem(projectLocation.toFile());
		}
	}
}