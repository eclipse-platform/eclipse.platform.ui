package org.eclipse.update.core.model;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */ 

/**
 * An object which represents the definition of a custom
 * install handler in the packaging manifest.
 * <p>
 * This class may be instantiated, or further subclassed.
 * </p>
 * @since 2.0
 */

public class ImportModel extends ModelObject {
	
	private String pluginId;
	private String pluginVersion;
	private String matchingRuleName;
	
	/**
	 * Creates a uninitialized model object.
	 * 
	 * @since 2.0
	 */
	public ImportModel() {
		super();
	}
	
	/**
	 * Returns the dependent plug-in identifier.
	 *
	 * @return plug-in identifier or <code>null</code>.
	 * @since 2.0
	 */
	public String getPluginIdentifier() {
		return pluginId;
	}
	
	/**
	 * Returns the dependent plug-in version.
	 *
	 * @return plug-in version or <code>null</code>.
	 * @since 2.0
	 */
	public String getPluginVersion() {
		return pluginVersion;
	}
	
	/**
	 * Returns the dependent plug-in version matching rule.
	 *
	 * @return matching rule identifier.
	 * @since 2.0
	 */
	public String getMatchingRuleName() {
		return matchingRuleName;
	}
	
	/**
	 * Sets the dependent plug-in identifier.
	 * This object must not be read-only.
	 *
	 * @param pluginId dependent plugi-in identifier. May be <code>null</code>.
	 * @since 2.0
	 */	
	public void setPluginIdentifier(String pluginId) {
		assertIsWriteable();
		this.pluginId = pluginId;
	}
	
	/**
	 * Sets the dependent plug-in version.
	 * This object must not be read-only.
	 *
	 * @param pluginVersion dependent plug-in version. May be <code>null</code>.
	 * @since 2.0
	 */	
	public void setPluginVersion(String pluginVersion) {
		assertIsWriteable();
		this.pluginVersion = pluginVersion;
	}
	
	/**
	 * Sets the dependent plug-in version matching rule. 
	 * This object must not be read-only.
	 *
	 * @param matchingRuleName dependent plug-in version matching rule.
	 * @since 2.0
	 */	
	public void setMatchingRuleName(String matchingRuleName) {
		assertIsWriteable();
		this.matchingRuleName = matchingRuleName;
	}
}
