package org.eclipse.update.core;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import org.eclipse.update.core.model.PluginEntryModel;

/**
 * Convenience implementation of plug-in entry.
 * <p>
 * This class may be instantiated or subclassed by clients.
 * </p> 
 * @see org.eclipse.update.core.IPluginEntry
 * @see org.eclipse.update.core.model.PluginEntryModel
 * @since 2.0
 */
public class PluginEntry extends PluginEntryModel implements IPluginEntry {

	/**
	 * Plug-in entry default constructor
	 */
	public PluginEntry() {
		super();
	}

	/**
	 * Returns the identifier of this plugin entry
	 * 
	 * @see IPluginEntry#getIdentifier()
	 * @since 2.0
	 */
	public VersionedIdentifier getVersionedIdentifier() {
		return new VersionedIdentifier(getPluginIdentifier(), getPluginVersion());
	}

	/**
	 * Sets the identifier of this plugin entry. 
	 * 
	 * @see IPluginEntry#setVersionedIdentifier(VersionedIdentifier)
	 * @since 2.0
	 */
	public void setVersionedIdentifier(VersionedIdentifier identifier) {
		setPluginIdentifier(identifier.getIdentifier());
		setPluginVersion(identifier.getVersion().toString());
	}

}