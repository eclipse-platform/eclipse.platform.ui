package org.eclipse.core.tests.resources.regression;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.tests.harness.EclipseWorkspaceTest;
import junit.framework.*;
/**
 * 1GALH44: ITPCORE:WINNT - SEVERE: Walkback saving workspace trees
 * 
 * This class needs to be used with PR_1GALH44_1Test.
 */
public class PR_1GALH44_2Test extends EclipseWorkspaceTest {
public PR_1GALH44_2Test() {
}
public PR_1GALH44_2Test(String name) {
	super(name);
}
public static Test suite() {
	// we do not add the whole class because the order is important
	TestSuite suite = new TestSuite();
	suite.addTest(new PR_1GALH44_2Test("testCrashEnvironment"));
	return suite;
}
public void testCrashEnvironment() {
	IProject project = getWorkspace().getRoot().getProject("MyProject");
	IFile file = project.getFile("foo.txt");
	try {
		file.setContents(getRandomContents(), true, true, getMonitor());
	} catch (CoreException e) {
		fail("1.0", e);
	}

	// crash
	System.exit(-1);
}
}
