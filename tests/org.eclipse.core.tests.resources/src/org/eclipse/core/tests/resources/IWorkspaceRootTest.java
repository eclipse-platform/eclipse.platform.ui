/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tests.resources;

import java.io.ByteArrayInputStream;

import java.net.URI;
import java.net.URISyntaxException;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.filesystem.*;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.tests.internal.filesystem.wrapper.WrapperFileSystem;

public class IWorkspaceRootTest extends ResourceTest {
	public IWorkspaceRootTest() {
		super();
	}

	public IWorkspaceRootTest(String name) {
		super(name);
	}

	public static Test suite() {
		//		return new IWorkspaceRootTest("testFindFilesForLocationOnWrappedFileSystem");
		return new TestSuite(IWorkspaceRootTest.class);
	}

	protected void tearDown() throws Exception {
		IProject[] projects = getWorkspace().getRoot().getProjects();
		getWorkspace().delete(projects, true, null);
	}

	/**
	 * Tests findFilesForLocation when non-canonical paths are used (bug 155101).
	 */
	public void testFindFilesNonCanonicalPath() {
		// this test is for windows only
		if (!isWindows())
			return;
		IProject project = getWorkspace().getRoot().getProject("testFindFilesNonCanonicalPath");
		ensureExistsInWorkspace(project, true);

		IFile link = project.getFile("file.txt");
		IFileStore fileStore = getTempStore();
		createFileInFileSystem(fileStore);
		assertEquals("0.1", EFS.SCHEME_FILE, fileStore.getFileSystem().getScheme());
		IPath fileLocationLower = URIUtil.toPath(fileStore.toURI());
		fileLocationLower = fileLocationLower.setDevice(fileLocationLower.getDevice().toLowerCase());
		IPath fileLocationUpper = fileLocationLower.setDevice(fileLocationLower.getDevice().toUpperCase());
		//create the link with lower case device
		try {
			link.createLink(fileLocationLower, IResource.NONE, getMonitor());
		} catch (CoreException e) {
			fail("1.99", e);
		}
		//try to find the file using the upper case device
		IFile[] files = getWorkspace().getRoot().findFilesForLocation(fileLocationUpper);
		assertEquals("1.0", 1, files.length);
		assertEquals("1.1", link, files[0]);
	}

	/**
	 * Tests the API method findContainersForLocation.
	 */
	public void testFindContainersForLocation() {
		IWorkspaceRoot root = getWorkspace().getRoot();
		IProject p1 = root.getProject("p1");
		IProject p2 = root.getProject("p2");
		testFindContainersForLocation(p1, p2);
	}
	
	private void replaceProject(IProject project, URI newLocation) throws CoreException {
		IProjectDescription projectDesc = project.getDescription();
		projectDesc.setLocationURI(newLocation);
		project.move(projectDesc, IResource.REPLACE, null);
	}

	public void testFindContainersForLocationOnWrappedFileSystem() {
		IWorkspaceRoot root = getWorkspace().getRoot();
		IProject p1 = root.getProject("p1");
		IProject p2 = root.getProject("p2");
		ensureExistsInWorkspace(new IResource[] {p1, p2}, true);
		try {
			replaceProject(p1, WrapperFileSystem.getWrappedURI(p1.getLocationURI()));
		} catch (CoreException e) {
			fail("0.1", e);
		}
		try {
			replaceProject(p2, WrapperFileSystem.getWrappedURI(p2.getLocationURI()));
		} catch (CoreException e) {
			fail("0.2", e);
		}
		testFindContainersForLocation(p1, p2);
	}

	/**
	 * Tests the API method findContainersForLocation.
	 */
	public void testFindContainersForLocation(IProject p1, IProject p2) {
		//should find the workspace root
		IWorkspaceRoot root = getWorkspace().getRoot();
		IContainer[] result = root.findContainersForLocation(root.getLocation());
		assertEquals("1.0", 1, result.length);
		assertEquals("1.1", root, result[0]);

		//deep linked resource
		IFolder parent = p2.getFolder("parent");
		IFolder link = parent.getFolder("link");
		ensureExistsInWorkspace(new IResource[] {p1, p2, parent}, true);
		try {
			link.createLink(p1.getLocationURI(), IResource.NONE, getMonitor());
		} catch (CoreException e) {
			fail("1.99", e);
		}
		assertResources("2.0", p1, link, root.findContainersForLocation(p1.getLocation()));

		//existing folder
		IFolder existing = p2.getFolder("existing");
		ensureExistsInWorkspace(existing, true);
		assertResources("3.0", existing, root.findContainersForLocation(existing.getLocation()));
		assertResources("3.1", existing, root.findContainersForLocationURI(existing.getLocationURI()));

		//non-existing
		IFolder nonExisting = p2.getFolder("nonExisting");
		assertResources("3.2", nonExisting, root.findContainersForLocation(nonExisting.getLocation()));
		assertResources("3.3", nonExisting, root.findContainersForLocationURI(nonExisting.getLocationURI()));

		//relative path
		assertResources("3.4", existing, root.findContainersForLocation(existing.getLocation().makeRelative()));
		assertResources("3.5", nonExisting, root.findContainersForLocation(nonExisting.getLocation().makeRelative()));

		//relative URI is illegal
		URI relative = null;
		try {
			relative = new URI(null, "hello", null);
		} catch (URISyntaxException e) {
			fail("4.99", e);
		}
		try {
			root.findContainersForLocationURI(relative);
			//should fail
			fail("4.1");
		} catch (RuntimeException e) {
			//expected
		}
		//linked folder that does not overlap a project location
		IFolder otherLink = p1.getFolder("otherLink");
		IFileStore linkStore = getTempStore();
		URI location = linkStore.toURI();
		try {
			linkStore.mkdir(EFS.NONE, getMonitor());
			otherLink.createLink(location, IResource.NONE, getMonitor());
		} catch (CoreException e) {
			fail("5.99", e);
		}
		result = root.findContainersForLocationURI(location);
		assertResources("5.1", otherLink, result);

		//child of linked folder
		IFolder child = otherLink.getFolder("link-child");
		URI childLocation = linkStore.getChild(child.getName()).toURI();
		result = root.findContainersForLocationURI(childLocation);
		assertResources("5.1", child, result);

	}

