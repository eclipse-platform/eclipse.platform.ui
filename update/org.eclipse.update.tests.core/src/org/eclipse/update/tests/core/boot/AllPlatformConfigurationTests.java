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
package org.eclipse.update.tests.core.boot;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllPlatformConfigurationTests
	extends PlatformConfigurationTestCase {
	/**
	 * Constructor
	 */
	public AllPlatformConfigurationTests(String name) {
		super(name);
	}
	
	/**
	 * List of API tests
	 */
	public static Test suite() throws Exception {
		TestSuite suite = new TestSuite();
		suite.setName("PlatformConfiguration Tests");

		suite.addTest(new TestSuite(TestPlatCfgAPI.class));

		return suite;
	}

}

