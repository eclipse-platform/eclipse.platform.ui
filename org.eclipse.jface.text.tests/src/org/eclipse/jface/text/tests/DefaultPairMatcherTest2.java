/*******************************************************************************
 * Copyright (c) 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.text.tests;

import junit.framework.Test;
import junit.framework.TestSuite;


/**
 * Tests for the default pair matcher.
 * 
 * @since 3.8
 */
public class DefaultPairMatcherTest2 extends AbstractPairMatcherTest {


	public DefaultPairMatcherTest2() {
		super(true);
	}

	public static Test suite() {
		return new TestSuite(DefaultPairMatcherTest2.class);
	}

	/** Tests that the test case reader works */
	public void testTestCaseReader() {
		performReaderTest("#( )%", 2, 0, "( )");
		performReaderTest("( )%", 2, -1, "( )");
	}
}