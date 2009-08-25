/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ua.tests.doc;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.ua.tests.doc.internal.linkchecker.PrebuiltIndexChecker;
import org.eclipse.ua.tests.doc.internal.linkchecker.TocLinkChecker;

/*
 * Tests all user assistance functionality (automated).
 */
public class AllTests extends TestSuite {

	/*
	 * Returns the entire test suite.
	 */
	public static Test suite() {
		return new AllTests();
	}

	/*
	 * Constructs a new test suite.
	 */
	public AllTests() {
		addTestSuite(PrebuiltIndexChecker.class);
		addTestSuite(TocLinkChecker.class);
	}
}
