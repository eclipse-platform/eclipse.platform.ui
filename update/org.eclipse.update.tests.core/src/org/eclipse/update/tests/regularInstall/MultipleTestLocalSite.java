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
package org.eclipse.update.tests.regularInstall;

import org.eclipse.update.tests.UpdateManagerTestCase;
import junit.framework.*;


public class MultipleTestLocalSite extends UpdateManagerTestCase {
public MultipleTestLocalSite(String name) {
	super(name);
}
public static Test suite() {
	TestSuite suite = new TestSuite();
	suite.setName("Multiple Test Local Site");
	
	// the following will take all teh test methods in teh class that start with 'test'
	
	for(int i=0; i<50; i++){
		suite.addTest(new TestSuite(TestLocalSite.class));
	}
	
	return suite;
}
}
