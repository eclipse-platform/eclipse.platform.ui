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
package org.eclipse.ui.internal.presentation;

import java.util.Iterator;
import java.util.Set;

import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.swt.widgets.Display;


/**
 * A temporary font registry.  It is essentially a copy of another registry 
 * that does not hook to the provided display.
 * 
 * @since 3.0
 */
public class TemporaryFontRegistry extends FontRegistry {

    /**
     */
    public TemporaryFontRegistry(FontRegistry registry) {
        // do not hook listeners        
        Set keySet = registry.getKeySet();
        for (Iterator i = keySet.iterator(); i.hasNext();) {
            String key = (String) i.next();
            put(key, registry.getFontData(key));
        }        
    }
    
    /**
     * Disposes of all allocated resources.
     */
    public void shutdown() {
        Display.getCurrent().asyncExec(displayRunnable);
    }
}
