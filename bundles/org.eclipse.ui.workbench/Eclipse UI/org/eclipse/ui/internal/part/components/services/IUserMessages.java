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
package org.eclipse.ui.internal.part.components.services;

import org.eclipse.core.runtime.IStatus;

/**
 * Interface that can be used to display messages to the user.
 * The message does not need to be explicitly cleared. It will be cleared once 
 * the user confirms it or a certain amount of time has passed.
 * 
 * <p>
 * Implementations of this interface could display messages in a dialog box,
 * log window, or time-delayed popup. This interface would not be used
 * for writing messages to the status line, as such messages would need to be
 * cleared programmatically rather than by the user.
 * </p>
 *
 * <p>
 * The default implementation of this interface displays each message in a modal
 * dialog.
 * </p>
 * 
 * @since 3.1
 */
public interface IUserMessages {
    /**
     * Display the given status message to the user
     *
     * @param message status message to display
     */
    public void show(IStatus message);
    
    /**
     * Display the given error and optional exception.
     *
     * @param message error message
     * @param cause optional cause (may be null if none)
     */
    public void showError(String message, Throwable cause);
    
    /**
     * Display the given message with the given severity.
     * 
     * @param severity one of the IStatus.* constants returned by <code>IStatus.getSeverity()</code>
     * @param message message to display
     */
    public void show(int severity, String message);
}
