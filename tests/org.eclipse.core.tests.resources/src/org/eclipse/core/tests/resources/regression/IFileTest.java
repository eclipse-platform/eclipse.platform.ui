/**********************************************************************
 * Copyright (c) 2000, 2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0 which
 * accompanies this distribution, and is available at http://www.eclipse.
 * org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.core.tests.resources.regression;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.boot.BootLoader;
import org.eclipse.core.internal.localstore.CoreFileSystemLibrary;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.tests.harness.EclipseWorkspaceTest;

public class IFileTest extends EclipseWorkspaceTest {
/**
 * Constructor for IFileTest.
 */
public IFileTest() {
	super();
}
/**
 * Constructor for IFileTest.
 * @param name
 */
public IFileTest(String name) {
	super(name);
}
public static Test suite() {
	return new TestSuite(IFileTest.class);
}
/**
 * Bug states that the error code in the CoreException which is thrown when
 * you try to create a file in a read-only folder on Linux should be 
 * FAILED_WRITE_LOCAL.
 */
public void testBug25658() {
	
	// We need to know whether or not we can set the folder to be read-only
	// in order to perform this test.
	if (!CoreFileSystemLibrary.usingNatives())
		return;
	
	// Don't test this on Windows
	if (BootLoader.getOS().equals(BootLoader.OS_WIN32))
		return;
	
	IProject project = getWorkspace().getRoot().getProject("MyProject");
	IFolder folder = project.getFolder("folder");
	ensureExistsInWorkspace(new IResource[] {project, folder}, true);
	IFile file = folder.getFile("file.txt");

	try {
		folder.setReadOnly(true);
		assertTrue("0.0", folder.isReadOnly());
		try {
			file.create(getRandomContents(), true, getMonitor());
			fail("0.1");
		} catch (CoreException e) {
			assertEquals("0.2", IResourceStatus.FAILED_WRITE_LOCAL, e.getStatus().getCode());
		}
	} finally {
		folder.setReadOnly(false);
	}
}
}

