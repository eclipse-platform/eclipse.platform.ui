/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tests.internal.localstore;

import java.util.Iterator;
import java.util.Set;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.internal.localstore.*;
import org.eclipse.core.internal.resources.FileState;
import org.eclipse.core.internal.resources.Workspace;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.tests.resources.ResourceTest;

/**
 * Tests for the conversion from the old to the new local history 
 * implementation.
 */
public class HistoryStoreConversionTest extends ResourceTest {

	public static Test suite() {
		TestSuite suite = new TestSuite(HistoryStoreConversionTest.class);
		return suite;
	}

	public HistoryStoreConversionTest(String name) {
		super(name);
	}

	private void compare(String tag, IHistoryStore base, IHistoryStore another) {
		Set baseFilesWithStates = base.allFiles(Path.ROOT, IResource.DEPTH_INFINITE, getMonitor());
		Set anotherFilesWithStates = another.allFiles(Path.ROOT, IResource.DEPTH_INFINITE, getMonitor());
		assertEquals(tag + ".1", baseFilesWithStates, anotherFilesWithStates);
		for (Iterator i = baseFilesWithStates.iterator(); i.hasNext();) {
			IPath filePath = (IPath) i.next();
			IFileState[] baseStates = base.getStates(filePath, getMonitor());
			IFileState[] anotherStates = another.getStates(filePath, getMonitor());
			assertEquals(tag + ".2." + filePath, baseStates.length, anotherStates.length);
			for (int j = 0; j < baseStates.length; j++) {
				assertEquals(tag + ".3." + j, ((FileState) baseStates[j]).getUUID(), ((FileState) anotherStates[j]).getUUID());
				assertEquals(tag + ".4." + j, baseStates[j].getModificationTime(), anotherStates[j].getModificationTime());
			}
		}
	}

	public void testConversion() {
		IPath baseLocation = getRandomLocation();
		HistoryStore original = null;
		try {
			IProject project1 = getWorkspace().getRoot().getProject("proj1");
			IFile file11 = project1.getFile("file11.txt");
			IFolder folder11 = project1.getFolder("folder11");
			IFile file111 = folder11.getFile("file111.txt");
			IProject project2 = getWorkspace().getRoot().getProject("proj2");
			IFile file21 = project2.getFile("file21.txt");
			IFolder folder21 = project2.getFolder("folder21");
			IFile file211 = folder21.getFile("file211.txt");

			IResource[] files = {file11, file111, file21, file211};
			ensureExistsInWorkspace(files, true);

			assertTrue("0.1", baseLocation.toFile().mkdirs());
			original = new HistoryStore((Workspace) getWorkspace(), baseLocation, 0x100);

			for (int i = 0; i < files.length; i++)
				for (int j = 0; j < 10; j++)
					original.addState(files[i].getFullPath(), files[i].getLocation().toFile(), files[i].getLocation().toFile().lastModified(), false);
			// close existing history store so all data is committed
			original.shutdown(getMonitor());
			// do the conversion
			HistoryStore2 destination = new HistoryStore2((Workspace) getWorkspace(), baseLocation, 0x100);
			new HistoryStoreConverter().convertHistory((Workspace) getWorkspace(), baseLocation, 0x100, destination, false);

			// reopen history store for comparison
			original = new HistoryStore((Workspace) getWorkspace(), baseLocation, 0x100);
			compare("1", original, destination);
		} finally {
			if (original != null)
				original.shutdown(getMonitor());
			ensureDoesNotExistInFileSystem(baseLocation.toFile());
		}
	}
}
