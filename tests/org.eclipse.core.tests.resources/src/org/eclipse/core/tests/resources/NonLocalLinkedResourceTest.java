/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tests.resources;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.filesystem.*;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.tests.internal.filesystem.ram.MemoryFileSystem;

/**
 * Tests behaviour of manipulating linked resources that are not linked into
 * the local file system.
 */
public class NonLocalLinkedResourceTest extends ResourceTest {
	private int nextFolder = 0;

	public static Test suite() {
		return new TestSuite(NonLocalLinkedResourceTest.class);
	}

	public NonLocalLinkedResourceTest() {

	}

	public NonLocalLinkedResourceTest(String name) {
		super(name);
	}

	/**
	 * Creates a folder in the test file system with the given name
	 */
	protected IFileStore createFolderStore(String name) {
		IFileSystem system = getFileSystem();
		IFileStore store = system.getStore(Path.ROOT.append(name));
		try {
			store.mkdir(EFS.NONE, getMonitor());
		} catch (CoreException e) {
			fail("createFolderStore", e);
		}
		return store;
	}

	protected IFileSystem getFileSystem() {
		try {
			return EFS.getFileSystem(MemoryFileSystem.SCHEME_MEMORY);
		} catch (CoreException e) {
			fail("Test file system missing", e);
		}
		//can't get here
		return null;
	}

	protected IFileStore getTempStore() {
		IFileSystem system = getFileSystem();
		IFileStore store;
		do {
			store = system.getStore(Path.ROOT.append(Integer.toString(nextFolder++)));
		} while (store.fetchInfo().exists());
		return store;
	}

	public void testCopyFile() {
		IFileStore sourceStore = createFolderStore("source");
		IFileStore destinationStore = createFolderStore("destination");
		IProject project = getWorkspace().getRoot().getProject("project");
		IFolder source = project.getFolder("source");
		IFolder destination = project.getFolder("destination");
		IFile sourceFile = source.getFile("file.txt");
		IFile destinationFile = destination.getFile(sourceFile.getName());
		IFile localFile = project.getFile(sourceFile.getName());

		//setup initial resources
		ensureExistsInWorkspace(project, true);
		try {
			source.createLink(sourceStore.toURI(), IResource.NONE, getMonitor());
			destination.createLink(destinationStore.toURI(), IResource.NONE, getMonitor());
			sourceFile.create(getRandomContents(), IResource.NONE, getMonitor());
		} catch (CoreException e) {
			fail("0.99", e);
		}

		//copy to linked destination should succeed
		try {
			sourceFile.copy(destinationFile.getFullPath(), IResource.NONE, getMonitor());
		} catch (CoreException e) {
			fail("1.0", e);
		}
		//copy to local destination should succeed
		try {
			sourceFile.copy(localFile.getFullPath(), IResource.NONE, getMonitor());
		} catch (CoreException e) {
			fail("2.0", e);
		}
		//copy from local to non local
		ensureDoesNotExistInWorkspace(destinationFile);
		//copy from local to non local
		try {
			localFile.copy(destinationFile.getFullPath(), IResource.NONE, getMonitor());
		} catch (CoreException e) {
			fail("3.0", e);
		}

		//copy to self should fail 
		try {
			localFile.copy(localFile.getFullPath(), IResource.NONE, getMonitor());
			fail("4.0");
		} catch (CoreException e) {
			//should fail
		}
	}
}
