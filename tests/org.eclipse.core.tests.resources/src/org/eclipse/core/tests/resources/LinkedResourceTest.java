/**********************************************************************
 * Copyright (c) 2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.core.tests.resources;

import java.io.IOException;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.internal.resources.Workspace;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.tests.harness.*;

/**
 * Tests the following API methods 
 * 	IFile#createLink
 * 	IFile#createLink
 */
public class LinkedResourceTest extends EclipseWorkspaceTest {
	protected IProject existingProject;
	protected IFolder existingTopFolder;
	protected IFolder existingSubFolder;
	protected IProject nonExistingProject;
	protected IFolder nonExistingTopFolder1;
	protected IFolder nonExistingTopFolder2;
	protected IFolder nonExistingSubFolder;
	protected IFile existingFile1;
	protected IFile nonExistingFile1;
	protected IFile nonExistingFile2;
	protected IPath existingLocation;
	protected IPath nonExistingLocation;
	protected IPath localFile;
	protected String childName = "File.txt";

public LinkedResourceTest() {
	super();
}
public LinkedResourceTest(String name) {
	super(name);
}
public static Test suite() {
	return new TestSuite(LinkedResourceTest.class);
}

protected void setUp() throws Exception {
	super.setUp();
	existingProject = getWorkspace().getRoot().getProject("ExistingProject");
	existingTopFolder = existingProject.getFolder("TopFolderInExistingProject");
	existingSubFolder = existingTopFolder.getFolder("SubFolderInExistingProject");
	nonExistingProject = getWorkspace().getRoot().getProject("NonProject");
	nonExistingTopFolder1 = existingProject.getFolder("NonExistingFolderInExistingProject");
	nonExistingTopFolder2 = nonExistingProject.getFolder("NonExistingFolderInNonExistingProject");
	nonExistingSubFolder = nonExistingTopFolder1.getFolder("SubFolder");
	
	existingFile1 = existingProject.getFile("ExistingFile1");
	nonExistingFile1 = existingProject.getFile("NonExistingFile1");
	nonExistingFile2 = existingTopFolder.getFile("NonExistingFile2");
	existingLocation = getRandomLocation();
	nonExistingLocation = getRandomLocation();
	localFile = existingLocation.append(childName);
	cleanup();
}
protected void cleanup() throws IOException {
	ensureExistsInWorkspace(new IResource[] {existingProject, existingTopFolder, existingSubFolder, existingFile1}, true);
	ensureDoesNotExistInWorkspace(new IResource[] {
		nonExistingProject, nonExistingTopFolder1, nonExistingTopFolder2, nonExistingSubFolder, nonExistingFile1, nonExistingFile2});
	ensureDoesNotExistInFileSystem(nonExistingLocation.toFile());
	existingLocation.toFile().mkdirs();
	createFileInFileSystem(localFile, getRandomContents());
}
protected void tearDown() throws Exception {
	super.tearDown();
	Workspace.clear(existingLocation.toFile());
	Workspace.clear(nonExistingLocation.toFile());
}
/**
 * Tests creation of a linked resource whose corresponding file system
 * path does not exist. This should suceed but no operations will be
 * available on the resulting resource. */
public void testAllowMissingLocal() {
	//get a non-existing location
	IPath location = getRandomLocation();
	IFolder folder = nonExistingTopFolder1;
	
	//try to create without the flag (should fail)
	try {
		folder.createLink(location, IResource.NONE, getMonitor());
		fail("1.0");
	} catch (CoreException e) {
		//should fail
	}

	//now try to create with the flag (should suceed)
	try {
		folder.createLink(location, IResource.ALLOW_MISSING_LOCAL, getMonitor());
	} catch (CoreException e) {
		fail("1.1", e);
	}
	assertEquals("1.2", location, folder.getLocation());
	assertTrue("1.3", !location.toFile().exists());
	//getting children should suceed (and be empty)
	try {
		assertEquals("1.4", 0, folder.members().length);
	} catch (CoreException e) {
		fail("1.5", e);
	}
	//delete should suceed
	try {
		folder.delete(IResource.NONE, getMonitor());
	} catch (CoreException e) {
		fail("1.6", e);
	}

	//try to create with local path that can never exist
	location = new Path("b:\\does\\not\\exist");
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
	//creating child should fail
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
	ensureExistsInFileSystem(nonExistingTopFolder1);
	IFile blockedFile = nonExistingTopFolder1.getFile("BlockedFile");
	try {
		createFileInFileSystem(blockedFile.getLocation(), getRandomContents());
	} catch (IOException e) {
		fail("1.0", e);
	}
	try {
		//link the folder elsewhere
		nonExistingTopFolder1.createLink(existingLocation, IResource.NONE, getMonitor());
		//refresh the project
		existingProject.refreshLocal(IResource.DEPTH_INFINITE, getMonitor());
	} catch (CoreException e) {
		fail("1.1", e);
	}
	
	//the blocked file should not exist in the workspace
	assertTrue("1.2", !blockedFile.exists());
	assertTrue("1.3", nonExistingTopFolder1.exists());
	assertTrue("1.4", nonExistingTopFolder1.getFile(childName).exists());
	assertEquals("1.5", nonExistingTopFolder1.getLocation(), existingLocation);
	
	//now delete the link
	try {
		nonExistingTopFolder1.delete(IResource.NONE, getMonitor());
	} catch (CoreException e) {
		fail("1.99", e);
	}
	//the blocked file and the linked folder should not exist in the workspace
	assertTrue("2.0", !blockedFile.exists());
	assertTrue("2.1", !nonExistingTopFolder1.exists());
	assertTrue("2.2", !nonExistingTopFolder1.getFile(childName).exists());
	assertEquals("2.3", nonExistingTopFolder1.getLocation(), existingProject.getLocation().append(nonExistingTopFolder1.getName()));
	
	//now refresh again to discover the blocked resource
	try {
		existingProject.refreshLocal(IResource.DEPTH_INFINITE, getMonitor());
	} catch (CoreException e) {
		fail("2.99", e);
	}

	//the blocked file should now exist
	assertTrue("3.0", blockedFile.exists());
	assertTrue("3.1", nonExistingTopFolder1.exists());
	assertTrue("3.2", !nonExistingTopFolder1.getFile(childName).exists());
	assertEquals("3.3", nonExistingTopFolder1.getLocation(), existingProject.getLocation().append(nonExistingTopFolder1.getName()));
	
	//attempting to link again will fail because the folder exists in the workspace
	try {
		nonExistingTopFolder1.createLink(existingLocation, IResource.NONE, getMonitor());
		fail("3.4");
	} catch (CoreException e) {
		//expected
	}
}
/**
 * Automated test of IFile#createLink
 */
public void testLinkFile() {
	IResource[] interestingResources = new IResource[] {existingFile1, nonExistingFile1, nonExistingFile2};
	IPath[] interestingLocations = new IPath[] {existingLocation, nonExistingLocation};
	IProgressMonitor[] monitors = new IProgressMonitor[] {new FussyProgressMonitor(), new CancelingProgressMonitor(), null};
	Object[][] inputs = new Object[][] {interestingResources, interestingLocations, monitors};
	new TestPerformer("LinkedResourceTest.testLinkFile") {
		protected static final String CANCELLED = "cancelled";
		public boolean shouldFail(Object[] args, int count) {
			IResource resource = (IResource) args[0];
			IPath location = (IPath)args[1];
			//This resource already exists in the workspace
			if (resource.exists())
				return true;
			//The corresponding location in the local file system does not exist.
			if (!location.toFile().exists())
				return true;
			//The workspace contains a resource of a different type at the same path as this resource
			if (getWorkspace().getRoot().findMember(resource.getFullPath()) != null)
				return true;
			//The parent of this resource does not exist.
			if (!resource.getParent().isAccessible())
				return true;
			//The parent of this resource is not an open project
			if (resource.getParent().getType() != IResource.PROJECT)
				return true;
			//The name of this resource is not valid (according to IWorkspace.validateName)
			if (!getWorkspace().validateName(resource.getName(), IResource.FOLDER).isOK())
				return true;
			//The corresponding location in the local file system is occupied by a directory (as opposed to a file)
			if (location.toFile().isDirectory())
				return true;
			//passed all failure case so it should suceed
			return false;
		}
		public Object invokeMethod(Object[] args, int count) throws Exception {
			IFile file = (IFile) args[0];
			IPath location = (IPath)args[1];
			IProgressMonitor monitor = (IProgressMonitor) args[2];
			if (monitor instanceof FussyProgressMonitor)
				 ((FussyProgressMonitor) monitor).prepare();
			 try {
				file.createLink(location, IResource.NONE, monitor);
			 } catch (OperationCanceledException e) {
			 	return CANCELLED;
			 }
			if (monitor instanceof FussyProgressMonitor)
				 ((FussyProgressMonitor) monitor).sanityCheck();
			return null;
		}
		public boolean wasSuccess(Object[] args, Object result, Object[] oldState) throws Exception {
			if (result == CANCELLED)
				return true;
			IFile resource = (IFile) args[0];
			IPath location = (IPath)args[1];
			if (!resource.exists() || !location.toFile().exists())
				return false;
			if (!resource.getLocation().equals(location))
				return false;
			return true;
		}
		public void cleanUp(Object[] args, int count) {
			super.cleanUp(args, count);
			try {
				cleanup();
			} catch (IOException e) {
				fail("invocation " + count + " failed to cleanup", e);
			}
		}
	}
	.performTest(inputs);
}/**
 * Automated test of IFolder#createLink
 */
public void testLinkFolder() {
	IResource[] interestingResources = new IResource[] {existingTopFolder, existingSubFolder,
		nonExistingTopFolder1, nonExistingTopFolder2, nonExistingSubFolder};
	IPath[] interestingLocations = new IPath[] {existingLocation, nonExistingLocation};
	IProgressMonitor[] monitors = new IProgressMonitor[] {new FussyProgressMonitor(), new CancelingProgressMonitor(), null};
	Object[][] inputs = new Object[][] {interestingResources, interestingLocations, monitors};
	new TestPerformer("LinkedResourceTest.testLinkFolder") {
		protected static final String CANCELLED = "cancelled";
		public boolean shouldFail(Object[] args, int count) {
			IResource resource = (IResource) args[0];
			IPath location = (IPath)args[1];
			//This resource already exists in the workspace
			if (resource.exists())
				return true;
			//The corresponding location in the local file system does not exist.
			if (!location.toFile().exists())
				return true;
			//The workspace contains a resource of a different type at the same path as this resource
			if (getWorkspace().getRoot().findMember(resource.getFullPath()) != null)
				return true;
			//The parent of this resource does not exist.
			if (!resource.getParent().isAccessible())
				return true;
			//The parent of this resource is not an open project
			if (resource.getParent().getType() != IResource.PROJECT)
				return true;
			//The name of this resource is not valid (according to IWorkspace.validateName)
			if (!getWorkspace().validateName(resource.getName(), IResource.FOLDER).isOK())
				return true;
			//The corresponding location in the local file system is occupied by a file (as opposed to a directory)
			if (location.toFile().isFile())
				return true;
			//passed all failure case so it should suceed
			return false;
		}
		public Object invokeMethod(Object[] args, int count) throws Exception {
			IFolder folder = (IFolder) args[0];
			IPath location = (IPath)args[1];
			IProgressMonitor monitor = (IProgressMonitor) args[2];
			if (monitor instanceof FussyProgressMonitor)
				 ((FussyProgressMonitor) monitor).prepare();
			 try {
				folder.createLink(location, IResource.NONE, monitor);
			 } catch (OperationCanceledException e) {
			 	return CANCELLED;
			 }
			if (monitor instanceof FussyProgressMonitor)
				 ((FussyProgressMonitor) monitor).sanityCheck();
			return null;
		}
		public boolean wasSuccess(Object[] args, Object result, Object[] oldState) throws Exception {
			if (result == CANCELLED)
				return true;
			IFolder resource = (IFolder) args[0];
			IPath location = (IPath)args[1];
			if (!resource.exists() || !location.toFile().exists())
				return false;
			if (!resource.getLocation().equals(location))
				return false;
			//ensure child exists
			if (!resource.getFile(childName).exists())
				return false;
			return true;
		}
		public void cleanUp(Object[] args, int count) {
			super.cleanUp(args, count);
			try {
				cleanup();
			} catch (IOException e) {
				fail("invocation " + count + " failed to cleanup", e);
			}
		}
	}
	.performTest(inputs);

}
}
