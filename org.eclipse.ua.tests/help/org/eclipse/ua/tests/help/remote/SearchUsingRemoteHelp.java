/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ua.tests.help.remote;

import org.eclipse.help.internal.base.BaseHelpSystem;
import org.eclipse.ua.tests.help.search.SearchTestUtils;

import junit.framework.TestCase;

public class SearchUsingRemoteHelp extends TestCase {
	
	private int mode;

	protected void setUp() throws Exception {
        RemotePreferenceStore.savePreferences();
        mode = BaseHelpSystem.getMode();
	}
	
	protected void tearDown() throws Exception {
		RemotePreferenceStore.restorePreferences();
		BaseHelpSystem.setMode(mode);
	}
	
	public void testSearchDefaultLocale() throws Exception {
		BaseHelpSystem.setMode(BaseHelpSystem.MODE_INFOCENTER);
		RemotePreferenceStore.setMockRemoteServer();
		SearchTestUtils.searchOneLocale("endfdsadsads", new String[] { "http://www.eclipse.org" }, 
				"en"); 
	    RemotePreferenceStore.disableRemoteHelp();
	    SearchTestUtils.searchOneLocale("endfdsadsads", new String[0], "en"); 
	}

	public void testSearchDeWordInDeLocale() throws Exception {
		BaseHelpSystem.setMode(BaseHelpSystem.MODE_INFOCENTER);
		RemotePreferenceStore.setMockRemoteServer();
		SearchTestUtils.searchOneLocale("dedfdsadsads", new String[] { "http://www.eclipse.org" }, "de"); 
	    RemotePreferenceStore.disableRemoteHelp();
	    SearchTestUtils.searchOneLocale("dedfdsadsads", new String[0], "de"); 
	}
	
	public void testSearchEnWordInDeLocale() throws Exception {
		BaseHelpSystem.setMode(BaseHelpSystem.MODE_INFOCENTER);
		RemotePreferenceStore.setMockRemoteServer();
	    SearchTestUtils.searchOneLocale("endfdsadsads", new String[0], "de"); 
	    RemotePreferenceStore.disableRemoteHelp();
	}
	
	public void testSearchDeWordInEnLocale() throws Exception {
		BaseHelpSystem.setMode(BaseHelpSystem.MODE_INFOCENTER);
		RemotePreferenceStore.setMockRemoteServer();
	    SearchTestUtils.searchOneLocale("dedfdsadsads", new String[0], "en"); 
	    RemotePreferenceStore.disableRemoteHelp();
	}

}
