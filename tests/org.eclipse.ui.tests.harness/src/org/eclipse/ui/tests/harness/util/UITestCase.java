/*******************************************************************************
 * Copyright (c) 2000, 2020 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Jeanderson Candido <http://jeandersonbc.github.io> - Bug 444070
 *     Lars Vogel <Lars.Vogel@vogella.com> - Bug 474957
 *     Paul Pazderski <paul-eclipse@ppazderski.de> - Bug 546537: improve compatibility with BlockJUnit4ClassRunner
 *******************************************************************************/
package org.eclipse.ui.tests.harness.util;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * <code>UITestCase</code> is a useful super class for most
 * UI tests cases.  It contains methods to create new windows
 * and pages.  It will also automatically close the test
 * windows when the tearDown method is called.
 */
@ExtendWith(CloseTestWindowsExtension.class)
public abstract class UITestCase {

	/**
	 * Simple implementation of setUp. Subclasses are prevented from overriding this
	 * method to maintain logging consistency. doSetUp() should be overridden
	 * instead.
	 */
	@BeforeEach
	public final void setUp() throws Exception {
		doSetUp();
	}

	/**
	 * Sets up the fixture, for example, open a network connection.
	 * This method is called before a test is executed.
	 * The default implementation does nothing.
	 * Subclasses may extend.
	 */
	protected void doSetUp() throws Exception {
		// do nothing.
	}

	/**
	 * Simple implementation of tearDown. Subclasses are prevented from overriding
	 * this method to maintain logging consistency. doTearDown() should be
	 * overridden instead.
	 */
	@AfterEach
	public final void tearDown() throws Exception {
		doTearDown();
	}

	/**
	 * Tears down the fixture, for example, close a network connection.
	 * This method is called after a test is executed.
	 * The default implementation closes all test windows, processing events both before
	 * and after doing so.
	 * Subclasses may extend.
	 */
	protected void doTearDown() throws Exception {
	}

}
