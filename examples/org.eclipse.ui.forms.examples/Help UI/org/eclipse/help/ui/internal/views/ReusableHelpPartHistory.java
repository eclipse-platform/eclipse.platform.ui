/*
 * Created on Dec 14, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.eclipse.help.ui.internal.views;

import java.util.LinkedList;

/**
 * @author dejan
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class ReusableHelpPartHistory {
	private LinkedList queue;
	private int cursor = -1;
	private int capacity = 50;
	private boolean blocked;
	/**
	 * 
	 */
	public ReusableHelpPartHistory() {
		queue = new LinkedList();
	}

	public void addEntry(HistoryEntry entry) {
		if (cursor!= -1) {
			// If we are adding a new entry while 
			// the cursor is not at the end, discard
			// all the entries after the cursor.
			int extra = queue.size()-1 -cursor;
			if (extra>0) {
				for (int i=extra; i>0; i--) {
					queue.removeLast();
				}
			}
		}
		queue.add(entry);
		if (queue.size()>capacity)
			queue.removeFirst();
		cursor = queue.size()-1;
	}

	public boolean hasNext() {
		return cursor != -1 && cursor < queue.size()-1;
	}

	public boolean hasPrev() {
		return cursor != -1 && cursor > 0;
	}

	public HistoryEntry next() {
		if (hasNext()) {
			return (HistoryEntry)queue.get(++cursor);
		}
		else
			return null;
	}
	public HistoryEntry prev() {
		if (hasPrev()) {
			return (HistoryEntry)queue.get(--cursor);
		}
		else
			return null;
	}
	/**
	 * @return Returns the blocked.
	 */
	public boolean isBlocked() {
		return blocked;
	}
	/**
	 * @param blocked The blocked to set.
	 */
	public void setBlocked(boolean blocked) {
		this.blocked = blocked;
	}
}