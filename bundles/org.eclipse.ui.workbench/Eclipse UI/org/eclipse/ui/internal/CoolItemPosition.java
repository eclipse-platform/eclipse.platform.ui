/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal;

import java.util.ArrayList;
import org.eclipse.swt.graphics.Point;

/**
 * Class for remembering information about the position of a removed cool item.
 */
public class CoolItemPosition {

	boolean added = false; // indicates whether or not the item has been added back 

	String id; // id of the cool bar contribution item associated to the removed cool item

	CoolBarLayout layout; // the coolbar layout before the item is removed

	CoolItemPosition() {
	}
	CoolItemPosition(String id, CoolBarLayout layout) {
		this.id = id;
		this.layout = layout;
	}
	/**
	 * Return the items on the saved layout, an ArrayList of Strings (i.e., CoolBarContributionItem ids).
	 */
	ArrayList getItems() {
		return layout.items;
	}
	/**
	 * Return the row index for cbItemId.  The saved layout will be looked at and the index
	 * will be relative to this layout.
	 */
	int getRowOf(String cbItemId) {
		int index = layout.items.indexOf(cbItemId);
		if (index == -1)
			return -1;
		return layout.getRowOfIndex(index);
	}
	/**
	 * Return the CoolItem size for the CoolBarContributionItem with an id of cbItemId.
	 */
	Point getSizeOf(String cbItemId) {
		int index = layout.items.indexOf(cbItemId);
		if (index == -1)
			return null;
		return layout.itemSizes[index];
	}
}
