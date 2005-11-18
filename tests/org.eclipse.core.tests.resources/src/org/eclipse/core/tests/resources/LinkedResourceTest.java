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
package org.eclipse.core.tests.resources;

import java.io.*;
import java.net.URI;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.internal.resources.Workspace;
import org.eclipse.core.internal.utils.FileUtil;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.tests.harness.CancelingProgressMonitor;
import org.eclipse.core.tests.harness.FussyProgressMonitor;

/**
 * Tests the following API methods:
 *  IFile#createLink  			
 * 	IFolder#createLink
 * 
 * This test supports both variable-based and non-variable-based locations.
 * Although the method used for creating random locations
 * <code>ResourceTest#getRandomLocation()</code> never returns variable-
 * based paths, this method is overwritten in the derived class
 * <code>LinkedResourceWithPathVariable</code> to always return variable-based
 * paths.
 * 
 * To support variable-based paths wherever a file system location is used, it
 * is mandatory first to resolve it and only then using it, except in calls to
 * <code>IFile#createLink</code> and <code>IFolder#createLink</code> and when
 * the location is obtained using <code>IResource#getLocation()</code>.
 */
public class LinkedResourceTest extends ResourceTest {
	protected String childName = "File.txt";
	protected IProject closedProject;
	protected IFile existingFileInExistingProject;
	protected IFolder existingFolderInExistingFolder;
	protected IFolder existingFolderInExistingProject;
	protected IProject existingProject;
	protected IPath localFolder;
	protected IPath localFile;
	protected IFile nonExistingFileInExistingFolder;
	protected IFile nonExistingFileInExistingProject;
	protected IFile nonExistingFileInOtherExistingProject;
	protected IFolder nonExistingFolderInExistingFolder;
	protected IFolder nonExistingFolderInExistingProject;
	protected IFolder nonExistingFolderInNonExistingFolder;
	protected IFolder nonExistingFolderInNonExistingProject;
	protected IFolder nonExistingFolderInOtherExistingProject;
	protected IPath nonExistingLocation;
	protected IProject nonExistingProject;
	protected IProject otherExistingProject;

	public static Test suite() {
		return new TestSuite(LinkedResourceTest.class);
//		TestSuite suite = new TestSuite();
//		suite.addTest(new LinkedResourceTest("testRefreshDeepLink"));
//		return suite;
	}

	public LinkedResourceTest() {
		super();
	}

	public LinkedResourceTest(String name) {
		super(name);
	}

	protected void doCleanup() throws Exception {
		ensureExistsInWorkspace(new IResource[] {existingProject, otherExistingProject, closedProject, existingFolderInExistingProject, existingFolderInExistingFolder, existingFileInExistingProject}, true);
		closedProject.close(getMonitor());
		ensureDoesNotExistInWorkspace(new IResource[] {nonExistingProject, nonExistingFolderInExistingProject, nonExistingFolderInExistingFolder, nonExistingFolderInOtherExistingProject, nonExistingFolderInNonExistingProject, nonExistingFolderInNonExistingFolder, nonExistingFileInExistingProject, nonExistingFileInOtherExistingProject, nonExistingFileInExistingFolder});
		ensureDoesNotExistInFileSystem(resolve(nonExistingLocation).toFile());
		resolve(localFolder).toFile().mkdirs();
		createFileInFileSystem(resolve(localFile), getRandomContents());
	}

	private byte[] getFileContents(IFile file) throws CoreException {
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		transferData(new BufferedInputStream(file.getContents()), bout);
		return bout.toByteArray();
	}

	/**
	 * Maybe overridden in subclasses that use path variables.
	 */
	protected IPath resolve(IPath path) {
		return path;
	}

	/**
	 * Maybe overridden in subclasses that use path variables.
	 */
	protected URI resolve(URI uri) {
		return uri;
	}

	protected void setUp() throws Exception {
		super.setUp();
		existingProject = getWorkspace().getRoot().getProject("ExistingProject");
		otherExistingProject = getWorkspace().getRoot().getProject("OtherExistingProject");
		closedProject = getWorkspace().getRoot().getProject("ClosedProject");
		existingFolderInExistingProject = existingProject.getFolder("existingFolderInExistingProject");
		existingFolderInExistingFolder = existingFolderInExistingProject.getFolder("existingFolderInExistingFolder");
		nonExistingFolderInExistingProject = existingProject.getFolder("nonExistingFolderInExistingProject");
		nonExistingFolderInOtherExistingProject = otherExistingProject.getFolder("nonExistingFolderInOtherExistingProject");
		nonExistingFolderInNonExistingFolder = nonExistingFolderInExistingProject.getFolder("nonExistingFolderInNonExistingFolder");
		nonExistingFolderInExistingFolder = existingFolderInExistingProject.getFolder("nonExistingFolderInExistingFolder");

		nonExistingProject = getWorkspace().getRoot().getProject("NonProject");
		nonExistingFolderInNonExistingProject = nonExistingProject.getFolder("nonExistingFolderInNonExistingProject");

		existingFileInExistingProject = existingProject.getFile("existingFileInExistingProject");
		nonExistingFileInExistingProject = existingProject.getFile("nonExistingFileInExistingProject");
		nonExistingFileInOtherExistingProject = otherExistingProject.getFile("nonExistingFileInOtherExistingProject");
		nonExistingFileInExistingFolder = existingFolderInExistingProject.getFile("nonExistingFileInExistingFolder");
		localFolder = getRandomLocation();
		nonExistingLocation = getRandomLocation();
		localFile = localFolder.append(childName);
		doCleanup();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		Workspace.clear(resolve(localFolder).toFile());
		Workspace.clear(resolve(nonExistingLocation).toFile());
	}

