package org.eclipse.core.runtime.model;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.PluginVersionIdentifier;

/**
 * An abstract class representing the common parts of the various install
 * manifest structures.
 * <p>
 * This class may not be instantiated but may be further subclassed.
 * </p>
 */

public abstract class InstallModel extends PluginModelObject {

	// DTD properties (included in install manifest)
	private String id = null;
	private String version = null;
	private String providerName = null;
	private String description = null;
	private String location = null;
	private URLModel[] updates = new URLModel[0];
	private URLModel[] discoveries = new URLModel[0];

/**
 * Returns the unique identifier of this component model
 * or <code>null</code>.
 * This identifier is a non-empty string and is unique 
 * across all components.
 *
 * @return the unique identifier of this component model
 *		(e.g. <code>"com.example"</code>) or <code>null</code>. 
 */
public String getId() {
	return id;
}

/**
 * Returns the location of this component.
 *
 * @return the location of this component or <code>null</code>
 */
public String getLocation() {
	return location;
}

/**
 * Returns the version name of this component.
 *
 * @return the version name of this component or <code>null</code>
 */
public String getVersion() {
	return version;
}

/**
 * Returns the name of the provider who authored this component.
 *
 * @return name of the provider who authored this component or <code>null</code>
 */
public String getProviderName() {
	return providerName;
}

/**
 * Returns the description of this component.
 *
 * @return description of this component or <code>null</code>
 */
public String getDescription() {
	return description;
}

/**
 * Returns the list of update URLs for this component.
 *
 * @return the update URLs for this component
 */
public URLModel[] getUpdates() {
	return updates;
}

/**
 * Returns the list of discovery URLs for this component.
 *
 * @return the discovery URLs for this component
 */
public URLModel[] getDiscoveries() {
	return discoveries;
}

/**
 * Sets this model object and all of its descendents to be read-only.
 * Subclasses may extend this implementation.
 *
 * @see #isReadOnly
 */
public void markReadOnly() {
	super.markReadOnly();
	if (updates != null)
		for (int i = 0; i < updates.length; i++)
			updates[i].markReadOnly();
	if (discoveries != null)
		for (int i = 0; i < discoveries.length; i++)
			discoveries[i].markReadOnly();
}

/**
 * Sets the unique identifier of this component model.
 * The identifier is a non-empty string and is unique 
 * across all components.
 * This object must not be read-only.
 *
 * @param value the unique identifier of this component model (e.g. <code>"com.example"</code>).
 *		May be <code>null</code>.
 */
public void setId(String value) {
	assertIsWriteable();
	id = value;
}

/**
 * Sets the version name of this component.  The version number
 * is canonicalized.
 * This object must not be read-only.
 *
 * @param value the version name of this component.
 *		May be <code>null</code>.
 */
public void setVersion(String value) {
	assertIsWriteable();
	// XXX workaround because some people still do not use the correct 
	// version format.
	int i = value.indexOf(' ');
	if (i > -1)
		value = value.substring(0, i);
	version = new PluginVersionIdentifier(value).toString();
}

/**
 * Sets the name of the provider who authored this component.
 * This object must not be read-only.
 *
 * @param value name of the provider who authored this component.
 *		May be <code>null</code>.
 */
public void setProviderName(String value) {
	assertIsWriteable();
	providerName = value;
}

/**
 * Sets the description of this component.
 * This object must not be read-only.
 *
 * @param value the description of this component.
 *		May be <code>null</code>.
 */
public void setDescription(String value) {
	assertIsWriteable();
	description = value;
}

/**
 * Sets the location of this component.
 * This object must not be read-only.
 *
 * @param value the location of this component
 */
public void setLocation(String value) {
	assertIsWriteable();
	location = value;
}

/**
 * Sets the list of update URLs for this component.
 * This object must not be read-only.
 *
 * @param value the update URLs for this component
 */
public void setUpdates(URLModel[] value) {
	assertIsWriteable();
	updates = value;
}

/**
 * Sets the list of discovery URLs for this component.
 * This object must not be read-only.
 *
 * @param value the discovery URLs for this component
 */
public void setDiscoveries(URLModel[] value) {
	assertIsWriteable();
	discoveries = value;
}

}
