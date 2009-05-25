/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.update.tests.mirror;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.update.tests.UpdateManagerTestCase;

public class AllMirrorTests extends UpdateManagerTestCase {
	
	public AllMirrorTests(String name){
		super(name);
	}
	
	public static Test suite(){
		TestSuite suite = new TestSuite();
		suite.setName("Mirror Tests");
		
		// the following will take all the test methods in the class that starts with "test"
		
		suite.addTest(new TestSuite(TestRemoteFeatureVersionMirror.class));
		suite.addTest(new TestSuite(TestRemoteEmbeddedFeatureMirror.class));
		suite.addTest(new TestSuite(TestRemoteDoubleEmbeddedFeatureMirror.class));
		
		// or you can specify the method
		//suite.addTest(new TestGetFeature("methodThatDoesNotStartWithtest"));
		return suite;	
	}

}
