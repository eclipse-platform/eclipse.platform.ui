package org.eclipse.ui.tests.api;
import junit.framework.*;
import junit.textui.TestRunner;

/**
 * Test all areas of the UI API.
 */
public class ApiTestSuite extends TestSuite {
	/**
	 * Construct the test suite.
	 */
	public ApiTestSuite() {
		addTest(new TestSuite(PlatformUITest.class));
		addTest(new TestSuite(IWorkbenchTest.class));
		addTest(new TestSuite(IWorkbenchWindowTest.class));
		addTest(new TestSuite(IWorkbenchPageTest.class));
		addTest(new TestSuite(IPageListenerTest.class));
		addTest(new TestSuite(IPageServiceTest.class));
		addTest(new TestSuite(IPerspectiveRegistryTest.class));
		//		addTest( new TestSuite( IPerspectiveDescriptorTest.class ) );
		//		addTest( new TestSuite( IFileEditorMappingTest.class ) );
		//		addTest( new TestSuite( IEditorDescriptorTest.class ) );
		//		addTest( new TestSuite( IEditorRegistryTest.class ) );
		//		addTest( new TestSuite( IPerspectiveListenerTest.class ) );
	}
}