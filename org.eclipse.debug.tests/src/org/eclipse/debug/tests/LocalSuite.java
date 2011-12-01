/*******************************************************************************
 * Copyright (c) 2009, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.tests;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipe.debug.tests.viewer.model.ColumnPresentationTests;
import org.eclipe.debug.tests.viewer.model.JFaceViewerCheckTests;
import org.eclipe.debug.tests.viewer.model.JFaceViewerContentTests;
import org.eclipe.debug.tests.viewer.model.JFaceViewerDeltaTests;
import org.eclipe.debug.tests.viewer.model.JFaceViewerFilterTests;
import org.eclipe.debug.tests.viewer.model.JFaceViewerLazyTests;
import org.eclipe.debug.tests.viewer.model.JFaceViewerSelectionTests;
import org.eclipe.debug.tests.viewer.model.JFaceViewerStateTests;
import org.eclipe.debug.tests.viewer.model.JFaceViewerTopIndexTests;
import org.eclipe.debug.tests.viewer.model.JFaceViewerUpdateTests;

/**
 * Tests to run locally.  They require a user terminal to execute correctly 
 * and have frequent issues when run on build machine. 
 * 
 * @since 3.7
 */
public class LocalSuite extends TestSuite {

	/**
	 * Returns the suite.  This is required to use the JUnit Launcher.
	 * 
	 * @return the test suite
	 */
	public static Test suite() {
		return new LocalSuite();
	}
	
	/**
	 * Constructs the automated test suite. Adds all tests. 
	 */
	public LocalSuite() {
		// JFace viewer tests
		addTest(new TestSuite(JFaceViewerCheckTests.class));
		addTest(new TestSuite(JFaceViewerContentTests.class));
		addTest(new TestSuite(JFaceViewerDeltaTests.class));
		addTest(new TestSuite(JFaceViewerSelectionTests.class));
		addTest(new TestSuite(JFaceViewerStateTests.class));
		addTest(new TestSuite(JFaceViewerUpdateTests.class));
        addTest(new TestSuite(JFaceViewerLazyTests.class));
        addTest(new TestSuite(JFaceViewerTopIndexTests.class));
        addTest(new TestSuite(JFaceViewerFilterTests.class));
        addTest(new TestSuite(ColumnPresentationTests.class));
	}
}
