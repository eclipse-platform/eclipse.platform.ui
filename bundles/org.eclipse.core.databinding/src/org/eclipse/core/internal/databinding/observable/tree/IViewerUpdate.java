/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.databinding.observable.tree;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;

/**
 * A context sensitive viewer update request.
 * 
 * @since 3.3
 */
public interface IViewerUpdate extends IProgressMonitor {

    /**
     * Sets the status of this request, possibly <code>null</code>.
     * When a request fails, the status indicates why the request failed.
     * A <code>null</code> status is considered to be successful.
     * 
     * @param status request status
     */
    public void setStatus(IStatus status);
    
    /**
     * Returns the status of this request, or <code>null</code>.
     * 
     * @return request status or <code>null</code>
     */
    public IStatus getStatus();	
    
    /**
     * Returns the model element corresponding to the given tree path.
     * Returns the root element for the empty path.
     * 
     * @param path viewer tree path
     * @return corresponding model element
     */
    public Object getElement(TreePath path);
}
