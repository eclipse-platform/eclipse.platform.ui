package org.eclipse.core.tests.resources.regression;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.tests.harness.EclipseWorkspaceTest;
import junit.framework.*;
/**
 *
 */
public class PR_1G1N9GZ_2Test extends EclipseWorkspaceTest {
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
	IProject[] projects = getWorkspace().getRoot().getProjects();
	getWorkspace().delete(projects, true, null);
}
}
