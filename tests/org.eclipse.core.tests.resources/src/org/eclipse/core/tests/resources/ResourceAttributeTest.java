/**********************************************************************
 * Copyright (c) 2005 IBM Corporation and others. All rights reserved.   This
 * program and the accompanying materials are made available under the terms of
 * the Common Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.core.tests.resources;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;

/**
 * 
 */
public class ResourceAttributeTest extends ResourceTest {

	public static Test suite() {
		return new TestSuite(ResourceAttributeTest.class);
	}

	public ResourceAttributeTest() {
		super();
	}

	public ResourceAttributeTest(String name) {
		super(name);
	}

	private void setArchive(IResource resource, boolean value) throws CoreException {
		ResourceAttributes attributes = resource.getResourceAttributes();
		assertNotNull("setAchive for null attributes", attributes);
		attributes.setArchive(value);
		resource.setResourceAttributes(attributes);
	}

	private void setExecutable(IResource resource, boolean value) throws CoreException {
		ResourceAttributes attributes = resource.getResourceAttributes();
		assertNotNull("setExecutable for null attributes", attributes);
		attributes.setExecutable(value);
		resource.setResourceAttributes(attributes);
	}

	private void setHidden(IResource resource, boolean value) throws CoreException {
		ResourceAttributes attributes = resource.getResourceAttributes();
		assertNotNull("setHidden for null attributes", attributes);
		attributes.setHidden(value);
		resource.setResourceAttributes(attributes);
	}

	private void setReadOnly(IResource resource, boolean value) throws CoreException {
		ResourceAttributes attributes = resource.getResourceAttributes();
		assertNotNull("setReadOnly for null attributes", attributes);
		attributes.setReadOnly(value);
		resource.setResourceAttributes(attributes);
	}

	public void testAttributeArchive() {
		// archive bit only implemented on windows
		if (!Platform.getOS().equals(Platform.OS_WIN32))
			return;
		IProject project = getWorkspace().getRoot().getProject("Project");
		IFile file = project.getFile("target");
		ensureExistsInWorkspace(file, getRandomContents());

		try {
			// file 
			// bit is set already for a new file
			assertTrue("1.0", file.getResourceAttributes().isArchive());
			setArchive(file, false);
			assertTrue("1.2", !file.getResourceAttributes().isArchive());
			setArchive(file, true);
			assertTrue("1.4", file.getResourceAttributes().isArchive());

			// folder
			// bit is not set already for a new folder
			assertTrue("2.0", !project.getResourceAttributes().isArchive());
			setArchive(project, true);
			assertTrue("2.2", project.getResourceAttributes().isArchive());
			setArchive(project, false);
			assertTrue("2.4", !project.getResourceAttributes().isArchive());
		} catch (CoreException e1) {
			fail("2.99", e1);
		}

		/* remove trash */
		try {
			project.delete(true, getMonitor());
		} catch (CoreException e) {
			fail("3.0", e);
		}
	}

	public void testAttributeExecutable() {
		// executable bit not implemented on windows
		if (Platform.getOS().equals(Platform.OS_WIN32))
			return;
		IProject project = getWorkspace().getRoot().getProject("Project");
		IFile file = project.getFile("target");
		ensureExistsInWorkspace(file, getRandomContents());

		try {
			// file
			assertTrue("1.0", !file.getResourceAttributes().isExecutable());
			setExecutable(file, true);
			assertTrue("1.2", file.getResourceAttributes().isExecutable());
			setExecutable(file, false);
			assertTrue("1.4", !file.getResourceAttributes().isExecutable());

			// folder
			//folder is executable initially
			assertTrue("2.0", project.getResourceAttributes().isExecutable());
			setExecutable(project, false);
			assertTrue("2.2", !project.getResourceAttributes().isExecutable());
			setExecutable(project, true);
			assertTrue("2.4", project.getResourceAttributes().isExecutable());
		} catch (CoreException e1) {
			fail("2.99", e1);
		}

		/* remove trash */
		try {
			project.delete(true, getMonitor());
		} catch (CoreException e) {
			fail("3.0", e);
		}
	}

