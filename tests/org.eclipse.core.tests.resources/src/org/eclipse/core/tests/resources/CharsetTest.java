/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tests.resources;

import java.io.*;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.internal.preferences.EclipsePreferences;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.content.*;

public class CharsetTest extends ResourceTest {

	public class CharsetVerifier extends ResourceDeltaVerifier {
		final static int IGNORE_BACKGROUND_THREAD = 0x02;
		final static int IGNORE_CREATION_THREAD = 0x01;
		private Thread creationThread = Thread.currentThread();
		private int flags;

		CharsetVerifier(int flags) {
			this.flags = flags;
		}

		void internalVerifyDelta(IResourceDelta delta) {
			// ignore any changes to project preferences
			IPath path = delta.getFullPath();
			if (path.segmentCount() == 2 && (path.segment(1).equals(".settings") || path.segment(1).equals(".project")))
				return;
			super.internalVerifyDelta(delta);
		}

		private boolean isSet(int mask) {
			return (mask & flags) == mask;
		}

		public synchronized void resourceChanged(IResourceChangeEvent e) {
			// to make testing easier, we allow events from the main or other thread to be ignored
			if (isSet(IGNORE_BACKGROUND_THREAD) && Thread.currentThread() != creationThread)
				return;
			if (isSet(IGNORE_CREATION_THREAD) && Thread.currentThread() == creationThread)
				return;
			super.resourceChanged(e);
			this.notify();
		}

		public synchronized boolean waitForEvent(long limit) {
			if (hasBeenNotified())
				return true;
			try {
				this.wait(limit);
			} catch (InterruptedException e) {
				// ignore
			}
			return hasBeenNotified();
		}
	}

	static final String SAMPLE_SPECIFIC_XML = "<?xml version=\"1.0\"?><org.eclipse.core.tests.resources.anotherXML/>";
	private static final String SAMPLE_XML_DEFAULT_ENCODING = "<?xml version=\"1.0\"?><org.eclipse.core.resources.tests.root/>";
	static final String SAMPLE_XML_ISO_8859_1_ENCODING = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?><org.eclipse.core.resources.tests.root/>";
	static final String SAMPLE_XML_US_ASCII_ENCODING = "<?xml version=\"1.0\" encoding=\"US-ASCII\"?><org.eclipse.core.resources.tests.root/>";

	private String savedWorkspaceCharset;

	public static Test suite() {
		//		TestSuite suite = new TestSuite();
		//		suite.addTest(new CharsetTest("testFileCreation"));
		//		suite.addTest(new CharsetTest("testPrefsFileCreation"));
		//		return suite;
		//		return new CharsetTest("testMovingProject");

		return new TestSuite(CharsetTest.class);

		//    // cause the same test to run several times to catch a timing problem		
		//		TestSuite suite = new TestSuite();
		//		for (int i = 0; i < 1000; i++)
		//			suite.addTest(new CharsetTest("testDeltasFile"));
		//		return suite;
	}

	public CharsetTest(String name) {
		super(name);
	}

