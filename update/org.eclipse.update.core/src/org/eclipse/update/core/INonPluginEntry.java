package org.eclipse.update.core;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

/**
 * Non-plug-in entry defines an arbitrary non-plug-in data file packaged
 * as part of a feature. Non-plug-in entries are not interpreted by the
 * platform (other than being downloaded as part of an install action).
 * They require a custom install handler to be specified as part of the
 * feature.
 * <p>
 * Clients may implement this interface. However, in most cases clients should 
 * directly instantiate or subclass the provided implementation of this 
 * interface.
 * </p>
 * @see org.eclipse.update.core.NonPluginEntry
 * @since 2.0
 */
public interface INonPluginEntry {

	/** 
	 * Returns the identifier of this data entry. 
	 * 
	 * @return data entry identifier
	 * @since 2.0 
	 */
	String getIdentifier();

	/**
	 * Returns the download size of the entry, if it can be determined.
	 * 
	 * @see org.eclipse.update.core.model.ContentEntryModel#UNKNOWN_SIZE
	 * @return download size of the feature in KiloBytes, or an indication 
	 * the size could not be determined
	 * @since 2.0 
	 */
	long getDownloadSize();

	/**
	 * Returns the install size of the feature, if it can be determined.
	 * 
	 * @see org.eclipse.update.core.model.ContentEntryModel#UNKNOWN_SIZE
	 * @return install size of the feature in KiloBytes, or an indication 
	 * the size could not be determined
	 * @since 2.0 
	 */
	long getInstallSize();

	/**
	 * Returns optional operating system specification.
	 * A comma-separated list of os designators defined by the platform.
	 * Indicates this feature should only be installed on one of the specified
	 * os systems. If this attribute is not specified, the feature can be
	 * installed on all systems (portable implementation).
	 * 
	 * This information is used as a hint by the installation and update
	 * support.
	 *
	 * @see org.eclipse.core.boot.BootLoader 
	 * @return the operating system specification, or <code>null</code>.
	 * @since 2.0 
	 */
	String getOS();

	/**
	 * Returns optional system architecture specification. 
	 * A comma-separated list of arch designators defined by the platform.
	 * Indicates this feature should only be installed on one of the specified
	 * systems. If this attribute is not specified, the feature can be
	 * installed on all systems (portable implementation).
	 * 
	 * This information is used as a hint by the installation and update
	 * support.
	 * 
	 * @see org.eclipse.core.boot.BootLoader 
	 * @return system architecture specification, or <code>null</code>.
	 * @since 2.0 
	 */
	String getWS();

	/**
	 * Returns optional system architecture specification. 
	 * A comma-separated list of arch designators defined by the platform.
	 * Indicates this feature should only be installed on one of the specified
	 * systems. If this attribute is not specified, the feature can be
	 * installed on all systems (portable implementation).
	 * 
	 * This information is used as a hint by the installation and update
	 * support.
	 * 
	 * @see org.eclipse.core.boot.BootLoader 
	 * @return system architecture specification, or <code>null</code>.
	 * @since 2.0 
	 */
	String getArch();

	/**
	 * Returns optional locale specification. 
	 * A comma-separated list of locale designators defined by Java.
	 * Indicates this feature should only be installed on a system running
	 * with a compatible locale (using Java locale-matching rules).
	 * If this attribute is not specified, the feature can be installed 
	 * on all systems (language-neutral implementation). 
	 * 
	 * This information is used as a hint by the installation and update
	 *  support.
	 * 
	 * @return the locale specification, or <code>null</code>.
	 * @since 2.0 
	 */
	String getNL();

}