/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.core.databinding.observable.list;

/**
 * A single addition of an element to a list or removal of an element from a list.
 *  
 * @since 1.0
 */
public abstract class ListDiffEntry {
	
	/**
	 * @return the 0-based position of the addition or removal
	 */
	public abstract int getPosition();
	
	/**
	 * @return true if this represents an addition, false if this represents a removal
	 */
	public abstract boolean isAddition();
	
	/**
	 * @return the element that was added or removed
	 */
	public abstract Object getElement();
	
	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer
			.append(this.getClass().getName())
			.append("{position [") //$NON-NLS-1$
			.append(getPosition())
			.append("], isAddition [") //$NON-NLS-1$
			.append(isAddition())
			.append("], element [") //$NON-NLS-1$
			.append(getElement() != null ? getElement().toString() : "null") //$NON-NLS-1$
			.append("]}"); //$NON-NLS-1$
		
		return buffer.toString();
	}
}
