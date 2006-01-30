/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.viewers.provisional;

import org.eclipse.ui.IWorkbenchPart;

/**
 * Context in which an asynchronous request has been made.
 * <p>
 * Clients may implement and extend this interface to provide
 * special contexts. Implementations must subclass {@link PresentationContext}.
 * </p>
 * @since 3.2
 */
public interface IPresentationContext {
    
    /**
     * Returns the part for which a request is being made
     * or <code>null</code> if none. 
     * 
     * @return the part for which a request is being made
     * or <code>null</code>
     */
    public IWorkbenchPart getPart();
    
}
