package org.eclipse.core.tests.resources.usecase;

import junit.framework.*;

public class AllTests extends TestCase {
/**
 * AllTests constructor comment.
 * @param name java.lang.String
 */
public AllTests() {
	super(null);
}
/**
 * AllTests constructor comment.
 * @param name java.lang.String
 */
public AllTests(String name) {
	super(name);
}
public static Test suite() {
	TestSuite suite = new TestSuite();
	suite.addTest(BuilderUseCaseTest.suite());
	suite.addTest(ConcurrencyTest.suite());
	suite.addTest(IFileTest.suite());
	suite.addTest(IFolderTest.suite());
	suite.addTest(IProjectTest.suite());
	suite.addTest(IWorkspaceRunnableUseCaseTest.suite());
	return suite;
}
}
