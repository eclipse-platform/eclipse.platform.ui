package org.eclipse.update.tests.types;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.update.tests.UpdateManagerTestCase;
import junit.framework.*;

public class AllTypesTests extends UpdateManagerTestCase {
public AllTypesTests(String name) {
	super(name);
}
public static Test suite() {
	TestSuite suite = new TestSuite();
	suite.setName("Regular Install Tests");
	
	// the following will take all teh test methods in teh class that start with 'test'
	suite.addTest(new TestSuite(TestFeatureType.class));
	suite.addTest(new TestSuite(TestSiteType.class));	
	
	// or you can specify the method
	//suite.addTest(new TestGetFeature("methodThatDoesNotStartWithtest"));	
	
	return suite;
}
}
