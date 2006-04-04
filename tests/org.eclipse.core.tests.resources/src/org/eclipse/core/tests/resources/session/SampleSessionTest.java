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
import org.eclipse.core.tests.resources.AutomatedTests;
import org.eclipse.core.tests.resources.WorkspaceSessionTest;
import org.eclipse.core.tests.session.WorkspaceSessionTestSuite;

/**
 * This class is a simple example of how session tests operate.  Each method 
 * starting with "test" will be invoked, in the order they are declared, in a separate 
 * runtime instance of the workspace.  Contents on disk are automatically 
 * cleaned up after the last test method is run.
 */
public class SampleSessionTest extends WorkspaceSessionTest {
	public SampleSessionTest() {
		super();
	}

	public SampleSessionTest(String name) {
		super(name);
	}

	public void test1() throws Exception {
		//create a project, save workspace
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IProject p1 = workspace.getRoot().getProject("P1");
		p1.create(null);
		p1.open(null);
		IFile file = p1.getFile("foo.txt");
		file.create(getRandomContents(), true, null);
		workspace.save(true, null);
	}

	public void test2() {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IProject p1 = workspace.getRoot().getProject("P1");
		IFile file = p1.getFile("foo.txt");
		assertTrue("1.0", p1.exists());
		assertTrue("1.1", file.exists());
	}

	public static Test suite() {
		return new WorkspaceSessionTestSuite(AutomatedTests.PI_RESOURCES_TESTS, SampleSessionTest.class);
	}

}
