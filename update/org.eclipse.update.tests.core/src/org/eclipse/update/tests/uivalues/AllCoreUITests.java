package org.eclipse.update.tests.uivalues;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.update.tests.UpdateManagerTestCase;
import junit.framework.*;

public class AllCoreUITests extends UpdateManagerTestCase {
public AllCoreUITests(String name) {
	super(name);
}
public static Test suite() {
	TestSuite suite = new TestSuite();
	suite.setName("Regular Install Tests");
	
	// the following will take all teh test methods in teh class that start with 'test'
	//suite.addTest(new TestSuite(TestUILabel.class));
	
	// or you can specify the method
	//suite.addTest(new TestGetFeature("methodThatDoesNotStartWithtest"));	
	
	return suite;
}
}
