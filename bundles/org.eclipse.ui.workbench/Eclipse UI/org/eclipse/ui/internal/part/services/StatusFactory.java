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
package org.eclipse.ui.internal.part.services;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.internal.misc.StatusUtil;
import org.eclipse.ui.internal.part.components.services.IStatusFactory;
import org.osgi.framework.Bundle;

/**
 * Constructs status messages associated with the given plugin bundle.
 * 
 * Not intended to be subclassed by clients
 * 
 * @since 3.1
 */
public class StatusFactory implements IStatusFactory {

    private Bundle pluginBundle;
    
    public StatusFactory(Bundle context) {
        this.pluginBundle = context;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.component.services.IErrorContext#getStatus(java.lang.Throwable)
     */
    public IStatus newError(Throwable t) {
        String message = StatusUtil.getLocalizedMessage(t);
        
        return newError(message, t); 
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.component.services.IErrorContext#getStatus(java.lang.String, java.lang.Throwable)
     */
    public IStatus newError(String message, Throwable t) {
        String pluginId = pluginBundle.getSymbolicName();
        int errorCode = IStatus.OK;
        
        // If this was a CoreException, keep the original plugin ID and error code
        if (t instanceof CoreException) {
            CoreException ce = (CoreException)t;
            pluginId = ce.getStatus().getPlugin();
            errorCode = ce.getStatus().getCode();
        }
        
        return new Status(IStatus.ERROR, pluginId, errorCode, message, 
                StatusUtil.getCause(t));
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.component.services.IErrorContext#getStatus(int, java.lang.String)
     */
    public IStatus newStatus(int severity, String message) {
        return new Status(severity, pluginBundle.getSymbolicName(), Status.OK, message, null);
    }

    public IStatus newMessage(String message) {
        return newStatus(IStatus.INFO, message);
    }
}
