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

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.part.services.IStatusFactory;
import org.osgi.framework.Bundle;

/**
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
    public IStatus newStatus(Throwable t) {
        String message = t.getLocalizedMessage();
        
        if (message == null) {
            message = t.getMessage();
        }
        
        if (message == null) {
            message = t.toString();
        }
        
        Throwable cause = t.getCause();
        if (cause == null) {
            cause = t;
        }
        
        return newStatus(message, cause); 
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.component.services.IErrorContext#getStatus(java.lang.String, java.lang.Throwable)
     */
    public IStatus newStatus(String message, Throwable t) {
        return new Status(IStatus.ERROR, pluginBundle.getSymbolicName(), Status.OK, message, t);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.component.services.IErrorContext#getStatus(int, java.lang.String)
     */
    public IStatus newStatus(int severity, String message) {
        return new Status(severity, pluginBundle.getSymbolicName(), Status.OK, message, null);
    }

}
