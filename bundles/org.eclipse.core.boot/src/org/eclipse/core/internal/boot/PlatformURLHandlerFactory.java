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

import java.net.URL;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;
import java.util.Hashtable;
import java.util.Properties;

import org.eclipse.core.boot.BootLoader;
 
public class PlatformURLHandlerFactory implements URLStreamHandlerFactory {

	private static Hashtable handlers = new Hashtable();
	
	private static final String ECLIPSE_HANDLER_FACTORY = "org.eclipse.protocol.handler.factory"; //$NON-NLS-1$
	
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
		try {	
			URL.setURLStreamHandlerFactory(p);
		} catch(Error e) {
			// Application has already set the factory. This is, for example,
			// the case when Eclipse is running as part of a servlet on
			// some web application servers. In this case we come up in
			// "toleration" mode where Eclipse URL protocols are handled
			// via explicitly supplied protocol proxy handlers using the base
			// Java convention. Eclipse "registers" its stream factory
			// using a system property. The explicit proxy handlers
			// then delegate to the Eclipse handlers via the factory looked
			// up as a Java property.
			Properties props = System.getProperties();
			props.put(ECLIPSE_HANDLER_FACTORY, p);
			System.setProperties(props);
			if (BootLoader.inDebugMode()){
				System.out.println("WARNING: Unable to set URLStreamHandlerFactory."); //$NON-NLS-1$
				System.out.println("WARNING: Starting in toleration mode."); //$NON-NLS-1$
			}
		}
	}
	p.setFactory(new PlatformURLHandlerFactory());
	PlatformURLConnection.startup(location);
}
}
