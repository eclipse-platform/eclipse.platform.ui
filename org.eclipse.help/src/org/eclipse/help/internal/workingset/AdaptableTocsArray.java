package org.eclipse.help.internal.workingset;

/*
 * (c) Copyright IBM Corp. 2002. 
 * All Rights Reserved.
 */

import java.util.*;

import org.eclipse.core.runtime.*;
import org.eclipse.help.*;

/**
 * Makes help resources adaptable and persistable
 */
public class AdaptableTocsArray implements IAdaptable {

	IToc[] element;
	AdaptableToc[] children;

	/**
	 * This constructor will be called when wrapping help resources.
	 */
	AdaptableTocsArray(IToc[] tocs) {
		this.element = tocs;
	}

	/**
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	public Object getAdapter(Class adapter) {
		if (adapter == IToc[].class)
			return element;
		else
			return null;
	}

	public IAdaptable[] getChildren() {

		if (children == null) {
			children = new AdaptableToc[element.length];
			for (int i = 0; i < element.length; i++) {
				children[i] = new AdaptableToc(element[i]);
				children[i].setParent(this);
			}
		}
		return children;

	}

	IToc[] asArray() {
		return element;
	}

	/**
	 * Tests the receiver and the object for equality
	 * 
	 * @param object object to compare the receiver to
	 * @return true=the object equals the receiver, the name is the same.
	 * 	false otherwise
	 */
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}
		if (!(object instanceof AdaptableTocsArray)) {
			return false;
		}

		AdaptableTocsArray res = (AdaptableTocsArray) object;
		return (Arrays.equals(asArray(), res.asArray()));

	}

	/**
	* Returns the hash code.
	* 
	* @return the hash code.
	*/
	public int hashCode() {
		if (element == null)
			return -1;
		else
			return element.hashCode();
	}
}

