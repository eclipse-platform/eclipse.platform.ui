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

import org.eclipse.ui.externaltools.internal.model.IExternalToolConstants;

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
	 * Ant View identifier (value <code>org.eclipse.ui.externaltools.AntView</code>).
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
	
	//	Label images
	 public static final String IMG_ANT_PROJECT= IExternalToolConstants.PLUGIN_ID + ".antProject"; //$NON-NLS-1$
	 public static final String IMG_ANT_TARGET= IExternalToolConstants.PLUGIN_ID + ".antTarget"; //$NON-NLS-1$
	 public static final String IMG_ANT_TARGET_PRIVATE = IExternalToolConstants.PLUGIN_ID + ".antPrivateTarget"; //$NON-NLS-1$
	 public static final String IMG_ANT_DEFAULT_TARGET= IExternalToolConstants.PLUGIN_ID + ".antDefaultTarget"; //$NON-NLS-1$
	 public static final String IMG_ANT_TARGET_ERROR = IExternalToolConstants.PLUGIN_ID + ".antTargetError"; //$NON-NLS-1$
	 

	// ------- Launch configuration types --------
	/**
	 * Ant launch configuration type identifier.
	 */
	public static final String ID_ANT_LAUNCH_CONFIGURATION_TYPE = "org.eclipse.ant.AntLaunchConfigurationType"; //$NON-NLS-1$
	
	/**
	 * Ant builder launch configuration type identifier. Ant project builders
	 * are of this type.
	 */
	public static final String ID_ANT_BUILDER_LAUNCH_CONFIGURATION_TYPE = "org.eclipse.ant.AntBuilderLaunchConfigurationType"; //$NON-NLS-1$

	// ------- Common Ant Launch Configuration Attributes -------
	/**
	 * String attribute indicating the Ant targets to execute. Default value is
	 * <code>null</code> which indicates that the default target is to be
	 * executed. Format is a comma separated listing of targets.
	 */
	public static final String ATTR_ANT_TARGETS = IExternalToolConstants.PLUGIN_ID + ".ATTR_ANT_TARGETS"; //$NON-NLS-1$

	/**
	 * Map attribute indicating the Ant properties to be defined during the
	 * build. Default value is <code>null</code> which indicates no additional
	 * properties will be defined.
	 */
	public static final String ATTR_ANT_PROPERTIES = IExternalToolConstants.PLUGIN_ID + ".ATTR_ANT_PROPERTIES"; //$NON-NLS-1$					

	/**
	 * String attribute indicating the Ant targets to execute. Default value is
	 * <code>null</code> which indicates that no additional property files
	 * will be defined. Format is a comma separated listing of property files.
	 */
	public static final String ATTR_ANT_PROPERTY_FILES = IExternalToolConstants.PLUGIN_ID + ".ATTR_ANT_PROPERTY_FILES"; //$NON-NLS-1$
	
	/**
 	* String attribute indicating the custom runtime classpath to use for an Ant
 	* build. Default value is <code>null</code> which indicates that the global
 	* classpath is to be used. Format is a comma separated listing of URLs.
  	*/
	public static final String ATTR_ANT_CUSTOM_CLASSPATH = IExternalToolConstants.PLUGIN_ID + ".ATTR_ANT_CUSTOM_CLASSPATH"; //$NON-NLS-1$
	
	/**
	 * String attribute indicating the custom Ant home to use for an Ant build.
	 * Default value is <code>null</code> which indicates that no Ant home is to
	 * be set 
	 */
	public static final String ATTR_ANT_HOME = IExternalToolConstants.PLUGIN_ID + ".ATTR_ANT_HOME"; //$NON-NLS-1$
	
	/**
	 * Identifier for Ant processes (value <code>org.eclipse.ant.ui.antProcess</code>). This identifier is
	 * set as the value for the <code>IProcess.ATTR_PROCESS_TYPE</code>
	 * attribute in processes create by the Ant launch delegate.
	 */
	public static final String ID_ANT_PROCESS_TYPE = "org.eclipse.ant.ui.antProcess"; //$NON-NLS-1$	
	
	// -------- Status Codes -------------
	
	/**
	 * Status code used by the 'Run Ant' status handler which is invoked when
	 * the launch dialog is opened by the 'Run Ant' action.
	 */
	public static final int STATUS_INIT_RUN_ANT = 1000;								
}
