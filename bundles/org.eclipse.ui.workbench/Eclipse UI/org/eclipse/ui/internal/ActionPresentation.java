package org.eclipse.ui.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.SubActionBars;
import org.eclipse.ui.internal.registry.IActionSet;
import org.eclipse.ui.internal.registry.IActionSetDescriptor;

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
	ArrayList sets = new ArrayList();
	while (iter.hasNext()) {
		IActionSetDescriptor desc = (IActionSetDescriptor)iter.next();
		if (!mapDescToRec.containsKey(desc)) {
			try {
				IActionSet set = desc.createActionSet();
				SubActionBars bars = new ActionSetActionBars(window.getActionBars(),
					desc.getId());
				SetRec rec = new SetRec(desc, set, bars);
				mapDescToRec.put(desc, rec);
				set.init(window, bars);
				sets.add(set);
			} catch (CoreException e) {
				WorkbenchPlugin.log("Unable to create ActionSet: " + desc.getId());//$NON-NLS-1$
			}
		}
	}
	// We process action sets in two passes for coolbar purposes.  First we process base contributions
	// (i.e., actions that the action set contributes to its toolbar), then we process adjunct contributions
	// (i.e., actions that the action set contributes to other toolbars).  This type of processing is
	// necessary in order to handle beforeGroup specification of adjunct contributions.
	PluginActionSetBuilder.processActionSets(sets, window);
	
	iter = sets.iterator();
	while (iter.hasNext()) {
		PluginActionSet set = (PluginActionSet)iter.next();
		set.getBars().activate();
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
