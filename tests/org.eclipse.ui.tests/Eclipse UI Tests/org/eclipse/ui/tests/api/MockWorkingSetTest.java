package org.eclipse.ui.tests.api;

import junit.framework.TestCase;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.registry.WorkingSetDescriptor;
import org.eclipse.ui.internal.registry.WorkingSetRegistry;

/**
 * Tests the WorkingSetDescriptor and WorkingSetRegistry.
 */
public class MockWorkingSetTest extends TestCase {
	final static String WORKING_SET_ID = "org.eclipse.ui.tests.api.MockWorkingSet";
	final static String WORKING_SET_NAME = "Mock Working Set";
	final static String WORKING_SET_PAGE_CLASS_NAME = "org.eclipse.ui.tests.api.MockWorkingSetPage";

	WorkingSetRegistry fRegistry;

	public MockWorkingSetTest(String name) {
		super(name);
	}
	/**
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		fRegistry = WorkbenchPlugin.getDefault().getWorkingSetRegistry();
	}
	public void testWorkingSetDescriptor() throws Throwable {
		WorkingSetDescriptor workingSetDescriptor = fRegistry.getWorkingSetDescriptor(WORKING_SET_ID);

		assertNotNull(workingSetDescriptor.getIcon());		
		assertEquals(WORKING_SET_ID, workingSetDescriptor.getId());
		assertEquals(WORKING_SET_NAME, workingSetDescriptor.getName());
		assertEquals(WORKING_SET_PAGE_CLASS_NAME, workingSetDescriptor.getPageClassName());		
	}
	public void testWorkingSetRegistry() throws Throwable {
		WorkingSetDescriptor[] workingSetDescriptors = fRegistry.getWorkingSetDescriptors();
		/*
		 * Should have at least resourceWorkingSetPage and MockWorkingSet
		 */
		assertTrue(workingSetDescriptors.length >= 2);

		assertEquals(Class.forName(WORKING_SET_PAGE_CLASS_NAME), fRegistry.getWorkingSetPage(WORKING_SET_ID).getClass());
	}

}

