package org.eclipse.ui.tests.api;
import junit.framework.*;
import junit.textui.TestRunner;
import org.eclipse.swt.SWT;

/**
 * Test all areas of the UI API.
 */
public class ApiTestSuite extends TestSuite {

	/**
	 * Returns the suite.  This is required to
	 * use the JUnit Launcher.
	 */
	public static Test suite() {
		return new ApiTestSuite();
	}
	
	/**
	 * Construct the test suite.
	 */
	public ApiTestSuite() {
		addTest(new TestSuite(PlatformUITest.class));
		addTest(new TestSuite(IWorkbenchTest.class));
		addTest(new TestSuite(IWorkbenchWindowTest.class));
		addTest(new TestSuite(IWorkbenchPageTest.class));
		addTest(new TestSuite(IActionFilterTest.class));
		addTest(new TestSuite(IPageListenerTest.class));
		addTest(new TestSuite(IPageServiceTest.class));
		addTest(new TestSuite(IPerspectiveRegistryTest.class));
		addTest(new TestSuite(IPerspectiveDescriptorTest.class));
		addTest(new TestSuite(IFileEditorMappingTest.class));
		addTest(new TestSuite(IEditorDescriptorTest.class));
		addTest(new TestSuite(IEditorRegistryTest.class));
		addTest(new TestSuite(IPerspectiveListenerTest.class));
		addTest(new TestSuite(IWorkbenchWindowActionDelegateTest.class));
		addTest(new TestSuite(IViewActionDelegateTest.class));
		addTest(new TestSuite(IViewSiteTest.class));
		addTest(new TestSuite(IEditorSiteTest.class));
		addTest(new TestSuite(IActionBarsTest.class));
		addTest(new TestSuite(IViewPartTest.class));
		addTest(new TestSuite(IEditorPartTest.class));
		addTest(new TestSuite(IEditorActionBarContributorTest.class));
		addTest(new TestSuite(ISelectionServiceTest.class));
		addTest(new TestSuite(IWorkingSetTest.class));
		addTest(new TestSuite(IWorkingSetManagerTest.class));
		addTest(new TestSuite(MockWorkingSetTest.class));		
	}
}