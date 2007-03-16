/*******************************************************************************
 * Copyright (c) 2002, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.cheatsheets.data;

import java.util.ArrayList;

public class RepeatedSubItem extends AbstractSubItem implements ISubItemItem {
	private String values;
	private ArrayList subItems;

	/**
	 * Constructor for RepeatedSubItem.
	 */
	public RepeatedSubItem() {
		super();
	}
	
	public RepeatedSubItem(String values) {
		super();
		this.values = values;
	}
	
	/**
	 * Returns the values.
	 * @return String
	 */
	public String getValues() {
		return values;
	}

	/**
	 * Sets the values.
	 * @param newValues The new values to set
	 */
	public void setValues(String newValues) {
		this.values = newValues;
	}

	/**
	 * @param subItem the SubItem to add.
	 */
	public void addSubItem(AbstractSubItem subItem) {
		if(subItems == null) {
			subItems = new ArrayList();
		}
		subItems.add(subItem);
	}

	/**
	 * Returns a list which will always only contain at most 1 entry.
	 * @return Returns the subItems.
	 */
	public ArrayList getSubItems() {
		return subItems;
	}
}
