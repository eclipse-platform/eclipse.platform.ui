package org.eclipse.ui.tests.datatransfer;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * .
 */
public class DataTransferTestSuite extends TestSuite {

	/**
	 * Returns the suite.  This is required to
	 * use the JUnit Launcher.
	 */
	public static Test suite() {
		return new DataTransferTestSuite();
	}
	
	/**
	 * Construct the test suite.
	 */
	public DataTransferTestSuite() {
		addTest(new TestSuite(ImportOperationTest.class));		
	}
}