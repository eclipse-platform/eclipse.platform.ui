/*******************************************************************************
 *  Copyright (c) 2008, 2009 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ua.tests.intro.other;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/*
 * Tests all intro parser functionality (automated).
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
	ReopenStateTest.class, NormalizeWhitespaceTest.class
})
public class AllOtherTests {
}
