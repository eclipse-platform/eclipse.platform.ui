package org.eclipse.core.tests.resources.regression;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.*;
import org.eclipse.core.tests.harness.EclipseWorkspaceTest;

public class PR_1GH2B0N_Test extends EclipseWorkspaceTest {
/**
 * Constructor for PR_1GH2B0N_Test
 */
public PR_1GH2B0N_Test() {
	super();
}
/**
 * Constructor for PR_1GH2B0N_Test
 */
public PR_1GH2B0N_Test(String name) {
	super(name);
}
public static Test suite() {
	return new TestSuite(PR_1GH2B0N_Test.class);
}
protected void tearDown() throws Exception {
	super.tearDown();
	ensureDoesNotExistInWorkspace(getWorkspace().getRoot());
}
public void test_1GH2B0N() {
	IPath path = new Path("c:/temp");
	IProject project = getWorkspace().getRoot().getProject("MyProject");
	IProjectDescription description = getWorkspace().newProjectDescription("MyProject");
	description.setLocation(path.append(project.getName()));
	try {
		project.create(description, getMonitor());
	} catch (CoreException e) {
		fail("1.0", e);
	}
	try {
		project.open(getMonitor());
	} catch (CoreException e) {
		fail("1.1", e);
	}
	
	IProject project2 = getWorkspace().getRoot().getProject("MyProject2");
	IStatus status = getWorkspace().validateProjectLocation(project2, project.getLocation().append(project2.getName()));
	assertTrue("2.0", !status.isOK());
}
}
