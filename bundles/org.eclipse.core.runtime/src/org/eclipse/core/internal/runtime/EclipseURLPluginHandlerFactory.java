package org.eclipse.core.internal.runtime;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */

import java.net.*;
import java.util.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.internal.boot.EclipseURLHandler;
import org.eclipse.core.internal.boot.EclipseURLHandlerFactory;
 
public class EclipseURLPluginHandlerFactory implements URLStreamHandlerFactory {

	IConfigurationElement ce = null;
	
	private static final String URL_HANDLERS_POINT = "org.eclipse.core.runtime.urlHandlers";
	private static final String PROTOCOL = "protocol";
	private static final String HANDLER = "class";
public EclipseURLPluginHandlerFactory(IConfigurationElement ce) {
	super();
	this.ce = ce;	
}
public URLStreamHandler createURLStreamHandler(String protocol) {

	URLStreamHandler handler = null;
	try {
		handler = (URLStreamHandler)ce.createExecutableExtension(HANDLER);
	} catch (CoreException e) {}
	return handler;
}
public static void startup() {

	// register URL handler extensions
	IPluginRegistry r = Platform.getPluginRegistry();
	IConfigurationElement[] ce = r.getConfigurationElementsFor(URL_HANDLERS_POINT);
	String protocol;
	for (int i=0; i<ce.length; i++) {
		// register factory elements (actual handlers lazily created on request)
		protocol = ce[i].getAttribute(PROTOCOL);
		if (protocol!=null) EclipseURLHandlerFactory.register(protocol,new EclipseURLPluginHandlerFactory(ce[i]));
	}

	// initialize plugin connection support
	EclipseURLPluginConnection.startup();
}
}
