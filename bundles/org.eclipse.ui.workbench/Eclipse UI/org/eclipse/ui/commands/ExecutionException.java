/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.commands;

/**
 * Signals that an exception occured during the execution of a command.
 * <p>
 * This class is not intended to be extended by clients.
 * </p>
 * 
 * @since 3.0
 * @deprecated Please use the "org.eclipse.core.commands" plug-in instead.
 * @see org.eclipse.core.commands.ExecutionException
 */
public final class ExecutionException extends CommandException {

    /**
     * Generated serial version UID for this class.
     * 
     * @since 3.1
     */
    private static final long serialVersionUID = 3258130262767448120L;

    /**
     * Creates a new instance of this class with the specified detail message
     * and cause.
     * 
     * @param message
     *            the detail message.
     * @param cause
     *            the cause.
     */
    public ExecutionException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new instance of <code>ExecutionException</code> using an
     * instance of the new <code>ExecutionException</code>.
     * 
     * @param e
     *            The exception from which this exception should be created;
     *            must not be <code>null</code>.
     * @since 3.1
     */
    public ExecutionException(final org.eclipse.core.commands.ExecutionException e) {
        super(e.getMessage(), e);
    }
}