	/**
	 * Tests creation of a linked resource whose corresponding file system
	 * path does not exist. This should succeed but no operations will be
	 * available on the resulting resource.
	 */
	public void testAllowMissingLocal() {
		//get a non-existing location
		IPath location = getRandomLocation();
		IFolder folder = nonExistingFolderInExistingProject;

		//try to create without the flag (should fail)
		try {
			folder.createLink(location, IResource.NONE, getMonitor());
			fail("1.0");
		} catch (CoreException e) {
			//should fail
		}

		//now try to create with the flag (should succeed)
		try {
			folder.createLink(location, IResource.ALLOW_MISSING_LOCAL, getMonitor());
		} catch (CoreException e) {
			fail("1.1", e);
		}
		assertEquals("1.2", resolve(location), folder.getLocation());
		assertTrue("1.3", !resolve(location).toFile().exists());
		//getting children should succeed (and be empty)
		try {
			assertEquals("1.4", 0, folder.members().length);
		} catch (CoreException e) {
			fail("1.5", e);
		}
		//delete should succeed
		try {
			folder.delete(IResource.NONE, getMonitor());
		} catch (CoreException e) {
			fail("1.6", e);
		}

		//try to create with local path that can never exist
		if (Platform.getOS().equals(Platform.OS_WIN32))
			location = new Path("b:\\does\\not\\exist");
		else
			location = new Path("/dev/null/does/not/exist");
		try {
			folder.createLink(location, IResource.NONE, getMonitor());
			fail("2.1");
		} catch (CoreException e) {
			//should fail
		}
		try {
			folder.createLink(location, IResource.ALLOW_MISSING_LOCAL, getMonitor());
		} catch (CoreException e) {
			fail("2.2", e);
		}
		assertEquals("2.3", location, folder.getLocation());
		assertTrue("2.4", !location.toFile().exists());
		// creating child should fail
		try {
			folder.getFile("abc.txt").create(getRandomContents(), IResource.NONE, getMonitor());
			fail("2.5");
		} catch (CoreException e) {
			//should fail
		}
	}

	/**
	 * Tests case where a resource in the file system cannot be added to the workspace
	 * because it is blocked by a linked resource of the same name.
	 */
	public void testBlockedFolder() {
		//create local folder that will be blocked
		ensureExistsInFileSystem(nonExistingFolderInExistingProject);
		IFile blockedFile = nonExistingFolderInExistingProject.getFile("BlockedFile");
		createFileInFileSystem(blockedFile.getLocation(), getRandomContents());
		try {
			//link the folder elsewhere
			nonExistingFolderInExistingProject.createLink(localFolder, IResource.NONE, getMonitor());
			//refresh the project
			existingProject.refreshLocal(IResource.DEPTH_INFINITE, getMonitor());
		} catch (CoreException e) {
			fail("1.1", e);
		}

		//the blocked file should not exist in the workspace
		assertTrue("1.2", !blockedFile.exists());
		assertTrue("1.3", nonExistingFolderInExistingProject.exists());
		assertTrue("1.4", nonExistingFolderInExistingProject.getFile(childName).exists());
		assertEquals("1.5", nonExistingFolderInExistingProject.getLocation(), resolve(localFolder));

		//now delete the link
		try {
			nonExistingFolderInExistingProject.delete(IResource.NONE, getMonitor());
		} catch (CoreException e) {
			fail("1.99", e);
		}
		//the blocked file and the linked folder should not exist in the workspace
		assertTrue("2.0", !blockedFile.exists());
		assertTrue("2.1", !nonExistingFolderInExistingProject.exists());
		assertTrue("2.2", !nonExistingFolderInExistingProject.getFile(childName).exists());
		assertEquals("2.3", nonExistingFolderInExistingProject.getLocation(), existingProject.getLocation().append(nonExistingFolderInExistingProject.getName()));

		//now refresh again to discover the blocked resource
		try {
			existingProject.refreshLocal(IResource.DEPTH_INFINITE, getMonitor());
		} catch (CoreException e) {
			fail("2.99", e);
		}

		//the blocked file should now exist
		assertTrue("3.0", blockedFile.exists());
		assertTrue("3.1", nonExistingFolderInExistingProject.exists());
		assertTrue("3.2", !nonExistingFolderInExistingProject.getFile(childName).exists());
		assertEquals("3.3", nonExistingFolderInExistingProject.getLocation(), existingProject.getLocation().append(nonExistingFolderInExistingProject.getName()));

		//attempting to link again will fail because the folder exists in the workspace
		try {
			nonExistingFolderInExistingProject.createLink(localFolder, IResource.NONE, getMonitor());
			fail("3.4");
		} catch (CoreException e) {
			//expected
		}
	}

	/**
	 * This test creates a linked folder resource, then changes the directory in
	 * the file system to be a file.  On refresh, the linked resource should
	 * still exist, should have the correct gender, and still be a linked
	 * resource.
	 */
	public void testChangeLinkGender() {
		IFolder folder = nonExistingFolderInExistingProject;
		IFile file = folder.getProject().getFile(folder.getProjectRelativePath());
		IPath resolvedLocation = resolve(localFolder);
		try {
			folder.createLink(localFolder, IResource.NONE, getMonitor());
		} catch (CoreException e) {
			fail("0.99", e);
		}
		ensureDoesNotExistInFileSystem(resolvedLocation.toFile());
		createFileInFileSystem(resolvedLocation, getRandomContents());
		try {
			folder.refreshLocal(IResource.DEPTH_INFINITE, getMonitor());
		} catch (CoreException e) {
			fail("2.99", e);
		}

		assertTrue("3.0", !folder.exists());
		assertTrue("3.1", file.exists());
		assertTrue("3.2", file.isLinked());
		assertEquals("3.3", resolvedLocation, file.getLocation());

		//change back to folder
		ensureDoesNotExistInFileSystem(resolvedLocation.toFile());
		resolvedLocation.toFile().mkdirs();

		try {
			folder.refreshLocal(IResource.DEPTH_INFINITE, getMonitor());
		} catch (CoreException e) {
			fail("3.99", e);
		}
		assertTrue("4.0", folder.exists());
		assertTrue("4.1", !file.exists());
		assertTrue("4.2", folder.isLinked());
		assertEquals("4.3", resolvedLocation, folder.getLocation());
	}

