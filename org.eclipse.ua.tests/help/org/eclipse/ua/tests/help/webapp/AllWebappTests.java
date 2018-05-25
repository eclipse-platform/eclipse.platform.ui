/*******************************************************************************
 * Copyright (c) 2006, 2016 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ua.tests.help.webapp;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/*
 * Tests utility classes and servlets used in Web Application
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
	BrowserIdentificationTest.class,
	TopicPathTest.class,
	FilterTest.class,
	PluginsRootReplacement.class,
	UrlCoderTest.class,
	UrlUtilsTests.class,
	LocaleTest.class,
	PrintSubtopics.class,
	RestrictedTopicParameter.class,
	FilterExtensionTest.class,
	FragmentServletTest.class,
	HelpServerInterrupt.class,
	HelpServerBinding.class,
	HtmlCoderTest.class,
	TocZipTest.class,
	JsonHelperTests.class,
	EclipseConnectorTests.class
})
public class AllWebappTests {
}
