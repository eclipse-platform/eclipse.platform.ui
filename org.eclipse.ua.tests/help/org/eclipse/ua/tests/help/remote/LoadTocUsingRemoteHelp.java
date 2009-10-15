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

import junit.framework.TestCase;

import org.eclipse.help.internal.HelpPlugin;
import org.eclipse.help.internal.base.BaseHelpSystem;
import org.eclipse.help.internal.toc.Toc;

public class LoadTocUsingRemoteHelp extends TestCase {
	
	private int mode;

	protected void setUp() throws Exception {
        RemotePreferenceStore.savePreferences();
        mode = BaseHelpSystem.getMode();
	}
	
	protected void tearDown() throws Exception {
		RemotePreferenceStore.restorePreferences();
		BaseHelpSystem.setMode(mode);
	}

	public void testTocContribution() throws Exception {
		BaseHelpSystem.setMode(BaseHelpSystem.MODE_INFOCENTER);
		RemotePreferenceStore.setMockRemoteServer();
		HelpPlugin.getTocManager().clearCache();
		Toc[] tocs = HelpPlugin.getTocManager().getTocs("en");
		int enTocs = matchingTocs(tocs, "Mock Toc en");
		assertEquals(1, enTocs);
		enTocs = matchingTocs(tocs, "Mock Toc 2 en");
		assertEquals(1, enTocs);
		int deTocs = matchingTocs(tocs, "Mock Toc de");
		assertEquals(0, deTocs);
		deTocs = matchingTocs(tocs, "Mock Toc 2 de");
		assertEquals(0, deTocs);
	    RemotePreferenceStore.disableRemoteHelp();
	}
	
	public void testTocContributionDe() throws Exception {
		BaseHelpSystem.setMode(BaseHelpSystem.MODE_INFOCENTER);
		RemotePreferenceStore.setMockRemoteServer();
		HelpPlugin.getTocManager().clearCache();
		Toc[] tocs = HelpPlugin.getTocManager().getTocs("de");
		int enTocs = matchingTocs(tocs, "Mock Toc en");
		assertEquals(0, enTocs);
		enTocs = matchingTocs(tocs, "Mock Toc 2 en");
		assertEquals(0, enTocs);
		int deTocs = matchingTocs(tocs, "Mock Toc de");
		assertEquals(1, deTocs);
		deTocs = matchingTocs(tocs, "Mock Toc 2 de");
		assertEquals(1, deTocs);
	    RemotePreferenceStore.disableRemoteHelp();
	}
	
	/*
	 * Fails, see  Bug 292176
	public void testTocContributionFromTwoServers() throws Exception {
		BaseHelpSystem.setMode(BaseHelpSystem.MODE_INFOCENTER);
		RemotePreferenceStore.setTwoMockRemoteServers();
		HelpPlugin.getTocManager().clearCache();
		Toc[] tocs = HelpPlugin.getTocManager().getTocs("en");
		int enTocs = matchingTocs(tocs, "Mock Toc en");
		assertEquals(1, enTocs);
		int deTocs = matchingTocs(tocs, "Mock Toc de");
		assertEquals(0, deTocs);
	    RemotePreferenceStore.disableRemoteHelp();
	}
	*/

	/*
	 * Return the number of tocs with this label
	 */
	private int matchingTocs(Toc[] tocs, String label) {
		int result = 0;
		for (int i = 0; i < tocs.length; i++) {
			if (label.equals(tocs[i].getLabel())) {
				result += 1;
			}
		}
		return result;
	}

}
