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

import java.net.URL;

import junit.framework.Test;
import junit.framework.TestCase;

import org.eclipse.help.internal.HelpPlugin;
import org.eclipse.help.internal.base.BaseHelpSystem;
import org.eclipse.help.internal.server.WebappManager;
import org.eclipse.test.OrderedTestSuite;

public class GetContentUsingRemoteHelp extends TestCase {
	

	public static Test suite() {
		return new OrderedTestSuite(GetContentUsingRemoteHelp.class, new String[] {
			"testContentNotFound",
			"testContentFound",
			"testContentFoundDe",
			"testLocalBeatsRemote",
			"testRemoteHelpPreferredPreference",
			"testRemoteOrdering",
			"testRemoteOrderingReversed",
			"testRemoteUsedIfLocalUnavaliable"
		});
	}


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
		int port = TestServerManager.getPort(0);
		String expectedContent = RemoteTestUtils.createMockContent("mock.toc", path, "en", port);
		assertEquals(expectedContent, remoteContent);
	}

	public void testContentFoundDe() throws Exception  {
        final String path = "/data/help/index/topic2.html";
        String remoteContent = getHelpContent("mock.toc", path, "de");
		int port = TestServerManager.getPort(0);
		String expectedContent = RemoteTestUtils.createMockContent("mock.toc", path, "de", port);
		assertEquals(expectedContent, remoteContent);
	}

	public void testLocalBeatsRemote() throws Exception  {
        final String path = "/doc/help_home.html";
        String plugin = "org.eclipse.help.base";
		String helpContent = getHelpContent(plugin, path, "en");
		String localContent = RemoteTestUtils.getLocalContent(plugin, path);
		assertEquals(localContent, helpContent);
	}
	
	public void testRemoteHelpPreferredPreference() throws Exception  {
		RemotePreferenceStore.setMockRemotePriority();
		HelpPlugin.getTocManager().clearCache();
		HelpPlugin.getTocManager().getTocs("en");
        final String path = "/doc/help_home.html";
        String plugin = "org.eclipse.help.base";
		String helpContent = getHelpContent(plugin, path, "en");

		int port = TestServerManager.getPort(0);
		String remoteContent = RemoteTestUtils.createMockContent(plugin, path, "en", port);
		assertEquals(remoteContent, helpContent);
	}

	public void testRemoteOrdering() throws Exception {
		RemotePreferenceStore.setTwoMockRemoteServers();
		RemotePreferenceStore.setMockRemotePriority();
		HelpPlugin.getTocManager().clearCache();
		HelpPlugin.getTocManager().getTocs("en");
		//Verify help coming from first one
		 final String path = "/doc/help_home.html";
	     String plugin = "org.eclipse.help.base";
		 String helpContent = GetContentUsingRemoteHelp.getHelpContent(plugin, path, "en");
		 
		 //Get remote content from first one in prefs

		 int port0 = TestServerManager.getPort(0);
		 String remoteContent0 = RemoteTestUtils.createMockContent(plugin, path, "en", port0);
		 
		 int port1 = TestServerManager.getPort(1);
		 String remoteContent1 = RemoteTestUtils.createMockContent(plugin, path, "en", port1);

		 assertEquals(remoteContent0, helpContent);
		 assertFalse(remoteContent1.equals(helpContent));

	}
	
	public void testRemoteOrderingReversed() throws Exception {
		RemotePreferenceStore.setTwoMockRemoteServersReversePriority();
		RemotePreferenceStore.setMockRemotePriority();
		HelpPlugin.getTocManager().clearCache();
		HelpPlugin.getTocManager().getTocs("en");
		//Verify help coming from first one
		 final String path = "/doc/help_home.html";
	     String plugin = "org.eclipse.help.base";
		 String helpContent = GetContentUsingRemoteHelp.getHelpContent(plugin, path, "en");
		 
		 //Get remote content from second in prefs

		 int port0 = TestServerManager.getPort(0);
		 String remoteContent0 = RemoteTestUtils.createMockContent(plugin, path, "en", port0);
		 
		 int port1 = TestServerManager.getPort(1);
		 String remoteContent1 = RemoteTestUtils.createMockContent(plugin, path, "en", port1);
		 
		 
		 assertEquals(remoteContent1, helpContent);
		 assertFalse(remoteContent0.equals(helpContent));

	}

	
	public void testRemoteUsedIfLocalUnavaliable() throws Exception  {
		RemotePreferenceStore.setMockRemoteServer();
		HelpPlugin.getTocManager().clearCache();
		HelpPlugin.getTocManager().getTocs("en");
		final String path = "/data/help/nonlocal.html";
        String plugin = "org.eclipse.help.base";
		String remoteContent = getHelpContent(plugin, path, "en");		
		int port = TestServerManager.getPort(0);
		String expectedContent = RemoteTestUtils.createMockContent(plugin, path, "en", port);
		assertEquals(expectedContent, remoteContent);
	}
	
	public static String getHelpContent(String plugin, String path, String locale)
			throws Exception {
		int port = WebappManager.getPort();
		URL url = new URL("http", "localhost", port, "/help/nftopic/" + plugin + path + "?lang=" + locale);
		return RemoteTestUtils.readFromURL(url);
	}

}
