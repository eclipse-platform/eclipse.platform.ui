/*******************************************************************************
 * Copyright (c) 2000, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Sergey Prigogin (Google) - [338010] Resource.createLink() does not preserve symbolic links
 *******************************************************************************/
package org.eclipse.core.tests.internal.utils;

import junit.framework.*;

public class AllTests extends TestCase {
	public static Test suite() {
		TestSuite suite = new TestSuite(AllTests.class.getName());
		suite.addTest(ObjectMapTest.suite());
		suite.addTest(CacheTest.suite());
		suite.addTest(FileUtilTest.suite());
		return suite;
	}
}
