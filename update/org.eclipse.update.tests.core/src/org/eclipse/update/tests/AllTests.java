package org.eclipse.update.tests;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.update.tests.api.AllAPITests;
import org.eclipse.update.tests.configurations.AllConfigurationsTests;
import org.eclipse.update.tests.core.boot.AllPlatformConfigurationTests;
import org.eclipse.update.tests.model.AllModelTests;
import org.eclipse.update.tests.nestedfeatures.AllNestedTests;
import org.eclipse.update.tests.parser.AllParserTests;
import org.eclipse.update.tests.reconciliation.AllReconciliationTests;
import org.eclipse.update.tests.regularInstall.AllRegularInstallTests;
import org.eclipse.update.tests.regularRemove.AllRegularRemoveTests;
import org.eclipse.update.tests.types.AllTypesTests;
import org.eclipse.update.tests.uivalues.AllCoreUITests;

public class AllTests extends TestSuite {
	public AllTests(String name) {
		super(name);
	}
	public static Test suite() throws Exception {
		TestSuite suite = new TestSuite();
		suite.setName("All Update Manager Tests");
		suite.addTest(AllPlatformConfigurationTests.suite());
		suite.addTest(AllRegularInstallTests.suite());
		suite.addTest(AllAPITests.suite());
		suite.addTest(AllModelTests.suite());
		suite.addTest(AllParserTests.suite());
		suite.addTest(AllCoreUITests.suite());
		suite.addTest(AllConfigurationsTests.suite());
		suite.addTest(AllTypesTests.suite());
		suite.addTest(AllRegularRemoveTests.suite());
		suite.addTest(AllNestedTests.suite());	
		//suite.addTest(AllReconciliationTests.suite());					
		return suite;
	}
}