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
package org.eclipse.core.tests.filesystem;

import junit.framework.*;

/**
 * Class for collecting all test classes that deal with the file system API.
 */
public class AllTests extends TestCase {
	public AllTests() {
		super(null);
	}

	public AllTests(String name) {
		super(name);
	}

	public static Test suite() {
		TestSuite suite = new TestSuite(AllTests.class.getName());
		suite.addTestSuite(CreateDirectoryTest.class);
		suite.addTestSuite(DeleteTest.class);
		suite.addTestSuite(OpenOutputStreamTest.class);
		suite.addTestSuite(PutInfoTest.class);
		return suite;
	}
}
