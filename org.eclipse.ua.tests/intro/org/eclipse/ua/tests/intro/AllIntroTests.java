/*******************************************************************************
 *  Copyright (c) 2005, 2016 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ua.tests.intro;

import org.eclipse.ua.tests.intro.anchors.ExtensionReorderingTest;
import org.eclipse.ua.tests.intro.contentdetect.ContentDetectorTest;
import org.eclipse.ua.tests.intro.other.AllOtherTests;
import org.eclipse.ua.tests.intro.parser.AllParserTests;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/*
 * Tests all intro (welcome) functionality (automated).
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
	AllParserTests.class, ContentDetectorTest.class,ExtensionReorderingTest.class,AllOtherTests.class
})
public class AllIntroTests {
}
