/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.expressions.tests;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {
	
	public static Test suite() {
		TestSuite suite= new TestSuite("All Expression Language Tests"); //$NON-NLS-1$
		suite.addTest(PropertyTesterTests.suite());
		suite.addTest(ExpressionTests.suite());
		suite.addTest(ExpressionInfoTests.suite());
		return suite;
	}
}

