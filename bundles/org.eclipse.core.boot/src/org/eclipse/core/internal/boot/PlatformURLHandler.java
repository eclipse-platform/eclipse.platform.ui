package org.eclipse.core.internal.boot;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

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
	public static final String PROTOCOL = "platform";
	public static final String FILE = "file";
	public static final String JAR = "jar";
	public static final String JAR_SEPARATOR = "!/";
	public static final String PROTOCOL_SEPARATOR = ":";
protected PlatformURLHandler() {
	super();
}
public URLConnection openConnection(URL url) throws IOException {
	// Note: openConnection() method is made public (rather than protected)
	//       to enable request delegation from proxy handlers

	String spec = url.getFile().trim();
	if (spec.startsWith("/")) spec = spec.substring(1);
	int ix = spec.indexOf("/");
	if (ix==-1) {
		String message = Policy.bind("url.invalidURL", url.toString());
		throw new MalformedURLException(message);
	}

	String type = spec.substring(0,ix);
	Constructor construct = (Constructor) connectionType.get(type);
	if (construct==null) {
		String message = Policy.bind("url.badVariant", url.toString());
		throw new MalformedURLException(message);
	}

	PlatformURLConnection c = null;
	try {
		c = (PlatformURLConnection) construct.newInstance(new Object[] { url });
	}
	catch(Exception e) {
		String message = Policy.bind("url.createConnection", url.toString());
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
	catch(NoSuchMethodException e) {}
}
}
