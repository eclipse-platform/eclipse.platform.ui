package org.eclipse.ui.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.ui.*;

/**
 * This class is used to record "open editor" actions as they
 * happen.  The input and type of each edior are recorded so that
 * the user can reopen an item from the "File Most Recently Used"
 * list. 
 */
public class EditorHistory {
	private int depth;
	private ArrayList stack;
	/**
	 * Constructs a new history.
	 */
	public EditorHistory() {
		this(10);
	}
	/**
	 * Constructs a new history.
	 */
	public EditorHistory(int depth) {
		this.depth = depth;
		stack = new ArrayList(depth);
	}
	/**
	 * Adds an item to the history.
	 */
	public void add(IEditorInput input) {
		add(input, null);
	}
	/**
	 * Adds an item to the history.
	 */
	public void add(IEditorInput input, IEditorDescriptor desc) {
		add(new EditorHistoryItem(input, desc));
	}
	/**
	 * Adds an item to the history.
	 */
	private void add(EditorHistoryItem item) {
		// Remove old item.
		remove(item.getInput());

		// Add the new item.
		stack.add(item);
		if (stack.size() > depth) {
			stack.remove(0);
		}
	}
	/**
	 * Returns an array of editor history items.  The items are returned in order
	 * of most recent first.
	 */
	public EditorHistoryItem[] getItems() {
		refresh();
		EditorHistoryItem[] array = new EditorHistoryItem[stack.size()];
		int length = array.length;
		for (int nX = 0; nX < length; nX++) {
			array[nX] = (EditorHistoryItem) stack.get(length - 1 - nX);
		}
		return array;
	}
	/**
	 * Returns the stack height.
	 */
	public int getSize() {
		return stack.size();
	}
	/**
	 * Refresh the editor list.  Any stale items are removed.
	 */
	public void refresh() {
		Iterator iter = stack.iterator();
		while (iter.hasNext()) {
			EditorHistoryItem item = (EditorHistoryItem) iter.next();
			if (!item.getInput().exists())
				iter.remove();
		}
	}
	/**
	 * Removes all traces of an editor input from the history.
	 */
	public void remove(IEditorInput input) {
		Iterator iter = stack.iterator();
		while (iter.hasNext()) {
			EditorHistoryItem item = (EditorHistoryItem) iter.next();
			if (input.equals(item.getInput()))
				iter.remove();
		}
	}
	/**
	 * Reset the editor history to have the specified size.  Trim the list if
	 * it does not conforms to the new size.
	 */
	public void reset(int depth) {
		this.depth = depth;
		while (stack.size() > depth) {
			stack.remove(0);
		}
	}
	/**
	 * Restore the most-recently-used history from the given memento.
	 * 
	 * @param memento the memento to restore the mru history from
	 */
	public void restoreState(IMemento memento) {
		IMemento[] mementos = memento.getChildren(IWorkbenchConstants.TAG_FILE);
		for (int i = 0; i < mementos.length; i++) {
			EditorHistoryItem item = new EditorHistoryItem();
			item.restoreState(mementos[i]);
			if (item.getInput() != null) {
				add(item);
			}
		}
	}
	/**
	 * Save the most-recently-used history in the given memento.
	 * 
	 * @param memento the memento to save the mru history in
	 */
	public void saveState(IMemento memento) {
		Iterator iterator = stack.iterator();

		while (iterator.hasNext()) {
			EditorHistoryItem historyItem = (EditorHistoryItem) iterator.next();
			IMemento itemMemento = memento.createChild(IWorkbenchConstants.TAG_FILE);
			historyItem.saveState(itemMemento);
		}
	}
}