package org.eclipse.update.internal.core;

import org.eclipse.update.core.IDataEntry;
import org.eclipse.update.core.VersionedIdentifier;

public class DataEntry implements IDataEntry {
	
	private String identifier;
	private int downloadSize ;
	private int installSize;
	
	/**
	 * Constructor
	 */
	public DataEntry(String identifier) {
		this.identifier = identifier;
	}

	/*
	 * @see IDataEntry#getIdentifier()
	 */
	public String getIdentifier() {
		return identifier;
	}

	/*
	 * @see IDataEntry#getDownloadSize()
	 */
	public int getDownloadSize() {
		return downloadSize;
	}

	/*
	 * @see IDataEntry#getInstallSize()
	 */
	public int getInstallSize() {
		return installSize;
	}

	/**
	 * Sets the identifier.
	 * @param identifier The identifier to set
	 */
	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	/**
	 * Sets the downloadSize.
	 * @param downloadSize The downloadSize to set
	 */
	public void setDownloadSize(int downloadSize) {
		this.downloadSize = downloadSize;
	}

	/**
	 * Sets the installSize.
	 * @param installSize The installSize to set
	 */
	public void setInstallSize(int installSize) {
		this.installSize = installSize;
	}

}

