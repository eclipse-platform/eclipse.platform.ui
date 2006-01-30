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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;

/**
 * Common interface for requests made on elements in an asynchronous viewer. Results
 * of a request are reported to a request monitor asynchronously (usually a
 * specialization of this interface). An request may be cancelled by the client
 * making the request, or by the adapter fulfilling the request.
 * Adapters may report failure by setting an appropriate status on this monitor. When a request
 * is complete, an adapter must call <code>done()</code> on the monitor, no matter
 * if the update succeeded or failed. The <code>done()</code> method does not need to be
 * called if a request is canceled.
 * <p>
 * Operations accepting a request monitor are expected to poll the
 * monitor (using <code>isCanceled</code>) periodically and abort at their
 * earliest convenience.
 * </p>
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 * @since 3.2
 */
public interface IAsynchronousRequestMonitor extends IProgressMonitor {

    /**
     * Sets the status of this request, possibly <code>null</code>.
     * When a request fails, the status indicates why the request failed.
     * A <code>null</code> status is considered to be successful.
     * 
     * @param status request status
     */
    public void setStatus(IStatus status);
}
