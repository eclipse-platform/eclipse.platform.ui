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
package org.eclipse.debug.core;

import org.eclipse.core.runtime.IStatus;

/**
 * Common base interface for an asynchronously processed request. A request may succeed,
 * fail or be canceled as indicated by the request's status. When a request is complete
 * the client fulfilling the request must call <code>done()</code> on the request whether
 * the operation succeeds, fails, or is canceled.
 * <p>
 * Specific requests (sub types of this interface) often include data pertaining to
 * the request and usually contain results of the request.
 * </p>
 * <p>
 * Clients are expected to poll a request (using <code>isCanceled</code>)
 * periodically and abort at their earliest convenience calling <code>done()</code>.
 * A request can be canceled by the originator of the request or a client
 * fulfilling a request. 
 * </p>
 * <p>
 * Clients that invoke request handlers may implemented this interface.
 * </p>
 * @since 3.3
 * <p>
 * <strong>EXPERIMENTAL</strong>. This interface has been added as
 * part of a work in progress. There is no guarantee that this API will
 * remain unchanged during the 3.3 release cycle. Please do not use this API
 * without consulting with the Platform/Debug team.
 * </p>
 */
public interface IRequest {

    /**
     * Sets the status for this request indicating whether this request
     * succeeded, failed, or was canceled. When a request fails, the status
     * indicates why the request failed. A <code>null</code> status is considered
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
     * whether the request succeeds, fails, or is cancelled to indicate that
     * processing is complete.
     */
    public void done();
    
    /**
     * Cancels this request. A request may be canceled by the originator of request
     * or a client fulfilling a request. Optionally a canceled status may be set on
     * this request with more details. Callers must still call <code>done()</code> to
     * indicate the request is complete.
     */
    public void cancel();
    
    /**
     * Returns whether this request has been canceled.
     * <p>
     * Request processors are expected to poll a request (using <code>isCanceled</code>)
     * periodically and abort at their earliest convenience calling <code>done()</code>.
     * A request can be canceled by the originator of the request or a processor
     * fulfilling a request. 
     * </p>
     * @return whether this request has been canceled
     */
    public boolean isCanceled();
    
}
