/*
 * Created on May 22, 2003
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package org.eclipse.compare.tests;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author weinand
 */
public class AllTests {

	public static Test suite() {
		TestSuite suite= new TestSuite("Test for org.eclipse.compare.tests");
		//$JUnit-BEGIN$
		suite.addTestSuite(DocLineComparatorTest.class);
		//$JUnit-END$
		return suite;
	}
}