	/**
	 * Tests the API method findFilesForLocation.
	 */
	public void testFindFilesForLocationOnWrappedFileSystem() {
		//should not find the workspace root
		IWorkspaceRoot root = getWorkspace().getRoot();
		IProject project = root.getProject("p1");
		ensureExistsInWorkspace(project, true);
		try {
			replaceProject(project, WrapperFileSystem.getWrappedURI(project.getLocationURI()));
		} catch (CoreException e) {
			fail("0.1", e);
		}
		testFindFilesForLocation(project);
	}

	/**
	 * Tests the API method findFilesForLocation on non-default file system.
	 */
	public void testFindFilesForLocation() {
		//should not find the workspace root
		IWorkspaceRoot root = getWorkspace().getRoot();
		testFindFilesForLocation(root.getProject("p1"));
	}

	/**
	 * Tests the API method findFilesForLocation.
	 */
	public void testFindFilesForLocation(IProject project) {
		//should not find the workspace root
		IWorkspaceRoot root = getWorkspace().getRoot();
		IFile[] result = root.findFilesForLocation(root.getLocation());
		assertEquals("1.0", 0, result.length);

		IFile existing = project.getFile("file1");
		ensureExistsInWorkspace(existing, true);

		//existing file
		final IPath existingFileLocation = existing.getLocation();
		result = root.findFilesForLocation(existingFileLocation);
		assertResources("2.0", existing, result);
		result = root.findFilesForLocationURI(existing.getLocationURI());
		assertResources("2.1", existing, result);

		//non-existing file
		IFile nonExisting = project.getFile("nonExisting");
		result = root.findFilesForLocation(nonExisting.getLocation());
		assertResources("3.1", nonExisting, result);
		result = root.findFilesForLocationURI(nonExisting.getLocationURI());
		assertResources("3.2", nonExisting, result);

		//relative path
		result = root.findFilesForLocation(existingFileLocation.makeRelative());
		assertResources("4.0", existing, result);
		result = root.findFilesForLocation(nonExisting.getLocation().makeRelative());
		assertResources("4.1", nonExisting, result);

		//existing file with different case
		if (!isCaseSensitive(existing)) {
			IPath differentCase = new Path(existingFileLocation.toOSString().toUpperCase());
			result = root.findFilesForLocation(differentCase);
			assertResources("5.0", existing, result);
			result = root.findFilesForLocationURI(existing.getLocationURI());
			assertResources("5.1", existing, result);
		}

		//linked resource
		IFolder link = project.getFolder("link");
		IFileStore linkStore = getTempStore();
		URI location = linkStore.toURI();
		try {
			linkStore.mkdir(EFS.NONE, getMonitor());
			link.createLink(location, IResource.NONE, getMonitor());
		} catch (CoreException e) {
			fail("5.99", e);
		}
		IFile child = link.getFile("link-child.txt");
		URI childLocation = linkStore.getChild(child.getName()).toURI();
		result = root.findFilesForLocationURI(childLocation);
		assertResources("2.1", child, result);
	}

	/**
	 * Asserts that the given result array contains only the given resource.
	 * @param string
	 * @param file1
	 * @param result
	 */
	private void assertResources(String message, IResource expected, IResource[] actual) {
		assertEquals(message, 1, actual.length);
		assertEquals(message, expected, actual[0]);
	}

	/**
	 * Asserts that the given result array contains only the two given resources
	 */
	private void assertResources(String message, IResource expected0, IResource expected1, IResource[] actual) {
		assertEquals(message, 2, actual.length);
		if (actual[0].equals(expected0))
			assertEquals(message, expected1, actual[1]);
		else if (actual[0].equals(expected1))
			assertEquals(message, expected0, actual[1]);
		else
			assertEquals(message, expected0, actual[0]);
	}

