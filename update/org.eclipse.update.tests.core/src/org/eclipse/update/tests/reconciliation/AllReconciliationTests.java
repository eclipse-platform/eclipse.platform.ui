package org.eclipse.update.tests.reconciliation;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.update.core.SiteManager;
import org.eclipse.update.internal.core.UpdateManagerUtils;
import org.eclipse.update.tests.UpdateManagerTestCase;
import java.io.File;
import junit.framework.*;

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