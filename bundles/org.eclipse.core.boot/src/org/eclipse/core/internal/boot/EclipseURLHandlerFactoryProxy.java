package org.eclipse.core.internal.boot;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */

import java.net.*;
 
public class EclipseURLHandlerFactoryProxy implements URLStreamHandlerFactory {

	private static EclipseURLHandlerFactoryProxy p = null;	// singleton - set into URL as factory
	private EclipseURLHandlerFactory f = null; // current actual factory
EclipseURLHandlerFactoryProxy() {
	super();
	if (p==null) p = this;
}
public URLStreamHandler createURLStreamHandler(String protocol) {

	if (f==null) return null;
	else return f.createURLStreamHandler(protocol);
}
EclipseURLHandlerFactory getFactory() {
	return f;
}
static EclipseURLHandlerFactoryProxy getFactoryProxy() {
	return p;
}
void setFactory(EclipseURLHandlerFactory factory) {
	f = factory;
}
}
