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

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.internal.preferences.EclipsePreferences;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.content.*;
import org.eclipse.core.tests.harness.EclipseWorkspaceTest;

public class CharsetTest extends EclipseWorkspaceTest {
	private static final String SAMPLE_XML_DEFAULT_ENCODING = "<?xml version=\"1.0\"?><org.eclipse.core.resources.tests.root/>";
	private static final String SAMPLE_XML_US_ASCII_ENCODING = "<?xml version=\"1.0\" encoding=\"US-ASCII\"?><org.eclipse.core.resources.tests.root/>";
	private static final String SAMPLE_XML_UTF_8_ENCODING = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?><org.eclipse.core.resources.tests.root/>";
	private static final String SAMPLE_SPECIFIC_XML = "<?xml version=\"1.0\"?><org.eclipse.core.tests.resources.anotherXML/>";

	public static Test suite() {
		//		TestSuite suite = new TestSuite();
		//		suite.addTest(new CharsetTest("testFileCreation"));
		//		suite.addTest(new CharsetTest("testPrefsFileCreation"));
		//		return suite;
		//return new CharsetTest("testPrefsFileCreation");
		return new TestSuite(CharsetTest.class);
	}

	public CharsetTest() {
		super();
	}

	public CharsetTest(String name) {
		super(name);
	}

	/**
	 * Asserts that the given resources have the given [default] charset.
	 */
	private void assertCharsetIs(String tag, String encoding, IResource[] resources, boolean checkImplicit) throws CoreException {
		for (int i = 0; i < resources.length; i++) {
			String resourceCharset = resources[i] instanceof IFile ? ((IFile) resources[i]).getCharset(checkImplicit) : ((IContainer) resources[i]).getDefaultCharset(checkImplicit);
			assertEquals(tag + " " + resources[i].getFullPath(), encoding, resourceCharset);
		}
	}

	private IFile getProjectEncodingSettings(IProject project) {
		IPath projectScopeLocation = new ProjectScope(project).getLocation();
		return project.getWorkspace().getRoot().getFileForLocation(projectScopeLocation.append(ResourcesPlugin.PI_RESOURCES + '.' + EclipsePreferences.PREFS_FILE_EXTENSION));
	}

