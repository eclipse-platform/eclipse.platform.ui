/**********************************************************************
 * Copyright (c) 2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.ant.internal.core;

import java.net.URL;

public class Type {

	protected String typeName;
	protected String className;
	protected URL library;

/**
 * Gets the className.
 * @return Returns a String
 */
public String getClassName() {
	return className;
}

/**
 * Sets the className.
 * @param className The className to set
 */
public void setClassName(String className) {
	this.className = className;
}

/**
 * Gets the library.
 * @return Returns a URL
 */
public URL getLibrary() {
	return library;
}

/**
 * Sets the library.
 * @param library The library to set
 */
public void setLibrary(URL library) {
	this.library = library;
}

/**
 * Gets the taskName.
 * @return Returns a String
 */
public String getTypeName() {
	return typeName;
}

/**
 * Sets the taskName.
 * @param taskName The taskName to set
 */
public void setTypeName(String taskName) {
	this.typeName = taskName;
}
}