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

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;

/**
 * Black box testing of {@link IFileStore#delete(int, org.eclipse.core.runtime.IProgressMonitor)}.
 */
public class DeleteTest extends FileSystemTest {

	public DeleteTest() {
		super();
	}

	public DeleteTest(String name) {
		super(name);
	}

	public void testDeleteFile() {
		IFileStore file = baseStore.getChild("child");
		ensureExists(file, false);

		assertTrue("1.0", file.fetchInfo().exists());
		try {
			file.delete(EFS.NONE, getMonitor());
		} catch (CoreException e) {
			fail("1.99", e);
		}
		assertTrue("1.1", !file.fetchInfo().exists());
	}

	public void testDeleteDirectory() {
		IFileStore dir = baseStore.getChild("child");
		ensureExists(dir, true);

		assertTrue("1.0", dir.fetchInfo().exists());
		try {
			dir.delete(EFS.NONE, getMonitor());
		} catch (CoreException e) {
			fail("1.99", e);
		}
		assertTrue("1.1", !dir.fetchInfo().exists());
	}
}
