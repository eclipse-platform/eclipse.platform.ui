package org.eclipse.core.internal.boot;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

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
