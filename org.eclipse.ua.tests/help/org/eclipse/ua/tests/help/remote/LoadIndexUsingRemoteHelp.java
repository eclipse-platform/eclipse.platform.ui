/*******************************************************************************
 * Copyright (c) 2009, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ua.tests.help.remote;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.eclipse.help.IIndex;
import org.eclipse.help.IIndexEntry;
import org.eclipse.help.internal.HelpPlugin;
import org.eclipse.help.internal.base.BaseHelpSystem;

public class LoadIndexUsingRemoteHelp extends TestCase {
	
	private int mode;

	protected void setUp() throws Exception {
        RemotePreferenceStore.savePreferences();
        mode = BaseHelpSystem.getMode();
		BaseHelpSystem.setMode(BaseHelpSystem.MODE_INFOCENTER);
	}
	
	protected void tearDown() throws Exception {
		RemotePreferenceStore.restorePreferences();
		BaseHelpSystem.setMode(mode);
	}

	public void testIndexContribution() throws Exception {
		String locale = "en";
		HelpPlugin.getIndexManager().clearCache();
		IIndex index = HelpPlugin.getIndexManager().getIndex(locale);
		assertEquals(0, matchingEntries(index, "entry1_" + locale).length);
		assertEquals(0, matchingEntries(index, "entry2_" + locale).length);
		RemotePreferenceStore.setMockRemoteServer();
		HelpPlugin.getIndexManager().clearCache();
		index = HelpPlugin.getIndexManager().getIndex(locale);
		assertEquals(1, matchingEntries(index, "entry1_" + locale).length);
		assertEquals(1, matchingEntries(index, "entry2_" + locale).length);
	}
	
	public void testIndexWithTwoRemoteServers() throws Exception {
		String locale = "en";
		HelpPlugin.getIndexManager().clearCache();
		IIndex index = HelpPlugin.getIndexManager().getIndex(locale);
		IIndexEntry[] entry1 = matchingEntries(index, "entry1_" + locale);
		assertEquals(0, entry1.length);
		IIndexEntry[] entry2 = matchingEntries(index, "entry2_" + locale);
		assertEquals(0, entry2.length);
		RemotePreferenceStore.setTwoMockRemoteServers();
		HelpPlugin.getIndexManager().clearCache();
		index = HelpPlugin.getIndexManager().getIndex(locale);
		// Entry 1 has the same child on each remote server, Entry 2 has different children
        entry1 = matchingEntries(index, "entry1_" + locale);
		entry2 = matchingEntries(index, "entry2_" + locale);
		assertEquals(1, entry1.length);
		assertEquals(1, entry1[0].getTopics().length);
		assertEquals(1, entry2.length);
		assertEquals(2, entry2[0].getTopics().length);
	}

	private IIndexEntry[] matchingEntries(IIndex index, String keyword) {
		List<IIndexEntry> matches = new ArrayList<IIndexEntry>();
		IIndexEntry[] entries = index.getEntries();
		for (int i = 0; i < entries.length; i++) {
			if (keyword.equals(entries[i].getKeyword())) {
				matches.add(entries[i]);
			}
		}
		return matches.toArray(new IIndexEntry[matches.size()]);
	}

}
