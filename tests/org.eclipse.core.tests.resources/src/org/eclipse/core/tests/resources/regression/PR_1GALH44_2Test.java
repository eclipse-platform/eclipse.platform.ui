package org.eclipse.core.tests.resources.regression;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.tests.harness.WorkspaceSessionTest;
/**
 * 1GALH44: ITPCORE:WINNT - SEVERE: Walkback saving workspace trees
 * 
 * This class needs to be used with PR_1GALH44_1Test.
 */
public class PR_1GALH44_2Test extends WorkspaceSessionTest {
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
