package org.eclipse.update.tests.sitevalidation;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.update.tests.UpdateManagerTestCase;
import junit.framework.*;

public class AllSiteValidationTests extends UpdateManagerTestCase {
	public AllSiteValidationTests(String name) {
		super(name);
	}
	public static Test suite() {
		TestSuite suite = new TestSuite();
		suite.setName("Site Validation Tests");

		// the following will take all teh test methods in teh class that start with 'test'

		suite.addTest(new TestSuite(TestSiteValidation.class));

		return suite;
	}
}