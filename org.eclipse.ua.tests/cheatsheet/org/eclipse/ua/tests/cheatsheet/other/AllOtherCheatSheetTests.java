/*******************************************************************************
 * Copyright (c) 2006, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ua.tests.cheatsheet.other;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Unit tests for cheatsheets which don't fill into any other category
 */
public class AllOtherCheatSheetTests {

	public static Test suite() {
		TestSuite suite = new TestSuite(
				"org.eclipse.ua.tests.cheatsheet.AllOtherCheatSheetTests");
		//$JUnit-BEGIN$
		suite.addTestSuite(TestStatePersistence.class);
		suite.addTestSuite(TestEscape.class);
		suite.addTestSuite(TestCheatSheetManager.class);
		suite.addTestSuite(TestCheatSheetCollection.class);
		suite.addTestSuite(TestCheatSheetCategories.class);
		//$JUnit-END$
		return suite;
	}

}
