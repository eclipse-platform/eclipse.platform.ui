package org.eclipse.update.configuration;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import java.util.Date;

import org.eclipse.core.runtime.IAdaptable;

/**
 * Install activity.
 * Represents a record of an installation action performed
 * on a particular installation configuration.
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 * @see org.eclipse.update.configuration.IInstallConfiguration
 * @since 2.0
 */
public interface IActivity extends IAdaptable {

	/**
	 * Indicates feature installation action
	 * 
	 * @since 2.0
	 */
	public static final int ACTION_FEATURE_INSTALL = 1;

	/**
	 * Indicates feature removal (uninstallation) action
	 * 
	 * @since 2.0
	 */
	public static final int ACTION_FEATURE_REMOVE = 2;

	/**
	 * Indicates an addition of a site to a configuration
	 * 
	 * @since 2.0
	 */
	public static final int ACTION_SITE_INSTALL = 3;

	/**
	 * Indicates removal of a site from a configuration
	 * 
	 * @since 2.0
	 */
	public static final int ACTION_SITE_REMOVE = 4;

	/**
	 * Indicates feature unconfiguration action
	 * 
	 * @since 2.0
	 */
	public static final int ACTION_UNCONFIGURE = 5;

	/**
	 * Indicates feature configuration action
	 * 
	 * @since 2.0
	 */
	public static final int ACTION_CONFIGURE = 6;

	/**
	 * Indicates reverting to a prior configuration state
	 * 
	 * @since 2.0
	 */
	public static final int ACTION_REVERT = 7;

	/**
	 * Indicates reconcilliation with changes made directly to the site
	 * installation directory
	 * 
	 * @since 2.0
	 */
	public static final int ACTION_RECONCILIATION = 8;

	/**
	 * Indicates adding the configuration to a preserved state
	 * 
	 * @since 2.0
	 */
	public static final int ACTION_ADD_PRESERVED = 9;
	
	/**
	 * Indicates the action completed cussessfully
	 * 
	 * @since 2.0
	 */
	public static final int STATUS_OK = 0;

	/**
	 * Indicates the action did not complete successfully
	 * 
	 * @since 2.0
	 */
	public static final int STATUS_NOK = 1;

	/**
	 * Returns the action code for this activity
	 * 
	 * @see #ACTION_FEATURE_INSTALL
	 * @see #ACTION_FEATURE_REMOVE
	 * @see #ACTION_SITE_INSTALL
	 * @see #ACTION_SITE_REMOVE
	 * @see #ACTION_UNCONFIGURE
	 * @see #ACTION_CONFIGURE
	 * @see #ACTION_REVERT
	 * @see #ACTION_RECONCILIATION
	 * @return action code, as defined in this interface
	 * @since 2.0 
	 */
	public int getAction();

	/**
	 * Returns the displayable label for this action
	 * 
	 * @return diplayable label for action
	 * @since 2.0 
	 */
	public String getLabel();

	/**
	 * Returns the creation date of this activity
	 * 
	 * @return activity date
	 * @since 2.0 
	 */
	public Date getDate();

	/** 
	 * Returns the activity completion status
	 * 
	 * @see #STATUS_OK
	 * @see #STATUS_NOK
	 * @return completion status, as defined in this interface
	 * @since 2.0 
	 */
	public int getStatus();

	/**
	 * Returns the installation configuration that was the result of 
	 * this action
	 * 
	 * @return installation configuration
	 * @since 2.0
	 */
	public IInstallConfiguration getInstallConfiguration();
}