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
package org.eclipse.ui.intro.internal;

import java.text.*;
import java.util.*;

import org.eclipse.core.runtime.*;
import org.eclipse.ui.intro.internal.extensions.*;
import org.eclipse.ui.intro.internal.model.*;
import org.eclipse.ui.intro.internal.util.*;
import org.eclipse.ui.plugin.*;

/**
 * Intro main plugin.
 */
public class IntroPlugin extends AbstractUIPlugin {

    // The static shared instance.
    private static IntroPlugin inst;

    // There should always be a single instance of all these classes.
    private ResourceBundle resourceBundle;

    /**
     * The constructor.
     */
    public IntroPlugin(IPluginDescriptor descriptor) {
        super(descriptor);
        inst = this;
        try {
            resourceBundle = ResourceBundle
                    .getBundle("org.eclipse.ui.intro.internal.IntroPluginResources"); //$NON-NLS-1$
        } catch (MissingResourceException x) {
            resourceBundle = null;
            Logger.logWarning("IntroPlugin - unable to load resource bundle"); //$NON-NLS-1$
        }

    }

    /**
     * Returns the shared plugin instance.
     */
    public static IntroPlugin getDefault() {
        return inst;
    }

    /**
     * Returns the string from the plugin's resource bundle, or 'key' if not
     * found.
     */
    public static String getResourceString(String key) {
        try {
            ResourceBundle bundle = IntroPlugin.getDefault()
                    .getResourceBundle();
            return (bundle != null ? bundle.getString(key) : key);
        } catch (MissingResourceException e) {
            Logger.logWarning("could not find resource string: " + key); //$NON-NLS-1$
            // ok to return Key.
            return key;
        }
    }

    /**
     * Utility method to get a resource from the given key, then format it with
     * the given substitutions. <br>
     */
    public static String getFormattedResourceString(String key, Object[] args) {
        return MessageFormat.format(key, args);
    }

    /**
     * Returns the plugin's resource bundle.
     */
    public ResourceBundle getResourceBundle() {
        return resourceBundle;
    }

    /**
     * @return Returns the extensionPointManager.
     */
    public ExtensionPointManager getExtensionPointManager() {
        return ExtensionPointManager.getInst();
    }

    /**
     * Returns the model root. Will always guarantee that model is loaded.
     * 
     * @return Returns the introModelRoot.
     */
    public IntroModelRoot getIntroModelRoot() {
        return getExtensionPointManager().getCurrentModel();
    }

}