package org.eclipse.update.internal.ui.manager;

import java.util.*;

public class DetailsHistory {
	private final static int MAX_SIZE = 100;
	private LinkedList history;
	private ListIterator iterator;
	
	public DetailsHistory() {
		history = new LinkedList();
	}
	public void add(DetailsHistoryItem item) {
		history.addFirst(item);
		if (history.size() > MAX_SIZE)
		   history.removeLast();
		iterator = history.listIterator();
	}
	public void add(String pageId, Object input) {
		this.add(new DetailsHistoryItem(pageId, input));
	}
	public boolean hasNext() {
		return iterator.hasPrevious();
	}
	public boolean hasPrevious() {
		return iterator.hasNext();
	}
	public DetailsHistoryItem getNext() {
		if (iterator.hasPrevious()) {
			DetailsHistoryItem item = (DetailsHistoryItem)iterator.previous();
			return item;
		}
		return null;
	}
	public DetailsHistoryItem getPrevious() {
		if (iterator.hasNext()) {
			DetailsHistoryItem item = (DetailsHistoryItem)iterator.next();
			return item;
		}
		return null;
	}
}