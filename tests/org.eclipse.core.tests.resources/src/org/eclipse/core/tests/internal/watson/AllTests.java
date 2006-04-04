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
package org.eclipse.core.tests.internal.watson;

import junit.framework.*;

public class AllTests extends TestCase {
	/**
	 * AllTests constructor comment.
	 * @param name java.lang.String
	 */
	public AllTests() {
		super(null);
	}

	/**
	 * AllTests constructor comment.
	 * @param name java.lang.String
	 */
	public AllTests(String name) {
		super(name);
	}

	public static Test suite() {
		TestSuite suite = new TestSuite(AllTests.class.getName());
		suite.addTest(DeltaChainFlatteningTest.suite());
		suite.addTest(DeltaFlatteningTest.suite());
		suite.addTest(ElementTreeDeltaChainTest.suite());
		suite.addTest(ElementTreeIteratorTest.suite());
		suite.addTest(TreeFlatteningTest.suite());
		return suite;
	}
}