	public void testChangesDifferentProject() throws CoreException {
		IWorkspace workspace = getWorkspace();
		IProject project1 = workspace.getRoot().getProject("Project1");
		IProject project2 = workspace.getRoot().getProject("Project2");
		try {
			IFolder folder = project1.getFolder("folder1");
			IFile file1 = project1.getFile("file1.txt");
			IFile file2 = folder.getFile("file2.txt");
			ensureExistsInWorkspace(new IResource[] {file1, file2, project2}, true);
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

	public void testChangesSameProject() throws CoreException {
		IWorkspace workspace = getWorkspace();
		IProject project = workspace.getRoot().getProject("MyProject");
		try {
			IFolder folder = project.getFolder("folder1");
			IFile file1 = project.getFile("file1.txt");
			IFile file2 = folder.getFile("file2.txt");
			ensureExistsInWorkspace(new IResource[] {file1, file2}, true);
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
			ensureExistsInWorkspace(new IResource[] {file1}, true);
			assertEquals("3.0", project.getDefaultCharset(), file1.getCharset());
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
			ensureExistsInWorkspace(new IResource[] {file1, file2}, true);
			project.setDefaultCharset("FOO");
			file1.setCharset("FRED");
			folder.setDefaultCharset("BAR");
			project.close(null);
			// now reopen the project and ensure the settings were not forgotten
			IProject projectB = workspace.getRoot().getProject(project.getName());
			projectB.open(null);
			assertExistsInWorkspace("0.9", getProjectEncodingSettings(projectB));
			assertEquals("1.0", "FOO", projectB.getDefaultCharset());
			assertEquals("3.0", "FRED", projectB.getFile("file1.txt").getCharset());
			assertEquals("2.0", "BAR", projectB.getFolder("folder").getDefaultCharset());
			assertEquals("2.1", "BAR", projectB.getFolder("folder").getFile("file2.txt").getCharset());
		} finally {
			ensureDoesNotExistInWorkspace(project);
		}
	}

	/**
	 * Tests Content Manager-based charset setting.  
	 */
	public void testContentBasedCharset() throws CoreException, UnsupportedEncodingException {
		IWorkspace workspace = getWorkspace();
		IProject project = workspace.getRoot().getProject("MyProject");
		try {
			ensureExistsInWorkspace(project, true);
			project.setDefaultCharset("FOO");
			IFile file = project.getFile("file.xml");
			assertEquals("0.9", "FOO", project.getDefaultCharset());
			// content-based encoding is BAR			
			ensureExistsInWorkspace(file, new ByteArrayInputStream(SAMPLE_XML_US_ASCII_ENCODING.getBytes("UTF-8")));
			assertEquals("1.0", "US-ASCII", file.getCharset());
			// content-based encoding is FRED			
			file.setContents(new ByteArrayInputStream(SAMPLE_XML_UTF_8_ENCODING.getBytes("UTF-8")), false, false, null);
			assertEquals("2.0", "ISO-8859-1", file.getCharset());
			// content-based encoding is UTF-8 (default for XML)
			file.setContents(new ByteArrayInputStream(SAMPLE_XML_DEFAULT_ENCODING.getBytes("UTF-8")), false, false, null);
			assertEquals("3.0", "UTF-8", file.getCharset());
			// tests with BOM -BOMs are strings for convenience, encoded itno bytes using ISO-8859-1 (which handles 128-255 bytes better) 
			// tests with UTF-8 BOM
			String UTF8_BOM = new String(IContentDescription.BOM_UTF_8, "ISO-8859-1");
			file.setContents(new ByteArrayInputStream((UTF8_BOM + SAMPLE_XML_DEFAULT_ENCODING).getBytes("ISO-8859-1")), false, false, null);
			assertEquals("4.0", "UTF-8", file.getCharset());
			// tests with UTF-16 Little Endian BOM			
			String UTF16_LE_BOM = new String(IContentDescription.BOM_UTF_16LE, "ISO-8859-1");
			file.setContents(new ByteArrayInputStream((UTF16_LE_BOM + SAMPLE_XML_DEFAULT_ENCODING).getBytes("ISO-8859-1")), false, false, null);
			assertEquals("5.0", "UTF-16", file.getCharset());
			// tests with UTF-16 Big Endian BOM			
			String UTF16_BE_BOM = new String(IContentDescription.BOM_UTF_16BE, "ISO-8859-1");
			file.setContents(new ByteArrayInputStream((UTF16_BE_BOM + SAMPLE_XML_DEFAULT_ENCODING).getBytes("ISO-8859-1")), false, false, null);
			assertEquals("6.0", "UTF-16", file.getCharset());
		} finally {
			ensureDoesNotExistInWorkspace(project);
		}
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
			ensureExistsInWorkspace(new IResource[] {file1, file2, file3}, true);
			// project and children should be using the workspace's default now
			assertCharsetIs("1.0", ResourcesPlugin.getEncoding(), new IResource[] {workspace.getRoot(), project, file1, folder1, file2, folder2, file3}, true);
			assertCharsetIs("1.1", null, new IResource[] {project, file1, folder1, file2, folder2, file3}, false);
			// sets workspace default charset
			workspace.getRoot().setDefaultCharset("FOO");
			assertCharsetIs("2.0", "FOO", new IResource[] {workspace.getRoot(), project, file1, folder1, file2, folder2, file3}, true);
			assertCharsetIs("2.1", null, new IResource[] {project, file1, folder1, file2, folder2, file3}, false);
			// sets project default charset
			project.setDefaultCharset("BAR");
			assertCharsetIs("3.0", "BAR", new IResource[] {project, file1, folder1, file2, folder2, file3}, true);
			assertCharsetIs("3.1", null, new IResource[] {file1, folder1, file2, folder2, file3}, false);
			assertCharsetIs("3.2", "FOO", new IResource[] {workspace.getRoot()}, true);
			// sets folder1 default charset
			folder1.setDefaultCharset("FRED");
			assertCharsetIs("4.0", "FRED", new IResource[] {folder1, file2, folder2, file3}, true);
			assertCharsetIs("4.1", null, new IResource[] {file2, folder2, file3}, false);
			assertCharsetIs("4.2", "BAR", new IResource[] {project, file1}, true);
			// sets folder2 default charset
			folder2.setDefaultCharset("ZOO");
			assertCharsetIs("5.0", "ZOO", new IResource[] {folder2, file3}, true);
			assertCharsetIs("5.1", null, new IResource[] {file3}, false);
			assertCharsetIs("5.2", "FRED", new IResource[] {folder1, file2}, true);
			// sets file3 charset
			file3.setCharset("ZIT");
			assertCharsetIs("6.0", "ZIT", new IResource[] {file3}, false);
			folder2.setDefaultCharset(null);
			assertCharsetIs("7.0", folder2.getParent().getDefaultCharset(), new IResource[] {folder2}, true);
			assertCharsetIs("7.1", null, new IResource[] {folder2}, false);
			assertCharsetIs("7.2", "ZIT", new IResource[] {file3}, false);
			folder1.setDefaultCharset(null);
			assertCharsetIs("8.0", folder1.getParent().getDefaultCharset(), new IResource[] {folder1, file2, folder2}, true);
			assertCharsetIs("8.1", null, new IResource[] {folder1, file2, folder2}, false);
			assertCharsetIs("8.2", "ZIT", new IResource[] {file3}, false);
			project.setDefaultCharset(null);
			assertCharsetIs("9.0", project.getParent().getDefaultCharset(), new IResource[] {project, file1, folder1, file2, folder2}, true);
			assertCharsetIs("9.1", null, new IResource[] {project, file1, folder1, file2, folder2}, false);
			assertCharsetIs("9.2", "ZIT", new IResource[] {file3}, false);
			workspace.getRoot().setDefaultCharset(null);
			assertCharsetIs("10.0", project.getParent().getDefaultCharset(), new IResource[] {project, file1, folder1, file2, folder2}, true);
			assertCharsetIs("10.1", "ZIT", new IResource[] {file3}, false);
			file3.setCharset(null);
			assertCharsetIs("11.0", ResourcesPlugin.getEncoding(), new IResource[] {workspace.getRoot(), project, file1, folder1, file2, folder2, file3}, true);
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
			ensureExistsInWorkspace(new IResource[] {file1, file2}, true);
			assertDoesNotExistInWorkspace("1.0", getProjectEncodingSettings(project));
			project.setDefaultCharset("FOO");
			assertExistsInWorkspace("2.0", getProjectEncodingSettings(project));
			project.setDefaultCharset(null);
			assertDoesNotExistInWorkspace("3.0", getProjectEncodingSettings(project));
			file1.setCharset("FRED");
			assertExistsInWorkspace("4.0", getProjectEncodingSettings(project));
			folder.setDefaultCharset("BAR");
			assertExistsInWorkspace("5.0", getProjectEncodingSettings(project));
			file1.setCharset(null);
			assertExistsInWorkspace("6.0", getProjectEncodingSettings(project));
			folder.setDefaultCharset(null);
			assertDoesNotExistInWorkspace("7.0", getProjectEncodingSettings(project));
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
			ensureExistsInWorkspace(new IResource[] {file1, file2}, true);
			project1.setDefaultCharset("FOO");
			folder.setDefaultCharset("BAR");

			assertEquals("1.0", "BAR", folder.getDefaultCharset());
			assertEquals("1.1", "BAR", file2.getCharset());
			assertEquals("1.2", "FOO", file1.getCharset());
			assertEquals("1.3", "FOO", project1.getDefaultCharset());

			// move project and ensures charsets settings are preserved
			project1.move(new Path("Project2"), false, null);
			project2 = workspace.getRoot().getProject("Project2");
			folder = project2.getFolder("folder1");
			file1 = project2.getFile("file1.txt");
			file2 = folder.getFile("file2.txt");
			assertEquals("2.0", "BAR", folder.getDefaultCharset());
			assertEquals("2.1", "BAR", file2.getCharset());
			assertEquals("2.2", "FOO", project2.getDefaultCharset());
			assertEquals("2.3", "FOO", file1.getCharset());
		} finally {
			ensureDoesNotExistInWorkspace(project1);
			if (project2 != null)
				ensureDoesNotExistInWorkspace(project2);
		}
	}

	/**
	 * Two things to test here:
	 * 	- non-existing resources default to the parent's default charset;
	 * 	- cannot set the charset for a non-existing resource (exception is thrown). 
	 */
	public void testNonExistingResource() throws CoreException {
		IWorkspace workspace = getWorkspace();
		IProject project = workspace.getRoot().getProject("MyProject");
		try {
			try {
				project.setDefaultCharset("FOO");
				fail("1.0");
			} catch (CoreException e) {
				// expected, project does not exist yet 
				assertEquals("1.1", IResourceStatus.RESOURCE_NOT_FOUND, e.getStatus().getCode());
			}
			ensureExistsInWorkspace(project, true);
			project.setDefaultCharset("FOO");
			IFile file = project.getFile("file.xml");
			assertDoesNotExistInWorkspace("2.0", file);
			assertEquals("2.2", "FOO", file.getCharset());
			try {
				file.setCharset("BAR");
				fail("2.4");
			} catch (CoreException e) {
				// expected, file does not exist yet
				assertEquals("2.6", IResourceStatus.RESOURCE_NOT_FOUND, e.getStatus().getCode());
			}
			ensureExistsInWorkspace(file, true);
			file.setCharset("BAR");
			assertEquals("2.8", "BAR", file.getCharset());
			file.delete(IResource.NONE, null);
			assertDoesNotExistInWorkspace("2.10", file);
			assertEquals("2.11", "FOO", file.getCharset());
		} finally {
			ensureDoesNotExistInWorkspace(project);
		}
	}

	public void testBug62732() throws UnsupportedEncodingException, CoreException {
		IWorkspace workspace = getWorkspace();
		IProject project = workspace.getRoot().getProject("MyProject");
		try {
			IContentTypeManager contentTypeManager = Platform.getContentTypeManager();
			IContentType anotherXML = contentTypeManager.getContentType("org.eclipse.core.tests.resources.anotherXML");
			assertNotNull("0.5", anotherXML);
			ensureExistsInWorkspace(project, true);
			IFile file = project.getFile("file.xml");
			ensureExistsInWorkspace(file, new ByteArrayInputStream(SAMPLE_SPECIFIC_XML.getBytes("UTF-8")));
			IContentDescription description = file.getContentDescription();
			assertNotNull("1.0", description);
			assertEquals("1.1", anotherXML, description.getContentType());
			description = file.getContentDescription();
			assertNotNull("2.0", description);
			assertEquals("2.1", anotherXML, description.getContentType());
		} finally {
			ensureDoesNotExistInWorkspace(project);
		}
	}
	public void testBug64503() throws CoreException {
		IWorkspace workspace = getWorkspace();
		IProject project = workspace.getRoot().getProject("MyProject");
		try {
			IContentTypeManager contentTypeManager = Platform.getContentTypeManager();
			IContentType text = contentTypeManager.getContentType("org.eclipse.core.runtime.text");			
			IFile file = project.getFile("file.txt");			
			ensureExistsInWorkspace(file, true);
			IContentDescription description = file.getContentDescription();
			assertNotNull("1.0", description);
			assertEquals("1.1", text, description.getContentType());
			ensureDoesNotExistInWorkspace(file);
			try {
				description = file.getContentDescription();
				fail("1.2 - should have failed");
			} catch (CoreException ce) {
				// ok, the resource does not exist
			}
		} finally {
			ensureDoesNotExistInWorkspace(project);
		}		
	}	
	
}