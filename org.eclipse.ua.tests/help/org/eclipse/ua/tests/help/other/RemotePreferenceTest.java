/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ua.tests.help.other;

import junit.framework.TestCase;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.help.internal.base.HelpBasePlugin;
import org.eclipse.help.internal.base.IHelpBaseConstants;
import org.eclipse.help.internal.base.remote.PreferenceFileHandler;
import org.eclipse.help.internal.base.remote.RemoteIC;

public class RemotePreferenceTest extends TestCase {
	
	private String namePreference;
	private String hostPreference;
	private String pathPreference;
	private String portPreference;
	private String icEnabledPreference;
	private String helpOn;
	private String defaultPort;

	private void setPreference(String name, String value) {
		HelpBasePlugin.getDefault().getPluginPreferences().setValue(name, value);
	}

	protected void setUp() throws Exception {
		super.setUp();
		Preferences prefs = HelpBasePlugin.getDefault().getPluginPreferences();

		namePreference = prefs
				.getString(IHelpBaseConstants.P_KEY_REMOTE_HELP_NAME);
		hostPreference = prefs
				.getString(IHelpBaseConstants.P_KEY_REMOTE_HELP_HOST);
		pathPreference = prefs
				.getString(IHelpBaseConstants.P_KEY_REMOTE_HELP_PATH);
		portPreference = prefs
				.getString(IHelpBaseConstants.P_KEY_REMOTE_HELP_PORT);
		icEnabledPreference = prefs
				.getString(IHelpBaseConstants.P_KEY_REMOTE_HELP_ICEnabled);
		defaultPort = prefs.getString(IHelpBaseConstants.P_KEY_REMOTE_HELP_DEFAULT_PORT);
		helpOn = prefs.getString(IHelpBaseConstants.P_KEY_REMOTE_HELP_ON);
	}
	
	protected void tearDown() throws Exception {
		Preferences prefs = HelpBasePlugin.getDefault().getPluginPreferences();
		prefs.setValue(IHelpBaseConstants.P_KEY_REMOTE_HELP_NAME, namePreference);
		prefs.setValue(IHelpBaseConstants.P_KEY_REMOTE_HELP_HOST, hostPreference);
		prefs.setValue(IHelpBaseConstants.P_KEY_REMOTE_HELP_PATH, pathPreference);
		prefs.setValue(IHelpBaseConstants.P_KEY_REMOTE_HELP_PORT, portPreference);
		prefs.setValue(IHelpBaseConstants.P_KEY_REMOTE_HELP_DEFAULT_PORT, defaultPort);
		prefs.setValue(IHelpBaseConstants.P_KEY_REMOTE_HELP_ON, helpOn);		
		prefs.setValue(IHelpBaseConstants.P_KEY_REMOTE_HELP_ICEnabled, icEnabledPreference);
		super.tearDown();
	}
	
	public void testDefaults() {
		Preferences prefs = HelpBasePlugin.getDefault().getPluginPreferences();
		prefs.setToDefault(IHelpBaseConstants.P_KEY_REMOTE_HELP_NAME);
		prefs.setToDefault(IHelpBaseConstants.P_KEY_REMOTE_HELP_HOST);
		prefs.setToDefault(IHelpBaseConstants.P_KEY_REMOTE_HELP_PATH);
		prefs.setToDefault(IHelpBaseConstants.P_KEY_REMOTE_HELP_PORT);
		prefs.setToDefault(IHelpBaseConstants.P_KEY_REMOTE_HELP_ICEnabled);
		prefs.setToDefault(IHelpBaseConstants.P_KEY_REMOTE_HELP_ON);
		prefs.setToDefault(IHelpBaseConstants.P_KEY_REMOTE_HELP_DEFAULT_PORT);
		PreferenceFileHandler handler = new PreferenceFileHandler();
		assertEquals(0, handler.getTotalRemoteInfocenters());	
		assertEquals(0, handler.getEnabledEntries().length);
	}

	/*
	 * Test the default settings from Eclipse 3.3
	 */
	public void test33Defaults() {
		setPreference("remoteHelpOn", "false");
		setPreference("remoteHelpHost", "");
		setPreference("remoteHelpPath", "/help");
		setPreference("remoteHelpUseDefaultPort", "true");
		setPreference("remoteHelpPort", "80");
		setPreference("remoteHelpName", "");
		setPreference("remoteHelpICEnabled", "");
		setPreference("remoteHelpICContributed", "");
		PreferenceFileHandler handler = new PreferenceFileHandler();
		assertEquals(0, handler.getTotalRemoteInfocenters());
		assertEquals(0, handler.getEnabledEntries().length);
	}
	
