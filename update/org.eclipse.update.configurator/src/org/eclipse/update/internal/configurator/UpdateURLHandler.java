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
package org.eclipse.update.internal.configurator;

import java.io.IOException;
import java.net.*;
import org.osgi.service.url.AbstractURLStreamHandlerService;

/**
 * URL handler for the "platform" protocol
 */
public class UpdateURLHandler extends AbstractURLStreamHandlerService {

	// URL protocol designations
	public static final String PROTOCOL = "update"; //$NON-NLS-1$
	public static final String FILE = "file"; //$NON-NLS-1$
	public static final String JAR = "jar"; //$NON-NLS-1$
	public static final String BUNDLE = "bundle"; //$NON-NLS-1$
	public static final String REFERENCE= "reference"; //$NON-NLS-1$
	public static final String PROTOCOL_SEPARATOR = ":"; //$NON-NLS-1$

	public UpdateURLHandler() {
		super();
	}

	public URLConnection openConnection(URL url) throws IOException {
		// Note: openConnection() method is made public (rather than protected)
		//       to enable request delegation from proxy handlers
		
		String spec = url.getFile().trim();
		
		URL realURL = new URL(spec);
		URLConnection connection = realURL.openConnection();
		return connection;
	}
}