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
 * <code>Key</code> is the abstract base class for all objects representing
 * keys on the keyboard.
 * </p>
 * <p>
 * All <code>Key</code> objects have a formal string representation, called
 * the 'name' of the key, available via the <code>toString()</code> method.
 * </p>
 * <p>
 * All <code>Key</code> objects, via the <code>format()</code> method,
 * provide a version of their formal string representation translated by
 * platform and locale, suitable for display to a user.
 * </p>
 * <p>
 * <code>Key</code> objects are immutable. Clients are not permitted to extend
 * this class.
 * </p>
 * 
 * @since 3.0
 */
public abstract class Key implements Comparable {

    /**
     * An internal constant used only in this object's hash code algorithm.
     */
    private final static int HASH_FACTOR = 89;

    /**
     * An internal constant used only in this object's hash code algorithm.
     */
    private final static int HASH_INITIAL = Key.class.getName().hashCode();

    /**
     * The cached hash code for this object. Because Key objects are immutable,
     * their hash codes need only to be computed once. After the first call to
     * <code>hashCode()</code>, the computed value is cached here to be used
     * for all subsequent calls.
     */
    private transient int hashCode;

    /**
     * A flag to determine if the <code>hashCode</code> field has been
     * computed and cached.
     */
    private transient boolean hashCodeComputed;

    /**
     * The formal string representation for this object. Equality of Key
     * objects is determined solely by this field.
     */
    protected String name;

    /**
     * Constructs an instance of <code>Key</code> given its formal string
     * representation.
     * 
     * @param name
     *            the formal string representation of this key. Must not be
     *            <code>null</code>.
     */
    Key(String name) {
        if (name == null)
            throw new NullPointerException();

        this.name = name;
    }

    /**
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(Object object) {
        Key castedObject = (Key) object;
        int compareTo = name.compareTo(castedObject.name);
        return compareTo;
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object object) {
        if (!(object instanceof Key))
            return false;

        Key castedObject = (Key) object;
        boolean equals = true;
        equals &= name.equals(castedObject.name);
        return equals;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        if (!hashCodeComputed) {
            hashCode = HASH_INITIAL;
            hashCode = hashCode * HASH_FACTOR + name.hashCode();
            hashCodeComputed = true;
        }

        return hashCode;
    }

    /**
     * Returns the formal string representation for this key.
     * 
     * @return The formal string representation for this key. Guaranteed not to
     *         be <code>null</code>.
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return name;
    }
}