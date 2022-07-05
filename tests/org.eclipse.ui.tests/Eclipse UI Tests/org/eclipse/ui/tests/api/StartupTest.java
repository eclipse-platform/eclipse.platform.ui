/*******************************************************************************
 * Copyright (c) 2003, 2006 IBM Corporation and others.
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
 *******************************************************************************/
package org.eclipse.ui.tests.api;

import static org.junit.Assert.assertTrue;

import org.eclipse.ui.tests.TestPlugin;
import org.eclipse.ui.tests.harness.util.UITestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class StartupTest extends UITestCase {

	/**
	 * Construct an instance.
	 */
	public StartupTest() {
		super(StartupTest.class.getSimpleName());
	}

	@Test
	public void testStartup() {
		assertTrue("Startup - explicit", StartupClass.getEarlyStartupCalled());
		assertTrue("Startup - implicit", TestPlugin.getEarlyStartupCalled());
		assertTrue("Startup - completed before tests", StartupClass.getEarlyStartupCompleted());
	}

	@Override
	protected void doTearDown() throws Exception {
		super.doTearDown();
		// NOTE:  tearDown will run after each test.  Therefore, we
		// only want one test in this suite (or the values set when
		// this plugin started up will be lost).
		StartupClass.reset();
		TestPlugin.clearEarlyStartup();
	}
}
