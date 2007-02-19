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
package org.eclipse.net.core;

/**
 * Manages the proxy data and related information.
 * <p>
 * This interface is not intended to be implemented by clients. 
 * @since 1.0
 */
public interface IProxyManager {
	
	/**
	 * Set whether proxy support should be enabled. When disabled, the data for all the 
	 * known proxy types will contain a <code>null</code> in the host field 
	 * (see {@link IProxyData#getHost()}). When enabled, the proxy data will not have any
	 * values until the next time it is set (see {@link #setProxyData(IProxyData[])}).
	 * @param enabled whether proxy support should be enabled
	 */
	void setProxiesEnabled(boolean enabled);
	
	/**
	 * Return whether proxy support should be enabled. When disabled, all connections
	 * will be direct.
	 * @return whether proxy support should be enabled
	 */
	boolean isProxiesEnabled();
	
	/**
	 * Return the list of know proxy types and their settings.
	 * Some of the returned proxy types may not be enabled (i.e,
	 * their hosts may be <code>null</code>.
	 * @return the list of know proxy types and their settings
	 */
	IProxyData[] getProxyData();
	
	/**
	 * Return the list of known proxy types and their settings for the
	 * given host. If proxies are disabled
	 * or if the host matches any entries in the non-proxied
	 * hosts lists or if a matching proxy type has no data, then
	 * an empty array is returned.
	 * @param host the host for which a connection is desired
	 * @return the list of known proxy types and their settings for the
	 * given host
	 */
	IProxyData[] getProxyDataForHost(String host);
	
	/**
	 * Return the proxy data for the proxy of the given type
	 * or <code>null</code> if the proxy type is not known by this
	 * manager.
	 * @param type the proxy type
	 * @return the proxy data for the proxy of the given type
	 * or <code>null</code>
	 * @see IProxyData#HTTP_PROXY_TYPE
	 * @see IProxyData#HTTPS_PROXY_TYPE
	 * @see IProxyData#SOCKS_PROXY_TYPE
	 */
	IProxyData getProxyData(String type);
	
	/**
	 * Return the proxy data for the proxy of the given type
	 * or <code>null</code> if the proxy type is not known by this
	 * manager, the proxy data is empty for that type or the
	 * host is in the non-proxied host list.
	 * @param host the host for which a connection is desired
	 * @param type the proxy type
	 * @return the proxy data for the proxy of the given type
	 * or <code>null</code>
	 * @see IProxyData#HTTP_PROXY_TYPE
	 * @see IProxyData#HTTPS_PROXY_TYPE
	 * @see IProxyData#SOCKS_PROXY_TYPE
	 */
	IProxyData getProxyDataForHost(String host, String type);
	
	/**
	 * Set the information associated with known proxy types.
	 * If an unknown type is provided, it will be ignored. Any
	 * known types that are not present in the list of the provided
	 * proxy data will be unaffected. Calls to this method when proxies
	 * are disabled will be ignored.
	 * @param proxies the proxy data whose information is to be set.
	 */
	void setProxyData(IProxyData[] proxies);
	
	/**
	 * Return the list of hosts for which non proxy should be used.
	 * The values returned from this method should only be used for displaying
	 * the non-proxed hosts list. Clients that which to make a connection and need
	 * to determine whether to use a proxy or not shoudl call either {@link #getProxyDataForHost(String)}
	 * or {@link #getProxyDataForHost(String, String)}.
	 * @return the list of hosts for which non proxy should be used.
	 * @see #getProxyDataForHost(String)
	 * @see #getProxyDataForHost(String, String)
	 */
	String[] getNonProxiedHosts();
	
	/**
	 * Set the list of hosts for which non proxy should be used.
	 * @param hosts the list of hosts for which non proxy should be used.
	 */
	void setNonProxiedHosts(String[] hosts);
	
	/**
	 * Register a listener that will be notified when proxy related
	 * information changes. Adding a listener that is already registered
	 * has no effect.
	 * @param listener a change listener
	 */
	void addProxyChangeListener(IProxyChangeListener listener);
	
	/**
	 * Remove a listener. Removing a listener that is not registered
	 * has no effect.
	 * @param listener a change listener
	 */
	void removeProxyChangeListener(IProxyChangeListener listener);

}
