package org.eclipse.update.core;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
 
 /**
  *  A pluginEntry is a handle to a plugin 
  */
public interface IPluginEntry {
	
	/**
	 * Returns <code>true</code> if the plugin is a fragment
	 * @return true if handles a fragment
	 * @since 2.0 
	 */

	boolean isFragment();
	
	/**
	 * Returns the container fo this plugin
	 * 
	 * @see IPluginContainer
	 * @return the plugin container
	 * @since 2.0 
	 */

	IPluginContainer getContainer();
	
	/** 
	 * Returns the identifier of this plugin entry
	 * 
	 * @return the identifier of the plugin entry
	 * @since 2.0 
	 */

	VersionedIdentifier getIdentifier();
	
	/**
	 * Optional operating system specification.
	 * A comma-separated list of os designators defined by Eclipse.
	 * Indicates this feature should only be installed on one of the specified
	 * os systems. If this attribute is not specified, the feature can be
	 * installed on all systems (portable implementation).
	 * 
	 * This information is used as a hint by the installation and update
	 * support (user can force installation of feature regardless of this setting).
	 *
	 * @see org.eclipse.core.boot.BootLoader 
	 * @return the operating system specification.
	 * @since 2.0 
	 */

	String getOS();
	
	/**
	 * Optional windowing system specification. 
	 * A comma-separated list of ws designators defined by Eclipse.
	 *  Indicates this feature should only be installed on one of the specified
	 *  ws systems. If this attribute is not specified, the feature can be
	 *  installed on all systems (portable implementation).
	 * 
	 * This information is used as a hint by the installation and update
	 * support (user can force installation of feature regardless of this setting).
	 * 
	 * @see org.eclipse.core.boot.BootLoader 
	 * @return the windowing system specification.
	 * @since 2.0 
	 */

	String getWS();
	
	/**
	 * Optional locale specification. 
	 * A comma-separated list of locale designators defined by Java.
	 * Indicates this feature should only be installed on a system running
	 * with a compatible locale (using Java locale-matching rules).
	 * If this attribute is not specified, the feature can be installed 
	 * on all systems (language-neutral implementation). 
	 * 
	 * This information is used as a hint by the installation and update
	 *  support (user can force installation of feature regardless of this setting).
	 * 
	 * @return the locale specification.
	 * @since 2.0 
	 */

	String getNL();
	
	
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
	 *  If not specified, the install size is not known 
	 * @return Returns a int
	 * @since 2.0 
	 */

	long getInstallSize(); 
	
}

