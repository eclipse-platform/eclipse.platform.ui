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
 *
 */
public class PR_1G1N9GZ_2Test extends WorkspaceSessionTest {
public PR_1G1N9GZ_2Test() {
}
public PR_1G1N9GZ_2Test(String name) {
	super(name);
}
public static Test suite() {
	// we do not add the whole class because the order is important
	TestSuite suite = new TestSuite();
	suite.addTest(new PR_1G1N9GZ_2Test("testSaveWorkspace"));
	suite.addTest(new PR_1G1N9GZ_2Test("cleanUp"));
	return suite;
}
public void testSaveWorkspace() {
	try {
		getWorkspace().save(true, null);
	} catch (CoreException e) {
		fail("2.0", e);
	}
}
public void cleanUp() throws CoreException {
	ensureDoesNotExistInWorkspace(getWorkspace().getRoot());
	getWorkspace().save(true, null);
}
}