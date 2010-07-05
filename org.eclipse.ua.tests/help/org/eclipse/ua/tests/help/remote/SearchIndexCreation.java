/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ua.tests.help.remote;

import java.net.URL;

import junit.framework.TestCase;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.help.internal.HelpPlugin;
import org.eclipse.help.internal.base.BaseHelpSystem;
import org.eclipse.help.internal.search.AnalyzerDescriptor;
import org.eclipse.help.internal.search.SearchIndex;
import org.eclipse.help.internal.search.SearchIndexWithIndexingProgress;

public class SearchIndexCreation extends TestCase {
	
	private int mode;
	private AnalyzerDescriptor analyzerDesc;

	protected void setUp() throws Exception {
		BaseHelpSystem.ensureWebappRunning();
        mode = BaseHelpSystem.getMode();
        RemotePreferenceStore.savePreferences();
		RemotePreferenceStore.setMockRemoteServer();
		RemotePreferenceStore.disableErrorPage();
		BaseHelpSystem.setMode(BaseHelpSystem.MODE_INFOCENTER);
		HelpPlugin.getTocManager().getTocs("en");
		analyzerDesc = new AnalyzerDescriptor("en-us");
	}
	
	protected void tearDown() throws Exception {
		RemotePreferenceStore.restorePreferences();
		BaseHelpSystem.setMode(mode);
	}

	public void testSearchIndexMakesNoRemoteCalls() throws Throwable {
		int initialCallCount = MockContentServlet.getCallcount();
		SearchIndexWithIndexingProgress index = new SearchIndexWithIndexingProgress("en-us", analyzerDesc, HelpPlugin
				.getTocManager());
		index.beginAddBatch(true);
		addHrefToIndex(index, "/org.eclipse.ua.tests/data/help/search/test7.html", true);
		addHrefToIndex(index, "/org.eclipse.ua.tests/data/help/search/test8.htm", true);
		addHrefToIndex(index, "/org.eclipse.ua.tests/data/help/search/test77.htm", false); // Does not exist
		int finalCallCount = MockContentServlet.getCallcount();
		assertEquals("Remote server called", 0, finalCallCount - initialCallCount);
	}
	
	public void testSearchIndexMakesNoRemoteCallsRemotePriority() throws Throwable {
		RemotePreferenceStore.setMockRemotePriority();
		int initialCallCount = MockContentServlet.getCallcount();
		SearchIndexWithIndexingProgress index = new SearchIndexWithIndexingProgress("en-us", analyzerDesc, HelpPlugin
				.getTocManager());
		index.beginAddBatch(true);
		addHrefToIndex(index, "/org.eclipse.ua.tests/data/help/search/test7.html", true);
		addHrefToIndex(index, "/org.eclipse.ua.tests/data/help/search/test8.htm", true);
		addHrefToIndex(index, "/org.eclipse.ua.tests/data/help/search/test77.htm", false); // Does not exist
		int finalCallCount = MockContentServlet.getCallcount();
		assertEquals("Remote server called", 0, finalCallCount - initialCallCount);
	}

	private void addHrefToIndex(SearchIndexWithIndexingProgress index,
			String doc, boolean exists) throws Throwable {
		URL url = SearchIndex.getIndexableURL(index.getLocale(), doc);
		IStatus status = index.addDocument(url.getFile(), url);
		if (exists && !status.isOK()) {
			if (status.getException() != null) {
				throw status.getException();
			}
		    fail(doc + " status = " + status.getMessage());
		}
	}

}
