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
package org.eclipse.core.tests.filesystem;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileSystem;

/**
 * Tests public API methods of the class EFS.
 * @see EFS
 */
public class EFSTest extends FileSystemTest {
	public static Test suite() {
		return new TestSuite(EFSTest.class);
	}

	public EFSTest() {
		super("");
	}

	public EFSTest(String name) {
		super(name);
	}

	public void testGetLocalFileSystem() {
		IFileSystem system = EFS.getLocalFileSystem();
		assertNotNull("1.0", system);
		assertEquals("1.1", "file", system.getScheme());
	}

	public void testGetNullFileSystem() {
		IFileSystem system = EFS.getNullFileSystem();
		assertNotNull("1.0", system);
		assertEquals("1.1", "null", system.getScheme());
	}
}
