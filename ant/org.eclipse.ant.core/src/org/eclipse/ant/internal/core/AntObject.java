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
package org.eclipse.ant.internal.core;


import java.net.URL;

public abstract class AntObject {

	protected String fClassName;
	protected URL fLibrary;
	protected String fName;
	private boolean fIsDefault= false;
	
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
	/**
	 * Returns whether this ant object has been created because of an extension
	 * point definition.
	 * @return boolean
	 */
	public boolean isDefault() {
		return fIsDefault;
	}

	/**
	 * Sets that this ant object has been created by the appropriate extension
	 * point.
	 * @param isDefault Whether this ant object has been created because of an
	 * extension point defintion.
	 */
	public void setIsDefault(boolean isDefault) {
		this.fIsDefault = isDefault;
	}

}
