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
 * <p>
 * An instance of this interface represents a priority for use with instances of
 * <code>HandlerSubmission</code>.
 * </p>
 * <p>
 * The order of precedence (from highest to lowest) is as follows.  Submissions
 * with higher priority will be preferred over those with lower priority.
 * </p>
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

    /**
     * An internal factor used in the hash function.
     */
    private final static int HASH_FACTOR = 89;

    /**
     * The hash seed is the hash code of this class' name.
     */
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

    /**
     * The generated hash code.  The hash code is only computed once (lazily).
     * After that, requests for the hash code simply get this value.
     */
    private transient int hashCode;

    /**
     * Whether the hash code has been computed yet.
     */
    private transient boolean hashCodeComputed;

    /**
     * The string representation of this priority.  This is computed once
     * (lazily).  Before it is computed, this value is <code>null</code>.
     */
    private transient String string = null;

    /**
     * The priority value for this instance.  A lesser integer is considered to
     * have a higher priority.
     */
    private int value;

    /**
     * Constructs a new instance of <code>Priority</code> using a value. This
     * constructor should only be used internally. Priority instances should be
     * retrieved from the static members defined above.
     * 
     * @param value
     *            The priority value; a lesser integer is consider to have a
     *            higher priority value.
     */
    private Priority(int value) {
        this.value = value;
    }

    /**
     * @see Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(Object object) {
        Priority castedObject = (Priority) object;
        int compareTo = Util.compare(-value, -castedObject.value);
        return compareTo;
    }

    /**
     * The value for this priority.  The lesser the value, the higher priority
     * this represents.
     * @return The integer priority value.
     */
    int getValue() {
        return value;
    }

    /**
     * @see Object#hashCode()
     */
    public int hashCode() {
        if (!hashCodeComputed) {
            hashCode = HASH_INITIAL;
            hashCode = hashCode * HASH_FACTOR + Util.hashCode(value);
            hashCodeComputed = true;
        }

        return hashCode;
    }

    /**
     * @see Object#toString()
     */
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