/*******************************************************************************
 * Copyright (c) 2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 * IBM - Initial API and implementation
 ******************************************************************************/
package org.eclipse.core.tests.resources.session;

import org.eclipse.core.resources.*;
import org.eclipse.core.tests.harness.WorkspaceSessionTest;

/**
 * This class is a simple example of how session tests operate.  Each method 
 * starting with "test" will be invoked, in the order they are declared, in a separate 
 * runtime instance of the workspace.  Contents on disk are automatically 
 * cleaned up after the last test method is run.
 */
public class SampleSessionTest extends WorkspaceSessionTest {
public SampleSessionTest() {
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
}