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
package org.eclipse.compare.tests;

import junit.framework.Test;
import junit.framework.TestSuite;


/**
 * Test some non-UI areas of the compare plugin.
 */
public class AllTests {

	public static Test suite() {
		TestSuite suite= new TestSuite("Test for org.eclipse.compare.tests"); //$NON-NLS-1$
		//$JUnit-BEGIN$
		suite.addTestSuite(UITest.class);
		//suite.addTestSuite(StreamMergerTest.class);
		//suite.addTestSuite(DocLineComparatorTest.class);
		//suite.addTestSuite(FilterTest.class);
		//$JUnit-END$
		return suite;
	}
}
