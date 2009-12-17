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
 * Signals that an attempt was made to access the properties of an unhandled
 * object.
 * <p>
 * This class is not intended to be extended by clients.
 * </p>
 * 
 * @since 3.0
 * @deprecated Please use the "org.eclipse.core.commands" plug-in instead.
 * @see org.eclipse.core.commands.NotHandledException
 */
public final class NotHandledException extends CommandException {

    /**
     * Generated serial version UID for this class.
     * 
     * @since 3.1
     */
    private static final long serialVersionUID = 3256446914827726904L;

    /**
     * Creates a new instance of this class with the specified detail message.
     * 
     * @param s
     *            the detail message.
     */
    public NotHandledException(String s) {
        super(s);
    }

    /**
     * Constructs a legacy <code>NotHandledException</code> based on the new
     * <code>NotHandledException</code>
     * 
     * @param e
     *            The exception from which this exception should be created;
     *            must not be <code>null</code>.
     * @since 3.1
     */
    public NotHandledException(final org.eclipse.core.commands.NotHandledException e) {
        super(e.getMessage(), e);
    }
}
