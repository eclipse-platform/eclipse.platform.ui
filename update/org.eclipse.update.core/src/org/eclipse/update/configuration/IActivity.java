package org.eclipse.update.configuration;

import java.util.Date;

import org.eclipse.core.runtime.IAdaptable;


/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

public interface IActivity extends IAdaptable {
	
	public static final int ACTION_FEATURE_INSTALL 	= 1;
	public static final int ACTION_FEATURE_REMOVE 	= 2;
	public static final int ACTION_SITE_INSTALL 		= 3;
	public static final int ACTION_SITE_REMOVE 		= 4;
	public static final int ACTION_UNCONFIGURE 		= 5;
	public static final int ACTION_CONFIGURE 		= 6;
	public static final int ACTION_REVERT 			= 7;	
	public static final int ACTION_RECONCILIATION		= 8;		
	
	public static final int STATUS_OK 				= 0;
	public static final int STATUS_NOK 				= 1;

	
	/**
	 * Returns teh user description of teh action
	 * @since 2.0 
	 */

	String getLabel();
	
	/**
	 * Return the Action String for this activity
	 * @since 2.0 
	 */
 
	int getAction();
	
	/**
	 * Return the creation date of this activity
	 * @since 2.0 
	 */

	Date getDate();
	
	/** 
	 * return the status
	 * @since 2.0 
	 */

	int getStatus();
	
	/**
	 * rerun the InstallConfig
	 * @since 2.0
	 */
	IInstallConfiguration getInstallConfiguration();
}

