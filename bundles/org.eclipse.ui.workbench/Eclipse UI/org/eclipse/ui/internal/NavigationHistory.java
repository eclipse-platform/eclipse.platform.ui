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

import org.eclipse.core.runtime.IAdaptable;

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IElementFactory;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.INavigationHistory;
import org.eclipse.ui.INavigationLocation;
import org.eclipse.ui.INavigationLocationProvider;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.XMLMemento;
import org.eclipse.ui.internal.dialogs.WorkInProgressPreferencePage;

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
	private int activeEntry;
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
				HistoryEntry firstEntry = null;
				while (e.hasNext()) {
					HistoryEntry entry= (HistoryEntry) e.next();
					if(id.equals(entry.editorID)) {
						if(input.equals(entry.editorInput)) {
							entry.mementoEntry = firstEntry;
							if (entry.handlePartClosed()) {
								if(firstEntry == null)
									firstEntry = entry;
							} else {
								e.remove();
								entry.dispose();
							}
						}
					}
				}
			}
		}
	});
}
/*
 * Adds an editor to the editor history without getting its location.
 */	
public void markEditor(IEditorPart part) {
	addEntry(part,false);
}
/*
 * (non-Javadoc)
 * Method declared on INavigationHistory.
 */	
public void markLocation(IEditorPart part) {
	addEntry(part,true);
}
/*
 * (non-Javadoc)
 * Method declared on INavigationHistory.
 */		
public INavigationLocation[] getLocations() {
	INavigationLocation result[] = new INavigationLocation[history.size()];
	for (int i= 0; i < result.length; i++) {
		HistoryEntry e= (HistoryEntry) history.get(i);
		result[i]= e.location;
	}
	return result;
}
/*
 * (non-Javadoc)
 * Method declared on INavigationHistory.
 */	
public INavigationLocation getCurrentLocation() {
	HistoryEntry entry = getEntry(activeEntry);
	return entry == null ? null : entry.location;
}
/**
 * Disposes this NavigationHistory and all entries. */
