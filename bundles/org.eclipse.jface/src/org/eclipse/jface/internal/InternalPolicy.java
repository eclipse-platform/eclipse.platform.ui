/*******************************************************************************
 * Copyright (c) 2007, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.jface.internal;

import org.eclipse.jface.util.BidiUtils;
import org.osgi.framework.FrameworkUtil;

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
	 * (NON-API) A flag to indicate whether {@link BidiUtils} should colorize
	 * processed text fields and add tooltips/messages that show the configured
	 * handlingType.
	 *
	 * @since 3.11
	 */
	public static boolean DEBUG_BIDI_UTILS = false;

	/**
	 * (NON-API) Trace time spent creating URLImageDescriptor images.
	 *
	 * @since 3.11
	 */
	public static boolean DEBUG_TRACE_URL_IMAGE_DESCRIPTOR = false;

	/**
	 * (NON-API) Log cases where an "@2x" image could not be found.
	 *
	 * @since 3.11
	 */
	public static boolean DEBUG_LOG_URL_IMAGE_DESCRIPTOR_MISSING_2x = false;

	/**
	 * (NON-API) If true, URLImageDescriptor loads images directly via
	 * URL#openStream(). If false, URLImageDescriptor first tries to use
	 * FileLocator#toFileURL(URL) and the Image(Device, String) constructor.
	 *
	 * @since 3.11
	 */
	public static boolean DEBUG_LOAD_URL_IMAGE_DESCRIPTOR_DIRECTLY = false;

	/**
	 * (NON-API) Enable high-dpi images via "@2x" filename convention.
	 *
	 * @since 3.11
	 */
	public static boolean DEBUG_LOAD_URL_IMAGE_DESCRIPTOR_2x = true;

	/**
	 * (NON-API) Always load the .png image of the "@2x" version, even if the
	 * original image was a .gif.
	 *
	 * @since 3.11
	 */
	public static boolean DEBUG_LOAD_URL_IMAGE_DESCRIPTOR_2x_PNG_FOR_GIF = false;

	/**
	 * (NON-API) A flag to indicate whether the JFace bundle is running inside
	 * an OSGi container
	 *
	 * @since 3.5
	 */
	public static boolean OSGI_AVAILABLE; // default value is false

	static {
		try {
			OSGI_AVAILABLE = FrameworkUtil.getBundle(InternalPolicy.class) != null;
		} catch (Throwable t) {
			OSGI_AVAILABLE = false;
		}
	}

}
