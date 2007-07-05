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
package org.eclipse.core.internal.net;

import java.net.Authenticator;
import java.util.*;

import org.eclipse.core.net.proxy.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.*;
import org.eclipse.osgi.util.NLS;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

public class ProxyManager implements IProxyService, IPreferenceChangeListener {
	
	private static final String PREF_HAS_MIGRATED = "org.eclipse.core.net.hasMigrated"; //$NON-NLS-1$
	
	/**
	 * Preference constants used by Update to record the HTTP proxy
	 */
	private static String HTTP_PROXY_HOST = "org.eclipse.update.core.proxy.host"; //$NON-NLS-1$
	private static String HTTP_PROXY_PORT = "org.eclipse.update.core.proxy.port"; //$NON-NLS-1$
	private static String HTTP_PROXY_ENABLE = "org.eclipse.update.core.proxy.enable"; //$NON-NLS-1$
	
	private static final String PREF_NON_PROXIED_HOSTS = "nonProxiedHosts"; //$NON-NLS-1$
	private static final String PREF_ENABLED = "proxiesEnabled"; //$NON-NLS-1$
	
	private static IProxyService proxyManager;
	
	ListenerList listeners = new ListenerList(ListenerList.IDENTITY);
	private String[] nonProxiedHosts;
	private final ProxyType[] proxies = new ProxyType[] {
			new ProxyType(IProxyData.HTTP_PROXY_TYPE),
			new ProxyType(IProxyData.HTTPS_PROXY_TYPE),
			new ProxyType(IProxyData.SOCKS_PROXY_TYPE)
		};

