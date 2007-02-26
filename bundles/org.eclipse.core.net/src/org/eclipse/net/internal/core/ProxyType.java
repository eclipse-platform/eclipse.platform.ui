/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.net.internal.core;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

import org.eclipse.core.runtime.*;
import org.eclipse.net.core.IProxyData;
import org.eclipse.net.core.NetCore;
import org.eclipse.osgi.util.NLS;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

public class ProxyType {

	/**
	 * Preference keys
	 */
	private static final String PREF_PROXY_DATA_NODE = "proxyData"; //$NON-NLS-1$
	private static final String PREF_PROXY_HOST = "host"; //$NON-NLS-1$
	private static final String PREF_PROXY_PORT = "port"; //$NON-NLS-1$
	private static final String PREF_PROXY_HAS_AUTH = "hasAuth"; //$NON-NLS-1$

	/*
	 * Fields used to cache authentication information in the keyring
	 */
    private static final String INFO_PROXY_USER = "user"; //$NON-NLS-1$ 
    private static final String INFO_PROXY_PASS = "pass"; //$NON-NLS-1$ 
    private static final URL FAKE_URL;
    static {
        URL temp = null;
        try {
            temp = new URL("http://org.eclipse.core.net.proxy.auth");//$NON-NLS-1$ 
        } catch (MalformedURLException e) {
            // Should never fail
        }
        FAKE_URL = temp;
    }
    
	private String name;

	public static String convertHostsToPropertyString(String[] value) {
		StringBuffer buffer = new StringBuffer();

		if (value == null)
			return ""; //$NON-NLS-1$

		if (value.length > 0) {
			buffer.append(value[0]);
		}

		for (int index = 1; index < value.length; index++) {
			buffer.append('|');
			buffer.append(value[index]);
		}

		return buffer.toString();
	}

	public static String[] convertPropertyStringToHosts(String property) {
		return property.split("\\|"); //$NON-NLS-1$
	}

	public ProxyType(String name) {
		super();
		this.name = name;
	}

	private Preferences getPreferenceNode() {
		return getParentPreferences().node(getName());
	}

	/**
	 * Return the preferences node whose child nodes are the know proxy types
	 * 
	 * @return a preferences node
	 */
	private Preferences getParentPreferences() {
		return NetCorePlugin.getInstance().getInstancePreferences().node(
				PREF_PROXY_DATA_NODE);
	}

	public IProxyData getProxyData() {
		return createProxyData(name, getPreferenceNode());
	}

	private IProxyData createProxyData(String type, Preferences node) {
		String host = node.get(PREF_PROXY_HOST, null);
		if (host.length() == 0)
			host = null;
		int port = node.getInt(PREF_PROXY_PORT, -1);
		boolean requiresAuth = node.getBoolean(PREF_PROXY_HAS_AUTH, false);
		ProxyData proxyData = new ProxyData(type, host, port, requiresAuth);
		loadProxyAuth(proxyData);
		return proxyData;
	}

	public boolean setProxyData(IProxyData proxyData, boolean proxiesEnabled) {
		Assert.isTrue(proxyData.getType().equals(getName()));
		IProxyData oldData = getProxyData();
		if (oldData.equals(proxyData))
			return false;
		Preferences node = getPreferenceNode();
		saveProxyAuth(proxyData);
		if (proxyData.getHost() == null) {
			try {
				Preferences parent = node.parent();
				node.removeNode();
				parent.flush();
			} catch (BackingStoreException e) {
				NetCorePlugin.logError(NLS.bind(
						"An error occurred removing the {0} proxy node from the preference store", proxyData.getType()), e); //$NON-NLS-1$
			}
		} else {
			node.put(PREF_PROXY_HOST, proxyData.getHost());
			node.putInt(PREF_PROXY_PORT, proxyData.getPort());
			node.putBoolean(PREF_PROXY_HAS_AUTH, proxyData.getUserId() != null);
			try {
				node.flush();
			} catch (BackingStoreException e) {
				NetCorePlugin.logError(NLS.bind(
					"The {0} proxy node could not be written", proxyData.getType()), e); //$NON-NLS-1$
			}
		}
		updateSystemProperties(proxyData, proxiesEnabled);
		return true;
	}
	
