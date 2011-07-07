/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.views.breakpoints;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPersistableElement;

/**
 * Adapter to save and restore breakpoints for a working set.
 */
public class BreakpointPersistableElementAdapter implements IPersistableElement {
    
    private IBreakpoint fBreakpoint;
    
    public static final String TAG_MARKER_ID = "TAG_MARKER_ID"; //$NON-NLS-1$
    public static final String TAG_RESOURCE_FACTORY_ID = "TAG_RESOURCE_FACTORY_ID"; //$NON-NLS-1$
    
    /**
     * Constructs a new persitable element adapter for the given breakpoint.
     * 
     * @param breakpoint the backing {@link IBreakpoint}
     */
    public BreakpointPersistableElementAdapter(IBreakpoint breakpoint) {
        fBreakpoint = breakpoint;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IPersistableElement#getFactoryId()
     */
    public String getFactoryId() {
        return "org.eclipse.debug.ui.elementFactory.breakpoints"; //$NON-NLS-1$
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IPersistableElement#saveState(org.eclipse.ui.IMemento)
     */
    public void saveState(IMemento memento) {
        IMarker marker = fBreakpoint.getMarker();
        if (marker != null) {
            IResource resource = marker.getResource();
            IPersistableElement pe = (IPersistableElement) resource.getAdapter(IPersistableElement.class);
            if (pe != null) {
                long id = marker.getId();
                String longString = Long.toString(id);
                memento.putString(TAG_MARKER_ID, longString);
                memento.putString(TAG_RESOURCE_FACTORY_ID, pe.getFactoryId());
                pe.saveState(memento);
            }            
        }
    }

}
