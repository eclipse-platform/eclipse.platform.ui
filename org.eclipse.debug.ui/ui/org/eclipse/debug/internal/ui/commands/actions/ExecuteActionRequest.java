/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.commands.actions;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.internal.core.commands.DebugCommandRequest;
import org.eclipse.debug.internal.ui.DebugUIMessages;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.jface.dialogs.MessageDialog;

/**
 * Plain status collector for actions. Has no result.
 * 
 * @since 3.3
 * 
 */
public class ExecuteActionRequest extends DebugCommandRequest {
	
	private ICommandParticipant fParticipant = null;
	
	public ExecuteActionRequest(Object[] elements) {
		super(elements);
	}

    public void done() {
    	if (fParticipant != null) {
			fParticipant.requestDone(this);
			fParticipant = null;
		}
        final IStatus status = getStatus();
        if (status != null) {
            switch (status.getSeverity()) {
            case IStatus.ERROR:
                DebugUIPlugin.getStandardDisplay().asyncExec(new Runnable() {
                    public void run() {
                        MessageDialog.openError(DebugUIPlugin.getShell(), DebugUIMessages.DebugUITools_Error_1, status.getMessage());
                    }
                });
                break;
            case IStatus.WARNING:
                DebugUIPlugin.getStandardDisplay().asyncExec(new Runnable() {
                    public void run() {
                        MessageDialog.openWarning(DebugUIPlugin.getShell(), DebugUIMessages.DebugUITools_Error_1, status.getMessage());
                    }
                });
                break;
            case IStatus.INFO:
                DebugUIPlugin.getStandardDisplay().asyncExec(new Runnable() {
                    public void run() {
                        MessageDialog.openInformation(DebugUIPlugin.getShell(), DebugUIMessages.DebugUITools_Error_1, status.getMessage());
                    }
                });
                break;
            }
        }
    }
    
	public void setCommandParticipant(ICommandParticipant participant) {
		fParticipant = participant;
	}

}
