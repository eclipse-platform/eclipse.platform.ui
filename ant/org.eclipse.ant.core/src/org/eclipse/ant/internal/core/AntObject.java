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
	private String fPluginLabel;
	
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
	 * Returns whether this Ant object has been created because of an extension
	 * point definition.
	 * @return boolean
	 */
	public boolean isDefault() {
		return fPluginLabel != null;
	}

	/**
	 * Sets that this ant object has been created by the appropriate extension
	 * point.
	 * @param isDefault Whether this Ant object has been created because of an
	 * extension point defintion.
	 * @deprecated Since 3.0 Set the plugin label to indicate a default object
	 */
	public void setIsDefault(boolean isDefault) {
		if (!isDefault) {
			fPluginLabel= null;
		}
	}
	
	/**
	 * Sets the label of the plugin that contributed this Ant object via an extension
	 * point.
	 * 
	 * @param pluginLabel The label of the plugin
	 * @since 3.0
	 */
	public void setPluginLabel(String pluginLabel) {
		fPluginLabel = pluginLabel;
	}

	/**
	 * Returns the label of the plugin that contributed this Ant object via an extension
	 * point.
	 * 
	 * @return pluginLabel The label of the plugin
	 * @since 3.0
	 */
	public String getPluginLabel() {
		return fPluginLabel;
	}
}
