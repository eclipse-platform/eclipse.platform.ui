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
package org.eclipse.net.core;

import org.eclipse.net.internal.core.ProxyManager;

/**
 * Provides access to the {@link IProxyManager} and other net related functionality.
 *  * <p>
 * This class is not intended to be subclasses or instantiated by clients.
 * @since 1.0
 */
public final class NetCore {

	private static IProxyManager proxyManager;
	
	private NetCore() {
		super();
	}

	/**
	 * Return the proxy manager.
	 * @return the proxy manager
	 */
	public synchronized static IProxyManager getProxyManager() {
		if (proxyManager == null)
			proxyManager = new ProxyManager();
		return proxyManager;
	}
}
