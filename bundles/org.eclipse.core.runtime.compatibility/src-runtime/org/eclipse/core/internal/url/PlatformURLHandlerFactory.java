/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.url;

import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;
import java.util.Hashtable;
import org.eclipse.core.internal.boot.PlatformURLHandler;

public class PlatformURLHandlerFactory implements URLStreamHandlerFactory {
	private static Hashtable handlers = new Hashtable();

	public PlatformURLHandlerFactory() {
		super();
	}

	public URLStreamHandler createURLStreamHandler(String protocol) {
		URLStreamHandler handler = null;
		// check for cached handler
		Object element = handlers.get(protocol);
		if (element == null)
			return null;
		if (element instanceof URLStreamHandler)
			return (URLStreamHandler) element;
		// convert registered factory to a handler
		URLStreamHandlerFactory factory = (URLStreamHandlerFactory) element;
		handler = factory.createURLStreamHandler(protocol);
		if (handler != null)
			handlers.put(protocol, handler);
		else
			handlers.remove(protocol); // bad entry
		return handler;
	}
	public static void register(String protocol, URLStreamHandlerFactory factory) {
		if (protocol.equals(PlatformURLHandler.PROTOCOL))
			return; // just in case ...
		handlers.put(protocol, factory);
	}
}
