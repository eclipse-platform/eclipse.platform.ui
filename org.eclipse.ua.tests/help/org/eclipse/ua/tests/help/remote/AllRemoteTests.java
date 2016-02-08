/*******************************************************************************
 * Copyright (c) 2009, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ua.tests.help.remote;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/*
 * Tests help keyword index functionality.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
	RemotePreferenceTest.class,
	TocServletTest.class,
	SearchServletTest.class,
	IndexServletTest.class,
	ContentServletTest.class,
	ContextServletTest.class,
	LoadTocUsingRemoteHelp.class,
	SearchUsingRemoteHelp.class,
	LoadIndexUsingRemoteHelp.class,
	GetContentUsingRemoteHelp.class,
	GetContextUsingRemoteHelp.class,
	TocManagerTest.class,
	SearchIndexCreation.class,
	ParallelSearchUsingRemote.class,
	ParallelSearchServletTest.class
})
public class AllRemoteTests {
}
