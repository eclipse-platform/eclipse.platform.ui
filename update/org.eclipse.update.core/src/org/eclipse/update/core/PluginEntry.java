package org.eclipse.update.core;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import org.eclipse.update.core.IPluginContainer;
import org.eclipse.update.core.IPluginEntry;
import org.eclipse.update.core.VersionedIdentifier;
import org.eclipse.update.core.model.PluginEntryModel;
import org.eclipse.update.internal.core.*;

public class PluginEntry extends PluginEntryModel implements IPluginEntry {

	private IPluginContainer container;
	/**
	 * Constructor
	 */
	public PluginEntry() {
		super();
	}

	/**
	 * @see IPluginEntry#getContainer()
	 */
	public IPluginContainer getContainer() {
		return container;
	}

	/**
	 * @see IPluginEntry#getIdentifier()
	 */
	public VersionedIdentifier getIdentifier() {
		return new VersionedIdentifier(getPluginIdentifier(), getPluginVersion());
	}

	/**
	 * Sets the container
	 * @param container The container to set
	 * @since 2.0
	 */
	public void setContainer(IPluginContainer container) {
		this.container = container;
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