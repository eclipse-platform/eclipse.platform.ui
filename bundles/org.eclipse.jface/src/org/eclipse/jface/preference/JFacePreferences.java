/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.preference;

/**
 * 
 * JFacePreferences is a class used to administer the preferences
 * used by JFace objects.
 */
public final class JFacePreferences {

    /**
     * Identifier for the Error Color
     */
    public static final String ERROR_COLOR = "ERROR_COLOR"; //$NON-NLS-1$

    /**
     * Identifier for the Hyperlink Color
     */
    public static final String HYPERLINK_COLOR = "HYPERLINK_COLOR"; //$NON-NLS-1$

    /**
     * Identifier for the Active Hyperlink Colour
     */
    public static final String ACTIVE_HYPERLINK_COLOR = "ACTIVE_HYPERLINK_COLOR"; //$NON-NLS-1$

    private static IPreferenceStore preferenceStore;

    /**
     * Prevent construction.
     */
    private JFacePreferences() {
    }

    /**
     * Return the preference store for the receiver.
     * @return IPreferenceStore or null
     */
    public static IPreferenceStore getPreferenceStore() {
        return preferenceStore;
    }

    /**
     * Set the preference store for the receiver.
     * @param store IPreferenceStore
     */
    public static void setPreferenceStore(IPreferenceStore store) {
        preferenceStore = store;
    }

}
