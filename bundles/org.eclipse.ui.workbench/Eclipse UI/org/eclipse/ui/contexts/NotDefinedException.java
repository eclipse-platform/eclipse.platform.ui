/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.contexts;

/**
 * Signals that an attempt was made to access the properties of an undefined
 * object.
 * <p>
 * This class is not intended to be extended by clients.
 * </p>
 * 
 * @since 3.0
 */
public final class NotDefinedException extends ContextException {

    /**
     * Generated serial version UID for this class.
     * @since 3.1
     */
    private static final long serialVersionUID = 3833750983926167092L;

    /**
     * Creates a new instance of this class with the specified detail message.
     * 
     * @param message
     *            the detail message.
     */
    public NotDefinedException(String message) {
        super(message);
    }
}