/*******************************************************************************
 * Copyright (c) 2006, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Matthew Hall - bugs 251884, 194734
 *******************************************************************************/

package org.eclipse.core.databinding.observable.set;

import java.util.Set;

import org.eclipse.core.databinding.observable.IDiff;

/**
 * @since 1.0
 *
 */
public abstract class SetDiff implements IDiff {
	
	/**
	 * @return the set of added elements
	 */
	public abstract Set getAdditions();
	
	/**
	 * @return the set of removed elements
	 */
	public abstract Set getRemovals();
	
	/**
	 * Returns true if the diff has no added or removed elements.
	 * 
	 * @return true if the diff has no added or removed elements.
	 * @since 1.2
	 */
	public boolean isEmpty() {
		return getAdditions().isEmpty() && getRemovals().isEmpty();
	}

	/**
	 * Applies the changes in this diff to the given set
	 * 
	 * @param set
	 *            the set to which the diff will be applied
	 * @since 1.2
	 */
	public void applyTo(Set set) {
		set.addAll(getAdditions());
		set.removeAll(getRemovals());
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer
			.append(getClass().getName())
			.append("{additions [") //$NON-NLS-1$
			.append(getAdditions() != null ? getAdditions().toString() : "null") //$NON-NLS-1$
			.append("], removals [") //$NON-NLS-1$
			.append(getRemovals() != null ? getRemovals().toString() : "null") //$NON-NLS-1$
			.append("]}"); //$NON-NLS-1$
		
		return buffer.toString();
	}
}
