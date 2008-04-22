/*******************************************************************************
 * Copyright (c) 2008 Oakland Software Incorporated and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Oakland Software Incorporated - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.net.proxy.unix;

import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.core.internal.net.AbstractProxyProvider;
import org.eclipse.core.internal.net.ProxyData;
import org.eclipse.core.net.proxy.IProxyData;

public class UnixProxyProvider extends AbstractProxyProvider {

	static {
		try {
			System.loadLibrary("libproxysupport"); //$NON-NLS-1$
		} catch (UnsatisfiedLinkError ex) {
			// This will happen on systems that are missing Gnome libraries
		}
	}

	public UnixProxyProvider() {
		// Nothing to initialize
	}

	public IProxyData[] getProxyData(URI uri) {
		String protocol = uri.getScheme();

		ProxyData pd = getSystemProxyInfo(protocol);

		if (pd != null) {
			IProxyData[] pds = new IProxyData[1];
			pds[0] = pd;
			return pds;
		}

		return new IProxyData[0];
	}

	protected String[] getNonProxiedHosts() {
		try {
			String[] npHosts = getGConfNonProxyHosts();
			if (npHosts != null && npHosts.length > 0)
				return npHosts;
			return getKdeNonProxyHosts();
		} catch (UnsatisfiedLinkError ex) {
			// This has already been reported (the native code did not load)
		}
		return new String[] {};
	}

	// Returns null if something wrong or there is no proxy for the protocol
	protected ProxyData getSystemProxyInfo(String protocol) {
		ProxyData pd = null;

		// First try the environment variable which is a URL
		// TODO: native calls for system properties, since System#getenx is
		// deprecated in 1.4
		String sysHttp = null;
		// System.getenv(protocol.toLowerCase() + "_proxy"); //$NON-NLS-1$
		if (sysHttp != null) {
			URI uri = null;
			try {
				uri = new URI(sysHttp);
			} catch (URISyntaxException e) {
				return null;
			}

			pd = new ProxyData(protocol);
			pd.setHost(uri.getHost());
			pd.setPort(uri.getPort());
			return pd;
		}

		try {
			// Then ask Gnome
			pd = getGConfProxyInfo(protocol);

			if (pd != null)
				return pd;

			// Then ask KDE
			pd = getKdeProxyInfo(protocol);
			if (pd != null)
				return pd;
		} catch (UnsatisfiedLinkError ex) {
			// This has already been reported when the native code did not load
		}

		return null;
	}

	protected static native ProxyData getGConfProxyInfo(String protocol);

	protected static native String[] getGConfNonProxyHosts();

	protected static native ProxyData getKdeProxyInfo(String protocol);

	protected static native String[] getKdeNonProxyHosts();

}
