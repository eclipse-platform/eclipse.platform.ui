package org.eclipse.ui.internal.registry;

import org.eclipse.core.runtime.*;
import org.eclipse.ui.internal.model.WorkbenchAdapter;
import org.eclipse.ui.*;
import org.eclipse.ui.model.IWorkbenchAdapter;
import java.util.*;

/**
 * ActionSetRegistry
 */
public class ActionSetRegistry extends Object
{
	private static final String OTHER_CATEGORY = "Other";
	private ArrayList children = new ArrayList(10);
	private ArrayList categories = new ArrayList();
/**
 * ActionSetRegistry constructor comment.
 */
public ActionSetRegistry() {
	super();
	readFromRegistry();
}
/**
 * Adds an action set.
 */
public void addActionSet(ActionSetDescriptor desc) {
	children.add(desc);
}
/**
 * Finds and returns the registered action set with the given id.
 *
 * @param id the action set id 
 * @return the action set, or <code>null</code> if none
 * @see IActionSetDescriptor#getId
 */
public IActionSetDescriptor findActionSet(String id) {
	Iterator enum = children.iterator();
	while (enum.hasNext()) {
		IActionSetDescriptor desc = (IActionSetDescriptor)enum.next();
		if (desc.getId().equals(id))
			return desc;
	}
	return null;
}
/**
 * Find a category with a given name.
 */
public ActionSetCategory findCategory(String name) {
	String catName = OTHER_CATEGORY;
	if (name != null)
		catName = name;

	Iterator enum = categories.iterator();
	while (enum.hasNext()) {
		ActionSetCategory cat = (ActionSetCategory) enum.next();
		if (catName.equals(cat.getLabel()))
			return cat;
	}

	ActionSetCategory cat = new ActionSetCategory(catName);
	categories.add(cat);
	return cat;
}
/**
 * Returns a list of the action sets known to the workbench.
 *
 * @return a list of action sets
 */
public IActionSetDescriptor[] getActionSets() {
	int count = children.size();
	IActionSetDescriptor [] array = new IActionSetDescriptor[count];
	for (int nX = 0; nX < count; nX ++) {
		array[nX] = (IActionSetDescriptor)children.get(nX);
	}
	return array;
}
/**
 * Returns a list of action set categories.
 *
 * @return a list of action sets categories
 */
public ActionSetCategory[] getCategories() {
	int count = categories.size();
	ActionSetCategory[] array = new ActionSetCategory[count];
	for (int i = 0; i < count; i++) {
		array[i] = (ActionSetCategory)categories.get(i);
	}
	return array;
}
/**
 * Adds each action set in the registry to a particular category.
 * The category may be defined in xml.  If not, the action set is
 * added to the "other" category.
 */
public void mapActionSetsToCategories() {
	Iterator enum = children.iterator();
	while (enum.hasNext()) {
		IActionSetDescriptor desc = (IActionSetDescriptor) enum.next();
		ActionSetCategory cat = findCategory(desc.getCategory());
		cat.addActionSet(desc);
	}
}
/**
 * Read the registry.
 */
public void readFromRegistry() {
	ActionSetRegistryReader reader = new ActionSetRegistryReader();
	reader.readRegistry(Platform.getPluginRegistry(), this);
}
}
