package org.eclipse.ant.tests.ui.performance;

import org.eclipse.ant.tests.ui.testplugin.AbstractAntUITest;
import org.eclipse.test.performance.PerformanceTestCase;

/**
 * Abstract class for ant performance tests, ensures the test project is created 
 * and ready in the test workspace.
 * 
 * @since 3.5
 */
public abstract class AbstractAntPerformanceTest extends PerformanceTestCase {

	/* (non-Javadoc)
	 * @see org.eclipse.test.performance.PerformanceTestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		AbstractAntUITest.assertProject();
	}
}
