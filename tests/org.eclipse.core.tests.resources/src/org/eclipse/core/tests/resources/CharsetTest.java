/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tests.resources;

import java.util.Map;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.internal.resources.*;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.tests.harness.EclipseWorkspaceTest;

public class CharsetTest extends EclipseWorkspaceTest {
	public CharsetTest() {
		super();
	}
	public CharsetTest(String name) {
		super(name);
	}
	public void testDefaults() throws CoreException {
		IProject project = null;
		String originalCharset = ResourcesPlugin.getPlugin().getPluginPreferences().getString(ResourcesPlugin.PREF_ENCODING);
		try {
			IWorkspace workspace = getWorkspace();
			project = workspace.getRoot().getProject("MyProject");
			IFolder folder1 = project.getFolder("folder1");
			IFolder folder2 = folder1.getFolder("folder2");
			IFile file1 = project.getFile("file1.txt");
			IFile file2 = folder1.getFile("file2.txt");
			IFile file3 = folder2.getFile("file3.txt");
			ensureExistsInWorkspace(new IResource[]{file1, file2, file3}, true);
			// project and children should be using the workspace's default now
			assertCharsetIs("1.0", ResourcesPlugin.getEncoding(), new IResource[]{workspace.getRoot(), project, file1, folder1, file2, folder2, file3});
			// sets workspace default charset
			workspace.getRoot().setDefaultCharset("FOO");
			assertCharsetIs("2.0", "FOO", new IResource[]{workspace.getRoot(), project, file1, folder1, file2, folder2, file3});
			// sets project default charset
			project.setDefaultCharset("BAR");
			assertCharsetIs("3.0", "BAR", new IResource[]{project, file1, folder1, file2, folder2, file3});
			assertCharsetIs("3.1", "FOO", new IResource[]{workspace.getRoot()});
			// sets folder1 default charset
			folder1.setDefaultCharset("FRED");
			assertCharsetIs("4.0", "FRED", new IResource[]{folder1, file2, folder2, file3});
			assertCharsetIs("4.1", "BAR", new IResource[]{project, file1});
			// sets folder2 default charset
			folder2.setDefaultCharset("ZOO");
			assertCharsetIs("5.0", "ZOO", new IResource[]{folder2, file3});
			assertCharsetIs("5.1", "FRED", new IResource[]{folder1, file2});
			// sets file3 charset
			file3.setCharset("ZIT");
			assertCharsetIs("5.0", "ZIT", new IResource[]{file3});
			folder2.setDefaultCharset(null);
			assertCharsetIs("6.0", folder2.getParent().getDefaultCharset(), new IResource[]{folder2});
			assertCharsetIs("6.1", "ZIT", new IResource[]{file3});
			folder1.setDefaultCharset(null);
			assertCharsetIs("7.0", folder1.getParent().getDefaultCharset(), new IResource[]{folder1, file2, folder2});
			assertCharsetIs("7.1", "ZIT", new IResource[]{file3});
			project.setDefaultCharset(null);
			assertCharsetIs("8.0", project.getParent().getDefaultCharset(), new IResource[]{project, file1, folder1, file2, folder2});
			assertCharsetIs("8.1", "ZIT", new IResource[]{file3});
			workspace.getRoot().setDefaultCharset(null);
			assertCharsetIs("9.0", project.getParent().getDefaultCharset(), new IResource[]{project, file1, folder1, file2, folder2});
			assertCharsetIs("9.1", "ZIT", new IResource[]{file3});
			file3.setCharset(null);
			assertCharsetIs("9.0", ResourcesPlugin.getEncoding(), new IResource[]{workspace.getRoot(), project, file1, folder1, file2, folder2, file3});
		} finally {
			ResourcesPlugin.getPlugin().getPluginPreferences().setValue(ResourcesPlugin.PREF_ENCODING, originalCharset);
			if (project != null)
				ensureDoesNotExistInWorkspace(project);
		}
	}
	public void testFileCreation() throws CoreException {
		IWorkspace workspace = getWorkspace();
		IProject project = workspace.getRoot().getProject("MyProject");
		try {
			IFolder folder = project.getFolder("folder");
			IFile file1 = project.getFile("file1.txt");
			IFile file2 = folder.getFile("file2.txt");
			ensureExistsInWorkspace(new IResource[]{file1, file2}, true);
			assertDoesNotExistInWorkspace("1.0", project.getFile(CharsetManager.ENCODING_FILE));
			project.setDefaultCharset("FOO");
			assertExistsInWorkspace("2.0", project.getFile(CharsetManager.ENCODING_FILE));
			project.setDefaultCharset(null);
			assertDoesNotExistInWorkspace("3.0", project.getFile(CharsetManager.ENCODING_FILE));
			file1.setCharset("FRED");
			assertExistsInWorkspace("4.0", project.getFile(CharsetManager.ENCODING_FILE));
			folder.setDefaultCharset("BAR");
			assertExistsInWorkspace("5.0", project.getFile(CharsetManager.ENCODING_FILE));
			file1.setCharset(null);
			assertExistsInWorkspace("6.0", project.getFile(CharsetManager.ENCODING_FILE));
			folder.setDefaultCharset(null);
			assertDoesNotExistInWorkspace("7.0", project.getFile(CharsetManager.ENCODING_FILE));
		} finally {
			ensureDoesNotExistInWorkspace(project);
		}
	}
	public void testClosingAndReopeningProject() throws CoreException {
		IWorkspace workspace = getWorkspace();
		IProject project = workspace.getRoot().getProject("MyProject");
		try {
			// create a project and set some explicit encodings
			IFolder folder = project.getFolder("folder");
			IFile file1 = project.getFile("file1.txt");
			IFile file2 = folder.getFile("file2.txt");
			ensureExistsInWorkspace(new IResource[]{file1, file2}, true);
			project.setDefaultCharset("FOO");
			file1.setCharset("FRED");
			folder.setDefaultCharset("BAR");
			project.close(null);
			// now reopen the project and ensure the settings were not
			// forgotten
			IProject projectB = workspace.getRoot().getProject(project.getName());
			projectB.open(null);
			assertExistsInWorkspace("0.9", projectB.getFile(CharsetManager.ENCODING_FILE));
			assertEquals("1.0", "FOO", projectB.getDefaultCharset());
			assertEquals("3.0", "FRED", projectB.getFile("file1.txt").getCharset());
			assertEquals("2.0", "BAR", projectB.getFolder("folder").getDefaultCharset());
			assertEquals("2.1", "BAR", projectB.getFolder("folder").getFile("file2.txt").getCharset());
		} finally {
			ensureDoesNotExistInWorkspace(project);
		}
	}
	public void testChangesSameProject() throws CoreException {
		IWorkspace workspace = getWorkspace();
		IProject project = workspace.getRoot().getProject("MyProject");
		try {
			IFolder folder = project.getFolder("folder1");
			IFile file1 = project.getFile("file1.txt");
			IFile file2 = folder.getFile("file2.txt");
			ensureExistsInWorkspace(new IResource[]{file1, file2}, true);
			project.setDefaultCharset("FOO");
			file1.setCharset("FRED");
			folder.setDefaultCharset("BAR");
			// move a folder inside the project and ensure its encoding is
			// preserved
			folder.move(project.getFullPath().append("folder2"), false, false, null);
			folder = project.getFolder("folder2");
			assertEquals("1.0", "BAR", folder.getDefaultCharset());
			assertEquals("1.1", "BAR", folder.getFile("file2.txt").getCharset());
			// move a file inside the project and ensure its encoding is
			// update accordingly
			file2 = folder.getFile("file2.txt");
			file2.move(project.getFullPath().append("file2.txt"), false, false, null);
			file2 = project.getFile("file2.txt");
			assertEquals("2.0", project.getDefaultCharset(), file2.getCharset());
			// delete a file and recreate it and ensure the encoding is not
			// remembered
			file1.delete(false, false, null);
			ensureExistsInWorkspace(new IResource[]{file1}, true);
			assertEquals("3.0", project.getDefaultCharset(), file1.getCharset());
		} finally {
			ensureDoesNotExistInWorkspace(project);
		}
	}
	public void testChangesDifferentProject() throws CoreException {
		IWorkspace workspace = getWorkspace();
		IProject project1 = workspace.getRoot().getProject("Project1");
		IProject project2 = workspace.getRoot().getProject("Project2");
		try {
			IFolder folder = project1.getFolder("folder1");
			IFile file1 = project1.getFile("file1.txt");
			IFile file2 = folder.getFile("file2.txt");
			ensureExistsInWorkspace(new IResource[]{file1, file2, project2}, true);
			project1.setDefaultCharset("FOO");
			project2.setDefaultCharset("ZOO");
			folder.setDefaultCharset("BAR");
			// move a folder to another project and ensure its encoding is
			// preserved
			folder.move(project2.getFullPath().append("folder"), false, false, null);
			folder = project2.getFolder("folder");
			assertEquals("1.0", "BAR", folder.getDefaultCharset());
			assertEquals("1.1", "BAR", folder.getFile("file2.txt").getCharset());
			// move a file with no charset set and check if it inherits
			// properly from the new parent
			assertEquals("2.0", project1.getDefaultCharset(), file1.getCharset());
			file1.move(project2.getFullPath().append("file1.txt"), false, false, null);
			file1 = project2.getFile("file1.txt");
			assertEquals("2.1", project2.getDefaultCharset(), file1.getCharset());
		} finally {
			ensureDoesNotExistInWorkspace(project1);
			ensureDoesNotExistInWorkspace(project2);
		}
	}
	/**
	 * Ensures we are not discarding the cached info when we receive our own
	 * changes.
	 */
	public void testAvoidOwnChanges() throws CoreException {
		IWorkspace workspace = getWorkspace();
		IProject project = workspace.getRoot().getProject("MyProject");
		try {
			IFile file = project.getFile("file.txt");
			ensureExistsInWorkspace(file, true);
			file.setCharset("FOO");
			Map charsets = ((ProjectInfo) ((Project) project).getResourceInfo(false, false)).getCharsets();
			assertNotNull("1.0", charsets);
			assertTrue("1.1", !charsets.isEmpty());
			assertTrue("1.2", charsets.containsKey(file.getFullPath().removeFirstSegments(1)));
			assertEquals("1.3", "FOO", charsets.get(file.getFullPath().removeFirstSegments(1)));
		} finally {
			ensureDoesNotExistInWorkspace(project);
		}
	}
	public void testExternalChanges() throws CoreException {
		IWorkspace workspace = getWorkspace();
		IProject project = workspace.getRoot().getProject("MyProject");
		try {
			IFile file = project.getFile("file.txt");
			ensureExistsInWorkspace(file, true);
			// cause an external change to the encoding file
			this.ensureExistsInWorkspace(project.getFile(CharsetManager.ENCODING_FILE), file.getFullPath().removeFirstSegments(1) + ":" + "ZOO");
			// we should have invalidated the cached info
			assertNull("1.0", ((ProjectInfo) ((Project) project).getResourceInfo(false, false)).getCharsets());
			// asking for the resource's charset should see the new encoding
			// set externally
			assertEquals("2.0", "ZOO", file.getCharset());
		} finally {
			ensureDoesNotExistInWorkspace(project);
		}
	}
	/**
	 * Moves a project and ensures the charsets are preserved.
	 */
	public void testMovingProject() throws CoreException {
		IWorkspace workspace = getWorkspace();
		IProject project1 = workspace.getRoot().getProject("Project1");
		IProject project2 = null;
		try {
			IFolder folder = project1.getFolder("folder1");
			IFile file1 = project1.getFile("file1.txt");
			IFile file2 = folder.getFile("file2.txt");
			ensureExistsInWorkspace(new IResource[]{file1, file2}, true);
			project1.setDefaultCharset("FOO");
			folder.setDefaultCharset("BAR");
			// move project and ensures charsets settings are preserved
			project1.move(new Path("Project2"), false, null);
			project2 = workspace.getRoot().getProject("Project2");
			folder = project2.getFolder("folder1");
			file1 = project2.getFile("file1.txt");
			file2 = folder.getFile("file2.txt");
			assertEquals("1.0", "BAR", folder.getDefaultCharset());
			assertEquals("1.1", "BAR", file2.getCharset());
			assertEquals("1.2", "FOO", file1.getCharset());
			assertEquals("1.3", "FOO", project2.getDefaultCharset());
		} finally {
			ensureDoesNotExistInWorkspace(project1);
			if (project2 != null)
				ensureDoesNotExistInWorkspace(project2);
		}
	}
	/**
	 * Asserts that the given resources have the given [default] charset.
	 */
	private void assertCharsetIs(String tag, String encoding, IResource[] resources) throws CoreException {
		for (int i = 0; i < resources.length; i++) {
			String resourceCharset = resources[i] instanceof IFile ? ((IFile) resources[i]).getCharset() : ((IContainer) resources[i]).getDefaultCharset();
			assertEquals(tag + " " + resources[i].getFullPath(), encoding, resourceCharset);
		}
	}
	public static Test suite() {
		return new TestSuite(CharsetTest.class);
		//		return new EncodingTest("testAvoidOwnChanges");
	}
}