	/*
	 * Test settings which worked in Eclipse 3.3 to read a remote infocenter
	 */
	public void test33Remote() {
		setPreference("remoteHelpOn", "true");
		setPreference("remoteHelpHost", "localhost");
		setPreference("remoteHelpPath", "/help");
		setPreference("remoteHelpUseDefaultPort", "false");
		setPreference("remoteHelpPort", "8081");
		setPreference("remoteHelpName", "");
		setPreference("remoteHelpICEnabled", "");
		setPreference("remoteHelpICContributed", "");
		PreferenceFileHandler handler = new PreferenceFileHandler();
		assertEquals(1, handler.getTotalRemoteInfocenters());
		assertEquals(1, handler.getEnabledEntries().length);
	}
	
	public void testZeroRemoteInfocenters() {
		setPreference("remoteHelpOn", "true");
		setPreference("remoteHelpHost", "");
		setPreference("remoteHelpPath", "");
		setPreference("remoteHelpUseDefaultPort", "");
		setPreference("remoteHelpPort", "");
		setPreference("remoteHelpName", "");
		setPreference("remoteHelpICEnabled", "");
		setPreference("remoteHelpICContributed", "");
		PreferenceFileHandler handler = new PreferenceFileHandler();
		assertEquals(0, handler.getTotalRemoteInfocenters());
		assertEquals(0, handler.getHostEntries().length);
		assertEquals(0, handler.getPortEntries().length);
		assertEquals(0, handler.getEnabledEntries().length);
		assertEquals(0, handler.getPathEntries().length);
	}
	
	public void testOneRemoteInfocenter() {
		setPreference("remoteHelpOn", "true");
		setPreference("remoteHelpHost", "localhost");
		setPreference("remoteHelpPath", "/help");
		setPreference("remoteHelpUseDefaultPort", "");
		setPreference("remoteHelpPort", "8081");
		setPreference("remoteHelpName", "local");
		setPreference("remoteHelpICEnabled", "true");
		setPreference("remoteHelpICContributed", "false");
		PreferenceFileHandler handler = new PreferenceFileHandler();
		assertEquals(1, handler.getTotalRemoteInfocenters());
		assertEquals(1, handler.getHostEntries().length);
		assertEquals("localhost", handler.getHostEntries()[0]);
		assertEquals(1, handler.getPortEntries().length);
		assertEquals("8081", handler.getPortEntries()[0]);
		assertEquals(1, handler.getEnabledEntries().length);
		assertEquals("true", handler.getEnabledEntries()[0].toLowerCase());
		assertEquals(1, handler.getPathEntries().length);
		assertEquals("/help", handler.getPathEntries()[0].toLowerCase());
	}
	
	public void testTwoRemoteInfocenters() {
		setPreference("remoteHelpHost", "localhost,www.eclipse.org");
		setPreference("remoteHelpPath", "/help,/eclipse/help");
		setPreference("remoteHelpUseDefaultPort", "");
		setPreference("remoteHelpPort", "8081,8082");
		setPreference("remoteHelpName", "local,remote");
		setPreference("remoteHelpICEnabled", "true,false");
		setPreference("remoteHelpICContributed", "false,false");
		PreferenceFileHandler handler = new PreferenceFileHandler();
		assertEquals(2, handler.getTotalRemoteInfocenters());
		assertEquals(2, handler.getHostEntries().length);
		assertEquals("localhost", handler.getHostEntries()[0]);
		assertEquals("www.eclipse.org", handler.getHostEntries()[1]);
		assertEquals(2, handler.getPortEntries().length);
		assertEquals("8081", handler.getPortEntries()[0]);
		assertEquals("8082", handler.getPortEntries()[1]);
		assertEquals(2, handler.getEnabledEntries().length);
		assertEquals("true", handler.getEnabledEntries()[0].toLowerCase());
		assertEquals("false", handler.getEnabledEntries()[1].toLowerCase());
		assertEquals(2, handler.getPathEntries().length);
		assertEquals("/help", handler.getPathEntries()[0].toLowerCase());
		assertEquals("/eclipse/help", handler.getPathEntries()[1].toLowerCase());
	}

