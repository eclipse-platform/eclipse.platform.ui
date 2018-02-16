/*******************************************************************************
 * Copyright (c) 2005, 2018 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ua.tests;

import org.eclipse.ua.tests.cheatsheet.AllCheatSheetTests;
import org.eclipse.ua.tests.help.AllHelpTests;
import org.eclipse.ua.tests.intro.AllIntroTests;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/*
 * Tests all user assistance functionality (automated).
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({ AllCheatSheetTests.class, AllIntroTests.class, AllHelpTests.class })
public class AllTests {
}
