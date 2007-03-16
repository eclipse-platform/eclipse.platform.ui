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
package org.eclipse.ui.tests.encoding;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * The EncodingTestSuite is the suite for encoding tests.
 */
public class EncodingTestSuite extends TestSuite {
	
	/**
	 * Returns the suite.  This is required to
	 * use the JUnit Launcher.
	 * @return Test
	 */
	public static Test suite() {
		return new EncodingTestSuite();
	}

	/**
	 * Create the suite.
	 */
	public EncodingTestSuite() {
		super();
		addTest(new TestSuite(EncodingTestCase.class));
	}

	

}
