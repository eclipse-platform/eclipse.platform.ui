package org.eclipse.update.internal.core;

import org.eclipse.update.core.IPluginContainer;
import org.eclipse.update.core.IPluginEntry;
import org.eclipse.update.core.VersionedIdentifier;

public class PluginEntry implements IPluginEntry {
	
	private boolean fragment = false;
	private IPluginContainer container;
	private VersionedIdentifier identifier;
	private String label;

	/**
	 * @see IPluginEntry#isFragment()
	 */
	public boolean isFragment() {
		return fragment;
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
		return identifier;
	}

	/**
	 * @see IPluginEntry#getLabel()
	 */
	public String getLabel() {
		return label;
	}

}

