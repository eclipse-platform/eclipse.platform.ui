package org.eclipse.update.tests.configurations;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.update.tests.UpdateManagerTestCase;
import junit.framework.*;

public class AllConfigurationsTests extends UpdateManagerTestCase {
public AllConfigurationsTests(String name) {
	super(name);
}
public static Test suite() {
	TestSuite suite = new TestSuite();
	suite.setName("Regular Install Tests");
	
	suite.addTest(new TestSuite(TestRevert.class));
	
	return suite;
}
}
