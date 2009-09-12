/*******************************************************************************
 * Copyright (c) 2003, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.navigator;

import org.eclipse.core.runtime.Platform;

/**
 * Policy is the class for the debug arguments in the navigator
 * 
 */
public class Policy {

	/**
	 * The default value
	 */
	public static final boolean DEFAULT = false;

	/**
	 * Option for tracing the reading and setup of the extensions
	 */
	public static boolean DEBUG_EXTENSION_SETUP = DEFAULT;

	/**
	 * Option for tracing extension resolution
	 */
	public static boolean DEBUG_RESOLUTION = DEFAULT;

	/**
	 * Option for tracing sort
	 */
	public static boolean DEBUG_SORT = DEFAULT;

	/**
	 * Option for tracing drag and drop
	 */
	public static boolean DEBUG_DND = DEFAULT;

	/**
	 * Option for tracing viewer/content descriptor association map
	 */
	public static boolean DEBUG_VIEWER_MAP = DEFAULT;

	static {
		if (getDebugOption("/debug")) { //$NON-NLS-1$
			DEBUG_DND = getDebugOption("/debug/dnd"); //$NON-NLS-1$
			DEBUG_RESOLUTION = getDebugOption("/debug/resolution"); //$NON-NLS-1$
			DEBUG_EXTENSION_SETUP = getDebugOption("/debug/setup"); //$NON-NLS-1$
			DEBUG_SORT = getDebugOption("/debug/sort"); //$NON-NLS-1$
			DEBUG_VIEWER_MAP = getDebugOption("/debug/viewermap"); //$NON-NLS-1$
		}
	}

	private static boolean getDebugOption(String option) {
		return "true".equalsIgnoreCase(Platform.getDebugOption(NavigatorPlugin.PLUGIN_ID + option)); //$NON-NLS-1$
	}
	
	/**
	 * @param obj
	 * @return a String
	 */
	public static String getObjectString(Object obj) {
		if (obj == null)
			return "(null)"; //$NON-NLS-1$
		String elemStr = obj.toString();
		if (elemStr.length() > 30)
			elemStr = elemStr.substring(0, 29);
		return "(" + obj.getClass().getName() + "): " + elemStr;  //$NON-NLS-1$//$NON-NLS-2$
	}

}
