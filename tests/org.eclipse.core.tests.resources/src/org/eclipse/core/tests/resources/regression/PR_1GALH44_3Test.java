package org.eclipse.core.tests.resources.regression;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.tests.harness.EclipseWorkspaceTest;
import junit.framework.*;
/**
 * 1GALH44: ITPCORE:WINNT - SEVERE: Walkback saving workspace trees
 * 
 * This class needs to be used with PR_1GALH44_1Test.
 * This class needs to be used with PR_1GALH44_2Test.
 */
public class PR_1GALH44_3Test extends EclipseWorkspaceTest {
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
	IProject[] projects = getWorkspace().getRoot().getProjects();
	getWorkspace().delete(projects, true, null);
}
}
