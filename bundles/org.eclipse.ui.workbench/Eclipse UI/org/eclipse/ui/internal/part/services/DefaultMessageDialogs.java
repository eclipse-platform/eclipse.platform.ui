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

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.part.components.services.IUserMessages;

/**
 * Default implementation of the IMessageDialogs interface. Takes the part's 
 * control as context and allows the part to open dialogs in a child shell.
 * 
 * @since 3.1
 */
public class DefaultMessageDialogs implements IUserMessages {

    private Composite control;
    
    /**
     * Component constructor. Do not invoke directly.
     */
    public DefaultMessageDialogs(Composite control) {
        this.control = control;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.workbench.services.IMessageDialogs#open(org.eclipse.core.runtime.IStatus)
     */
    public void show(IStatus message) {
        if (message.getSeverity() == IStatus.ERROR) {
            ErrorDialog.openError(control.getShell(), null, null, message);
        } else {
            show(message.getSeverity(), message.getMessage());
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.workbench.services.IMessageDialogs#openError(java.lang.String, java.lang.Throwable)
     */
    public void showError(String message, Throwable cause) {
        show(new Status(IStatus.ERROR, 
                WorkbenchPlugin.getDefault().getBundle().getSymbolicName(),
                IStatus.OK,
                message,
                cause));
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.part.components.services.IMessageDialogs#open(int, java.lang.String)
     */
    public void show(int severity, String message) {
        if (severity == IStatus.ERROR) {
            MessageDialog.openError(control.getShell(), null, message);
        } else if (severity == IStatus.WARNING) {
            MessageDialog.openWarning(control.getShell(), null, message);
        } else {
            MessageDialog.openInformation(control.getShell(), null, message);
        }    
    }
}
