package org.eclipse.update.internal.ui.forms;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.util.*;

public class DetailsHistory {
	private final static int MAX_SIZE = 50;
	private LinkedList history;
	private int current = -1;
	
	public DetailsHistory() {
		history = new LinkedList();
	}
	
	private void resetIterator() {
		current = history.size() -1;
	}
	
	public void add(DetailsHistoryItem item) {
		if (history.isEmpty() ||
		    history.getLast().equals(item)==false) {
			//System.out.println("Item added: "+item);
			history.addLast(item);
			if (history.size() > MAX_SIZE)
		   		history.removeFirst();
		    }
		resetIterator();
	}
	public void add(String pageId, Object input) {
		this.add(new DetailsHistoryItem(pageId, input));
	}
	public boolean hasNext() {
		//if (iterator==null) return false;
		//return iterator.hasNext();
		if (current== -1) return false;
		if (current == history.size()-1) return false;
		return true;
	}
	public boolean hasPrevious() {
		//if (iterator==null) return false;
		//return iterator.hasPrevious();
		if (current == -1) return false;
		return (current>0);
	}
	public DetailsHistoryItem getNext() {
		if (hasNext()) {
			DetailsHistoryItem item = (DetailsHistoryItem)history.get(++current);
			//System.out.println("Next returned: "+item);
			return item;
		}
		return null;
	}
	public DetailsHistoryItem getPrevious() {
		if (hasPrevious()) {
			DetailsHistoryItem item = (DetailsHistoryItem)history.get(--current);
			//System.out.println("Prev returned: "+item);
			return item;
		}
		return null;
	}
}