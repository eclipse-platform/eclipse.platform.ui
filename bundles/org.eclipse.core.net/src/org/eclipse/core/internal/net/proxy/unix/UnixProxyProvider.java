/*******************************************************************************
 * Copyright (c) 2008 Oakland Software Incorporated and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Oakland Software Incorporated - initial API and implementation
 *     IBM Corporation - implementation
 *******************************************************************************/
package org.eclipse.core.internal.net.proxy.unix;

import java.io.IOException;
import java.net.URI;
import java.util.Locale;
import java.util.Properties;

import org.eclipse.core.internal.net.AbstractProxyProvider;
import org.eclipse.core.internal.net.Activator;
import org.eclipse.core.internal.net.ProxyData;
import org.eclipse.core.net.proxy.IProxyData;

public class UnixProxyProvider extends AbstractProxyProvider {

	public static boolean DEBUG = false;

	static {
		try {
			System.loadLibrary("proxygnome"); //$NON-NLS-1$
			// Start it up on the main thread, it seems to hand sometimes
			// otherwise
			gconfInit();
			if (DEBUG)
				System.out.println("Loaded (Gnome) libraries"); //$NON-NLS-1$
		} catch (UnsatisfiedLinkError ex) {
			// Expected on systems that are missing Gnome libraries
			if (DEBUG)
				System.out.println("Missing gconf (Gnome) libraries"); //$NON-NLS-1$
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

	public String[] getNonProxiedHosts() {
		String[] npHosts;

		// First try the environment variable which is a URL
		String npEnv = getEnv("no_proxy"); //$NON-NLS-1$
		if (npEnv != null) {
			npHosts = npEnv.split(","); //$NON-NLS-1$
			for (int i = 0; i < npHosts.length; i++)
				npHosts[i] = npHosts[i].trim();
			if (DEBUG) {
				System.out.println("got env no_proxy: " + npEnv); //$NON-NLS-1$
				debugPrint(npHosts);
			}
			return npHosts;
		}

		try {
			npHosts = getGConfNonProxyHosts();
			if (npHosts != null && npHosts.length > 0) {
				if (DEBUG) {
					System.out.println("got gnome no_proxy"); //$NON-NLS-1$
					debugPrint(npHosts);
				}
				return npHosts;
			}
		} catch (UnsatisfiedLinkError ex) {
			// Expected on systems that are missing Gnome libraries
			// This has already been reported (the native code did not load)
		}

		return new String[] {};
	}

	// Returns null if something wrong or there is no proxy for the protocol
	protected ProxyData getSystemProxyInfo(String protocol) {
		ProxyData pd = null;

		String envName = null;
		String proxyEnv = null;
		URI uri = null;

		try {
			if (DEBUG)
				System.out.println("getting ProxyData for: " + protocol); //$NON-NLS-1$

			// protocol schemes are ISO 8859 (ASCII)
			protocol = protocol.toLowerCase(Locale.ENGLISH);

			// First try the environment variable which is a URL
			envName = protocol + "_proxy"; //$NON-NLS-1$
			proxyEnv = getEnv(envName);
			if (DEBUG)
				System.out.println("got proxyEnv: " + proxyEnv); //$NON-NLS-1$

			if (proxyEnv != null) {
				uri = new URI(proxyEnv);
				pd = new ProxyData(protocol);
				pd.setHost(uri.getHost());
				pd.setPort(uri.getPort());
				String userInfo = uri.getUserInfo();
				if (userInfo != null) {
					String user = null;
					String password = null;
					int pwInd = userInfo.indexOf(':');
					if (pwInd >= 0) {
						user = userInfo.substring(0, pwInd);
						password = userInfo.substring(pwInd + 1);
					} else {
						user = userInfo;
					}
					pd.setUserid(user);
					pd.setPassword(password);
				}

				if (DEBUG)
					System.out.println("env proxy data: " + pd); //$NON-NLS-1$
				return pd;
			}
		} catch (Exception ex) {
			Activator.logError(
					"Problem during accessing system variable: " + envName, ex); //$NON-NLS-1$
		}

		try {
			// Then ask Gnome
			pd = getGConfProxyInfo(protocol);
			if (DEBUG)
				System.out.println("Gnome proxy data: " + pd); //$NON-NLS-1$
			return pd;

		} catch (UnsatisfiedLinkError ex) {
			// Expected on systems that are missing Gnome libraries
			// This has already been reported (the native code did not load)
		}

		return null;
	}

	private String getEnv(String env) {
		Properties props = new Properties();
		try {
			props.load(Runtime.getRuntime().exec("env").getInputStream()); //$NON-NLS-1$
		} catch (IOException e) {
			Activator.logError(
					"Problem during accessing system variable: " + env, e); //$NON-NLS-1$
		}
		return props.getProperty(env);
	}

	private void debugPrint(String[] strs) {
		System.out.println("npHosts: "); //$NON-NLS-1$
		for (int i = 0; i < strs.length; i++)
			System.out.println(i + ": " + strs[i]); //$NON-NLS-1$
	}

	protected static native void gconfInit();

	protected static native ProxyData getGConfProxyInfo(String protocol);

	protected static native String[] getGConfNonProxyHosts();
}
