/**********************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.runtime;

import org.osgi.framework.Bundle;

public interface IBundleGroup {

	/**
	 * Returns the identifier of this bundle group.  Bundle groups are uniquely identified by the combination of
	 * their identifier and their version.
	 * @see getVersion()
	 * @return the identifier for this bundle group
	 */
	public String getIdentifier();

	/**
	 * Returns the human-readable name of this bundle group.
	 * @return the human-readable name
	 */
	public String getName();

	/**
	 * Returns the version of this bundle group. Bundle group version strings have the same format as 
	 * bundle versions (i.e., major.minor.service.qualifier).  Bundle groups are uniquely identified 
	 * by the combination of their identifier and their version.
	 * @see getIdentifier
	 * @return the string form of this bundle group's version
	 */
	public String getVersion();
	/**
	 * Returns a text description of this bundle group
	 * @return text description of this bundle group
	 */
	public String getDescription();
	
	/**
	 * Returns the name of the provider of this bundle group.
	 * @return the name of the provider or null if none
	 */
	public String getProviderName();
	
	/**
	 * Returns a list of all bundles supplied by this bundle group.  
	 * @return the bundles supplied by this bundle group
	 */
	public Bundle getBundles();

	/**
	 * Returns the property of this bundle group with the given key.
	 * null is returned if there is no such key/value pair.
	 * @param key the name of the property to return
	 * @return the value associated with the given key
	 */
	public String getProperty(String key);
	
}
