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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.eclipse.help.internal.util.ResourceLocator;

/**
 * @author sturmash
 * 
 * To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Generation - Code and Comments
 */
public class IndexFile {

    private String plugin;

    private String href;

    private String locale;

    protected IndexFile(String plugin, String href, String locale) {
        this.plugin = plugin;
        this.href = href;
        this.locale = locale;
    }

    /**
     * @return Returns the href.
     */
    public String getHref() {
        return href;
    }

    /**
     * @return Returns the locale.
     */
    public String getLocale() {
        return locale;
    }

    /**
     * @return Returns the plugin.
     */
    public String getPluginID() {
        return plugin;
    }

    /**
     * @param builder
     */
    public void build(IndexBuilder builder) {
        builder.buildIndexFile(this);
    }

    /**
     * @return
     */
    protected InputStream getInputStream() {
        InputStream stream = null;
        try {
            if (plugin != null)
                stream = ResourceLocator.openFromPlugin(plugin, href, locale);
            else
                stream = new FileInputStream(href);
        } catch (IOException e) {
            // Nothing to do
        }
        return stream;
    }

    /**
     * Used by debugger
     */
    public String toString() {
        return plugin + "/" + href; //$NON-NLS-1$
    }
}
