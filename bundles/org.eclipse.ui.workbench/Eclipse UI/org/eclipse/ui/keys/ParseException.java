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

package org.eclipse.ui.keys;

/**
 * <p>
 * An exception indicating problems while parsing formal string representations
 * of either <code>KeyStroke</code> or <code>KeySequence</code> objects.
 * </p>
 * <p>
 * <code>ParseException</code> objects are immutable. Clients are not
 * permitted to extend this class.
 * </p>
 * 
 * @since 3.0
 */
public final class ParseException extends Exception {

    /**
     * Constructs a <code>ParseException</code> with the specified detail
     * message.
     * 
     * @param s
     *            the detail message.
     */
    public ParseException(final String s) {
        super(s);
    }
}