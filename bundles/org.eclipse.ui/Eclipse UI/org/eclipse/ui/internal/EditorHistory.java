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
	private int size;
	private ArrayList fifoList;
	/**
	 * Constructs a new history.
	 */
	public EditorHistory() {
		this(10);
	}
	/**
	 * Constructs a new history.
	 */
	public EditorHistory(int size) {
		this.size = size;
		fifoList = new ArrayList(size);
	}
	/**
	 * Adds an item to the history.  Added in fifo fashion.
	 */
	public void add(IEditorInput input) {
		add(input, null);
	}
	/**
	 * Adds an item to the history.  Added in fifo fashion.
	 */
	public void add(IEditorInput input, IEditorDescriptor desc) {
		add(new EditorHistoryItem(input, desc), 0);
	}
	/**
	 * Adds an item to the history.
	 */
	private void add(EditorHistoryItem newItem, int index) {
		// Remove the item if it already exists so that it will be put 
		// at the top of the list.
		remove(newItem.getInput());

		// Add the new item.
		fifoList.add(index, newItem);
		if (fifoList.size() > size) {
			fifoList.remove(size);
		}
	}
	/**
	 * Returns an array of editor history items.  The items are returned in order
	 * of most recent first.
	 */
	public EditorHistoryItem[] getItems() {
		refresh();
		EditorHistoryItem[] array = new EditorHistoryItem[fifoList.size()];
		int length = array.length;
		for (int i = 0; i < length; i++) {
			array[i] = (EditorHistoryItem) fifoList.get(i);
		}
		return array;
	}
	/**
	 * Returns the stack height.
	 */
	public int getSize() {
		return fifoList.size();
	}
	/**
	 * Refresh the editor list.  Any stale items are removed.
	 */
	public void refresh() {
		Iterator iter = fifoList.iterator();
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
		Iterator iter = fifoList.iterator();
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
	public void reset(int size) {
		this.size = size;
		while (fifoList.size() > size) {
			fifoList.remove(size);
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
				add(item, fifoList.size());
			}
		}
	}
	/**
	 * Save the most-recently-used history in the given memento.
	 * 
	 * @param memento the memento to save the mru history in
	 */
	public void saveState(IMemento memento) {
		Iterator iterator = fifoList.iterator();

		while (iterator.hasNext()) {
			EditorHistoryItem historyItem = (EditorHistoryItem) iterator.next();
			IEditorInput editorInput = historyItem.getInput();
			
			if (editorInput != null && editorInput.getPersistable() != null) {
				IMemento itemMemento = memento.createChild(IWorkbenchConstants.TAG_FILE);
				historyItem.saveState(itemMemento);
			}
		}
	}
}