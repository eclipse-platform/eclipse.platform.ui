/*******************************************************************************
 * Copyright (c) 2005, 2018 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tests.internal.registry.simple;

import junit.framework.*;

public class AllSimpleRegistryTests extends TestCase {

	public AllSimpleRegistryTests() {
		super(null);
	}

	public AllSimpleRegistryTests(String name) {
		super(name);
	}

	public static Test suite() {
		TestSuite suite = new TestSuite(AllSimpleRegistryTests.class.getName());
		suite.addTestSuite(XMLExtensionCreate.class);
		suite.addTestSuite(DirectExtensionCreate.class);
		suite.addTestSuite(XMLExecutableExtension.class);
		suite.addTestSuite(DirectExtensionCreateTwoRegistries.class);
		suite.addTestSuite(TokenAccess.class);
		suite.addTestSuite(XMLExtensionCreateEclipse.class);
		suite.addTestSuite(DirectExtensionRemove.class);
		suite.addTestSuite(MergeContribution.class);
		suite.addTestSuite(DuplicatePoints.class);
		return suite;
	}
}
