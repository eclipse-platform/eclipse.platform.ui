/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tests.internal.localstore;

import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.Set;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.internal.localstore.IHistoryStore;
import org.eclipse.core.internal.resources.*;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.tests.resources.ResourceTest;
import org.osgi.framework.Bundle;

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
		Bundle compatibility = Platform.getBundle("org.eclipse.core.resources.compatibility");
		if (compatibility == null || compatibility.getState() != Bundle.RESOLVED)
			// compatibility fragment not available
			return;
		IPath baseLocation = getRandomLocation();
		IHistoryStore original = null;
		boolean success = false;
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
			original = createHistoryStore("1", baseLocation, 0x100, false, false, false);
			for (int i = 0; i < files.length; i++)
				for (int j = 0; j < 10; j++) {
					IFileStore store = ((Resource) files[i]).getStore();
					original.addState(files[i].getFullPath(), store, store.fetchInfo(), false);
				}
			// close existing history store so all data is committed
			try {
				original.shutdown(getMonitor());
			} catch (CoreException e) {
				fail("2.0", e);
			}
			// do the conversion
			IHistoryStore destination = null;
			destination = createHistoryStore("3", baseLocation, 0x100, true, true, false);
			// reopen history store for comparison
			original = createHistoryStore("4", baseLocation, 0x100, false, false, false);
			compare("5", original, destination);
			success = true;
		} finally {
			if (original != null)
				try {
					original.shutdown(getMonitor());
				} catch (CoreException e) {
					if (success)
						fail("99.99", e);
				}
			ensureDoesNotExistInFileSystem(baseLocation.toFile());
		}
	}

	private IHistoryStore createHistoryStore(String tag, IPath location, int limit, boolean newImpl, boolean convert, boolean rename) {
		try {
			return ResourcesCompatibilityHelper.createHistoryStore(location, limit, newImpl, convert, rename);
		} catch (ClassNotFoundException e) {
			fail(tag + ".1", e);
		} catch (NoSuchMethodException e) {
			fail(tag + ".2", e);
		} catch (IllegalAccessException e) {
			fail(tag + ".3", e);
		} catch (InvocationTargetException e) {
			fail(tag + ".4", e.getTargetException());
		}
		// never gets here
		return null;
	}
}
