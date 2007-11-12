/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tests.filesystem;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import org.eclipse.core.filesystem.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.tests.harness.CoreTest;
import org.eclipse.core.tests.harness.FileSystemHelper;
import org.eclipse.core.tests.internal.filesystem.ram.MemoryTree;

/**
 * Abstract superclass for all generic file system tests.
 */
public abstract class FileSystemTest extends CoreTest {
	protected IFileStore baseStore, localFileBaseStore;

	public FileSystemTest() {
		super();
	}

	public FileSystemTest(String name) {
		super(name);
	}
	
	protected void ensureDoesNotExist(IFileStore store) {
		try {
			store.delete(EFS.NONE, getMonitor());
			assertTrue("1.0", !store.fetchInfo().exists());
		} catch (CoreException e) {
			fail("ensureDoesNotExist", e);
		}
	}

	/**
	 * Asserts that a file store exists.
	 * 
	 * @param message The failure message if the assertion fails
	 * @param store The store to check for existence
	 */
	protected void assertExists(String message, IFileStore store) {
		IFileInfo info = store.fetchInfo();
		assertTrue(message, info.exists());
		//check that the parent knows about it
		try {
			IFileInfo[] children = store.getParent().childInfos(EFS.NONE, getMonitor());
			for (int i = 0; i < children.length; i++) {
				if (children[i].getName().equals(store.getName()))
					return;
			}
			assertTrue(message, false);
		} catch (CoreException e) {
			fail(message, e);
		}
	}

	/**
	 * Ensures that the provided store exists, as either a file or directory.
	 */
	protected void ensureExists(IFileStore store, boolean directory) {
		try {
			if (directory) {
				store.mkdir(EFS.NONE, getMonitor());
				final IFileInfo info = store.fetchInfo();
				assertTrue("1.0", info.exists());
				assertTrue("1.1", info.isDirectory());
			} else {
				OutputStream out = store.openOutputStream(EFS.NONE, getMonitor());
				out.write(5);
				out.close();
				final IFileInfo info = store.fetchInfo();
				assertTrue("1.5", info.exists());
				assertTrue("1.6", !info.isDirectory());
			}
		} catch (CoreException e) {
			fail("ensureExists", e);
		} catch (IOException e) {
			fail("ensureExists", e);
		}

	}

	protected void setUp() throws Exception {
		super.setUp();
		//the base file system to be tested is setup here.
		//to test a different file system implementation, set up
		//its base directory here.
		MemoryTree.TREE.deleteAll();
		baseStore = EFS.getStore(URI.create("mem:/baseStore"));
		baseStore.mkdir(EFS.NONE, null);
		localFileBaseStore = EFS.getLocalFileSystem().getStore(FileSystemHelper.getRandomLocation(getTempDir()));
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		baseStore.delete(EFS.NONE, null);
		MemoryTree.TREE.deleteAll();
		localFileBaseStore.delete(EFS.NONE, null);
	}
}
