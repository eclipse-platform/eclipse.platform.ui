package org.eclipse.update.tests.regularInstall;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.update.tests.UpdateManagerTestCase;
import junit.framework.*;

public class AllRegularInstallTests extends UpdateManagerTestCase {
public AllRegularInstallTests(String name) {
	super(name);
}
public static Test suite() {
	TestSuite suite = new TestSuite();
	suite.setName("Regular Install Tests");
	
	// the following will take all teh test methods in teh class that start with 'test'

	suite.addTest(new TestSuite(TestInstall.class));
	suite.addTest(new TestSuite(TestGetFeature.class));
	suite.addTest(new TestSuite(TestExecutableInstall.class));	
	suite.addTest(new TestSuite(TestExecutablePackagedInstall.class));		
	suite.addTest(new TestSuite(TestDataEntryInstall.class));
	suite.addTest(new TestSuite(TestLocalSite.class));		
	
	// or you can specify the method
	//suite.addTest(new TestGetFeature("methodThatDoesNotStartWithtest"));	
	
	return suite;
}
}
