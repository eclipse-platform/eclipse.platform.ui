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
package org.eclipse.ui.internal.part;

import org.eclipse.core.components.Components;
import org.eclipse.core.components.IServiceProvider;
import org.eclipse.ui.internal.part.multiplexer.IDelegatingContext;
import org.eclipse.ui.part.interfaces.IFocusable;

/**
 * @since 3.1
 */
public class DelegatingFocusable implements IFocusable {

    private IDelegatingContext context;
    
    public DelegatingFocusable(IDelegatingContext context) {
        this.context = context;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.part.interfaces.IFocusable#setFocus()
     */
    public boolean setFocus() {
        IServiceProvider active = context.getActive();

        if (active == null) {
            return false;
        }
        
        IFocusable focusable = (IFocusable)Components.getAdapter(active, IFocusable.class);
        if (focusable == null) {
            return false;
        }
        
        return focusable.setFocus();
    }

}
