/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.examples.readmetool;

import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * This is the top-level class of the Readme plugin tool.
 *
 * @see AbstractUIPlugin for additional information on UI plugins
 */
public class ReadmePlugin extends AbstractUIPlugin {
    // Default instance of the receiver

    private static ReadmePlugin inst;

    /**
     * Creates the Readme plugin and caches its default instance
     *
     * @param descriptor  the plugin descriptor which the receiver is made from
     */
    public ReadmePlugin(IPluginDescriptor descriptor) {
        super(descriptor);
        if (inst == null)
            inst = this;
    }

    /**
     * Gets the plugin singleton.
     *
     * @return the default ReadmePlugin instance
     */
    static public ReadmePlugin getDefault() {
        return inst;
    }

    /** 
     * Sets default preference values. These values will be used
     * until some preferences are actually set using Preference dialog.
     */
    protected void initializeDefaultPreferences(IPreferenceStore store) {
        // These settings will show up when Preference dialog
        // opens up for the first time.
        store.setDefault(IReadmeConstants.PRE_CHECK1, true);
        store.setDefault(IReadmeConstants.PRE_CHECK2, true);
        store.setDefault(IReadmeConstants.PRE_CHECK3, false);
        store.setDefault(IReadmeConstants.PRE_RADIO_CHOICE, 2);
        store.setDefault(IReadmeConstants.PRE_TEXT, MessageUtil
                .getString("Default_text")); //$NON-NLS-1$
    }
}