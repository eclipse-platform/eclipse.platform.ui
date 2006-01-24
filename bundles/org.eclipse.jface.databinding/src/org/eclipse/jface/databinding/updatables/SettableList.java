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

package org.eclipse.jface.databinding.updatables;

import java.util.List;

import org.eclipse.jface.databinding.ChangeEvent;
import org.eclipse.jface.databinding.UpdatableCollection;

/**
 * An updatable list that can be instantiated directly.
 * 
 * @since 3.2
 */
public class SettableList extends UpdatableCollection {
	
	private final List list;
	private final Class elementType;
	
	/**
	 * A java.util.List updatable collection that can be instantiated directly.
	 * 
	 * @param list The list to wrap.
	 * @param elementType The type of elements inside the list.
	 */
	public SettableList(List list, Class elementType) {
		this.list = list;
		this.elementType = elementType;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.databinding.UpdatableCollection#getSize()
	 */
	public int getSize() {
		return list.size();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.databinding.UpdatableCollection#addElement(java.lang.Object, int)
	 */
	public int addElement(Object value, int index) {
		list.add(index, value);
		fireChangeEvent(ChangeEvent.ADD, null, value, index);
		return index;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.databinding.UpdatableCollection#removeElement(int)
	 */
	public void removeElement(int index) {
		list.remove(index);
		fireChangeEvent(ChangeEvent.REMOVE, null, null, index);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.databinding.UpdatableCollection#setElement(int, java.lang.Object)
	 */
	public void setElement(int index, Object value) {
		list.set(index, value);
		fireChangeEvent(ChangeEvent.CHANGE, null, value, index);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.databinding.UpdatableCollection#getElement(int)
	 */
	public Object getElement(int index) {
		return list.get(index);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.databinding.UpdatableCollection#getElementType()
	 */
	public Class getElementType() {
		return elementType;
	}

}
