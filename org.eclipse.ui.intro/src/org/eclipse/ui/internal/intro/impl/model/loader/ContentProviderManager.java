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
    // class on each navigation. Key is the contentProvider id, value is the
    // actual Intro content provider instance. We use id as key because in both
    // the swt and XHTML modes a page is refreshed (reflowed) by clearing all
    // children and recreating them. So we do not want to recreate the content
    // provider class too.
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
        if (provider.getId() == null) {
            // no id defined. return null to create a new instance each time.
            // bad, so log fact.
            Log
                .info("Intro content provider from class: " //$NON-NLS-1$
                        + provider.getClassName()
                        + " does not have an id defined. An id is required for a content provider."); //$NON-NLS-1$
            return null;
        }
        // safe to cast since we know the object class in table.
        IIntroContentProvider providerClass = (IIntroContentProvider) contentProviders
            .get(provider.getId());
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
            if (provider.getId() != null)
                // cache only when an id is defined.
                contentProviders.put(provider.getId(), providerClass);
        } else
            Log.warning("Failed to create Intro model content provider: " //$NON-NLS-1$
                    + provider.getClassName());
        return providerClass;
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


