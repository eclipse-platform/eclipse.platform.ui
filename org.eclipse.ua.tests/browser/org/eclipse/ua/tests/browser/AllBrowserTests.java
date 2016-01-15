/*******************************************************************************
 * Copyright (c) 2008, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ua.tests.browser;

import org.eclipse.ua.tests.browser.external.AllExternalBrowserTests;
import org.eclipse.ua.tests.browser.other.AllOtherBrowserTests;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/*
 * Tests all cheat sheet functionality (automated).
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
	AllExternalBrowserTests.class,
	AllOtherBrowserTests.class
})
public class AllBrowserTests {
}
