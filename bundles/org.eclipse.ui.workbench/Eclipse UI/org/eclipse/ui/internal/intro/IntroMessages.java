/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.intro;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Introduction NLS bundle.
 * 
 * @since 3.0
 */
public class IntroMessages {

    private static final String BUNDLE_NAME = "org.eclipse.ui.internal.intro.intro"; //$NON-NLS-1$

    private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle
            .getBundle(BUNDLE_NAME);

    /**
     */
    private IntroMessages() {
        //no-op
    }

    /**
     * @param key
     * @return
     * @since 3.0
     */
    public static String getString(String key) {
        try {
            return RESOURCE_BUNDLE.getString(key);
        } catch (MissingResourceException e) {
            return '!' + key + '!';
        }
    }
}