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
package org.eclipse.core.runtime.model;

/**
 * An object which represents the user-defined properties in a configuration
 * element of a plug-in manifest. Properties are <code>String</code> -based
 * key/value pairs.
 * <p>
 * This class may be instantiated, or further subclassed.
 * </p>
 * <p>
 * <b>Note</b>: This is obsolete API that will be replaced in time with
 * the OSGI-based Eclipse Platform Runtime introduced with Eclipse 3.0.
 * This API will be deprecated once the APIs for the new Eclipse Platform
 * Runtime achieve their final and stable form (post-3.0). </p>
 */
public class ConfigurationPropertyModel extends PluginModelObject {

	// DTD properties (included in plug-in manifest)
	private String value = null;

	/**
	 * Creates a new configuration property model in which all fields are
	 * <code>null</code>.
	 */
	public ConfigurationPropertyModel() {
		super();
	}

	/**
	 * Returns the value of this property.
	 * 
	 * @return the value of this property or <code>null</code>
	 */
	public String getValue() {
		return value;
	}

	/**
	 * Optimization to replace a non-localized key with its localized value.
	 * Avoids having to access resource bundles for further lookups.
	 */
	public void setLocalizedValue(String value) {
		this.value = value;
	}

	/**
	 * Sets the value of this property. This object must not be read-only.
	 * 
	 * @param value the new value of this property. May be <code>null</code>.
	 */
	public void setValue(String value) {
		assertIsWriteable();
		this.value = value;
	}
}