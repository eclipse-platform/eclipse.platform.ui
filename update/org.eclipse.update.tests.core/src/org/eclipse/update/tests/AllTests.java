package org.eclipse.update.tests;

import junit.framework.*;
import org.eclipse.update.tests.api.AllAPITests;
import org.eclipse.update.tests.parser.AllParserTests;
import org.eclipse.update.tests.regularInstall.AllRegularInstallTests;

public class AllTests extends TestCase {
public AllTests(String name) {
	super(name);
}
public static Test suite() {
	TestSuite suite = new TestSuite();
	suite.setName("All Update Manager Tests");
	suite.addTest(AllRegularInstallTests.suite());
	suite.addTest(AllAPITests.suite());	
	suite.addTest(AllParserTests.suite());
	return suite;
}
}
