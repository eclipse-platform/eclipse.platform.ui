package org.eclipse.ant.internal.core;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/

import java.net.URL;

public abstract class AntObject {

	protected String fClassName;
	protected URL fLibrary;
	protected String fName;
	
	/**
	 * Gets the className.
	 * @return Returns a String
	 */
	public String getClassName() {
		return fClassName;
	}
	/**
	 * Sets the className.
	 * @param className The className to set
	 */
	public void setClassName(String className) {
		fClassName = className;
	}
	/**
	 * Gets the library.
	 * @return Returns a URL
	 */
	public URL getLibrary() {
		return fLibrary;
	}
	/**
	 * Sets the library.
	 * @param library The library to set
	 */
	public void setLibrary(URL library) {
		fLibrary = library;
	}
	
	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return fName;
	}
}
