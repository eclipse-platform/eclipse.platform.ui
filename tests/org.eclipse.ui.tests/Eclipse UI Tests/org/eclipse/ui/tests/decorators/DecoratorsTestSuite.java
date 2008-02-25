/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.decorators;

import junit.framework.Test;
import junit.framework.TestSuite;

public class DecoratorsTestSuite extends TestSuite {

	/**
	 * Returns the suite.  This is required to
	 * use the JUnit Launcher.
	 */
	public static Test suite() {
		return new DecoratorsTestSuite();
	}

	/**
	 * Construct the test suite.
	 */
	public DecoratorsTestSuite() {
		addTest(new TestSuite(ExceptionDecoratorTestCase.class));
		addTest(new TestSuite(DecoratorTestCase.class));
		addTest(new TestSuite(LightweightDecoratorTestCase.class));
		addTest(new TestSuite(BadIndexDecoratorTestCase.class));
//		addTest(new TestSuite(DecoratorTreeTest.class));
//		addTest(new TestSuite(DecoratorTableTest.class));
//		addTest(new TestSuite(DecoratorTableTreeTest.class));
		addTest(new TestSuite(DecoratorAdaptableTests.class));
//		addTest(new TestSuite(DecoratorCacheTest.class));
	}

}
