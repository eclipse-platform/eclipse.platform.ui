package org.eclipse.core.runtime.model;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.PluginVersionIdentifier;

/**
 * An object which represents the user-defined contents of a plug-in fragment
 * in a plug-in manifest.
 * <p>
 * This class may be instantiated, or further subclassed.
 * </p>
 */
public class PluginFragmentModel extends PluginModel {

	// DTD properties (included in plug-in manifest)
	private String plugin = null;
	private String pluginVersion = null;
/**
 * Creates a new plug-in descriptor model in which all fields
 * are <code>null</code>.
 */
public PluginFragmentModel() {
	super();
}
/**
 * Returns the fully qualified name of the plug-in for which this is a fragment
 *
 * @return the name of this fragment's plug-in or <code>null</code>.
 */
public String getPlugin() {
	return plugin;
}
/**
 * Returns the unique identifier of the plug-in related to this model
 * or <code>null</code>.  
 * This identifier is a non-empty string and is unique 
 * within the plug-in registry.
 *
 * @return the unique identifier of the plug-in related to this model
 *		(e.g. <code>"com.example"</code>) or <code>null</code>. 
 */
public String getPluginId() {
	return getPlugin();
}
/**
 * Returns the version name of the plug-in for which this is a fragment.
 *
 * @return the version name of this fragment's plug-in or <code>null</code>
 */
public String getPluginVersion() {
	return pluginVersion;
}
/**
 * Sets the fully qualified name of the plug-in for which this is a fragment
 * This object must not be read-only.
 *
 * @param value the name of this fragment's plug-in.
 *		May be <code>null</code>.
 */
public void setPlugin(String value) {
	assertIsWriteable();
	plugin = value;
}
/**
 * Sets the version name of the plug-in for which this is a fragment.
 * The given version number is canonicalized.
 * This object must not be read-only.
 *
 * @param value the version name of this fragment's plug-in.
 *		May be <code>null</code>.
 */
public void setPluginVersion(String value) {
	assertIsWriteable();
	// XXX workaround because some people still do not use the correct 
	// version format.
	int i = value.indexOf(' ');
	if (i > -1)
		value = value.substring(0, i);
	pluginVersion = new PluginVersionIdentifier(value).toString();
}
}
