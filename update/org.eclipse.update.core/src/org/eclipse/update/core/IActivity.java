package org.eclipse.update.core;

import java.util.Date;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

public interface IActivity {
	
	public static final int ACTION_FEATURE_INSTALL 	= 1;
	public static final int ACTION_FEATURE_REMOVE 	= 2;
	public static final int ACTION_SITE_INSTALL 			= 3;
	public static final int ACTION_SITE_REMOVE 			= 4;
	public static final int ACTION_UNCONFIGURE 			= 5;
	public static final int ACTION_CONFIGURE 				= 6;
	public static final int ACTION_REVERT 						= 7;	
	
	public static final int STATUS_OK 			= 0;
	public static final int STATUS_NOK 		= 1;

	
	/**
	 * Returns teh user description of teh action
	 */
	String getLabel();
	
	/**
	 * Return the Action String for this activity
	 */ 
	int getAction();
	
	/**
	 * Return the creation date of this activity
	 */
	Date getDate();
	
	/** 
	 * return the status
	 */
	int getStatus();
}

