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

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.help.internal.HelpPlugin;


/**
 * @author sturmash
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class IndexManager {
    
    private Collection contributingPlugins;
    private Map indexesByLang;

    /**
     * IndexManager constructor
     */
    public IndexManager() {
        indexesByLang = new HashMap();
        build(Platform.getNL());
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
        indexesByLang.put(Platform.getNL(), index);
    }
    
    /**
     * Returns all index files contributed by separate help plugins
     * @param locale
     * @return
     */
    private Collection getContributedIndexFiles(String locale) {
        contributingPlugins = new HashSet();
        Collection contributedIndexFiles = new ArrayList();
        
        IExtensionPoint xpt = Platform.getExtensionRegistry().getExtensionPoint(HelpPlugin.PLUGIN_ID, "index"); //$NON-NLS-1$
        if (xpt == null)
             return contributedIndexFiles;
        
        IExtension[] extensions = xpt.getExtensions();
        for (int i = 0; i < extensions.length; i++) {
            contributingPlugins.add(extensions[i].getNamespace());
            IConfigurationElement[] configElements = extensions[i].getConfigurationElements();
            for (int j = 0; j < configElements.length; j++) {
                if (configElements[j].getName().equals("index")) { //$NON-NLS-1$
                    String pluginId = configElements[j].getDeclaringExtension().getNamespace();
                    String href = configElements[j].getAttribute("file"); //$NON-NLS-1$
                    if (href != null) {
                        contributedIndexFiles.add(new IndexFile(pluginId, href, Platform.getNL()));
                    }
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
    
    public IIndex getIndex(String locale) {
        if (locale == null)
            return new Index();
        
        IIndex index = (IIndex) indexesByLang.get(locale);
        if (index == null) {
            synchronized(this) {
                if (index == null) {
                    build(locale);
                }
            }
            index = (IIndex) indexesByLang.get(locale);
            if (index == null)
                index = new Index();
        }
        
        return index;
            
    }
    
}