	public void testCopyFile() {
		IResource[] sources = new IResource[] {nonExistingFileInExistingProject, nonExistingFileInExistingFolder};
		IResource[] destinationResources = new IResource[] {existingProject, closedProject, nonExistingFileInOtherExistingProject, nonExistingFileInExistingFolder};
		Boolean[] deepCopy = new Boolean[] {Boolean.TRUE, Boolean.FALSE};
		IProgressMonitor[] monitors = new IProgressMonitor[] {new FussyProgressMonitor(), new CancelingProgressMonitor(), null};
		Object[][] inputs = new Object[][] {sources, destinationResources, deepCopy, monitors};
		new TestPerformer("LinkedResourceTest.testCopyFile") {
			protected static final String CANCELED = "canceled";

			public void cleanUp(Object[] args, int count) {
				super.cleanUp(args, count);
				try {
					doCleanup();
				} catch (Exception e) {
					fail("invocation " + count + " failed to cleanup", e);
				}
			}

			public Object invokeMethod(Object[] args, int count) throws Exception {
				IFile source = (IFile) args[0];
				IResource destination = (IResource) args[1];
				boolean isDeep = ((Boolean) args[2]).booleanValue();
				IProgressMonitor monitor = (IProgressMonitor) args[3];
				if (monitor instanceof FussyProgressMonitor)
					((FussyProgressMonitor) monitor).prepare();
				try {
					source.createLink(localFile, IResource.NONE, null);
					source.copy(destination.getFullPath(), isDeep ? IResource.NONE : IResource.SHALLOW, monitor);
				} catch (OperationCanceledException e) {
					return CANCELED;
				}
				if (monitor instanceof FussyProgressMonitor)
					((FussyProgressMonitor) monitor).sanityCheck();
				return null;
			}

			public boolean shouldFail(Object[] args, int count) {
				IFile source = (IFile) args[0];
				IResource destination = (IResource) args[1];
				boolean isDeep = ((Boolean) args[2]).booleanValue();
				IProgressMonitor monitor = (IProgressMonitor) args[3];
				if (monitor instanceof CancelingProgressMonitor)
					return false;
				if (source.equals(destination))
					return true;
				IResource parent = destination.getParent();
				if (!isDeep && parent == null)
					return true;
				if (!parent.isAccessible())
					return true;
				if (destination.exists())
					return true;
				//passed all failure cases so it should succeed
				return false;
			}

			public boolean wasSuccess(Object[] args, Object result, Object[] oldState) throws Exception {
				IFile source = (IFile) args[0];
				IResource destination = (IResource) args[1];
				boolean isDeep = ((Boolean) args[2]).booleanValue();
				IProgressMonitor monitor = (IProgressMonitor) args[3];
				if (result == CANCELED)
					return monitor instanceof CancelingProgressMonitor;
				if (!destination.exists())
					return false;
				//destination should only be linked for a shallow copy
				if (isDeep) {
					if (destination.isLinked())
						return false;
					if (source.getLocation().equals(destination.getLocation()))
						return false;
					if (!destination.getProject().getLocation().isPrefixOf(destination.getLocation()))
						return false;
				} else {
					if (!destination.isLinked())
						return false;
					if (!source.getLocation().equals(destination.getLocation()))
						return false;
					if (!source.getRawLocation().equals(destination.getRawLocation()))
						return false;
					if (!source.getLocationURI().equals(destination.getLocationURI()))
						return false;
				}
				return true;
			}
		}.performTest(inputs);
	}

	public void testCopyFolder() {
		IFolder[] sources = new IFolder[] {nonExistingFolderInExistingProject, nonExistingFolderInExistingFolder};
		IResource[] destinations = new IResource[] {existingProject, closedProject, nonExistingProject, existingFolderInExistingProject, nonExistingFolderInOtherExistingProject, nonExistingFolderInExistingFolder};
		Boolean[] deepCopy = new Boolean[] {Boolean.TRUE, Boolean.FALSE};
		IProgressMonitor[] monitors = new IProgressMonitor[] {new FussyProgressMonitor(), new CancelingProgressMonitor(), null};
		Object[][] inputs = new Object[][] {sources, destinations, deepCopy, monitors};
		new TestPerformer("LinkedResourceTest.testCopyFolder") {
			protected static final String CANCELED = "canceled";

			public void cleanUp(Object[] args, int count) {
				super.cleanUp(args, count);
				try {
					doCleanup();
				} catch (Exception e) {
					fail("invocation " + count + " failed to cleanup", e);
				}
			}

			public Object invokeMethod(Object[] args, int count) throws Exception {
				IFolder source = (IFolder) args[0];
				IResource destination = (IResource) args[1];
				boolean isDeep = ((Boolean) args[2]).booleanValue();
				IProgressMonitor monitor = (IProgressMonitor) args[3];
				if (monitor instanceof FussyProgressMonitor)
					((FussyProgressMonitor) monitor).prepare();
				try {
					source.createLink(localFolder, IResource.NONE, null);
					source.copy(destination.getFullPath(), isDeep ? IResource.NONE : IResource.SHALLOW, monitor);
				} catch (OperationCanceledException e) {
					return CANCELED;
				}
				if (monitor instanceof FussyProgressMonitor)
					((FussyProgressMonitor) monitor).sanityCheck();
				return null;
			}

			public boolean shouldFail(Object[] args, int count) {
				IFolder source = (IFolder) args[0];
				IResource destination = (IResource) args[1];
				boolean isDeep = ((Boolean) args[2]).booleanValue();
				IProgressMonitor monitor = (IProgressMonitor) args[3];
				if (monitor instanceof CancelingProgressMonitor)
					return false;
				IResource parent = destination.getParent();
				if (destination.getType() == IResource.PROJECT)
					return true;
				if (source.equals(destination))
					return true;
				if (!isDeep && parent == null)
					return true;
				if (!parent.isAccessible())
					return true;
				if (destination.exists())
					return true;
				//passed all failure case so it should succeed
				return false;
			}

			public boolean wasSuccess(Object[] args, Object result, Object[] oldState) throws Exception {
				IFolder source = (IFolder) args[0];
				IResource destination = (IResource) args[1];
				boolean isDeep = ((Boolean) args[2]).booleanValue();
				IProgressMonitor monitor = (IProgressMonitor) args[3];
				if (result == CANCELED)
					return monitor instanceof CancelingProgressMonitor;
				if (!destination.exists())
					return false;
				//destination should only be linked for a shallow copy
				if (isDeep) {
					if (destination.isLinked())
						return false;
					if (source.getLocation().equals(destination.getLocation()))
						return false;
					if (!destination.getProject().getLocation().isPrefixOf(destination.getLocation()))
						return false;
				} else {
					if (!destination.isLinked())
						return false;
					if (!source.getLocation().equals(destination.getLocation()))
						return false;
					if (!source.getLocationURI().equals(destination.getLocationURI()))
						return false;
					if (!source.getRawLocation().equals(destination.getRawLocation()))
						return false;
				}
				return true;
			}
		}.performTest(inputs);
	}

	/**
	 * Tests copying a linked file resource that doesn't exist in the file system
	 */
	public void testCopyMissingFile() {
		IPath location = getRandomLocation();
		IFile linkedFile = nonExistingFileInExistingProject;
		try {
			linkedFile.createLink(location, IResource.ALLOW_MISSING_LOCAL, getMonitor());
		} catch (CoreException e) {
			fail("1.99", e);
		}

		IFile dest = existingProject.getFile("FailedCopyDest");
		try {
			linkedFile.copy(dest.getFullPath(), IResource.NONE, getMonitor());
			fail("2.0");
		} catch (CoreException e1) {
			//should fail
		}
		assertTrue("2.1", !dest.exists());
		try {
			linkedFile.copy(dest.getFullPath(), IResource.FORCE, getMonitor());
			fail("2.2");
		} catch (CoreException e1) {
			//should fail
		}
		assertTrue("2.3", !dest.exists());
	}

