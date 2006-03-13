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
package org.eclipse.jface.internal.databinding.internal.swt;

import org.eclipse.swt.widgets.Combo;

/**
 * @since 3.2
 * 
 */
public class ComboObservableList extends SWTObservableList {

	private final Combo combo;

	/**
	 * @param combo
	 */
	public ComboObservableList(Combo combo) {
		this.combo = combo;
	}

	protected int getItemCount() {
		return combo.getItemCount();
	}

	protected void setItems(String[] newItems) {
		combo.setItems(newItems);
	}

	protected String[] getItems() {
		return combo.getItems();
	}

	protected String getItem(int index) {
		return combo.getItem(index);
	}

	protected void setItem(int index, String string) {
		combo.setItem(index, string);
	}
}
