package org.eclipse.core.internal.boot;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */

import java.net.*;
import java.util.*;
 
public class EclipseURLHandlerFactory implements URLStreamHandlerFactory {

	private static Hashtable handlers = new Hashtable();
public EclipseURLHandlerFactory() {
	super();

	// register eclipse handler
	handlers.put(EclipseURLHandler.ECLIPSE, new EclipseURLHandler());
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
	if (protocol.equals(EclipseURLHandler.ECLIPSE)) return;	// just in case ...
	handlers.put(protocol,factory);
}
public static void shutdown() {	
	
	EclipseURLHandlerFactoryProxy p = EclipseURLHandlerFactoryProxy.getFactoryProxy();
	if (p!=null) p.setFactory(null);
	EclipseURLConnection.shutdown();
}
public static void startup(String location) {

	EclipseURLHandlerFactoryProxy p = EclipseURLHandlerFactoryProxy.getFactoryProxy();
	if (p==null) {
		p = new EclipseURLHandlerFactoryProxy();	
		URL.setURLStreamHandlerFactory(p);
	}
	p.setFactory(new EclipseURLHandlerFactory());
	EclipseURLConnection.startup(location);
}
}
