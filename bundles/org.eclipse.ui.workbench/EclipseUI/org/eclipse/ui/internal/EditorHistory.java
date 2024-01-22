/*******************************************************************************
 * Copyright (c) 2000, 2018 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal;

import java.util.ArrayList;
import java.util.Iterator;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IMemento;

/**
 * This class is used to record "open editor" actions as they happen. The input
 * and type of each editor are recorded so that the user can reopen an item from
 * the recently used files list.
 */
public class EditorHistory {
	/**
	 * The maximum of entries in the history.
	 */
	public static final int MAX_SIZE = 100;

	/**
	 * The list of editor entries, in FIFO order.
	 */
	private ArrayList<EditorHistoryItem> fifoList = new ArrayList<>(MAX_SIZE);

	/**
	 * Constructs a new history.
	 */
	public EditorHistory() {
		super();
	}

	/**
	 * Adds an item to the history. Added in fifo fashion.
	 */
	public void add(IEditorInput input, IEditorDescriptor desc) {
		if (input != null && input.exists()) {
			add(new EditorHistoryItem(input, desc), 0);
		}
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

		// Remove the oldest one
		if (fifoList.size() == MAX_SIZE) {
			fifoList.remove(MAX_SIZE - 1);
		}

		// Add the new item.
		fifoList.add(index < MAX_SIZE ? index : MAX_SIZE - 1, newItem);
	}

	/**
	 * Returns an array of editor history items. The items are returned in order of
	 * most recent first.
	 */
	public EditorHistoryItem[] getItems() {
		refresh();
		EditorHistoryItem[] array = new EditorHistoryItem[fifoList.size()];
		fifoList.toArray(array);
		return array;
	}

	/**
	 * Refresh the editor list. Any stale items are removed. Only restored items are
	 * considered.
	 */
	public void refresh() {
		Iterator<EditorHistoryItem> iter = fifoList.iterator();
		while (iter.hasNext()) {
			EditorHistoryItem item = iter.next();
			if (item.isRestored()) {
				IEditorInput input = item.getInput();
				if (input != null && !input.exists()) {
					iter.remove();
				}
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
		Iterator<EditorHistoryItem> iter = fifoList.iterator();
		while (iter.hasNext()) {
			EditorHistoryItem item = iter.next();
			if (item.matches(input)) {
				iter.remove();
			}
		}
	}

	/**
	 * Restore the most-recently-used history from the given memento.
	 *
	 * @param memento the memento to restore the mru history from
	 */
	public IStatus restoreState(IMemento memento) {
		for (IMemento childMemento : memento.getChildren(IWorkbenchConstants.TAG_FILE)) {
			EditorHistoryItem item = new EditorHistoryItem(childMemento);
			if (!"".equals(item.getName()) || !"".equals(item.getToolTipText())) { //$NON-NLS-1$ //$NON-NLS-2$
				add(item, fifoList.size());
			}
		}
		return Status.OK_STATUS;
	}

	/**
	 * Save the most-recently-used history in the given memento.
	 *
	 * @param memento the memento to save the mru history in
	 */
	public IStatus saveState(IMemento memento) {
		for (EditorHistoryItem item : fifoList) {
			if (item.canSave()) {
				IMemento itemMemento = memento.createChild(IWorkbenchConstants.TAG_FILE);
				item.saveState(itemMemento);
			}
		}
		return Status.OK_STATUS;
	}
}
