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
package org.eclipse.ui.internal.themes;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;

/**
 * @since 3.0
 */
public class CascadingFontRegistry extends FontRegistry {

    private FontRegistry parent;

    private IPropertyChangeListener listener = new IPropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent event) {
            fireMappingChanged(event.getProperty(), event.getOldValue(), event
                    .getNewValue());
        }
    };

    /**
     * Create a new instance of this class.
     * 
     * @param parent the parent registry
     */
    public CascadingFontRegistry(FontRegistry parent) {
    	super(Display.getCurrent(), false);
        this.parent = parent;
        parent.addListener(listener);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.resource.FontRegistry#get(java.lang.String)
     */
    public Font get(String symbolicName) {
        if (super.hasValueFor(symbolicName))
            return super.get(symbolicName);
        return parent.get(symbolicName);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.resource.FontRegistry#getKeySet()
     */
    public Set getKeySet() {
        Set keyUnion = new HashSet(super.getKeySet());
        keyUnion.addAll(parent.getKeySet());
        return keyUnion;
    }

    public FontData[] getFontData(String symbolicName) {
        if (super.hasValueFor(symbolicName))
            return super.getFontData(symbolicName);
        return parent.getFontData(symbolicName);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.resource.ColorRegistry#hasValueFor(java.lang.String)
     */
    public boolean hasValueFor(String colorKey) {
        return super.hasValueFor(colorKey) || parent.hasValueFor(colorKey);
    }

    /**
     * Returns whether this cascading registry has an override for the provided 
     * color key.
     * 
     * @param fontKey the provided color key
     * @return hether this cascading registry has an override
     */
    public boolean hasOverrideFor(String fontKey) {
        return super.hasValueFor(fontKey);
    }

    /**
     * Disposes of all allocated resources.
     */
    public void dispose() {
        parent.removeListener(listener);
        PlatformUI.getWorkbench().getDisplay().asyncExec(displayRunnable);
    }
}
