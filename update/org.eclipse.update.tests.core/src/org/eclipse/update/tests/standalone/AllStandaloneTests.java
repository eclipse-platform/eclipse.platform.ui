/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.update.tests.standalone;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.update.tests.UpdateManagerTestCase;


public class AllStandaloneTests extends UpdateManagerTestCase{

	public AllStandaloneTests(String name){
		super(name);
	}
	
	public static Test suite(){
		TestSuite suite = new TestSuite();
		suite.setName("Standalone Tests");
		
		// the following will take all the test methods in 
		// the class that start with "test"
		suite.addTest(new TestSuite(TestFeatureInstall.class));
		suite.addTest(new TestSuite(TestFeatureUpdate.class));
		suite.addTest(new TestSuite(TestFeatureEnable.class));
		suite.addTest(new TestSuite(TestFeatureDisable.class));
		suite.addTest(new TestSuite(TestFeatureUninstall.class));
		suite.addTest(new TestSuite(TestBundlesInstall.class));
		//and this one's for fun :)
//		suite.addTest(new TestSuite(TestSiteSearch.class));
		
		return suite;
	}
}
