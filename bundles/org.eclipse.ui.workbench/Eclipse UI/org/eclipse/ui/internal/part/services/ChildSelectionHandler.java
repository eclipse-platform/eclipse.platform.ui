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
package org.eclipse.ui.internal.part.services;

import org.eclipse.core.components.ComponentException;
import org.eclipse.core.components.IServiceProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.internal.part.multiplexer.INestedComponent;
import org.eclipse.ui.internal.part.multiplexer.ISharedContext;
import org.eclipse.ui.part.services.ISelectionHandler;

/**
 * Multiplexed version of the ISelectionHandler interface
 * 
 * @since 3.1
 */
public class ChildSelectionHandler implements ISelectionHandler, INestedComponent {

    private ISelectionHandler parent;
    private ISelection selection;
    private boolean isActive = false;
    
    public ChildSelectionHandler(ISharedContext shared) throws ComponentException {
        
        IServiceProvider parentContainer = shared.getSharedComponents();
        // Get access to the shared ISelectionHandler being multiplexed (we should
        // only modify it when we're the active child)
        this.parent = (ISelectionHandler)parentContainer.getService(ISelectionHandler.class);
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.part.interfaces.INestedComponent#activate()
     */
    public void activate() {
        // Forward our stored selection to the shared interface
        parent.setSelection(selection);
        isActive = true;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.part.interfaces.INestedComponent#deactivate()
     */
    public void deactivate() {
        isActive = false;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.part.interfaces.ISelectionHandler#setSelection(org.eclipse.jface.viewers.ISelection)
     */
    public void setSelection(ISelection newSelection) {
        // Remember the child's new selection
        selection = newSelection;
        if (isActive) {
            // If we're active, forward the selection directly to the shared
            // interface
            parent.setSelection(newSelection);
        }
    }
    
}
