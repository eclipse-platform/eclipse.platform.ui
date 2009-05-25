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
package org.eclipse.update.tests.nestedfeatures;
import org.eclipse.update.tests.UpdateManagerTestCase;
import junit.framework.*;

public class AllNestedTests extends UpdateManagerTestCase {
public AllNestedTests(String name) {
	super(name);
}
public static Test suite() {
	TestSuite suite = new TestSuite();
	suite.setName("Nested Install Tests");
	
	// the following will take all teh test methods in teh class that start with 'test'

	suite.addTest(new TestSuite(TestInstall.class));

	// or you can specify the method
	//suite.addTest(new TestGetFeature("methodThatDoesNotStartWithtest"));	
	
	return suite;
}
}
