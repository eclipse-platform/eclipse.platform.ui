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

import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.INavigationHistory;
import org.eclipse.ui.INavigationLocation;
import org.eclipse.ui.INavigationLocationProvider;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPart;

/**
 * 2.1 - WORK_IN_PROGRESS do not use.
 */
public class NavigationHistory implements INavigationHistory {
	
	private static final int CAPACITY= 50;
	
	private NavigationHistoryAction backwardAction;
	private NavigationHistoryAction forwardAction;
	private int ignoreEntries;
	
	private ArrayList history= new ArrayList(CAPACITY);
	private WorkbenchPage page;
	private int activeEntry = 0;
/**
 * Creates a new NavigationHistory to keep the NavigationLocation
 * entries of the specified page. */
public NavigationHistory(WorkbenchPage page) {
	this.page= page;
	page.addPartListener(new IPartListener() {
		public void partActivated(IWorkbenchPart part) {}
		public void partBroughtToTop(IWorkbenchPart part) {}
		public void partDeactivated(IWorkbenchPart part) {}
		public void partOpened(IWorkbenchPart part) {}
		
		public void partClosed(IWorkbenchPart part) {
			if (part instanceof IEditorPart) {
				IEditorPart editor= (IEditorPart) part;
				IEditorInput input = editor.getEditorInput();
				String id = editor.getSite().getId();
				Iterator e= history.iterator();
				NavigationHistoryEntry firstEntry = null;
				int i = 0;
				while (e.hasNext()) {
					NavigationHistoryEntry entry= (NavigationHistoryEntry) e.next();
					if(id.equals(entry.editorID)) {
						if(input.equals(entry.editorInput)) {
							entry.mementoEntry = firstEntry;
							if (entry.handlePartClosed()) {
								if(firstEntry == null)
									firstEntry = entry;
								i++;
							} else {
								// update the active entry since we are removing an item
								if (i < activeEntry) {
									activeEntry--;
								} else if (i == activeEntry) {
									if (i != 0) activeEntry--;
								} else {
									// activeEntry is before item we deleted
									i++;
								}
								e.remove();
								entry.dispose();
							}
						}
					}
				}
				updateActions();
			}
		}
	});
}
/*
 *Clear the current history.
 */
public void clear() {
	activeEntry = 0;
	for (int i= 0; i < history.size(); i++) {
		NavigationHistoryEntry e= (NavigationHistoryEntry) history.get(i);
		e.dispose();
	}
	history = new ArrayList();
	updateActions();
}
private Display getDisplay() {
	return page.getWorkbenchWindow().getShell().getDisplay();
}
/*
 * Adds an editor to the editor history without getting its location.
 */	
public void markEditor(final IEditorPart part) {
	if (ignoreEntries > 0 || part == null)
		return;
	/* Ignore all entries until the async exec runs. Workaround to avoid 
	 * extra entry when using Open Declaration (F3) that opens another editor. */
	ignoreEntries++;
	getDisplay().asyncExec(new Runnable() {
		public void run() {
			ignoreEntries--;
			EditorSite site = (EditorSite)(part.getEditorSite());
			Control c = site.getPane().getControl();
			if(c == null || c.isDisposed())
				return;
			NavigationHistoryEntry e = getEntry(activeEntry);
			if (e != null && part.getEditorInput() != e.editorInput)
				updateEntry(e);
			addEntry(part,true);
		}
	});
}
/*
 * (non-Javadoc)
 * Method declared on INavigationHistory.
 */	
public void markLocation(IEditorPart part) {
	addEntry(part,true);
}
/*
 * Return the backward history entries.  Return in restore order (i.e., the
 * first entry is the entry that would become active if the "Backward" action 
 * was executed).
 */
/* package */ NavigationHistoryEntry[] getBackwardEntries() {
	int length = activeEntry;
	NavigationHistoryEntry[] entries = new NavigationHistoryEntry[length];
	for (int i=0; i<activeEntry; i++) {
		entries[activeEntry - 1 - i] = getEntry(i);
	}
	return entries;
}
/*
 * Return the forward history entries.  Return in restore order (i.e., the first
 * entry is the entry that would become active if the "Forward" action was
 * executed).
 */
/* package */ NavigationHistoryEntry[] getForwardEntries() {
 	int length = history.size() - activeEntry - 1;
 	length = Math.max(0, length);
 	NavigationHistoryEntry[] entries = new NavigationHistoryEntry[length];
 	for (int i=activeEntry + 1; i<history.size(); i++) {
 		entries[i - activeEntry - 1] = getEntry(i);
 	}
 	return entries;
}
/*
 * (non-Javadoc)
 * Method declared on INavigationHistory.
 */		
public INavigationLocation[] getLocations() {
	INavigationLocation result[] = new INavigationLocation[history.size()];
	for (int i= 0; i < result.length; i++) {
		NavigationHistoryEntry e= (NavigationHistoryEntry) history.get(i);
		result[i]=e.location;
	}
	return result;
}
/*
 * (non-Javadoc)
 * Method declared on INavigationHistory.
 */	
public INavigationLocation getCurrentLocation() {
	NavigationHistoryEntry entry = getEntry(activeEntry);
	return entry == null ? null : entry.location;
}
/**
 * Disposes this NavigationHistory and all entries. */
public void dispose() {
	Iterator e= history.iterator();
	while (e.hasNext()) {
		NavigationHistoryEntry entry= (NavigationHistoryEntry) e.next();
		entry.dispose();
	}
}
/**
 * Keeps a reference to the forward action to update its state
 * whenever needed. */
public void setForwardAction(NavigationHistoryAction action) {
	forwardAction= action;
	updateActions();
}
/**
 * Keeps a reference to the backward action to update its state
 * whenever needed.
 */
public void setBackwardAction(NavigationHistoryAction action) {
	backwardAction= action;
	updateActions();
}
/*
 * Returns the history entry indexed by <code>index</code>
 */
private NavigationHistoryEntry getEntry(int index) {
	if (0 <= index && index < history.size())
		return (NavigationHistoryEntry) history.get(index);
	return null;
}
/*
 * Adds the specified entry to the history. */
private void add(NavigationHistoryEntry entry) {
	removeForwardEntries();
	if(history.size() == CAPACITY) {
		NavigationHistoryEntry e= (NavigationHistoryEntry) history.remove(0);
		e.dispose();		
	}
	history.add(entry);
	activeEntry = history.size() - 1;
}
/*
 * Remove all entries after the active entry. */
private void removeForwardEntries() {
	int length= history.size();
	for (int i= activeEntry + 1; i < length; i++) {
		NavigationHistoryEntry e= (NavigationHistoryEntry) history.remove(activeEntry + 1);
		e.dispose();
	}
}
/*
 * Adds a location to the history. */
private void addEntry(IEditorPart part, boolean markLocation) {
	if (ignoreEntries > 0 || part == null)
		return;
	
	INavigationLocation location = null;	
	if(markLocation && part instanceof INavigationLocationProvider)
		location = ((INavigationLocationProvider)part).createNavigationLocation();
		
	NavigationHistoryEntry e= new NavigationHistoryEntry(page, part, location);
	NavigationHistoryEntry current= getEntry(activeEntry);
	if (current != null)
	current.restoreEditor();
	if (current == null || !e.mergeInto(current)) {
		add(e);
	} else {
		removeForwardEntries();
	}
	printEntries("added entry"); //$NON-NLS-1$
	updateActions();
}
/*
 * Prints all the entries in the console. For debug only. */
private void printEntries(String label) {
	if (false) {
		System.out.println("+++++ " + label + "+++++ "); //$NON-NLS-1$ //$NON-NLS-2$
		int size= history.size();
		for (int i= 0; i < size; i++) {
			String append= activeEntry == i ? ">>" : ""; //$NON-NLS-1$ //$NON-NLS-2$
			System.out.println(append + "Index: " + i + " " + history.get(i));	//$NON-NLS-1$ //$NON-NLS-2$
		};
	}
}
/*
 * Returns true if the forward action can be performed otherwise returns false. */
/* package */ boolean canForward() {
	return (0 <= activeEntry + 1) && (activeEntry + 1 < history.size());
}
/*
 * Returns true if the backward action can be performed otherwise returns false.
 */
/* package */ boolean canBackward() {
	return (0 <= activeEntry - 1) && (activeEntry - 1 < history.size());
}
/*
 * Update the actions enable/disable and tooltip state. */
private void updateActions() {
	if (backwardAction != null) backwardAction.update();
	if (forwardAction != null) forwardAction.update();
}
/*
 * Restore the specified entry */
private void gotoEntry(NavigationHistoryEntry entry) {
	if(entry == null)
		return;
	try {
		ignoreEntries++;
		entry.restoreLocation();
		updateActions();
		printEntries("goto entry");	//$NON-NLS-1$
	} finally {
		ignoreEntries--;
	}
}
/*
 * update the active entry */
private void updateEntry(NavigationHistoryEntry activeEntry) {
	if(activeEntry == null || activeEntry.location == null)
		return;
	activeEntry.location.update();
	printEntries("updateEntry"); //$NON-NLS-1$
}
	
/*
 * Perform the forward action by getting the next location and restoring
 * its context. */
public void forward() {
	if (canForward())
		shiftEntry(true);
}
/*
 * Perform the backward action by getting the privious location and restoring
 * its context.
 */
public void backward() {
	if (canBackward())
		shiftEntry(false);
}
/*
 * Shift the history back or forward */
private void shiftEntry(boolean forward) {
	updateEntry(getEntry(activeEntry));
	if(forward)
		activeEntry++;
	else
		activeEntry--;
	NavigationHistoryEntry entry = getEntry(activeEntry);
	if (entry != null)
		gotoEntry(entry);
}
/*
 * Shift the history to the given entry.
 */
protected void shiftCurrentEntry(NavigationHistoryEntry entry) {
	updateEntry(getEntry(activeEntry));
	activeEntry = history.indexOf(entry);
	gotoEntry(entry);
}
/**
 * Save the state of this history into the memento. */
/* package */ void saveState(IMemento memento) {
	NavigationHistoryEntry cEntry = (NavigationHistoryEntry)getEntry(activeEntry);
	if(cEntry == null || !cEntry.isPersistable())
		return;
		
	ArrayList list = new ArrayList(history.size());
	int size = history.size();
	for (int i = 0; i < size; i++) {
		NavigationHistoryEntry entry = (NavigationHistoryEntry)history.get(i);
		if(entry.isPersistable())
			list.add(entry);
	}
	size = list.size();
	for (int i = 0; i < size; i++) {
		NavigationHistoryEntry entry = (NavigationHistoryEntry)list.get(i);
		IMemento childMem = memento.createChild(IWorkbenchConstants.TAG_ITEM);
		if(entry == cEntry)
			childMem.putString(IWorkbenchConstants.TAG_ACTIVE,"true"); //$NON-NLS-1$
		entry.saveState(childMem,list);
	}
}
/**
 * Restore the state of this history from the memento. */
/* package */ void restoreState(IMemento memento) {
	IMemento items[] = memento.getChildren(IWorkbenchConstants.TAG_ITEM);
	if(items.length == 0) {
		if(page.getActiveEditor() != null)
			markLocation(page.getActiveEditor());
	} 
	boolean oldHistory = false;
	// track entries that are actually restored
	int j = 0;
	for (int i = 0; i < items.length; i++) {
		IMemento item = items[i];
		// Track whether or not we are dealing with a navigation history that has the
		// drop down list support.
		String entryText = item.getString(IWorkbenchConstants.TAG_HISTORY_LABEL);
		if (entryText == null) {
			oldHistory = true;
			break;
		}
		// Clear out history if "blank" entries.  Fix for 27317.
		if (entryText.equals("")) { //$NON-NLS-1$
			oldHistory = true;
			break;
		}
		NavigationHistoryEntry entry = new NavigationHistoryEntry(page);
		history.add(entry);
		entry.restoreState(item,history);
		if(item.getString(IWorkbenchConstants.TAG_ACTIVE) != null)
			activeEntry = j;
		j++;
	}
	if (oldHistory) {
		// If we have an old or invalid history (one that doesn't support the drop down 
		// list feature), clear the old history since it is not compatible with the 
		// drop down feature.
		history.clear();
		if(page.getActiveEditor() != null)
			markLocation(page.getActiveEditor());
		return;
	}	
	if(items.length != 0 && j == 0) {
		// no items were restored
		if(page.getActiveEditor() != null)
			markLocation(page.getActiveEditor());
	}
		 
	NavigationHistoryEntry entry = getEntry(activeEntry);
	if(entry != null && entry.editorInput != null) {
		if(page.getActiveEditor() == page.findEditor(entry.editorInput))
			gotoEntry(entry);
	}
}
}
