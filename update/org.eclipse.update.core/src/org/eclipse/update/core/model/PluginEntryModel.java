package org.eclipse.update.core.model;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */ 

/**
 * An object which represents a plug-in entry in the
 * packaging manifest.
 * <p>
 * This class may be instantiated, or further subclassed.
 * </p>
 * @since 2.0
 */

public class PluginEntryModel extends ContentEntryModel {
	
	private String pluginId;
	private String pluginVersion;
	private boolean isFragment = false;
	
	/**
	 * Creates a uninitialized plug-in entry model object.
	 * 
	 * @since 2.0
	 */
	public PluginEntryModel() {
		super();
	}
	
	/**
	 * Returns the plug-in identifier for this entry.
	 * 
	 * @return the plug-in identifier, or <code>null</code>
	 * @since 2.0 
	 */
	public String getPluginIdentifier() {
		return pluginId;
	}
	
	/**
	 * Returns the plug-in version for this entry.
	 * 
	 * @return the plug-in version, or <code>null</code>
	 * @since 2.0 
	 */
	public String getPluginVersion() {
		return pluginVersion;
	}
			
	/**
	 * Indicates whether the entry describes a full plug-in, or 
	 * a plug-in fragment.
	 * @return <code>true</code> if the plugin is a fragment, 
	 * 		<code>false</code> otherwise
	 * @since 2.0 
	 */
	public boolean isFragment() {
		return isFragment;
	}
	
	/**
	 * Sets the entry plug-in identifier.
	 * This object must not be read-only.
	 *
	 * @param pluginId the entry identifier. May be <code>null</code>.
	 * @since 2.0
	 */	
	public void setPluginIdentifier(String pluginId) {
		assertIsWriteable();
		this.pluginId = pluginId;
	}
	
	/**
	 * Sets the entry plug-in version.
	 * This object must not be read-only.
	 *
	 * @param pluginVersion the entry version. May be <code>null</code>.
	 * @since 2.0
	 */	
	public void setPluginVersion(String pluginVersion) {
		assertIsWriteable();
		this.pluginVersion = pluginVersion;
	}
	
	/**
	 * Indicates whether this entry represents a fragment.
	 * This object must not be read-only.
	 *
	 * @param isFragment fragment setting
	 * @since 2.0
	 */	
	public void isFragment(boolean isFragment) {
		assertIsWriteable();
		this.isFragment = isFragment;
	}
}
