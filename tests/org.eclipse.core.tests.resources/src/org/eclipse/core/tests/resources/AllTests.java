package org.eclipse.core.tests.resources;

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
		suite.addTest(IFileTest.suite());
		suite.addTest(IFolderTest.suite());
		suite.addTest(IProjectTest.suite());
		suite.addTest(IResourceChangeEventTest.suite());
		suite.addTest(IResourceChangeListenerTest.suite());
		suite.addTest(IResourceDeltaTest.suite());
		suite.addTest(IResourceTest.suite());
		suite.addTest(ISynchronizerTest.suite());
		suite.addTest(IWorkspaceRootTest.suite());
		suite.addTest(IWorkspaceTest.suite());
		suite.addTest(MarkerSetTest.suite());
		suite.addTest(MarkerTest.suite());
		suite.addTest(NatureTest.suite());
		suite.addTest(ResourceURLTest.suite());
		suite.addTest(TeamPrivateMemberTest.suite());
		suite.addTest(WorkspaceTest.suite());
		return suite;
	}
}
