/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.boot;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.*;
import java.util.Hashtable;
import org.eclipse.core.internal.runtime.Messages;
import org.eclipse.osgi.util.NLS;
import org.osgi.service.url.AbstractURLStreamHandlerService;

/**
 * URL handler for the "platform" protocol
 */
public class PlatformURLHandler extends AbstractURLStreamHandlerService {

	private static Hashtable connectionType = new Hashtable();

	// URL protocol designations
	public static final String PROTOCOL = "platform"; //$NON-NLS-1$
	public static final String FILE = "file"; //$NON-NLS-1$
	public static final String JAR = "jar"; //$NON-NLS-1$
	public static final String BUNDLE = "bundle"; //$NON-NLS-1$
	public static final String JAR_SEPARATOR = "!/"; //$NON-NLS-1$
	public static final String PROTOCOL_SEPARATOR = ":"; //$NON-NLS-1$

	public PlatformURLHandler() {
		super();
	}

	public URLConnection openConnection(URL url) throws IOException {
		// Note: openConnection() method is made public (rather than protected)
		//       to enable request delegation from proxy handlers
		String spec = url.getFile().trim();
		if (spec.startsWith("/")) //$NON-NLS-1$
			spec = spec.substring(1);
		int ix = spec.indexOf("/"); //$NON-NLS-1$
		if (ix == -1)
			throw new MalformedURLException(NLS.bind(Messages.url_invalidURL, url.toExternalForm()));

		String type = spec.substring(0, ix);
		Constructor construct = (Constructor) connectionType.get(type);
		if (construct == null)
			throw new MalformedURLException(NLS.bind(Messages.url_badVariant, type));

		PlatformURLConnection connection = null;
		try {
			connection = (PlatformURLConnection) construct.newInstance(new Object[] {url});
		} catch (Exception e) {
			throw new IOException(NLS.bind(Messages.url_createConnection, e.getMessage()));
		}
		connection.setResolvedURL(connection.resolve());
		return connection;
	}

	public static void register(String type, Class connectionClass) {
		try {
			Constructor c = connectionClass.getConstructor(new Class[] {URL.class});
			connectionType.put(type, c);
		} catch (NoSuchMethodException e) {
			//don't register connection classes that don't conform to the spec
		}
	}
	
	public static void unregister(String type) {
		connectionType.remove(type);
	}
}
