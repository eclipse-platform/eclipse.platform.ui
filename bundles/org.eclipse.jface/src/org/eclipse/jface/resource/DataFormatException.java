/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.resource;

/**
 * An exception indicating that a string value could not be
 * converted into the requested data type.
 *
 * @see StringConverter
 */
public class DataFormatException extends IllegalArgumentException {
    /**
     * Creates a new exception.
     */
    public DataFormatException() {
        super();
    }

    /**
     * Creates a new exception.
     *
     * @param message the message
     */
    public DataFormatException(String message) {
        super(message);
    }
}