/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
 * @deprecated In Eclipse 3.0 the runtime was refactored and all 
 * non-essential elements removed.  This class provides facilities primarily intended
 * for tooling.  As such it has been removed and no directly substitutable API provided.
 * This API will be deleted in a future release. See bug 370248 for details.
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
	 * 
	 * @param value the localized value of this model object
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