	/**
	 * Tests copying a linked folder that doesn't exist in the file system
	 */
	public void testCopyMissingFolder() {
		IPath location = getRandomLocation();
		IFolder linkedFolder = nonExistingFolderInExistingProject;
		try {
			linkedFolder.createLink(location, IResource.ALLOW_MISSING_LOCAL, getMonitor());
		} catch (CoreException e) {
			fail("1.99", e);
		}

		IFolder dest = existingProject.getFolder("FailedCopyDest");
		try {
			linkedFolder.copy(dest.getFullPath(), IResource.NONE, getMonitor());
			fail("2.0");
		} catch (CoreException e1) {
			//should fail
		}
		assertTrue("2.1", !dest.exists());
		try {
			linkedFolder.copy(dest.getFullPath(), IResource.FORCE, getMonitor());
			fail("2.2");
		} catch (CoreException e1) {
			//should fail
		}
		assertTrue("2.3", !dest.exists());
	}

	public void testCopyProjectWithLinks() {
		IPath fileLocation = getRandomLocation();
		IFile linkedFile = nonExistingFileInExistingProject;
		IFolder linkedFolder = nonExistingFolderInExistingProject;
		try {
			try {
				createFileInFileSystem(resolve(fileLocation), getRandomContents());
				linkedFolder.createLink(localFolder, IResource.NONE, getMonitor());
				linkedFile.createLink(fileLocation, IResource.NONE, getMonitor());
			} catch (CoreException e) {
				fail("1.0", e);
			}

			//copy the project
			IProject destination = getWorkspace().getRoot().getProject("CopyTargetProject");
			try {
				existingProject.copy(destination.getFullPath(), IResource.SHALLOW, getMonitor());
			} catch (CoreException e) {
				fail("2.0", e);
			}

			IFile newFile = destination.getFile(linkedFile.getProjectRelativePath());
			assertTrue("3.0", newFile.isLinked());
			assertEquals("3.1", linkedFile.getLocation(), newFile.getLocation());

			IFolder newFolder = destination.getFolder(linkedFolder.getProjectRelativePath());
			assertTrue("4.0", newFolder.isLinked());
			assertEquals("4.1", linkedFolder.getLocation(), newFolder.getLocation());

			//test project deep copy
			try {
				destination.delete(IResource.NONE, getMonitor());
				existingProject.copy(destination.getFullPath(), IResource.NONE, getMonitor());
			} catch (CoreException e) {
				fail("5.0", e);
			}
			assertTrue("5.1", !newFile.isLinked());
			assertEquals("5.2", destination.getLocation().append(newFile.getProjectRelativePath()), newFile.getLocation());
			assertTrue("5.3", !newFolder.isLinked());
			assertEquals("5.4", destination.getLocation().append(newFolder.getProjectRelativePath()), newFolder.getLocation());

			//test copy project when linked resources don't exist with force=false
			try {
				destination.delete(IResource.NONE, getMonitor());
			} catch (CoreException e) {
				fail("5.99", e);
			}
			assertTrue("6.0", resolve(fileLocation).toFile().delete());

			try {
				existingProject.copy(destination.getFullPath(), IResource.NONE, getMonitor());
				fail("6.1");
			} catch (CoreException e) {
				//should fail
			}
			//all members except the missing link should have been copied
			assertTrue("6.2", destination.exists());
			assertTrue("6.2.1", !destination.getFile(linkedFile.getName()).exists());
			try {
				IResource[] srcChildren = existingProject.members();
				for (int i = 0; i < srcChildren.length; i++) {
					if (!srcChildren[i].equals(linkedFile))
						assertNotNull("6.3." + i, destination.findMember(srcChildren[i].getProjectRelativePath()));
				}
			} catch (CoreException e) {
				fail("6.4", e);
			}
			//test copy project when linked resources don't exist with force=true
			//this should mostly succeed, but still throw an exception indicating
			//a resource could not be copied because its location was missing
			try {
				destination.delete(IResource.NONE, getMonitor());
			} catch (CoreException e) {
				fail("6.5", e);
			}
			try {
				existingProject.copy(destination.getFullPath(), IResource.FORCE, getMonitor());
				fail("6.6");
			} catch (CoreException e) {
				//should fail
			}
			assertTrue("6.7", destination.exists());
			assertTrue("6.7.1", !destination.getFile(linkedFile.getName()).exists());
			//all members except the missing link should have been copied
			try {
				IResource[] srcChildren = existingProject.members();
				for (int i = 0; i < srcChildren.length; i++) {
					if (!srcChildren[i].equals(linkedFile))
						assertNotNull("6.8." + i, destination.findMember(srcChildren[i].getProjectRelativePath()));
				}
			} catch (CoreException e) {
				fail("6.99", e);
			}
		} finally {
			Workspace.clear(resolve(fileLocation).toFile());
		}
	}

	/**
	 * Tests creating a linked resource by modifying the .project file directly.
	 * This is a regression test for bug 63331.
	 */
	public void testCreateLinkInDotProject() {
		final IFile dotProject = existingProject.getFile(IProjectDescription.DESCRIPTION_FILE_NAME);
		IFile link = nonExistingFileInExistingProject;
		byte[] oldContents = null;
		try {
			//create a linked file
			link.createLink(localFile, IResource.NONE, getMonitor());
			//copy the .project file contents
			oldContents = getFileContents(dotProject);
			//delete linked file
			link.delete(IResource.NONE, getMonitor());
		} catch (CoreException e) {
			fail("1.99", e);
		}
		final byte[] finalContents = oldContents;
		try {
			//recreate the link in a workspace runnable with create scheduling rule
			getWorkspace().run(new IWorkspaceRunnable() {
				public void run(IProgressMonitor monitor) throws CoreException {
					dotProject.setContents(new ByteArrayInputStream(finalContents), IResource.NONE, getMonitor());
				}
			}, getWorkspace().getRuleFactory().modifyRule(dotProject), IResource.NONE, getMonitor());
		} catch (CoreException e1) {
			fail("2.99", e1);
		}
	}

