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
package org.eclipse.jface.internal.databinding.swt;

import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.swt.widgets.List;

/**
 * @since 3.2
 * 
 */
public class ListObservableList extends SWTObservableList {

	private final List list;

	/**
	 * @param list
	 */
	public ListObservableList(List list) {
		super(SWTObservables.getRealm(list.getDisplay()));
		this.list = list;
	}

	protected int getItemCount() {
		return list.getItemCount();
	}

	protected void setItems(String[] newItems) {
		list.setItems(newItems);
	}

	protected String[] getItems() {
		return list.getItems();
	}

	protected String getItem(int index) {
		return list.getItem(index);
	}

	protected void setItem(int index, String string) {
		list.setItem(index, string);
	}
}
