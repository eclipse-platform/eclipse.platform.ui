/*******************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.ui.commands;

import org.eclipse.ui.internal.util.Util;

/**
 * An instance of this interface represents a priority for use with instances of
 * <code>HandlerSubmission</code>.
 * <p>
 * Order of precedence:
 * <ol>
 * <li>MEDIUM</li>
 * <li>LOW</li>
 * <li>LEGACY</li>
 * </ol>
 * </p>
 * <p>
 * This class is not intended to be extended by clients.
 * </p>
 * 
 * @since 3.0
 * @see HandlerSubmission
 */
public final class Priority implements Comparable {

    private final static int HASH_FACTOR = 89;

    private final static int HASH_INITIAL = Priority.class.getName().hashCode();

    /**
     * An instance representing 'legacy' priority.
     */
    public final static Priority LEGACY = new Priority(3);

    /**
     * An instance representing 'low' priority.
     */
    public final static Priority LOW = new Priority(2);

    /**
     * An instance representing 'medium' priority.
     */
    public final static Priority MEDIUM = new Priority(1);

    private transient int hashCode;

    private transient boolean hashCodeComputed;

    private transient String string;

    private int value;

    private Priority(int value) {
        this.value = value;
    }

    public int compareTo(Object object) {
        Priority castedObject = (Priority) object;
        int compareTo = Util.compare(-value, -castedObject.value);
        return compareTo;
    }

    int getValue() {
        return value;
    }

    public int hashCode() {
        if (!hashCodeComputed) {
            hashCode = HASH_INITIAL;
            hashCode = hashCode * HASH_FACTOR + Util.hashCode(value);
            hashCodeComputed = true;
        }

        return hashCode;
    }

    public String toString() {
        if (string == null) {
            final StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("[value="); //$NON-NLS-1$
            stringBuffer.append(value);
            stringBuffer.append(']');
            string = stringBuffer.toString();
        }

        return string;
    }
}