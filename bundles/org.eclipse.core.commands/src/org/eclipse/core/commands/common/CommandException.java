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
 * <p>
 * <em>EXPERIMENTAL</em>.  The commands architecture is currently under
 * development for Eclipse 3.1.  This class -- its existence, its name and its
 * methods -- are in flux.  Do not use this class yet.
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
