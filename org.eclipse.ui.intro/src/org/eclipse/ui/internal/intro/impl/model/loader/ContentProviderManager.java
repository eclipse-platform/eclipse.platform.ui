/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.intro.impl.model.loader;

import java.util.*;

import org.eclipse.ui.internal.intro.impl.model.*;
import org.eclipse.ui.internal.intro.impl.util.*;
import org.eclipse.ui.intro.config.*;

/**
 * Class for handling/caching all the loaded Intro Content providers, from all
 * loaded models. <br>
 * Content provider ids are used as keys in the hashtable, and their
 * corresponding classes as values.
 */

public class ContentProviderManager {

    // singleton instance. Can be retrieved from here or from the Intro Plugin.
    private static ContentProviderManager inst = new ContentProviderManager();


    // Holds all created content providers, to prevent the need to recreate the
    // class on each navigation. Key is the contentProvider model class, value
    // is the actual Intro content provider instance.
    private Hashtable contentProviders = new Hashtable();


    /*
     * Prevent creation.
     */
    protected ContentProviderManager() {
    }

    /**
     * @return Returns the inst.
     */
    public static ContentProviderManager getInst() {
        return inst;
    }

    /**
     * Retrieve an existing content provider class, or null if never created
     * before.
     * 
     * @param provider
     * @return
     */
    public IIntroContentProvider getContentProvider(
            IntroContentProvider provider) {
        // safe to cast since we know the object class in table.
        IIntroContentProvider providerClass = (IIntroContentProvider) contentProviders
            .get(provider);
        return providerClass;
    }

    /**
     * Tries to create an intro content provider class. may return null if
     * creation fails. This will be logged.
     * 
     * @param provider
     * @param site
     * @return
     */
    public IIntroContentProvider createContentProvider(
            IntroContentProvider provider, IIntroContentProviderSite site) {

        // the content provider has never been created before. Create and cache
        // one.
        String pluginId = (provider.getPluginId() != null) ? provider
            .getPluginId() : provider.getBundle().getSymbolicName();
        Object aClass = (IIntroContentProvider) ModelLoaderUtil
            .createClassInstance(pluginId, provider.getClassName());
        IIntroContentProvider providerClass = null;
        if (aClass != null && aClass instanceof IIntroContentProvider) {
            providerClass = ((IIntroContentProvider) aClass);
            providerClass.init(site);
            if (provider.getId() != null) {
                // cache only when an id is defined.
                contentProviders.put(provider, providerClass);
            }
        } else
            Log.warning("Failed to create Intro model content provider: " //$NON-NLS-1$
                    + provider.getClassName());
        return providerClass;
    }


    public String getContentProviderPageId(IIntroContentProvider provider) {
        Enumeration keys = contentProviders.keys();
        while (keys.hasMoreElements()) {
            IntroContentProvider key = (IntroContentProvider) keys
                .nextElement();
            boolean foundKey = contentProviders.get(key).equals(provider) ? true
                    : false;
            if (foundKey) {
                return key.getParentPage().getId();
            }
        }
        return null;
    }

    public void clear() {
        for (Iterator it = contentProviders.values().iterator(); it.hasNext();) {
            Object provider = it.next();
            if (provider instanceof IIntroContentProvider) {
                ((IIntroContentProvider) provider).dispose();
            }
        }
        contentProviders.clear();
        Log.info("Cleared Intro model content providers"); //$NON-NLS-1$
    }


}


