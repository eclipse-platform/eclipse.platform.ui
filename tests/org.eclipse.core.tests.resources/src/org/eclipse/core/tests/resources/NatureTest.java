/**********************************************************************
 * Copyright (c) 2000,2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.core.tests.resources;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.tests.harness.EclipseWorkspaceTest;
/**
 * Tests all aspects of project natures.  These tests only
 * exercise API classes and methods.  Note that the nature-related
 * APIs on IWorkspace are tested by IWorkspaceTest.
 */
public class NatureTest extends EclipseWorkspaceTest {
/**
 * Constructor for NatureTest.
 */
public NatureTest() {
	super();
}
/**
 * Constructor for NatureTest.
 * @param name
 */
public NatureTest(String name) {
	super(name);
}
public static Test suite() {
	return new TestSuite(NatureTest.class);
}
/**
 * Sets the given set of natures for the project.  If success
 * does not match the "shouldFail" argument, an assertion error
 * with the given message is thrown.
 */
protected void setNatures(String message, boolean shouldFail, IProject project, String[] natures) {
	try {
		IProjectDescription desc = project.getDescription();
		desc.setNatureIds(natures);
		project.setDescription(desc, getMonitor());
		if (shouldFail)
			fail(message);
	} catch (CoreException e) {
		if (!shouldFail)
			fail(message, e);
	}
}
protected void tearDown() throws Exception {
	super.tearDown();
	getWorkspace().getRoot().refreshLocal(IResource.DEPTH_INFINITE, null);
	ensureDoesNotExistInWorkspace(getWorkspace().getRoot());
}
/**
 * Tests invalid additions to the set of natures for a project.
 */
public void testInvalidAdditions() {
	IWorkspace ws = ResourcesPlugin.getWorkspace();
	IProject project = ws.getRoot().getProject("Project");
	ensureExistsInWorkspace(project, true);
	setNatures("1.0", false, project, new String[] {NATURE_SIMPLE});

	//Adding a nature that is not available. 
	setNatures("2.0", true, project, new String[] {NATURE_SIMPLE, NATURE_MISSING});
	try {
		assertTrue("2.1", project.hasNature(NATURE_SIMPLE));
		assertTrue("2.2", !project.hasNature(NATURE_MISSING));
		assertTrue("2.3", project.isNatureEnabled(NATURE_SIMPLE));
		assertTrue("2.4", !project.isNatureEnabled(NATURE_MISSING));
	} catch (CoreException e) {
		fail("2.99", e);
	}
	//Adding a nature that has a missing prerequisite. 
	setNatures("3.0", true, project, new String[] {NATURE_SIMPLE, NATURE_SNOW});
	try {
		assertTrue("3.1", project.hasNature(NATURE_SIMPLE));
		assertTrue("3.2", !project.hasNature(NATURE_SNOW));
		assertTrue("3.3", project.isNatureEnabled(NATURE_SIMPLE));
		assertTrue("3.4", !project.isNatureEnabled(NATURE_SNOW));
	} catch (CoreException e) {
		fail("3.99", e);
	}
	//Adding a nature that creates a duplicated set member. 
	setNatures("4.0", false, project, new String[] {NATURE_EARTH});
	setNatures("4.1", true, project, new String[] {NATURE_EARTH, NATURE_WATER});
	try {
		assertTrue("3.1", project.hasNature(NATURE_EARTH));
		assertTrue("3.2", !project.hasNature(NATURE_WATER));
		assertTrue("3.3", project.isNatureEnabled(NATURE_EARTH));
		assertTrue("3.4", !project.isNatureEnabled(NATURE_WATER));
	} catch (CoreException e) {
		fail("3.99", e);
	}
}
/**
 * Tests invalid removals from the set of natures for a project.
 */
public void testInvalidRemovals() {
	IWorkspace ws = ResourcesPlugin.getWorkspace();
	IProject project = ws.getRoot().getProject("Project");
	ensureExistsInWorkspace(project, true);

	//Removing a nature that still has dependents.
	setNatures("1.0", false, project, new String[] {NATURE_WATER, NATURE_SNOW});
	setNatures("2.0", true, project, new String[] {NATURE_SNOW});
	try {
		assertTrue("2.1", project.hasNature(NATURE_WATER));
		assertTrue("2.2", project.hasNature(NATURE_SNOW));
		assertTrue("2.3", project.isNatureEnabled(NATURE_WATER));
		assertTrue("2.4", project.isNatureEnabled(NATURE_SNOW));
	} catch (CoreException e) {
		fail("2.99", e);
	}

}
/**
 * Test simple addition and removal of natures.
 */
public void testSimpleNature() {
	IWorkspace ws = ResourcesPlugin.getWorkspace();
	IProject project = ws.getRoot().getProject("Project");
	ensureExistsInWorkspace(project, true);

	String[][] valid = getValidNatureSets();
	for (int i = 0; i < valid.length; i++) {
		setNatures("valid: " + i, false, project, valid[i]);
	}
	//configure a valid nature before starting invalid tests
	String[] currentSet = new String[] {NATURE_SIMPLE};
	setNatures("1.0", false, project, currentSet);

	//now do invalid tests and ensure simple nature is still configured	
	String[][] invalid = getInvalidNatureSets();
	for (int i = 0; i < invalid.length; i++) {
		setNatures("invalid: " + i, true, project, invalid[i]);
		try {
			assertTrue("2.0", project.hasNature(NATURE_SIMPLE));
			assertTrue("2.1", !project.hasNature(NATURE_EARTH));
			assertTrue("2.2", project.isNatureEnabled(NATURE_SIMPLE));
			assertTrue("2.3", !project.isNatureEnabled(NATURE_EARTH));
			assertEquals("2.4", project.getDescription().getNatureIds(), currentSet);
		} catch (CoreException e) {
			fail("2.99", e);
		}
	}
}
}