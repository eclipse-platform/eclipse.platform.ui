package org.eclipse.ui.internal;

import java.text.Collator;
import java.util.*;
import org.eclipse.ui.*;

/**
 * This is used to store the MRU list of perspectives.
 */
public class PerspectiveHistory {

	final static private int DEFAULT_DEPTH = 7;
	private ArrayList shortcuts;
	private ArrayList sortedShortcuts;
	private IPerspectiveRegistry reg; 

	private Comparator comparator = new Comparator() {
		private Collator collator = Collator.getInstance();
		
		public int compare(Object ob1, Object ob2) {
			IPerspectiveDescriptor d1 = (IPerspectiveDescriptor)ob1;
			IPerspectiveDescriptor d2 = (IPerspectiveDescriptor)ob2;
			return collator.compare(d1.getLabel(), d2.getLabel());
		}
	};
	
	public PerspectiveHistory(IPerspectiveRegistry reg) {
		shortcuts = new ArrayList(DEFAULT_DEPTH);
		sortedShortcuts = new ArrayList(DEFAULT_DEPTH);
		this.reg = reg;
	}
	
	public void restoreState(IMemento memento) {
		// Read the shortcuts.
		IMemento [] children = memento.getChildren("desc");
		for (int x = 0; x < children.length; x ++) {
			IMemento childMem = children[x];
			String id = childMem.getID();
			IPerspectiveDescriptor desc = reg.findPerspectiveWithId(id);
			if (desc != null) 
				shortcuts.add(desc);
		}
		
		// Create sorted shortcuts.
		sortedShortcuts = (ArrayList)shortcuts.clone();
		Collections.sort(sortedShortcuts, comparator);
	}
	
	public void saveState(IMemento memento) {
		// Save the shortcuts.
		Iterator iter = shortcuts.iterator();
		while (iter.hasNext()) {
			IPerspectiveDescriptor desc = (IPerspectiveDescriptor)iter.next();
			memento.createChild("desc", desc.getId());
		}
	}

	public void add(IPerspectiveDescriptor desc) {
		// If the new desc is already in the shortcut list, just return.
		if (sortedShortcuts.contains(desc))
			return;
			
		// Add desc to shortcut lists.
		shortcuts.add(0, desc); // insert at top as most recent

		// If the shortcut list is too long then remove the oldest ones.
		int size = shortcuts.size();
		int preferredSize = DEFAULT_DEPTH;
		while (size > preferredSize) {
			shortcuts.remove(size - 1);
			-- size;
		}
		
		// Sort descriptor into ordered list.
		sortedShortcuts = (ArrayList)shortcuts.clone();
		Collections.sort(sortedShortcuts, comparator);
	}
	
	/**
	 * Returns an array list of IPerspectiveDescriptor objects.
	 */
	public ArrayList getItems() {
		return sortedShortcuts;
	}
}

