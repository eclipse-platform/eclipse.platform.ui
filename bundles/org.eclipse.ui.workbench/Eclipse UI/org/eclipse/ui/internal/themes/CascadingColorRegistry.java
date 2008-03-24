/*******************************************************************************
 * Copyright (c) 2004, 2008 IBM Corporation and others.
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

import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;

/**
 * @since 3.0
 */
public class CascadingColorRegistry extends ColorRegistry {

    private ColorRegistry parent;

    private IPropertyChangeListener listener = new IPropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent event) {
        	// check to see if we have an override for the given key. If so,
			// then a change in our parent registry shouldn't cause a change in
			// us. Without this check we will propagate a new value
			// (event.getNewValue()) to our listeners despite the fact that this
			// value is NOT our current value.
			if (!hasOverrideFor(event.getProperty()))
				fireMappingChanged(event.getProperty(), event.getOldValue(),
						event.getNewValue());
        }
    };

    /**
     * Create a new instance of this class.
     * 
     * @param parent the parent registry
     */
    public CascadingColorRegistry(ColorRegistry parent) {
    	super(Display.getCurrent(), false);
        this.parent = parent;
        parent.addListener(listener);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.resource.ColorRegistry#get(java.lang.String)
     */
    public Color get(String symbolicName) {
        if (super.hasValueFor(symbolicName)) {
			return super.get(symbolicName);
		}
        
        return parent.get(symbolicName);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.resource.ColorRegistry#getKeySet()
     */
    public Set getKeySet() {
        Set keyUnion = new HashSet(super.getKeySet());
        keyUnion.addAll(parent.getKeySet());
        return keyUnion;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.resource.ColorRegistry#getRGB(java.lang.String)
     */
    public RGB getRGB(String symbolicName) {
        if (super.hasValueFor(symbolicName)) {
			return super.getRGB(symbolicName);
		}
        
        return parent.getRGB(symbolicName);
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
     * @param colorKey the provided color key
     * @return hether this cascading registry has an override
     */
    public boolean hasOverrideFor(String colorKey) {
        return super.hasValueFor(colorKey);
    }

    /**
     * Disposes of all allocated resources.
     */
    public void dispose() {
        parent.removeListener(listener);
        PlatformUI.getWorkbench().getDisplay().asyncExec(displayRunnable);
    }
}
