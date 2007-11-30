/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tests.resources;

import java.util.*;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.internal.resources.Resource;
import org.eclipse.core.internal.resources.Workspace;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;

public class IProjectTest extends ResourceTest {

	public static Test suite() {
		return new TestSuite(IProjectTest.class);

		//		TestSuite suite = new TestSuite();
		//		suite.addTest(new IProjectTest("testReplaceLocation"));
		//		return suite;
	}

	public IProjectTest() {
		super();
	}

	public IProjectTest(String name) {
		super(name);
	}

	public void ensureExistsInWorkspace(final IProject project, final IProjectDescription description) {
		if (project == null)
			return;
		IWorkspaceRunnable body = new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				project.create(description, monitor);
				project.open(monitor);
			}
		};
		try {
			getWorkspace().run(body, null);
		} catch (CoreException e) {
			fail("#ensureExistsInWorkspace(IProject, IProjectDescription): " + project.getFullPath(), e);
		}
	}

	public void setGetPersistentProperty(IResource target) throws CoreException {
		String value = "this is a test property value";
		QualifiedName name = new QualifiedName("itp-test", "testProperty");
		target.setPersistentProperty(name, value);
		// see if we can get the property
		assertTrue("get not equal set", target.getPersistentProperty(name).equals(value));
		// see what happens if we get a non-existant property
		name = new QualifiedName("eclipse-test", "testNonProperty");
		assertNull("non-existant persistent property not missing", target.getPersistentProperty(name));
	}

	protected void setUp() throws Exception {
		super.setUp();
	}

	/**
	 * Note that project copying is tested more thoroughly by IResourceTest#testCopy.
	 */
	public void testCopy() {
		IProject project = getWorkspace().getRoot().getProject("Source");
		try {
			project.create(getMonitor());
			project.open(getMonitor());
			project.createMarker(IMarker.TASK);
			IProject destination = getWorkspace().getRoot().getProject("Destination");

			assertTrue("1.0", !destination.exists());
			project.copy(destination.getFullPath(), IResource.NONE, getMonitor());
			assertTrue("1.1", destination.exists());
			assertEquals("1.2", 0, destination.findMarkers(IMarker.TASK, true, IResource.DEPTH_INFINITE).length);
		} catch (CoreException e) {
			fail("1.99", e);
		}
	}

	/**
	 * Tests the API method IProject#getNature
	 */
	public void testGetNature() {
		IWorkspace ws = ResourcesPlugin.getWorkspace();
		IProject project = ws.getRoot().getProject("Project");

		//getNature on non-existent project should fail
		try {
			project.getNature(NATURE_SIMPLE);
			fail("1.0");
		} catch (CoreException e) {
			// expected
		}
		try {
			project.getNature(NATURE_MISSING);
			fail("1.1");
		} catch (CoreException e) {
			// expected
		}
		try {
			project.create(getMonitor());
		} catch (CoreException e) {
			fail("1.99", e);
		}
		//getNature on closed project should fail
		try {
			project.getNature(NATURE_SIMPLE);
			fail("2.0");
		} catch (CoreException e) {
			// expected
		}
		try {
			project.getNature(NATURE_MISSING);
			fail("2.1");
		} catch (CoreException e) {
			// expected
		}
		try {
			project.open(getMonitor());
		} catch (CoreException e) {
			fail("2.99", e);
		}
		//getNature on open project with no natures
		try {
			assertNull("3.0", project.getNature(NATURE_SIMPLE));
			assertNull("3.1", project.getNature(NATURE_MISSING));
			assertNull("3.2", project.getNature(NATURE_EARTH));
		} catch (CoreException e) {
			fail("3.99", e);
		}
		try {
			IProjectDescription desc = project.getDescription();
			desc.setNatureIds(new String[] {NATURE_SIMPLE});
			project.setDescription(desc, getMonitor());
		} catch (CoreException e) {
			fail("4.99", e);
		}
		//getNature on open project with natures
		IProjectNature nature = null;
		try {
			nature = project.getNature(NATURE_SIMPLE);
			assertNotNull("5.0", nature);
			assertNull("5.1", project.getNature(NATURE_MISSING));
			assertNull("5.2", project.getNature(NATURE_EARTH));
		} catch (CoreException e) {
			fail("5.99", e);
			return;
		}
		assertEquals("6.0", project, nature.getProject());
		
		//ensure nature is preserved on copy
		IProject project2 = getWorkspace().getRoot().getProject("testGetNature.Destination");
		IProjectNature nature2 = null;
		try {
			project.copy(project2.getFullPath(), IResource.NONE, getMonitor());
			nature2 = project2.getNature(NATURE_SIMPLE);
		} catch (CoreException e) {
			fail("6.99", e);
			return;
		}
		assertNotNull("7.0", nature2);
		assertEquals("7.1", project2, nature2.getProject());
		assertEquals("7.2", project, nature.getProject());
	}

	/**
	 * Tests the API method IProject#hasNature.
	 */
	public void testHasNature() {
		IWorkspace ws = ResourcesPlugin.getWorkspace();
		IProject project = ws.getRoot().getProject("Project");

		//hasNature on non-existent project should fail
		try {
			project.hasNature(NATURE_SIMPLE);
			fail("1.0");
		} catch (CoreException e) {
			// expected
		}
		try {
			project.hasNature(NATURE_MISSING);
			fail("1.1");
		} catch (CoreException e) {
			// expected
		}
		try {
			project.create(getMonitor());
		} catch (CoreException e) {
			fail("1.99", e);
		}
		//hasNature on closed project should fail
		try {
			project.hasNature(NATURE_SIMPLE);
			fail("2.0");
		} catch (CoreException e) {
			// expected
		}
		try {
			project.hasNature(NATURE_MISSING);
			fail("2.1");
		} catch (CoreException e) {
			// expected
		}
		try {
			project.open(getMonitor());
		} catch (CoreException e) {
			fail("2.99", e);
		}
		//hasNature on open project with no natures
		try {
			assertTrue("3.0", !project.hasNature(NATURE_SIMPLE));
			assertTrue("3.1", !project.hasNature(NATURE_MISSING));
			assertTrue("3.2", !project.hasNature(NATURE_EARTH));
		} catch (CoreException e) {
			fail("3.99", e);
		}
		try {
			IProjectDescription desc = project.getDescription();
			desc.setNatureIds(new String[] {NATURE_SIMPLE});
			project.setDescription(desc, getMonitor());
		} catch (CoreException e) {
			fail("4.99", e);
		}
		//hasNature on open project with natures
		try {
			assertTrue("5.0", project.hasNature(NATURE_SIMPLE));
			assertTrue("5.1", !project.hasNature(NATURE_MISSING));
			assertTrue("5.2", !project.hasNature(NATURE_EARTH));
		} catch (CoreException e) {
			fail("5.99", e);
		}
	}

	/**
	 * Tests creation and manipulation of projects names that are reserved on some platforms.
	 */
	public void testInvalidProjectNames() {
		IWorkspaceRoot root = getWorkspace().getRoot();

		//should not be able to create a project with invalid path on any platform
		String[] names = new String[] {"", "/"};
		for (int i = 0; i < names.length; i++) {
			try {
				root.getProject(names[i]);
				fail("0.99");
			} catch (RuntimeException e) {
				//should fail
			}
		}
		//do some tests with invalid names
		names = new String[0];
		if (isWindows()) {
			//invalid windows names
			names = new String[] {"foo:bar", "prn", "nul", "con", "aux", "clock$", "com1", "com2", "com3", "com4", "com5", "com6", "com7", "com8", "com9", "lpt1", "lpt2", "lpt3", "lpt4", "lpt5", "lpt6", "lpt7", "lpt8", "lpt9", "AUX", "con.foo", "LPT4.txt", "*", "?", "\"", "<", ">", "|", "::"};
		} else {
			//invalid names on non-windows platforms
			names = new String[] {};
		}
		for (int i = 0; i < names.length; i++) {
			IProject project = root.getProject(names[i]);
			assertTrue("1.0 " + names[i], !project.exists());
			try {
				project.create(getMonitor());
				project.open(getMonitor());
				fail("1.1 " + names[i]);
			} catch (CoreException e) {
				// expected
			}
			assertTrue("1.2 " + names[i], !project.exists());
			assertTrue("1.3 " + names[i], !project.isOpen());
		}

		//do some tests with valid names that are *almost* invalid
		if (isWindows()) {
			//these names are valid on windows
			names = new String[] {"hello.prn.txt", "null", "con3", "foo.aux", "lpt0", "com0", "com10", "lpt10", ",", "'", ";"};
		} else {
			//these names are valid on non-windows platforms
			names = new String[] {"foo:bar", "prn", "nul", "con", "aux", "clock$", "com1", "com2", "com3", "com4", "com5", "com6", "com7", "com8", "com9", "lpt1", "lpt2", "lpt3", "lpt4", "lpt5", "lpt6", "lpt7", "lpt8", "lpt9", "con.foo", "LPT4.txt", "*", "?", "\"", "<", ">", "|", "hello.prn.txt", "null", "con3", "foo.aux", "lpt0", "com0", "com10", "lpt10", ",", "'", ";"};
		}
		for (int i = 0; i < names.length; i++) {
			IProject project = root.getProject(names[i]);
			assertTrue("2.0 " + names[i], !project.exists());
			try {
				project.create(getMonitor());
				project.open(getMonitor());
			} catch (CoreException e) {
				fail("2.1 " + names[i], e);
			}
			assertTrue("2.2 " + names[i], project.exists());
			assertTrue("2.3 " + names[i], project.isOpen());
		}
	}

	/**
	 * Tests the API method IProject#isNatureEnabled.
	 */
	public void testIsNatureEnabled() {
		IWorkspace ws = ResourcesPlugin.getWorkspace();
		IProject project = ws.getRoot().getProject("Project");

		//isNatureEnabled on non-existent project should fail
		try {
			project.isNatureEnabled(NATURE_SIMPLE);
			fail("1.0");
		} catch (CoreException e) {
			// expected
		}
		try {
			project.isNatureEnabled(NATURE_MISSING);
			fail("1.1");
		} catch (CoreException e) {
			// expected
		}
		try {
			project.create(getMonitor());
		} catch (CoreException e) {
			fail("1.99", e);
		}
		//isNatureEnabled on closed project should fail
		try {
			project.isNatureEnabled(NATURE_SIMPLE);
			fail("2.0");
		} catch (CoreException e) {
			// expected
		}
		try {
			project.isNatureEnabled(NATURE_MISSING);
			fail("2.1");
		} catch (CoreException e) {
			// expected
		}
		try {
			project.open(getMonitor());
		} catch (CoreException e) {
			fail("2.99", e);
		}
		//isNatureEnabled on open project with no natures
		try {
			assertTrue("3.0", !project.isNatureEnabled(NATURE_SIMPLE));
			assertTrue("3.1", !project.isNatureEnabled(NATURE_MISSING));
			assertTrue("3.2", !project.isNatureEnabled(NATURE_EARTH));
		} catch (CoreException e) {
			fail("3.99", e);
		}
		try {
			IProjectDescription desc = project.getDescription();
			desc.setNatureIds(new String[] {NATURE_SIMPLE});
			project.setDescription(desc, getMonitor());
		} catch (CoreException e) {
			fail("4.99", e);
		}
		//isNatureEnabled on open project with natures
		try {
			assertTrue("5.0", project.isNatureEnabled(NATURE_SIMPLE));
			assertTrue("5.1", !project.isNatureEnabled(NATURE_MISSING));
			assertTrue("5.2", !project.isNatureEnabled(NATURE_EARTH));
		} catch (CoreException e) {
			fail("5.99", e);
		}
	}

	/**
	 * Tests creation of a project whose location is specified by
	 * a path variable. See bug 56274.
	 */
	public void testPathVariableLocation() {
		final String projectName = "Project";
		final String varName = "ProjectLocatio";
		IPath varValue = Platform.getLocation().removeLastSegments(1);
		IPath rawLocation = new Path(varName).append("ProjectLocation");
		//define the variable
		try {
			getWorkspace().getPathVariableManager().setValue(varName, varValue);
		} catch (CoreException e) {
			fail("1.0", e);
		}
		IProject project = getWorkspace().getRoot().getProject(projectName);
		IProjectDescription description = getWorkspace().newProjectDescription(projectName);
		description.setLocation(rawLocation);
		//create the project
		try {
			project.create(description, getMonitor());
			project.open(getMonitor());
		} catch (CoreException e) {
			fail("9.99", e);
		}

		assertEquals("1.0", varValue, getWorkspace().getPathVariableManager().getValue(varName));
		assertTrue("1.1", project.exists());
		assertTrue("1.2", project.isOpen());
		assertEquals("1.3", rawLocation, project.getRawLocation());
		assertEquals("1.4", varValue.append(rawLocation.lastSegment()), project.getLocation());
	}

	public void testProjectCloseOpen() {
		IProject target = getWorkspace().getRoot().getProject("Project");
		ensureExistsInWorkspace(target, true);
		IFolder folder = target.getFolder("Folder");
		ensureExistsInWorkspace(folder, true);

		try {
			target.close(getMonitor());
		} catch (CoreException e) {
			fail("1.0", e);
		}
		assertTrue("1.1", target.exists());
		assertTrue("1.2", !target.isOpen());
		assertTrue("1.3", !folder.exists());

		try {
			target.open(getMonitor());
		} catch (CoreException e) {
			fail("2.0", e);
		}
		assertTrue("2.1", target.isOpen());
		assertTrue("2.2", folder.exists());
	}

	public void testProjectCopyVariations() {
		IProject project, destProject;
		IResource[] resources;
		IResource destination, source, sourceChild, destChild;
		String[] children;
		QualifiedName qname = new QualifiedName("com.example", "myProperty");
		String actual = null;
		String value = "this is my property value.";

		// copy a project via the copy(IPath) API
		project = getWorkspace().getRoot().getProject("SourceProject");
		source = project;
		children = new String[] {"/1/", "/1/2"};
		resources = buildResources(project, children);
		ensureExistsInWorkspace(project, true);
		ensureExistsInWorkspace(resources, true);
		destination = getWorkspace().getRoot().getProject("DestProject");
		assertDoesNotExistInWorkspace("1.0", destination);
		// set a property to copy
		sourceChild = resources[1];
		try {
			sourceChild.setPersistentProperty(qname, value);
		} catch (CoreException e) {
			fail("1.1", e);
		}
		try {
			source.copy(destination.getFullPath(), false, getMonitor());
		} catch (CoreException e) {
			fail("1.2", e);
		}
		assertExistsInWorkspace("1.3", project);
		assertExistsInWorkspace("1.4", resources);
		resources = buildResources((IProject) destination, children);
		assertExistsInWorkspace("1.5", destination);
		assertExistsInWorkspace("1.6", resources);
		// ensure the properties were copied ok
		destChild = resources[1];
		try {
			actual = destChild.getPersistentProperty(qname);
		} catch (CoreException e) {
			fail("1.7", e);
		}
		assertNotNull("1.8", actual);
		assertEquals("1.9", value, actual);
		// cleanup
		try {
			getWorkspace().getRoot().delete(false, getMonitor());
		} catch (CoreException e) {
			fail("1.10", e);
		}

		// copy a project via the copy(IProjectDescription) API
		project = getWorkspace().getRoot().getProject("SourceProject");
		source = project;
		children = new String[] {"/1/", "/1/2"};
		resources = buildResources(project, children);
		ensureExistsInWorkspace(project, true);
		ensureExistsInWorkspace(resources, true);
		destination = getWorkspace().getRoot().getProject("DestProject");
		IProjectDescription description = getWorkspace().newProjectDescription(destination.getName());
		assertDoesNotExistInWorkspace("2.0", destination);
		// set a property to copy
		sourceChild = resources[1];
		try {
			sourceChild.setPersistentProperty(qname, value);
		} catch (CoreException e) {
			fail("2.1", e);
		}
		try {
			((IProject) source).copy(description, false, getMonitor());
		} catch (CoreException e) {
			fail("2.2", e);
		}
		assertExistsInWorkspace("2.3", project);
		assertExistsInWorkspace("2.4", resources);
		resources = buildResources((IProject) destination, children);
		assertExistsInWorkspace("2.5", destination);
		assertExistsInWorkspace("2.6", resources);
		// ensure the properties were copied ok
		destChild = resources[1];
		try {
			actual = destChild.getPersistentProperty(qname);
		} catch (CoreException e) {
			fail("2.7", e);
		}
		assertNotNull("2.8", actual);
		assertEquals("2.9", value, actual);
		// cleanup
		try {
			getWorkspace().getRoot().delete(false, getMonitor());
		} catch (CoreException e) {
			fail("2.10", e);
		}

		// create the source project, copy it to a be a folder under another project.
		// This isn't allowed so catch the exception.
		project = getWorkspace().getRoot().getProject("SourceProject");
		source = project;
		children = new String[] {"/1/", "/1/2"};
		resources = buildResources(project, children);
		destProject = getWorkspace().getRoot().getProject("DestProject");
		destination = destProject.getFolder("MyFolder");
		ensureExistsInWorkspace(new IResource[] {project, destProject}, true);
		ensureExistsInWorkspace(resources, true);
		assertDoesNotExistInWorkspace("3.0", destination);
		try {
			source.copy(destination.getFullPath(), true, getMonitor());
			fail("3.1");
		} catch (CoreException e) {
			// expected
		}
		try {
			getWorkspace().getRoot().delete(false, getMonitor());
		} catch (CoreException e) {
			fail("3.99", e);
		}

		// create a source folder and copy it to be a project.
		// This isn't allowed so catch the exception
		project = getWorkspace().getRoot().getProject("SourceProject");
		children = new String[] {"/1/", "/1/2"};
		source = project.getFolder("1");
		resources = buildResources(project, children);
		destination = getWorkspace().getRoot().getProject("DestProject");
		ensureExistsInWorkspace(project, true);
		ensureExistsInWorkspace(resources, true);
		assertDoesNotExistInWorkspace("4.0", destination);
		try {
			source.copy(destination.getFullPath(), true, getMonitor());
			fail("4.1");
		} catch (CoreException e) {
			// expected
		}
		// cleanup
		try {
			getWorkspace().getRoot().delete(true, getMonitor());
		} catch (CoreException e) {
			fail("4.99", e);
		}
	}

	public void testProjectCreateOpenCloseDelete() {
		IProject target = getWorkspace().getRoot().getProject("Project");
		try {
			target.create(getMonitor());
		} catch (CoreException e) {
			fail("1.0", e);
		}
		assertTrue("1.1", target.exists());

		try {
			target.open(getMonitor());
		} catch (CoreException e) {
			fail("2.0", e);
		}
		assertTrue("2.1", target.isOpen());

		try {
			target.close(getMonitor());
		} catch (CoreException e) {
			fail("3.0", e);
		}
		assertTrue("3.1", !target.isOpen());

		try {
			target.delete(true, getMonitor());
		} catch (CoreException e) {
			fail("4.0", e);
		}
		assertTrue("4.1", !target.exists());
	}

	public void testProjectCreation() {
		IProject target = getWorkspace().getRoot().getProject("Project");

		try {
			target.create(getMonitor());
			assertTrue("1.0", target.exists());
		} catch (CoreException e) {
			fail("1.1", e);
		}

		try {
			target.open(getMonitor());
			assertTrue("2.0", target.isOpen());
		} catch (CoreException e) {
			fail("2.1", e);
		}
	}

	/**
	 * Tests creating a project whose location is invalid
	 */
	public void testProjectCreationInvalidLocation() {
		IProject target = getWorkspace().getRoot().getProject("Project");
		IProjectDescription description = getWorkspace().newProjectDescription(target.getName());
		description.setLocation(Platform.getLocation().append(".metadata"));
		try {
			target.create(description, getMonitor());
			fail("1.0");
		} catch (CoreException e) {
			//expected
		}

		//default location for a project called .metadata is invalid
		target = getWorkspace().getRoot().getProject(".metadata");
		description = getWorkspace().newProjectDescription(target.getName());
		try {
			target.create(description, getMonitor());
			fail("1.1");
		} catch (CoreException e) {
			//expected
		}
		
		//same with one argument constructor
		try {
			target.create(getMonitor());
			fail("1.2");
		} catch (CoreException e) {
			//expected
		}
	}
	
	/**
	 * Tests creating a project whose location already exists with different case
	 */
	public void testProjectCreationLocationExistsWithDifferentCase() {
		if (isWindows()) {
			String projectName = getUniqueString() + "a";
			IProject project = getWorkspace().getRoot().getProject(projectName);

			try {
				project.create(getMonitor());
				project.delete(false, true, getMonitor());
			} catch (CoreException ex) {
				fail("1.0");
			}

			// the attempt to create a project in an already existing location with different case
			project = getWorkspace().getRoot().getProject(projectName.toUpperCase());

			try {
				project.create(getMonitor());
				fail("2.0");
			} catch (CoreException e) {
				//expected
			}

			// the attempt to create a project in an already existing location with the same case
			project = getWorkspace().getRoot().getProject(projectName);

			try {
				project.create(getMonitor());
			} catch (CoreException e) {
				fail("3.0", e);
			}
		}
	}

	/**
	 * Tests for IProject.delete where:
	 * 	- project is CLOSED
	 * 	- content area is the DEFAULT
	 * 	- resources are IN_SYNC with the file system
	 */
	public void testProjectDeletionClosedDefaultInSync() {
		IProject project = getWorkspace().getRoot().getProject("Project");
		IFile file = project.getFile("myfile.txt");
		IFile otherFile = project.getFile("myotherfile.txt");
		IFileStore projectStore, fileStore, otherFileStore;

		/* ======================================================================
		 * Force = TRUE
		 * Delete content = ALWAYS
		 * =======================================================================*/
		ensureExistsInWorkspace(new IResource[] {project, file, otherFile}, true);
		projectStore = ((Resource) project).getStore();
		fileStore = ((Resource) file).getStore();
		otherFileStore = ((Resource) otherFile).getStore();
		assertTrue("1.0", project.exists());
		assertTrue("1.1", file.exists());
		assertTrue("1.2", otherFile.exists());
		try {
			project.close(getMonitor());
		} catch (CoreException e) {
			fail("1.3", e);
		}
		assertTrue("1.4", project.exists());
		assertTrue("1.5", !project.isOpen());
		assertTrue("1.6", !project.isAccessible());
		assertTrue("1.7", !file.exists());
		assertTrue("1.8", !otherFile.exists());
		try {
			int updateFlags = IResource.FORCE;
			updateFlags |= IResource.ALWAYS_DELETE_PROJECT_CONTENT;
			project.delete(updateFlags, getMonitor());
		} catch (CoreException e) {
			fail("1.9", e);
		}
		assertTrue("1.10", !project.exists());
		assertTrue("1.11", !file.exists());
		assertTrue("1.12", !otherFile.exists());
		assertTrue("1.13", !projectStore.fetchInfo().exists());
		assertTrue("1.14", !fileStore.fetchInfo().exists());
		assertTrue("1.15", !otherFileStore.fetchInfo().exists());

		/* ======================================================================
		 * Force = FALSE
		 * Delete content = ALWAYS
		 * =======================================================================*/
		ensureExistsInWorkspace(new IResource[] {project, file, otherFile}, true);
		projectStore = ((Resource) project).getStore();
		fileStore = ((Resource) file).getStore();
		otherFileStore = ((Resource) otherFile).getStore();
		assertTrue("2.0", project.exists());
		assertTrue("2.1", file.exists());
		assertTrue("2.2", otherFile.exists());
		try {
			project.close(getMonitor());
		} catch (CoreException e) {
			fail("2.3", e);
		}
		assertTrue("2.4", project.exists());
		assertTrue("2.5", !project.isOpen());
		assertTrue("2.6", !project.isAccessible());
		assertTrue("2.7", !file.exists());
		assertTrue("2.8", !otherFile.exists());
		try {
			int updateFlags = IResource.ALWAYS_DELETE_PROJECT_CONTENT;
			project.delete(updateFlags, getMonitor());
		} catch (CoreException e) {
			fail("2.9", e);
		}
		assertTrue("2.10", !project.exists());
		assertTrue("2.11", !file.exists());
		assertTrue("2.12", !otherFile.exists());
		assertTrue("2.13", !projectStore.fetchInfo().exists());
		assertTrue("2.14", !fileStore.fetchInfo().exists());
		assertTrue("2.15", !otherFileStore.fetchInfo().exists());

		/* ======================================================================
		 * Force = TRUE
		 * Delete content = NEVER
		 * =======================================================================*/
		ensureExistsInWorkspace(new IResource[] {project, file, otherFile}, true);
		projectStore = ((Resource) project).getStore();
		fileStore = ((Resource) file).getStore();
		otherFileStore = ((Resource) otherFile).getStore();
		assertTrue("3.0", project.exists());
		assertTrue("3.1", file.exists());
		assertTrue("3.2", otherFile.exists());
		try {
			project.close(getMonitor());
		} catch (CoreException e) {
			fail("3.3", e);
		}
		assertTrue("3.4", project.exists());
		assertTrue("3.5", !project.isOpen());
		assertTrue("3.6", !project.isAccessible());
		assertTrue("3.7", !file.exists());
		assertTrue("3.8", !otherFile.exists());
		try {
			int updateFlags = IResource.FORCE;
			updateFlags |= IResource.NEVER_DELETE_PROJECT_CONTENT;
			project.delete(updateFlags, getMonitor());
		} catch (CoreException e) {
			fail("3.9", e);
		}
		assertTrue("3.10", !project.exists());
		assertTrue("3.11", !file.exists());
		assertTrue("3.12", !otherFile.exists());
		assertTrue("3.13", projectStore.fetchInfo().exists());
		assertTrue("3.14", fileStore.fetchInfo().exists());
		assertTrue("3.15", otherFileStore.fetchInfo().exists());
		// cleanup
		clear(projectStore);

		/* ======================================================================
		 * Force = FALSE
		 * Delete content = NEVER
		 * =======================================================================*/
		ensureExistsInWorkspace(new IResource[] {project, file, otherFile}, true);
		projectStore = ((Resource) project).getStore();
		fileStore = ((Resource) file).getStore();
		otherFileStore = ((Resource) otherFile).getStore();
		assertTrue("4.0", project.exists());
		assertTrue("4.1", file.exists());
		assertTrue("4.2", otherFile.exists());
		try {
			project.close(getMonitor());
		} catch (CoreException e) {
			fail("4.3", e);
		}
		assertTrue("4.4", project.exists());
		assertTrue("4.5", !project.isOpen());
		assertTrue("4.6", !project.isAccessible());
		assertTrue("4.7", !file.exists());
		assertTrue("4.8", !otherFile.exists());
		try {
			int updateFlags = IResource.NEVER_DELETE_PROJECT_CONTENT;
			project.delete(updateFlags, getMonitor());
		} catch (CoreException e) {
			fail("4.9", e);
		}
		assertTrue("4.10", !project.exists());
		assertTrue("4.11", !file.exists());
		assertTrue("4.12", !otherFile.exists());
		assertTrue("4.13", projectStore.fetchInfo().exists());
		assertTrue("4.14", fileStore.fetchInfo().exists());
		assertTrue("4.15", otherFileStore.fetchInfo().exists());
		// cleanup
		clear(projectStore);

		/* ======================================================================
		 * Force = TRUE
		 * Delete content = DEFAULT
		 * =======================================================================*/
		ensureExistsInWorkspace(new IResource[] {project, file, otherFile}, true);
		projectStore = ((Resource) project).getStore();
		fileStore = ((Resource) file).getStore();
		otherFileStore = ((Resource) otherFile).getStore();
		assertTrue("5.0", project.exists());
		assertTrue("5.1", file.exists());
		assertTrue("5.2", otherFile.exists());
		try {
			project.close(getMonitor());
		} catch (CoreException e) {
			fail("5.3", e);
		}
		assertTrue("5.4", project.exists());
		assertTrue("5.5", !project.isOpen());
		assertTrue("5.6", !project.isAccessible());
		assertTrue("5.7", !file.exists());
		assertTrue("5.8", !otherFile.exists());
		try {
			int updateFlags = IResource.FORCE;
			project.delete(updateFlags, getMonitor());
		} catch (CoreException e) {
			fail("5.9", e);
		}
		assertTrue("5.10", !project.exists());
		assertTrue("5.11", !file.exists());
		assertTrue("5.12", !otherFile.exists());
		assertTrue("5.13", projectStore.fetchInfo().exists());
		assertTrue("5.14", fileStore.fetchInfo().exists());
		assertTrue("5.15", otherFileStore.fetchInfo().exists());
		// cleanup
		clear(projectStore);

		/* ======================================================================
		 * Force = FALSE
		 * Delete content = DEFAULT
		 * =======================================================================*/
		ensureExistsInWorkspace(new IResource[] {project, file, otherFile}, true);
		projectStore = ((Resource) project).getStore();
		fileStore = ((Resource) file).getStore();
		otherFileStore = ((Resource) otherFile).getStore();
		assertTrue("6.0", project.exists());
		assertTrue("6.1", file.exists());
		assertTrue("6.2", otherFile.exists());
		try {
			project.close(getMonitor());
		} catch (CoreException e) {
			fail("6.3", e);
		}
		assertTrue("6.4", project.exists());
		assertTrue("6.5", !project.isOpen());
		assertTrue("6.6", !project.isAccessible());
		assertTrue("6.7", !file.exists());
		assertTrue("6.8", !otherFile.exists());
		try {
			int updateFlags = IResource.NONE;
			project.delete(updateFlags, getMonitor());
		} catch (CoreException e) {
			fail("6.9", e);
		}
		assertTrue("6.10", !project.exists());
		assertTrue("6.11", !file.exists());
		assertTrue("6.12", !otherFile.exists());
		assertTrue("6.13", projectStore.fetchInfo().exists());
		assertTrue("6.14", fileStore.fetchInfo().exists());
		assertTrue("6.15", otherFileStore.fetchInfo().exists());
		// cleanup
		clear(projectStore);
	}

	/**
	 * Tests for IProject.delete where:
	 * 	- project is CLOSED
	 * 	- content area is the DEFAULT
	 * 	- resources are OUT_OF_SYNC with the file system
	 */
	public void testProjectDeletionClosedDefaultOutOfSync() {
		IProject project = getWorkspace().getRoot().getProject("Project");
		IFile file = project.getFile("myfile.txt");
		IFile otherFile = project.getFile("myotherfile.txt");
		IFileStore projectStore, fileStore, otherFileStore;

		/* ======================================================================
		 * Force = TRUE
		 * Delete content = ALWAYS
		 * =======================================================================*/
		ensureExistsInWorkspace(new IResource[] {project, file}, true);
		projectStore = ((Resource) project).getStore();
		fileStore = ((Resource) file).getStore();
		otherFileStore = ((Resource) otherFile).getStore();
		assertTrue("1.0", project.exists());
		assertTrue("1.1", file.exists());
		assertTrue("1.2", !otherFile.exists());
		try {
			project.close(getMonitor());
		} catch (CoreException e) {
			fail("1.3", e);
		}
		createFileInFileSystem(otherFileStore);
		assertTrue("1.5", otherFileStore.fetchInfo().exists());
		assertTrue("1.6", project.exists());
		assertTrue("1.7", !project.isOpen());
		assertTrue("1.8", !project.isAccessible());
		assertTrue("1.9", !file.exists());
		assertTrue("1.10", !otherFile.exists());
		try {
			int updateFlags = IResource.FORCE;
			updateFlags |= IResource.ALWAYS_DELETE_PROJECT_CONTENT;
			project.delete(updateFlags, getMonitor());
		} catch (CoreException e) {
			fail("1.11", e);
		}
		assertTrue("1.12", !project.exists());
		assertTrue("1.13", !file.exists());
		assertTrue("1.14", !otherFile.exists());
		assertTrue("1.15", !projectStore.fetchInfo().exists());
		assertTrue("1.16", !fileStore.fetchInfo().exists());
		assertTrue("1.17", !otherFileStore.fetchInfo().exists());

		/* ======================================================================
		 * Force = FALSE
		 * Delete content = ALWAYS
		 * =======================================================================*/
		ensureExistsInWorkspace(new IResource[] {project, file}, true);
		projectStore = ((Resource) project).getStore();
		fileStore = ((Resource) file).getStore();
		otherFileStore = ((Resource) otherFile).getStore();
		assertTrue("2.0", project.exists());
		assertTrue("2.1", file.exists());
		assertTrue("2.2", !otherFile.exists());
		try {
			project.close(getMonitor());
		} catch (CoreException e) {
			fail("2.3", e);
		}
		createFileInFileSystem(otherFileStore);
		assertTrue("2.5", otherFileStore.fetchInfo().exists());
		assertTrue("2.6", project.exists());
		assertTrue("2.7", !project.isOpen());
		assertTrue("2.8", !project.isAccessible());
		assertTrue("2.9", !file.exists());
		assertTrue("2.10", !otherFile.exists());
		try {
			int updateFlags = IResource.ALWAYS_DELETE_PROJECT_CONTENT;
			project.delete(updateFlags, getMonitor());
		} catch (CoreException e) {
			fail("2.11", e);
		}
		assertTrue("2.12", !project.exists());
		assertTrue("2.13", !file.exists());
		assertTrue("2.14", !otherFile.exists());
		assertTrue("2.15", !projectStore.fetchInfo().exists());
		assertTrue("2.16", !fileStore.fetchInfo().exists());
		assertTrue("2.17", !otherFileStore.fetchInfo().exists());

		/* ======================================================================
		 * Force = TRUE
		 * Delete content = NEVER
		 * =======================================================================*/
		ensureExistsInWorkspace(new IResource[] {project, file}, true);
		projectStore = ((Resource) project).getStore();
		fileStore = ((Resource) file).getStore();
		otherFileStore = ((Resource) otherFile).getStore();
		assertTrue("3.0", project.exists());
		assertTrue("3.1", file.exists());
		assertTrue("3.2", !otherFile.exists());
		try {
			project.close(getMonitor());
		} catch (CoreException e) {
			fail("3.3", e);
		}
		createFileInFileSystem(otherFileStore);
		assertTrue("3.5", otherFileStore.fetchInfo().exists());
		assertTrue("3.6", project.exists());
		assertTrue("3.7", !project.isOpen());
		assertTrue("3.8", !project.isAccessible());
		assertTrue("3.9", !file.exists());
		assertTrue("3.10", !otherFile.exists());
		try {
			int updateFlags = IResource.FORCE;
			updateFlags |= IResource.NEVER_DELETE_PROJECT_CONTENT;
			project.delete(updateFlags, getMonitor());
		} catch (CoreException e) {
			fail("3.11", e);
		}
		assertTrue("3.12", !project.exists());
		assertTrue("3.13", !file.exists());
		assertTrue("3.14", !otherFile.exists());
		assertTrue("3.15", projectStore.fetchInfo().exists());
		assertTrue("3.16", fileStore.fetchInfo().exists());
		assertTrue("3.17", otherFileStore.fetchInfo().exists());
		// cleanup
		clear(projectStore);

		/* ======================================================================
		 * Force = FALSE
		 * Delete content = NEVER
		 * =======================================================================*/
		ensureExistsInWorkspace(new IResource[] {project, file}, true);
		projectStore = ((Resource) project).getStore();
		fileStore = ((Resource) file).getStore();
		otherFileStore = ((Resource) otherFile).getStore();
		assertTrue("4.0", project.exists());
		assertTrue("4.1", file.exists());
		assertTrue("4.2", !otherFile.exists());
		try {
			project.close(getMonitor());
		} catch (CoreException e) {
			fail("4.3", e);
		}
		createFileInFileSystem(otherFileStore);
		assertTrue("4.5", otherFileStore.fetchInfo().exists());
		assertTrue("4.6", project.exists());
		assertTrue("4.7", !project.isOpen());
		assertTrue("4.8", !project.isAccessible());
		assertTrue("4.9", !file.exists());
		assertTrue("4.10", !otherFile.exists());
		try {
			int updateFlags = IResource.NEVER_DELETE_PROJECT_CONTENT;
			project.delete(updateFlags, getMonitor());
		} catch (CoreException e) {
			fail("4.11", e);
		}
		assertTrue("4.12", !project.exists());
		assertTrue("4.13", !file.exists());
		assertTrue("4.14", !otherFile.exists());
		assertTrue("4.15", projectStore.fetchInfo().exists());
		assertTrue("4.16", fileStore.fetchInfo().exists());
		assertTrue("4.17", otherFileStore.fetchInfo().exists());
		// cleanup
		clear(projectStore);

		/* ======================================================================
		 * Force = TRUE
		 * Delete content = DEFAULT
		 * =======================================================================*/
		ensureExistsInWorkspace(new IResource[] {project, file}, true);
		projectStore = ((Resource) project).getStore();
		fileStore = ((Resource) file).getStore();
		otherFileStore = ((Resource) otherFile).getStore();
		assertTrue("5.0", project.exists());
		assertTrue("5.1", file.exists());
		assertTrue("5.2", !otherFile.exists());
		try {
			project.close(getMonitor());
		} catch (CoreException e) {
			fail("5.3", e);
		}
		createFileInFileSystem(otherFileStore);
		assertTrue("5.5", otherFileStore.fetchInfo().exists());
		assertTrue("5.6", project.exists());
		assertTrue("5.7", !project.isOpen());
		assertTrue("5.8", !project.isAccessible());
		assertTrue("5.9", !file.exists());
		assertTrue("5.10", !otherFile.exists());
		try {
			int updateFlags = IResource.FORCE;
			project.delete(updateFlags, getMonitor());
		} catch (CoreException e) {
			fail("5.11", e);
		}
		assertTrue("5.12", !project.exists());
		assertTrue("5.13", !file.exists());
		assertTrue("5.14", !otherFile.exists());
		assertTrue("5.15", projectStore.fetchInfo().exists());
		assertTrue("5.16", fileStore.fetchInfo().exists());
		assertTrue("5.17", otherFileStore.fetchInfo().exists());
		// cleanup
		clear(projectStore);

		/* ======================================================================
		 * Force = FALSE
		 * Delete content = DEFAULT
		 * =======================================================================*/
		ensureExistsInWorkspace(new IResource[] {project, file}, true);
		projectStore = ((Resource) project).getStore();
		fileStore = ((Resource) file).getStore();
		otherFileStore = ((Resource) otherFile).getStore();
		assertTrue("6.0", project.exists());
		assertTrue("6.1", file.exists());
		assertTrue("6.2", !otherFile.exists());
		try {
			project.close(getMonitor());
		} catch (CoreException e) {
			fail("6.3", e);
		}
		createFileInFileSystem(otherFileStore);
		assertTrue("6.5", otherFileStore.fetchInfo().exists());
		assertTrue("6.6", project.exists());
		assertTrue("6.7", !project.isOpen());
		assertTrue("6.8", !project.isAccessible());
		assertTrue("6.9", !file.exists());
		assertTrue("6.10", !otherFile.exists());
		try {
			int updateFlags = IResource.NONE;
			project.delete(updateFlags, getMonitor());
		} catch (CoreException e) {
			fail("6.11", e);
		}
		assertTrue("6.12", !project.exists());
		assertTrue("6.13", !file.exists());
		assertTrue("6.14", !otherFile.exists());
		assertTrue("6.15", projectStore.fetchInfo().exists());
		assertTrue("6.16", fileStore.fetchInfo().exists());
		assertTrue("6.17", otherFileStore.fetchInfo().exists());
		// cleanup
		clear(projectStore);
	}

	/**
	 * Tests for IProject.delete where:
	 * 	- project is CLOSED
	 * 	- content area is USER-DEFINED
	 * 	- resources are IN_SYNC with the file system
	 */
	public void testProjectDeletionClosedUserDefinedInSync() {
		IProject project = getWorkspace().getRoot().getProject("Project");
		IFile file = project.getFile("myfile.txt");
		IFileStore projectStore, fileStore;
		IProjectDescription description = getWorkspace().newProjectDescription(project.getName());

		/* ======================================================================
		 * Force = TRUE
		 * Delete content = ALWAYS
		 * =======================================================================*/
		projectStore = getTempStore();
		description.setLocationURI(projectStore.toURI());
		ensureExistsInWorkspace(project, description);
		ensureExistsInWorkspace(file, true);
		fileStore = ((Resource) file).getStore();
		assertTrue("1.2", project.exists());
		assertTrue("1.3", file.exists());
		try {
			project.close(getMonitor());
		} catch (CoreException e) {
			fail("1.4", e);
		}
		try {
			int updateFlags = IResource.FORCE;
			updateFlags |= IResource.ALWAYS_DELETE_PROJECT_CONTENT;
			project.delete(updateFlags, getMonitor());
		} catch (CoreException e) {
			fail("1.5", e);
		}
		assertTrue("1.6", !project.exists());
		assertTrue("1.7", !file.exists());
		// ensure the project directory and files no longer exist
		assertTrue("1.8", !projectStore.fetchInfo().exists());
		assertTrue("1.9", !fileStore.fetchInfo().exists());
		clear(projectStore);

		/* ======================================================================
		 * Force = FALSE
		 * Delete content = ALWAYS
		 * =======================================================================*/
		projectStore = getTempStore();
		description.setLocationURI(projectStore.toURI());
		ensureExistsInWorkspace(project, description);
		ensureExistsInWorkspace(file, true);
		fileStore = ((Resource) file).getStore();
		assertTrue("2.2", project.exists());
		assertTrue("2.3", file.exists());
		try {
			project.close(getMonitor());
		} catch (CoreException e) {
			fail("2.4", e);
		}
		try {
			int updateFlags = IResource.ALWAYS_DELETE_PROJECT_CONTENT;
			project.delete(updateFlags, getMonitor());
		} catch (CoreException e) {
			fail("2.5", e);
		}
		assertTrue("2.6", !project.exists());
		assertTrue("2.7", !file.exists());
		// ensure the project directory and files no longer exist
		assertTrue("2.8", !projectStore.fetchInfo().exists());
		assertTrue("2.9", !fileStore.fetchInfo().exists());
		clear(projectStore);

		/* ======================================================================
		 * Force = TRUE
		 * Delete content = NEVER
		 * =======================================================================*/
		projectStore = getTempStore();
		description.setLocationURI(projectStore.toURI());
		ensureExistsInWorkspace(project, description);
		ensureExistsInWorkspace(file, true);
		fileStore = ((Resource) file).getStore();
		assertTrue("3.2", project.exists());
		assertTrue("3.3", file.exists());
		try {
			project.close(getMonitor());
		} catch (CoreException e) {
			fail("3.4", e);
		}
		try {
			int updateFlags = IResource.FORCE;
			updateFlags |= IResource.NEVER_DELETE_PROJECT_CONTENT;
			project.delete(updateFlags, getMonitor());
		} catch (CoreException e) {
			fail("3.5", e);
		}
		assertTrue("3.6", !project.exists());
		assertTrue("3.7", !file.exists());
		assertTrue("3.8", projectStore.fetchInfo().exists());
		assertTrue("3.9", fileStore.fetchInfo().exists());
		// cleanup
		clear(projectStore);

		/* ======================================================================
		 * Force = FALSE
		 * Delete content = NEVER
		 * =======================================================================*/
		projectStore = getTempStore();
		description.setLocationURI(projectStore.toURI());
		ensureExistsInWorkspace(project, description);
		ensureExistsInWorkspace(file, true);
		fileStore = ((Resource) file).getStore();
		assertTrue("4.2", project.exists());
		assertTrue("4.3", file.exists());
		try {
			project.close(getMonitor());
		} catch (CoreException e) {
			fail("4.4", e);
		}
		try {
			int updateFlags = IResource.NEVER_DELETE_PROJECT_CONTENT;
			project.delete(updateFlags, getMonitor());
		} catch (CoreException e) {
			fail("4.5", e);
		}
		assertTrue("4.6", !project.exists());
		assertTrue("4.7", !file.exists());
		assertTrue("4.8", projectStore.fetchInfo().exists());
		assertTrue("4.9", fileStore.fetchInfo().exists());
		// cleanup
		clear(projectStore);

		/* ======================================================================
		 * Force = TRUE
		 * Delete content = DEFAULT
		 * =======================================================================*/
		projectStore = getTempStore();
		description.setLocationURI(projectStore.toURI());
		ensureExistsInWorkspace(project, description);
		ensureExistsInWorkspace(new IResource[] {project, file}, true);
		fileStore = ((Resource) file).getStore();
		assertTrue("5.2", project.exists());
		assertTrue("5.3", file.exists());
		try {
			project.close(getMonitor());
		} catch (CoreException e) {
			fail("5.4", e);
		}
		try {
			int updateFlags = IResource.FORCE;
			project.delete(updateFlags, getMonitor());
		} catch (CoreException e) {
			fail("5.5", e);
		}
		assertTrue("5.6", !project.exists());
		assertTrue("5.7", !file.exists());
		assertTrue("5.8", projectStore.fetchInfo().exists());
		assertTrue("5.9", fileStore.fetchInfo().exists());
		// cleanup
		clear(projectStore);

		/* ======================================================================
		 * Force = FALSE
		 * Delete content = DEFAULT
		 * =======================================================================*/
		projectStore = getTempStore();
		description.setLocationURI(projectStore.toURI());
		ensureExistsInWorkspace(project, description);
		ensureExistsInWorkspace(file, true);
		fileStore = ((Resource) file).getStore();
		assertTrue("6.2", project.exists());
		assertTrue("6.3", file.exists());
		try {
			project.close(getMonitor());
		} catch (CoreException e) {
			fail("6.4", e);
		}
		try {
			int updateFlags = IResource.NONE;
			project.delete(updateFlags, getMonitor());
		} catch (CoreException e) {
			fail("6.5", e);
		}
		assertTrue("6.6", !project.exists());
		assertTrue("6.7", !file.exists());
		assertTrue("6.8", projectStore.fetchInfo().exists());
		assertTrue("6.9", fileStore.fetchInfo().exists());
		// cleanup
		clear(projectStore);
	}

	/**
	 * Tests for IProject.delete where:
	 * 	- project is CLOSED
	 * 	- content area is USER-DEFINED
	 * 	- resources are OUT_OF_SYNC with the file system
	 */
	public void testProjectDeletionClosedUserDefinedOutOfSync() {
		IProject project = getWorkspace().getRoot().getProject("Project");
		IFile file = project.getFile("myfile.txt");
		IFile otherFile = project.getFile("myotherfile.txt");
		IFileStore projectStore, fileStore, otherFileStore;
		IProjectDescription description = getWorkspace().newProjectDescription(project.getName());

		/* ======================================================================
		 * Force = TRUE
		 * Delete content = ALWAYS
		 * =======================================================================*/
		projectStore = getTempStore();
		description.setLocationURI(projectStore.toURI());
		ensureExistsInWorkspace(project, description);
		ensureExistsInWorkspace(file, true);
		ensureExistsInFileSystem(otherFile);
		fileStore = ((Resource) file).getStore();
		otherFileStore = ((Resource) otherFile).getStore();
		assertTrue("1.0", project.exists());
		assertTrue("1.1", file.exists());
		assertTrue("1.2", !otherFile.exists());
		try {
			project.close(getMonitor());
		} catch (CoreException e) {
			fail("1.3", e);
		}
		try {
			int updateFlags = IResource.FORCE;
			updateFlags |= IResource.ALWAYS_DELETE_PROJECT_CONTENT;
			project.delete(updateFlags, getMonitor());
		} catch (CoreException e) {
			fail("1.4", e);
		}
		assertTrue("1.5", !project.exists());
		assertTrue("1.6", !file.exists());
		assertTrue("1.7", !otherFile.exists());
		// ensure the project directory and files no longer exist
		assertTrue("1.8", !projectStore.fetchInfo().exists());
		assertTrue("1.9", !fileStore.fetchInfo().exists());
		assertTrue("1.10", !otherFileStore.fetchInfo().exists());
		clear(projectStore);

		/* ======================================================================
		 * Force = FALSE
		 * Delete content = ALWAYS
		 * =======================================================================*/
		projectStore = getTempStore();
		description.setLocationURI(projectStore.toURI());
		ensureExistsInWorkspace(project, description);
		ensureExistsInWorkspace(file, true);
		ensureExistsInFileSystem(otherFile);
		fileStore = ((Resource) file).getStore();
		otherFileStore = ((Resource) otherFile).getStore();
		assertTrue("2.0", project.exists());
		assertTrue("2.1", file.exists());
		assertTrue("2.2", !otherFile.exists());
		try {
			project.close(getMonitor());
		} catch (CoreException e) {
			fail("2.3", e);
		}
		try {
			int updateFlags = IResource.ALWAYS_DELETE_PROJECT_CONTENT;
			project.delete(updateFlags, getMonitor());
		} catch (CoreException e) {
			fail("2.4", e);
		}
		assertTrue("2.5", !project.exists());
		assertTrue("2.6", !file.exists());
		assertTrue("2.7", !otherFile.exists());
		// ensure the project directory and files no longer exist
		assertTrue("2.8", !projectStore.fetchInfo().exists());
		assertTrue("2.9", !fileStore.fetchInfo().exists());
		assertTrue("2.10", !otherFileStore.fetchInfo().exists());
		clear(projectStore);

		/* ======================================================================
		 * Force = TRUE
		 * Delete content = NEVER
		 * =======================================================================*/
		projectStore = getTempStore();
		description.setLocationURI(projectStore.toURI());
		ensureExistsInWorkspace(project, description);
		ensureExistsInWorkspace(file, true);
		ensureExistsInFileSystem(otherFile);
		fileStore = ((Resource) file).getStore();
		otherFileStore = ((Resource) otherFile).getStore();
		assertTrue("3.0", project.exists());
		assertTrue("3.1", file.exists());
		assertTrue("3.2", !otherFile.exists());
		try {
			project.close(getMonitor());
		} catch (CoreException e) {
			fail("3.3", e);
		}
		try {
			int updateFlags = IResource.FORCE;
			updateFlags |= IResource.NEVER_DELETE_PROJECT_CONTENT;
			project.delete(updateFlags, getMonitor());
		} catch (CoreException e) {
			fail("3.4", e);
		}
		assertTrue("3.5", !project.exists());
		assertTrue("3.6", !file.exists());
		assertTrue("3.7", !otherFile.exists());
		assertTrue("3.8", projectStore.fetchInfo().exists());
		assertTrue("3.9", fileStore.fetchInfo().exists());
		assertTrue("3.10", otherFileStore.fetchInfo().exists());

		/* ======================================================================
		 * Force = FALSE
		 * Delete content = NEVER
		 * =======================================================================*/
		projectStore = getTempStore();

		description.setLocationURI(projectStore.toURI());
		ensureExistsInWorkspace(project, description);
		ensureExistsInWorkspace(file, true);
		ensureExistsInFileSystem(otherFile);
		fileStore = ((Resource) file).getStore();
		otherFileStore = ((Resource) otherFile).getStore();
		assertTrue("4.0", project.exists());
		assertTrue("4.1", file.exists());
		assertTrue("4.2", !otherFile.exists());

		try {
			project.close(getMonitor());
		} catch (CoreException e) {
			fail("4.3", e);
		}
		try {
			int updateFlags = IResource.NEVER_DELETE_PROJECT_CONTENT;
			project.delete(updateFlags, getMonitor());
		} catch (CoreException e) {
			fail("4.4", e);
		}
		assertTrue("4.5", !project.exists());
		assertTrue("4.6", !file.exists());
		assertTrue("4.7", !otherFile.exists());
		assertTrue("4.8", projectStore.fetchInfo().exists());
		assertTrue("4.9", fileStore.fetchInfo().exists());
		assertTrue("4.10", otherFileStore.fetchInfo().exists());

		/* ======================================================================
		 * Force = TRUE
		 * Delete content = DEFAULT
		 * =======================================================================*/
		projectStore = getTempStore();
		description.setLocationURI(projectStore.toURI());
		ensureExistsInWorkspace(project, description);
		ensureExistsInWorkspace(new IResource[] {project, file}, true);
		ensureExistsInFileSystem(otherFile);
		fileStore = ((Resource) file).getStore();
		otherFileStore = ((Resource) otherFile).getStore();
		assertTrue("5.0", project.exists());
		assertTrue("5.1", file.exists());
		assertTrue("5.2", !otherFile.exists());
		try {
			project.close(getMonitor());
		} catch (CoreException e) {
			fail("5.3", e);
		}
		try {
			int updateFlags = IResource.FORCE;
			project.delete(updateFlags, getMonitor());
		} catch (CoreException e) {
			fail("5.4", e);
		}
		assertTrue("5.5", !project.exists());
		assertTrue("5.6", !file.exists());
		assertTrue("5.7", !otherFile.exists());
		// don't delete the directory itself since the location is user-defined, but delete the contents
		assertTrue("5.8", projectStore.fetchInfo().exists());
		assertTrue("5.9", fileStore.fetchInfo().exists());
		assertTrue("5.10", otherFileStore.fetchInfo().exists());

		/* ======================================================================
		 * Force = FALSE
		 * Delete content = DEFAULT
		 * =======================================================================*/
		projectStore = getTempStore();
		description.setLocationURI(projectStore.toURI());
		ensureExistsInWorkspace(project, description);
		ensureExistsInWorkspace(file, true);
		ensureExistsInFileSystem(otherFile);
		fileStore = ((Resource) file).getStore();
		otherFileStore = ((Resource) otherFile).getStore();
		assertTrue("6.0", project.exists());
		assertTrue("6.1", file.exists());
		assertTrue("6.2", !otherFile.exists());
		try {
			project.close(getMonitor());
		} catch (CoreException e) {
			fail("6.3", e);
		}
		try {
			int updateFlags = IResource.NONE;
			project.delete(updateFlags, getMonitor());
		} catch (CoreException e) {
			fail("6.4", e);
		}
		assertTrue("6.5", !project.exists());
		// delete was best effort so this file should be gone.
		assertTrue("6.6", !file.exists());
		assertTrue("6.7", !otherFile.exists());
		// don't delete the directory itself since its user-defined, but delete the contents
		assertTrue("6.8", projectStore.fetchInfo().exists());
		assertTrue("6.9", fileStore.fetchInfo().exists());
		assertTrue("6.10", otherFileStore.fetchInfo().exists());
		// cleanup
		clear(projectStore);
	}

	/**
	 * Tests for IProject.delete where:
	 * 	- project is OPEN
	 * 	- content area is the DEFAULT
	 * 	- resources are IN_SYNC with the file system
	 */
	public void testProjectDeletionOpenDefaultInSync() {
		IProject project = getWorkspace().getRoot().getProject("Project");
		IFile file = project.getFile("myfile.txt");
		IFileStore projectStore, fileStore;

		/* ======================================================================
		 * Force = TRUE
		 * Delete content = ALWAYS
		 * =======================================================================*/
		ensureExistsInWorkspace(new IResource[] {project, file}, true);
		projectStore = ((Resource) project).getStore();
		fileStore = ((Resource) file).getStore();
		assertTrue("1.0", project.exists());
		assertTrue("1.1", file.exists());
		try {
			int updateFlags = IResource.FORCE;
			updateFlags |= IResource.ALWAYS_DELETE_PROJECT_CONTENT;
			project.delete(updateFlags, getMonitor());
		} catch (CoreException e) {
			fail("1.2", e);
		}
		assertTrue("1.3", !project.exists());
		assertTrue("1.4", !file.exists());
		assertTrue("1.5", !projectStore.fetchInfo().exists());
		assertTrue("1.6", !fileStore.fetchInfo().exists());

		/* ======================================================================
		 * Force = FALSE
		 * Delete content = ALWAYS
		 * =======================================================================*/
		ensureExistsInWorkspace(new IResource[] {project, file}, true);
		projectStore = ((Resource) project).getStore();
		fileStore = ((Resource) file).getStore();
		assertTrue("2.0", project.exists());
		assertTrue("2.1", file.exists());
		try {
			int updateFlags = IResource.ALWAYS_DELETE_PROJECT_CONTENT;
			project.delete(updateFlags, getMonitor());
		} catch (CoreException e) {
			fail("2.2", e);
		}
		assertTrue("2.3", !project.exists());
		assertTrue("2.4", !file.exists());
		assertTrue("2.5", !projectStore.fetchInfo().exists());
		assertTrue("2.6", !fileStore.fetchInfo().exists());

		/* ======================================================================
		 * Force = TRUE
		 * Delete content = NEVER
		 * =======================================================================*/
		ensureExistsInWorkspace(new IResource[] {project, file}, true);
		projectStore = ((Resource) project).getStore();
		fileStore = ((Resource) file).getStore();
		assertTrue("3.0", project.exists());
		assertTrue("3.1", file.exists());
		try {
			int updateFlags = IResource.FORCE;
			updateFlags |= IResource.NEVER_DELETE_PROJECT_CONTENT;
			project.delete(updateFlags, getMonitor());
		} catch (CoreException e) {
			fail("3.2", e);
		}
		assertTrue("3.3", !project.exists());
		assertTrue("3.4", !file.exists());
		assertTrue("3.5", projectStore.fetchInfo().exists());
		assertTrue("3.6", fileStore.fetchInfo().exists());

		/* ======================================================================
		 * Force = FALSE
		 * Delete content = NEVER
		 * =======================================================================*/
		ensureExistsInWorkspace(new IResource[] {project, file}, true);
		projectStore = ((Resource) project).getStore();
		fileStore = ((Resource) file).getStore();
		assertTrue("4.0", project.exists());
		assertTrue("4.1", file.exists());
		try {
			int updateFlags = IResource.NEVER_DELETE_PROJECT_CONTENT;
			project.delete(updateFlags, getMonitor());
		} catch (CoreException e) {
			fail("4.2", e);
		}
		assertTrue("4.3", !project.exists());
		assertTrue("4.4", !file.exists());
		assertTrue("4.5", projectStore.fetchInfo().exists());
		assertTrue("4.6", fileStore.fetchInfo().exists());

		/* ======================================================================
		 * Force = TRUE
		 * Delete content = DEFAULT
		 * =======================================================================*/
		ensureExistsInWorkspace(new IResource[] {project, file}, true);
		projectStore = ((Resource) project).getStore();
		fileStore = ((Resource) file).getStore();
		assertTrue("5.0", project.exists());
		assertTrue("5.1", file.exists());
		try {
			int updateFlags = IResource.FORCE;
			project.delete(updateFlags, getMonitor());
		} catch (CoreException e) {
			fail("5.2", e);
		}
		assertTrue("5.3", !project.exists());
		assertTrue("5.4", !file.exists());
		assertTrue("5.5", !projectStore.fetchInfo().exists());
		assertTrue("5.6", !fileStore.fetchInfo().exists());

		/* ======================================================================
		 * Force = FALSE
		 * Delete content = DEFAULT
		 * =======================================================================*/
		ensureExistsInWorkspace(new IResource[] {project, file}, true);
		projectStore = ((Resource) project).getStore();
		fileStore = ((Resource) file).getStore();
		assertTrue("6.0", project.exists());
		assertTrue("6.1", file.exists());
		try {
			int updateFlags = IResource.NONE;
			project.delete(updateFlags, getMonitor());
		} catch (CoreException e) {
			fail("6.2", e);
		}
		assertTrue("6.3", !project.exists());
		assertTrue("6.4", !file.exists());
		assertTrue("6.5", !projectStore.fetchInfo().exists());
		assertTrue("6.6", !fileStore.fetchInfo().exists());
	}

	/**
	 * Tests for IProject.delete where:
	 * 	- project is OPEN
	 * 	- content area is the DEFAULT
	 * 	- resources are OUT_OF_SYNC with the file system
	 */
	public void testProjectDeletionOpenDefaultOutOfSync() {
		IProject project = getWorkspace().getRoot().getProject("Project");
		IFile file = project.getFile("myfile.txt");
		IFileStore projectStore, fileStore;

		/* ======================================================================
		 * Force = TRUE
		 * Delete content = ALWAYS
		 * =======================================================================*/
		ensureExistsInWorkspace(project, true);
		ensureExistsInFileSystem(file);
		projectStore = ((Resource) project).getStore();
		fileStore = ((Resource) file).getStore();
		assertTrue("1.0", project.exists());
		assertTrue("1.1", !file.exists());
		try {
			int updateFlags = IResource.FORCE;
			updateFlags |= IResource.ALWAYS_DELETE_PROJECT_CONTENT;
			project.delete(updateFlags, getMonitor());
		} catch (CoreException e) {
			fail("1.2", e);
		}
		assertTrue("1.3", !project.exists());
		assertTrue("1.4", !file.exists());
		assertTrue("1.5", !projectStore.fetchInfo().exists());
		assertTrue("1.6", !fileStore.fetchInfo().exists());

		/* ======================================================================
		 * Force = FALSE
		 * Delete content = ALWAYS (always_delete_content over-rides FORCE flag)
		 * =======================================================================*/
		ensureExistsInWorkspace(project, true);
		ensureExistsInFileSystem(file);
		projectStore = ((Resource) project).getStore();
		fileStore = ((Resource) file).getStore();
		assertTrue("2.0", project.exists());
		assertTrue("2.1", !file.exists());
		try {
			int updateFlags = IResource.ALWAYS_DELETE_PROJECT_CONTENT;
			project.delete(updateFlags, getMonitor());
		} catch (CoreException e) {
			fail("2.2", e);
		}
		assertTrue("2.3", !project.exists());
		assertTrue("2.4", !file.exists());
		assertTrue("2.5", !projectStore.fetchInfo().exists());
		assertTrue("2.6", !fileStore.fetchInfo().exists());

		/* ======================================================================
		 * Force = TRUE
		 * Delete content = NEVER
		 * =======================================================================*/
		ensureExistsInWorkspace(project, true);
		ensureExistsInFileSystem(file);
		projectStore = ((Resource) project).getStore();
		fileStore = ((Resource) file).getStore();
		assertTrue("3.0", project.exists());
		assertTrue("3.1", !file.exists());
		try {
			int updateFlags = IResource.FORCE;
			updateFlags |= IResource.NEVER_DELETE_PROJECT_CONTENT;
			project.delete(updateFlags, getMonitor());
		} catch (CoreException e) {
			fail("3.2", e);
		}
		assertTrue("3.3", !project.exists());
		assertTrue("3.4", !file.exists());
		assertTrue("3.5", projectStore.fetchInfo().exists());
		assertTrue("3.6", fileStore.fetchInfo().exists());
		// cleanup
		clear(projectStore);

		/* ======================================================================
		 * Force = FALSE
		 * Delete content = NEVER
		 * =======================================================================*/
		ensureExistsInWorkspace(project, true);
		ensureExistsInFileSystem(file);
		projectStore = ((Resource) project).getStore();
		fileStore = ((Resource) file).getStore();
		assertTrue("4.0", project.exists());
		assertTrue("4.1", !file.exists());
		try {
			int updateFlags = IResource.NEVER_DELETE_PROJECT_CONTENT;
			project.delete(updateFlags, getMonitor());
		} catch (CoreException e) {
			fail("4.2", e);
		}
		assertTrue("4.3", !project.exists());
		assertTrue("4.4", !file.exists());
		assertTrue("4.5", projectStore.fetchInfo().exists());
		assertTrue("4.6", fileStore.fetchInfo().exists());
		// cleanup
		clear(projectStore);

		/* ======================================================================
		 * Force = TRUE
		 * Delete content = DEFAULT
		 * =======================================================================*/
		ensureExistsInWorkspace(project, true);
		ensureExistsInFileSystem(file);
		projectStore = ((Resource) project).getStore();
		fileStore = ((Resource) file).getStore();
		assertTrue("5.0", project.exists());
		assertTrue("5.1", !file.exists());
		try {
			int updateFlags = IResource.FORCE;
			project.delete(updateFlags, getMonitor());
		} catch (CoreException e) {
			fail("5.2", e);
		}
		assertTrue("5.3", !project.exists());
		assertTrue("5.4", !file.exists());
		assertTrue("5.5", !projectStore.fetchInfo().exists());
		assertTrue("5.6", !fileStore.fetchInfo().exists());

		/* ======================================================================
		 * Force = FALSE
		 * Delete content = DEFAULT
		 * =======================================================================*/
		ensureExistsInWorkspace(project, true);
		ensureExistsInFileSystem(file);
		projectStore = ((Resource) project).getStore();
		fileStore = ((Resource) file).getStore();
		assertTrue("6.0", project.exists());
		assertTrue("6.1", !file.exists());
		try {
			int updateFlags = IResource.NONE;
			project.delete(updateFlags, getMonitor());
			fail("6.2");
		} catch (CoreException e) {
			// expected
		}
		assertTrue("6.3", project.exists());
		assertTrue("6.4", !file.exists());
		assertTrue("6.5", projectStore.fetchInfo().exists());
		assertTrue("6.6", fileStore.fetchInfo().exists());
	}

	/**
	 * Tests for IProject.delete where:
	 * 	- project is OPEN
	 * 	- content area is USER-DEFINED
	 * 	- resources are IN_SYNC with the file system
	 */
	public void testProjectDeletionOpenUserDefinedInSync() {
		IProject project = getWorkspace().getRoot().getProject("Project");
		IFile file = project.getFile("myfile.txt");
		IFileStore projectStore, fileStore;
		IProjectDescription description = getWorkspace().newProjectDescription(project.getName());
		/* ======================================================================
		 * Force = TRUE
		 * Delete content = ALWAYS
		 * =======================================================================*/
		projectStore = getTempStore();
		description.setLocationURI(projectStore.toURI());
		ensureExistsInWorkspace(project, description);
		ensureExistsInWorkspace(file, true);
		fileStore = ((Resource) file).getStore();
		assertTrue("1.2", project.exists());
		assertTrue("1.3", file.exists());
		try {
			int updateFlags = IResource.FORCE;
			updateFlags |= IResource.ALWAYS_DELETE_PROJECT_CONTENT;
			project.delete(updateFlags, getMonitor());
		} catch (CoreException e) {
			fail("1.4", e);
		}
		assertTrue("1.5", !project.exists());
		assertTrue("1.6", !file.exists());
		// ensure the project directory and files no longer exist
		assertTrue("1.7", !projectStore.fetchInfo().exists());
		assertTrue("1.8", !fileStore.fetchInfo().exists());

		/* ======================================================================
		 * Force = FALSE
		 * Delete content = ALWAYS
		 * =======================================================================*/
		projectStore = getTempStore();
		description.setLocationURI(projectStore.toURI());
		ensureExistsInWorkspace(project, description);
		ensureExistsInWorkspace(file, true);
		fileStore = ((Resource) file).getStore();
		assertTrue("2.2", project.exists());
		assertTrue("2.3", file.exists());
		try {
			int updateFlags = IResource.ALWAYS_DELETE_PROJECT_CONTENT;
			project.delete(updateFlags, getMonitor());
		} catch (CoreException e) {
			fail("2.4", e);
		}
		assertTrue("2.5", !project.exists());
		assertTrue("2.6", !file.exists());
		// ensure the project directory and files no longer exist
		assertTrue("2.7", !projectStore.fetchInfo().exists());
		assertTrue("2.8", !fileStore.fetchInfo().exists());

		/* ======================================================================
		 * Force = TRUE
		 * Delete content = NEVER
		 * =======================================================================*/
		projectStore = getTempStore();
		description.setLocationURI(projectStore.toURI());
		ensureExistsInWorkspace(project, description);
		ensureExistsInWorkspace(file, true);
		fileStore = ((Resource) file).getStore();
		assertTrue("3.2", project.exists());
		assertTrue("3.3", file.exists());
		try {
			int updateFlags = IResource.FORCE;
			updateFlags |= IResource.NEVER_DELETE_PROJECT_CONTENT;
			project.delete(updateFlags, getMonitor());
		} catch (CoreException e) {
			fail("3.4", e);
		}
		assertTrue("3.5", !project.exists());
		assertTrue("3.6", !file.exists());
		assertTrue("3.7", projectStore.fetchInfo().exists());
		assertTrue("3.8", fileStore.fetchInfo().exists());

		/* ======================================================================
		 * Force = FALSE
		 * Delete content = NEVER
		 * =======================================================================*/
		projectStore = getTempStore();
		description.setLocationURI(projectStore.toURI());
		ensureExistsInWorkspace(project, description);
		ensureExistsInWorkspace(file, true);
		fileStore = ((Resource) file).getStore();
		assertTrue("4.2", project.exists());
		assertTrue("4.3", file.exists());
		try {
			int updateFlags = IResource.NEVER_DELETE_PROJECT_CONTENT;
			project.delete(updateFlags, getMonitor());
		} catch (CoreException e) {
			fail("4.4", e);
		}
		assertTrue("4.5", !project.exists());
		assertTrue("4.6", !file.exists());
		assertTrue("4.7", projectStore.fetchInfo().exists());
		assertTrue("4.8", fileStore.fetchInfo().exists());
		// cleanup
		clear(projectStore);

		/* ======================================================================
		 * Force = TRUE
		 * Delete content = DEFAULT
		 * =======================================================================*/
		projectStore = getTempStore();
		description.setLocationURI(projectStore.toURI());
		ensureExistsInWorkspace(project, description);
		ensureExistsInWorkspace(new IResource[] {project, file}, true);
		fileStore = ((Resource) file).getStore();
		assertTrue("5.2", project.exists());
		assertTrue("5.3", file.exists());
		try {
			int updateFlags = IResource.FORCE;
			project.delete(updateFlags, getMonitor());
		} catch (CoreException e) {
			fail("5.4", e);
		}
		assertTrue("5.5", !project.exists());
		assertTrue("5.6", !file.exists());
		// ensure the project directory and files no longer exist
		assertTrue("5.7", !projectStore.fetchInfo().exists());
		assertTrue("5.8", !fileStore.fetchInfo().exists());

		/* ======================================================================
		 * Force = FALSE
		 * Delete content = DEFAULT
		 * =======================================================================*/
		projectStore = getTempStore();
		description.setLocationURI(projectStore.toURI());
		ensureExistsInWorkspace(project, description);
		ensureExistsInWorkspace(file, true);
		fileStore = ((Resource) file).getStore();
		assertTrue("6.2", project.exists());
		assertTrue("6.3", file.exists());
		try {
			int updateFlags = IResource.NONE;
			project.delete(updateFlags, getMonitor());
		} catch (CoreException e) {
			fail("6.4", e);
		}
		assertTrue("6.5", !project.exists());
		assertTrue("6.6", !file.exists());
		// ensure the project directory and files no longer exist
		assertTrue("6.7", !projectStore.fetchInfo().exists());
		assertTrue("6.8", !fileStore.fetchInfo().exists());
	}

	/**
	 * Tests for IProject.delete where:
	 * 	- project is OPEN
	 * 	- content area is USER-DEFINED
	 * 	- resources are OUT_OF_SYNC with the file system
	 */
	public void testProjectDeletionOpenUserDefinedOutOfSync() {
		IProject project = getWorkspace().getRoot().getProject("testProjectDeletionOpenUserDefinedOutOfSync");
		IFile file = project.getFile("myfile.txt");
		IFile otherFile = project.getFile("myotherfile.txt");
		IFileStore projectStore, fileStore, otherFileStore;
		IProjectDescription description = getWorkspace().newProjectDescription(project.getName());

		/* ======================================================================
		 * Force = TRUE
		 * Delete content = ALWAYS
		 * =======================================================================*/
		projectStore = getTempStore();
		description.setLocationURI(projectStore.toURI());
		ensureExistsInWorkspace(project, description);
		ensureExistsInWorkspace(file, true);
		ensureExistsInFileSystem(otherFile);
		fileStore = ((Resource) file).getStore();
		otherFileStore = ((Resource) otherFile).getStore();
		assertTrue("1.0", project.exists());
		assertTrue("1.1", file.exists());
		assertTrue("1.2", !otherFile.exists());
		try {
			int updateFlags = IResource.FORCE;
			updateFlags |= IResource.ALWAYS_DELETE_PROJECT_CONTENT;
			project.delete(updateFlags, getMonitor());
		} catch (CoreException e) {
			fail("1.3", e);
		}
		assertTrue("1.4", !project.exists());
		assertTrue("1.5", !file.exists());
		assertTrue("1.6", !otherFile.exists());
		// ensure the project directory and files no longer exist
		assertTrue("1.7", !projectStore.fetchInfo().exists());
		assertTrue("1.8", !fileStore.fetchInfo().exists());
		assertTrue("1.9", !otherFileStore.fetchInfo().exists());
		clear(projectStore);

		/* ======================================================================
		 * Force = FALSE
		 * Delete content = ALWAYS
		 * =======================================================================*/
		projectStore = getTempStore();
		description.setLocationURI(projectStore.toURI());
		ensureExistsInWorkspace(project, description);
		ensureExistsInWorkspace(file, true);
		ensureExistsInFileSystem(otherFile);
		fileStore = ((Resource) file).getStore();
		otherFileStore = ((Resource) otherFile).getStore();
		assertTrue("2.0", project.exists());
		assertTrue("2.1", file.exists());
		assertTrue("2.2", !otherFile.exists());
		try {
			int updateFlags = IResource.ALWAYS_DELETE_PROJECT_CONTENT;
			project.delete(updateFlags, getMonitor());
		} catch (CoreException e) {
			fail("2.3", e);
		}
		assertTrue("2.4", !project.exists());
		assertTrue("2.5", !file.exists());
		assertTrue("2.6", !otherFile.exists());
		// ensure the project directory and files no longer exist
		assertTrue("2.7", !projectStore.fetchInfo().exists());
		assertTrue("2.8", !fileStore.fetchInfo().exists());
		assertTrue("2.9", !otherFileStore.fetchInfo().exists());
		clear(projectStore);

		/* ======================================================================
		 * Force = TRUE
		 * Delete content = NEVER
		 * =======================================================================*/
		projectStore = getTempStore();
		description.setLocationURI(projectStore.toURI());
		ensureExistsInWorkspace(project, description);
		ensureExistsInWorkspace(file, true);
		ensureExistsInFileSystem(otherFile);
		fileStore = ((Resource) file).getStore();
		otherFileStore = ((Resource) otherFile).getStore();
		assertTrue("3.0", project.exists());
		assertTrue("3.1", file.exists());
		assertTrue("3.2", !otherFile.exists());
		try {
			int updateFlags = IResource.FORCE;
			updateFlags |= IResource.NEVER_DELETE_PROJECT_CONTENT;
			project.delete(updateFlags, getMonitor());
		} catch (CoreException e) {
			fail("3.3", e);
		}
		assertTrue("3.4", !project.exists());
		assertTrue("3.5", !file.exists());
		assertTrue("3.6", !otherFile.exists());
		assertTrue("3.7", projectStore.fetchInfo().exists());
		assertTrue("3.8", fileStore.fetchInfo().exists());
		assertTrue("3.9", otherFileStore.fetchInfo().exists());
		// cleanup
		clear(projectStore);

		/* ======================================================================
		 * Force = FALSE
		 * Delete content = NEVER
		 * =======================================================================*/
		projectStore = getTempStore();
		description.setLocationURI(projectStore.toURI());
		ensureExistsInWorkspace(project, description);
		ensureExistsInWorkspace(file, true);
		ensureExistsInFileSystem(otherFile);
		fileStore = ((Resource) file).getStore();
		otherFileStore = ((Resource) otherFile).getStore();
		assertTrue("4.0", project.exists());
		assertTrue("4.1", file.exists());
		assertTrue("4.2", !otherFile.exists());
		try {
			int updateFlags = IResource.NEVER_DELETE_PROJECT_CONTENT;
			project.delete(updateFlags, getMonitor());
		} catch (CoreException e) {
			fail("4.3", e);
		}
		assertTrue("4.4", !project.exists());
		assertTrue("4.5", !file.exists());
		assertTrue("4.6", !otherFile.exists());
		assertTrue("4.7", projectStore.fetchInfo().exists());
		assertTrue("4.8", fileStore.fetchInfo().exists());
		assertTrue("4.9", otherFileStore.fetchInfo().exists());

		/* ======================================================================
		 * Force = TRUE
		 * Delete content = DEFAULT
		 * =======================================================================*/
		projectStore = getTempStore();
		description.setLocationURI(projectStore.toURI());
		ensureExistsInWorkspace(project, description);
		ensureExistsInWorkspace(new IResource[] {project, file}, true);
		ensureExistsInFileSystem(otherFile);
		fileStore = ((Resource) file).getStore();
		otherFileStore = ((Resource) otherFile).getStore();
		assertTrue("5.0", project.exists());
		assertTrue("5.1", file.exists());
		assertTrue("5.2", !otherFile.exists());
		try {
			int updateFlags = IResource.FORCE;
			project.delete(updateFlags, getMonitor());
		} catch (CoreException e) {
			fail("5.3", e);
		}
		assertTrue("5.4", !project.exists());
		assertTrue("5.5", !file.exists());
		assertTrue("5.6", !otherFile.exists());
		// ensure the project directory and files no longer exist
		assertTrue("5.7", !projectStore.fetchInfo().exists());
		assertTrue("5.8", !fileStore.fetchInfo().exists());
		assertTrue("5.9", !otherFileStore.fetchInfo().exists());

		/* ======================================================================
		 * Force = FALSE
		 * Delete content = DEFAULT
		 * =======================================================================*/
		projectStore = getTempStore();
		description.setLocationURI(projectStore.toURI());
		ensureExistsInWorkspace(project, description);
		ensureExistsInWorkspace(file, true);
		ensureExistsInFileSystem(otherFile);
		fileStore = ((Resource) file).getStore();
		otherFileStore = ((Resource) otherFile).getStore();
		assertTrue("6.0", project.exists());
		assertTrue("6.1", file.exists());
		assertTrue("6.2", !otherFile.exists());
		try {
			int updateFlags = IResource.NONE;
			project.delete(updateFlags, getMonitor());
			fail("6.3");
		} catch (CoreException e) {
			// expected
		}
		assertTrue("6.4", project.exists());
		// delete was best effort so this file should be gone.
		assertTrue("6.5", !file.exists());
		assertTrue("6.6", !otherFile.exists());
		// don't delete the directory itself since its user-defined, but delete the contents
		assertTrue("6.7", projectStore.fetchInfo().exists());
		assertTrue("6.8", !fileStore.fetchInfo().exists());
		assertTrue("6.9", otherFileStore.fetchInfo().exists());
	}

	/**
	 * Tests API on IProjectDescription
	 */
	public void testProjectDescriptionDynamic() {
		IProjectDescription desc = getWorkspace().newProjectDescription("foo");
		IProject project1 = getWorkspace().getRoot().getProject("P1");
		IProject project2 = getWorkspace().getRoot().getProject("P2");

		//dynamic project references
		assertEquals("2.0", 0, desc.getDynamicReferences().length);
		IProject[] refs = new IProject[] {project1, project2};
		desc.setDynamicReferences(refs);
		IProject[] result = desc.getDynamicReferences();
		assertEquals("2.1", 2, result.length);
		assertEquals("2.2", project1, result[0]);
		assertEquals("2.3", project2, result[1]);

		//destroying the result should not affect the description
		result[0] = null;
		result[1] = null;
		result = desc.getDynamicReferences();
		assertEquals("2.4", 2, result.length);
		assertEquals("2.5", project1, result[0]);
		assertEquals("2.6", project2, result[1]);

		//duplicates (should be automatically omitted)
		refs = new IProject[] {project1, project2, project2, project1, project1};
		desc.setDynamicReferences(refs);
		result = desc.getDynamicReferences();
		assertEquals("3.1", 2, result.length);
		assertEquals("3.2", project1, result[0]);
		assertEquals("3.3", project2, result[1]);
	}

	/**
	 * Tests API on IProjectDescription
	 */
	public void testProjectDescriptionReferences() {
		IProjectDescription desc = getWorkspace().newProjectDescription("foo");
		IProject project1 = getWorkspace().getRoot().getProject("P1");
		IProject project2 = getWorkspace().getRoot().getProject("P2");

		//project name
		assertEquals("1.0", "foo", desc.getName());

		//project references
		assertEquals("2.0", 0, desc.getReferencedProjects().length);
		IProject[] refs = new IProject[] {project1, project2};
		desc.setReferencedProjects(refs);
		IProject[] result = desc.getReferencedProjects();
		assertEquals("2.1", 2, result.length);
		assertEquals("2.2", project1, result[0]);
		assertEquals("2.3", project2, result[1]);

		//destroying the result should not affect the description
		result[0] = null;
		result[1] = null;
		result = desc.getReferencedProjects();
		assertEquals("2.4", 2, result.length);
		assertEquals("2.5", project1, result[0]);
		assertEquals("2.6", project2, result[1]);

		//duplicates (should be automatically omitted)
		refs = new IProject[] {project1, project2, project2, project1, project1};
		desc.setReferencedProjects(refs);
		result = desc.getReferencedProjects();
		assertEquals("3.1", 2, result.length);
		assertEquals("3.2", project1, result[0]);
		assertEquals("3.3", project2, result[1]);
	}

	public void testProjectLocationValidation() {

		// validation of the initial project should be ok
		IProject project1 = getWorkspace().getRoot().getProject("Project1");
		IPath root = getWorkspace().getRoot().getLocation().removeLastSegments(1).append("temp");
		IPath path = root.append("foo");
		assertTrue("1.0", getWorkspace().validateProjectLocation(project1, path).isOK());
		// but not if its in the default default area
		path = Platform.getLocation().append(project1.getName());
		assertTrue("1.1", !getWorkspace().validateProjectLocation(project1, path).isOK());
		// create the first project with its default default mapping
		try {
			project1.create(getMonitor());
		} catch (CoreException e) {
			fail("1.2", e);
		}

		// create the second project with a non-default mapping
		IProjectDescription desc = getWorkspace().newProjectDescription("Project2");
		IProject project2 = getWorkspace().getRoot().getProject("Project2");
		path = root.append("project2");
		assertTrue("2.0", getWorkspace().validateProjectLocation(project2, path).isOK());
		desc.setLocation(path);
		try {
			project2.create(desc, getMonitor());
		} catch (CoreException e) {
			fail("2.1", e);
		}

		// create a third project with the default default location
		IProject project3 = getWorkspace().getRoot().getProject("Project3");
		try {
			project3.create(getMonitor());
		} catch (CoreException e) {
			fail("3.0", e);
		}
		// it should be ok to re-set a current project's location.
		path = root.append("project3");
		assertTrue("3.1", getWorkspace().validateProjectLocation(project3, path).isOK());

		// other cases
		assertTrue("4.0", getWorkspace().validateProjectLocation(project3, root).isOK());
		assertTrue("4.1", !getWorkspace().validateProjectLocation(project3, root.append("project2")).isOK());
		assertTrue("4.2", getWorkspace().validateProjectLocation(project3, root.append("project2/foo")).isOK());
		assertTrue("4.3", getWorkspace().validateProjectLocation(project3, null).isOK());
		assertTrue("4.4", getWorkspace().validateProjectLocation(project3, root.append("%20foo")).isOK());

		try {
			project1.delete(true, getMonitor());
			project2.delete(true, getMonitor());
			project3.delete(true, getMonitor());
		} catch (CoreException e) {
			fail("20.1", e);
		}
	}

	/**
	 * Tests creating a project at a location that contains URL escape sequences or spaces.
	 */
	public void testProjectLocationWithEscapes() {
		IProject project1 = getWorkspace().getRoot().getProject("Project1");
		IPath root = getWorkspace().getRoot().getLocation().removeLastSegments(1).append("temp");
		IPath location = root.append("%20foo bar");
		IProjectDescription desc = getWorkspace().newProjectDescription(project1.getName());
		desc.setLocation(location);
		try {
			project1.create(desc, getMonitor());
			project1.open(null);

			assertTrue("1.0", project1.exists());
			assertTrue("1.1", project1.isAccessible());
			assertEquals("1.2", location, project1.getLocation());
			assertEquals("1.3", location, project1.getRawLocation());

			project1.delete(IResource.FORCE, getMonitor());
		} catch (CoreException e) {
			fail("1.99", e);
		} finally {
			Workspace.clear(location.toFile());
		}
	}

	public void testProjectMoveContent() {
		IProject project = getWorkspace().getRoot().getProject("MyProject");
		String[] children = new String[] {"/1/", "/1/2"};
		IResource[] resources = buildResources(project, children);
		ensureExistsInWorkspace(project, true);
		ensureExistsInWorkspace(resources, true);
		Set pathsToDelete = new HashSet(5);

		try {
			// move the project content
			IProjectDescription destination = null;
			try {
				destination = project.getDescription();
			} catch (CoreException e) {
				fail("1.0", e);
			}
			IPath oldPath = project.getLocation();
			IPath newPath = getTempDir().append(Long.toString(System.currentTimeMillis()));
			pathsToDelete.add(newPath);
			destination.setLocation(newPath);
			try {
				project.move(destination, false, getMonitor());
			} catch (CoreException e) {
				fail("1.1", e);
			}
			newPath = project.getLocation();

			// ensure that the new description was set correctly and the locations
			// aren't the same
			assertTrue("2.0", !oldPath.equals(newPath));

			// make sure all the resources still exist.	
			IResourceVisitor visitor = new IResourceVisitor() {
				public boolean visit(IResource resource) {
					assertExistsInWorkspace("2.1." + resource.getFullPath(), resource);
					return true;
				}
			};
			try {
				getWorkspace().getRoot().accept(visitor);
			} catch (CoreException e) {
				fail("2.2", e);
			}
		} finally {
			for (Iterator i = pathsToDelete.iterator(); i.hasNext();) {
				Workspace.clear(((IPath) i.next()).toFile());
			}
		}
	}

	public void testProjectMoveVariations() {
		IProject project, destProject;
		IResource[] resources;
		IResource destination, source, sourceChild, destChild;
		String[] children;
		IMarker[] markers = null;
		String actual = null;
		QualifiedName qname = new QualifiedName("com.example", "myProperty");
		String value = "this is my property value.";

		// rename a project via the move(IPath) API
		project = getWorkspace().getRoot().getProject("SourceProject");
		source = project;
		children = new String[] {"/1/", "/1/2"};
		resources = buildResources(project, children);
		ensureExistsInWorkspace(project, true);
		ensureExistsInWorkspace(resources, true);
		destination = getWorkspace().getRoot().getProject("DestProject");
		assertDoesNotExistInWorkspace("1.0", destination);
		// set a property to move
		sourceChild = resources[1];
		try {
			sourceChild.setPersistentProperty(qname, value);
		} catch (CoreException e) {
			fail("1.1", e);
		}
		// create a marker to be moved
		try {
			sourceChild.createMarker(IMarker.PROBLEM);
		} catch (CoreException e) {
			fail("1.2", e);
		}
		try {
			source.move(destination.getFullPath(), false, getMonitor());
		} catch (CoreException e) {
			fail("1.3", e);
		}
		assertDoesNotExistInWorkspace("1.4", project);
		assertDoesNotExistInWorkspace("1.5", resources);
		resources = buildResources((IProject) destination, children);
		assertExistsInWorkspace("1.6", destination);
		assertExistsInWorkspace("1.7", resources);
		// ensure properties are moved too
		destChild = resources[1];
		try {
			actual = destChild.getPersistentProperty(qname);
		} catch (CoreException e) {
			fail("1.6", e);
		}
		assertNotNull("1.7", actual);
		assertEquals("1.8", value, actual);
		// ensure the marker was moved
		try {
			markers = destChild.findMarkers(null, true, IResource.DEPTH_ZERO);
		} catch (CoreException e) {
			fail("1.9", e);
		}
		assertEquals("1.10", 1, markers.length);
		// cleanup
		try {
			getWorkspace().getRoot().delete(false, getMonitor());
		} catch (CoreException e) {
			fail("1.11", e);
		}

		// rename a project via the move(IProjectDescription) API
		project = getWorkspace().getRoot().getProject("SourceProject");
		source = project;
		children = new String[] {"/1/", "/1/2"};
		resources = buildResources(project, children);
		ensureExistsInWorkspace(project, true);
		ensureExistsInWorkspace(resources, true);
		// set a property to move
		sourceChild = resources[1];
		try {
			sourceChild.setPersistentProperty(qname, value);
		} catch (CoreException e) {
			fail("2.1", e);
		}
		// create a marker to be moved
		try {
			sourceChild.createMarker(IMarker.PROBLEM);
		} catch (CoreException e) {
			fail("2.2", e);
		}
		destination = getWorkspace().getRoot().getProject("DestProject");
		IProjectDescription description = getWorkspace().newProjectDescription(destination.getName());
		assertDoesNotExistInWorkspace("2.3", destination);
		try {
			((IProject) source).move(description, false, getMonitor());
		} catch (CoreException e) {
			fail("2.4", e);
		}
		assertDoesNotExistInWorkspace("2.5", project);
		assertDoesNotExistInWorkspace("2.6", resources);
		resources = buildResources((IProject) destination, children);
		assertExistsInWorkspace("2.7", destination);
		assertExistsInWorkspace("2.8", resources);
		// ensure properties are moved too
		destChild = resources[1];
		try {
			actual = destChild.getPersistentProperty(qname);
		} catch (CoreException e) {
			fail("2.9", e);
		}
		assertNotNull("2.10", actual);
		assertEquals("2.11", value, actual);
		// ensure the marker was moved
		try {
			markers = destChild.findMarkers(null, true, IResource.DEPTH_ZERO);
		} catch (CoreException e) {
			fail("2.12", e);
		}
		assertEquals("2.13", 1, markers.length);
		// cleanup
		try {
			getWorkspace().getRoot().delete(false, getMonitor());
		} catch (CoreException e) {
			fail("2.14", e);
		}

		// create the source project, move it to a be a folder under another project,
		// This is no longer allowed so ignore the error.
		project = getWorkspace().getRoot().getProject("SourceProject");
		source = project;
		destProject = getWorkspace().getRoot().getProject("DestProject");
		destination = destProject.getFolder("MyFolder");
		try {
			source.move(destination.getFullPath(), true, getMonitor());
			fail("3.3");
		} catch (CoreException e) {
			// expected
		}

		// create a source folder and move it to be a project.
		// This is no longer allowed so ignore the error.
		project = getWorkspace().getRoot().getProject("MySourceProject");
		children = new String[] {"/1/", "/1/2"};
		source = project.getFolder("1");
		resources = buildResources(project, children);
		destination = getWorkspace().getRoot().getProject("MyDestProject");
		try {
			source.move(destination.getFullPath(), true, getMonitor());
			fail("4.3");
		} catch (CoreException e) {
			// expected
		}
	}

	/**
	 * Tests renaming a project using the move(IProjectDescription) API
	 * where the project contents are stored outside the workspace location.
	 */
	public void testRenameExternalProject() {
		IProject project = getWorkspace().getRoot().getProject("SourceProject");
		String[] children = new String[] {"/1/", "/1/2"};
		IResource[] resources = buildResources(project, children);
		// create the project at an external location
		IProjectDescription description = getWorkspace().newProjectDescription(project.getName());
		description.setLocationURI(getTempStore().toURI());
		try {
			project.create(description, getMonitor());
			project.open(getMonitor());
		} catch (CoreException e) {
			fail("1.1", e);
		}
		ensureExistsInWorkspace(project, true);
		ensureExistsInWorkspace(resources, true);
		// set a property to move
		IResource sourceChild = resources[1];
		QualifiedName qname = new QualifiedName("com.example", "myProperty");
		String propertyValue = "this is my property value.";
		String actualPropertyValue = null;
		try {
			sourceChild.setPersistentProperty(qname, propertyValue);
		} catch (CoreException e) {
			fail("2.1", e);
		}
		// create a marker to be moved
		try {
			sourceChild.createMarker(IMarker.PROBLEM);
		} catch (CoreException e) {
			fail("2.2", e);
		}
		IProject destination = getWorkspace().getRoot().getProject("DestProject");
		description.setName(destination.getName());
		assertDoesNotExistInWorkspace("2.3", destination);
		try {
			project.move(description, false, getMonitor());
		} catch (CoreException e) {
			fail("2.4", e);
		}
		assertDoesNotExistInWorkspace("2.5", project);
		assertDoesNotExistInWorkspace("2.6", resources);
		resources = buildResources(destination, children);
		assertExistsInWorkspace("2.7", destination);
		assertExistsInWorkspace("2.8", resources);
		// ensure properties are moved too
		IResource destChild = resources[1];
		try {
			actualPropertyValue = destChild.getPersistentProperty(qname);
		} catch (CoreException e) {
			fail("2.9", e);
		}
		assertNotNull("2.10", actualPropertyValue);
		assertEquals("2.11", propertyValue, actualPropertyValue);
		// ensure the marker was moved
		IMarker[] markers = null;
		try {
			markers = destChild.findMarkers(null, true, IResource.DEPTH_ZERO);
		} catch (CoreException e) {
			fail("2.12", e);
		}
		assertEquals("2.13", 1, markers.length);
		// cleanup
		try {
			getWorkspace().getRoot().delete(false, getMonitor());
		} catch (CoreException e) {
			fail("2.14", e);
		}
	}

	/**
	 * Tests {@link IResource#move(IProjectDescription, int, IProgressMonitor)}
	 * in conjunction with {@link IResource#REPLACE}.
	 */
	public void testReplaceLocation() {
		IProject target = getWorkspace().getRoot().getProject("testReplaceLocation");
		ensureExistsInWorkspace(target, true);

		IFileStore projectStore = getTempStore();
		IFileStore childFile = projectStore.getChild("File.txt");

		//add some content to the current location
		IFolder folder = target.getFolder("Folder");
		IFile file = folder.getFile("File.txt");
		ensureExistsInWorkspace(file, true);

		//add content to new location
		IFile newFile = target.getFile(childFile.getName());
		createFileInFileSystem(childFile);

		//replace project location
		try {
			IProjectDescription description = target.getDescription();
			description.setLocationURI(projectStore.toURI());
			target.move(description, IResource.REPLACE, getMonitor());
		} catch (CoreException e) {
			fail("1.99", e);
		}

		//existing contents should no longer exist
		assertTrue("2.0", !folder.exists());
		assertTrue("2.1", !file.exists());
		assertTrue("2.2", newFile.exists());

		//move back to default location
		try {
			IProjectDescription description = target.getDescription();
			description.setLocationURI(null);
			target.move(description, IResource.REPLACE, getMonitor());
		} catch (CoreException e) {
			fail("2.99", e);
		}

		//old resources should now exist
		assertTrue("3.0", folder.exists());
		assertTrue("3.1", file.exists());
		assertTrue("3.2", !newFile.exists());
	}

	public void testSetGetProjectPersistentProperty() {
		IProject target = getWorkspace().getRoot().getProject("Project");
		ensureExistsInWorkspace(target, true);
		try {
			setGetPersistentProperty(target);
		} catch (CoreException e) {
			fail("1.0", e);
		}
	}

	public void testWorkspaceNotificationClose() {
		final int[] count = new int[1];
		IResourceChangeListener listener = new IResourceChangeListener() {
			public void resourceChanged(IResourceChangeEvent event) {
				assertEquals("1.0", IResourceChangeEvent.PRE_CLOSE, event.getType());
				count[0]++;
				assertEquals("1.1", IResource.PROJECT, event.getResource().getType());
				assertTrue("1.2", event.getResource().exists());
				assertTrue("1.3", ((IProject) event.getResource()).isOpen());
			}
		};
		getWorkspace().addResourceChangeListener(listener, IResourceChangeEvent.PRE_CLOSE);
		IProject project = getWorkspace().getRoot().getProject("MyProject");
		try {
			project.create(getMonitor());
			project.open(getMonitor());
		} catch (CoreException e) {
			fail("1.4", e);
		}
		assertTrue("1.5", project.exists());
		assertTrue("1.6", project.isOpen());
		try {
			project.close(getMonitor());
		} catch (CoreException e) {
			fail("1.7", e);
		}
		assertEquals("1.8", 1, count[0]);
		assertTrue("1.9", project.exists());
		assertTrue("1.10", !project.isOpen());
		getWorkspace().removeResourceChangeListener(listener);
	}

	public void testWorkspaceNotificationDelete() {
		final int[] count = new int[1];
		IResourceChangeListener listener = new IResourceChangeListener() {
			public void resourceChanged(IResourceChangeEvent event) {
				assertEquals("1.0", IResourceChangeEvent.PRE_DELETE, event.getType());
				count[0]++;
				assertEquals("1.1", IResource.PROJECT, event.getResource().getType());
				assertTrue("1.2", event.getResource().exists());
			}
		};
		getWorkspace().addResourceChangeListener(listener, IResourceChangeEvent.PRE_DELETE);
		IProject project = getWorkspace().getRoot().getProject("MyProject");
		try {
			project.create(getMonitor());
			project.open(getMonitor());
		} catch (CoreException e) {
			fail("1.3", e);
		}
		assertTrue("1.4", project.exists());
		try {
			project.delete(IResource.FORCE, getMonitor());
		} catch (CoreException e) {
			fail("1.5", e);
		}
		assertEquals("1.6", 1, count[0]);
		assertTrue("1.7", !project.exists());
		getWorkspace().removeResourceChangeListener(listener);
	}

	public void testWorkspaceNotificationMove() {
		final int[] count = new int[1];
		IResourceChangeListener listener = new IResourceChangeListener() {
			public void resourceChanged(IResourceChangeEvent event) {
				assertEquals("1.0", IResourceChangeEvent.PRE_DELETE, event.getType());
				count[0]++;
				assertEquals("1.1", IResource.PROJECT, event.getResource().getType());
				assertTrue("1.2", event.getResource().exists());
			}
		};
		getWorkspace().addResourceChangeListener(listener, IResourceChangeEvent.PRE_DELETE);
		IProject project = getWorkspace().getRoot().getProject("MyProject");
		try {
			project.create(getMonitor());
			project.open(getMonitor());
		} catch (CoreException e) {
			fail("1.3", e);
		}
		assertTrue("1.4", project.exists());
		try {
			project.move(new Path("MyNewProject"), IResource.FORCE, getMonitor());
		} catch (CoreException e) {
			fail("1.5", e);
		}
		assertEquals("1.6", 1, count[0]);
		assertTrue("1.7", !project.exists());
		getWorkspace().removeResourceChangeListener(listener);
	}
	
	public void testCreateHiddenProject() {
		IProject hiddenProject = getWorkspace().getRoot().getProject(getUniqueString());
		ensureDoesNotExistInWorkspace(hiddenProject);

		try {
			hiddenProject.create(null, IResource.HIDDEN, getMonitor());
		} catch (CoreException e) {
			fail("1.0", e);
		}
		
		assertTrue("2.0", hiddenProject.isHidden());
		
		// try to delete and recreate the project
		try {
			hiddenProject.delete(false, getMonitor());
			hiddenProject.create(getMonitor());
		} catch (CoreException e) {
			fail("3.0", e);
		}
		
		// it should not be hidden
		assertTrue("4.0", !hiddenProject.isHidden());
	}
}
