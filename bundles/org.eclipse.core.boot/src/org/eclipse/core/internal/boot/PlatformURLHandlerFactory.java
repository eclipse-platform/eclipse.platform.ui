package org.eclipse.core.internal.boot;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.net.*;
import java.util.*;
 
public class PlatformURLHandlerFactory implements URLStreamHandlerFactory {

	private static Hashtable handlers = new Hashtable();
public PlatformURLHandlerFactory() {
	super();

	// register eclipse handler
	handlers.put(PlatformURLHandler.PROTOCOL, new PlatformURLHandler());
}
public URLStreamHandler createURLStreamHandler(String protocol) {

	URLStreamHandler handler = null;

	// check for cached handler
	Object element = handlers.get(protocol);
	if (element==null) return null;
	if (element instanceof URLStreamHandler) handler = (URLStreamHandler)element;
	else {
		// convert registered factory to a handler
		URLStreamHandlerFactory f = (URLStreamHandlerFactory) element;
		handler = f.createURLStreamHandler(protocol);
		if (handler!=null) handlers.put(protocol, handler);
		else handlers.remove(protocol);	// bad entry
	}
	return handler;
}
public static void register(String protocol, URLStreamHandlerFactory factory) {
	if (protocol.equals(PlatformURLHandler.PROTOCOL)) return;	// just in case ...
	handlers.put(protocol,factory);
}
public static void shutdown() {	
	
	PlatformURLHandlerFactoryProxy p = PlatformURLHandlerFactoryProxy.getFactoryProxy();
	if (p!=null) p.setFactory(null);
	PlatformURLConnection.shutdown();
}
public static void startup(String location) {

	PlatformURLHandlerFactoryProxy p = PlatformURLHandlerFactoryProxy.getFactoryProxy();
	if (p==null) {
		p = new PlatformURLHandlerFactoryProxy();	
		URL.setURLStreamHandlerFactory(p);
	}
	p.setFactory(new PlatformURLHandlerFactory());
	PlatformURLConnection.startup(location);
}
}
