/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp. and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
	IBM Corporation - Initial implementation
**********************************************************************/

package org.eclipse.ui.internal;

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.PlatformUI;

/**
 * This class is used to record "open editor" actions as they
 * happen.  The input and type of each editor are recorded so that
 * the user can reopen an item from the recently used files list.
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
		if (newItem.isRestored()) {
			remove(newItem.getInput());
		}

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
		fifoList.toArray(array);
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
	 * Only restored items are considered.
	 */
	public void refresh() {
		Iterator iter = fifoList.iterator();
		while (iter.hasNext()) {
			EditorHistoryItem item = (EditorHistoryItem) iter.next();
			if (item.isRestored()) {
				IEditorInput input = item.getInput();
				if (input != null && !input.exists())
					iter.remove();
			}
		}
	}

	/**
	 * Removes the given history item.
	 */
	public void remove(EditorHistoryItem item) {
		fifoList.remove(item);
	}

	/**
	 * Removes all traces of an editor input from the history.
	 */
	public void remove(IEditorInput input) {
		if (input == null) {
			return;
		}
		Iterator iter = fifoList.iterator();
		while (iter.hasNext()) {
			EditorHistoryItem item = (EditorHistoryItem) iter.next();
			if (item.matches(input)) {
				iter.remove();
			}
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
	public IStatus restoreState(IMemento memento) {
		IMemento[] mementos = memento.getChildren(IWorkbenchConstants.TAG_FILE);
		for (int i = 0; i < mementos.length; i++) {
			EditorHistoryItem item = new EditorHistoryItem(mementos[i]);
			if (!"".equals(item.getName()) || !"".equals(item.getToolTipText())) { //$NON-NLS-1$ //$NON-NLS-2$
				add(item, fifoList.size());
			}
		}
		return new Status(IStatus.OK,PlatformUI.PLUGIN_ID,0,"",null); //$NON-NLS-1$
	}
	
	/**
	 * Save the most-recently-used history in the given memento.
	 * 
	 * @param memento the memento to save the mru history in
	 */
	public IStatus saveState(IMemento memento) {
		Iterator iterator = fifoList.iterator();
		while (iterator.hasNext()) {
			EditorHistoryItem item = (EditorHistoryItem) iterator.next();
			if (item.canSave()) {
				IMemento itemMemento = memento.createChild(IWorkbenchConstants.TAG_FILE);
				item.saveState(itemMemento);
			}
		}
		return new Status(IStatus.OK,PlatformUI.PLUGIN_ID,0,"",null); //$NON-NLS-1$
	}
}