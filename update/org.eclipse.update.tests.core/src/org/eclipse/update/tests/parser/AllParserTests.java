package org.eclipse.update.tests.parser;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.update.tests.UpdateManagerTestCase;
import junit.framework.*;

public class AllParserTests extends UpdateManagerTestCase {
public AllParserTests(String name) {
	super(name);
}
public static Test suite() {
	TestSuite suite = new TestSuite();
	suite.setName("Parsing Tests");
	
	// the following will take all teh test methods in teh class that start with 'test'
	suite.addTest(new TestSuite(TestFeatureParse.class));
	suite.addTest(new TestSuite(TestSiteParse.class));	
	suite.addTest(new TestSuite(TestCategories.class));		
	suite.addTest(new TestSuite(TestSiteGeneration.class));		

	
	// or you can specify the method
	//suite.addTest(new TestGetFeature("methodThatDoesNotStartWithtest"));	
	
	return suite;
}
}
