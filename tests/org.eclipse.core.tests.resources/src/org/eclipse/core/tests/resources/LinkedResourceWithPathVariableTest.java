/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.core.tests.resources;

import java.util.ArrayList;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.internal.resources.Workspace;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.tests.harness.FileSystemHelper;

/**
 * This class extends <code>LinkedResourceTest</code> in order to use
 * randomly generated locations that are always variable-based.
 * TODO: add tests specific to linking resources using path variables (then
 * removing the variable, change the variable value, etc)
 */
public class LinkedResourceWithPathVariableTest extends LinkedResourceTest {

	private final static String VARIABLE_NAME = "ROOT";
	private final ArrayList toDelete = new ArrayList();

	public LinkedResourceWithPathVariableTest() {
		super();
	}

	public LinkedResourceWithPathVariableTest(String name) {
		super(name);
	}

	public static Test suite() {
		return new TestSuite(LinkedResourceWithPathVariableTest.class);
	}

	protected void setUp() throws Exception {
		IPath base = super.getRandomLocation();
		toDelete.add(base);
		getWorkspace().getPathVariableManager().setValue(VARIABLE_NAME, base);
		super.setUp();
	}

	protected void tearDown() throws Exception {
		getWorkspace().getPathVariableManager().setValue(VARIABLE_NAME, null);
		IPath [] paths = (IPath[]) toDelete.toArray(new IPath[0]);
		toDelete.clear();
		for (int i = 0; i < paths.length; i++) 
			Workspace.clear(paths[i].toFile());
		super.tearDown();
	}