public void dispose() {
	Iterator e= history.iterator();
	while (e.hasNext()) {
		HistoryEntry entry= (HistoryEntry) e.next();
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
private HistoryEntry getEntry(int index) {
	if (0 <= index && index < history.size())
		return (HistoryEntry) history.get(index);
	return null;
}
/*
 * Adds the specified entry to the history. */
private void add(HistoryEntry entry) {
	removeForwardEntries();
	if(history.size() == CAPACITY) {
		HistoryEntry e= (HistoryEntry) history.remove(0);
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
		HistoryEntry e= (HistoryEntry) history.remove(activeEntry + 1);
		e.dispose();
	}
}
/*
 * Adds a location to the history. */
private void addEntry(IEditorPart part, boolean markLocation) {
	if(!WorkInProgressPreferencePage.useNavigationHistory())
		return;

	if (ignoreEntries > 0 || part == null)
		return;
	
	INavigationLocation location = null;	
	if(markLocation && part instanceof INavigationLocationProvider)
		location = ((INavigationLocationProvider)part).createNavigationLocation();
		
	HistoryEntry e= new HistoryEntry(page,part);
	e.location= location;
	HistoryEntry current= getEntry(activeEntry);
	if (current == null || !e.mergeInto(current)) {
		add(e);
	} else {
		removeForwardEntries();
	}
		
	updateActions();
}
/*
 * Prints all the entries in the console. For debug only. */
private void printEntries() {
	if (false) {
		int size= history.size();
		for (int i= 0; i < size; i++) {
			String append= activeEntry == i ? ">>" : "";
			System.out.println(append + "Index: " + i + " " + history.get(i));	
		};
	}
}
/*
 * Returns true if the forward action can be performed otherwise returns false. */
private boolean canForward() {
	return (0 <= activeEntry + 1) && (activeEntry + 1 < history.size());
}
/*
 * Returns true if the backward action can be performed otherwise returns false.
 */
private boolean canBackward() {
	return (0 <= activeEntry - 1) && (activeEntry - 1 < history.size());
}
/*
 * Update the actions enable/disable state. */
private void updateActions() {
	if (backwardAction != null)
		backwardAction.setEnabled(canBackward());
	if (forwardAction != null)
		forwardAction.setEnabled(canForward());
}
/*
 * Restore the specified entry */
private void gotoEntry(HistoryEntry entry) {
	if(entry == null)
		return;
	try {
		ignoreEntries++;
		entry.restoreLocation();
		updateActions();
		printEntries();		
	} finally {
		ignoreEntries--;
	}
}
/*
 * update the active entry */
private void updateEntry(HistoryEntry activeEntry) {
	if(activeEntry == null || activeEntry.location == null)
		return;
	activeEntry.location.update();
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
	HistoryEntry entry = getEntry(activeEntry);
	if (entry != null)
		gotoEntry(entry);
}
/**
 * Save the state of this history into the memento. */
public void saveState(IMemento memento) {
	HistoryEntry cEntry = (HistoryEntry)getEntry(activeEntry);
	if(cEntry == null || !cEntry.isPersistable())
		return;
		
	ArrayList list = new ArrayList(history.size());
	int size = history.size();
	for (int i = 0; i < size; i++) {
		HistoryEntry entry = (HistoryEntry)history.get(i);
		if(entry.isPersistable())
			list.add(entry);
	}
	size = list.size();
	for (int i = 0; i < size; i++) {
		HistoryEntry entry = (HistoryEntry)list.get(i);
		IMemento childMem = memento.createChild(IWorkbenchConstants.TAG_ITEM);
		if(entry == cEntry)
			childMem.putString(IWorkbenchConstants.TAG_ACTIVE,"true");
		entry.saveState(childMem,list);
	}
}
/**
 * Restore the state of this history from the memento. */
public void restoreState(IMemento memento) {
	IMemento items[] = memento.getChildren(IWorkbenchConstants.TAG_ITEM);
	if(items.length == 0)
		if(page.getActiveEditor() != null)
			markLocation(page.getActiveEditor());
			
	for (int i = 0; i < items.length; i++) {
		IMemento item = items[i];
		HistoryEntry entry = new HistoryEntry(page);
		history.add(entry);
		entry.restoreState(item,history);
		if(item.getString(IWorkbenchConstants.TAG_ACTIVE) != null)
			activeEntry = i;
	}
	gotoEntry(getEntry(activeEntry));
}
/*
 * Wraps the INavigationLocation and keeps the info
 * about the editor.
 */
private static class HistoryEntry {

	private IWorkbenchPage page;

	/* The entry will have the pair input and id or a memento that
	 * saves it or the entry that has one of the above.
	 * Only one of them (input, memento, mementoEntry) will be set at a time;
	 * the other two are null.
	 */		
	private String editorID;
	private IEditorInput editorInput;
	private IMemento memento;
	private HistoryEntry mementoEntry;
	
	/* Both may be set at the same time. */
	private INavigationLocation location;
	private IMemento locationMemento;
	
	/**
	 * Constructs a new HistoryEntry and intializes its editor input and editor id.	 */
	public HistoryEntry(IWorkbenchPage page,IEditorPart part) {
		this.page = page;
		editorID= part.getSite().getId();
		editorInput= part.getEditorInput();
	}
	/**
	 * Constructs a new HistoryEntry. <code>restoreState</code> should be called
	 * to intialize the state of this entry.
	 */
	public HistoryEntry(IWorkbenchPage page) {
		this.page = page;
	}
	/**
	 * Restores the state of the entry and the location if needed and then
	 * restores the location.	 */
	public void restoreLocation() {
		if(editorInput == null) {
			if(memento != null) {
				restoreEditor(memento);
				memento = null;
			} else if(mementoEntry != null) {
				if(mementoEntry.memento != null) {
					mementoEntry.restoreEditor(mementoEntry.memento);
					mementoEntry.memento = null;
				}
				editorID = mementoEntry.editorID;
				editorInput = mementoEntry.editorInput;
				mementoEntry = null;
			}
		}
			
		if (editorInput != null && editorID != null) {
			try {
				IEditorPart editor = page.openEditor(editorInput, editorID, true);
				if (location == null) {
					if(editor instanceof INavigationLocationProvider)
						location = ((INavigationLocationProvider)editor).createEmptyNavigationLocation();
				}
					
				if (location != null) {
					if (locationMemento != null) {
						location.setInput(editorInput);
						location.restoreState(locationMemento);
						locationMemento = null;
					}
					location.restoreLocation();
				}
			} catch (PartInitException e) {
				// ignore for now
			}
		}
	}
	/**
	 * Returns true if this entry can be persisted otherwise returns false;	 */
	public boolean isPersistable() {
		if(editorInput != null) {
			IPersistableElement persistable = editorInput.getPersistable();
			return persistable != null;
		}
		return memento != null || mementoEntry != null;
	}
	/** 
	 * Saves the state of this entry and its location.
	 * Returns true if possible otherwise returns false.	 */
	public boolean handlePartClosed() {
		if(!isPersistable())
			return false;
				
		if(mementoEntry == null && memento == null) {	
			IPersistableElement persistable = editorInput.getPersistable();	
			memento = XMLMemento.createWriteRoot(IWorkbenchConstants.TAG_EDITOR);
			memento.putString(IWorkbenchConstants.TAG_ID, editorID);
			memento.putString(IWorkbenchConstants.TAG_FACTORY_ID, persistable.getFactoryId());
			persistable.saveState(memento);
		}
		
		if (location != null) {
			locationMemento = XMLMemento.createWriteRoot(IWorkbenchConstants.TAG_POSITION);
			location.saveState(locationMemento);
			location.releaseState();
		}
		editorID= null;
		editorInput= null;
		return true;
	}
	/**
	 * Saves the state of this entry and its location.	 */
	public void saveState(IMemento mem, ArrayList entries) {
		if(editorInput != null) {
			int mementoEntryIndex = -1;
			int size = entries.size();
			for (int i = 0; i < size; i++) {
				HistoryEntry entry = (HistoryEntry)entries.get(i);
				if(entry == this)
					break;
				if(editorInput.equals(entry.editorInput) && editorID.equals(entry.editorID)) {
					mementoEntryIndex = i;
					break;
				}
			}
			if(mementoEntryIndex >= 0) { 
				IMemento childMem = mem.createChild(IWorkbenchConstants.TAG_INDEX);			
				childMem.putInteger(IWorkbenchConstants.TAG_INDEX,mementoEntryIndex);
			} else {			
				IPersistableElement persistable = editorInput.getPersistable();
				IMemento childMem = mem.createChild(IWorkbenchConstants.TAG_EDITOR);
				childMem.putString(IWorkbenchConstants.TAG_ID, editorID);
				childMem.putString(IWorkbenchConstants.TAG_FACTORY_ID, persistable.getFactoryId());
				persistable.saveState(childMem);
			}
		} else if(memento != null) {
			IMemento childMem = mem.createChild(IWorkbenchConstants.TAG_EDITOR);
			childMem.putMemento(memento);
		} else {
			int mementoEntryIndex = entries.indexOf(mementoEntry);
			IMemento childMem = mem.createChild(IWorkbenchConstants.TAG_INDEX);			
			childMem.putInteger(IWorkbenchConstants.TAG_INDEX,mementoEntryIndex);
		}
		
		if(locationMemento != null) {
			IMemento childMem = mem.createChild(IWorkbenchConstants.TAG_POSITION);
			childMem.putMemento(locationMemento);			
		} else if (location != null) {
			IMemento childMem = mem.createChild(IWorkbenchConstants.TAG_POSITION);
			location.saveState(childMem);
		}
	}
	/**
	 * Restore the state of this entry.	 */
	public void restoreState(IMemento mem,ArrayList entries) {
		memento = mem.getChild(IWorkbenchConstants.TAG_EDITOR);
		locationMemento = mem.getChild(IWorkbenchConstants.TAG_POSITION);;
		IMemento childMem = mem.getChild(IWorkbenchConstants.TAG_INDEX);
		if(childMem != null) {
			Integer index = childMem.getInteger(IWorkbenchConstants.TAG_INDEX);
			mementoEntry = (HistoryEntry)entries.get(index.intValue());
			memento = null;
		}
	}
	/*
	 * Restores the editor input and editor id.	 */
	private void restoreEditor(IMemento memento) {
		String factoryID= memento.getString(IWorkbenchConstants.TAG_FACTORY_ID);
		IElementFactory factory= WorkbenchPlugin.getDefault().getElementFactory(factoryID);
		if (factory != null) {
			IAdaptable element= factory.createElement(memento);
			if (element instanceof IEditorInput) {
				editorInput= (IEditorInput) element;
				editorID= memento.getString(IWorkbenchConstants.TAG_ID);
			}
		}
	}
	/*
 	 * (non-Javadoc)
     * Method declared on Object.
     */	
	public String toString() {
		return "Input<" + editorInput + "> Input<" + editorInput + "> Details<" + location + ">";
	}
	/**
	 * Disposes this entry and its location.	 */
	public void dispose() { 
		if (location != null)
			location.dispose();
	}
	/**
	 * Merges this entry into the current entry. Returns true
	 * if the merge was possible otherwise returns false.	 */
	public boolean mergeInto(HistoryEntry currentEntry) {
		if (editorInput != null && editorInput.equals(currentEntry.editorInput)) {
			if (location != null) {
				if (currentEntry.location == null) {
					currentEntry.location= location;
					return true;
				} else {
					return location.mergeInto(currentEntry.location);
				}
			}
		}
		return false; 
	}
};	
}
