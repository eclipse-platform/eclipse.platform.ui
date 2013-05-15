/*******************************************************************************
 * Copyright (c) 2009, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.e4.ui.tests.application;

import junit.framework.Test;
import junit.framework.TestSuite;

public class StartupTestSuite extends TestSuite {

	public static Test suite() {
		TestSuite suite = new StartupTestSuite();

		suite.addTestSuite(EModelServiceTest.class);
		suite.addTestSuite(EModelServiceFindTest.class);
		suite.addTestSuite(EModelServiceInsertTest.class);
		suite.addTestSuite(EPartServiceTest.class);
		suite.addTestSuite(ESelectionServiceTest.class);
		suite.addTestSuite(EventBrokerTest.class);

		suite.addTestSuite(HeadlessContactsDemoTest.class);
		suite.addTestSuite(HeadlessPhotoDemoTest.class);

		// suite.addTestSuite(UIContactsDemoTest.class);
		// suite.addTestSuite(UIPhotoDemoTest.class);

		suite.addTestSuite(UIEventsTest.class);

		return suite;
	}

}