	/**
	 * @see org.eclipse.core.tests.harness.ResourceTest#getRandomLocation()
	 */
	public IPath getRandomLocation() {
		IPathVariableManager pathVars = getWorkspace().getPathVariableManager();
		//low order bits are current time, high order bits are static counter
		IPath parent = new Path(VARIABLE_NAME);
		IPath path = FileSystemHelper.computeRandomLocation(parent);
		while (pathVars.resolvePath(path).toFile().exists()) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				//ignore
			}
			path = FileSystemHelper.computeRandomLocation(parent);
		}
		toDelete.add(pathVars.resolvePath(path));
		return path;
	}

	/**
	 * @see LinkedResourceTest#resolvePath(org.eclipse.core.runtime.IPath)
	 */
	protected IPath resolvePath(IPath path) {
		return getWorkspace().getPathVariableManager().resolvePath(path);
	}

	/**
	 * Tests a scenario where a variable used in a linked file location is
	 * removed.
	 */
	public void testFileVariableRemoved() {
		final IPathVariableManager manager = getWorkspace().getPathVariableManager();

		IFile file = nonExistingFileInExistingProject;
		IPath existingValue = manager.getValue(VARIABLE_NAME);

		// creates a variable-based location
		IPath variableBasedLocation = getRandomLocation();

		// the file should not exist yet
		assertDoesNotExistInWorkspace("1.0", file);

		try {
			file.createLink(variableBasedLocation, IResource.ALLOW_MISSING_LOCAL, null);
		} catch (CoreException e) {
			fail("1.1", e);
		}
		try {
			file.setContents(getContents("contents for a file"), IResource.FORCE, null);
		} catch (CoreException e) {
			fail("1.2", e);
		}

		// now the file exists in both workspace and file system
		assertExistsInWorkspace("2.0", file);
		assertExistsInFileSystem("2.1", file);

		// removes the variable - the location will be undefined (null)
		try {
			manager.setValue(VARIABLE_NAME, null);
		} catch (CoreException e) {
			fail("3.0", e);
		}
		assertExistsInWorkspace("3,1", file);

		//refresh local - should not fail or make the link disappear
		try {
			file.refreshLocal(IResource.DEPTH_ONE, getMonitor());
			file.getProject().refreshLocal(IResource.DEPTH_INFINITE, getMonitor());
		} catch (CoreException e) {
			fail("3.2");
		}

		assertExistsInWorkspace("3.3", file);

		// try to change resource's contents
		try {
			file.setContents(getContents("new contents"), IResource.NONE, null);
			// Resource has no-defined location - should fail
			fail("3.4");
		} catch (CoreException re) {
			// success: resource had no defined location
		}

		assertExistsInWorkspace("3.5", file);
		// the location is null
		assertNull("3.6", file.getLocation());

		// try validating another link location while there is a link with null location
		IFile other = existingProject.getFile("OtherVar");
		getWorkspace().validateLinkLocation(other, getRandomLocation());

		// re-creates the variable with its previous value
		try {
			manager.setValue(VARIABLE_NAME, existingValue);
		} catch (CoreException e) {
			fail("4.0", e);
		}

		assertExistsInWorkspace("5.0", file);
		assertNotNull("5.1", file.getLocation());
		assertExistsInFileSystem("5.2", file);
		// the contents must be the original ones
		try {
			assertTrue("5.3", compareContent(file.getContents(true), getContents("contents for a file")));
		} catch (CoreException e) {
			fail("5.4", e);
		}
	}

	/**
	 * Tests a scenario where a variable used in a linked folder location is
	 * removed.
	 */
	public void testFolderVariableRemoved() {
		final IPathVariableManager manager = getWorkspace().getPathVariableManager();

		IFolder folder = nonExistingFolderInExistingProject;
		IFile childFile = folder.getFile(childName);
		IPath existingValue = manager.getValue(VARIABLE_NAME);

		// creates a variable-based location
		IPath variableBasedLocation = getRandomLocation();

		// the file should not exist yet
		assertDoesNotExistInWorkspace("1.0", folder);

		try {
			folder.createLink(variableBasedLocation, IResource.ALLOW_MISSING_LOCAL, null);
			childFile.create(getRandomContents(), IResource.NONE, getMonitor());
		} catch (CoreException e) {
			fail("1.1", e);
		}
		try {
			childFile.setContents(getContents("contents for a file"), IResource.FORCE, null);
		} catch (CoreException e) {
			fail("1.2", e);
		}

		// now the file exists in both workspace and file system
		assertExistsInWorkspace("2.0", folder);
		assertExistsInWorkspace("2.1", childFile);
		assertExistsInFileSystem("2.2", folder);
		assertExistsInFileSystem("2.3", childFile);

		// removes the variable - the location will be undefined (null)
		try {
			manager.setValue(VARIABLE_NAME, null);
		} catch (CoreException e) {
			fail("3.0", e);
		}
		assertExistsInWorkspace("3.1", folder);

		//refresh local - should not fail but should cause link's children to disappear
		try {
			folder.refreshLocal(IResource.DEPTH_INFINITE, getMonitor());
			folder.getProject().refreshLocal(IResource.DEPTH_INFINITE, getMonitor());
		} catch (CoreException e) {
			fail("3.2", e);
		}
		assertExistsInWorkspace("3.3", folder);
		assertDoesNotExistInWorkspace("3.4", childFile);

		//try to copy a file to the folder
		IFile destination = folder.getFile(existingFileInExistingProject.getName());
		try {
			existingFileInExistingProject.copy(destination.getFullPath(), IResource.NONE, getMonitor());
			//should fail
			fail("3.5");
		} catch (CoreException e) {
			//expected
		}
		assertTrue("3.6", !destination.exists());

		//try to create a sub-file
		try {
			destination.create(getRandomContents(), IResource.NONE, getMonitor());
			//should fail
			fail("3.7");
		} catch (CoreException e) {
			//expected
		}

		//try to create a sub-folder
		IFolder subFolder = folder.getFolder("SubFolder");
		try {
			subFolder.create(IResource.NONE, true, getMonitor());
			//should fail
			fail("3.8");
		} catch (CoreException e) {
			//expected
		}

		// try to change resource's contents
		try {
			childFile.setContents(getContents("new contents"), IResource.NONE, null);
			// Resource has no-defined location - should fail
			fail("4.0");
		} catch (CoreException re) {
			// success: resource had no defined location
		}

		assertExistsInWorkspace("4.1", folder);
		// the location is null
		assertNull("4.2", folder.getLocation());

		// re-creates the variable with its previous value
		try {
			manager.setValue(VARIABLE_NAME, existingValue);
		} catch (CoreException e) {
			fail("5.0", e);
		}

		assertExistsInWorkspace("6.0", folder);
		assertNotNull("6.1", folder.getLocation());
		assertExistsInFileSystem("6.2", folder);
		assertDoesNotExistInWorkspace("6.3", childFile);
		assertExistsInFileSystem("6.4", childFile);

		//refresh should recreate the child
		try {
			folder.refreshLocal(IResource.DEPTH_INFINITE, getMonitor());
		} catch (CoreException e) {
			fail("7.0", e);
		}
		assertExistsInWorkspace("7.1", folder);
		assertExistsInWorkspace("7.2", childFile);
	}

	/**
	 * Tests scenario where links are relative to undefined variables
	 */
	public void testUndefinedVariable() {
		IPath folderLocation = new Path("NOVAR/folder");
		IPath fileLocation = new Path("NOVAR/abc.txt");
		IFile testFile = existingProject.getFile("UndefinedVar.txt");
		IFolder testFolder = existingProject.getFolder("UndefinedVarTest");

		//should fail to create links
		try {
			testFile.createLink(fileLocation, IResource.NONE, getMonitor());
			fail("1.0");
		} catch (CoreException e) {
			//should fail
		}
		try {
			testFolder.createLink(folderLocation, IResource.NONE, getMonitor());
			fail("1.1");
		} catch (CoreException e) {
			//should fail
		}

		//validate method should return warning
		assertTrue("1.2", getWorkspace().validateLinkLocation(testFolder, folderLocation).getSeverity() == IStatus.WARNING);
		assertTrue("1.3", getWorkspace().validateLinkLocation(testFile, fileLocation).getSeverity() == IStatus.WARNING);

		//should succeed with ALLOW_MISSING_LOCAL
		try {
			testFile.createLink(fileLocation, IResource.ALLOW_MISSING_LOCAL, getMonitor());
		} catch (CoreException e) {
			fail("2.0", e);
		}
		try {
			testFolder.createLink(folderLocation, IResource.ALLOW_MISSING_LOCAL, getMonitor());
		} catch (CoreException e) {
			fail("2.1", e);
		}

		//copy should fail
		IPath copyFileDestination = existingProject.getFullPath().append("CopyFileDest");
		IPath copyFolderDestination = existingProject.getFullPath().append("CopyFolderDest");

		try {
			testFile.copy(copyFileDestination, IResource.NONE, getMonitor());
			fail("3.0");
		} catch (CoreException e) {
			//should fail
		}
		try {
			testFolder.copy(copyFolderDestination, IResource.NONE, getMonitor());
			fail("3.1");
		} catch (CoreException e) {
			//should fail
		}

		//move should fail
		IPath moveFileDestination = existingProject.getFullPath().append("MoveFileDest");
		IPath moveFolderDestination = existingProject.getFullPath().append("MoveFolderDest");

		try {
			testFile.move(moveFileDestination, IResource.NONE, getMonitor());
			fail("4.0");
		} catch (CoreException e) {
			//should fail
		}
		try {
			testFolder.move(moveFolderDestination, IResource.NONE, getMonitor());
			fail("4.1");
		} catch (CoreException e) {
			//should fail
		}

		//refresh local should succeed
		try {
			testFile.refreshLocal(IResource.DEPTH_INFINITE, getMonitor());
			testFolder.refreshLocal(IResource.DEPTH_INFINITE, getMonitor());
			testFile.refreshLocal(IResource.DEPTH_ZERO, getMonitor());
			testFolder.refreshLocal(IResource.DEPTH_ZERO, getMonitor());
			existingProject.refreshLocal(IResource.NONE, getMonitor());
		} catch (CoreException e) {
			fail("5.0", e);
		}

		//renaming the project shallow is ok
		try {
			IProject project = testFolder.getProject();
			IProjectDescription desc = project.getDescription();
			desc.setName("moveDest");
			project.move(desc, IResource.SHALLOW | IResource.FORCE, getMonitor());
		} catch (CoreException e) {
			fail("6.0");
		}

		//delete should succeed
		try {
			testFile.delete(IResource.NONE, getMonitor());
			testFolder.delete(IResource.NONE, getMonitor());
		} catch (CoreException e) {
			fail("9.0", e);
		}
	}

	/**
	 * Tests a scenario where a variable used in a linked file location is
	 * changed.
	 */
	public void testVariableChanged() {
		final IPathVariableManager manager = getWorkspace().getPathVariableManager();

		IPath existingValue = manager.getValue(VARIABLE_NAME);

		IFile file = nonExistingFileInExistingProject;

		// creates a variable-based location 
		IPath variableBasedLocation = getRandomLocation();

		// the file should not exist yet
		assertDoesNotExistInWorkspace("1.0", file);

		try {
			file.createLink(variableBasedLocation, IResource.ALLOW_MISSING_LOCAL, getMonitor());
		} catch (CoreException e) {
			fail("1.1", e);
		}
		try {
			file.setContents(getContents("contents for a file"), IResource.FORCE, getMonitor());
		} catch (CoreException e) {
			fail("1.2", e);
		}

		// now the file exists in both workspace and file system
		assertExistsInWorkspace("2.0", file);
		assertExistsInFileSystem("2.1", file);

		// changes the variable value - the file location will change
		try {
			IPath newLocation = super.getRandomLocation();
			toDelete.add(newLocation);
			manager.setValue(VARIABLE_NAME, newLocation);
		} catch (CoreException e) {
			fail("2.2", e);
		}

		// try to change resource's contents				 
		try {
			file.setContents(getContents("new contents"), IResource.NONE, getMonitor());
			// Resource was out of sync - should not be able to change
			fail("3.0");
		} catch (CoreException e) {
			assertEquals("3.1", IResourceStatus.OUT_OF_SYNC_LOCAL, e.getStatus().getCode());
		}

		assertExistsInWorkspace("3.2", file);
		// the location is different - does not exist anymore
		assertDoesNotExistInFileSystem("3.3", file);

		// successfully changes resource's contents (using IResource.FORCE)
		try {
			file.setContents(getContents("contents in different location"), IResource.FORCE, getMonitor());
		} catch (CoreException e) {
			fail("4.0", e);
		}

		// now the file exists in a different location
		assertExistsInFileSystem("4.1", file);

		// its location must have changed reflecting the variable change
		IPath expectedNewLocation = manager.resolvePath(variableBasedLocation);
		IPath actualNewLocation = file.getLocation();
		assertEquals("4.2", expectedNewLocation, actualNewLocation);

		// its contents are as just set
		try {
			assertTrue("4.3", compareContent(file.getContents(), getContents("contents in different location")));
		} catch (CoreException e) {
			fail("4.4", e);
		}

		// clean-up
		ensureDoesNotExistInFileSystem(file);

		// restore the previous value
		try {
			manager.setValue(VARIABLE_NAME, existingValue);
		} catch (CoreException e) {
			fail("5.0", e);
		}

		assertExistsInWorkspace("5.1", file);
		assertExistsInFileSystem("5.2", file);
		// the contents must be the original ones
		try {
			assertTrue("5.3", compareContent(file.getContents(true), getContents("contents for a file")));
		} catch (CoreException e) {
			fail("5.4", e);
		}
	}
}
