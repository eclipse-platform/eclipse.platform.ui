/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.update.tests.regularRemove;

import org.eclipse.update.tests.UpdateManagerTestCase;
import junit.framework.*;


public class AllRegularRemoveTests extends UpdateManagerTestCase {
public AllRegularRemoveTests(String name) {
	super(name);
}
public static Test suite() {
	TestSuite suite = new TestSuite();
	suite.setName("Regular Remove Tests");
	
	suite.addTest(new TestSuite(TestRemove.class));
	
	return suite;
}
}
