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

	/**
	 * Compares two plugin entries for equality
	 * 
	 * @param object plugin entry object to compare with
	 * @return <code>true</code> if the two entries are equal, 
	 * <code>false</code> otherwise
	 * @since 2.0
	 */
	public boolean equals(Object object) {
		if (!(object instanceof IPluginEntry))
			return false;
		IPluginEntry e = (IPluginEntry) object;
		return getVersionedIdentifier().equals(e.getVersionedIdentifier());
	}

}