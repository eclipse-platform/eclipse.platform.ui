package org.eclipse.core.runtime.model;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.PluginVersionIdentifier;

/**
 * An object which represents the user-defined contents of a component model
 * in a component manifest.
 * <p>
 * This class may be instantiated and further subclassed.
 * </p>
 */

public class ComponentModel extends InstallModel {

	// DTD properties (included in install manifest)
	private boolean allowUpgrade = false;
	private boolean optional = false;
	private PluginDescriptorModel[] plugins = new PluginDescriptorModel[0];
	private PluginFragmentModel[] fragments = new PluginFragmentModel[0];

/**
 * Returns whether this component can be upgraded within its configuration.
 *
 * @return whether this component is upgradeable
 */
public boolean getAllowUpgrade() {
	return allowUpgrade;
}

/**
 * Returns whether this component is optional within its configuration.
 *
 * @return whether this component is optional
 */
public boolean getOptional() {
	return optional;
}

/**
 * Returns the list of plug-ins managed by this component.
 *
 * @return the plug-ins in this component
 */
public PluginDescriptorModel[] getPlugins() {
	return plugins;
}

/**
 * Returns the list of fragments managed by this component.
 *
 * @return the fragments in this component
 */
public PluginFragmentModel[] getFragments() {
	return fragments;
}

/**
 * Sets this model object and all of its descendents to be read-only.
 * Subclasses may extend this implementation.
 *
 * @see #isReadOnly
 */
public void markReadOnly() {
	super.markReadOnly();
	if (plugins != null)
		for (int i = 0; i < plugins.length; i++)
			plugins[i].markReadOnly();
	if (fragments != null)
		for (int i = 0; i < fragments.length; i++)
			fragments[i].markReadOnly();
}

/**
 * Sets whether this component can be upgraded within its configuration.
 * This object must not be read-only.
 *
 * @param value whether this component is upgradeable
 */
public void setAllowUpgrade(boolean value) {
	assertIsWriteable();
	allowUpgrade = value;
}

/**
 * Sets whether this component is optional within its configuration.
 * This object must not be read-only.
 *
 * @param value whether this component is optional
 */
public void setOptional(boolean value) {
	assertIsWriteable();
	optional = value;
}

/**
 * Sets the list of plug-ins managed by this component.
 * This object must not be read-only.
 *
 * @param value the plug-ins managed by this component
 */
public void setPlugins(PluginDescriptorModel[] value) {
	assertIsWriteable();
	plugins = value;
}

/**
 * Sets the list of fragments managed by this component.
 * This object must not be read-only.
 *
 * @param value the fragments managed by this component
 */
public void setFragments(PluginFragmentModel[] value) {
	assertIsWriteable();
	fragments = value;
}

}
