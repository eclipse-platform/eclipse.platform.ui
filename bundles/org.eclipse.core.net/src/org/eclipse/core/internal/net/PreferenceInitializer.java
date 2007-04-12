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
package org.eclipse.core.internal.net;

import org.eclipse.core.net.proxy.IProxyData;
import org.eclipse.core.runtime.preferences.*;

public class PreferenceInitializer extends AbstractPreferenceInitializer {

	private static final String PREF_HAS_MIGRATED = "org.eclipse.core.net.hasMigrated"; //$NON-NLS-1$
	/**
	 * Preference constants used by Update to record the HTTP proxy
	 */
	public static String HTTP_PROXY_HOST = "org.eclipse.update.core.proxy.host"; //$NON-NLS-1$
	public static String HTTP_PROXY_PORT = "org.eclipse.update.core.proxy.port"; //$NON-NLS-1$
	public static String HTTP_PROXY_ENABLE = "org.eclipse.update.core.proxy.enable"; //$NON-NLS-1$
	
	public PreferenceInitializer() {
		super();
	}

	public void initializeDefaultPreferences() {
		// TODO: We should set defaults in the default scope
		//((ProxyManager)ProxyManager.getProxyManager()).initialize();
	}
	
	public static IProxyData getMigratedHttpProxy() {
		if (!Activator.getInstance().getInstancePreferences().getBoolean(PREF_HAS_MIGRATED, false)) {
			Activator.getInstance().getInstancePreferences().putBoolean(PREF_HAS_MIGRATED, true);
			IEclipsePreferences prefs = new InstanceScope().getNode("org.eclipse.update.core"); //$NON-NLS-1$
			String httpProxyHost = prefs.get(HTTP_PROXY_HOST, ""); //$NON-NLS-1$
			if ("".equals(httpProxyHost)) //$NON-NLS-1$
				httpProxyHost = null;
			
			String httpProxyPort = prefs.get(HTTP_PROXY_PORT, ""); //$NON-NLS-1$
			if ("".equals(httpProxyPort)) //$NON-NLS-1$
				httpProxyPort = null;
			int port = -1;
			if (httpProxyPort != null)
				try {
					port = Integer.parseInt(httpProxyPort);
				} catch (NumberFormatException e) {
					// Ignore
				}
			boolean httpProxyEnable = prefs.getBoolean(HTTP_PROXY_ENABLE, false);
			if (httpProxyEnable) {
				return new ProxyData(IProxyData.HTTP_PROXY_TYPE, httpProxyHost, port, false);
			}
		}
		return null;
	}

}
