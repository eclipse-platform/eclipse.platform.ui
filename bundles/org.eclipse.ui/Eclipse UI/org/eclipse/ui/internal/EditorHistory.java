package org.eclipse.ui.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.ui.*;
import java.util.*;

/**
 * This class is used to record "open editor" actions as they
 * happen.  The input and type of each edior are recorded so that
 * the user can reopen an item from the "File Most Recently Used"
 * list. 
 */
public class EditorHistory {
	final static private int DEFAULT_DEPTH = 15;
	private int depth;
	private ArrayList stack;
/**
 * Constructs a new history.
 */
public EditorHistory() {
	this(DEFAULT_DEPTH);
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
	// Remove old item.
	remove(input);

	// Add the new item.
	EditorHistoryItem item = new EditorHistoryItem(input, desc);
	stack.add(item);
	while (stack.size() > depth) {
		stack.remove(0);
	}
}

/**
 * Returns an array of editor history items.  The items are returned in order
 * of most recent first.
 */
public EditorHistoryItem [] getItems() {
	refresh();
	EditorHistoryItem [] array = new EditorHistoryItem[stack.size()];
	int length = array.length;
	for (int nX = 0; nX < length; nX ++) {
		array[nX] = (EditorHistoryItem)stack.get(length - 1 - nX);
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
		EditorHistoryItem item = (EditorHistoryItem)iter.next();
		if (!item.input.exists())
			iter.remove();
	}
}
/**
 * Removes all traces of an editor input from the history.
 */
public void remove(IEditorInput input) {
	Iterator iter = stack.iterator();
	while (iter.hasNext()) {
		EditorHistoryItem item = (EditorHistoryItem)iter.next();
		if (input.equals(item.input))
			iter.remove();
	}
}
}
