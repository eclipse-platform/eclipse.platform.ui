package org.eclipse.update.core.model;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

/**
 * Plug-in dependency model object.
 * <p>
 * This class may be instantiated or subclassed by clients. However, in most 
 * cases clients should instead instantiate or subclass the provided 
 * concrete implementation of this model.
 * </p>
 * @see org.eclipse.update.core.Import
 * @since 2.0
 */
public class ImportModel extends ModelObject {

	private String pluginId;
	private String pluginVersion;
	private String matchingRuleName;

	/**
	 * Creates a uninitialized plug-in dependency model object.
	 * 
	 * @since 2.0
	 */
	public ImportModel() {
		super();
	}

	/**
	 * Returns the dependent plug-in identifier.
	 *
	 * @return plug-in identifier, or <code>null</code>.
	 * @since 2.0
	 */
	public String getPluginIdentifier() {
		return pluginId;
	}

	/**
	 * Returns the dependent plug-in version.
	 *
	 * @return plug-in version, or <code>null</code>.
	 * @since 2.0
	 */
	public String getPluginVersion() {
		return pluginVersion;
	}

	/**
	 * Returns the dependent plug-in version matching rule name.
	 *
	 * @return matching rule name, or <code>null</code>.
	 * @since 2.0
	 */
	public String getMatchingRuleName() {
		return matchingRuleName;
	}

	/**
	 * Sets the dependent plug-in identifier.
	 * Throws a runtime exception if this object is marked read-only.
	 *
	 * @param pluginId dependent plug-in identifier
	 * @since 2.0
	 */
	public void setPluginIdentifier(String pluginId) {
		assertIsWriteable();
		this.pluginId = pluginId;
	}

	/**
	 * Sets the dependent plug-in version.
	 * Throws a runtime exception if this object is marked read-only.
	 *
	 * @param pluginVersion dependent plug-in version
	 * @since 2.0
	 */
	public void setPluginVersion(String pluginVersion) {
		assertIsWriteable();
		this.pluginVersion = pluginVersion;
	}

	/**
	 * Sets the dependent plug-in version matching rule name. 
	 * Throws a runtime exception if this object is marked read-only.
	 *
	 * @param matchingRuleName dependent plug-in version matching rule.
	 * @since 2.0
	 */
	public void setMatchingRuleName(String matchingRuleName) {
		assertIsWriteable();
		this.matchingRuleName = matchingRuleName;
	}
}