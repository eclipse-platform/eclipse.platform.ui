package org.eclipse.update.core;

import java.util.Date;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

public interface IActivity {
	
	public static final String ACTION_FEATURE_INSTALL = "feature_install";
	public static final String ACTION_FEATURE_REMOVE = "feature_uninstall";
	public static final String ACTION_SITE_INSTALL = "site_install";
	public static final String ACTION_SITE_REMOVE = "site_uninstall";
	public static final String ACTION_UNCONFIGURE = "unconfigure";
	public static final String ACTION_CONFIGURE = "configure";
	
	public static final int STATUS_OK = 0;
	public static final int STATUS_NOK = 1;
	public static final int STATUS_REVERT = 2;
	
	/**
	 * Returns teh user description of teh action
	 */
	String getLabel();
	
	/**
	 * Return the Action String for this activity
	 */ 
	String getAction();
	
	/**
	 * Return the creation date of this activity
	 */
	Date getDate();
	
	/** 
	 * return the status
	 */
	int getStatus();
}

