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
package org.eclipse.debug.core.commands;

import org.eclipse.core.runtime.IStatus;

/**
 * A result collector that accepts a status. This common base interface is used
 * to make non-blocking requests on models that may reply asynchronously. A request
 * may succeed or fail as indicated by the status. When a request is complete, the
 * client fulfilling the request must call <code>done()</code> on the
 * collector, whether the request succeeds or fails.
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 * @since 3.3
 * <p>
 * <strong>EXPERIMENTAL</strong>. This interface has been added as
 * part of a work in progress. There is no guarantee that this API will
 * remain unchanged during the 3.3 release cycle. Please do not use this API
 * without consulting with the Platform/Debug team.
 * </p>
 */
public interface IStatusCollector {

    /**
     * Sets the status for this request indicating whether this request
     * succeeded or failed. When a request fails, the status indicates
     * why the request failed. A <code>null</code> status is considered
     * to be successful.
     * 
     * @param status request status or <code>null</code>
     */
    public void setStatus(IStatus status);
    
    /**
     * Returns the status of this request, or <code>null</code>.
     * 
     * @return request status - <code>null</code> is equivalent
     *  to an OK status
     */
    public IStatus getStatus();
    
    /**
     * Indicates this request is complete. Clients must call this method
     * whether the request succeeds, fails, or is cancelled.
     */
    public void done();
    
}