	/**
	 * When the executable bit is cleared on a folder, it effectively
	 * causes the children of that folder to be removed from the
	 * workspace because the folder contents can no longer be listed.
	 * A refresh should happen automatically when the executable
	 * bit on a folder is changed. See bug 109979 for details.
	 */
	public void testRefreshExecutableOnFolder() {
		// only test on platforms that implement the executable bit
		if ((EFS.getLocalFileSystem().attributes() & EFS.ATTRIBUTE_EXECUTABLE) == 0)
			return;
		IProject project = getWorkspace().getRoot().getProject("testRefreshExecutableOnFolder");
		IFolder folder = project.getFolder("folder");
		IFile file = folder.getFile("file");
		ensureExistsInWorkspace(file, getRandomContents());

		try {
			//folder is executable initially and the file should exist
			assertTrue("1.0", project.getResourceAttributes().isExecutable());
			assertTrue("1.1", file.exists());
			
			setExecutable(folder, false);
			waitForRefresh();
			
			boolean wasExecutable = folder.getResourceAttributes().isExecutable();
			boolean fileExists = file.exists();

			//set the folder executable before asserting anything, otherwise cleanup will fail
			setExecutable(folder, true);

			assertTrue("2.1", !wasExecutable);
			assertTrue("2.2", !fileExists);
			
		} catch (CoreException e1) {
			fail("2.99", e1);
		}

		/* remove trash */
		try {
			project.delete(true, getMonitor());
		} catch (CoreException e) {
			fail("3.0", e);
		}
	}

	public void testAttributeHidden() {
		// hidden bit only implemented on windows
		if (!Platform.getOS().equals(Platform.OS_WIN32))
			return;
		IProject project = getWorkspace().getRoot().getProject("Project");
		IFile file = project.getFile("target");
		ensureExistsInWorkspace(file, getRandomContents());

		try {
			// file 
			assertTrue("1.0", !file.getResourceAttributes().isHidden());
			setHidden(file, true);
			assertTrue("1.2", file.getResourceAttributes().isHidden());
			setHidden(file, false);
			assertTrue("1.4", !file.getResourceAttributes().isHidden());

			// folder
			assertTrue("2.0", !project.getResourceAttributes().isHidden());
			setHidden(project, true);
			assertTrue("2.2", project.getResourceAttributes().isHidden());
			setHidden(project, false);
			assertTrue("2.4", !project.getResourceAttributes().isHidden());
		} catch (CoreException e1) {
			fail("2.99", e1);
		}

		/* remove trash */
		try {
			project.delete(true, getMonitor());
		} catch (CoreException e) {
			fail("3.0", e);
		}
	}

	public void testAttributeReadOnly() {
		IProject project = getWorkspace().getRoot().getProject("Project");
		IFile file = project.getFile("target");
		ensureExistsInWorkspace(file, getRandomContents());

		try {
			// file
			assertTrue("1.0", !file.getResourceAttributes().isReadOnly());
			setReadOnly(file, true);
			assertTrue("1.2", file.getResourceAttributes().isReadOnly());
			setReadOnly(file, false);
			assertTrue("1.4", !file.getResourceAttributes().isReadOnly());

			// folder
			assertTrue("2.0", !project.getResourceAttributes().isReadOnly());
			setReadOnly(project, true);
			assertTrue("2.2", project.getResourceAttributes().isReadOnly());
			setReadOnly(project, false);
			assertTrue("2.4", !project.getResourceAttributes().isReadOnly());
		} catch (CoreException e1) {
			fail("2.99", e1);
		}

		/* remove trash */
		try {
			project.delete(true, getMonitor());
		} catch (CoreException e) {
			fail("3.0", e);
		}
	}

	public void testNonExistingResource() {
		//asking for attributes of a non-existent resource should return null
		IProject project = getWorkspace().getRoot().getProject("testNonExistingResource");
		IFolder folder = project.getFolder("folder");
		IFile file = project.getFile("file");
		ensureDoesNotExistInWorkspace(project);
		assertNull("1.0", project.getResourceAttributes());
		assertNull("1.1", folder.getResourceAttributes());
		assertNull("1.2", file.getResourceAttributes());

		//now create the resources and ensure non-null result
		ensureExistsInWorkspace(project, true);
		ensureExistsInWorkspace(folder, true);
		ensureExistsInWorkspace(file, true);
		assertNotNull("2.0", project.getResourceAttributes());
		assertNotNull("2.1", folder.getResourceAttributes());
		assertNotNull("2.2", file.getResourceAttributes());
	}

}
