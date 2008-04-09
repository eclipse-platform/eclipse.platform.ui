/*******************************************************************************
 * Copyright (c) 2008 compeople AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 	compeople AG (Stefan Liebig) - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.net.proxy.win32.winhttp;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * WinHttpConfig unwraps the information from WinHttpCurrentIEProxyConfig.
 * <p>
 * The fields will be written by the jni glue code.
 * </p>
 */
public class WinHttpConfig {

	public List universalProxies= new ArrayList();

	public Map protocolSpecificProxies= new HashMap();

	public ProxyBypass proxyBypass;

	/**
	 * @param proxyConfig
	 */
	public WinHttpConfig(WinHttpCurrentUserIEProxyConfig proxyConfig) {
		ProxySelectorUtils.fillProxyLists(proxyConfig.getProxy(), universalProxies, protocolSpecificProxies);
		proxyBypass= new ProxyBypass(proxyConfig.getProxyBypass());
	}

	public boolean useUniversalProxy() {
		return !universalProxies.isEmpty();
	}

	public boolean useProtocolSpecificProxies() {
		return !protocolSpecificProxies.isEmpty();
	}

	public List getProtocolSpecificProxies(URI uri) {
		return (List)protocolSpecificProxies.get(uri.getScheme());
	}

	public List getUniversalProxies() {
		return universalProxies;
	}

	public boolean bypassProxyFor(URI uri) {
		return proxyBypass.bypassProxyFor(uri);
	}
}
