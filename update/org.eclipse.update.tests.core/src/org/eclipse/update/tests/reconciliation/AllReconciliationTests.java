package org.eclipse.update.tests.reconciliation;
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
public class AllReconciliationTests extends UpdateManagerTestCase {
	/**
	 * Constructor
	 */
	public AllReconciliationTests(String name) {
		super(name);
	}
	
	/**
	 * List of API tests
	 */
	public static Test suite() throws Exception {
		TestSuite suite = new TestSuite();
		suite.setName("API Tests");


		suite.addTest(new TestSuite(TestSiteReconciliation.class));

		return suite;
	}
}