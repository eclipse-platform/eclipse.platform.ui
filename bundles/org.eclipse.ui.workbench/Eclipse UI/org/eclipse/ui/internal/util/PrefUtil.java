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
package org.eclipse.ui.internal.util;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.internal.WorkbenchPlugin;

/**
 * Internal utility class to help with getting/setting preferences.
 * <p>
 * API preferences are defined in {@link org.eclipse.ui.IWorkbenchPreferenceConstants}
 * and are obtained from the <code>org.eclipse.ui</code> plug-in's preference store.
 * </p>
 * <p>
 * Internal preferences are defined in {@link org.eclipse.ui.internal.IPreferenceConstants}
 * and are obtained from the <code>org.eclipse.ui.workbench</code> plug-in's preference store.
 * </p>
 * 
 * @since 3.0
 */
public class PrefUtil {

    private PrefUtil() {
        // prevents instantiation
    }

    /**
     * Callback interface to obtain and save the UI preference store.
     */
    public static interface ICallback {
        IPreferenceStore getPreferenceStore();

        void savePreferences();
    }

    private static ICallback uiCallback;

    private static IPreferenceStore uiPreferenceStore;

    /**
     * Sets the callback used to obtain and save the UI preference store.
     */
    public static final void setUICallback(ICallback callback) {
        Assert.isTrue(uiCallback == null);
        uiCallback = callback;
    }

    /**
     * Returns the API preference store.
     * 
     * @return the API preference store
     */
    public static IPreferenceStore getAPIPreferenceStore() {
        if (uiPreferenceStore == null) {
            Assert.isNotNull(uiCallback);
            uiPreferenceStore = uiCallback.getPreferenceStore();
        }
        return uiPreferenceStore;
    }

    /**
     * Returns the internal preference store.
     * 
     * @return the internal preference store
     */
    public static IPreferenceStore getInternalPreferenceStore() {
        return WorkbenchPlugin.getDefault().getPreferenceStore();
    }

    /**
     * Saves both the API and internal preference stores.
     */
    public static void savePrefs() {
        saveAPIPrefs();
        saveInternalPrefs();
    }

    /**
     * Saves the API preference store, if needed.
     */
    public static void saveAPIPrefs() {
        Assert.isNotNull(uiCallback);
        uiCallback.savePreferences();
    }

    /**
     * Saves the internal preference store, if needed.
     */
    public static void saveInternalPrefs() {
        WorkbenchPlugin.getDefault().savePluginPreferences();
    }
}
