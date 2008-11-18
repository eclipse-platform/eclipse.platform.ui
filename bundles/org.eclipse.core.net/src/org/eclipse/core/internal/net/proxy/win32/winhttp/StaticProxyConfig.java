/*******************************************************************************
 * Copyright (c) 2008 compeople AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 	compeople AG (Stefan Liebig) - initial API and implementation
 *  IBM Corporation - handling URI without a scheme by select(URI, List) (bug 246065)
 *  IBM Corporation - Add proxy providers layer on the top of ProxyManager (bug 255616)
 *******************************************************************************/
package org.eclipse.core.internal.net.proxy.win32.winhttp;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.net.proxy.IProxyData;

/**
 * StaticProxyConfig wraps certain information of WinHttpCurrentIEProxyConfig,
 * i.e. the Windows specific list of proxies and the proxy bypass list.
 */
public class StaticProxyConfig {

	private List universalProxies = new ArrayList();
	private Map protocolSpecificProxies = new HashMap();
	private ProxyBypass proxyBypass;

	/**
	 * @param proxiesString
	 * @param proxyBypassString
	 */
	public StaticProxyConfig(String proxiesString, String proxyBypassString) {
		ProxyProviderUtil.fillProxyLists(proxiesString, universalProxies,
				protocolSpecificProxies);
		proxyBypass = new ProxyBypass(proxyBypassString);
	}

	/**
	 * Select the static proxies for the given uri and add it to the given list
	 * of proxies.<br>
	 * This respects also the proxy bypass definition.
	 * 
	 * @param uri
	 * @param proxies
	 */
	public void select(URI uri, List proxies) {
		if (proxyBypass.bypassProxyFor(uri))
			return;

		if (!protocolSpecificProxies.isEmpty()) {
			if (uri.getScheme() != null) {
				List protocolProxies = (List) protocolSpecificProxies.get(uri
						.getScheme().toUpperCase());
				if (protocolProxies == null)
					return;
				proxies.addAll(protocolProxies);
			} else {
				Iterator it = protocolSpecificProxies.values().iterator();
				while (it.hasNext()) {
					List protocolProxies = (List) it.next();
					if (protocolProxies == null)
						return;
					proxies.addAll(protocolProxies);
				}
			}
		} else
			proxies.addAll(universalProxies);
	}

	public IProxyData[] getProxyData() {
		List proxies = new ArrayList();
		Iterator it = protocolSpecificProxies.values().iterator();
		while (it.hasNext()) {
			List protocolProxies = (List) it.next();
			if (protocolProxies != null) {
				proxies.addAll(protocolProxies);
			}
		}
		return (IProxyData[]) proxies.toArray(new IProxyData[0]);
	}

	public String[] getNonProxiedHosts() {
		return proxyBypass.getNonProxiedHosts();
	}

}
