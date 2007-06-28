/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tests.filesystem;

import java.net.URI;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;

/**
 * Tests API methods of the class {@link org.eclipse.core.filesystem.URIUtil}.
 */
public class URIUtilTest extends FileSystemTest {
	public static Test suite() {
		return new TestSuite(URIUtilTest.class);
	}

	public URIUtilTest() {
		super("");
	}

	public URIUtilTest(String name) {
		super(name);
	}
	
	/**
	 * Tests API method {@link org.eclipse.core.filesystem.URIUtil#equals(java.net.URI, java.net.URI)}.
	 */
	public void testEquals() {
		if (EFS.getLocalFileSystem().isCaseSensitive()) {
			//test that case variants are equal
			URI one = new java.io.File("c:\\temp\\test").toURI();
			URI two = new java.io.File("c:\\TEMP\\test").toURI();
			assertTrue("1.0", URIUtil.equals(one, two));
		}

	}

	/**
	 * Tests API method {@link org.eclipse.core.filesystem.URIUtil#toURI(org.eclipse.core.runtime.IPath)}.
	 */
	public void testPathToURI() {
		if (Platform.getOS().equals(Platform.OS_WIN32)) {
			//path with spaces
			assertEquals("1.0", "/c:/temp/with spaces", URIUtil.toURI("c:\\temp\\with spaces").getSchemeSpecificPart());
		} else {
			//path with spaces
			assertEquals("2.0", "/tmp/with spaces", URIUtil.toURI("/tmp/with spaces").getSchemeSpecificPart());
		}
	}

	/**
	 * Tests API method {@link org.eclipse.core.filesystem.URIUtil#toURI(String)}.
	 */
	public void testStringToURI() {
		if (Platform.getOS().equals(Platform.OS_WIN32)) {
			assertEquals("1.0", "/c:/temp/with spaces", URIUtil.toURI(new Path("c:\\temp\\with spaces")).getSchemeSpecificPart());
		} else {
			assertEquals("1.0", "/tmp/with spaces", URIUtil.toURI(new Path("/tmp/with spaces")).getSchemeSpecificPart());
		}
	}

	/**
	 * Tests API method {@link org.eclipse.core.filesystem.URIUtil#toPath(java.net.URI)}.
	 */
	public void testToPath() {
		//TODO
	}
}
