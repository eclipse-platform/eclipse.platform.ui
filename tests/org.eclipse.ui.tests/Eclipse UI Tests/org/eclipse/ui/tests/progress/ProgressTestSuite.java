/*******************************************************************************
 * Copyright (c) 2009, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.tests.progress;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Test suite for the Progress View and related API
 * 
 * @since 3.6
 * @author Prakash G.R. (grprakash@in.ibm.com)
 * 
 */
public class ProgressTestSuite extends TestSuite {

	/**
	 * Returns the suite. This is required to use the JUnit Launcher.
	 */
	public static final Test suite() {
		return new ProgressTestSuite();
	}

	public ProgressTestSuite() {
		addTest(new TestSuite(ProgressContantsTest.class));
		addTest(new TestSuite(ProgressViewTests.class));
		addTest(new TestSuite(JobInfoTest.class));
		addTest(new TestSuite(JobInfoTestOrdering.class));
	}
}
