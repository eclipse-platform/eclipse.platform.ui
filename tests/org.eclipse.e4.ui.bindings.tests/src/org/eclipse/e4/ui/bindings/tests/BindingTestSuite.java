package org.eclipse.e4.ui.bindings.tests;

import junit.framework.Test;
import junit.framework.TestSuite;

public class BindingTestSuite extends TestSuite {
	public static Test suite() {
		return new BindingTestSuite();
	}
	
	public BindingTestSuite() {
		addTestSuite(BindingLookupTest.class);
		addTestSuite(KeyDispatcherTest.class);
		addTestSuite(BindingTableTests.class);
		addTestSuite(BindingCreateTest.class);
	}
}
