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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * A checked exception indicating a recoverable error occured internal to the
 * workbench. The status provides a further description of the problem.
 * <p>
 * This exception class is not intended to be subclassed by clients.
 * </p>
 */
public class WorkbenchException extends CoreException {

    /**
     * Generated serial version UID for this class.
     * @since 3.1
     */
    private static final long serialVersionUID = 3258125864872129078L;

    /**
     * Creates a new exception with the given message.
     * 
     * @param message the message
     */
    public WorkbenchException(String message) {
        this(new Status(IStatus.ERROR, PlatformUI.PLUGIN_ID, 0, message, null));
    }

    /**
     * Creates a new exception with the given message.
     *
     * @param message the message
     * @param nestedException an exception to be wrapped by this WorkbenchException
     */
    public WorkbenchException(String message, Throwable nestedException) {
        this(new Status(IStatus.ERROR, PlatformUI.PLUGIN_ID, 0, message,
                nestedException));
    }

    /**
     * Creates a new exception with the given status object.  The message
     * of the given status is used as the exception message.
     *
     * @param status the status object to be associated with this exception
     */
    public WorkbenchException(IStatus status) {
        super(status);
    }
}
