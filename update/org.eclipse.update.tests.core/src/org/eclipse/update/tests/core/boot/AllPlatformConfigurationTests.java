package org.eclipse.update.tests.core.boot;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllPlatformConfigurationTests
	extends PlatformConfigurationTestCase {
	/**
	 * Constructor
	 */
	public AllPlatformConfigurationTests(String name) {
		super(name);
	}
	
	/**
	 * List of API tests
	 */
	public static Test suite() throws Exception {
		TestSuite suite = new TestSuite();
		suite.setName("PlatformConfiguration Tests");

		suite.addTest(new TestSuite(TestPlatCfgAPI.class));

		return suite;
	}

}

