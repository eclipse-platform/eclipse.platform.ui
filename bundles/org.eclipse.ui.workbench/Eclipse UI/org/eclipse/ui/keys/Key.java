/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.keys;

/**
 * <p>
 * <code>Key</code> is the abstract base class for all objects representing keys 
 * on the keyboard. All <code>Key</code> objects have a name describing which 
 * key they represent. The name of a <code>Key</code> object is provided by its
 * <code>toString()</code> method. <code>Key</code> objects are considered equal 
 * iff their names are equal. All <code>Key</code> objects provide a translated
 * version of their name, suitable for display to a user. It is not permitted to
 * extend this class.
 * </p>
 * <p>
 * <em>EXPERIMENTAL</em>
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
	 * The name of this key object. Equality of Key objects is determined 
	 * solely by this field.
	 */
	protected String name;

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
	 * Constructs an instance of Key given a name.
	 * 
	 * @param name The name of the key, must not be null.
	 */
	Key(String name) {	
		if (name == null)
			throw new NullPointerException();
		
		this.name = name;
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(Object object) {
		Key key = (Key) object;
		int compareTo = name.compareTo(key.name);
		return compareTo;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object object) {
		if (!(object instanceof Key))
			return false;

		Key key = (Key) object;
		boolean equals = true;
		equals &= name.equals(key.name);
		return equals;
	}

	/**
	 * Returns the name of the key that this object represents, translated for
	 * the user's current platform and locale.
	 * 
	 * @return The translated name of the key that this object represents, 
	 *         suitable for display to the user. Guaranteed not to be null.
	 */
	public abstract String format();

	/* (non-Javadoc)
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
	 * Returns the name of the key that this object represents.
	 * 
	 * @return The name of the key that this object represents. Guaranteed not
	 * 		   to be null.
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return name;
	}
}