	public void testDeepMoveProjectWithLinks() {
		IPath fileLocation = getRandomLocation();
		IFile file = nonExistingFileInExistingProject;
		IFolder folder = nonExistingFolderInExistingProject;
		IFile childFile = folder.getFile(childName);
		IResource[] oldResources = new IResource[] {file, folder, existingProject, childFile};
		try {
			try {
				createFileInFileSystem(resolve(fileLocation));
				folder.createLink(localFolder, IResource.NONE, getMonitor());
				file.createLink(fileLocation, IResource.NONE, getMonitor());
			} catch (CoreException e) {
				fail("1.0", e);
			}

			//move the project
			IProject destination = getWorkspace().getRoot().getProject("MoveTargetProject");
			IFile newFile = destination.getFile(file.getProjectRelativePath());
			IFolder newFolder = destination.getFolder(folder.getProjectRelativePath());
			IFile newChildFile = newFolder.getFile(childName);
			IResource[] newResources = new IResource[] {destination, newFile, newFolder, newChildFile};

			assertDoesNotExistInWorkspace("2.0", destination);

			try {
				existingProject.move(destination.getFullPath(), IResource.NONE, getMonitor());
			} catch (CoreException e) {
				fail("2.1", e);
			}
			assertExistsInWorkspace("3.0", newResources);
			assertDoesNotExistInWorkspace("3.1", oldResources);
			assertTrue("3.2", existingProject.isSynchronized(IResource.DEPTH_INFINITE));
			assertTrue("3.3", destination.isSynchronized(IResource.DEPTH_INFINITE));

			assertTrue("3.4", !newFile.isLinked());
			assertEquals("3.5", destination.getLocation().append(newFile.getProjectRelativePath()), newFile.getLocation());

			assertTrue("3.6", !newFolder.isLinked());
			assertEquals("3.7", destination.getLocation().append(newFolder.getProjectRelativePath()), newFolder.getLocation());

			assertTrue("3.8", destination.isSynchronized(IResource.DEPTH_INFINITE));
		} finally {
			Workspace.clear(resolve(fileLocation).toFile());
		}
	}

	/**
	 * Tests deleting and then recreating a project
	 */
	public void testDeleteProjectWithLinks() {
		IFolder link = nonExistingFolderInExistingProject;
		try {
			link.createLink(localFolder, IResource.NONE, getMonitor());
			existingProject.delete(IResource.NEVER_DELETE_PROJECT_CONTENT, getMonitor());
			existingProject.create(getMonitor());
		} catch (CoreException e) {
			fail("0.99", e);
		}

		//link should not exist until the project is open
		assertTrue("1.0", !link.exists());

		try {
			existingProject.open(getMonitor());
		} catch (CoreException e) {
			fail("1.99", e);
		}

		//link should now exist
		assertTrue("2.0", link.exists());
		assertTrue("2.1", link.isLinked());
		assertEquals("2.2", resolve(localFolder), link.getLocation());
	}

	/**
	 * Tests creating a linked resource whose location contains a colon character.
	 */
	public void testLocationWithColon() {
		//windows does not allow a location with colon in the name
		if (Platform.getOS().equals(Platform.OS_WIN32))
			return;
		IFolder folder = nonExistingFolderInExistingProject;
		try {
			//Note that on *nix, "c:/temp" is a relative path with two segments
			//so this is treated as relative to an undefined path variable called "c:".
			IPath location = new Path("c:/temp");
			folder.createLink(location, IResource.ALLOW_MISSING_LOCAL, getMonitor());
			assertEquals("1.0", location, folder.getRawLocation());
		} catch (CoreException e) {
			fail("1.99", e);
		}
	}

	/**
	 * Specific testing of links within links.
	 */
	public void testLinkedFileInLinkedFolder() {
		//setup handles
		IProject project = existingProject;
		IFolder top = project.getFolder("topFolder");
		IFolder linkedFolder = top.getFolder("linkedFolder");
		IFolder subFolder = linkedFolder.getFolder("subFolder");
		IFile linkedFile = subFolder.getFile("Link.txt");
		IFileStore folderStore = getTempStore();
		IFileStore subFolderStore = folderStore.getChild(subFolder.getName());
		IFileStore fileStore = getTempStore();
		IPath folderPath = FileUtil.toPath(folderStore.toURI());
		IPath filePath = FileUtil.toPath(fileStore.toURI());

		try {
			//create the structure on disk
			subFolderStore.mkdir(EFS.NONE, getMonitor());
			fileStore.openOutputStream(EFS.NONE, getMonitor()).close();

			//create the structure in the workspace
			ensureExistsInWorkspace(top, true);
			linkedFolder.createLink(folderStore.toURI(), IResource.NONE, getMonitor());
			linkedFile.createLink(fileStore.toURI(), IResource.NONE, getMonitor());
		} catch (CoreException e) {
			fail("4.99", e);
		} catch (IOException e) {
			fail("4.99", e);
		}

		//assert locations
		assertEquals("1.0", folderPath, linkedFolder.getLocation());
		assertEquals("1.1", folderPath.append(subFolder.getName()), subFolder.getLocation());
		assertEquals("1.2", filePath, linkedFile.getLocation());
		//assert URIs
		assertEquals("1.0", folderStore.toURI(), linkedFolder.getLocationURI());
		assertEquals("1.1", subFolderStore.toURI(), subFolder.getLocationURI());
		assertEquals("1.2", fileStore.toURI(), linkedFile.getLocationURI());

	}

