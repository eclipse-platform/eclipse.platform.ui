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
package org.eclipse.ui.internal.part;

import org.eclipse.ui.internal.components.framework.Components;
import org.eclipse.ui.internal.components.framework.IServiceProvider;
import org.eclipse.ui.internal.part.components.interfaces.IFocusable;
import org.eclipse.ui.internal.part.multiplexer.IDelegatingContext;

/**
 * @since 3.1
 */
public class DelegatingFocusable implements IFocusable {

    private IDelegatingContext context;
    
    public DelegatingFocusable(IDelegatingContext context) {
        this.context = context;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.part.components.interfaces.IFocusable#setFocus()
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
