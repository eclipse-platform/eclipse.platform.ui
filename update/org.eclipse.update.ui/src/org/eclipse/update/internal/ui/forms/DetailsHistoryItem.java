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
package org.eclipse.update.internal.ui.forms;
public class DetailsHistoryItem {
	private String pageId;
	private Object input;
	
	public DetailsHistoryItem(String pageId, Object input) {
		this.pageId = pageId;
		this.input = input;
	}
	
	public String getPageId() {
		return pageId;
	}

	public Object getInput() {
		return input;
	}
	public boolean equals(Object itemObj) {
		if (itemObj instanceof DetailsHistoryItem) {
			DetailsHistoryItem item = (DetailsHistoryItem)itemObj;
			if (!pageId.equals(item.getPageId())) return false;
			if (input==null && item.getInput()==null) return true;
			if (input.equals(item.getInput())) return true;
		}
		return false;
	}
	public String toString() {
		return pageId+", "+input;
	}
}
