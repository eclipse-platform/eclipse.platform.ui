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
package org.eclipse.core.commands.common;

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
     *            the detail message; may be <code>null</code>.
     */
    public CommandException(final String message) {
        super(message);
    }

    /**
     * Creates a new instance of this class with the specified detail message
     * and cause.
     * 
     * @param message
     *            the detail message; may be <code>null</code>.
     * @param cause
     *            the cause; may be <code>null</code>.
     */
    public CommandException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
