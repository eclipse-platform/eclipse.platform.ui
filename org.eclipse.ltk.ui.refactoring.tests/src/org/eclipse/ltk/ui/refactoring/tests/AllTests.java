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
package org.eclipse.ltk.ui.refactoring.tests;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {
	
	public static Test suite() {
		TestSuite suite= new TestSuite("All LTK Refactoring UI Tests"); //$NON-NLS-1$
		suite.addTestSuite(EmptySuite.class);
		return suite;
	}
}

