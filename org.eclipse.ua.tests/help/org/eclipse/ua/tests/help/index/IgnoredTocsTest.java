/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ua.tests.help.index;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.core.runtime.Preferences;
import org.eclipse.help.HelpSystem;
import org.eclipse.help.IHelpResource;
import org.eclipse.help.IIndex;
import org.eclipse.help.IIndexEntry;
import org.eclipse.help.internal.HelpPlugin;

public class IgnoredTocsTest extends TestCase {
	
	private String oldValue;
	
	/*
	 * Returns an instance of this Test.
	 */
	public static Test suite() {
		return new TestSuite(IgnoredTocsTest.class);
	}

	protected void setUp() throws Exception {
		super.setUp();
		Preferences prefs = HelpPlugin.getDefault().getPluginPreferences();
		oldValue = prefs.getString(HelpPlugin.IGNORED_TOCS_KEY);
		prefs.setValue(HelpPlugin.IGNORED_TOCS_KEY, "/org.eclipse.ua.tests/data/help/index/tocIgnored.xml");
		HelpPlugin.getTocManager().reset();
	}
	
	protected void tearDown() throws Exception {
		super.tearDown();
		Preferences prefs = HelpPlugin.getDefault().getPluginPreferences();
		prefs.setValue(HelpPlugin.IGNORED_TOCS_KEY, oldValue);
	}
	
	public void testIgnoredTocs() {
		IIndex index = HelpSystem.getIndex();
		assertIndexHasTopic(index, "/org.eclipse.ua.tests/data/help/index/topic1.html");
		assertIndexHasTopic(index, "/org.eclipse.ua.tests/data/help/index/topic2.html");
		assertIndexHasTopic(index, "/org.eclipse.ua.tests/data/help/index/topic3.html");
		assertIndexDoesntHaveTopic(index, "/org.eclipse.ua.tests/data/help/index/topic4.html");
	}
	
	private void assertIndexHasTopic(IIndex index, String href) {
		assertTrue("One of the expected help keywords could not be found: " + href, containsTopic(index, href));
	}

	private void assertIndexDoesntHaveTopic(IIndex index, String href) {
		assertTrue("One of the help keywords that should have been ignored was not properly ignored: " + href, !containsTopic(index, href));
	}
	
	private boolean containsTopic(IIndex index, String href) {
		IIndexEntry[] entries = index.getEntries();
		for (int i=0;i<entries.length;++i) {
			IIndexEntry entry = entries[i];
			if (containsTopic(entry, href)) {
				return true;
			}
		}
		return false;
	}
	
	private boolean containsTopic(IIndexEntry entry, String href) {
		IHelpResource[] topics = entry.getTopics();
		for (int i=0;i<topics.length;++i) {
			IHelpResource topic = topics[i];
			if (containsTopic(topic, href)) {
				return true;
			}
		}
		IIndexEntry[] entries = entry.getSubentries();
		for (int i=0;i<entries.length;++i) {
			IIndexEntry subEntry = entries[i];
			if (containsTopic(subEntry, href)) {
				return true;
			}
		}
		return false;
	}
	
	private boolean containsTopic(IHelpResource topic, String href) {
		return href.equals(topic.getHref());
	}
}