	/**
	 * See bug 67606.
	 * 
	 * TODO enable when bug is fixed
	 */
	public void _testBug67606() throws CoreException {
		IWorkspace workspace = getWorkspace();
		final IProject project = workspace.getRoot().getProject("MyProject");
		try {
			final IFile file = project.getFile("file.txt");
			ensureExistsInWorkspace(file, true);
			project.setDefaultCharset("FOO", getMonitor());
			workspace.run(new IWorkspaceRunnable() {
				public void run(IProgressMonitor monitor) throws CoreException {
					assertEquals("0.9", "FOO", file.getCharset());
					file.setCharset("BAR", getMonitor());
					assertEquals("1.0", "BAR", file.getCharset());
					file.move(project.getFullPath().append("file2.txt"), IResource.NONE, monitor);
					IFile file2 = project.getFile("file2.txt");
					assertExistsInWorkspace(file2, false);
					assertEquals("2.0", "BAR", file.getCharset());
				}
			}, null);
		} finally {
			ensureDoesNotExistInWorkspace(project);
		}
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

	private void clearAllEncodings(IResource root) throws CoreException {
		if (root == null || !root.exists())
			return;
		IResourceVisitor visitor = new IResourceVisitor() {
			public boolean visit(IResource resource) throws CoreException {
				if (!resource.exists())
					return false;
				switch (resource.getType()) {
					case IResource.FILE :
						((IFile) resource).setCharset(null, getMonitor());
						break;
					case IResource.ROOT :
						// do nothing
						break;
					default :
						((IContainer) resource).setDefaultCharset(null, getMonitor());
				}
				return true;
			}
		};
		root.accept(visitor);
	}

	private IFile getProjectEncodingSettings(IProject project) {
		IPath projectScopeLocation = new ProjectScope(project).getLocation();
		return project.getWorkspace().getRoot().getFileForLocation(projectScopeLocation.append(ResourcesPlugin.PI_RESOURCES + '.' + EclipsePreferences.PREFS_FILE_EXTENSION));
	}

	IFile getResourcesPreferenceFile(IProject project) {
		return project.getFolder(".settings").getFile("org.eclipse.core.resources.prefs");
	}

	private Reader getTextContents(String text) {
		return new StringReader(text);
	}

	protected void setUp() throws Exception {
		super.setUp();
		// save the workspace charset so it can be restored after the test
		savedWorkspaceCharset = ResourcesPlugin.getPlugin().getPluginPreferences().getString(ResourcesPlugin.PREF_ENCODING);
	}

	protected void tearDown() throws Exception {
		// restore the workspace charset 
		ResourcesPlugin.getPlugin().getPluginPreferences().setValue(ResourcesPlugin.PREF_ENCODING, savedWorkspaceCharset);
		super.tearDown();
	}

	public void testBug59899() {
		IWorkspace workspace = getWorkspace();
		IProject project = workspace.getRoot().getProject(getUniqueString());
		try {
			IFile file = project.getFile("file.txt");
			IFolder folder = project.getFolder("folder");
			ensureExistsInWorkspace(new IResource[] {file, folder}, true);
			try {
				file.setCharset("FOO", getMonitor());
			} catch (CoreException e) {
				fail("1.0", e);
			}
			try {
				folder.setDefaultCharset("BAR", getMonitor());
			} catch (CoreException e) {
				fail("2.0", e);
			}
			try {
				project.setDefaultCharset("PROJECT_CHARSET", getMonitor());
			} catch (CoreException e) {
				fail("3.0", e);
			}
			try {
				getWorkspace().getRoot().setDefaultCharset("ROOT_CHARSET", getMonitor());
			} catch (CoreException e) {
				fail("4.0", e);
			}
		} finally {
			try {
				clearAllEncodings(project);
			} catch (CoreException e) {
				fail("99.9", e);
			}
		}
	}

	public void testBug62732() throws UnsupportedEncodingException, CoreException {
		IWorkspace workspace = getWorkspace();
		IProject project = workspace.getRoot().getProject("MyProject");
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
	}

	public void testBug64503() throws CoreException {
		IWorkspace workspace = getWorkspace();
		IProject project = workspace.getRoot().getProject("MyProject");
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
	}

	public void testBug94279() throws CoreException {
		final IWorkspaceRoot root = getWorkspace().getRoot();
		String originalUserCharset = root.getDefaultCharset(false);
		try {
			root.setDefaultCharset(null);
			assertNull("1.0", root.getDefaultCharset(false));
		} finally {
			if (originalUserCharset != null)
				root.setDefaultCharset(originalUserCharset);
		}
	}
	
	public void testBug186984() {
		IWorkspace workspace = getWorkspace();
		IProject project = workspace.getRoot().getProject(getUniqueString());
		IFile file = project.getFile("file.xml");

		// test if we can get the charset, when the file doesn't exist in a file system
		try {
			file.getCharset(true);
		} catch (CoreException ex) {
			fail("1.0");
		}

		// test if we can get the charset, when the file is out-of-sync
		ensureExistsInWorkspace(file, true);
		try {
			if (!file.getLocation().toFile().delete())
				fail("2.0");
			file.getCharset(true);
			fail("2.1");
		} catch (CoreException ex) {
		}

		ensureExistsInFileSystem(file);
		ensureOutOfSync(file);
		//getCharset uses a cached value, so it will still pass
		try {
			file.getCharset(true);
		} catch (CoreException ex) {
			fail("3.0");
		}
		//getContentDescription checks synchronization state, so it should fail
		try {
			file.getContentDescription();
			fail("3.1");
		} catch (CoreException ex) {
			assertEquals("3.2", IResourceStatus.OUT_OF_SYNC_LOCAL, ex.getStatus().getCode());
		}
		

		// test if we can get the charset, when the file is refreshed
		try {
			file.refreshLocal(IResource.DEPTH_ZERO, null);
			file.getCharset(true);
		} catch (CoreException ex) {
			fail("4.0");
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
			ensureExistsInWorkspace(new IResource[] {file1, file2, project2}, true);
			project1.setDefaultCharset("FOO", getMonitor());
			project2.setDefaultCharset("ZOO", getMonitor());
			folder.setDefaultCharset("BAR", getMonitor());
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
			try {
				clearAllEncodings(project1);
				clearAllEncodings(project2);
			} catch (CoreException e) {
				fail("99.9", e);
			}
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
			project.setDefaultCharset("FOO", getMonitor());
			file1.setCharset("FRED", getMonitor());
			folder.setDefaultCharset("BAR", getMonitor());
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
			try {
				clearAllEncodings(project);
			} catch (CoreException e) {
				fail("99.9", e);
			}
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
			project.setDefaultCharset("FOO", getMonitor());
			file1.setCharset("FRED", getMonitor());
			folder.setDefaultCharset("BAR", getMonitor());
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
			try {
				clearAllEncodings(project);
			} catch (CoreException e) {
				fail("99.9", e);
			}
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
			project.setDefaultCharset("FOO", getMonitor());
			IFile file = project.getFile("file.xml");
			assertEquals("0.9", "FOO", project.getDefaultCharset());
			// content-based encoding is BAR			
			ensureExistsInWorkspace(file, new ByteArrayInputStream(SAMPLE_XML_US_ASCII_ENCODING.getBytes("UTF-8")));
			assertEquals("1.0", "US-ASCII", file.getCharset());
			// content-based encoding is FRED			
			file.setContents(new ByteArrayInputStream(SAMPLE_XML_ISO_8859_1_ENCODING.getBytes("ISO-8859-1")), false, false, null);
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
			try {
				clearAllEncodings(project);
			} catch (CoreException e) {
				fail("99.9", e);
			}
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
			workspace.getRoot().setDefaultCharset("FOO", getMonitor());
			assertCharsetIs("2.0", "FOO", new IResource[] {workspace.getRoot(), project, file1, folder1, file2, folder2, file3}, true);
			assertCharsetIs("2.1", null, new IResource[] {project, file1, folder1, file2, folder2, file3}, false);
			// sets project default charset
			project.setDefaultCharset("BAR", getMonitor());
			assertCharsetIs("3.0", "BAR", new IResource[] {project, file1, folder1, file2, folder2, file3}, true);
			assertCharsetIs("3.1", null, new IResource[] {file1, folder1, file2, folder2, file3}, false);
			assertCharsetIs("3.2", "FOO", new IResource[] {workspace.getRoot()}, true);
			// sets folder1 default charset
			folder1.setDefaultCharset("FRED", getMonitor());
			assertCharsetIs("4.0", "FRED", new IResource[] {folder1, file2, folder2, file3}, true);
			assertCharsetIs("4.1", null, new IResource[] {file2, folder2, file3}, false);
			assertCharsetIs("4.2", "BAR", new IResource[] {project, file1}, true);
			// sets folder2 default charset
			folder2.setDefaultCharset("ZOO", getMonitor());
			assertCharsetIs("5.0", "ZOO", new IResource[] {folder2, file3}, true);
			assertCharsetIs("5.1", null, new IResource[] {file3}, false);
			assertCharsetIs("5.2", "FRED", new IResource[] {folder1, file2}, true);
			// sets file3 charset
			file3.setCharset("ZIT", getMonitor());
			assertCharsetIs("6.0", "ZIT", new IResource[] {file3}, false);
			folder2.setDefaultCharset(null, getMonitor());
			assertCharsetIs("7.0", folder2.getParent().getDefaultCharset(), new IResource[] {folder2}, true);
			assertCharsetIs("7.1", null, new IResource[] {folder2}, false);
			assertCharsetIs("7.2", "ZIT", new IResource[] {file3}, false);
			folder1.setDefaultCharset(null, getMonitor());
			assertCharsetIs("8.0", folder1.getParent().getDefaultCharset(), new IResource[] {folder1, file2, folder2}, true);
			assertCharsetIs("8.1", null, new IResource[] {folder1, file2, folder2}, false);
			assertCharsetIs("8.2", "ZIT", new IResource[] {file3}, false);
			project.setDefaultCharset(null, getMonitor());
			assertCharsetIs("9.0", project.getParent().getDefaultCharset(), new IResource[] {project, file1, folder1, file2, folder2}, true);
			assertCharsetIs("9.1", null, new IResource[] {project, file1, folder1, file2, folder2}, false);
			assertCharsetIs("9.2", "ZIT", new IResource[] {file3}, false);
			workspace.getRoot().setDefaultCharset(null, getMonitor());
			assertCharsetIs("10.0", project.getParent().getDefaultCharset(), new IResource[] {project, file1, folder1, file2, folder2}, true);
			assertCharsetIs("10.1", "ZIT", new IResource[] {file3}, false);
			file3.setCharset(null, getMonitor());
			assertCharsetIs("11.0", ResourcesPlugin.getEncoding(), new IResource[] {workspace.getRoot(), project, file1, folder1, file2, folder2, file3}, true);
		} finally {
			ResourcesPlugin.getPlugin().getPluginPreferences().setValue(ResourcesPlugin.PREF_ENCODING, originalCharset);
			try {
				clearAllEncodings(project);
			} catch (CoreException e) {
				fail("99.9", e);
			}
		}
	}

	// check we react to content type changes
	public void testDeltaOnContentTypeChanges() {
		final String USER_SETTING = "USER_CHARSET";
		final String PROVIDER_SETTING = "PROVIDER_CHARSET";

		// install a verifier		
		CharsetVerifier backgroundVerifier = new CharsetVerifier(CharsetVerifier.IGNORE_CREATION_THREAD);
		getWorkspace().addResourceChangeListener(backgroundVerifier, IResourceChangeEvent.POST_CHANGE);
		IContentTypeManager contentTypeManager = Platform.getContentTypeManager();
		IContentType myType = contentTypeManager.getContentType("org.eclipse.core.tests.resources.myContent");
		assertNotNull("0.1", myType);
		assertEquals("0.2", PROVIDER_SETTING, myType.getDefaultCharset());
		IProject project = getWorkspace().getRoot().getProject("project1");
		try {
			IFolder folder1 = project.getFolder("folder1");
			IFile file1 = folder1.getFile("file1.txt");
			IFile file2 = folder1.getFile("file2.resources-mc");
			IFile file3 = project.getFile("file3.resources-mc");
			IFile file4 = project.getFile("file4.resources-mc");
			ensureExistsInWorkspace(new IResource[] {file1, file2, file3, file4}, true);
			try {
				project.setDefaultCharset("FOO", getMonitor());
			} catch (CoreException e) {
				fail("1.0", e);
			}
			// even files with a user-set charset will appear in the delta
			try {
				file4.setCharset("BAR", getMonitor());
			} catch (CoreException e) {
				fail("1.1", e);
			}
			// configure verifier
			backgroundVerifier.reset();
			backgroundVerifier.addExpectedChange(new IResource[] {file2, file3, file4}, IResourceDelta.CHANGED, IResourceDelta.ENCODING);
			// change content type's default charset
			try {
				myType.setDefaultCharset(USER_SETTING);
			} catch (CoreException e) {
				fail("2.0", e);
			}
			// ensure the property events were generated
			assertTrue("2.1", backgroundVerifier.waitForEvent(10000));
			assertTrue("2.2 " + backgroundVerifier.getMessage(), backgroundVerifier.isDeltaValid());
			try {
				assertEquals("3.0", USER_SETTING, file2.getCharset());
				assertEquals("3.1", USER_SETTING, file3.getCharset());
				assertEquals("3.2", "BAR", file4.getCharset());
			} catch (CoreException e) {
				fail("3.9", e);
			}

			// change back to the provider-provided default
			// configure verifier
			backgroundVerifier.reset();
			backgroundVerifier.addExpectedChange(new IResource[] {file2, file3, file4}, IResourceDelta.CHANGED, IResourceDelta.ENCODING);
			// reset charset to default
			try {
				myType.setDefaultCharset(null);
			} catch (CoreException e) {
				fail("4.0", e);
			}
			// ensure the property events were generated
			assertTrue("4.1", backgroundVerifier.waitForEvent(10000));
			assertTrue("4.2 " + backgroundVerifier.getMessage(), backgroundVerifier.isDeltaValid());
			try {
				assertEquals("5.0", PROVIDER_SETTING, file2.getCharset());
				assertEquals("5.1", PROVIDER_SETTING, file3.getCharset());
				assertEquals("5.2", "BAR", file4.getCharset());
			} catch (CoreException e) {
				fail("5.9", e);
			}
		} finally {
			getWorkspace().removeResourceChangeListener(backgroundVerifier);
			try {
				myType.setDefaultCharset(null);
			} catch (CoreException e) {
				fail("99.0", e);
			}
			try {
				clearAllEncodings(project);
			} catch (CoreException e) {
				fail("99.9", e);
			}
		}
	}

	// check preference change events are reflected in the charset settings
	// temporarily disabled
	public void testDeltaOnPreferenceChanges() {

		CharsetVerifier backgroundVerifier = new CharsetVerifier(CharsetVerifier.IGNORE_CREATION_THREAD);
		getWorkspace().addResourceChangeListener(backgroundVerifier, IResourceChangeEvent.POST_CHANGE);
		IProject project = getWorkspace().getRoot().getProject("project1");
		try {
			IFolder folder1 = project.getFolder("folder1");
			IFile file1 = folder1.getFile("file1.txt");
			IFile file2 = project.getFile("file2.txt");
			ensureExistsInWorkspace(new IResource[] {file1, file2}, true);

			IFile resourcesPrefs = getResourcesPreferenceFile(project);
			assertTrue("0.9", !resourcesPrefs.exists());

			try {
				file1.setCharset("CHARSET1", getMonitor());
			} catch (CoreException e) {
				fail("1.0", e);
			}
			assertTrue("1.1", resourcesPrefs.exists());

			backgroundVerifier.reset();
			backgroundVerifier.addExpectedChange(new IResource[] {project, folder1, file1, file2}, IResourceDelta.CHANGED, IResourceDelta.ENCODING);
			// cause a resource change event without actually changing contents			
			try {
				resourcesPrefs.setContents(resourcesPrefs.getContents(), 0, getMonitor());
			} catch (CoreException e) {
				fail("2.0", e);
			}
			assertTrue("2.1", backgroundVerifier.waitForEvent(10000));
			assertTrue("2.2 " + backgroundVerifier.getMessage(), backgroundVerifier.isDeltaValid());

			backgroundVerifier.reset();
			backgroundVerifier.addExpectedChange(new IResource[] {project, folder1, file1, file2}, IResourceDelta.CHANGED, IResourceDelta.ENCODING);
			try {
				// delete the preferences file
				resourcesPrefs.delete(true, getMonitor());
			} catch (CoreException e) {
				fail("3.0", e);
			}
			assertTrue("3.1", backgroundVerifier.waitForEvent(10000));
			assertTrue("3.2 " + backgroundVerifier.getMessage(), backgroundVerifier.isDeltaValid());

		} finally {
			getWorkspace().removeResourceChangeListener(backgroundVerifier);
			try {
				clearAllEncodings(project);
			} catch (CoreException e) {
				fail("99.9", e);
			}
		}
	}

	/**
	 * Test the contents of the resource deltas which are generated
	 * when we make encoding changes to containers (folders, projects, root).
	 */
	public void testDeltasContainer() {
		CharsetVerifier verifier = new CharsetVerifier(CharsetVerifier.IGNORE_BACKGROUND_THREAD);
		IProject project = getWorkspace().getRoot().getProject(getUniqueString());
		getWorkspace().addResourceChangeListener(verifier, IResourceChangeEvent.POST_CHANGE);
		try {
			// leaf folder
			IFolder folder1 = project.getFolder("folder1");
			ensureExistsInWorkspace(new IResource[] {project, folder1}, true);
			verifier.reset();
			verifier.addExpectedChange(folder1, IResourceDelta.CHANGED, IResourceDelta.ENCODING);
			try {
				folder1.setDefaultCharset("new_charset", getMonitor());
			} catch (CoreException e) {
				fail("1.0", e);
			}
			assertTrue("1.1." + verifier.getMessage(), verifier.isDeltaValid());

			// folder with children
			IFolder folder2 = folder1.getFolder("folder2");
			IFile file1 = folder1.getFile("file1.txt");
			IFile file2 = folder2.getFile("file2.txt");
			ensureExistsInWorkspace(new IResource[] {folder2, file1, file2}, true);
			verifier.reset();
			verifier.addExpectedChange(new IResource[] {folder1, folder2, file1, file2}, IResourceDelta.CHANGED, IResourceDelta.ENCODING);
			try {
				folder1.setDefaultCharset("a_charset", getMonitor());
			} catch (CoreException e) {
				fail("2.0", e);
			}
			assertTrue("2.1." + verifier.getMessage(), verifier.isDeltaValid());

			// folder w. children, some with non-inherited values
			try {
				// set the child to have a non-inherited value
				folder2.setDefaultCharset("non-Default", getMonitor());
			} catch (CoreException e) {
				fail("3.0", e);
			}
			verifier.reset();
			verifier.addExpectedChange(new IResource[] {folder1, file1}, IResourceDelta.CHANGED, IResourceDelta.ENCODING);
			try {
				folder1.setDefaultCharset("newOne", getMonitor());
			} catch (CoreException e) {
				fail("3.1", e);
			}
			assertTrue("3.2." + verifier.getMessage(), verifier.isDeltaValid());

			// change from non-default to another non-default
			verifier.reset();
			verifier.addExpectedChange(new IResource[] {folder1, file1}, IResourceDelta.CHANGED, IResourceDelta.ENCODING);
			try {
				folder1.setDefaultCharset("newTwo", getMonitor());
			} catch (CoreException e) {
				fail("4.1", e);
			}
			assertTrue("4.2." + verifier.getMessage(), verifier.isDeltaValid());

			// change to default (clear it)
			verifier.reset();
			verifier.addExpectedChange(new IResource[] {folder1, file1}, IResourceDelta.CHANGED, IResourceDelta.ENCODING);
			try {
				folder1.setDefaultCharset(null, getMonitor());
			} catch (CoreException e) {
				fail("5.0", e);
			}
			assertTrue("5.1." + verifier.getMessage(), verifier.isDeltaValid());

			// change to default (equal to it but it doesn't inherit)
			verifier.reset();
			verifier.addExpectedChange(new IResource[] {folder1, file1}, IResourceDelta.CHANGED, IResourceDelta.ENCODING);
			try {
				folder1.setDefaultCharset(project.getDefaultCharset(), getMonitor());
			} catch (CoreException e) {
				fail("6.0", e);
			}
			assertTrue("6.1." + verifier.getMessage(), verifier.isDeltaValid());

			// clear all the encoding info before we start working with the project
			try {
				clearAllEncodings(project);
			} catch (CoreException e) {
				fail("7.0", e);
			}
			verifier.reset();
			verifier.addExpectedChange(new IResource[] {project, folder1, folder2, file1, file2}, IResourceDelta.CHANGED, IResourceDelta.ENCODING);
			try {
				project.setDefaultCharset("foo", getMonitor());
			} catch (CoreException e) {
				fail("7.1", e);
			}
			assertTrue("7.2." + verifier.getMessage(), verifier.isDeltaValid());

			// clear all the encoding info before we start working with the root
			try {
				clearAllEncodings(project);
			} catch (CoreException e) {
				fail("8.0", e);
			}
			verifier.reset();
			verifier.addExpectedChange(new IResource[] {project, folder1, folder2, file1, file2}, IResourceDelta.CHANGED, IResourceDelta.ENCODING);
			try {
				getWorkspace().getRoot().setDefaultCharset("foo", getMonitor());
			} catch (CoreException e) {
				fail("8.1", e);
			}
			assertTrue("8.2." + verifier.getMessage(), verifier.isDeltaValid());
		} finally {
			getWorkspace().removeResourceChangeListener(verifier);
			try {
				clearAllEncodings(project);
			} catch (CoreException e) {
				fail("99.9", e);
			}
		}
	}

	/**
	 * Check that we are broadcasting the correct resource deltas when
	 * making encoding changes.
	 *
	 */
	public void testDeltasFile() {
		IWorkspace workspace = getWorkspace();
		CharsetVerifier verifier = new CharsetVerifier(CharsetVerifier.IGNORE_BACKGROUND_THREAD);
		workspace.addResourceChangeListener(verifier, IResourceChangeEvent.POST_CHANGE);
		final IProject project = workspace.getRoot().getProject("MyProject");
		try {
			// File:
			// single file		
			final IFile file1 = project.getFile("file1.txt");
			ensureExistsInWorkspace(file1, getRandomContents());
			// change from default		
			verifier.reset();
			verifier.addExpectedChange(file1, IResourceDelta.CHANGED, IResourceDelta.ENCODING);
			try {
				file1.setCharset("FOO", getMonitor());
			} catch (CoreException e) {
				fail("1.0.0", e);
			}
			assertTrue("1.0.1" + verifier.getMessage(), verifier.isDeltaValid());

			// change to default (clear it)
			verifier.reset();
			verifier.addExpectedChange(file1, IResourceDelta.CHANGED, IResourceDelta.ENCODING);
			try {
				file1.setCharset(null, getMonitor());
			} catch (CoreException e) {
				fail("1.1.0", e);
			}
			assertTrue("1.1.1" + verifier.getMessage(), verifier.isDeltaValid());

			// change to default (equal to it but it doesn't inherit)
			verifier.reset();
			verifier.addExpectedChange(file1, IResourceDelta.CHANGED, IResourceDelta.ENCODING);
			try {
				file1.setCharset(file1.getCharset(), getMonitor());
			} catch (CoreException e) {
				fail("1.2.0", e);
			}
			assertTrue("1.2.1" + verifier.getMessage(), verifier.isDeltaValid());

			// change from non-default to another non-default
			try {
				// sets to a non-default value first
				file1.setCharset("FOO", getMonitor());
			} catch (CoreException e) {
				fail("1.3.0", e);
			}
			verifier.reset();
			verifier.addExpectedChange(file1, IResourceDelta.CHANGED, IResourceDelta.ENCODING);
			try {
				// sets to another non-defauilt value
				file1.setCharset("BAR", getMonitor());
			} catch (CoreException e) {
				fail("1.3.1", e);
			}
			assertTrue("1.3.2" + verifier.getMessage(), verifier.isDeltaValid());

			// multiple files (same operation)
			verifier.reset();
			final IFile file2 = project.getFile("file2.txt");
			ensureExistsInWorkspace(file2, getRandomContents());
			verifier.addExpectedChange(new IResource[] {file1, file2}, IResourceDelta.CHANGED, IResourceDelta.ENCODING);
			try {
				workspace.run(new IWorkspaceRunnable() {
					public void run(IProgressMonitor monitor) throws CoreException {
						file1.setCharset("FOO", getMonitor());
						file2.setCharset("FOO", getMonitor());
					}
				}, getMonitor());
			} catch (CoreException e) {
				fail("1.4.0", e);
			}
			assertTrue("1.4.1" + verifier.getMessage(), verifier.isDeltaValid());
		} finally {
			getWorkspace().removeResourceChangeListener(verifier);
			try {
				clearAllEncodings(project);
			} catch (CoreException e) {
				fail("99.9", e);
			}
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
			project.setDefaultCharset("FOO", getMonitor());
			assertExistsInWorkspace("2.0", getProjectEncodingSettings(project));
			project.setDefaultCharset(null, getMonitor());
			assertDoesNotExistInWorkspace("3.0", getProjectEncodingSettings(project));
			file1.setCharset("FRED", getMonitor());
			assertExistsInWorkspace("4.0", getProjectEncodingSettings(project));
			folder.setDefaultCharset("BAR", getMonitor());
			assertExistsInWorkspace("5.0", getProjectEncodingSettings(project));
			file1.setCharset(null, getMonitor());
			assertExistsInWorkspace("6.0", getProjectEncodingSettings(project));
			folder.setDefaultCharset(null, getMonitor());
			assertDoesNotExistInWorkspace("7.0", getProjectEncodingSettings(project));
		} finally {
			try {
				clearAllEncodings(project);
			} catch (CoreException e) {
				fail("99.9", e);
			}
		}
	}

	/**
	 * See enhancement request 60636.
	 */
	public void testGetCharsetFor() throws CoreException {
		IProject project = null;
		try {
			IContentTypeManager contentTypeManager = Platform.getContentTypeManager();
			//			IContentType text = contentTypeManager.getContentType("org.eclipse.core.runtime.text");
			IContentType xml = contentTypeManager.getContentType("org.eclipse.core.runtime.xml");

			IWorkspace workspace = getWorkspace();
			project = workspace.getRoot().getProject("MyProject");
			IFile oldFile = project.getFile("oldfile.xml");
			IFile newXMLFile = project.getFile("newfile.xml");
			IFile newTXTFile = project.getFile("newfile.txt");
			IFile newRandomFile = project.getFile("newFile." + (long) (Math.random() * (Long.MAX_VALUE)));
			ensureExistsInWorkspace(oldFile, SAMPLE_XML_DEFAULT_ENCODING);
			// sets project default charset
			project.setDefaultCharset("BAR", getMonitor());
			oldFile.setCharset("FOO", getMonitor());
			// project and non-existing file share the same encoding 
			assertCharsetIs("0.1", "BAR", new IResource[] {project, newXMLFile, newTXTFile, newRandomFile}, true);
			// existing file has encoding determined by user
			assertCharsetIs("0.2", "FOO", new IResource[] {oldFile}, true);

			// for an existing file, user set charset will prevail (if any)
			assertEquals("1.0", "FOO", oldFile.getCharsetFor(getTextContents("")));
			assertEquals("1.1", "FOO", oldFile.getCharsetFor(getTextContents(SAMPLE_XML_DEFAULT_ENCODING)));

			// for a non-existing file, or for a file that exists but does not have a user set charset,  the content type (if any) rules
			assertEquals("2.0", xml.getDefaultCharset(), newXMLFile.getCharsetFor(getTextContents("")));
			assertEquals("2.1", xml.getDefaultCharset(), newXMLFile.getCharsetFor(getTextContents(SAMPLE_XML_DEFAULT_ENCODING)));
			assertEquals("2.2", "US-ASCII", newXMLFile.getCharsetFor(getTextContents(SAMPLE_XML_US_ASCII_ENCODING)));
			oldFile.setCharset(null, getMonitor());
			assertEquals("2.3", xml.getDefaultCharset(), oldFile.getCharsetFor(getTextContents("")));
			assertEquals("2.4", xml.getDefaultCharset(), oldFile.getCharsetFor(getTextContents(SAMPLE_XML_DEFAULT_ENCODING)));
			assertEquals("2.5", "US-ASCII", oldFile.getCharsetFor(getTextContents(SAMPLE_XML_US_ASCII_ENCODING)));

			// if the content type has no default charset, or the file has no known content type, fallback to parent default charset
			assertEquals("3.0", project.getDefaultCharset(), newTXTFile.getCharsetFor(getTextContents("")));
			assertEquals("3.1", project.getDefaultCharset(), newRandomFile.getCharsetFor(getTextContents("")));
		} finally {
			try {
				clearAllEncodings(project);
			} catch (CoreException e) {
				fail("99.9", e);
			}
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
			project1.setDefaultCharset("FOO", getMonitor());
			folder.setDefaultCharset("BAR", getMonitor());

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
			try {
				clearAllEncodings(project1);
				clearAllEncodings(project2);
			} catch (CoreException e) {
				fail("99.9", e);
			}
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
				project.setDefaultCharset("FOO", getMonitor());
				fail("1.0");
			} catch (CoreException e) {
				// expected, project does not exist yet 
				assertEquals("1.1", IResourceStatus.RESOURCE_NOT_FOUND, e.getStatus().getCode());
			}
			ensureExistsInWorkspace(project, true);
			project.setDefaultCharset("FOO", getMonitor());
			IFile file = project.getFile("file.xml");
			assertDoesNotExistInWorkspace("2.0", file);
			assertEquals("2.2", "FOO", file.getCharset());
			try {
				file.setCharset("BAR", getMonitor());
				fail("2.4");
			} catch (CoreException e) {
				// expected, file does not exist yet
				assertEquals("2.6", IResourceStatus.RESOURCE_NOT_FOUND, e.getStatus().getCode());
			}
			ensureExistsInWorkspace(file, true);
			file.setCharset("BAR", getMonitor());
			assertEquals("2.8", "BAR", file.getCharset());
			file.delete(IResource.NONE, null);
			assertDoesNotExistInWorkspace("2.10", file);
			assertEquals("2.11", "FOO", file.getCharset());
		} finally {
			try {
				clearAllEncodings(project);
			} catch (CoreException e) {
				fail("99.9", e);
			}
		}
	}
}
