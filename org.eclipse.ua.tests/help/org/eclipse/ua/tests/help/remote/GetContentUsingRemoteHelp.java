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

import java.net.URL;

import junit.framework.TestCase;

import org.eclipse.help.internal.HelpPlugin;
import org.eclipse.help.internal.base.BaseHelpSystem;
import org.eclipse.help.internal.server.WebappManager;

public class GetContentUsingRemoteHelp extends TestCase {
	
	private int mode;

	protected void setUp() throws Exception {
		BaseHelpSystem.ensureWebappRunning();
        mode = BaseHelpSystem.getMode();
        RemotePreferenceStore.savePreferences();
		RemotePreferenceStore.setMockRemoteServer();
		RemotePreferenceStore.disableErrorPage();
		BaseHelpSystem.setMode(BaseHelpSystem.MODE_INFOCENTER);
		HelpPlugin.getTocManager().getTocs("en");
	}
	
	protected void tearDown() throws Exception {
		RemotePreferenceStore.restorePreferences();
		BaseHelpSystem.setMode(mode);
	}

	public void testContentNotFound()  {
		try {
			getHelpContent("mock.toc", "/invalid/nosuchfile.html", "en");
			fail("No exception thrown");
		} catch (Exception e) {
			// Exception thrown as expected
		}
	}

	public void testContentFound() throws Exception  {
        final String path = "/data/help/index/topic1.html";
        String remoteContent = getHelpContent("mock.toc", path, "en");
		String expectedContent = RemoteTestUtils.createMockContent("mock.toc", path, "en");
		assertEquals(expectedContent, remoteContent);
	}

	public void testContentFoundDe() throws Exception  {
        final String path = "/data/help/index/topic2.html";
        String remoteContent = getHelpContent("mock.toc", path, "de");
		String expectedContent = RemoteTestUtils.createMockContent("mock.toc", path, "de");
		assertEquals(expectedContent, remoteContent);
	}
	
	public void testLocalBeatsRemote() throws Exception  {
        final String path = "/doc/help_home.html";
        String plugin = "org.eclipse.help.base";
		String remoteContent = getHelpContent(plugin, path, "en");
		String localContent = RemoteTestUtils.getLocalContent(plugin, path);
		assertEquals(localContent, remoteContent);
	}

	/*
	public void testRemoteUsedIfLocalUnavaliable() throws Exception  {
        final String path = "/data/help/nonlocal.html";
        String plugin = "org.eclipse.help.base";
		String remoteContent = getHelpContent(plugin, path, "en");
		String expectedContent = RemoteTestUtils.createMockContent(plugin, path, "en");
		assertEquals(expectedContent, remoteContent);
	}
	*/
	
	public static String getHelpContent(String plugin, String path, String locale)
			throws Exception {
		int port = WebappManager.getPort();
		URL url = new URL("http", "localhost", port, "/help/nftopic/" + plugin + path + "?lang=" + locale);
		return RemoteTestUtils.readFromURL(url);
	}

}
