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

/**
 * Wrapper for Win32 WINHTTP_CURRENT_USER_IE_PROXY_CONFIG structure.
 * <p>
 * The fields will be written by the jni glue code.
 * </p>
 */
public class WinHttpCurrentUserIEProxyConfig {

	public boolean isAutoDetect;

	public String autoConfigUrl;

	public String proxy;

	public String proxyBypass;

	/**
	 * @return the autoConfigUrl
	 */
	public String getAutoConfigUrl() {
		return autoConfigUrl;
	}

	/**
	 * @return the isAutoDetect
	 */
	public boolean isAutoDetect() {
		return isAutoDetect;
	}

	/**
	 * @return the proxy
	 */
	public String getProxy() {
		return proxy;
	}

	/**
	 * @return the proxyBypass
	 */
	public String getProxyBypass() {
		return proxyBypass;
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj instanceof WinHttpCurrentUserIEProxyConfig) {
			WinHttpCurrentUserIEProxyConfig that= (WinHttpCurrentUserIEProxyConfig)obj;
			return (this.isAutoDetect == that.isAutoDetect) && equals(this.autoConfigUrl, that.autoConfigUrl) && equals(this.proxy, that.proxy) && equals(this.proxyBypass, that.proxyBypass);
		}

		return false;
	}

	/**
	 * Tests equality of the given strings.
	 * 
	 * @param sequence1 candidate 1, may be null
	 * @param sequence2 candidate 2, may be null
	 * @return true if both sequences are null or the sequences are equal
	 */
	private static final boolean equals(final CharSequence sequence1, final CharSequence sequence2) {
		if (sequence1 == sequence2) {
			return true;
		} else if (sequence1 == null || sequence2 == null) {
			return false;
		} else {
			return sequence1.equals(sequence2);
		}
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return (autoConfigUrl + proxy).hashCode();
	}

}
