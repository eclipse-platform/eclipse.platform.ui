/*
 * (c) Copyright 2001 MyCorporation.
 * All Rights Reserved.
 */
package org.eclipse.ui.tests.adaptable;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * The AdaptableTestSuite is the TestSuite for the
 * adaptable support in the UI.
 */
public class AdaptableTestSuite extends TestSuite {
	
	/**
	 * Returns the suite.  This is required to
	 * use the JUnit Launcher.
	 */
	public static Test suite() {
		return new AdaptableTestSuite();
	}

	/**
	 * Constructor for AdaptableTestSuite.
	 */
	public AdaptableTestSuite() {
		addTest(new TestSuite(AdaptableDecoratorTestCase.class));
	}

	

}
