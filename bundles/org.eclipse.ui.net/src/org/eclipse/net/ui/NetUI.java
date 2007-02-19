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
package org.eclipse.net.ui;

import java.net.Authenticator;

import org.eclipse.net.core.*;
import org.eclipse.net.internal.ui.auth.NetAuthenticator;

/**
 * Provides access to the UI functionality associated with networking.
 * <p>
 * This class is not intended to be subclasses or instantiated by clients.
 * @since 1.0
 */
public final class NetUI {
	
	private NetUI() {
		super();
	}

	/**
	 * Method to be called by the application to ensure that the Java system
	 * properties related to proxies are set along with the default {@link Authenticator}.
	 */
	public static void initialize() {
		// Prime the core to set the system properties
		NetCore.getProxyManager();
		// Set the authenticator
		Authenticator.setDefault(new NetAuthenticator());
	}

}
