package org.eclipse.core.runtime.model;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

/**
 * An object which represents the user-defined properties in a configuration
 * element of a plug-in manifest.  Properties are <code>String</code>-based
 * key/value pairs.
 * <p>
 * This class may be instantiated, or further subclassed.
 * </p>
 */
public class ConfigurationPropertyModel extends PluginModelObject {

	// DTD properties (included in plug-in manifest)
	private String value = null;
/**
 * Creates a new configuration property model in which all fields
 * are <code>null</code>.
 */
public ConfigurationPropertyModel() {
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
 * This object must not be read-only.
 * 
 * @param value the new value of this property.  May be <code>null</code>.
 */
public void setValue(String value) {
	assertIsWriteable();
	this.value = value;
}
}