	/* package */void updateSystemProperties(IProxyData proxyData, boolean proxiesEnabled) {
		try {
			if (proxyData.getType().equals(IProxyData.HTTP_PROXY_TYPE)) {
				updateHttpSystemProperties(proxyData, proxiesEnabled);
			} else if (proxyData.getType().equals(IProxyData.HTTPS_PROXY_TYPE)) {
				updateHttpsSystemProperties(proxyData, proxiesEnabled);
			} else if (proxyData.getType().equals(IProxyData.SOCKS_PROXY_TYPE)) {
				updateSocksSystemProperties(proxyData, proxiesEnabled);
			}
		} catch (SecurityException e) {
			NetCorePlugin.logError("A security exception occurred while trying to put the proxy data into the system properties", e); //$NON-NLS-1$
		}
	}

	public String getName() {
		return name;
	}

	private void updateHttpSystemProperties(IProxyData data, boolean proxiesEnabled) {
		Assert.isTrue(data.getType().equals(IProxyData.HTTP_PROXY_TYPE));
		Properties sysProps = System.getProperties();
		if (!proxiesEnabled || data.getHost() == null) {
			sysProps.remove("http.proxySet"); //$NON-NLS-1$
			sysProps.remove("http.proxyHost"); //$NON-NLS-1$
			sysProps.remove("http.proxyPort"); //$NON-NLS-1$
			sysProps.remove("http.nonProxyHosts"); //$NON-NLS-1$
			sysProps.remove("http.proxyUser"); //$NON-NLS-1$
			sysProps.remove("http.proxyUserName"); //$NON-NLS-1$
			sysProps.remove("http.proxyPassword"); //$NON-NLS-1$
		} else {
			sysProps.put("http.proxySet", "true"); //$NON-NLS-1$ //$NON-NLS-2$
			sysProps.put("http.proxyHost", data.getHost()); //$NON-NLS-1$
			int port = data.getPort();
			if (port == -1) {
				sysProps.remove("http.proxyPort"); //$NON-NLS-1$
			} else {
				sysProps.put("http.proxyPort", String.valueOf(port)); //$NON-NLS-1$
			}
			sysProps.put("http.nonProxyHosts",  //$NON-NLS-1$
					convertHostsToPropertyString(NetCore.getProxyManager().getNonProxiedHosts()));

			String userid = data.getUserId();
			String password = data.getPassword();
			if (userid == null || password == null || userid.length() == 0
					|| password.length() == 0) {
				sysProps.remove("http.proxyUser"); //$NON-NLS-1$
				sysProps.remove("http.proxyUserName"); //$NON-NLS-1$
				sysProps.remove("http.proxyPassword"); //$NON-NLS-1$
			} else {
				sysProps.put("http.proxyUser", userid); //$NON-NLS-1$
				sysProps.put("http.proxyUserName", userid); //$NON-NLS-1$
				sysProps.put("http.proxyPassword", password); //$NON-NLS-1$
			}
		}
	}
	
