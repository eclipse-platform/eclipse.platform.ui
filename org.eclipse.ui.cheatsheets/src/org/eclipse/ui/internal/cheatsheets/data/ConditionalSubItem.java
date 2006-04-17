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
import java.util.Iterator;

import org.eclipse.ui.internal.cheatsheets.views.CheatSheetManager;

public class ConditionalSubItem extends AbstractSubItem implements ISubItemItem {
	private String condition;
	private ArrayList subItems;
	private SubItem selectedSubItem;

	/**
	 * Constructor for ConditionalSubItem.
	 */
	public ConditionalSubItem() {
		super();
	}
	
	public ConditionalSubItem(String condition) {
		super();
		this.condition = condition;
	}
	
	/**
	 * Returns the condition.
	 * @return String
	 */
	public String getCondition() {
		return condition;
	}

	/**
	 * Sets the condition.
	 * @param newCondition The new condition to set
	 */
	public void setCondition(String newCondition) {
		this.condition = newCondition;
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
	 * @return Returns the subItems.
	 */
	public ArrayList getSubItems() {
		return subItems;
	}

	public SubItem getSelectedSubItem() {
		return selectedSubItem;
	}

	public void setSelectedSubItem(CheatSheetManager csm) {
		String conditionValue = csm.getVariableData(condition);

		for (Iterator iter = subItems.iterator(); iter.hasNext();) {
			SubItem subItem = (SubItem) iter.next();
			if(subItem.getWhen() != null && subItem.getWhen().equals(conditionValue)) {
				selectedSubItem = subItem;
				break;
			}
		}
	}
}
