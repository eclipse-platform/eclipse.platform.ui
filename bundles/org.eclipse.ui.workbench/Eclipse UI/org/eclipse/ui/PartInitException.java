/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui;

import org.eclipse.core.runtime.IStatus;

/**
 * A checked exception indicating a workbench part cannot be initialized 
 * correctly. The message text provides a further description of the problem.
 * <p>
 * This exception class is not intended to be subclassed by clients.
 * </p>
 * @noextend This class is not intended to be subclassed by clients.
 */
public class PartInitException extends WorkbenchException {
    
    /**
     * Generated serial version UID for this class.
     * @since 3.1
     */
    private static final long serialVersionUID = 3257284721296684850L;

    /**
     * Creates a new exception with the given message.
     * 
     * @param message the message
     */
    public PartInitException(String message) {
        super(message);
    }

    /**
     * Creates a new exception with the given message.
     * 
     * @param message the message
     * @param nestedException a exception to be wrapped by this PartInitException
     */
    public PartInitException(String message, Throwable nestedException) {
        super(message, nestedException);
    }

    /**
     * Creates a new exception with the given status object.  The message
     * of the given status is used as the exception message.
     *
     * @param status the status object to be associated with this exception
     */
    public PartInitException(IStatus status) {
        super(status);
    }
}
