/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal;

/**
 * Preference constants for the heap status.
 */
public interface IHeapStatusConstants {

	/**
	 * Preference key for the update interval (value in milliseconds).
	 */
    String PREF_UPDATE_INTERVAL = "updateInterval"; //$NON-NLS-1$

    /**
     * ID for the Kyrsoft Memory Monitor plug-in.
     */
    String KYRSOFT_PLUGIN_ID = "de.kyrsoft.memmonitor"; //$NON-NLS-1$

    /**
     * ID for the Kyrsoft Memory Monitor view.
     */
    String KYRSOFT_VIEW_ID = "de.kyrsoft.memmonitor.views.MemoryView"; //$NON-NLS-1$

}
