package org.eclipse.update.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
 /**
  *  A pluginEntry is a handle to a plugin 
  */
public interface INonPluginEntry {
	
	/** 
	 * Returns the identifier of this data entry
	 * 
	 * @return the identifier of the data entry
	 * @since 2.0 
	 */

	String getIdentifier();
	
	/**
	 * Returns the downloadSize
	 * optional hint supplied by the feature
	 *  packager, indicating the download size
	 *  in KBytes of the referenced data archive.
	 *  If not specified, the download size is not known 
	 * @return Returns a int
	 * @since 2.0 
	 */

	long getDownloadSize();


	/**
	 * Returns the installSize
	 * optional hint supplied by the feature
	 *  packager, indicating the install size in
	 *  KBytes of the referenced data archive.
	 *  If not specified, the install size is not known 	 * 
	 * @return Returns a int
	 * @since 2.0 
	 */

	long getInstallSize(); 
	
}