	public void testOnePathTwoOfEverythingElse() {
		setPreference("remoteHelpOn", "true");
		setPreference("remoteHelpHost", "localhost");
		setPreference("remoteHelpPath", "/help,/nhelp");
		setPreference("remoteHelpUseDefaultPort", "true,false");
		setPreference("remoteHelpPort", "8081,8082");
		setPreference("remoteHelpName", "local,remote");
		setPreference("remoteHelpICEnabled", "true,false");
		setPreference("remoteHelpICContributed", "false,true");
		PreferenceFileHandler handler = new PreferenceFileHandler();
		assertEquals(1, handler.getTotalRemoteInfocenters());
		assertEquals(1, handler.getHostEntries().length);
		assertEquals("localhost", handler.getHostEntries()[0]);
		assertEquals(1, handler.getPortEntries().length);
		assertEquals("8081", handler.getPortEntries()[0]);
		assertEquals(1, handler.getEnabledEntries().length);
		assertEquals("true", handler.getEnabledEntries()[0].toLowerCase());
		assertEquals(1, handler.getPathEntries().length);
		assertEquals("/help", handler.getPathEntries()[0].toLowerCase());
	}
	
	public void testPathOnly() {
		setPreference("remoteHelpOn", "true");
		setPreference("remoteHelpHost", "localhost");
		setPreference("remoteHelpPath", "");
		setPreference("remoteHelpUseDefaultPort", "");
		setPreference("remoteHelpPort", "");
		setPreference("remoteHelpName", "");
		setPreference("remoteHelpICEnabled", "");
		setPreference("remoteHelpICContributed", "");
		PreferenceFileHandler handler = new PreferenceFileHandler();
		assertEquals(1, handler.getTotalRemoteInfocenters());
		assertEquals(1, handler.getHostEntries().length);
		assertEquals("localhost", handler.getHostEntries()[0]);
		assertEquals(1, handler.getPortEntries().length);
		//assertEquals("80", handler.getPortEntries()[0]);
		assertEquals(1, handler.getEnabledEntries().length);
		//assertEquals("true", handler.getEnabledEntries()[0].toLowerCase());
		assertEquals(1, handler.getPathEntries().length);
		//assertEquals("/help", handler.getPathEntries()[0].toLowerCase());
	}

	public void testWriteNoRemote() {
		PreferenceFileHandler.commitRemoteICs(new RemoteIC[0]);
		PreferenceFileHandler handler = new PreferenceFileHandler();
		assertEquals(0, handler.getTotalRemoteInfocenters());
		assertEquals(0, handler.getHostEntries().length);
		assertEquals(0, handler.getPortEntries().length);
		assertEquals(0, handler.getEnabledEntries().length);
		assertEquals(0, handler.getPathEntries().length);
	}

	public void testWriteOneRemote() {
		RemoteIC[] ic = {new RemoteIC(true, "name", "host", "/help", "8080")};
		PreferenceFileHandler.commitRemoteICs(ic);
		PreferenceFileHandler handler = new PreferenceFileHandler();
		assertEquals(1, handler.getTotalRemoteInfocenters());
		assertEquals(1, handler.getHostEntries().length);
		assertEquals("host", handler.getHostEntries()[0]);
		assertEquals(1, handler.getPortEntries().length);
		assertEquals("8080", handler.getPortEntries()[0]);
		assertEquals(1, handler.getEnabledEntries().length);
		assertEquals("true", handler.getEnabledEntries()[0].toLowerCase());
		assertEquals(1, handler.getPathEntries().length);
		assertEquals("/help", handler.getPathEntries()[0].toLowerCase());
	}
	
	public void testWriteTwoRemote() {
		RemoteIC[] ic = {new RemoteIC(true, "name", "host", "/help", "8080"),
				new RemoteIC(false, "remote", "remotehost", "/help2", "8081")};
		PreferenceFileHandler.commitRemoteICs(ic);
		PreferenceFileHandler handler = new PreferenceFileHandler();
		assertEquals(2, handler.getTotalRemoteInfocenters());
		assertEquals(2, handler.getHostEntries().length);
		assertEquals("host", handler.getHostEntries()[0]);
		assertEquals("remotehost", handler.getHostEntries()[1]);
		assertEquals(2, handler.getPortEntries().length);
		assertEquals("8080", handler.getPortEntries()[0]);
		assertEquals("8081", handler.getPortEntries()[1]);
		assertEquals(2, handler.getEnabledEntries().length);
		assertEquals("true", handler.getEnabledEntries()[0].toLowerCase());
		assertEquals("false", handler.getEnabledEntries()[1].toLowerCase());
		assertEquals(2, handler.getPathEntries().length);
		assertEquals("/help", handler.getPathEntries()[0].toLowerCase());
		assertEquals("/help2", handler.getPathEntries()[1].toLowerCase());
	}
		
}
