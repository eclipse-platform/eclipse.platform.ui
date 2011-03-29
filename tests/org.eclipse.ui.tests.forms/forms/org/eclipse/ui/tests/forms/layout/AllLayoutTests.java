/*******************************************************************************
 * Copyright (c) 2007, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.tests.forms.layout;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllLayoutTests {

	public static Test suite() {
		TestSuite suite = new TestSuite(
				"org.eclipse.ua.tests.forms.AllLayoutTests");
		//$JUnit-BEGIN$
		suite.addTestSuite(TestTableWrapLayout.class);
		suite.addTestSuite(TestColumnWrapLayout.class);
		//$JUnit-END$
		return suite;
	}

}
