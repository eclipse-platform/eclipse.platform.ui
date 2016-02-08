/*******************************************************************************
 * Copyright (c) 2011, 2016 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ua.tests.help.webapp.service;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/*
 * Tests utility classes and servlets used in Web Application
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
	AdvancedSearchServiceTest.class,
	ContentServiceTest.class,
	ContextServiceTest.class,
	ExtensionServiceTest.class,
	IndexFragmentServiceTest.class,
	IndexServiceTest.class,
	SearchServiceTest.class,
	TocFragmentServiceTest.class,
	TocServiceTest.class
})
public class AllWebappServiceTests {
}
