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
package org.eclipse.update.tests.configurations;

import org.eclipse.update.tests.UpdateManagerTestCase;
import junit.framework.*;


public class AllConfigurationsTests extends UpdateManagerTestCase {
public AllConfigurationsTests(String name) {
	super(name);
}
public static Test suite() {
	TestSuite suite = new TestSuite();
	suite.setName("Regular Install Tests");
	
//	suite.addTest(new TestSuite(TestRevert.class));
	suite.addTest(new TestSuite(TestBackward.class));
		
	return suite;
}
}
