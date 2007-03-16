/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.examples.rcp.browser;

/**
 * Interface defining constants for use in the browser example.
 */
public interface IBrowserConstants {

    /**
     * ID of the RCP Browser Example plug-in.
     */
    public static final String PLUGIN_ID = "org.eclipse.ui.examples.rcp.browser"; //$NON-NLS-1$
    
    /**
     * ID of the Browser perspective.
     */
    public static final String BROWSER_PERSPECTIVE_ID = PLUGIN_ID + ".browserPerspective"; //$NON-NLS-1$
    
    /**
     * ID of the Browser view.
     */
    public static final String BROWSER_VIEW_ID = PLUGIN_ID + ".browserView"; //$NON-NLS-1$

    /**
     * ID of the History view.
     */
    public static final String HISTORY_VIEW_ID = PLUGIN_ID + ".historyView"; //$NON-NLS-1$

    /**
     * Common prefix for command ids.
     */
    public static final String COMMAND_PREFIX = PLUGIN_ID + ".commands."; //$NON-NLS-1$
    
    /**
     * Preference key for the home page URL (property is String-valued).
     */
    public static final String PREF_HOME_PAGE = "homePage"; //$NON-NLS-1$

    /**
     * Memento attribute name for the browser URL (attribute is String-valued).
     */
    public static final String MEMENTO_URL = "url"; //$NON-NLS-1$
    
}
