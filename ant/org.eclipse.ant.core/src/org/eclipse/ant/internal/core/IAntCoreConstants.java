package org.eclipse.ant.internal.core;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/

/**
 * Common constants.
 */
public interface IAntCoreConstants {

	// default values
	public static final String DEFAULT_BUILD_FILENAME = "build.xml"; //$NON-NLS-1$

	// preferences
	public static final String PREFERENCE_TASKS = "tasks"; //$NON-NLS-1$
	public static final String PREFERENCE_TYPES = "types"; //$NON-NLS-1$
	public static final String PREFERENCE_URLS = "urls"; //$NON-NLS-1$
	
	public static final String PREFIX_TASK = "task."; //$NON-NLS-1$
	public static final String PREFIX_TYPE = "type."; //$NON-NLS-1$
	
	/**
	 * Debug constant
	 * @since 2.1
	 */
	public static boolean DEBUG_BUILDFILE_TIMING = false;
	
	/**
	 * Preferences
	 * @since 2.1
	 */
	public static final String PREFERENCE_PROPERTIES = "properties"; //$NON-NLS-1$
	public static final String PREFERENCE_PROPERTY_FILES = "propertyfiles"; //$NON-NLS-1$
	public static final String PREFIX_PROPERTY = "property."; //$NON-NLS-1$
}