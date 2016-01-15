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
package org.eclipse.ua.tests.cheatsheet.parser;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/*
 * Tests all cheat sheet parser functionality (automated).
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
	ValidTest.class, TolerateTest.class, InvalidCheatsheet.class, ParseFromString.class, NoError.class
})
public class AllParserTests {
}
