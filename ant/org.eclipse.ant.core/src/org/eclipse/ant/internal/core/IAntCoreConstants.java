/**********************************************************************
 * Copyright (c) 2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.ant.internal.core;

/**
 * Common constants.
 */
public interface IAntCoreConstants {

	// default values
	public static final String PI_ANTCORE = "org.eclipse.ant.core"; //$NON-NLS-1$
	public static final String DEFAULT_BUILD_FILENAME = "build.xml"; //$NON-NLS-1$
	
	// error codes
	public static final int ERROR_RUNNING_SCRIPT = 1;
	public static final int ERROR_MALFORMED_URL = 2;
	public static final int ERROR_LIBRARY_NOT_SPECIFIED = 3;

	// preferences
	public static final String PREFERENCE_TASKS = "tasks"; //$NON-NLS-1$
	public static final String PREFERENCE_TYPES = "types"; //$NON-NLS-1$
	public static final String PREFERENCE_URLS = "urls"; //$NON-NLS-1$
	public static final String PREFIX_TASK = "task."; //$NON-NLS-1$
	public static final String PREFIX_TYPE = "type."; //$NON-NLS-1$
	public static final String PREFIX_URL = "url."; //$NON-NLS-1$
}