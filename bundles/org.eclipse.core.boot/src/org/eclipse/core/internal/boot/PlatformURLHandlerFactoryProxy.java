/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.boot;

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
