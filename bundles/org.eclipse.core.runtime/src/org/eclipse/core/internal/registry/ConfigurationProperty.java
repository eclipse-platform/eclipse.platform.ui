/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.registry;

import org.eclipse.core.internal.runtime.InternalPlatform;

/**
 * An object which represents the user-defined properties in a configuration
 * element of a plug-in manifest.  Properties are <code>String</code>-based
 * key/value pairs.
 * <p>
 * This class may be instantiated, or further subclassed.
 * </p>
 */
public class ConfigurationProperty extends RegistryModelObject {

	// DTD properties (included in plug-in manifest)
	private String value = null;

	/**
	 * Creates a new configuration property model in which all fields
	 * are <code>null</code>.
	 */
	public ConfigurationProperty() {
		super();
	}

	/**
	 * Returns the value of this property.
	 * 
	 * @return the value of this property
	 *  or <code>null</code>
	 */
	public String getValue() {
		return value;
	}

	/**
	 * Sets the value of this property.
	 * 
	 * @param value the new value of this property.  May be <code>null</code>.
	 */
	public void setValue(String value) {
		this.value = value;
	}

	/**
	 * Optimization to replace a non-localized key with its localized value.  Avoids having
	 * to access resource bundles for further lookups.
	 */
	public void setLocalizedValue(String value) {
		this.value = value;
		((ExtensionRegistry) InternalPlatform.getDefault().getRegistry()).setDirty(true);
	}
}