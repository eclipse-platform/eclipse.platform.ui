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
package org.eclipse.core.tests.resources.regression;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.tests.resources.ResourceTest;

public class PR_1GHOM0N_Test extends ResourceTest {
	public PR_1GHOM0N_Test() {
		super();
	}

	public PR_1GHOM0N_Test(String name) {
		super(name);
	}

	/**
	 * Sets up the fixture, for example, open a network connection.
	 * This method is called before a test is executed.
	 */
	protected void setUp() throws Exception {
		super.setUp();
	}

	public static Test suite() {
		return new TestSuite(PR_1GHOM0N_Test.class);
	}

	/**
	 * Tears down the fixture, for example, close a network connection.
	 * This method is called after a test is executed.
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
		ensureDoesNotExistInWorkspace(getWorkspace().getRoot());
	}

	/*
	 * Ensure that we get ADDED and OPEN in the delta when we create and open
	 * a project in a workspace runnable.
	 */
	public void test_1GEAB3C() {
		// turn off auto-build
		//	try {
		//		IWorkspaceDescription wd = getWorkspace().getDescription();
		//		wd.setAutoBuilding(false);
		//		getWorkspace().setDescription(wd);
		//	} catch (CoreException e) {
		//		fail("1.0", e);
		//	}

		// setup the project
		final IProject project = getWorkspace().getRoot().getProject("MyProject");
		try {
			IProjectDescription description = getWorkspace().newProjectDescription(project.getName());
			ICommand command = description.newCommand();
			command.setBuilderName(SimpleBuilder.BUILDER_ID);
			description.setBuildSpec(new ICommand[] {command});
			project.create(description, getMonitor());
			project.open(getMonitor());
		} catch (CoreException e) {
			fail("2.0", e);
		}

		// try and reproduce the error (there are problems when calling an incremental
		// build from within an operation...it leaves the tree immutable)
		IWorkspaceRunnable body = new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				project.build(IncrementalProjectBuilder.INCREMENTAL_BUILD, getMonitor());
				IFile file = project.getFile("test.txt");
				file.create(getRandomContents(), true, getMonitor());
			}
		};
		try {
			getWorkspace().run(body, getMonitor());
		} catch (CoreException e) {
			fail("3.0", e);
		}
	}
}
