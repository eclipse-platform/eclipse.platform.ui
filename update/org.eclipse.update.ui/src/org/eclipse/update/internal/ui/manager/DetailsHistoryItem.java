package org.eclipse.update.internal.ui.manager;

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
	DetailsHistoryItem nextItem;
}

