/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.internal.workingset;

import java.util.Arrays;
import java.util.HashMap;

import org.eclipse.core.runtime.*;
import org.eclipse.help.*;

/**
 * Makes help resources adaptable and persistable
 */
public class AdaptableTocsArray implements IAdaptable {

	IToc[] element;
	AdaptableToc[] children;
	HashMap<String, AdaptableToc> map;

	/**
	 * This constructor will be called when wrapping help resources.
	 */
	public AdaptableTocsArray(IToc[] tocs) {
		this.element = tocs;
	}

	/**
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	public Object getAdapter(Class adapter) {
		if (adapter == IToc[].class)
			return element;
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

	public AdaptableToc getAdaptableToc(String href) {
		if (map == null) {
			getChildren(); // make sure children are initialized
			map = new HashMap<String, AdaptableToc>(children.length);
			for (int i = 0; i < children.length; i++)
				map.put(children[i].getHref(), children[i]);
		}
		return (AdaptableToc) map.get(href);
	}

	IToc[] asArray() {
		return element;
	}

	/**
	 * Tests the receiver and the object for equality
	 * 
	 * @param object
	 *            object to compare the receiver to
	 * @return true=the object equals the receiver, the name is the same. false
	 *         otherwise
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
		return element.hashCode();
	}
}

