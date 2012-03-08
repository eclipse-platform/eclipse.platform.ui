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

/**
 * An object which represents the relationship between a plug-in and a
 * prerequisite plug-in in the dependent's plug-in manifest.
 * <p>
 * This class may be instantiated, or further subclassed.
 * </p>
 * @deprecated In Eclipse 3.0 the runtime was refactored and all 
 * non-essential elements removed.  This class provides facilities primarily intended
 * for tooling.  As such it has been removed and no directly substitutable API provided.
 * This API will be deleted in a future release. See bug 370248 for details.
 */
public class PluginPrerequisiteModel extends PluginModelObject {

	public static final byte PREREQ_MATCH_UNSPECIFIED = 0;
	public static final byte PREREQ_MATCH_PERFECT = 1;
	public static final byte PREREQ_MATCH_EQUIVALENT = 2;
	public static final byte PREREQ_MATCH_COMPATIBLE = 3;
	public static final byte PREREQ_MATCH_GREATER_OR_EQUAL = 4;

	// DTD properties (included in plug-in manifest)
	private String plugin = null;
	private String version = null;
	private byte match = PREREQ_MATCH_UNSPECIFIED;
	private boolean export = false;
	private String resolvedVersion = null;
	private boolean optional = false;

	/**
	 * Creates a new plug-in prerequisite model in which all fields
	 * are <code>null</code>.
	 */
	public PluginPrerequisiteModel() {
		super();
	}

	/**
	 * Returns whether or not the code in this pre-requisite is exported.
	 *
	 * @return whether or not the code in this pre-requisite is exported
	 */
	public boolean getExport() {
		return export;
	}

	/**
	 * Returns whether or not this pre-requisite requires an exact match.
	 *
	 * @return whether or not this pre-requisite requires an exact match
	 * @deprecated - use getMatchByte
	 */
	public boolean getMatch() {
		return (match == PREREQ_MATCH_EQUIVALENT);
	}

	/**
	 * Returns a byte code indicating the type of match this pre-requisite requires.
	 * The byte code can be any one of the following:
	 * PREREQ_MATCH_UNSPECIFIED			initial value
	 * PREREQ_MATCH_PERFECT				perfectly equal match
	 * PREREQ_MATCH_EQUIVALENT			equivalent match
	 * PREREQ_MATCH_COMPATIBLE			compatible match
	 * PREREQ_MATCH_GREATER_OR_EQUAL	greater than or equal to match
	 *
	 * @return a byte code indicating the type of match this pre-requisite requires
	 * @since 2.0
	 */
	public byte getMatchByte() {
		return match;
	}

	/**
	 * Returns whether this pre-requisite is optional.
	 *
	 * @return whether this pre-requisite is optional
	 */
	public boolean getOptional() {
		return optional;
	}

	/**
	 * Returns the plug-in identifier of the prerequisite plug-in.
	 * 
	 * @return the plug-in identifier or <code>null</code>
	 */
	public String getPlugin() {
		return plugin;
	}

	/**
	 * Returns the resolved version of the prerequisite plug-in.  The
	 * returned value is in the format specified by <code>PluginVersionIdentifier</code>.
	 *
	 * @return the version of the prerequisite plug-in
	 * @see org.eclipse.core.runtime.PluginVersionIdentifier
	 */
	public String getResolvedVersion() {
		return resolvedVersion;
	}

	/**
	 * Returns the version name of this plug-in.
	 *
	 * @return the version name of this plug-in or <code>null</code>
	 */
	public String getVersion() {
		return version;
	}

	/**
	 * Sets whether or not the code in this pre-requisite is exported.
	 * This object must not be read-only.
	 *
	 * @param value whether or not the code in this pre-requisite is exported
	 */
	public void setExport(boolean value) {
		assertIsWriteable();
		export = value;
	}

	/**
	 * Sets whether or not this pre-requisite requires an exact match.
	 * This object must not be read-only.
	 *
	 * @param value whether or not this pre-requisite requires an exact match
	 * @deprecated use setMatchByte
	 */
	public void setMatch(boolean value) {
		assertIsWriteable();
		if (value) {
			setMatchByte(PREREQ_MATCH_EQUIVALENT);
		} else {
			setMatchByte(PREREQ_MATCH_COMPATIBLE);
		}
	}

	/**
	 * Sets whether or not this pre-requisite requires an exact match.
	 * This object must not be read-only.
	 *
	 * @param value whether or not this pre-requisite requires an exact match
	 * @since 2.0
	 */
	public void setMatchByte(byte value) {
		assertIsWriteable();
		Assert.isTrue((value == PREREQ_MATCH_PERFECT) || (value == PREREQ_MATCH_EQUIVALENT) || (value == PREREQ_MATCH_COMPATIBLE) || (value == PREREQ_MATCH_GREATER_OR_EQUAL));
		match = value;
	}

	/**
	 * Sets whether this pre-requisite is optional.
	 * This object must not be read-only.
	 *
	 * @param value whether this pre-requisite is optional
	 */
	public void setOptional(boolean value) {
		assertIsWriteable();
		optional = value;
	}

	/**
	 * Sets the plug-in identifier of this prerequisite plug-in.
	 * This object must not be read-only.
	 * 
	 * @param value the prerequisite plug-in identifier.  May be <code>null</code>.
	 */
	public void setPlugin(String value) {
		assertIsWriteable();
		plugin = value;
	}

	/**
	 * Sets the resolved version of the prerequisite plug-in.  The
	 * given value is in the format specified by <code>PluginVersionIdentifier</code>.
	 *
	 * @param value the version of the prerequisite plug-in
	 * @see org.eclipse.core.runtime.PluginVersionIdentifier
	 */
	public void setResolvedVersion(String value) {
		assertIsWriteable();
		resolvedVersion = value;
	}

	/**
	 * Sets the version name of this plug-in prerequisite.
	 * This object must not be read-only.
	 *
	 * @param value the version name of this plug-in prerequisite.
	 *		May be <code>null</code>.
	 */
	public void setVersion(String value) {
		assertIsWriteable();
		version = value;
	}
}
