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
import java.util.regex.Pattern;

/**
 * Encapsulates the windows specific proxy bypass list. It transforms the native
 * API proxy bypass list into queryable java object.
 * 
 * @see "http://msdn2.microsoft.com/en-us/library/aa383912(VS.85).aspx"
 */
public class ProxyBypass {

	private final String proxyBypass;

	private final Pattern proxyBypassPattern;

	private final static String BYPASS_LOCAL_ADDESSES_TOKEN = "<local>"; //$NON-NLS-1$

	/**
	 * Create a ProxyBypass instance from the proxy bypass list string.
	 * 
	 * @param proxyBypass
	 */
	public ProxyBypass(String proxyBypass) {
		this.proxyBypass = proxyBypass != null ? proxyBypass : ""; //$NON-NLS-1$

		if (proxyBypass != null) {
			String regExp = ProxyProviderUtil.replace(proxyBypass, ";", "|"); //$NON-NLS-1$ //$NON-NLS-2$
			regExp = ProxyProviderUtil.replace(regExp, ".", "\\."); //$NON-NLS-1$ //$NON-NLS-2$
			regExp = ProxyProviderUtil.replace(regExp, "*", ".*"); //$NON-NLS-1$ //$NON-NLS-2$
			this.proxyBypassPattern = Pattern.compile(regExp);
		} else {
			this.proxyBypassPattern = Pattern.compile(""); //$NON-NLS-1$
		}
	}

	/**
	 * Check whether the given uri should bypass the proxy.
	 * 
	 * @param uri
	 * @return true if the the uri should bypass the proxy; otherwise false
	 */
	public boolean bypassProxyFor(URI uri) {
		final String host = uri.getHost();
		if (host == null)
			return false;
		return (isLocal(host) && isBypassLocalAddresses(proxyBypass))
				|| isInBypassList(host);
	}

	/**
	 * @param proxyBypass
	 * @param uri
	 * @return
	 */
	private boolean isInBypassList(String host) {
		return proxyBypassPattern.matcher(host).matches();
	}

	/**
	 * @param uri
	 * @return
	 */
	private static boolean isLocal(String host) {
		return host.indexOf(".") == -1; //$NON-NLS-1$
	}

	/**
	 * @param addressListString
	 * @return
	 */
	private static boolean isBypassLocalAddresses(String proxyBypass) {
		return proxyBypass.indexOf(BYPASS_LOCAL_ADDESSES_TOKEN) != -1;
	}

}
