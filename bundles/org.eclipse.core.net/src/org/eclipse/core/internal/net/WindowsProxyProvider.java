/*******************************************************************************
 * Copyright (c) 2008 compeople AG and others.
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
	
	static {
		try {
			System.loadLibrary(LIBRARY_NAME);
			if (Policy.DEBUG_SYSTEM_PROVIDERS)
				Policy.debug("Loaded " + LIBRARY_NAME + " library"); //$NON-NLS-1$ //$NON-NLS-2$
		} catch (final UnsatisfiedLinkError e) {
			Activator.logError(
					"Could not load library: " + System.mapLibraryName(LIBRARY_NAME), e); //$NON-NLS-1$
		}
	}

	private WinHttpProxyProvider winHttpProxyProvider;

	public WindowsProxyProvider() {
		winHttpProxyProvider = new WinHttpProxyProvider();
	}

	protected IProxyData[] getProxyData(URI uri) {
		return winHttpProxyProvider.getProxyData(uri);
	}

}
