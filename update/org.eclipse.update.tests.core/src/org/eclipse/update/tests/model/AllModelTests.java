package org.eclipse.update.tests.model;
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