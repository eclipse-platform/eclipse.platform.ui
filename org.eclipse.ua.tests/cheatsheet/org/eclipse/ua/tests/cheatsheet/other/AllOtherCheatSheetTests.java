/*******************************************************************************
 * Copyright (c) 2006, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ua.tests.cheatsheet.other;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Unit tests for cheatsheets which don't fill into any other category
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
	TestStatePersistence.class,
	TestEscape.class,
	TestCheatSheetManager.class,
	TestCheatSheetCollection.class,
	TestCheatSheetCategories.class
})
public class AllOtherCheatSheetTests {
}
