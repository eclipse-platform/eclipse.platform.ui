package org.eclipse.help.internal.proxy.protocol;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
 
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;
import java.util.Properties;

public class ProxyHandler extends URLStreamHandler {
	
	private static URLStreamHandlerFactory eclipseFactory = null;
	private static boolean initialized = false;
	private static ProxyHandler proxy = null;
	
	// NOTE: the property name must match the value declared 
	//       in org.eclipse.core.internal.boot.PlatformURLHandleFactory
	private static final String ECLIPSE_HANDLER_FACTORY = "org.eclipse.protocol.handler.factory";
	
	public static void initialize() {
		// register proxy handlers
		Properties props = System.getProperties();
		String propName = "java.protocol.handler.pkgs";
		String pkgs = System.getProperty(propName);
		String proxyPkgs = "org.eclipse.help.internal.proxy.protocol";
		if (pkgs != null) 
			pkgs = pkgs + "|" + proxyPkgs;
		else 
			pkgs = proxyPkgs;
		props.put(propName,pkgs);
		System.setProperties(props);
		
		// create singleton delegating proxy
		proxy = new ProxyHandler();
	}
	
	public static URLConnection open(URL url) throws IOException {
		return proxy.openConnection(url);		
	}
	
	/**
	 * @see URLStreamHandler#openConnection(URL)
	 */
	protected URLConnection openConnection(URL arg0) throws IOException {
		// first time through attempt to lookup Eclipse factory
		if (!initialized) {
			Object o = System.getProperties().get(ECLIPSE_HANDLER_FACTORY);
			if (o!=null && o instanceof URLStreamHandlerFactory)
				eclipseFactory = (URLStreamHandlerFactory)o;
			initialized = true;
		}
		
		// if we have found Eclipse factory try to get a connection matching
		// the supplied URL		
		if (initialized && eclipseFactory != null) {
			URLStreamHandler h = eclipseFactory.createURLStreamHandler(arg0.getProtocol());
			if (h == null)
				throw new IOException();
				
			// Call the handler by reflection to create a connection.
			// Eclipse handlers implement openConnection() as a public
			// method to enable this delegation. In base URLStreamHandler
			// the call is protected, and therefore not visible for
			// delegation. The reflective call does not require
			// a compile-time dependency on Eclipse boot.jar.

			try {
				Method openConnection = h.getClass().getMethod("openConnection", new Class[] {URL.class});
				URLConnection c = (URLConnection)openConnection.invoke(h, new Object[] {arg0});
				return c;
			} catch (NoSuchMethodException e) {
				throw new IOException(e.getMessage());
			} catch (IllegalAccessException e) {
				throw new IOException(e.getMessage());
			} catch (InvocationTargetException e) {
				throw new IOException(e.getMessage());
			} catch (ClassCastException e) {
				throw new IOException(e.getMessage());
			}
		}
		else
			throw new IOException();
	}

}
