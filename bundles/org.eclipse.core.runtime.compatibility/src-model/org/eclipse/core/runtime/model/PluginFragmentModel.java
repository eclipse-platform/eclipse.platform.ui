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

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.PluginVersionIdentifier;

/**
 * An object which represents the user-defined contents of a plug-in fragment
 * in a plug-in manifest.
 * <p>
 * This class may be instantiated, or further subclassed.
 * </p>
 * @deprecated In Eclipse 3.0 the runtime was refactored and all 
 * non-essential elements removed.  This class provides facilities primarily intended
 * for tooling.  As such it has been removed and no directly substitutable API provided.
 * This API will be deleted in a future release. See bug 370248 for details.
 */
public class PluginFragmentModel extends PluginModel {

	public static final byte FRAGMENT_MATCH_UNSPECIFIED = 0;
	public static final byte FRAGMENT_MATCH_PERFECT = 1;
	public static final byte FRAGMENT_MATCH_EQUIVALENT = 2;
	public static final byte FRAGMENT_MATCH_COMPATIBLE = 3;
	public static final byte FRAGMENT_MATCH_GREATER_OR_EQUAL = 4;

	// DTD properties (included in plug-in manifest)
	private String plugin = null;
	private String pluginVersion = null;
	private byte pluginMatch = FRAGMENT_MATCH_UNSPECIFIED;

	/**
	 * Creates a new plug-in descriptor model in which all fields
	 * are <code>null</code>.
	 */
	public PluginFragmentModel() {
		super();
	}

	/**
	 * Returns a byte code indicating the type of match this fragment requires
	 * when trying to find its associated plugin.
	 * The byte code can be any one of the following:
	 * FRAGMENT_MATCH_UNSPECIFIED			initial value
	 * FRAGMENT_MATCH_PERFECT				perfectly equal match
	 * FRAGMENT_MATCH_EQUIVALENT			equivalent match
	 * FRAGMENT_MATCH_COMPATIBLE			compatible match
	 * FRAGMENT_MATCH_GREATER_OR_EQUAL		greater than or equal to match
	 *
	 * @return a byte code indicating the type of match this fragment requires
	 * @since 2.0
	 */
	public byte getMatch() {
		return pluginMatch;
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
	 * Sets the type of match this fragment requires when trying to
	 * find its associated plugin.  The value parameter may be any
	 * one of the following:
	 * FRAGMENT_MATCH_UNSPECIFIED			initial value
	 * FRAGMENT_MATCH_PERFECT				perfectly equal match
	 * FRAGMENT_MATCH_EQUIVALENT			equivalent match
	 * FRAGMENT_MATCH_COMPATIBLE			compatible match
	 * FRAGMENT_MATCH_GREATER_OR_EQUAL		greater than or equal to match
	 * This object must not be read-only.
	 *
	 * @param value the type of match required with the associated plugin
	 * @since 2.0
	 */
	public void setMatch(byte value) {
		assertIsWriteable();
		Assert.isTrue((value == FRAGMENT_MATCH_PERFECT) || (value == FRAGMENT_MATCH_EQUIVALENT) || (value == FRAGMENT_MATCH_COMPATIBLE) || (value == FRAGMENT_MATCH_GREATER_OR_EQUAL));
		pluginMatch = value;
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
		pluginVersion = new PluginVersionIdentifier(value).toString();
	}
}
