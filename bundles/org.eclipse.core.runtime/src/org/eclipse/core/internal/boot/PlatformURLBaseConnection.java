/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.boot;

import java.io.IOException;
import java.net.URL;
import org.eclipse.core.internal.runtime.Policy;

/**
 * Platform URL support
 * platform:/base/	maps to platform installation location
 */
public class PlatformURLBaseConnection extends PlatformURLConnection {

	// platform/ protocol
	public static final String PLATFORM = "base"; //$NON-NLS-1$
	public static final String PLATFORM_URL_STRING = PlatformURLHandler.PROTOCOL + PlatformURLHandler.PROTOCOL_SEPARATOR + "/" + PLATFORM + "/"; //$NON-NLS-1$ //$NON-NLS-2$

	private static URL installURL;

	public PlatformURLBaseConnection(URL url) {
		super(url);
	}

	protected boolean allowCaching() {
		return true;
	}

	protected URL resolve() throws IOException {
		String spec = url.getFile().trim();
		if (spec.startsWith("/")) //$NON-NLS-1$
			spec = spec.substring(1);
		if (!spec.startsWith(PLATFORM + "/")) { //$NON-NLS-1$
			String message = Policy.bind("url.badVariant", url.toString()); //$NON-NLS-1$
			throw new IOException(message);
		}
		return spec.length() == PLATFORM.length() + 1 ? installURL : new URL(installURL, spec.substring(PLATFORM.length() + 1));
	}

	public static void startup(URL url) {
		// register connection type for platform:/base/ handling
		if (installURL != null)
			return;
		installURL = url;
		PlatformURLHandler.register(PLATFORM, PlatformURLBaseConnection.class);
	}
}