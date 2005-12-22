/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.internal.databinding;

import java.util.List;

import org.eclipse.jface.databinding.ChangeEvent;
import org.eclipse.jface.databinding.IUpdatableCollection;
import org.eclipse.jface.databinding.Updatable;

/**
 * @since 3.2
 *
 */
public class ListUpdatableCollection extends Updatable implements
		IUpdatableCollection {

	private final List elements;

	private boolean updating;

	/**
	 * @param elements
	 * @param elementType
	 */
	public ListUpdatableCollection(List elements, Class elementType) {
		this.elements = elements;
	}

	public int getSize() {
		return elements.size();
	}

	public int addElement(Object value, int index) {
		updating = true;
		try {
			elements.add(index, value);
			fireChangeEvent(ChangeEvent.ADD, null, value, index);
			return index;
		} finally {
			updating = false;
		}
	}

	public void removeElement(int index) {
		updating = true;
		try {
			Object oldValue = elements.remove(index);
			fireChangeEvent(ChangeEvent.REMOVE, oldValue, null, index);
		} finally {
			updating = false;
		}
	}

	public void setElement(int index, Object value) {
		updating = true;
		try {
			Object oldValue = elements.set(index, value);
			fireChangeEvent(ChangeEvent.CHANGE, oldValue, value, index);
		} finally {
			updating = false;
		}
	}

	public Object getElement(int index) {
		return elements.get(index);
	}

	public Class getElementType() {
		return Object.class;
	}

}