	/**
	 * Return the proxy manager.
	 * @return the proxy manager
	 */
	public synchronized static IProxyService getProxyManager() {
		if (proxyManager == null)
			proxyManager = new ProxyManager();
		return proxyManager;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.core.net.IProxyManager#addProxyChangeListener(org.eclipse.core.net.IProxyChangeListener)
	 */
	public void addProxyChangeListener(IProxyChangeListener listener) {
		listeners.add(listener);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.core.net.IProxyManager#removeProxyChangeListener(org.eclipse.core.net.IProxyChangeListener)
	 */
	public void removeProxyChangeListener(IProxyChangeListener listener) {
		listeners.remove(listener);
	}
	
	private void fireChange(final IProxyChangeEvent event) {
		Object[] l = listeners.getListeners();
		for (int i = 0; i < l.length; i++) {
			final IProxyChangeListener listener = (IProxyChangeListener)l[i];
			SafeRunner.run(new ISafeRunnable() {
				public void run() throws Exception {
					listener.proxyInfoChanged(event);
				}
				public void handleException(Throwable exception) {
					// Logged by SafeRunner
				}
			});
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.net.IProxyManager#getNonProxiedHosts()
	 */
	public synchronized String[] getNonProxiedHosts() {
		if (nonProxiedHosts == null) {
			String prop = Activator.getInstance().getInstancePreferences().get(PREF_NON_PROXIED_HOSTS, "localhost|127.0.0.1"); //$NON-NLS-1$
			nonProxiedHosts = ProxyType.convertPropertyStringToHosts(prop);
		}
		if (nonProxiedHosts.length == 0)
			return nonProxiedHosts;
		String[] result = new String[nonProxiedHosts.length];
		System.arraycopy(nonProxiedHosts, 0, result, 0, nonProxiedHosts.length );
		return result;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.net.IProxyManager#setNonProxiedHosts(java.lang.String[])
	 */
	public void setNonProxiedHosts(String[] hosts) {
		Assert.isNotNull(hosts);
		for (int i = 0; i < hosts.length; i++) {
			String host = hosts[i];
			Assert.isNotNull(host);
			Assert.isTrue(host.length() > 0);
		}
		String[] oldHosts = nonProxiedHosts;
		nonProxiedHosts = hosts;
		Activator.getInstance().getInstancePreferences().put(PREF_NON_PROXIED_HOSTS, ProxyType.convertHostsToPropertyString(nonProxiedHosts));
		try {
			Activator.getInstance().getInstancePreferences().flush();
		} catch (BackingStoreException e) {
			Activator.logError(
					"An error occurred while writing out the non-proxied hosts list", e); //$NON-NLS-1$
		}
		IProxyData[] data = getProxyData();
		IProxyChangeEvent event = new ProxyChangeEvent(IProxyChangeEvent.NONPROXIED_HOSTS_CHANGED, oldHosts, getNonProxiedHosts(), data, new IProxyData[0]);
		fireChange(event);
	}

	
	public IProxyData[] getProxyData() {
		IProxyData[] result = new IProxyData[proxies.length];
		for (int i = 0; i < proxies.length; i++) {
			ProxyType type = proxies[i];
			result[i] = type.getProxyData(ProxyType.VERIFY_EQUAL);
		}
		return result;
	}
	
	public void setProxyData(IProxyData[] proxies) {
		doSetProxyData(proxies);
	}
	
	private void doSetProxyData(IProxyData[] proxyDatas) {
		IProxyData[] oldData = getProxyData();
		String[] hosts = getNonProxiedHosts();
		IProxyData[] changedProxies = internalSetProxyData(proxyDatas);
		if (changedProxies.length > 0) {
			IProxyChangeEvent event = new ProxyChangeEvent(IProxyChangeEvent.PROXY_SERVICE_ENABLEMENT_CHANGE, hosts, hosts, oldData, changedProxies);
			fireChange(event);
		}
	}

	private IProxyData[] internalSetProxyData(IProxyData[] proxyDatas) {
		List result = new ArrayList();
		for (int i = 0; i < proxyDatas.length; i++) {
			IProxyData proxyData = proxyDatas[i];
			ProxyType type = getType(proxyData);
			if (type != null && type.setProxyData(proxyData, isProxiesEnabled())) {
				result.add(proxyData);
			}
		}
		return (IProxyData[]) result.toArray(new IProxyData[result.size()]);
	}

	private ProxyType getType(IProxyData proxyData) {
		for (int i = 0; i < proxies.length; i++) {
			ProxyType type = proxies[i];
			if (type.getName().equals(proxyData.getType())) {
				return type;
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.net.IProxyManager#isProxiesEnabled()
	 */
	public boolean isProxiesEnabled() {
		return Activator.getInstance().getInstancePreferences().getBoolean(PREF_ENABLED, false);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.net.IProxyManager#setProxiesEnabled(boolean)
	 */
	public void setProxiesEnabled(boolean enabled) {
		boolean current = isProxiesEnabled();
		if (current == enabled)
			return;
		// Setting the preference will trigger the system property update
		// (see preferenceChange)
		Activator.getInstance().getInstancePreferences().putBoolean(PREF_ENABLED, enabled);
	}

	private void internalSetEnabled(boolean enabled) {
		Properties sysProps = System.getProperties();
		sysProps.put("proxySet", enabled ? "true" : "false"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		updateSystemProperties();
		try {
			Activator.getInstance().getInstancePreferences().flush();
		} catch (BackingStoreException e) {
			Activator.logError(
					"An error occurred while writing out the enablement state", e); //$NON-NLS-1$
		}
		String[] hosts = getNonProxiedHosts();
		IProxyData[] data = getProxyData();
		IProxyChangeEvent event = new ProxyChangeEvent(IProxyChangeEvent.PROXY_DATA_CHANGED, hosts, hosts, data, data);
		fireChange(event);
	}

	private void updateSystemProperties() {
		for (int i = 0; i < proxies.length; i++) {
			ProxyType type = proxies[i];
			type.updateSystemProperties(internalGetProxyData(type.getName(), ProxyType.DO_NOT_VERIFY), isProxiesEnabled());
		}
	}

	public void initialize() {
		// First see if there is an http proxy to migrate
		migrateUpdateHttpProxy(new InstanceScope().getNode(""), true); //$NON-NLS-1$
		((IEclipsePreferences)Activator.getInstance().getInstancePreferences()).addPreferenceChangeListener(this);
		// Now initialize each proxy type
		for (int i = 0; i < proxies.length; i++) {
			ProxyType type = proxies[i];
			type.initialize(isProxiesEnabled());
		}
		registerAuthenticator();
	}

	public IProxyData getProxyData(String type) {
		return internalGetProxyData(type, ProxyType.VERIFY_EQUAL);
	}

	private IProxyData internalGetProxyData(String type, int verifySystemProperties) {
		for (int i = 0; i < proxies.length; i++) {
			ProxyType pt = proxies[i];
			if (pt.getName().equals(type)) {
				return pt.getProxyData(verifySystemProperties);
			}
		}
		return null;
	}

	public IProxyData[] getProxyDataForHost(String host) {
		if (isHostFiltered(host))
			return new IProxyData[0];
		IProxyData[] data = getProxyData();
		List result = new ArrayList();
		for (int i = 0; i < data.length; i++) {
			IProxyData proxyData = data[i];
			if (proxyData.getHost() != null)
				result.add(proxyData);
		}
		return (IProxyData[]) result.toArray(new IProxyData[result.size()]);
	}

	private boolean isHostFiltered(String host) {
		String[] filters = getNonProxiedHosts();
		for (int i = 0; i < filters.length; i++) {
			String filter = filters[i];
			if (matchesFilter(host, filter))
				return true;
		}
		return false;
	}

	private boolean matchesFilter(String host, String filter) {
		StringMatcher matcher = new StringMatcher(filter, false, false);
		return matcher.match(host);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.net.core.IProxyManager#getProxyDataForHost(java.lang.String, java.lang.String)
	 */
	public IProxyData getProxyDataForHost(String host, String type) {
		IProxyData[] data = getProxyDataForHost(host);
		for (int i = 0; i < data.length; i++) {
			IProxyData proxyData = data[i];
			if (proxyData.getType().equals(type) && proxyData.getHost() != null)
				return proxyData;
		}
		return null;
	}
	
	private void registerAuthenticator() {
		Authenticator a = getPluggedInAuthenticator();
		if (a != null) {
			Authenticator.setDefault(a);
		}
	}
	
	private Authenticator getPluggedInAuthenticator() {
		IExtension[] extensions = Platform.getExtensionRegistry().getExtensionPoint(Activator.ID, Activator.PT_AUTHENTICATOR).getExtensions();
		if (extensions.length == 0)
			return null;
		IExtension extension = extensions[0];
		IConfigurationElement[] configs = extension.getConfigurationElements();
		if (configs.length == 0) {
			Activator.log(IStatus.ERROR, NLS.bind("Authenticator {0} is missing required fields", (new Object[] {extension.getUniqueIdentifier()})), null);//$NON-NLS-1$ 
			return null;
		}
		try {
			IConfigurationElement config = configs[0];
			return (Authenticator) config.createExecutableExtension("class");//$NON-NLS-1$ 
		} catch (CoreException ex) {
			Activator.log(IStatus.ERROR, NLS.bind("Unable to instantiate authenticator {0}", (new Object[] {extension.getUniqueIdentifier()})), ex);//$NON-NLS-1$ 
			return null;
		}
	}
	
	void migrateUpdateHttpProxy(Preferences node, boolean isInitialize) {
		Preferences netPrefs = node.node(Activator.ID);
		if (!netPrefs.getBoolean(PREF_HAS_MIGRATED, false)) {
			// Only set the migration bit when initializing
			if (isInitialize)
				netPrefs.putBoolean(PREF_HAS_MIGRATED, true);
			Preferences updatePrefs = node.node("org.eclipse.update.core"); //$NON-NLS-1$
			String httpProxyHost = getHostToMigrate(updatePrefs, isInitialize /* checkSystemProperties */);
			int port = getPortToMigrate(updatePrefs, isInitialize /* checkSystemProperties */);
			boolean httpProxyEnable = getEnablementToMigrate(updatePrefs, isInitialize /* checkSystemProperties */);
			if (httpProxyHost != null) {
				ProxyData proxyData = new ProxyData(IProxyData.HTTP_PROXY_TYPE, httpProxyHost, port, false);
				ProxyType type = getType(proxyData);
				type.updatePreferencesIfMissing(netPrefs, proxyData);
				if (httpProxyEnable) {
					netPrefs.putBoolean(ProxyManager.PREF_ENABLED, true);
				}
			}
		}
	}

	private boolean getEnablementToMigrate(Preferences updatePrefs, boolean checkSystemProperties) {
		boolean httpProxyEnable = false;
		if (checkSystemProperties && updatePrefs.get(HTTP_PROXY_ENABLE, null) == null) {
			httpProxyEnable = Boolean.getBoolean("http.proxySet"); //$NON-NLS-1$
		} else {
			httpProxyEnable = updatePrefs.getBoolean(HTTP_PROXY_ENABLE, false);
			updatePrefs.remove(HTTP_PROXY_ENABLE);
		}
		return httpProxyEnable;
	}

	private int getPortToMigrate(Preferences updatePrefs, boolean checkSystemProperties) {
		String httpProxyPort = updatePrefs.get(HTTP_PROXY_PORT, ""); //$NON-NLS-1$
		if (checkSystemProperties && "".equals(httpProxyPort)) { //$NON-NLS-1$
			httpProxyPort = System.getProperty("http.proxyPort", ""); //$NON-NLS-1$ //$NON-NLS-2$
		}
		updatePrefs.remove(HTTP_PROXY_PORT);
		int port = -1;
		if (httpProxyPort != null && !"".equals(httpProxyPort)) //$NON-NLS-1$
			try {
				port = Integer.parseInt(httpProxyPort);
			} catch (NumberFormatException e) {
				// Ignore
			}
		return port;
	}

	private String getHostToMigrate(Preferences updatePrefs, boolean checkSystemProperties) {
		String httpProxyHost = updatePrefs.get(HTTP_PROXY_HOST, ""); //$NON-NLS-1$
		if (checkSystemProperties && "".equals(httpProxyHost)) { //$NON-NLS-1$
			httpProxyHost = System.getProperty("http.proxyHost", ""); //$NON-NLS-1$ //$NON-NLS-2$
		}
		if ("".equals(httpProxyHost)) //$NON-NLS-1$
			httpProxyHost = null;
		updatePrefs.remove(HTTP_PROXY_HOST);
		return httpProxyHost;
	}

	public void preferenceChange(PreferenceChangeEvent event) {
		if (event.getKey().equals(PREF_ENABLED)) {
			internalSetEnabled(Activator.getInstance().getInstancePreferences().getBoolean(PREF_ENABLED, false));
		}
	}

}
