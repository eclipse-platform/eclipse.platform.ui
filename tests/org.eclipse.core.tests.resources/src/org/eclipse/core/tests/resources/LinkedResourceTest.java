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
import org.eclipse.core.boot.BootLoader;
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
	protected IProject otherExistingProject;
	protected IProject closedProject;
	protected IProject nonExistingProject;
	protected IFolder existingFolderInExistingProject;
	protected IFolder existingFolderInExistingFolder;
	protected IFolder nonExistingFolderInExistingProject;
	protected IFolder nonExistingFolderInOtherExistingProject;
	protected IFolder nonExistingFolderInNonExistingProject;
	protected IFolder nonExistingFolderInExistingFolder;
	protected IFolder nonExistingFolderInNonExistingFolder;
	protected IFile existingFileInExistingProject;
	protected IFile nonExistingFileInExistingProject;
	protected IFile nonExistingFileInOtherExistingProject;
	protected IFile nonExistingFileInExistingFolder;
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
	existingLocation = getRandomLocation();
	nonExistingLocation = getRandomLocation();
	localFile = existingLocation.append(childName);
	doCleanup();
}
protected void doCleanup() throws Exception {
	ensureExistsInWorkspace(new IResource[] {
		existingProject, 
		otherExistingProject,
		closedProject, 
		existingFolderInExistingProject, 
		existingFolderInExistingFolder, 
		existingFileInExistingProject}, true);
	closedProject.close(getMonitor());
	ensureDoesNotExistInWorkspace(new IResource[] {
		nonExistingProject, 
		nonExistingFolderInExistingProject, 
		nonExistingFolderInExistingFolder, 
		nonExistingFolderInOtherExistingProject, 
		nonExistingFolderInNonExistingProject, 
		nonExistingFolderInNonExistingFolder, 
		nonExistingFileInExistingProject, 
		nonExistingFileInOtherExistingProject,
		nonExistingFileInExistingFolder});
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
	IFolder folder = nonExistingFolderInExistingProject;
	
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
	// creating child should fail when the OS is Windows
	try {
		folder.getFile("abc.txt").create(getRandomContents(), IResource.NONE, getMonitor());
		if (BootLoader.getOS().equals(BootLoader.OS_WIN32))
		fail("2.5");
	} catch (CoreException e) {
		// catch this failure for non-Windows OSes
		if (!BootLoader.getOS().equals(BootLoader.OS_WIN32))
			fail("2.6");
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
	try {
		createFileInFileSystem(blockedFile.getLocation(), getRandomContents());
	} catch (IOException e) {
		fail("1.0", e);
	}
	try {
		//link the folder elsewhere
		nonExistingFolderInExistingProject.createLink(existingLocation, IResource.NONE, getMonitor());
		//refresh the project
		existingProject.refreshLocal(IResource.DEPTH_INFINITE, getMonitor());
	} catch (CoreException e) {
		fail("1.1", e);
	}
	
	//the blocked file should not exist in the workspace
	assertTrue("1.2", !blockedFile.exists());
	assertTrue("1.3", nonExistingFolderInExistingProject.exists());
	assertTrue("1.4", nonExistingFolderInExistingProject.getFile(childName).exists());
	assertEquals("1.5", nonExistingFolderInExistingProject.getLocation(), existingLocation);
	
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
		nonExistingFolderInExistingProject.createLink(existingLocation, IResource.NONE, getMonitor());
		fail("3.4");
	} catch (CoreException e) {
		//expected
	}
}
public void testCopyFile() {
	IResource[] sourceResources = new IResource[] {nonExistingFileInExistingProject};
	IResource[] destinationResources = new IResource[] {existingProject, closedProject, nonExistingFileInOtherExistingProject, nonExistingFileInExistingFolder};
	IProgressMonitor[] monitors = new IProgressMonitor[] {new FussyProgressMonitor(), new CancelingProgressMonitor(), null};
	Object[][] inputs = new Object[][] {sourceResources, destinationResources, monitors};
	new TestPerformer("LinkedResourceTest.testCopyFile") {
		protected static final String CANCELLED = "cancelled";
		public boolean shouldFail(Object[] args, int count) {
			IResource destination = (IResource) args[1];
			IResource parent = destination.getParent();
			if (parent == null || parent.getType() != IResource.PROJECT)
				return true;
			if (!parent.isAccessible())
				return true;
			if (destination.exists())
				return true;
			//passed all failure case so it should suceed
			return false;
		}
		public Object invokeMethod(Object[] args, int count) throws Exception {
			IFile source = (IFile) args[0];
			IResource destination = (IResource) args[1];
			IProgressMonitor monitor = (IProgressMonitor) args[2];
			if (monitor instanceof FussyProgressMonitor)
				 ((FussyProgressMonitor) monitor).prepare();
			 try {
	 			source.createLink(localFile, IResource.NONE, null);
				source.copy(destination.getFullPath(), IResource.NONE, monitor);
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
			IResource source = (IResource) args[0];
			IResource destination = (IResource) args[1];
			if (!destination.exists())
				return false;
			if (!destination.isLinked())
				return false;
			if (!source.getLocation().equals(destination.getLocation()))
				return false;
			return true;
		}
		public void cleanUp(Object[] args, int count) {
			super.cleanUp(args, count);
			try {
				doCleanup();
			} catch (Exception e) {
				fail("invocation " + count + " failed to cleanup", e);
			}
		}
	}
	.performTest(inputs);
}
public void testCopyFolder() {
	IResource[] sourceResources = new IResource[] {nonExistingFolderInExistingProject};
	IResource[] destinationResources = new IResource[] {existingProject, closedProject, nonExistingProject, existingFolderInExistingProject, nonExistingFolderInOtherExistingProject, nonExistingFolderInExistingFolder};
	IProgressMonitor[] monitors = new IProgressMonitor[] {new FussyProgressMonitor(), new CancelingProgressMonitor(), null};
	Object[][] inputs = new Object[][] {sourceResources, destinationResources, monitors};
	new TestPerformer("LinkedResourceTest.testCopyFolder") {
		protected static final String CANCELLED = "cancelled";
		public boolean shouldFail(Object[] args, int count) {
			IResource destination = (IResource) args[1];
			IResource parent = destination.getParent();
			if (parent == null || parent.getType() != IResource.PROJECT)
				return true;
			if (!parent.isAccessible())
				return true;
			if (destination.exists())
				return true;
			//passed all failure case so it should suceed
			return false;
		}
		public Object invokeMethod(Object[] args, int count) throws Exception {
			IFolder source = (IFolder) args[0];
			IResource destination = (IResource) args[1];
			IProgressMonitor monitor = (IProgressMonitor) args[2];
			if (monitor instanceof FussyProgressMonitor)
				 ((FussyProgressMonitor) monitor).prepare();
			 try {
	 			source.createLink(existingLocation, IResource.NONE, null);
				source.copy(destination.getFullPath(), IResource.NONE, monitor);
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
			IResource source = (IResource) args[0];
			IResource destination = (IResource) args[1];
			if (!destination.exists())
				return false;
			if (!destination.isLinked())
				return false;
			if (!source.getLocation().equals(destination.getLocation()))
				return false;
			return true;
		}
		public void cleanUp(Object[] args, int count) {
			super.cleanUp(args, count);
			try {
				doCleanup();
			} catch (Exception e) {
				fail("invocation " + count + " failed to cleanup", e);
			}
		}
	}
	.performTest(inputs);
}
public void testMoveFolder() {
	IResource[] sourceResources = new IResource[] {nonExistingFolderInExistingProject};
	IResource[] destinationResources = new IResource[] {existingProject, closedProject, nonExistingProject, existingFolderInExistingProject, nonExistingFolderInOtherExistingProject, nonExistingFolderInExistingFolder};
	IProgressMonitor[] monitors = new IProgressMonitor[] {new FussyProgressMonitor(), new CancelingProgressMonitor(), null};
	Object[][] inputs = new Object[][] {sourceResources, destinationResources, monitors};
	new TestPerformer("LinkedResourceTest.testMoveFolder") {
		protected static final String CANCELLED = "cancelled";
		public boolean shouldFail(Object[] args, int count) {
			IResource destination = (IResource) args[1];
			IResource parent = destination.getParent();
			if (parent == null || parent.getType() != IResource.PROJECT)
				return true;
			if (!parent.isAccessible())
				return true;
			if (destination.exists())
				return true;
			//passed all failure case so it should suceed
			return false;
		}
		public Object invokeMethod(Object[] args, int count) throws Exception {
			IFolder source = (IFolder) args[0];
			IResource destination = (IResource) args[1];
			IProgressMonitor monitor = (IProgressMonitor) args[2];
			if (monitor instanceof FussyProgressMonitor)
				 ((FussyProgressMonitor) monitor).prepare();
			 try {
	 			source.createLink(existingLocation, IResource.NONE, null);
				source.move(destination.getFullPath(), IResource.NONE, monitor);
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
			IResource destination = (IResource) args[1];
			if (!destination.exists())
				return false;
			if (!destination.isLinked())
				return false;
			if (!existingLocation.equals(destination.getLocation()))
				return false;
			return true;
		}
		public void cleanUp(Object[] args, int count) {
			super.cleanUp(args, count);
			try {
				doCleanup();
			} catch (Exception e) {
				fail("invocation " + count + " failed to cleanup", e);
			}
		}
	}
	.performTest(inputs);
}
/**
 * Automated test of IFile#createLink
 */
public void testLinkFile() {
	IResource[] interestingResources = new IResource[] {existingFileInExistingProject, nonExistingFileInExistingProject, nonExistingFileInExistingFolder};
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
				doCleanup();
			} catch (Exception e) {
				fail("invocation " + count + " failed to cleanup", e);
			}
		}
	}
	.performTest(inputs);
}/**
 * Automated test of IFolder#createLink
 */
