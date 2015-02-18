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
 * An object which represents the user-defined contents of a plug-in
 * in a plug-in manifest.
 * <p>
 * This class may be instantiated, or further subclassed.
 * </p>
 * @deprecated In Eclipse 3.0 the runtime was refactored and all 
 * non-essential elements removed.  This class provides facilities primarily intended
 * for tooling.  As such it has been removed and no directly substitutable API provided.
 * This API will be deleted in a future release. See bug 370248 for details.
 */
public class PluginDescriptorModel extends PluginModel {

	// DTD properties (included in plug-in manifest)
	private String pluginClass = null;

	// transient properties (not included in plug-in manifest)
	private boolean enabled = true; // whether or not the plugin definition loaded ok
	private PluginFragmentModel[] fragments;

	/**
	 * Creates a new plug-in descriptor model in which all fields
	 * are <code>null</code>.
	 */
	public PluginDescriptorModel() {
		super();
	}

	/**
	 * Returns true if this plugin has all of it's prerequisites and is,
	 * therefore enabled.
	 * 
	 * @return whether or not this plug-in is fully resolved
	 */
	public boolean getEnabled() {
		return enabled;
	}

	/**
	 * Returns the fragments installed for this plug-in.
	 *
	 * @return this plug-in's fragments or <code>null</code>
	 */
	public PluginFragmentModel[] getFragments() {
		return fragments;
	}

	/**
	 * Returns the fully qualified name of the Java class which implements
	 * the runtime support for this plug-in.
	 *
	 * @return the name of this plug-in's runtime class or <code>null</code>.
	 */
	public String getPluginClass() {
		return pluginClass;
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
		return getId();
	}

	/**
	 * Sets the value of the field 'enabled' to the parameter 'value'.
	 * If this plugin is enabled (default) it is assumed to have all
	 * of it's prerequisites.
	 *
	 * @param value set to false if this plugin should be disabled and
	 * true otherwise.
	 */
	public void setEnabled(boolean value) {
		enabled = value;
	}

	/**
	 * Sets the list of fragments for this plug-in.
	 * This object must not be read-only.
	 *
	 * @param value the fragments for this plug-in.  May be <code>null</code>.
	 */
	public void setFragments(PluginFragmentModel[] value) {
		assertIsWriteable();
		fragments = value;
	}

	/**
	 * Sets the fully qualified name of the Java class which implements
	 * the runtime support for this plug-in.
	 * This object must not be read-only.
	 *
	 * @param value the name of this plug-in's runtime class.
	 *		May be <code>null</code>.
	 */
	public void setPluginClass(String value) {
		assertIsWriteable();
		pluginClass = value;
	}
}
