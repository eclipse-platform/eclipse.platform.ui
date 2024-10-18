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

import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.tests.TestPlugin;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class StartupTest {

	@Test
	public void testStartup() {
		assertTrue("Startup - explicit", StartupClass.getEarlyStartupCalled());
		assertTrue("Startup - implicit", TestPlugin.getEarlyStartupCalled());
		assertTrue("Startup - completed before tests", StartupClass.getEarlyStartupCompleted());
	}

	@Before
	public void doSetUp() {
		PlatformUI.getWorkbench();
	}

	@After
	public void doTearDown() throws Exception {
		// NOTE:  tearDown will run after each test.  Therefore, we
		// only want one test in this suite (or the values set when
		// this plugin started up will be lost).
		StartupClass.reset();
		TestPlugin.clearEarlyStartup();
	}
}
