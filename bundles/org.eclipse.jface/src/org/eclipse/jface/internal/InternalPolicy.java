/*******************************************************************************
 * Copyright (c) 2007, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.jface.internal;


/**
 * Internal class used for non-API debug flags.
 * 
 * @since 3.3
 */
public class InternalPolicy {

	/**
	 * (NON-API) A flag to indicate whether reentrant viewer calls should always be
	 * logged. If false, only the first reentrant call will cause a log entry.
	 * 
	 * @since 3.3
	 */
	public static boolean DEBUG_LOG_REENTRANT_VIEWER_CALLS = false;
	
	/**
	 * (NON-API) A flag to indicate whether illegal equal elements in a viewer should be logged.
	 * 
	 * @since 3.7
	 */
	public static boolean DEBUG_LOG_EQUAL_VIEWER_ELEMENTS= false;

	/**
	 * (NON-API) A flag to indicate whether label provider changed notifications
	 * should always be logged when the underlying control has been disposed. If
	 * false, only the first notification when disposed will cause a log entry.
	 * 
	 * @since 3.5
	 */
	public static boolean DEBUG_LOG_LABEL_PROVIDER_NOTIFICATIONS_WHEN_DISPOSED = false;
	
	/**
	 * (NON-API) A flag to indicate whether the JFace bundle is running inside an OSGi
	 * container
	 * 
	 * @since 3.5
	 */
	public static boolean OSGI_AVAILABLE; // default value is false
	
}
