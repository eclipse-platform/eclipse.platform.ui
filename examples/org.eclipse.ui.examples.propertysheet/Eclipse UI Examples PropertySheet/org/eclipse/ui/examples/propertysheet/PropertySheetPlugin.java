/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Joe Bowbeer (jozart@blarg.net) - removed dependency on runtime compatibility layer (bug 74526)
 *******************************************************************************/
package org.eclipse.ui.examples.propertysheet;

import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * This is the top-level class of the property sheet example.
 *
 * @see AbstractUIPlugin for additional information on UI plugins
 */
public class PropertySheetPlugin extends AbstractUIPlugin {
    // Default instance of the receiver
    private static PropertySheetPlugin inst;

    /**
     * Create the PropertySheet plugin and cache its default instance
     */
    public PropertySheetPlugin() {
        if (inst == null)
            inst = this;
    }

    /**
     * Returns the plugin singleton.
     *
     * @return the default PropertySheetPlugin instance
     */
    static public PropertySheetPlugin getDefault() {
        return inst;
    }
}