	/**
	 * Automated test of IFile#createLink
	 */
	public void testLinkFile() {
		IResource[] interestingResources = new IResource[] {existingFileInExistingProject, nonExistingFileInExistingProject, nonExistingFileInExistingFolder};
		IPath[] interestingLocations = new IPath[] {localFile, localFolder, nonExistingLocation};
		IProgressMonitor[] monitors = new IProgressMonitor[] {new FussyProgressMonitor(), new CancelingProgressMonitor(), null};
		Object[][] inputs = new Object[][] {interestingResources, interestingLocations, monitors};
		new TestPerformer("LinkedResourceTest.testLinkFile") {
			protected static final String CANCELED = "canceled";

			public void cleanUp(Object[] args, int count) {
				super.cleanUp(args, count);
				try {
					doCleanup();
				} catch (Exception e) {
					fail("invocation " + count + " failed to cleanup", e);
				}
			}

			public Object invokeMethod(Object[] args, int count) throws Exception {
				IFile file = (IFile) args[0];
				IPath location = (IPath) args[1];
				IProgressMonitor monitor = (IProgressMonitor) args[2];
				if (monitor instanceof FussyProgressMonitor)
					((FussyProgressMonitor) monitor).prepare();
				try {
					file.createLink(location, IResource.NONE, monitor);
				} catch (OperationCanceledException e) {
					return CANCELED;
				}
				if (monitor instanceof FussyProgressMonitor)
					((FussyProgressMonitor) monitor).sanityCheck();
				return null;
			}

			public boolean shouldFail(Object[] args, int count) {
				IResource resource = (IResource) args[0];
				IPath location = (IPath) args[1];
				IProgressMonitor monitor = (IProgressMonitor) args[2];
				if (monitor instanceof CancelingProgressMonitor)
					return false;
				//This resource already exists in the workspace
				if (resource.exists())
					return true;
				IPath resolvedLocation = resolve(location);
				//The corresponding location in the local file system does not exist.
				if (!resolvedLocation.toFile().exists())
					return true;
				//The workspace contains a resource of a different type at the same path as this resource
				if (getWorkspace().getRoot().findMember(resource.getFullPath()) != null)
					return true;
				//The parent of this resource does not exist.
				if (!resource.getParent().isAccessible())
					return true;
				//The name of this resource is not valid (according to IWorkspace.validateName)
				if (!getWorkspace().validateName(resource.getName(), IResource.FOLDER).isOK())
					return true;
				//The corresponding location in the local file system is occupied by a directory (as opposed to a file)
				if (resolvedLocation.toFile().isDirectory())
					return true;
				//passed all failure case so it should succeed
				return false;
			}

			public boolean wasSuccess(Object[] args, Object result, Object[] oldState) throws Exception {
				IFile resource = (IFile) args[0];
				IPath location = (IPath) args[1];
				IProgressMonitor monitor = (IProgressMonitor) args[2];
				if (result == CANCELED)
					return monitor instanceof CancelingProgressMonitor;
				IPath resolvedLocation = resolve(location);
				if (!resource.exists() || !resolvedLocation.toFile().exists())
					return false;
				if (!resource.getLocation().equals(resolvedLocation))
					return false;
				if (!resource.isSynchronized(IResource.DEPTH_INFINITE))
					return false;
				return true;
			}
		}.performTest(inputs);
	}

	/**
	 * Automated test of IFolder#createLink
	 */
	public void testLinkFolder() {
		IResource[] interestingResources = new IResource[] {existingFolderInExistingProject, existingFolderInExistingFolder, nonExistingFolderInExistingProject, nonExistingFolderInNonExistingProject, nonExistingFolderInNonExistingFolder, nonExistingFolderInExistingFolder};
		IPath[] interestingLocations = new IPath[] {localFile, localFolder, nonExistingLocation};
		IProgressMonitor[] monitors = new IProgressMonitor[] {new FussyProgressMonitor(), new CancelingProgressMonitor(), null};
		Object[][] inputs = new Object[][] {interestingResources, interestingLocations, monitors};
		new TestPerformer("LinkedResourceTest.testLinkFolder") {
			protected static final String CANCELED = "canceled";

			public void cleanUp(Object[] args, int count) {
				super.cleanUp(args, count);
				try {
					doCleanup();
				} catch (Exception e) {
					fail("invocation " + count + " failed to cleanup", e);
				}
			}

			public Object invokeMethod(Object[] args, int count) throws Exception {
				IFolder folder = (IFolder) args[0];
				IPath location = (IPath) args[1];
				IProgressMonitor monitor = (IProgressMonitor) args[2];
				if (monitor instanceof FussyProgressMonitor)
					((FussyProgressMonitor) monitor).prepare();
				try {
					folder.createLink(location, IResource.NONE, monitor);
				} catch (OperationCanceledException e) {
					return CANCELED;
				}
				if (monitor instanceof FussyProgressMonitor)
					((FussyProgressMonitor) monitor).sanityCheck();
				return null;
			}

			public boolean shouldFail(Object[] args, int count) {
				IResource resource = (IResource) args[0];
				IPath location = (IPath) args[1];
				IProgressMonitor monitor = (IProgressMonitor) args[2];
				if (monitor instanceof CancelingProgressMonitor)
					return false;
				//This resource already exists in the workspace
				if (resource.exists())
					return true;
				//The corresponding location in the local file system does not exist.
				if (!resolve(location).toFile().exists())
					return true;
				//The workspace contains a resource of a different type at the same path as this resource
				if (getWorkspace().getRoot().findMember(resource.getFullPath()) != null)
					return true;
				//The parent of this resource does not exist.
				if (!resource.getParent().isAccessible())
					return true;
				//The name of this resource is not valid (according to IWorkspace.validateName)
				if (!getWorkspace().validateName(resource.getName(), IResource.FOLDER).isOK())
					return true;
				//The corresponding location in the local file system is occupied by a file (as opposed to a directory)
				if (resolve(location).toFile().isFile())
					return true;
				//passed all failure case so it should succeed
				return false;
			}

			public boolean wasSuccess(Object[] args, Object result, Object[] oldState) throws Exception {
				IFolder resource = (IFolder) args[0];
				IPath location = (IPath) args[1];
				IProgressMonitor monitor = (IProgressMonitor) args[2];
				if (result == CANCELED)
					return monitor instanceof CancelingProgressMonitor;
				IPath resolvedLocation = resolve(location);
				if (!resource.exists() || !resolvedLocation.toFile().exists())
					return false;
				if (!resource.getLocation().equals(resolvedLocation))
					return false;
				//ensure child exists
				if (!resource.getFile(childName).exists())
					return false;
				return true;
			}
		}.performTest(inputs);

	}

	/**
	 * Tests the timestamp of a linked file when the local file is created or
	 * deleted. See bug 34150 for more details.
	 */
	public void testModificationStamp() {
		IPath location = getRandomLocation();
		IFile linkedFile = nonExistingFileInExistingProject;
		try {
			try {
				linkedFile.createLink(location, IResource.ALLOW_MISSING_LOCAL, getMonitor());
			} catch (CoreException e) {
				fail("1.99", e);
			}
			assertEquals("1.0", IResource.NULL_STAMP, linkedFile.getModificationStamp());
			//create local file
			try {
				resolve(location).toFile().createNewFile();
				linkedFile.refreshLocal(IResource.DEPTH_INFINITE, getMonitor());
			} catch (CoreException e) {
				fail("2.91", e);
			} catch (IOException e) {
				fail("2.92", e);
			}
			assertTrue("2.0", linkedFile.getModificationStamp() >= 0);

			//delete local file
			ensureDoesNotExistInFileSystem(resolve(location).toFile());
			try {
				linkedFile.refreshLocal(IResource.DEPTH_INFINITE, getMonitor());
			} catch (CoreException e) {
				fail("3.99", e);
			}
			assertEquals("4.0", IResource.NULL_STAMP, linkedFile.getModificationStamp());
		} finally {
			Workspace.clear(resolve(location).toFile());
		}
	}

