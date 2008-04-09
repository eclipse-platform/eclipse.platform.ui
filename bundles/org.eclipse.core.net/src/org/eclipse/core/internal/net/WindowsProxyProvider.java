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
package org.eclipse.core.internal.net;

import java.net.URI;

import org.eclipse.core.internal.net.proxy.win32.winhttp.WinHttpProxyProvider;
import org.eclipse.core.net.proxy.IProxyData;

public class WindowsProxyProvider extends AbstractProxyProvider {

	static {
		System.loadLibrary("jWinHttp"); //$NON-NLS-1$
	}

	private WinHttpProxyProvider winHttpProxyProvider;

	public WindowsProxyProvider() {
		winHttpProxyProvider = new WinHttpProxyProvider();
		Activator.logInfo("WinProxyProvider initialized", null); //$NON-NLS-1$
	}

	protected IProxyData[] getProxyData(URI uri) {
		return winHttpProxyProvider.getProxyData(uri);
	}

}
