package org.eclipse.update.core;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import org.eclipse.update.core.IPluginEntry;
import org.eclipse.update.core.VersionedIdentifier;
import org.eclipse.update.core.model.PluginEntryModel;
import org.eclipse.update.internal.core.*;

public class PluginEntry extends PluginEntryModel implements IPluginEntry {

	/**
	 * Constructor
	 */
	public PluginEntry() {
		super();
	}

	
	/**
	 * @see IPluginEntry#getIdentifier()
	 */
	public VersionedIdentifier getVersionedIdentifier() {
		return new VersionedIdentifier(getPluginIdentifier(), getPluginVersion());
	}

	
	/**
	 * Sets the identifier
	 * @param identifier The identifier to set
	 * @since 2.0
	 */
	public void setIdentifier(VersionedIdentifier identifier) {
		setPluginIdentifier(identifier.getIdentifier());
		setPluginVersion(identifier.getVersion().toString());
	}
	
	
}