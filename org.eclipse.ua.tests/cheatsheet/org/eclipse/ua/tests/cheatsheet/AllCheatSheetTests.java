/*******************************************************************************
 * Copyright (c) 2005, 2016 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ua.tests.cheatsheet;

import org.eclipse.ua.tests.cheatsheet.composite.AllCompositeTests;
import org.eclipse.ua.tests.cheatsheet.execution.AllExecutionTests;
import org.eclipse.ua.tests.cheatsheet.other.AllOtherCheatSheetTests;
import org.eclipse.ua.tests.cheatsheet.parser.AllParserTests;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/*
 * Tests all cheat sheet functionality (automated).
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({ AllParserTests.class, AllExecutionTests.class, AllCompositeTests.class,
		AllOtherCheatSheetTests.class })
public class AllCheatSheetTests {
}
