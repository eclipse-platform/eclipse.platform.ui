/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tests.internal.localstore;

import java.io.*;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.internal.localstore.SafeFileInputStream;
import org.eclipse.core.internal.localstore.SafeFileOutputStream;
import org.eclipse.core.internal.resources.Workspace;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.tests.resources.ResourceTest;

public class SafeFileInputOutputStreamTest extends ResourceTest {
	protected File temp;

	public SafeFileInputOutputStreamTest() {
		super();
	}

	public SafeFileInputOutputStreamTest(String name) {
		super(name);
	}

	public SafeFileOutputStream createSafeStream(File target, String errorCode) {
		return createSafeStream(target.getAbsolutePath(), null, errorCode);
	}

	public SafeFileOutputStream createSafeStream(String targetPath, String tempFilePath, String errorCode) {
		try {
			return new SafeFileOutputStream(targetPath, tempFilePath);
		} catch (IOException e) {
			fail(errorCode, e);
		}
		return null; // never happens
	}

	public InputStream getContents(java.io.File target, String errorCode) {
		try {
			return new SafeFileInputStream(target);
		} catch (IOException e) {
			fail(errorCode, e);
		}
		return null; // never happens
	}

	protected void setUp() throws Exception {
		super.setUp();
		IPath location = getRandomLocation();
		temp = location.append("temp").toFile();
		temp.mkdirs();
		assertTrue("could not create temp directory", temp.isDirectory());
	}

	public static Test suite() {
		return new TestSuite(SafeFileInputOutputStreamTest.class);
	}

	protected void tearDown() throws Exception {
		Workspace.clear(temp.getParentFile());
		super.tearDown();
	}

	public void testSafeFileInputStream() {
		File target = new File(temp, "target");
		Workspace.clear(target); // make sure there was nothing here before
		assertTrue("1.0", !target.exists());

		// define temp path
		Path parentLocation = new Path(target.getParentFile().getAbsolutePath());
		IPath tempLocation = parentLocation.append(target.getName() + ".backup");

		// we did not have a file on the destination, so we should not have a temp file
		SafeFileOutputStream safeStream = createSafeStream(target.getAbsolutePath(), tempLocation.toOSString(), "2.0");
		File tempFile = tempLocation.toFile();
		String contents = getRandomString();
		transferStreams(getContents(contents), safeStream, target.getAbsolutePath(), null);

		// now we should have a temp file
		safeStream = createSafeStream(target.getAbsolutePath(), tempLocation.toOSString(), "4.0");
		tempFile = tempLocation.toFile();
		transferStreams(getContents(contents), safeStream, target.getAbsolutePath(), null);

		assertTrue("5.0", target.exists());
		assertTrue("5.1", !tempFile.exists());
		InputStream diskContents;
		try {
			diskContents = new SafeFileInputStream(tempLocation.toOSString(), target.getAbsolutePath());
			assertTrue("5.2", compareContent(diskContents, getContents(contents)));
		} catch (IOException e) {
			fail("5.3", e);
		}
		Workspace.clear(target); // make sure there was nothing here before
	}

	public void testSimple() {
		File target = new File(temp, "target");
		Workspace.clear(target); // make sure there was nothing here before
		assertTrue("1.0", !target.exists());

		// basic use (like a FileOutputStream)
		SafeFileOutputStream safeStream = createSafeStream(target, "1.0");
		String contents = getRandomString();
		transferStreams(getContents(contents), safeStream, target.getAbsolutePath(), null);
		InputStream diskContents = getContents(target, "1.2");
		assertTrue("1.3", compareContent(diskContents, getContents(contents)));

		// update target contents
		contents = getRandomString();
		safeStream = createSafeStream(target, "2.0");
		File tempFile = new File(safeStream.getTempFilePath());
		assertTrue("2.0", tempFile.exists());
		transferStreams(getContents(contents), safeStream, target.getAbsolutePath(), null);
		diskContents = getContents(target, "3.1");
		assertTrue("3.2", compareContent(diskContents, getContents(contents)));
		assertTrue("3.3", !tempFile.exists());
		Workspace.clear(target); // make sure there was nothing here before
	}

	public void testSpecifiedTempFile() {
		File target = new File(temp, "target");
		Workspace.clear(target); // make sure there was nothing here before
		assertTrue("1.0", !target.exists());

		// define temp path
		Path parentLocation = new Path(target.getParentFile().getAbsolutePath());
		IPath tempLocation = parentLocation.append(target.getName() + ".backup");

		// we did not have a file on the destination, so we should not have a temp file
		SafeFileOutputStream safeStream = createSafeStream(target.getAbsolutePath(), tempLocation.toOSString(), "2.0");
		File tempFile = tempLocation.toFile();
		assertTrue("2.1", !tempFile.exists());

		// update target contents
		String contents = getRandomString();
		transferStreams(getContents(contents), safeStream, target.getAbsolutePath(), null);
		InputStream diskContents = getContents(target, "3.1");
		assertTrue("3.2", compareContent(diskContents, getContents(contents)));
		assertTrue("3.3", !tempFile.exists());

		// now we should have a temp file
		safeStream = createSafeStream(target.getAbsolutePath(), tempLocation.toOSString(), "4.0");
		tempFile = tempLocation.toFile();
		assertTrue("4.1", tempFile.exists());

		// update target contents
		contents = getRandomString();
		transferStreams(getContents(contents), safeStream, target.getAbsolutePath(), null);
		diskContents = getContents(target, "5.1");
		assertTrue("5.2", compareContent(diskContents, getContents(contents)));
		assertTrue("5.3", !tempFile.exists());
		Workspace.clear(target); // make sure there was nothing here before
	}
}
