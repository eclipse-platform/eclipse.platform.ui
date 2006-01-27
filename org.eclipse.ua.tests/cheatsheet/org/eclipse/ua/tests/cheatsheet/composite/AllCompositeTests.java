/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ua.tests.cheatsheet.composite;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllCompositeTests {

	public static Test suite() {
		TestSuite suite = new TestSuite(
				"Test for org.eclipse.ua.tests.cheatsheet.AllCompositeTests");
		//$JUnit-BEGIN$
		suite.addTestSuite(TestCompositeParser.class);
		suite.addTestSuite(TestEventing.class);
		//$JUnit-END$
		return suite;
	}

}
