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

package org.eclipse.ua.tests.help.other;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
	LinkUtilTest.class,
	TestEscapeUtils.class,
	ContextLinkSorter.class,
	UAElementTest.class,
	ContextTest.class,
	TopicTest.class,
	TocObjectTest.class,
	DocumentReaderTest.class,
	EntityResolutionTest.class,
	ResourceTest.class,
	ConcurrentTocAccess.class,
	XHTMLEntityTest.class,
	PathResolutionTest.class,
	IndexEntryTest.class,
	IndexSeeTest.class
})
public class AllOtherHelpTests {
}
