/*******************************************************************************
 * Copyright (c) 2007, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.forms.util;

import junit.framework.Test;
import junit.framework.TestSuite;

/*
 * Tests forms performance (automated).
 */
public class AllUtilityTests extends TestSuite {

	/*
	 * Returns the entire test suite.
	 */
	public static Test suite() {
		return new AllUtilityTests();
	}

	/*
	 * Constructs a new performance test suite.
	 */
	public AllUtilityTests() {
		addTestSuite(FormImagesTests.class);
		addTestSuite(FormFontsTests.class);
		addTestSuite(FormColorsTests.class);
		addTestSuite(FormToolkitTest.class);
		addTestSuite(ImageHyperlinkTest.class);
	}
}
