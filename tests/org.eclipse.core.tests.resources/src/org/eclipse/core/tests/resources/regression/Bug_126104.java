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
package org.eclipse.core.tests.resources.regression;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.tests.resources.ResourceTest;

/**
 * Tests copying a file to a linked folder that does not exist on disk
 */
public class Bug_126104 extends ResourceTest {

	public static Test suite() {
		return new TestSuite(Bug_126104.class);
	}

	public Bug_126104(String name) {
		super(name);
	}

	public void testBug() {
		IProject project = getWorkspace().getRoot().getProject("p1");
		IFile source = project.getFile("source");
		ensureExistsInWorkspace(source, true);
		IFolder link = project.getFolder("link");
		IFileStore location = getTempStore();
		try {
			link.createLink(location.toURI(), IResource.ALLOW_MISSING_LOCAL, getMonitor());
		} catch (CoreException e) {
			fail("0.99", e);
		}
		IFile destination = link.getFile(source.getName());
		try {
			source.copy(destination.getFullPath(), IResource.NONE, getMonitor());
		} catch (CoreException e) {
			fail("1.99", e);
		}
		assertTrue("1.0", destination.exists());

		//try the same thing with move
		ensureDoesNotExistInWorkspace(destination);
		try {
			location.delete(EFS.NONE, getMonitor());
		} catch (CoreException e) {
			fail("2.99", e);
		}
		try {
			source.move(destination.getFullPath(), IResource.NONE, getMonitor());
		} catch (CoreException e) {
			fail("3.99", e);
		}
		assertTrue("3.0", !source.exists());
		assertTrue("3.1", destination.exists());
	}

}
