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
package org.eclipse.ui.externaltools.internal.ui;


import org.eclipse.ui.externaltools.internal.model.IExternalToolConstants;


public interface IExternalToolsUIConstants {

	// Ant Editor images
	/**
	 * Icon for property proposal.
	 */
	public static final String IMAGE_ID_PROPERTY = IExternalToolConstants.PLUGIN_ID + ".property"; //$NON-NLS-1$
	/**
	 * Icon for task proposal.
	 */
	public static final String IMAGE_ID_TASK = IExternalToolConstants.PLUGIN_ID + ".html_tab_obj"; //$NON-NLS-1$
	/**
	 * Icon for target in the outline view.
	 */
	public static final String IMAGE_ID_TARGET = IExternalToolConstants.PLUGIN_ID + ".ant_tsk_arrow"; //$NON-NLS-1$
	// Action images
	public static final String IMG_REMOVE= IExternalToolConstants.PLUGIN_ID + ".remove"; //$NON-NLS-1$
	public static final String IMG_MOVE_UP = IExternalToolConstants.PLUGIN_ID + ".moveUp"; //$NON-NLS-1$
	public static final String IMG_MOVE_DOWN = IExternalToolConstants.PLUGIN_ID + ".moveDown"; //$NON-NLS-1$
	public static final String IMG_ACTIVATE = IExternalToolConstants.PLUGIN_ID + ".activate"; //$NON-NLS-1$
	public static final String IMG_DEACTIVATE = IExternalToolConstants.PLUGIN_ID + ".deactivate"; //$NON-NLS-1$
	public static final String IMG_REMOVE_ALL= IExternalToolConstants.PLUGIN_ID + ".removeAll"; //$NON-NLS-1$
	public static final String IMG_ADD= IExternalToolConstants.PLUGIN_ID + ".add"; //$NON-NLS-1$
	public static final String IMG_RUN= IExternalToolConstants.PLUGIN_ID + ".run"; //$NON-NLS-1$
	public static final String IMG_SEARCH= IExternalToolConstants.PLUGIN_ID + ".search"; //$NON-NLS-1$
	public static final String IMG_GO_TO_FILE= IExternalToolConstants.PLUGIN_ID + ".goToFile"; //$NON-NLS-1$
	public static final String IMG_TOGGLE= IExternalToolConstants.PLUGIN_ID + ".toggle"; //$NON-NLS-1$
	
	// Label images
	public static final String IMG_ANT_PROJECT= IExternalToolConstants.PLUGIN_ID + ".antProject"; //$NON-NLS-1$
	public static final String IMG_ANT_PROJECT_ERROR = IExternalToolConstants.PLUGIN_ID + ".antProjectError"; //$NON-NLS-1$
	public static final String IMG_ANT_TARGET= IExternalToolConstants.PLUGIN_ID + ".antTarget"; //$NON-NLS-1$
	public static final String IMG_ANT_TARGET_PRIVATE = IExternalToolConstants.PLUGIN_ID + ".antPrivateTarget"; //$NON-NLS-1$
	public static final String IMG_ANT_DEFAULT_TARGET= IExternalToolConstants.PLUGIN_ID + ".antDefaultTarget"; //$NON-NLS-1$
	public static final String IMG_ANT_TARGET_ERROR = IExternalToolConstants.PLUGIN_ID + ".antTargetError"; //$NON-NLS-1$
	public static final String IMG_ANT_TARGET_ELEMENTS= IExternalToolConstants.PLUGIN_ID + ".antTargetElements"; //$NON-NLS-1$
	public static final String IMG_ANT_TARGET_ELEMENT= IExternalToolConstants.PLUGIN_ID + ".antTargetElement"; //$NON-NLS-1$
	
	// Overlays
	public static final String IMG_OVR_DEFAULT = IExternalToolConstants.PLUGIN_ID + ".ovrDefault";  //$NON-NLS-1$
	public static final String IMG_OVR_ERROR = IExternalToolConstants.PLUGIN_ID + ".ovrError";  //$NON-NLS-1$
	

	public static final String DIALOGSTORE_LASTEXTJAR= IExternalToolConstants.PLUGIN_ID + ".lastextjar"; //$NON-NLS-1$
	public static final String DIALOGSTORE_LASTEXTFILE= IExternalToolConstants.PLUGIN_ID + ".lastextfile"; //$NON-NLS-1$
	public static final String DIALOGSTORE_LASTFOLDER= IExternalToolConstants.PLUGIN_ID + ".lastfolder"; //$NON-NLS-1$
	public static final String DIALOGSTORE_LASTANTHOME= IExternalToolConstants.PLUGIN_ID + ".lastanthome"; //$NON-NLS-1$
}