	public void testMoveFile() {
		IResource[] sources = new IResource[] {nonExistingFileInExistingProject, nonExistingFileInExistingFolder};
		IResource[] destinations = new IResource[] {existingProject, closedProject, nonExistingFileInOtherExistingProject, nonExistingFileInExistingFolder};
		Boolean[] deepCopy = new Boolean[] {Boolean.TRUE, Boolean.FALSE};
		IProgressMonitor[] monitors = new IProgressMonitor[] {new FussyProgressMonitor(), new CancelingProgressMonitor(), null};
		Object[][] inputs = new Object[][] {sources, destinations, deepCopy, monitors};
		new TestPerformer("LinkedResourceTest.testMoveFile") {
			protected static final String CANCELED = "canceled";

			public void cleanUp(Object[] args, int count) {
				super.cleanUp(args, count);
				try {
					doCleanup();
				} catch (Exception e) {
					fail("invocation " + count + " failed to cleanup", e);
				}
			}
			
			public Object invokeMethod(Object[] args, int count) throws Exception {
				IFile source = (IFile) args[0];
				IResource destination = (IResource) args[1];
				boolean isDeep = ((Boolean) args[2]).booleanValue();
				IProgressMonitor monitor = (IProgressMonitor) args[3];
				if (monitor instanceof FussyProgressMonitor)
					((FussyProgressMonitor) monitor).prepare();
				try {
					source.createLink(localFile, IResource.NONE, null);
					source.move(destination.getFullPath(), isDeep ? IResource.NONE : IResource.SHALLOW, monitor);
				} catch (OperationCanceledException e) {
					return CANCELED;
				}
				if (monitor instanceof FussyProgressMonitor)
					((FussyProgressMonitor) monitor).sanityCheck();
				return null;
			}

			public boolean shouldFail(Object[] args, int count) {
				IFile source = (IFile) args[0];
				IResource destination = (IResource) args[1];
				boolean isDeep = ((Boolean) args[2]).booleanValue();
				IProgressMonitor monitor = (IProgressMonitor) args[3];
				if (monitor instanceof CancelingProgressMonitor)
					return false;
				IResource parent = destination.getParent();
				if (!isDeep && parent == null)
					return true;
				if (!parent.isAccessible())
					return true;
				if (source.equals(destination))
					return true;
				if (source.getType() != destination.getType())
					return true;
				if (destination.exists())
					return true;
				//passed all failure case so it should succeed
				return false;
			}

			public boolean wasSuccess(Object[] args, Object result, Object[] oldState) throws Exception {
				IResource destination = (IResource) args[1];
				boolean isDeep = ((Boolean) args[2]).booleanValue();
				IProgressMonitor monitor = (IProgressMonitor) args[3];
				IPath sourceLocation = resolve(localFile);
				URI sourceLocationURI = FileUtil.toURI(sourceLocation);
				if (result == CANCELED)
					return monitor instanceof CancelingProgressMonitor;
				if (!destination.exists())
					return false;
				//destination should only be linked for a shallow move
				if (isDeep) {
					if (destination.isLinked())
						return false;
					if (resolve(localFile).equals(destination.getLocation()))
						return false;
					if (!destination.getProject().getLocation().isPrefixOf(destination.getLocation()))
						return false;
				} else {
					if (!destination.isLinked())
						return false;
					if (!sourceLocation.equals(destination.getLocation()))
						return false;
					if (!sourceLocationURI.equals(destination.getLocationURI()))
						return false;
				}
				return true;
			}
		}.performTest(inputs);
	}

	public void testMoveFolder() {
		IResource[] sourceResources = new IResource[] {nonExistingFolderInExistingProject, nonExistingFolderInExistingFolder};
		IResource[] destinationResources = new IResource[] {existingProject, closedProject, nonExistingProject, existingFolderInExistingProject, nonExistingFolderInOtherExistingProject, nonExistingFolderInExistingFolder};
		IProgressMonitor[] monitors = new IProgressMonitor[] {new FussyProgressMonitor(), new CancelingProgressMonitor(), null};
		Object[][] inputs = new Object[][] {sourceResources, destinationResources, monitors};
		new TestPerformer("LinkedResourceTest.testMoveFolder") {
			protected static final String CANCELED = "canceled";

			public void cleanUp(Object[] args, int count) {
				super.cleanUp(args, count);
				try {
					doCleanup();
				} catch (Exception e) {
					fail("invocation " + count + " failed to cleanup", e);
				}
			}

			public Object invokeMethod(Object[] args, int count) throws Exception {
				IFolder source = (IFolder) args[0];
				IResource destination = (IResource) args[1];
				IProgressMonitor monitor = (IProgressMonitor) args[2];
				if (monitor instanceof FussyProgressMonitor)
					((FussyProgressMonitor) monitor).prepare();
				try {
					source.createLink(localFolder, IResource.NONE, null);
					source.move(destination.getFullPath(), IResource.SHALLOW, monitor);
				} catch (OperationCanceledException e) {
					return CANCELED;
				}
				if (monitor instanceof FussyProgressMonitor)
					((FussyProgressMonitor) monitor).sanityCheck();
				return null;
			}

			public boolean shouldFail(Object[] args, int count) {
				IFolder source = (IFolder) args[0];
				IResource destination = (IResource) args[1];
				IProgressMonitor monitor = (IProgressMonitor) args[2];
				if (monitor instanceof CancelingProgressMonitor)
					return false;
				IResource parent = destination.getParent();
				if (parent == null)
					return true;
				if (source.equals(destination))
					return true;
				if (source.getType() != destination.getType())
					return true;
				if (!parent.isAccessible())
					return true;
				if (destination.exists())
					return true;
				//passed all failure case so it should succeed
				return false;
			}

			public boolean wasSuccess(Object[] args, Object result, Object[] oldState) throws Exception {
				IResource destination = (IResource) args[1];
				IProgressMonitor monitor = (IProgressMonitor) args[2];
				if (result == CANCELED)
					return monitor instanceof CancelingProgressMonitor;
				if (!destination.exists())
					return false;
				if (!destination.isLinked())
					return false;
				if (!resolve(localFolder).equals(destination.getLocation()))
					return false;
				return true;
			}
		}.performTest(inputs);
	}

