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
package org.eclipse.commands.misc;

/**
 * Signals that an exception occured within the command architecture.
 * <p>
 * This class is not intended to be extended by clients.
 * </p>
 * 
 * @since 3.1
 */
public abstract class CommandException extends Exception {

    /**
     * Creates a new instance of this class with the specified detail message.
     * 
     * @param message
     *            the detail message.
     */
    public CommandException(final String message) {
        super(message);
    }

    /**
     * Creates a new instance of this class with the specified detail message
     * and cause.
     * 
     * @param message
     *            the detail message.
     * @param cause
     *            the cause.
     */
    public CommandException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
