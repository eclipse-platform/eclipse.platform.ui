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
package org.eclipse.core.tests.resources.regression;

/**
 */
import java.io.*;
import java.util.Date;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.internal.localstore.SafeChunkyInputStream;
import org.eclipse.core.internal.localstore.SafeChunkyOutputStream;
import org.eclipse.core.internal.resources.Workspace;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.tests.internal.localstore.LocalStoreTest;

//

public class LocalStoreRegressionTests extends LocalStoreTest {
	/**
	 * LocalStoreRegressionTests constructor comment.
	 */
	public LocalStoreRegressionTests(String name) {
		super(name);
	}

	public static Test suite() {
		return new TestSuite(LocalStoreRegressionTests.class);
	}

	/**
	 * 1FU4PJA: ITPCORE:ALL - refreshLocal for new file with depth zero doesn't work
	 */
	public void test_1FU4PJA() throws Throwable {
		/* initialize common objects */
		IProject project = projects[0];

		/* */
		IFile file = project.getFile("file");
		ensureExistsInFileSystem(file);
		assertTrue("1.0", !file.exists());
		file.refreshLocal(IResource.DEPTH_ZERO, null);
		assertTrue("1.1", file.exists());
	}

	/**
	 * From: 1FU4TW7: ITPCORE:ALL - Behaviour not specified for refreshLocal when parent doesn't exist
	 */
	public void test_1FU4TW7() throws Throwable {
		IFolder folder = projects[0].getFolder("folder");
		IFile file = folder.getFile("file");
		ensureExistsInFileSystem(folder);
		ensureExistsInFileSystem(file);
		file.refreshLocal(IResource.DEPTH_INFINITE, null);
		assertTrue("1.1", folder.exists());
		assertTrue("1.2", file.exists());
		ensureDoesNotExistInWorkspace(folder);
		ensureDoesNotExistInFileSystem(folder);
	}

	/**
	 * The PR reported a problem with longs, but we are testing more types here.
	 */
	public void test_1G65KR1() {
		/* evaluate test environment */
		IPath root = getWorkspace().getRoot().getLocation().append("" + new Date().getTime());
		File temp = root.toFile();
		temp.mkdirs();

		File target = new File(temp, "target");
		Workspace.clear(target); // make sure there was nothing here before
		assertTrue("1.0", !target.exists());

		// write chunks
		SafeChunkyOutputStream output = null;
		try {
			output = new SafeChunkyOutputStream(target);
			DataOutputStream dos = new DataOutputStream(output);
			try {
				dos.writeLong(1234567890l);
				output.succeed();
			} finally {
				dos.close();
			}
		} catch (IOException e) {
			fail("2.0", e);
		}

		// read chunks
		SafeChunkyInputStream input = null;
		try {
			input = new SafeChunkyInputStream(target);
			DataInputStream dis = new DataInputStream(input);
			try {
				assertEquals("3.0", dis.readLong(), 1234567890l);
			} finally {
				dis.close();
			}
		} catch (IOException e) {
			fail("3.10", e);
		}
	}
}
