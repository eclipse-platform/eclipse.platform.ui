/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ant.ui.internal.model;


/**
 * Defines the constants available for client use.
 * <p>
 * This interface is not intended to be extended or implemented by clients.
 * </p>
 */
public interface IAntUIConstants {
	/**
	 * Plugin identifier for Ant ui(value <code>org.eclipse.ant.ui</code>).
	 */
	public static final String PLUGIN_ID = "org.eclipse.ant.ui"; //$NON-NLS-1$;
	

	// ------- Views -------

	/**
	 * Ant View identifier (value <code>org.eclipse.ant.ui.AntView</code>).
	 */
	public static final String ANT_VIEW_ID = PLUGIN_ID + ".AntView"; //$NON-NLS-1$

	/**
	 * External tool type for Ant build files (value <code>antBuildType</code>).
	 */
	public static final String TOOL_TYPE_ANT_BUILD = "antBuildType"; //$NON-NLS-1$;

	// ------- Images -------
	/**
	 * Refresh action image
	 */
	public static final String IMG_ACTION_REFRESH = PLUGIN_ID + ".IMG_ACTION_REFRESH"; //$NON-NLS-1$

	/**
	 * Main tab image.
	 */
	public static final String IMG_TAB_MAIN = PLUGIN_ID + ".IMG_TAB_MAIN"; //$NON-NLS-1$

	/**
	 * Property image
	 */
	public static final String IMG_PROPERTY = PLUGIN_ID + ".IMG_PROPERTY"; //$NON-NLS-1$
	
	/**
	 * Ant Targets tab image.
	 */
	public static final String IMG_TAB_ANT_TARGETS = PLUGIN_ID + ".IMG_TAB_ANT_TARGETS"; //$NON-NLS-1$
	public static final String IMG_TAB_CLASSPATH = PLUGIN_ID + ".IMG_TAB_CLASSPATH"; //$NON-NLS-1$;
	public static final String IMG_JAR_FILE = PLUGIN_ID + ".IMG_JAR_FILE"; //$NON-NLS-1$;
	
	//	Label images
	 public static final String IMG_ANT_PROJECT= PLUGIN_ID + ".antProject"; //$NON-NLS-1$
	 public static final String IMG_ANT_TARGET= PLUGIN_ID + ".antTarget"; //$NON-NLS-1$
	 public static final String IMG_ANT_TARGET_PRIVATE = PLUGIN_ID + ".antPrivateTarget"; //$NON-NLS-1$
	 public static final String IMG_ANT_DEFAULT_TARGET= PLUGIN_ID + ".antDefaultTarget"; //$NON-NLS-1$
	 public static final String IMG_ANT_TARGET_ERROR = PLUGIN_ID + ".antTargetError"; //$NON-NLS-1$
	 
	/**
	 * Icon for task proposal.
	 */
	public static final String IMG_TASK_PROPOSAL = PLUGIN_ID + ".html_tab_obj"; //$NON-NLS-1$
	
	// Action images
	public static final String IMG_REMOVE= PLUGIN_ID + ".remove"; //$NON-NLS-1$
	public static final String IMG_REMOVE_ALL= PLUGIN_ID + ".removeAll"; //$NON-NLS-1$
	public static final String IMG_ADD= PLUGIN_ID + ".add"; //$NON-NLS-1$
	public static final String IMG_RUN= PLUGIN_ID + ".run"; //$NON-NLS-1$
	public static final String IMG_SEARCH= PLUGIN_ID + ".search"; //$NON-NLS-1$
	public static final String IMG_TOGGLE= PLUGIN_ID + ".toggle"; //$NON-NLS-1$
	 
	//	Overlays
	 public static final String IMG_OVR_ERROR = PLUGIN_ID + ".ovrError";  //$NON-NLS-1$
	
	public static final String IMG_ANT_TYPE= PLUGIN_ID + ".type"; //$NON-NLS-1$

	// -------- Status Codes -------------
	
	/**
	 * Status code used by the 'Run Ant' status handler which is invoked when
	 * the launch dialog is opened by the 'Run Ant' action.
	 */
	public static final int STATUS_INIT_RUN_ANT = 1000;
	
	public static final String DIALOGSTORE_LASTEXTJAR= PLUGIN_ID + ".lastextjar"; //$NON-NLS-1$
	public static final String DIALOGSTORE_LASTEXTFILE= PLUGIN_ID + ".lastextfile"; //$NON-NLS-1$
	public static final String DIALOGSTORE_LASTFOLDER= PLUGIN_ID + ".lastfolder"; //$NON-NLS-1$
	public static final String DIALOGSTORE_LASTANTHOME= PLUGIN_ID + ".lastanthome"; //$NON-NLS-1$
	
	/**
	 * Size of left-hand column for right-justified task name.
	 * Used for Ant Build logging.
	 * @see org.eclipse.ant.ui,internal.antsupport.logger.AntProcessBuildLogger#logMessage(String, BuildEvent, -1)
	  */
	public static final int LEFT_COLUMN_SIZE = 15;			
}
