/**********************************************************************
 * Copyright (c) 2005 IBM Corporation and others. All rights reserved.   This
 * program and the accompanying materials are made available under the terms of
 * the Common Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.core.tests.filesystem;

import org.eclipse.core.filesystem.*;
import org.eclipse.core.runtime.CoreException;

/**
 * Black box testing of mkdir method.
 */
public class CreateDirectoryTest extends FileSystemTest {
	protected IFileStore topDir, subDir, file, subFile;

	public CreateDirectoryTest() {
		super();
	}

	protected void setUp() throws Exception {
		super.setUp();
		topDir = baseStore.getChild("topDir");
		subDir = topDir.getChild("subDir");
		file = baseStore.getChild("file");
		subFile = file.getChild("subFile");
		ensureExists(topDir.getParent(), true);
		ensureDoesNotExist(topDir);
		ensureDoesNotExist(file);
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		ensureDoesNotExist(topDir);
		ensureDoesNotExist(file);
	}

	public void testParentExistsDeep() {
		try {
			topDir.mkdir(EFS.NONE, getMonitor());
		} catch (CoreException e) {
			fail("1.99", e);
		}
		IFileInfo info = topDir.fetchInfo();
		assertTrue("1.1", info.exists());
		assertTrue("1.2", info.isDirectory());
	}

	public void testParentExistsShallow() {
		try {
			topDir.mkdir(EFS.SHALLOW, getMonitor());
		} catch (CoreException e) {
			fail("2.99", e);
		}
		IFileInfo info = topDir.fetchInfo();
		assertTrue("2.1", info.exists());
		assertTrue("2.2", info.isDirectory());
	}

	public void testParentFileDeep() {
		ensureExists(file, false);
		try {
			subFile.mkdir(EFS.NONE, getMonitor());
			fail("1.99");
		} catch (CoreException e) {
			//should fail
		}
		IFileInfo info = subFile.fetchInfo();
		assertTrue("2.1", !info.exists());
		assertTrue("2.2", !info.isDirectory());
	}

	public void testParentFileShallow() {
		ensureExists(file, false);
		try {
			subFile.mkdir(EFS.SHALLOW, getMonitor());
			fail("1.99");
		} catch (CoreException e) {
			//should fail
		}
		IFileInfo info = subFile.fetchInfo();
		assertTrue("2.1", !info.exists());
		assertTrue("2.2", !info.isDirectory());
	}

	public void testParentNotExistsDeep() {
		try {
			subDir.mkdir(EFS.NONE, getMonitor());
		} catch (CoreException e) {
			fail("1.99", e);
		}
		IFileInfo info = topDir.fetchInfo();
		assertTrue("1.1", info.exists());
		assertTrue("1.2", info.isDirectory());
		info = subDir.fetchInfo();
		assertTrue("1.3", info.exists());
		assertTrue("1.4", info.isDirectory());
	}

	public void testParentNotExistsShallow() {
		try {
			subDir.mkdir(EFS.SHALLOW, getMonitor());
			fail("1.99");
		} catch (CoreException e) {
			//expected
		}
		IFileInfo info = topDir.fetchInfo();
		assertTrue("1.1", !info.exists());
		assertTrue("1.2", !info.isDirectory());
		info = subDir.fetchInfo();
		assertTrue("1.3", !info.exists());
		assertTrue("1.4", !info.isDirectory());
	}
}