	/**
	 * Tests the API method getContainerForLocation.
	 */
	public void testGetContainerForLocation() {
		IWorkspaceRoot root = getWorkspace().getRoot();
		assertEquals("1.0", root, root.getContainerForLocation(root.getLocation()));
	}

	/**
	 * Tests the AP method getFile(IPath)
	 */
	public void testGetFile() {
		IWorkspaceRoot root = getWorkspace().getRoot();
		IFile file = root.getFile(new Path("//P1/a.txt"));
		assertTrue("1.0", !file.getFullPath().isUNC());
	}

	/**
	 * Tests the API method getFileForLocation
	 */
	public void testGetFileForLocation() {
		IWorkspaceRoot root = getWorkspace().getRoot();
		assertTrue("1.0", root.getFileForLocation(root.getLocation()) == null);
	}

	public void testPersistentProperty() {
		IWorkspaceRoot root = getWorkspace().getRoot();
		String value = "this is a test property value";
		QualifiedName name = new QualifiedName("test", "testProperty");
		try {
			root.setPersistentProperty(name, value);
		} catch (CoreException e) {
			fail("1.0", e);
		}

		try {
			String storedValue = root.getPersistentProperty(name);
			assertEquals("2.0", value, storedValue);
		} catch (CoreException e) {
			fail("2.1", e);
		}

		try {
			name = new QualifiedName("test", "testNonProperty");
			String storedValue = root.getPersistentProperty(name);
			assertEquals("3.0", null, storedValue);
		} catch (CoreException e) {
			fail("3.1", e);
		}
	}

	/**
	 * Since reading a persistent property doesn't normally happen inside an
	 * operation, try it inside an operation to make sure it still works.
	 * (See bug 14179).
	 */
	public void testPersistentPropertyInRunnable() {
		final IWorkspaceRoot root = getWorkspace().getRoot();
		final String value = "this is a test property value";
		final QualifiedName name = new QualifiedName("test", "testProperty");
		try {
			getWorkspace().run(new IWorkspaceRunnable() {
				public void run(IProgressMonitor monitor) throws CoreException {
					root.setPersistentProperty(name, value);
				}
			}, getMonitor());
		} catch (CoreException e) {
			fail("1.0", e);
		}

		try {
			final String[] storedValue = new String[1];
			getWorkspace().run(new IWorkspaceRunnable() {
				public void run(IProgressMonitor monitor) throws CoreException {
					storedValue[0] = root.getPersistentProperty(name);
				}
			}, getMonitor());
			assertEquals("2.0", value, storedValue[0]);
		} catch (CoreException e) {
			fail("2.1", e);
		}

		try {
			final QualifiedName name2 = new QualifiedName("test", "testNonProperty");
			final String[] storedValue = new String[1];
			getWorkspace().run(new IWorkspaceRunnable() {
				public void run(IProgressMonitor monitor) throws CoreException {
					storedValue[0] = root.getPersistentProperty(name2);
				}
			}, getMonitor());
			assertEquals("3.0", null, storedValue[0]);
		} catch (CoreException e) {
			fail("3.1", e);
		}
	}

	public void testRefreshLocal() {
		IWorkspaceRoot root = getWorkspace().getRoot();
		IProject project = root.getProject("Project");
		ensureExistsInWorkspace(project, true);
		try {
			project.close(getMonitor());
		} catch (CoreException e) {
			fail("1.0", e);
		}
		//refreshing the root shouldn't fail
		try {
			root.refreshLocal(IResource.DEPTH_INFINITE, getMonitor());
		} catch (CoreException e) {
			fail("1.1", e);
		}
	}
	
	public void testBug234343_folderInHiddenProject() {
		IWorkspaceRoot root = getWorkspace().getRoot();
		IProject hiddenProject = root.getProject(getUniqueString());
		ensureDoesNotExistInWorkspace(hiddenProject);
		try {
			hiddenProject.create(null, IResource.HIDDEN, getMonitor());
			hiddenProject.open(getMonitor());
		} catch (CoreException e) {
			fail("1.0", e);
		}

		IFolder folder = hiddenProject.getFolder("foo");
		try {
			folder.create(true, true, getMonitor());
		} catch (CoreException e) {
			fail("4.99", e);
		}

		IContainer[] containers = root.findContainersForLocation(folder.getLocation());
		assertEquals("2.0", 0, containers.length);
	}
	
	public void testBug234343_fileInHiddenProject() {
		IWorkspaceRoot root = getWorkspace().getRoot();
		IProject hiddenProject = root.getProject(getUniqueString());
		ensureDoesNotExistInWorkspace(hiddenProject);
		try {
			hiddenProject.create(null, IResource.HIDDEN, getMonitor());
			hiddenProject.open(getMonitor());
		} catch (CoreException e) {
			fail("1.0", e);
		}

		IFile file = hiddenProject.getFile("foo");
		try {
			file.create(new ByteArrayInputStream("foo".getBytes()), true, getMonitor());
		} catch (CoreException e) {
			fail("2.0", e);
		}

		IContainer[] containers = root.findContainersForLocation(file.getLocation());
		assertEquals("3.0", 0, containers.length);
	}
}
