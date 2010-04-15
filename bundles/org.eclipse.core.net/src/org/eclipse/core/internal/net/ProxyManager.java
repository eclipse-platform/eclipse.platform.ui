/*******************************************************************************
 * Copyright (c) 2007, 2010 IBM Corporation and others.
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
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.eclipse.core.net.proxy.IProxyChangeEvent;
import org.eclipse.core.net.proxy.IProxyChangeListener;
import org.eclipse.core.net.proxy.IProxyData;
import org.eclipse.core.net.proxy.IProxyService;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.RegistryFactory;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent;
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
	private static final String PREF_OS = "systemProxiesEnabled"; //$NON-NLS-1$
	
	private static IProxyService proxyManager;
	
	private AbstractProxyProvider nativeProxyProvider;
	
	ListenerList listeners = new ListenerList(ListenerList.IDENTITY);
	private String[] nonProxiedHosts;
	private final ProxyType[] proxies = new ProxyType[] {
			new ProxyType(IProxyData.HTTP_PROXY_TYPE),
			new ProxyType(IProxyData.HTTPS_PROXY_TYPE),
			new ProxyType(IProxyData.SOCKS_PROXY_TYPE)
		};

	private boolean migrated = false;

	private ProxyManager() {
		try {
			nativeProxyProvider = (AbstractProxyProvider) Class.forName(
					"org.eclipse.core.net.ProxyProvider").newInstance(); //$NON-NLS-1$
		} catch (ClassNotFoundException e) {
			// no class found
		} catch (Exception e) {
			Activator.logInfo("Problems occured during the proxy provider initialization.", e); //$NON-NLS-1$
		}
	}

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
		checkMigrated();
		if (nonProxiedHosts == null) {
			String prop = Activator.getInstance().getPreferences().get(PREF_NON_PROXIED_HOSTS, "localhost|127.0.0.1"); //$NON-NLS-1$
			nonProxiedHosts = ProxyType.convertPropertyStringToHosts(prop);
		}
		if (nonProxiedHosts.length == 0)
			return nonProxiedHosts;
		String[] result = new String[nonProxiedHosts.length];
		System.arraycopy(nonProxiedHosts, 0, result, 0, nonProxiedHosts.length );
		return result;
	}

	public String[] getNativeNonProxiedHosts() {
		if (hasSystemProxies()) {
			return nativeProxyProvider.getNonProxiedHosts();
		}
		return new String[0];
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.net.IProxyManager#setNonProxiedHosts(java.lang.String[])
	 */
	public void setNonProxiedHosts(String[] hosts) {
		checkMigrated();
		Assert.isNotNull(hosts);
		for (int i = 0; i < hosts.length; i++) {
			String host = hosts[i];
			Assert.isNotNull(host);
			Assert.isTrue(host.length() > 0);
		}
		String[] oldHosts = nonProxiedHosts;
		nonProxiedHosts = hosts;
		Activator.getInstance().getPreferences().put(PREF_NON_PROXIED_HOSTS, ProxyType.convertHostsToPropertyString(nonProxiedHosts));
		try {
			Activator.getInstance().getPreferences().flush();
		} catch (BackingStoreException e) {
			Activator.logError(
					"An error occurred while writing out the non-proxied hosts list", e); //$NON-NLS-1$
		}
		IProxyData[] data = getProxyData();
		IProxyChangeEvent event = new ProxyChangeEvent(IProxyChangeEvent.NONPROXIED_HOSTS_CHANGED, oldHosts, getNonProxiedHosts(), data, new IProxyData[0]);
		updateSystemProperties();
		fireChange(event);
	}

	
	public IProxyData[] getProxyData() {
		checkMigrated();
		IProxyData[] result = new IProxyData[proxies.length];
		for (int i = 0; i < proxies.length; i++) {
			ProxyType type = proxies[i];
			result[i] = type.getProxyData(ProxyType.VERIFY_EQUAL);
		}
		return resolveType(result);
	}

	public IProxyData[] getNativeProxyData() {
		if (hasSystemProxies()) {
			return resolveType(nativeProxyProvider.getProxyData());
		}
		return new IProxyData[0];
	}

	public void setProxyData(IProxyData[] proxies) {
		checkMigrated();
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
			if (type != null && type.setProxyData(proxyData)) {
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
		checkMigrated();
		return internalIsProxiesEnabled()
				&& (!isSystemProxiesEnabled() || (isSystemProxiesEnabled() && hasSystemProxies()));
	}

	private boolean internalIsProxiesEnabled() {
		return Activator.getInstance().getPreferences().getBoolean(
				PREF_ENABLED, true);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.net.IProxyManager#setProxiesEnabled(boolean)
	 */
	public void setProxiesEnabled(boolean enabled) {
		checkMigrated();
		boolean current = internalIsProxiesEnabled();
		if (current == enabled)
			return;
		// Setting the preference will trigger the system property update
		// (see preferenceChange)
		Activator.getInstance().getPreferences().putBoolean(PREF_ENABLED, enabled);
	}

	private void internalSetEnabled(boolean enabled, boolean systemEnabled) {
		Properties sysProps = System.getProperties();
		sysProps.put("proxySet", enabled ? "true" : "false"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		sysProps.put("systemProxySet", systemEnabled ? "true" : "false"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		updateSystemProperties();
		try {
			Activator.getInstance().getPreferences().flush();
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
			type.updateSystemProperties(internalGetProxyData(type.getName(), ProxyType.DO_NOT_VERIFY));
		}
	}

	public void initialize() {
		checkMigrated();
		((IEclipsePreferences)Activator.getInstance().getPreferences()).addPreferenceChangeListener(this);
		// Now initialize each proxy type
		for (int i = 0; i < proxies.length; i++) {
			ProxyType type = proxies[i];
			type.initialize();
		}
		registerAuthenticator();
	}

	public IProxyData getProxyData(String type) {
		checkMigrated();
		return resolveType(internalGetProxyData(type, ProxyType.VERIFY_EQUAL));
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
		checkMigrated();
		if (!internalIsProxiesEnabled()) {
			return new IProxyData[0];
		}
		URI uri = tryGetURI(host);
		if (uri == null) {
			return new IProxyData[0];
		}
		if (hasSystemProxies() && isSystemProxiesEnabled()) {
			return resolveType(nativeProxyProvider.select(uri));
		}

		if (isHostFiltered(uri))
			return new IProxyData[0];
		IProxyData[] data = getProxyData();
		List result = new ArrayList();
		for (int i = 0; i < data.length; i++) {
			IProxyData proxyData = data[i];
			if (proxyData.getHost() != null)
				result.add(proxyData);
		}
		IProxyData ret[] = (IProxyData[]) result.toArray(new IProxyData[result.size()]);
		return resolveType(ret);
	}

	public static URI tryGetURI(String host) {
		try {
			int i = host.indexOf(":"); //$NON-NLS-1$
			if (i == -1) {
				return new URI("//" + host); //$NON-NLS-1$
			}
			return new URI(host.substring(i + 1));
		} catch (URISyntaxException e) {
			return null;
		}
	}

	private boolean isHostFiltered(URI uri) {
		String[] filters = getNonProxiedHosts();
		for (int i = 0; i < filters.length; i++) {
			String filter = filters[i];
			if (matchesFilter(uri.getHost(), filter))
				return true;
		}
		return false;
	}

	private boolean matchesFilter(String host, String filter) {
		StringMatcher matcher = new StringMatcher(filter, true, false);
		return matcher.match(host);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.net.core.IProxyManager#getProxyDataForHost(java.lang.String, java.lang.String)
	 */
	public IProxyData getProxyDataForHost(String host, String type) {
		checkMigrated();
		if (!internalIsProxiesEnabled()) {
			return null;
		}
		if (hasSystemProxies() && isSystemProxiesEnabled())
			try {
				URI uri = new URI(type, "//" + host, null); //$NON-NLS-1$
				IProxyData[] proxyDatas = nativeProxyProvider.select(uri);
				return proxyDatas.length > 0 ? resolveType(nativeProxyProvider.select(uri)[0]) : null;
			} catch (URISyntaxException e) {
				return null;
			}
			
		IProxyData[] data = getProxyDataForHost(host);
		for (int i = 0; i < data.length; i++) {
			IProxyData proxyData = data[i];
			if (proxyData.getType().equalsIgnoreCase(type)
					&& proxyData.getHost() != null)
				return resolveType(proxyData);
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
		IExtension[] extensions = RegistryFactory.getRegistry().getExtensionPoint(Activator.ID, Activator.PT_AUTHENTICATOR).getExtensions();
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

	
	private synchronized void checkMigrated() {
		if (migrated || !Activator.getInstance().instanceLocationAvailable())
			return;
		
		migrated = true;
		if (Activator.getInstance().getPreferences().getBoolean(PREF_HAS_MIGRATED, false))
			return;
		
		Activator.getInstance().getPreferences().putBoolean(PREF_HAS_MIGRATED, true);
		migrateInstanceScopePreferences(new InstanceScope().getNode(""), new ConfigurationScope().getNode(""), true);  //$NON-NLS-1$//$NON-NLS-2$
	}
	
	void migrateInstanceScopePreferences(Preferences instanceNode, Preferences configurationNode, boolean isInitialize) {
		migrateUpdateHttpProxy(instanceNode, isInitialize);
		
		Preferences netInstancePrefs = instanceNode.node(Activator.ID);
		Preferences netConfigurationPrefs = configurationNode.node(Activator.ID);
		
		// migrate enabled status
		if (netConfigurationPrefs.get(PREF_ENABLED, null) == null) {
			String instanceEnabled = netInstancePrefs.get(PREF_ENABLED, null);
			if (instanceEnabled != null)
				netConfigurationPrefs.put(PREF_ENABLED, instanceEnabled);
		}
		
		// migrate enabled status
		if (netConfigurationPrefs.get(PREF_OS, null) == null) {
			String instanceEnabled = netInstancePrefs.get(PREF_OS, null);
			if (instanceEnabled != null)
				netConfigurationPrefs.put(PREF_OS, instanceEnabled);
		}
	
		// migrate non proxied hosts if not already set
		if (netConfigurationPrefs.get(PREF_NON_PROXIED_HOSTS, null) == null) {
			String instanceNonProxiedHosts = netInstancePrefs.get(PREF_NON_PROXIED_HOSTS, null);
			if (instanceNonProxiedHosts != null) {
				netConfigurationPrefs.put(PREF_NON_PROXIED_HOSTS, instanceNonProxiedHosts);
				nonProxiedHosts = null;
			}
		}
		
		// migrate proxy data
		for (int i = 0; i < proxies.length; i++) {
			ProxyType type = proxies[i];
			IProxyData data = type.getProxyData(ProxyType.DO_NOT_VERIFY);
			if (data.getHost() == null) {
				ProxyType instanceType = new ProxyType(type.getName(),netInstancePrefs);
				IProxyData instanceData = instanceType.getProxyData(ProxyType.DO_NOT_VERIFY);
				if (instanceData.getHost() != null)
					type.setProxyData(instanceData);
			}
		}
		
		// if this an import we should remove the old node
		if (! isInitialize) {
			try {
				netInstancePrefs.removeNode();
			} catch (BackingStoreException e) {
				// ignore
			}
		}			
	}
	
	private void migrateUpdateHttpProxy(Preferences node, boolean isInitialize) {
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
				ProxyData proxyData = new ProxyData(IProxyData.HTTP_PROXY_TYPE,
						httpProxyHost, port, false, null);
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
		if (event.getKey().equals(PREF_ENABLED) || event.getKey().equals(PREF_OS)) {
			checkMigrated();
			internalSetEnabled(Activator.getInstance().getPreferences().getBoolean(PREF_ENABLED, true),
					Activator.getInstance().getPreferences().getBoolean(PREF_OS, true));
		}
	}

	public boolean hasSystemProxies() {
		return nativeProxyProvider != null;
	}

	public boolean isSystemProxiesEnabled() {
		checkMigrated();
		return Activator.getInstance().getPreferences().getBoolean(PREF_OS,
				true);
	}

	public void setSystemProxiesEnabled(boolean enabled) {
		checkMigrated();
		boolean current = isSystemProxiesEnabled();
		if (current == enabled)
			return;
		// Setting the preference will trigger the system property update
		// (see preferenceChange)
		Activator.getInstance().getPreferences().putBoolean(PREF_OS, enabled);
	}

	public IProxyData[] select(URI uri) {
		IProxyData data = getProxyDataForHost(uri.getHost(), uri.getScheme());
		if (data != null) {
			return resolveType(new IProxyData[] { data });
		}
		return new IProxyData[0];
	}

	public IProxyData resolveType(IProxyData data) {
		if (data == null) {
			return null;
		}
		ProxyData d = (ProxyData) data;
		if (d.getType().equalsIgnoreCase(IProxyData.HTTP_PROXY_TYPE)) {
			d.setType(IProxyData.HTTP_PROXY_TYPE);
		} else if (d.getType().equalsIgnoreCase(IProxyData.HTTPS_PROXY_TYPE)) {
			d.setType(IProxyData.HTTPS_PROXY_TYPE);
		} else if (d.getType().equalsIgnoreCase(IProxyData.SOCKS_PROXY_TYPE)) {
			d.setType(IProxyData.SOCKS_PROXY_TYPE);
		}
		return d;
	}

	public IProxyData[] resolveType(IProxyData[] data) {
		if (data == null) {
			return null;
		}
		for (int i = 0; i < data.length; i++) {
			resolveType(data[i]);
		}
		return data;
	}

}
