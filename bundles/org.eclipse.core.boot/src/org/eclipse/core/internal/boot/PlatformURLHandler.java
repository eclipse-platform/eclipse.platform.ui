/**********************************************************************
 * Copyright (c) 2000,2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.core.internal.boot;

import java.net.*;
import java.io.IOException;
import java.util.Hashtable;
import java.lang.reflect.Constructor;

 /**
 * URL handler for the "platform" protocol
 */
public class PlatformURLHandler extends URLStreamHandler {

	private static Hashtable connectionType = new Hashtable();

	// URL protocol designations
	public static final String PROTOCOL = "platform"; //$NON-NLS-1$
	public static final String FILE = "file"; //$NON-NLS-1$
	public static final String JAR = "jar"; //$NON-NLS-1$
	public static final String JAR_SEPARATOR = "!/"; //$NON-NLS-1$
	public static final String PROTOCOL_SEPARATOR = ":"; //$NON-NLS-1$
protected PlatformURLHandler() {
	super();
}
public URLConnection openConnection(URL url) throws IOException {
	// Note: openConnection() method is made public (rather than protected)
	//       to enable request delegation from proxy handlers

	String spec = url.getFile().trim();
	if (spec.startsWith("/")) spec = spec.substring(1); //$NON-NLS-1$
	int ix = spec.indexOf("/"); //$NON-NLS-1$
	if (ix==-1) {
		String message = Policy.bind("url.invalidURL", url.toString()); //$NON-NLS-1$
		throw new MalformedURLException(message);
	}

	String type = spec.substring(0,ix);
	Constructor construct = (Constructor) connectionType.get(type);
	if (construct==null) {
		String message = Policy.bind("url.badVariant", url.toString()); //$NON-NLS-1$
		throw new MalformedURLException(message);
	}

	PlatformURLConnection c = null;
	try {
		c = (PlatformURLConnection) construct.newInstance(new Object[] { url });
	}
	catch(Exception e) {
		String message = Policy.bind("url.createConnection", url.toString()); //$NON-NLS-1$
		throw new IOException(message);
	}
	c.setResolvedURL(c.resolve());
	return c;
}
public static void register(String type, Class connectionClass) {
	try {
		Constructor c = connectionClass.getConstructor(new Class[] { URL.class });	
		connectionType.put(type, c);
	}
	catch(NoSuchMethodException e) {
		//don't register connection classes that don't conform to the spec
	}
}
}
