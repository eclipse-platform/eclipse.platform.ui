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

import java.net.*;

public class ResourceLoader extends URLClassLoader {
public ResourceLoader(URL[] resourcePath) {
	super(resourcePath, null);
}
/**
 * Looks for a given class.   Resource loaders can never find classes and
 * so always throw <code>ClassNotFoundException</code>.
 */
protected Class findClass(final String name) throws ClassNotFoundException {
	throw new ClassNotFoundException(name);
}
/**
 * Looks for a given class.   Resource loaders can never find classes and
 * so always throw <code>ClassNotFoundException</code>.
 */
protected Class loadClass(String name, boolean resolve) throws ClassNotFoundException {
	throw new ClassNotFoundException();
}
}
