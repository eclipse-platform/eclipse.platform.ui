package org.eclipse.update.tests.regularRemove;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.update.tests.UpdateManagerTestCase;
import junit.framework.*;


public class AllRegularRemoveTests extends UpdateManagerTestCase {
public AllRegularRemoveTests(String name) {
	super(name);
}
public static Test suite() {
	TestSuite suite = new TestSuite();
	suite.setName("Regular Remove Tests");
	
	suite.addTest(new TestSuite(TestRemove.class));
	
	return suite;
}
}
