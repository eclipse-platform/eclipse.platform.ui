package org.eclipse.core.tests.resources.regression;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.tests.harness.EclipseWorkspaceTest;
import org.eclipse.core.tests.resources.ResourceDeltaVerifier;

public class PR_1GEAB3C_Test extends EclipseWorkspaceTest {
	ResourceDeltaVerifier verifier;

	protected static final String VERIFIER_NAME = "TestListener";

public PR_1GEAB3C_Test() {
}
public PR_1GEAB3C_Test(String name) {
	super(name);
}
public void assertDelta() {
	assertTrue(verifier.getMessage(), verifier.isDeltaValid());
}
/**
 * Runs code to handle a core exception
 */
protected void handleCoreException(CoreException e) {
	assertTrue("CoreException: " + e.getMessage(), false);
}
/**
 * Sets up the fixture, for example, open a network connection.
 * This method is called before a test is executed.
 */
protected void setUp() throws Exception {
	super.setUp();
	verifier = new ResourceDeltaVerifier();
	getWorkspace().addResourceChangeListener(verifier);
}
public static Test suite() {
	return new TestSuite(PR_1GEAB3C_Test.class);
}
/**
 * Tears down the fixture, for example, close a network connection.
 * This method is called after a test is executed.
 */
protected void tearDown() throws Exception {
	super.tearDown();
	ensureDoesNotExistInWorkspace(getWorkspace().getRoot());
	getWorkspace().removeResourceChangeListener(verifier);
}
/*
 * Ensure that we get ADDED and OPEN in the delta when we create and open
 * a project in a workspace runnable.
 */
public void test_1GEAB3C() {
	verifier.reset();
	final IProject project = getWorkspace().getRoot().getProject("MyAddedAndOpenedProject");
	verifier.addExpectedChange(project, IResourceDelta.ADDED, IResourceDelta.OPEN);
	verifier.addExpectedChange(project.getFile(".project"), IResourceDelta.ADDED, 0);
	IWorkspaceRunnable body = new IWorkspaceRunnable() {
		public void run (IProgressMonitor monitor) throws CoreException {
			monitor.beginTask("Creating and deleting", 100);
			try {
				project.create(new SubProgressMonitor(monitor, 50));
				project.open(new SubProgressMonitor(monitor, 50));
			} finally {
				monitor.done();
			}
		}
	};
	try {
		getWorkspace().run(body, getMonitor());
	} catch (CoreException e) {
		fail("1.1", e);
	}
	assertDelta();
}
}
