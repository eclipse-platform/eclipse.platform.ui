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
package org.eclipse.ui.internal.intro.impl.html;

import java.util.*;


/**
 * Cache for all HTML pages displayed so far. The design is that every HTML page
 * that is displayed is cached here as a string. This prevents content providers
 * from being called each time a page is displayed. The HTML generator or the
 * XSLT transform do the job only once per page. After that, pages are recreated
 * on a per need babsis.
 */
public class HTMLCache {

    // singleton instance. Can be retrieved from here or from the Intro Plugin.
    private static HTMLCache inst = new HTMLCache();

    private Hashtable cache = new Hashtable();

    /*
     * Prevent creation.
     */
    protected HTMLCache() {
    }

    /**
     * @return Returns the inst.
     */
    public static HTMLCache getInst() {
        return inst;
    }


    public String addPage(String pageId, String htmlString) {
        if (!hasPage(pageId))
            return (String) cache.get(pageId);
        return null;
    }

    public String getPage(String pageId) {
        if (hasPage(pageId))
            return (String) cache.get(pageId);
        return null;
    }

    public void clearPage(String pageId) {
        if (hasPage(pageId))
            cache.remove(pageId);
    }

    public boolean hasPage(String pageId) {
        return cache.containsKey(pageId);
    }

}



