package org.eclipse.update.internal.ui.forms;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
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
