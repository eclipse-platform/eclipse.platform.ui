/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Lars Vogel <Lars.Vogel@vogella.com> - Bug 480928
 *******************************************************************************/
package org.eclipse.ui.examples.multipageeditor;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Utility class which helps with managing messages.
 */
/* package */class MessageUtil {

    private static final String RESOURCE_BUNDLE = "org.eclipse.ui.examples.multipageeditor.messages";//$NON-NLS-1$

    private static ResourceBundle fgResourceBundle = ResourceBundle
            .getBundle(RESOURCE_BUNDLE);

    private MessageUtil() {
        // prevent instantiation of class
    }

    /**
     * Returns the resource object with the given key in
     * the resource bundle. If there isn't any value under
     * the given key, the key is returned, surrounded by '!'s.
     *
     * @param key the resource name
     * @return the string
     */
    public static String getString(String key) {
        try {
            return fgResourceBundle.getString(key);
        } catch (MissingResourceException e) {
            return "!" + key + "!";//$NON-NLS-2$ //$NON-NLS-1$
        }
    }
}
