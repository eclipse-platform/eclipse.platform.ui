/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.viewers;

import org.eclipse.ui.IWorkbenchPart;

/**
 * Presentation context.
 * <p>
 * Clients may instantiate and subclass this class.
 * </p>
 * @since 3.2
 */
public class PresentationContext implements IPresentationContext {
    
    private IWorkbenchPart fPart;
    
    /**
     * Constructs a presentation context for the given part.
     * 
     * @param part workbench part
     */
    public PresentationContext(IWorkbenchPart part) {
        fPart = part;
    }

    /* (non-Javadoc)
     * @see org.eclipse.debug.ui.viewers.IPresentationContext#getPart()
     */
    public IWorkbenchPart getPart() {
        return fPart;
    }

}
