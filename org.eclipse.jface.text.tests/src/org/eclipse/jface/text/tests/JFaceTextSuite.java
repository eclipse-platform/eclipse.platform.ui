/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.text.tests;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Test JFace/Text.
 */
public class JFaceTextSuite extends TestSuite {

	/**
	 * Returns the suite.  This is required to use the JUnit Launcher.
	 */
	public static Test suite() {
		return new JFaceTextSuite();
	}

	/**
	 * Construct the test suite.
	 */
	public JFaceTextSuite() {
		addTest(TextHoverPopupTest.suite());
		addTest(TextPresentationTest.suite());
		addTest(TextUtilitiesTest.suite());
		addTest(UndoManagerTest.suite());
	}
}
