/*
 * Licensed Materials - Property of IBM
 * (c) Copyright IBM Corporation 2000, 2003.
 * All Rights Reserved. 
 * Note to U.S. Government Users Restricted Rights:  Use, duplication or disclosure restricted by GSA ADP  schedule Contract with IBM Corp. 
*/

package org.eclipse.ui.internal.cheatsheets.data;

import org.eclipse.ui.cheatsheets.ISubItem;

public class SubContentItem extends ActionItem implements ISubItem {
	
	private String label;

	public SubContentItem() {
		super();
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String string) {
		label = string;
	}

}
