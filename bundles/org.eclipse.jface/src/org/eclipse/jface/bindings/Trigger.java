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
package org.eclipse.jface.bindings;

/**
 * <p>
 * Any object that can be used as a trigger for a binding. This ensures that
 * trigger conform to certain minimum requirements. Namely, triggers need to be
 * hashable.
 * </p>
 * <p>
 * To assist with the hashing, some member fields have been provided:
 * <code>HASH_FACTOR</code>,<code>hashCode</code> and
 * <code>hashCodeComputed</code>.
 * </p>
 * 
 * @since 3.1
 */
public abstract class Trigger {

    /**
     * A factor for computing the hash code for all schemes.
     */
    protected final static int HASH_FACTOR = 89;

    /**
     * The hash code for this object. This value is computed lazily, and marked
     * as invalid when one of the values on which it is based changes.
     */
    protected transient int hashCode;

    /**
     * Whether <code>hashCode</code> still contains a valid value.
     */
    protected transient boolean hashCodeComputed = false;

    /**
     * Tests whether this object is equal to another object. A handle object is
     * only equal to another trigger with the same properties.
     * 
     * @param object
     *            The object with which to compare; may be <code>null</code>.
     * @return <code>true</code> if the objects are equal; <code>false</code>
     *         otherwise.
     */
    public abstract boolean equals(final Object object);

    /**
     * Computes the hash code for this object.
     * 
     * @return The hash code for this object.
     */
    public abstract int hashCode();
}