	/**
	 * Tests moving a linked file resource that doesn't exist in the file system
	 */
	public void testMoveMissingFile() {
		IPath location = getRandomLocation();
		IFile linkedFile = nonExistingFileInExistingProject;
		try {
			linkedFile.createLink(location, IResource.ALLOW_MISSING_LOCAL, getMonitor());
		} catch (CoreException e) {
			fail("1.99", e);
		}

		IFile dest = existingProject.getFile("FailedMoveDest");
		try {
			linkedFile.move(dest.getFullPath(), IResource.NONE, getMonitor());
			fail("2.0");
		} catch (CoreException e1) {
			//should fail
		}
		assertTrue("2.1", !dest.exists());
		try {
			linkedFile.move(dest.getFullPath(), IResource.FORCE, getMonitor());
			fail("2.2");
		} catch (CoreException e1) {
			//should fail
		}
		assertTrue("2.3", !dest.exists());
	}

	/**
	 * Tests moving a linked folder that doesn't exist in the file system
	 */
	public void testMoveMissingFolder() {
		IPath location = getRandomLocation();
		IFolder linkedFolder = nonExistingFolderInExistingProject;
		try {
			linkedFolder.createLink(location, IResource.ALLOW_MISSING_LOCAL, getMonitor());
		} catch (CoreException e) {
			fail("1.99", e);
		}

		IFolder dest = existingProject.getFolder("FailedMoveDest");
		try {
			linkedFolder.move(dest.getFullPath(), IResource.NONE, getMonitor());
			fail("2.0");
		} catch (CoreException e1) {
			//should fail
		}
		assertTrue("2.1", !dest.exists());
		try {
			linkedFolder.move(dest.getFullPath(), IResource.FORCE, getMonitor());
			fail("2.2");
		} catch (CoreException e1) {
			//should fail
		}
		assertTrue("2.3", !dest.exists());
	}

	public void testMoveProjectWithLinks() {
		IPath fileLocation = getRandomLocation();
		IFile file = nonExistingFileInExistingProject;
		IFolder folder = nonExistingFolderInExistingProject;
		IFile childFile = folder.getFile(childName);
		IResource[] oldResources = new IResource[] {file, folder, existingProject, childFile};
		try {
			try {
				createFileInFileSystem(resolve(fileLocation));
				folder.createLink(localFolder, IResource.NONE, getMonitor());
				file.createLink(fileLocation, IResource.NONE, getMonitor());
			} catch (CoreException e) {
				fail("1.0", e);
			}

			//move the project
			IProject destination = getWorkspace().getRoot().getProject("MoveTargetProject");
			IFile newFile = destination.getFile(file.getProjectRelativePath());
			IFolder newFolder = destination.getFolder(folder.getProjectRelativePath());
			IFile newChildFile = newFolder.getFile(childName);
			IResource[] newResources = new IResource[] {destination, newFile, newFolder, newChildFile};

			assertDoesNotExistInWorkspace("2.0", destination);

			try {
				existingProject.move(destination.getFullPath(), IResource.SHALLOW, getMonitor());
			} catch (CoreException e) {
				fail("2.1", e);
			}
			assertExistsInWorkspace("3.0", newResources);
			assertDoesNotExistInWorkspace("3.1", oldResources);

			assertTrue("3.2", newFile.isLinked());
			assertEquals("3.3", resolve(fileLocation), newFile.getLocation());

			assertTrue("3.4", newFolder.isLinked());
			assertEquals("3.5", resolve(localFolder), newFolder.getLocation());

			assertTrue("3.6", destination.isSynchronized(IResource.DEPTH_INFINITE));

			//now do a deep move back to the original project
			try {
				destination.move(existingProject.getFullPath(), IResource.NONE, getMonitor());
			} catch (CoreException e) {
				fail("5.0", e);
			}
			assertExistsInWorkspace("5.1", oldResources);
			assertDoesNotExistInWorkspace("5.2", newResources);
			assertTrue("5.3", !file.isLinked());
			assertTrue("5.4", !folder.isLinked());
			assertEquals("5.5", existingProject.getLocation().append(file.getProjectRelativePath()), file.getLocation());
			assertEquals("5.6", existingProject.getLocation().append(folder.getProjectRelativePath()), folder.getLocation());
			assertTrue("5.7", existingProject.isSynchronized(IResource.DEPTH_INFINITE));
			assertTrue("5.8", destination.isSynchronized(IResource.DEPTH_INFINITE));
		} finally {
			Workspace.clear(resolve(fileLocation).toFile());
		}
	}

	public void testNatureVeto() {
		//note: simpleNature has the link veto turned on.

		//test create link on project with nature veto
		try {
			IProjectDescription description = existingProject.getDescription();
			description.setNatureIds(new String[] {NATURE_SIMPLE});
			existingProject.setDescription(description, IResource.NONE, getMonitor());
		} catch (CoreException e) {
			fail("1.0", e);
		}
		try {
			nonExistingFolderInExistingProject.createLink(localFolder, IResource.NONE, getMonitor());
			fail("1.1");
		} catch (CoreException e) {
			//should fail
		}
		try {
			nonExistingFileInExistingProject.createLink(localFile, IResource.NONE, getMonitor());
			fail("1.2");
		} catch (CoreException e) {
			//should fail
		}

		//test add nature with veto to project that already has link
		try {
			existingProject.delete(IResource.FORCE, getMonitor());
			existingProject.create(getMonitor());
			existingProject.open(getMonitor());
			nonExistingFolderInExistingProject.createLink(localFolder, IResource.NONE, getMonitor());
		} catch (CoreException e) {
			fail("2.0", e);
		}
		try {
			IProjectDescription description = existingProject.getDescription();
			description.setNatureIds(new String[] {NATURE_SIMPLE});
			existingProject.setDescription(description, IResource.NONE, getMonitor());
			fail("3.0");
		} catch (CoreException e) {
			//should fail
		}
	}
	
	/**
	 * Create a project with a linked resource at depth > 2, and refresh it.
	 */
	public void testRefreshDeepLink() {
		IFolder link = nonExistingFolderInExistingFolder;
		IPath linkLocation = localFolder;
		IPath localChild = linkLocation.append("Child");
		IFile linkChild = link.getFile(localChild.lastSegment());
		createFileInFileSystem(localChild);
		try {
			link.createLink(linkLocation, IResource.NONE, getMonitor());
		} catch (CoreException e) {
			fail("0.99", e);
		}
		assertTrue("1.0", link.exists());
		assertTrue("1.1", linkChild.exists());
		
		try {
			IProject project = link.getProject();
			project.refreshLocal(IResource.DEPTH_INFINITE, getMonitor());
		} catch (CoreException e) {
			fail("1.99", e);
		}

		assertTrue("2.0", link.exists());
		assertTrue("2.1", linkChild.exists());
	}
}
