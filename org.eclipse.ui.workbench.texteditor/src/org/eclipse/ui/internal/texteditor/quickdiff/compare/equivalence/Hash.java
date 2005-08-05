package org.eclipse.ui.internal.texteditor.quickdiff.compare.equivalence;

/**
 * Value objects. Subclasses must override <code>equals</code> and are
 * typically <code>final</code>.
 * 
 * @since 3.2
 */
public abstract class Hash implements Cloneable {
	
	public Object clone() {
		try {
			return super.clone();
		} catch (CloneNotSupportedException x) {
			throw new AssertionError(x);
		}
	}
	
	/**
	 * Returns <code>true</code> if the two hashes are equal,
	 * <code>false</code> if not. Subclasses must override.
	 * 
	 * @param obj {@inheritDoc}
	 * @return <code>true</code> if the receiver is equal to <code>obj</code>
	 */
	public boolean equals(Object obj) {
		throw new AssertionError("Subclasses of " + Hash.class.toString() + "must override equals()."); //$NON-NLS-1$ //$NON-NLS-2$
	}
}