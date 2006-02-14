/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.internal.databinding.api.observable.list;

/**
 * Standard implementation of IListDiffEntry.
 * 
 * @since 1.0
 */
public class ListDiffEntry implements IListDiffEntry {

	private int position;
	private boolean isAddition;
	private Object element;

	/**
	 * @param position
	 * @param isAddition
	 * @param element
	 */
	public ListDiffEntry(int position, boolean isAddition, Object element) {
		this.position = position;
		this.isAddition = isAddition;
		this.element = element;
	}

	public int getPosition() {
		return position;
	}

	public boolean isAddition() {
		return isAddition;
	}

	public Object getElement() {
		return element;
	}

}
