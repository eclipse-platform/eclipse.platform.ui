/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ant.internal.core;


/**
 * Common constants.
 */
public interface IAntCoreConstants {

	// default values
	public static final String DEFAULT_BUILD_FILENAME = "build.xml"; //$NON-NLS-1$

	// preferences
	public static final String PREFERENCE_TASKS = "tasks"; //$NON-NLS-1$
	public static final String PREFERENCE_TYPES = "types"; //$NON-NLS-1$
	
	public static final String PREFIX_TASK = "task."; //$NON-NLS-1$
	public static final String PREFIX_TYPE = "type."; //$NON-NLS-1$
	
	/**
	 * Preferences
	 * @since 3.0
	 */
	public static final String PREFERENCE_ANT_HOME_ENTRIES = "ant_home_entries"; //$NON-NLS-1$
	public static final String PREFERENCE_ADDITIONAL_ENTRIES = "additional_entries"; //$NON-NLS-1$
	
	public static final String PREFERENCE_CLASSPATH_CHANGED = "classpath_changed"; //$NON-NLS-1$
	
	/**
	 * Preferences
	 * @since 2.1
	 */
	public static final String PREFERENCE_ANT_HOME = "ant_home"; //$NON-NLS-1$
	public static final String PREFERENCE_PROPERTIES = "properties"; //$NON-NLS-1$
	public static final String PREFERENCE_PROPERTY_FILES = "propertyfiles"; //$NON-NLS-1$
	public static final String PREFIX_PROPERTY = "property."; //$NON-NLS-1$
}
