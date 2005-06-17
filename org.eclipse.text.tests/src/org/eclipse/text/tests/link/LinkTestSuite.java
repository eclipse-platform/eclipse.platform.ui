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

package org.eclipse.text.tests.link;

import junit.framework.Test;
import junit.framework.TestSuite;


/**
 * Test Suite org.eclipse.text.tests.link.
 * 
 * @since 3.0
 */
public class LinkTestSuite {

	public static Test suite() {
		TestSuite suite = new TestSuite("Test Suite org.eclipse.text.tests.link"); //$NON-NLS-1$
		//$JUnit-BEGIN$
		suite.addTestSuite(LinkedPositionGroupTest.class);
		suite.addTestSuite(LinkedPositionTest.class);
		suite.addTestSuite(InclusivePositionUpdaterTest.class);
		suite.addTestSuite(LinkedModeModelTest.class);
		//$JUnit-END$
		return suite;
	}
}
