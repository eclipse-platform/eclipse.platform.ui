package org.eclipse.core.tests.resources.regression;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.tests.harness.WorkspaceSessionTest;
/**
 * 1GALH44: ITPCORE:WINNT - SEVERE: Walkback saving workspace trees
 * 
 * This class needs to be used with PR_1GALH44_1Test.
 * This class needs to be used with PR_1GALH44_2Test.
 */
public class PR_1GALH44_3Test extends WorkspaceSessionTest {
public PR_1GALH44_3Test() {
}
public PR_1GALH44_3Test(String name) {
	super(name);
}
public static Test suite() {
	// we do not add the whole class because the order is important
	TestSuite suite = new TestSuite();
	suite.addTest(new PR_1GALH44_3Test("testCrashedEnvironment"));
	suite.addTest(new PR_1GALH44_3Test("cleanUp"));
	return suite;
}
public void testCrashedEnvironment() {
	try {
		getWorkspace().save(true, getMonitor());
	} catch (CoreException e) {
		fail("3.0", e);
	}
}
public void cleanUp() throws CoreException {
	ensureDoesNotExistInWorkspace(getWorkspace().getRoot());
	getWorkspace().save(true, null);
}
}
