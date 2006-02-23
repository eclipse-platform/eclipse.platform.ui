/*******************************************************************************
 * Copyright (c) 2005 Intel Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Intel Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.internal.index;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.StringTokenizer;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.help.IIndex;
import org.eclipse.help.internal.HelpPlugin;


/**
 * @author sturmash
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class IndexManager {
	public static final String INDEX_XP_NAME = "index"; //$NON-NLS-1$

    private Collection contributingPlugins;
    private Map indexesByLang;

    /**
     * IndexManager constructor
     */
    public IndexManager() {
        indexesByLang = new HashMap();
    }
    
    /**
     * Builds the index from all contributed index files
     * @param locale
     */
    private void build(String locale) {
        Collection contributedIndexFiles = getContributedIndexFiles(locale);
        IndexBuilder builder = new IndexBuilder();
        builder.build(contributedIndexFiles);
        IIndex index = builder.getBuiltIndex();
        indexesByLang.put(locale, index);
    }
    
    /**
     * Returns all index files contributed by separate help plugins
     * @param locale
     * @return
     */
    private Collection getContributedIndexFiles(String locale) {
        contributingPlugins = new HashSet();
        Collection contributedIndexFiles = new ArrayList();
        Collection ignored = getIgnoredIndexes();

        IExtensionPoint xpt = Platform.getExtensionRegistry().getExtensionPoint(HelpPlugin.PLUGIN_ID, INDEX_XP_NAME);
        if (xpt == null)
             return contributedIndexFiles;
        
        IExtension[] extensions = xpt.getExtensions();
        for (int i = 0; i < extensions.length; i++) {
            contributingPlugins.add(extensions[i].getContributor().getName());
            IConfigurationElement[] configElements = extensions[i].getConfigurationElements();
            for (int j = 0; j < configElements.length; j++) {
                if (configElements[j].getName().equals("index")) { //$NON-NLS-1$
                    String pluginId = configElements[j].getDeclaringExtension().getContributor().getName();
                    String href = configElements[j].getAttribute("file"); //$NON-NLS-1$
                    if (href == null
                    		|| ignored.contains("/" + pluginId + "/" + href)) { //$NON-NLS-1$ //$NON-NLS-2$)
                    	continue;
                    }
                    contributedIndexFiles.add(new IndexFile(pluginId, href, locale));
                }
            }
        }
        return contributedIndexFiles;
    }
    
    /**
     * @return Returns the contributingPlugins.
     */
    public Collection getContributingPlugins() {
        return contributingPlugins;
    }
    
    public Index getIndex(String locale) {
        if (locale == null)
            return new Index();

        Index index = (Index) indexesByLang.get(locale);
        if (index == null) {
            synchronized(this) {
                if (index == null) {
                    build(locale);
                }
            }
            index = (Index) indexesByLang.get(locale);
            if (index == null)
                index = new Index();
        }

        return index;
            
    }

    private Collection getIgnoredIndexes() {
    	HashSet ignored = new HashSet();
		try {
			Preferences pref = HelpPlugin.getDefault().getPluginPreferences();
			String ignoredIndexes = pref.getString(HelpPlugin.IGNORED_INDEXES_KEY);
			if (ignoredIndexes != null) {
				StringTokenizer tokens = new StringTokenizer(
						ignoredIndexes, " ;,"); //$NON-NLS-1$

				while (tokens.hasMoreTokens()) {
					ignored.add(tokens.nextToken());
				}
			}
		} catch (Exception e) {
			HelpPlugin.logError(
					"Problems occurred reading plug-in preferences.", e); //$NON-NLS-1$
		}
    	return ignored;
    }
}