public void testLinkFolder() {
	IResource[] interestingResources = new IResource[] {existingFolderInExistingProject, existingFolderInExistingFolder,
		nonExistingFolderInExistingProject, nonExistingFolderInNonExistingProject, nonExistingFolderInNonExistingFolder};
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
				doCleanup();
			} catch (Exception e) {
				fail("invocation " + count + " failed to cleanup", e);
			}
		}
	}
	.performTest(inputs);

}
public void testMoveFile() {
	IResource[] sourceResources = new IResource[] {nonExistingFileInExistingProject};
	IResource[] destinationResources = new IResource[] {existingProject, closedProject, nonExistingFileInOtherExistingProject, nonExistingFileInExistingFolder};
	IProgressMonitor[] monitors = new IProgressMonitor[] {new FussyProgressMonitor(), new CancelingProgressMonitor(), null};
	Object[][] inputs = new Object[][] {sourceResources, destinationResources, monitors};
	new TestPerformer("LinkedResourceTest.testMoveFile") {
		protected static final String CANCELLED = "cancelled";
		public boolean shouldFail(Object[] args, int count) {
			IResource destination = (IResource) args[1];
			IResource parent = destination.getParent();
			if (parent == null || parent.getType() != IResource.PROJECT)
				return true;
			if (!parent.isAccessible())
				return true;
			if (destination.exists())
				return true;
			//passed all failure case so it should suceed
			return false;
		}
		public Object invokeMethod(Object[] args, int count) throws Exception {
			IFile source = (IFile) args[0];
			IResource destination = (IResource) args[1];
			IProgressMonitor monitor = (IProgressMonitor) args[2];
			if (monitor instanceof FussyProgressMonitor)
				 ((FussyProgressMonitor) monitor).prepare();
			 try {
	 			source.createLink(localFile, IResource.NONE, null);
				source.move(destination.getFullPath(), IResource.NONE, monitor);
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
			IResource destination = (IResource) args[1];
			if (!destination.exists())
				return false;
			if (!destination.isLinked())
				return false;
			if (!localFile.equals(destination.getLocation()))
				return false;
			return true;
		}
		public void cleanUp(Object[] args, int count) {
			super.cleanUp(args, count);
			try {
				doCleanup();
			} catch (Exception e) {
				fail("invocation " + count + " failed to cleanup", e);
			}
		}
	}
	.performTest(inputs);
}
}
