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
import java.util.Collections;
import java.util.List;

import org.eclipse.core.internal.net.Activator;
import org.eclipse.core.internal.net.ProxyData;
import org.eclipse.core.net.proxy.IProxyData;

/**
 * The provider that gets its settings from the "internet options >> connection settings". For this
 * it uses the Windows WinHttp API.
 * 
 * @see "http://msdn2.microsoft.com/en-us/library/aa382925(VS.85).aspx"
 */
public class WinHttpProxyProvider {

	private WinHttpCurrentUserIEProxyConfig proxyConfig= null;

	private WinHttpConfig winHttpConfig;

	private String wpadAutoConfigUrl;

	private boolean retryWpad= false;

	private static final ProxyData[] EMPTY_PROXIES= new ProxyData[0];

	private static final String MY_NAME= WinHttpProxyProvider.class.getName();

	/**
	 * Retrieve the proxies that are suitable for the given uri. An empty array of proxies indicates
	 * that no proxy should be used. This method considers already the ´no proxy for´ definition of
	 * the internet options dialog.
	 * 
	 * @param uri
	 * @return an array of proxies
	 */
	public IProxyData[] getProxyData(URI uri) {
		WinHttpCurrentUserIEProxyConfig newProxyConfig= new WinHttpCurrentUserIEProxyConfig();
		if (!WinHttp.getIEProxyConfigForCurrentUser(newProxyConfig)) {
			Activator.logError("WinHttp.GetIEProxyConfigForCurrentUser failed with error '" + WinHttp.getLastErrorMessage() + "' #" + WinHttp.getLastError() + ".", null); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			return EMPTY_PROXIES;
		}

		if (proxyConfig == null) {
			proxyConfig= newProxyConfig;
			retryWpad= proxyConfig.isAutoDetect();
		}

		// Let´s find out if auto detect has changed.
		if (newProxyConfig.isAutoDetect() != proxyConfig.isAutoDetect())
			retryWpad= proxyConfig.isAutoDetect();

		boolean proxyConfigChanged= !newProxyConfig.equals(proxyConfig);

		if (proxyConfigChanged)
			proxyConfig= newProxyConfig;

		List proxies= new ArrayList();

		// Explicit proxies defined?
		if (proxyConfig.getProxy() != null && proxyConfig.getProxy().length() != 0) {
			// Yes, let´s see if we are still up-to-date or not yet initialized.
			if (proxyConfigChanged || winHttpConfig == null) {
				winHttpConfig= new WinHttpConfig(proxyConfig);
			}

			if (!winHttpConfig.bypassProxyFor(uri)) {
				if (winHttpConfig.useProtocolSpecificProxies()) {
					List protocolSpecificProxies= winHttpConfig.getProtocolSpecificProxies(uri);
					if (protocolSpecificProxies != null) {
						proxies.addAll(protocolSpecificProxies);
					}
				} else {
					proxies.addAll(winHttpConfig.getUniversalProxies());
				}
			}
		}

		boolean isPac= proxyConfig.getAutoConfigUrl() != null;
		boolean isWpad= proxyConfig.isAutoDetect();

		if (!isPac && !isWpad)
			return toArray(proxies);

		// Create the WinHTTP session.
		int hHttpSession= WinHttp.open(MY_NAME, WinHttpProxyInfo.WINHTTP_ACCESS_TYPE_NO_PROXY, WinHttp.NO_PROXY_NAME, WinHttp.NO_PROXY_BYPASS, 0);
		if (hHttpSession == 0) {
			Activator.logError("WinHttp.Open failed with error'" + WinHttp.getLastErrorMessage() + "' #" + WinHttp.getLastError() + ".", null); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			return toArray(proxies);
		}

		try {
			// PAC file?
			if (isPac) {
				proxies.addAll(pacSelect(hHttpSession, uri));
			}

			// WPAD?
			if (isWpad) {
				proxies.addAll(wpadSelect(hHttpSession, uri));
			}
		} finally {
			WinHttp.closeHandle(hHttpSession);
		}

		return toArray(proxies);
	}

	private static IProxyData[] toArray(List proxies) {
		return (IProxyData[])proxies.toArray(new IProxyData[proxies.size()]);
	}

	protected List pacSelect(int hHttpSession, URI uri) {
		return pacSelect(hHttpSession, proxyConfig.getAutoConfigUrl(), uri);
	}

	protected List pacSelect(int hHttpSession, String configUrl, URI uri) {
		// Don´t ask for anything else than http or https since that is not supported
		// by WinHttp pac file support: ERROR_WINHTTP_UNRECOGNIZED_SCHEME 
		if ( !IProxyData.HTTP_PROXY_TYPE.equalsIgnoreCase(uri.getScheme()) && !IProxyData.HTTPS_PROXY_TYPE.equalsIgnoreCase(uri.getScheme()))
				return Collections.EMPTY_LIST;
		// Set up the autoproxy call.
		WinHttpAutoProxyOptions autoProxyOptions= new WinHttpAutoProxyOptions();
		autoProxyOptions.setFlags(WinHttpAutoProxyOptions.WINHTTP_AUTOPROXY_CONFIG_URL);
		autoProxyOptions.setAutoConfigUrl(configUrl);
		autoProxyOptions.setAutoLogonIfChallenged(true);
		WinHttpProxyInfo proxyInfo= new WinHttpProxyInfo();

		boolean ok= WinHttp.getProxyForUrl(hHttpSession, uri.toString(), autoProxyOptions, proxyInfo);
		if (!ok) {
			Activator.logError("WinHttp.GetProxyForUrl for pac failed with error '" + WinHttp.getLastErrorMessage() + "' #" + WinHttp.getLastError() + ".", null); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			return Collections.EMPTY_LIST;
		}
		ProxyBypass proxyBypass= new ProxyBypass(proxyInfo.getProxyBypass());
		if (proxyBypass.bypassProxyFor(uri))
			return Collections.EMPTY_LIST;
		return ProxySelectorUtils.getProxies(proxyInfo.getProxy());
	}

	protected List wpadSelect(int hHttpSession, URI uri) {
		if (wpadAutoConfigUrl == null || retryWpad) {
			AutoProxyHolder autoProxyHolder= new AutoProxyHolder();
			autoProxyHolder.setAutoDetectFlags(WinHttpAutoProxyOptions.WINHTTP_AUTO_DETECT_TYPE_DHCP | WinHttpAutoProxyOptions.WINHTTP_AUTO_DETECT_TYPE_DNS_A);
			boolean ok= WinHttp.detectAutoProxyConfigUrl(autoProxyHolder);
			if (!ok) {
				Activator.logError("WinHttp.DetectAutoProxyConfigUrl for wpad failed with error '" + WinHttp.getLastErrorMessage() + "' #" + WinHttp.getLastError() + ".", null); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				return Collections.EMPTY_LIST;
			}
			wpadAutoConfigUrl= autoProxyHolder.getAutoConfigUrl();
			retryWpad= false;
		}
		return pacSelect(hHttpSession, wpadAutoConfigUrl, uri);
	}

}
