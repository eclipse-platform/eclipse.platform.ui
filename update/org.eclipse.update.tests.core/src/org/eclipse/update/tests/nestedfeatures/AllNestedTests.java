package org.eclipse.update.tests.nestedfeatures;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.update.tests.UpdateManagerTestCase;
import junit.framework.*;

public class AllNestedTests extends UpdateManagerTestCase {
public AllNestedTests(String name) {
	super(name);
}
public static Test suite() {
	TestSuite suite = new TestSuite();
	suite.setName("Nested Install Tests");
	
	// the following will take all teh test methods in teh class that start with 'test'

	suite.addTest(new TestSuite(TestInstall.class));

	// or you can specify the method
	//suite.addTest(new TestGetFeature("methodThatDoesNotStartWithtest"));	
	
	return suite;
}
}
