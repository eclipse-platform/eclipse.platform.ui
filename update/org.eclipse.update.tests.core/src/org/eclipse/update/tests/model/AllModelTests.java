package org.eclipse.update.tests.model;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.update.tests.UpdateManagerTestCase;

/**
 * Manages the API tests
 */
public class AllModelTests extends UpdateManagerTestCase {
	/**
	 * Constructor
	 */
	public AllModelTests(String name) {
		super(name);
	}
	
	/**
	 * List of API tests
	 */
	public static Test suite() throws Exception {
		TestSuite suite = new TestSuite();
		suite.setName("Model Tests");

		suite.addTest(new TestSuite(FeatureMain.class));
		suite.addTest(new TestSuite(SiteMain.class));		

		return suite;
	}
}