/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.runtime;

import org.osgi.framework.Bundle;

/**
 * Bundle groups represent a logical collection of plug-ins (aka bundles).  Bundle
 * groups do not contain their constituents but rather collect them together under
 * a common label.  The main role of a bundle group is to report to the system
 * (e.g., the About dialog) what bundles have been installed.  They are not intended
 * for use in managing the set of bundles they represent.
 * <p>
 * Since the bulk of the branding related information is specific to the consumer, 
 * bundle groups also carry an arbitrary set of properties.  The valid set of 
 * key-value pairs and their interpretation defined by the consumer in the 
 * target environment.
 * </p><p>
 * The Eclipse UI is the typical consumer of bundle groups and defines various 
 * property keys that it will use, for example, to display About information.  See
 * <code>org.eclipse.ui.branding.IBundleGroupConstants</code>.
 * </p>
 * @see IBundleGroupProvider
 * @since 3.0
 */
public interface IBundleGroup {

	/**
	 * Returns the identifier of this bundle group.  Bundle groups are uniquely identified by the combination of
	 * their identifier and their version.
	 * @see #getVersion()
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
	 * @see #getIdentifier()
	 * @return the string form of this bundle group's version
	 */
	public String getVersion();

	/**
	 * Returns a text description of this bundle group.
	 * @return text description of this bundle group
	 */
	public String getDescription();

	/**
	 * Returns the name of the provider of this bundle group.
	 * @return the name of the provider or <code>null</code> if none
	 */
	public String getProviderName();

	/**
	 * Returns a list of all bundles supplied by this bundle group.  
	 * @return the bundles supplied by this bundle group
	 */
	public Bundle[] getBundles();

	/**
	 * Returns the property of this bundle group with the given key.
	 * <code>null</code> is returned if there is no such key/value pair.
	 * @param key the name of the property to return
	 * @return the value associated with the given key or <code>null</code> if none
	 */
	public String getProperty(String key);
}
