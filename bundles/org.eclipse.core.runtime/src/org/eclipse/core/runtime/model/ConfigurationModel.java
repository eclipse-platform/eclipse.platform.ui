package org.eclipse.core.runtime.model;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.PluginVersionIdentifier;

/**
 * An object which represents the user-defined contents of a configuration model
 * in a configuration manifest.
 * <p>
 * This class may be instantiated and further subclassed.
 * </p>
 */

public class ConfigurationModel extends InstallModel {

	// DTD properties (included in install manifest)
	private String application = null;
	private ComponentModel[] components = new ComponentModel[0];

/**
 * Returns the name of the application to be run for this configuration.
 *
 * @return the application to run for this configuration
 */
public String getApplication() {
	return application;
}

/**
 * Returns the list of components managed by this configuration.
 *
 * @return the components in this configuration
 */
public ComponentModel[] getComponents() {
	return components;
}

/**
 * Sets this model object and all of its descendents to be read-only.
 * Subclasses may extend this implementation.
 *
 * @see #isReadOnly
 */
public void markReadOnly() {
	super.markReadOnly();
	if (components!= null)
		for (int i = 0; i < components.length; i++)
			components[i].markReadOnly();
}

/**
 * Sets the application to run for this configuration.
 * This object must not be read-only.
 *
 * @param value the application to run for this configuration
 */
public void setApplication(String value) {
	assertIsWriteable();
	application = value;
}

/**
 * Sets the list of components managed by this configuration.
 * This object must not be read-only.
 *
 * @param value the components managed by this configuration
 */
public void setComponents(ComponentModel[] value) {
	assertIsWriteable();
	components = value;
}

}
