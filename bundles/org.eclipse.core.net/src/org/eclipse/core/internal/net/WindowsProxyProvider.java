/*******************************************************************************
 * Copyright (c) 2008, 2009 compeople AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 		compeople AG (Stefan Liebig) - initial API and implementation
 * 		IBM Corporation - implementation
 *******************************************************************************/
package org.eclipse.core.internal.net;

import java.net.URI;

import org.eclipse.core.internal.net.proxy.win32.winhttp.WinHttpProxyProvider;
import org.eclipse.core.net.proxy.IProxyData;

public class WindowsProxyProvider extends AbstractProxyProvider {

	private static final String LIBRARY_NAME = "jWinHttp-1.0.0"; //$NON-NLS-1$

	private static boolean jWinHttpLoaded = false;

	static {
		try {
			System.loadLibrary(LIBRARY_NAME);
			if (Policy.DEBUG_SYSTEM_PROVIDERS)
				Policy.debug("Loaded " + LIBRARY_NAME + " library"); //$NON-NLS-1$ //$NON-NLS-2$
			jWinHttpLoaded = true;
		} catch (final UnsatisfiedLinkError e) {
			Activator.logError(
					"Could not load library: " + System.mapLibraryName(LIBRARY_NAME), e); //$NON-NLS-1$
		}
	}

	private WinHttpProxyProvider winHttpProxyProvider;

	public WindowsProxyProvider() {
		if (jWinHttpLoaded) {
			winHttpProxyProvider = new WinHttpProxyProvider();
		} else {
			winHttpProxyProvider = null;
		}
	}

	protected IProxyData[] getProxyData(URI uri) {
		if (jWinHttpLoaded) {
			return winHttpProxyProvider.getProxyData(uri);
		}
		return new IProxyData[0];
	}

	protected IProxyData[] getProxyData() {
		if (jWinHttpLoaded) {
			return winHttpProxyProvider.getProxyData();
		}
		return new IProxyData[0];
	}

	protected String[] getNonProxiedHosts() {
		if (jWinHttpLoaded) {
			return winHttpProxyProvider.getNonProxiedHosts();
		}
		return new String[0];
	}

}
