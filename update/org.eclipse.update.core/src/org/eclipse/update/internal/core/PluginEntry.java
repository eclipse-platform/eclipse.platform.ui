package org.eclipse.update.internal.core;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.update.core.IPluginContainer;
import org.eclipse.update.core.IPluginEntry;
import org.eclipse.update.core.VersionedIdentifier;

public class PluginEntry implements IPluginEntry {
	
	private boolean fragment = false;
	private IPluginContainer container;
	private VersionedIdentifier identifier;
	private String label;
	private int downloadSize ;
	private int installSize;
	private String os;
	private String ws;
	private String nl;
	
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

	/**
	 * Gets the downloadSize
	 * @return Returns a int
	 */
	public int getDownloadSize() {
		return downloadSize;
	}
	/**
	 * Sets the downloadSize
	 * @param downloadSize The downloadSize to set
	 */
	public void setDownloadSize(int downloadSize) {
		this.downloadSize = downloadSize;
	}

	/**
	 * Gets the installSize
	 * @return Returns a int
	 */
	public int getInstallSize() {
		return installSize;
	}
	/**
	 * Sets the installSize
	 * @param installSize The installSize to set
	 */
	public void setInstallSize(int installSize) {
		this.installSize = installSize;
	}

	/**
	 * @see IPluginEntry#getOS()
	 */
	public String getOS() {
		return null;
	}

	/**
	 * @see IPluginEntry#getWS()
	 */
	public String getWS() {
		return null;
	}

	/**
	 * @see IPluginEntry#getNL()
	 */
	public String getNL() {
		return null;
	}

/**
	 * Sets the nl
	 * @param nl The nl to set
	 */
	public void setNL(String nl) {
		this.nl = nl;
	}
	
	/**
	 * Sets the os
	 * @param os The os to set
	 */
	public void setOS(String os) {
		this.os = os;
	}
	
	/**
	 * Sets the ws
	 * @param ws The ws to set
	 */
	public void setWS(String ws) {
		this.ws = ws;
	}
}

