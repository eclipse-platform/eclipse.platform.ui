/*******************************************************************************
 * Copyright (c) 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.examples.rcp.browser;

import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * Plug-in class for the browser example.
 */
public class BrowserPlugin extends AbstractUIPlugin {
    private static BrowserPlugin DEFAULT;
    
    public BrowserPlugin() {
        DEFAULT = this;
    }

    public static BrowserPlugin getDefault() {
        return DEFAULT;
    }
}
