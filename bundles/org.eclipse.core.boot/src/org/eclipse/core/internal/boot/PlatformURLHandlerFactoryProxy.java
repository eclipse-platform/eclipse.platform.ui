package org.eclipse.core.internal.boot;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */

import java.net.*;
 
public class PlatformURLHandlerFactoryProxy implements URLStreamHandlerFactory {

	private static PlatformURLHandlerFactoryProxy p = null;	// singleton - set into URL as factory
	private PlatformURLHandlerFactory f = null; // current actual factory
PlatformURLHandlerFactoryProxy() {
	super();
	if (p==null) p = this;
}
public URLStreamHandler createURLStreamHandler(String protocol) {

	if (f==null) return null;
	else return f.createURLStreamHandler(protocol);
}
PlatformURLHandlerFactory getFactory() {
	return f;
}
static PlatformURLHandlerFactoryProxy getFactoryProxy() {
	return p;
}
void setFactory(PlatformURLHandlerFactory factory) {
	f = factory;
}
}