	private void updateHttpsSystemProperties(IProxyData data, boolean proxiesEnabled) {
		Assert.isTrue(data.getType().equals(IProxyData.HTTPS_PROXY_TYPE));
		Properties sysProps = System.getProperties();
		if (!proxiesEnabled || data.getHost() == null) {
			sysProps.remove("https.proxySet"); //$NON-NLS-1$
			sysProps.remove("https.proxyHost"); //$NON-NLS-1$
			sysProps.remove("https.proxyPort"); //$NON-NLS-1$
			sysProps.remove("https.nonProxyHosts"); //$NON-NLS-1$
			sysProps.remove("https.proxyUser"); //$NON-NLS-1$
			sysProps.remove("https.proxyUserName"); //$NON-NLS-1$
			sysProps.remove("https.proxyPassword"); //$NON-NLS-1$
		} else {
			sysProps.put("https.proxySet", "true"); //$NON-NLS-1$ //$NON-NLS-2$
			sysProps.put("https.proxyHost", data.getHost()); //$NON-NLS-1$
			int port = data.getPort();
			if (port == -1) {
				sysProps.remove("https.proxyPort"); //$NON-NLS-1$
			} else {
				sysProps.put("https.proxyPort", String.valueOf(port)); //$NON-NLS-1$
			}
			sysProps.put("https.nonProxyHosts",  //$NON-NLS-1$
					convertHostsToPropertyString(NetCore.getProxyManager().getNonProxiedHosts()));

			String userid = data.getUserId();
			String password = data.getPassword();
			if (userid == null || password == null || userid.length() == 0
					|| password.length() == 0) {
				sysProps.remove("https.proxyUser"); //$NON-NLS-1$
				sysProps.remove("https.proxyUserName"); //$NON-NLS-1$
				sysProps.remove("https.proxyPassword"); //$NON-NLS-1$
			} else {
				sysProps.put("https.proxyUser", userid); //$NON-NLS-1$
				sysProps.put("https.proxyUserName", userid); //$NON-NLS-1$
				sysProps.put("https.proxyPassword", password); //$NON-NLS-1$
			}
		}
	}
	
	private void updateSocksSystemProperties(IProxyData data, boolean proxiesEnabled) {
		Assert.isTrue(data.getType().equals(IProxyData.SOCKS_PROXY_TYPE));
		Properties sysProps = System.getProperties();
		if (!proxiesEnabled || data.getHost() == null) {
			sysProps.remove("socksProxyHost"); //$NON-NLS-1$
			sysProps.remove("socksProxyPort"); //$NON-NLS-1$
		} else {
			sysProps.put("socksProxyHost", data.getHost()); //$NON-NLS-1$
			int port = data.getPort();
			if (port == -1) {
				sysProps.remove("socksProxyPort"); //$NON-NLS-1$
			} else {
				sysProps.put("socksProxyPort", String.valueOf(port)); //$NON-NLS-1$
			}
			// TODO: There does appear to be a way to set the non-proxy hosts for Socks
			// TODO: See http://java.sun.com/j2se/1.5.0/docs/guide/net/properties.html for a description
			// of how to set the Socks user and password
		}
	}

	public void initialize(boolean proxiesEnabled) {
		updateSystemProperties(getProxyData(), proxiesEnabled);
	}
	
    private Map getAuthInfo() {
		// Retrieve username and password from keyring.
		Map authInfo = Platform.getAuthorizationInfo(FAKE_URL, getName(), ""); //$NON-NLS-1$
		return authInfo != null ? authInfo : Collections.EMPTY_MAP;
	}

    private void loadProxyAuth(IProxyData data) {
		Map authInfo = getAuthInfo();
		data.setUserid((String)authInfo.get(INFO_PROXY_USER));
		data.setPassword((String)authInfo.get(INFO_PROXY_PASS));
	}
    
	private void saveProxyAuth(IProxyData data) {
		Map authInfo = getAuthInfo();
		if (authInfo.size() == 0) {
			authInfo = new java.util.HashMap(4);
		}
		String proxyUser = data.getUserId();
		if (proxyUser != null && data.getHost() != null) {
			authInfo.put(INFO_PROXY_USER, proxyUser);
		} else {
			authInfo.remove(INFO_PROXY_USER);
		}
		String proxyPass = data.getPassword();
		if (proxyPass != null && data.getHost() != null) {
			authInfo.put(INFO_PROXY_PASS, proxyPass);
		} else {
			authInfo.remove(INFO_PROXY_PASS);
		}
		try {
			if (authInfo.isEmpty()) {
				Platform.flushAuthorizationInfo(FAKE_URL, getName(), ""); //$NON-NLS-1$
			} else {
				Platform.addAuthorizationInfo(FAKE_URL, getName(), "", authInfo); //$NON-NLS-1$
			}
		} catch (CoreException e) {
			NetCorePlugin.logError(e.getMessage(), e);
		}
	}

}
