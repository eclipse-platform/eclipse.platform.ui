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
package org.eclipse.core.tests.internal.localstore;

import java.io.File;
import java.io.IOException;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;

/**
 * 
 */
public class FileCacheTest extends LocalStoreTest {

	public static Test suite() {
		return new TestSuite(FileCacheTest.class);
	}
	
	public FileCacheTest(String name) {
		super(name);
	}
	
	public void testLocalCache() {
		IFileStore store = getTempStore();
		try {
			store.openOutputStream(EFS.NONE, getMonitor()).close();
			File cachedFile = store.toLocalFile(EFS.CACHE, getMonitor());
			assertTrue("1.0", cachedFile.exists());
		} catch (IOException e) {
			fail("1.99", e);
		} catch (CoreException e) {
			fail("1.99", e);
		}
	}
}
