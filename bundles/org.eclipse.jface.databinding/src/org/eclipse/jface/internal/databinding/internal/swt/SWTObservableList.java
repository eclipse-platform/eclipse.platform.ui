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
package org.eclipse.jface.internal.databinding.internal.swt;

import org.eclipse.core.databinding.BindingException;
import org.eclipse.core.databinding.observable.Diffs;
import org.eclipse.core.databinding.observable.ObservableTracker;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.list.AbstractObservableList;

/**
 * Abstract base class of CComboObservableList, ComboObservableList, and
 * ListObservableList.
 * 
 * @since 3.2
 * 
 */
public abstract class SWTObservableList extends AbstractObservableList {

	/**
	 * 
	 */
	public SWTObservableList() {
		super();
	}

	/**
	 * @param realm
	 */
	public SWTObservableList(Realm realm) {
		super(realm);
	}

	public void add(int index, Object element) {
		int size = doGetSize();
		if (index < 0 || index > size)
			index = size;
		String[] newItems = new String[size + 1];
		System.arraycopy(getItems(), 0, newItems, 0, index);
		newItems[index] = (String) element;
		System.arraycopy(getItems(), index, newItems, index + 1, size - index);
		setItems(newItems);
		fireListChange(Diffs.createListDiff(Diffs.createListDiffEntry(index,
				true, element)));
	}

	public int doGetSize() {
		return getItemCount();
	}

	public Object get(int index) {
		getterCalled();
		return getItem(index);
	}

	public Object getElementType() {
		return String.class;
	}

	/**
	 * @param index
	 * @return the item at the given index
	 */
	protected abstract String getItem(int index);

	/**
	 * @return the item count
	 */
	protected abstract int getItemCount();

	/**
	 * @return the items
	 */
	protected abstract String[] getItems();

	private void getterCalled() {
		ObservableTracker.getterCalled(this);
	}

	public Object remove(int index) {
		getterCalled();
		int size = doGetSize();
		if (index < 0 || index > size - 1)
			throw new BindingException(
					"Request to remove an element out of the collection bounds"); //$NON-NLS-1$

		String[] newItems = new String[size - 1];
		String oldElement = getItem(index);
		if (newItems.length > 0) {
			System.arraycopy(getItems(), 0, newItems, 0, index);
			if (size - 1 > index) {
				System.arraycopy(getItems(), index + 1, newItems, index, size
						- index - 1);
			}
		}
		setItems(newItems);
		fireListChange(Diffs.createListDiff(Diffs.createListDiffEntry(index,
				false, oldElement)));
		return oldElement;
	}

	public Object set(int index, Object element) {
		String oldElement = getItem(index);
		setItem(index, (String) element);
		fireListChange(Diffs.createListDiff(Diffs.createListDiffEntry(index,
				false, oldElement), Diffs.createListDiffEntry(index, true,
				element)));
		return oldElement;
	}

	/**
	 * @param index
	 * @param string
	 */
	protected abstract void setItem(int index, String string);

	/**
	 * @param newItems
	 */
	protected abstract void setItems(String[] newItems);

}
