/*******************************************************************************
 * Copyright (c) 2005, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.views.navigator;


/**
 * Utility class which helps managing messages
 * @since 2.0
 * @deprecated These messages are not API and should not be referenced
 * outside of this plug-in.
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients. 
 */
public class ResourceNavigatorMessages {
    
	private ResourceNavigatorMessages() {
        // prevent instantiation of class
    }

    /**
     * Returns the formatted message for the given key in
     * the resource bundle. 
     *
     * @param key the resource name
     * @param args the message arguments
     * @return the string
     */
    public static String format(String key, Object[] args) {
        return key;
    }

    /**
     * Returns the resource object with the given key in
     * the resource bundle. If there isn't any value under
     * the given key, the key is returned.
     *
     * @param key the resource name
     * @return the string
     */
    public static String getString(String key) {
        return key;
    }
}
