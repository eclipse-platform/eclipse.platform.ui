/**********************************************************************
 * IBM - Initial API and implementation
 * Copyright (c) 2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * IBM - Initial API and implementation
 * Contributors:
 * IBM - Initial API and implementation
 *********************************************************************/

package org.eclipse.core.tests.resources;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;

/**
 * This class extends <code>LinkedResourceTest</code> in order to use
 * randomly generated locations that are always variable-based.
 * TODO: add tests specific to linking resources using path variables (then
 * removing the variable, change the variable value, etc)
 */
public class LinkedResourceWithPathVariableTest extends LinkedResourceTest {

	private final static String VARIABLE_NAME = "ROOT";

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
		IPath base = Platform.getLocation().removeLastSegments(1);
		getWorkspace().getPathVariableManager().setValue(VARIABLE_NAME, base);
		super.setUp();
	}
	protected void tearDown() throws Exception {
		getWorkspace().getPathVariableManager().setValue(VARIABLE_NAME, null);
		super.tearDown();
	}
	/**
	 * @see org.eclipse.core.tests.harness.EclipseWorkspaceTest#getRandomLocation()
	 */
	public IPath getRandomLocation() {
		IPathVariableManager pathVars = getWorkspace().getPathVariableManager();
		//low order bits are current time, high order bits are static counter
		IPath parent = new Path(VARIABLE_NAME);
		final long mask = 0x00000000FFFFFFFFL;
		long segment = (((long) ++nextLocationCounter) << 32) | (System.currentTimeMillis() & mask);
		IPath path = parent.append(Long.toString(segment));
		while (pathVars.resolvePath(path).toFile().exists()) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
			}
			segment = (((long) ++nextLocationCounter) << 32) | (System.currentTimeMillis() & mask);
			path = parent.append(Long.toString(segment));
		}
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
	 * changed.
	 */
	public void testVariableChanged() throws Exception {
		final IPathVariableManager manager = getWorkspace().getPathVariableManager();

		IPath existingValue = manager.getValue(VARIABLE_NAME);

		IFile file = nonExistingFileInExistingProject;

		// creates a variable-based location 
		IPath variableBasedLocation = getRandomLocation();

		// the file should not exist yet
		assertDoesNotExistInWorkspace("1.0", file);

		file.createLink(variableBasedLocation, IResource.ALLOW_MISSING_LOCAL, getMonitor());
		file.setContents(getContents("contents for a file"), IResource.FORCE, getMonitor());

		// now the file exists in both workspace and file system
		assertExistsInWorkspace("2.0", file);
		assertExistsInFileSystem("2.1", file);

		// changes the variable value - the file location will change
		manager.setValue(VARIABLE_NAME, super.getRandomLocation());

		// try to change resource's contents				 
		try {
			file.setContents(getContents("new contents"), IResource.NONE, getMonitor());
			// Resource was out of sync - should not be able to change
			fail("3.0");
		} catch (CoreException re) {
			if (re.getStatus().getCode() != IResourceStatus.OUT_OF_SYNC_LOCAL)
				throw re;
			// else success: resource was out of sync
		}

		assertExistsInWorkspace("3.1", file);
		// the location is different - does not exist anymore
		assertDoesNotExistInFileSystem("3.2", file);

		// successfuly changes resource's contents (using IResource.FORCE)
		file.setContents(getContents("contents in different location"), IResource.FORCE, getMonitor());

		// now the file exists in a different location
		assertExistsInFileSystem("4.1", file);

		// its location must have changed reflecting the variable change
		IPath expectedNewLocation = manager.resolvePath(variableBasedLocation);
		IPath actualNewLocation = file.getLocation();
		assertEquals("4.2", expectedNewLocation, actualNewLocation);

		// its contents are as just set
		assertTrue("4.3", compareContent(file.getContents(), getContents("contents in different location")));

		// clean-up
		ensureDoesNotExistInFileSystem(file);

		// restore the previous value
		manager.setValue(VARIABLE_NAME, existingValue);

		assertExistsInWorkspace("5.0", file);
		assertExistsInFileSystem("5.1", file);
		// the contents must be the original ones
		assertTrue("5.2", compareContent(file.getContents(true), getContents("contents for a file")));
	}
	/**
	 * Tests a scenario where a variable used in a linked file location is
	 * removed.
	 */
	public void testVariableRemoved() throws Exception {
		final IPathVariableManager manager = getWorkspace().getPathVariableManager();

		IFile file = nonExistingFileInExistingProject;
		IPath existingValue = manager.getValue(VARIABLE_NAME);

		// creates a variable-based location
		IPath variableBasedLocation = getRandomLocation();

		// the file should not exist yet
		assertDoesNotExistInWorkspace("1.0", file);

		file.createLink(variableBasedLocation, IResource.ALLOW_MISSING_LOCAL, null);
		file.setContents(getContents("contents for a file"), IResource.FORCE, null);

		// now the file exists in both workspace and file system
		assertExistsInWorkspace("2.0", file);
		assertExistsInFileSystem("2.1", file);

		// removes the variable - the location will be undefined (null)
		manager.setValue(VARIABLE_NAME, null);

		// try to change resource's contents
		try {
			file.setContents(getContents("new contents"), IResource.NONE, null);
			// Resource has no-defined location - should fail
			fail("3.0");
		} catch (CoreException re) {
			// success: resource had no defined location
		}

		assertExistsInWorkspace("3.1", file);
		// the location is null
		assertNull("3.2", file.getLocation());

		// re-creates the variable with its previous value
		manager.setValue(VARIABLE_NAME, existingValue);

		assertExistsInWorkspace("5.0", file);
		assertNotNull("5.1", file.getLocation());
		assertExistsInFileSystem("5.2", file);
		// the contents must be the original ones
		assertTrue("5.3", compareContent(file.getContents(true), getContents("contents for a file")));
	}
}
