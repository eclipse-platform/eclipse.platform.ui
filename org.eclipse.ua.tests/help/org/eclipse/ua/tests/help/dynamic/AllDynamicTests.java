/*******************************************************************************
 * Copyright (c) 2006, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ua.tests.help.dynamic;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/*
 * Tests help dynamic content functionality (automated).
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
	XMLProcessorTest.class,
	DynamicXHTMLProcessorTest.class
})
public class AllDynamicTests {
}
