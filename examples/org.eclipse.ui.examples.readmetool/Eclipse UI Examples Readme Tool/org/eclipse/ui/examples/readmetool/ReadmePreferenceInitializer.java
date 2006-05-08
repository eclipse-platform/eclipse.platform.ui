/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.examples.readmetool;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

/**
 * Initializes the preferences for the readme plug-in.
 * 
 * @since 3.0
 */
public class ReadmePreferenceInitializer extends AbstractPreferenceInitializer {

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
     */
    public void initializeDefaultPreferences() {
        // These settings will show up when the Readme preference page
        // is shown for the first time.
        IPreferenceStore store = ReadmePlugin.getDefault().getPreferenceStore();
        store.setDefault(IReadmeConstants.PRE_CHECK1, true);
        store.setDefault(IReadmeConstants.PRE_CHECK2, true);
        store.setDefault(IReadmeConstants.PRE_CHECK3, false);
        store.setDefault(IReadmeConstants.PRE_RADIO_CHOICE, 2);
        store.setDefault(IReadmeConstants.PRE_TEXT, MessageUtil
                .getString("Default_text")); //$NON-NLS-1$
    }

}
