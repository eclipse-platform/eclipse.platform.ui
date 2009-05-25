/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.update.tests.api;
import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.update.tests.UpdateManagerTestCase;

/**
 * Manages the API tests
 */
public class AllAPITests extends UpdateManagerTestCase {
	/**
	 * Constructor
	 */
	public AllAPITests(String name) {
		super(name);
	}
	
	/**
	 * List of API tests
	 */
	public static Test suite() throws Exception {
		TestSuite suite = new TestSuite();
		suite.setName("API Tests");

		suite.addTest(new TestSuite(TestSiteAPI.class));
		suite.addTest(new TestSuite(TestLocalSiteAPI.class));		
		suite.addTest(new TestSuite(TestUpdateManagerUtilsAPI.class));		
		suite.addTest(new TestSuite(TestDefaultExecutableFeatureAPI.class));
		suite.addTest(new TestSuite(TestDefaultPackageFeatureAPI.class));
		suite.addTest(new TestSuite(TestPluginContainerAPI.class));
		suite.addTest(new TestSuite(TestSiteManagerAPI.class));		
		suite.addTest(new TestSuite(TestNestedFeatureAPI.class));
		suite.addTest(new TestSuite(TestFeatureContentProvider_FileFilterAPI.class));
		return suite;
	}
}
