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
	 * Constructor
	 */
	public PluginEntry(VersionedIdentifier identifier) {
		this.identifier = identifier;
	}
	
	/**
	 * Constructor
	 */
	public PluginEntry(String id, String ver) {
		this(new VersionedIdentifier(id,ver));
	}

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

	

	/**
	 * Sets the label
	 * @param label The label to set
	 */
	public void setLabel(String label) {
		this.label = label;
	}

	/**
	 * Sets the container
	 * @param container The container to set
	 */
	public void setContainer(IPluginContainer container) {
		this.container = container;
	}

	/**
	 * Sets the fragment
	 * @param fragment The fragment to set
	 */
	public void setFragment(boolean fragment) {
		this.fragment = fragment;
	}

}

