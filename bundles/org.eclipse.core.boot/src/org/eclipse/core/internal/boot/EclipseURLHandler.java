package org.eclipse.core.internal.boot;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */

import java.net.*;
import java.io.IOException;
import java.util.Hashtable;
import java.lang.reflect.Constructor;

 /**
 * URL handler for the "eclipse:" protocol
 */
public class EclipseURLHandler extends URLStreamHandler {

	private static Hashtable connectionType = new Hashtable();

	// URL protocol designations
	public static final String ECLIPSE = "eclipse";
	public static final String FILE = "file";
	public static final String JAR = "jar";
	public static final String VA = "valoader";
	public static final String JAR_SEPARATOR = "!/";
	public static final String PROTOCOL_SEPARATOR = ":";
protected EclipseURLHandler() {
	super();
}
protected URLConnection openConnection(URL url) throws IOException {

	String spec = url.getFile().trim();
	if (spec.startsWith("/")) spec = spec.substring(1);
	int ix = spec.indexOf("/");
	if (ix==-1) throw new MalformedURLException("Invalid \""+ECLIPSE+":\" URL "+url.toString());

	String type = spec.substring(0,ix);
	Constructor construct = (Constructor) connectionType.get(type);
	if (construct==null) throw new MalformedURLException("Unsupported \""+ECLIPSE+":\" protocol variation "+url.toString());

	EclipseURLConnection c = null;
	try {
		c = (EclipseURLConnection) construct.newInstance(new Object[] { url });
	}
	catch(Exception e) { throw new IOException("Unable to create connection "+url.toString()+"\n"+e); }
	
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
