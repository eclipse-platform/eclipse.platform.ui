/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.runtime;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import org.eclipse.core.internal.boot.PlatformURLConnection;
import org.eclipse.osgi.service.urlconversion.URLConverter;

/**
 * Class which implements the URLConverter service. Manages conversion of a 
 * platform: URL to one with a more well known protocol.
 * 
 * @since 3.2
 */
public class PlatformURLConverter implements URLConverter {

	/* (non-Javadoc)
	 * @see org.eclipse.osgi.service.urlconversion.URLConverter#toFileURL(java.net.URL)
	 */
	public URL toFileURL(URL url) throws IOException {
		URLConnection connection = url.openConnection();
		if (connection instanceof PlatformURLConnection)
			return ((PlatformURLConnection) connection).getURLAsLocal();
		return url;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.osgi.service.urlconversion.URLConverter#resolve(java.net.URL)
	 */
	public URL resolve(URL url) throws IOException {
		URLConnection connection = url.openConnection();
		if (connection instanceof PlatformURLConnection)
			return ((PlatformURLConnection) connection).getResolvedURL();
		return url;
	}

}
