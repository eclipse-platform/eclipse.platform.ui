package org.eclipse.ui.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.util.*;

import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.util.ListenerList;
import org.eclipse.ui.*;

/**
 * This is used to store the most recently used (MRU) list
 * of perspectives for the entire workbench.
 */
public class PerspectiveHistory {
	
	private static final int DEFAULT_DEPTH = 50;
	
	private ArrayList shortcuts;
	private IPerspectiveRegistry reg; 
	private ListenerList listeners = new ListenerList();

	public PerspectiveHistory(IPerspectiveRegistry reg) {
		this.shortcuts = new ArrayList(DEFAULT_DEPTH);
		this.reg = reg;
	}

	public void addListener(IPropertyListener l) {
		listeners.add(l);
	}	
	
	public void removeListener(IPropertyListener l) {
		listeners.remove(l);
	}	
	
	private void fireChange() {
		Object[] array = listeners.getListeners();
		for (int i = 0; i < array.length; i++) {
			IPropertyListener element = (IPropertyListener)array[i];
			element.propertyChanged(this, 0);
		}
	}
	
	public IStatus restoreState(IMemento memento) {
		IMemento [] children = memento.getChildren("desc"); //$NON-NLS-1$
		for (int i = 0; i < children.length && i < DEFAULT_DEPTH; i++) {
			IPerspectiveDescriptor desc =
				reg.findPerspectiveWithId(children[i].getID());
			if (desc != null) 
				shortcuts.add(desc);
		}
		return new Status(IStatus.OK,PlatformUI.PLUGIN_ID,0,"",null); //$NON-NLS-1$
	}
	
	public IStatus saveState(IMemento memento) {
		Iterator iter = shortcuts.iterator();
		while (iter.hasNext()) {
			IPerspectiveDescriptor desc = (IPerspectiveDescriptor)iter.next();
			memento.createChild("desc", desc.getId()); //$NON-NLS-1$
		}
		return new Status(IStatus.OK,PlatformUI.PLUGIN_ID,0,"",null); //$NON-NLS-1$
	}

	public void add(String id) {
		IPerspectiveDescriptor desc = reg.findPerspectiveWithId(id);
		if (desc != null) 
			add(desc);
	}
	
	public void add(IPerspectiveDescriptor desc) {
		// Avoid duplicates
		if (shortcuts.contains(desc))
			return;

		// If the shortcut list will be too long, remove oldest ones			
		int size = shortcuts.size();
		int preferredSize = DEFAULT_DEPTH;
		while (size >= preferredSize) {
			size--;
			shortcuts.remove(size);
		}
		
		// Insert at top as most recent
		shortcuts.add(0, desc);
		fireChange();
	}
	
	public void refreshFromRegistry() {
		boolean change = false;
		
		Iterator iter = shortcuts.iterator();
		while (iter.hasNext()) {
			IPerspectiveDescriptor desc = (IPerspectiveDescriptor)iter.next();
			if (reg.findPerspectiveWithId(desc.getId()) == null) {
				iter.remove();
				change = true;
			}
		}
		
		if (change)
			fireChange();
	}

	/**
	 * Copy the requested number of items from the history into
	 * the destination list at the given index.
	 * 
	 * @param dest destination list to contain the items
	 * @param destStart index in destination list to start copying items at
	 * @param count number of items to copy from history
	 * @return the number of items actually copied
	 */
	public int copyItems(List dest, int destStart, int count) {
		int itemCount = count;
		if (itemCount > shortcuts.size())
			itemCount = shortcuts.size();
			
		for (int i = 0; i < itemCount; i++)
			dest.add(destStart + i, shortcuts.get(i));
			
		return itemCount;
	} 
}

