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

package org.eclipse.core.internal.runtime;

import java.net.*;
import java.util.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.internal.boot.PlatformURLHandler;
import org.eclipse.core.internal.boot.PlatformURLHandlerFactory;
 
public class PlatformURLPluginHandlerFactory implements URLStreamHandlerFactory {

	IConfigurationElement ce = null;
	
	private static final String URL_HANDLERS_POINT = "org.eclipse.core.runtime.urlHandlers"; //$NON-NLS-1$
	private static final String PROTOCOL = "protocol";
	private static final String HANDLER = "class";
public PlatformURLPluginHandlerFactory(IConfigurationElement ce) {
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
		if (protocol!=null) PlatformURLHandlerFactory.register(protocol,new PlatformURLPluginHandlerFactory(ce[i]));
	}

	// initialize plugin and fragment connection support
	PlatformURLPluginConnection.startup();
	PlatformURLFragmentConnection.startup();
}
}
