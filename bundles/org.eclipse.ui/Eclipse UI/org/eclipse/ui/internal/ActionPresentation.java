package org.eclipse.ui.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.runtime.*;
import org.eclipse.ui.*;
import org.eclipse.ui.internal.registry.*;
import java.util.*;

/**
 * Manage the configurable actions for one window.
 */
public class ActionPresentation {
	private WorkbenchWindow window;
	private HashMap mapDescToRec = new HashMap(3);

	private class SetRec {
		public SetRec(IActionSetDescriptor desc, IActionSet set,
			SubActionBars bars) {
			this.desc = desc;
			this.set = set;
			this.bars = bars;
		}
		public IActionSetDescriptor desc;
		public IActionSet set;
		public SubActionBars bars;
	}
/**
 * ActionPresentation constructor comment.
 */
public ActionPresentation(WorkbenchWindow window) {
	super();
	this.window = window;
}
/**
 * Create an action set from a descriptor.
 */
public void addActionSet(IActionSetDescriptor desc) {
	try {
		IActionSet set = desc.createActionSet();
		SubActionBars bars = new ActionSetActionBars(window.getActionBars(),
			desc.getId());
		SetRec rec = new SetRec(desc, set, bars);
		mapDescToRec.put(desc, rec);
		set.init(window, bars);
		bars.activate();
	} catch (CoreException e) {
		WorkbenchPlugin.log("Unable to create ActionSet: " + desc.getId());//$NON-NLS-1$
	}
}
/**
 * Remove all action sets.
 */
public void clearActionSets() {
	List oldList = copyActionSets();
	Iterator iter = oldList.iterator();
	while (iter.hasNext()) {
		IActionSetDescriptor desc = (IActionSetDescriptor)iter.next();
		removeActionSet(desc);
	}
}
/**
 * Returns a copy of the visible action set.
 */
private List copyActionSets() {
	Set keys = mapDescToRec.keySet();
	ArrayList list = new ArrayList(keys.size());
	Iterator iter = keys.iterator();
	while (iter.hasNext()) {
		list.add(iter.next());
	}
	return list;
}
/**
 * Destroy an action set.
 */
public void removeActionSet(IActionSetDescriptor desc) {
	SetRec rec = (SetRec)mapDescToRec.get(desc);
	if (rec != null) {
		mapDescToRec.remove(desc);
		IActionSet set = rec.set;
		SubActionBars bars = rec.bars;
		bars.dispose();
		set.dispose();
	}
}
/**
 * Sets the list of visible action set.
 */
public void setActionSets(IActionSetDescriptor [] newArray) {
	// Convert array to list.
	List newList = Arrays.asList(newArray);
	List oldList = copyActionSets();

	// Remove obsolete actions.
	Iterator iter = oldList.iterator();
	while (iter.hasNext()) {
		IActionSetDescriptor desc = (IActionSetDescriptor)iter.next();
		if (!newList.contains(desc)) {
			removeActionSet(desc);
		}
	}
	
	// Add new actions.
	iter = newList.iterator();
	while (iter.hasNext()) {
		IActionSetDescriptor desc = (IActionSetDescriptor)iter.next();
		if (!mapDescToRec.containsKey(desc)) {
			addActionSet(desc);
		}
	}
}
/**
 */
public IActionSet[] getActionSets() {
	Collection setRecCollection = mapDescToRec.values();
	IActionSet result[] = new IActionSet[setRecCollection.size()];
	int i = 0;
	for (Iterator iterator = setRecCollection.iterator(); iterator.hasNext();i++)
		result[i] = ((SetRec)iterator.next()).set;
	return result;	
}
}
