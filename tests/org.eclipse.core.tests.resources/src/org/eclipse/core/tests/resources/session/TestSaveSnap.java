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
import org.eclipse.core.runtime.CoreException;

/**
 * Tests saving the workspace, then performing snapshots, then crashing and recovering
 */
public class TestSaveSnap extends WorkspaceSerializationTest {
/**
 * Constructor for TestSaveSnap.
 */
public TestSaveSnap() {
	super();
}
/**
 * Constructor for TestSaveSnap.
 * @param name
 */
public TestSaveSnap(String name) {
	super(name);
}
public void test1() throws Exception {
	/* create some resource handles */
	IProject project = getWorkspace().getRoot().getProject(PROJECT);
	project.create(getMonitor());
	project.open(getMonitor());

	/* full save */
	workspace.save(true, getMonitor());

	/* do more stuff */ 
	IFolder folder = project.getFolder(FOLDER);
	folder.create(true, true, getMonitor());

	//snapshot
	workspace.save(false, getMonitor());

	/* do even more stuff */
	IFile file = folder.getFile(FILE);
	byte[] bytes = "Test bytes".getBytes();
	java.io.ByteArrayInputStream in = new java.io.ByteArrayInputStream(bytes);
	file.create(in, true, getMonitor());

	//snapshot
	workspace.save(false, getMonitor());
	
	//exit without saving
}
public void test2() throws CoreException {
	IProject project = getWorkspace().getRoot().getProject(PROJECT);
	IFolder folder = project.getFolder(FOLDER);
	IFile file = folder.getFile(FILE);

	/* see if the workspace contains the resources created earlier*/
	IResource[] children = getWorkspace().getRoot().members();
	assertEquals("1.0", 1, children.length);
	assertEquals("1.1", children[0], project);
	assertTrue("1.2", project.exists());
	assertTrue("1.3", project.isOpen());
	
	assertExistsInWorkspace("1.4", new IResource[] {project, folder, file});
}
}
