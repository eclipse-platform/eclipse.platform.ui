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
package org.eclipse.core.internal.net;

import java.net.URI;

import org.eclipse.core.net.proxy.IProxyData;

/**
 * Returns proxies to use.
 */
public abstract class AbstractProxyProvider {

	/**
	 * Returns proxies to use with the given URI. Returns empty array
	 * when there is no appropriate proxy.
	 * 
	 * @param uri
	 *            the URI that a connection is required to
	 * @return an array of proxies for the given URI
	 */
	public IProxyData[] select(URI uri) {
		String[] nonProxyHosts = getNonProxiedHosts();
		
		if (nonProxyHosts != null) {
			String host = uri.getHost();
			for (int npIndex = 0; npIndex < nonProxyHosts.length; npIndex++) {
				if (host.equals(nonProxyHosts[npIndex])) {
					return new IProxyData[0];
				}
			}
		}

		return getProxyData(uri);
	}

	protected abstract IProxyData[] getProxyData(URI uri);

	protected String[] getNonProxiedHosts() {
		return new String[] {};
	}
}
