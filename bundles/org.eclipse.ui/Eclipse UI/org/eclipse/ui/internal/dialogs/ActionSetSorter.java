package org.eclipse.ui.internal.dialogs;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.ui.internal.registry.*;
import org.eclipse.jface.viewers.*;

/**
 * This is used to sort views in a ShowViewDialog.
 */
public class ActionSetSorter extends ViewerSorter {
/**
 * Construct new sorter.
 */
public ActionSetSorter() {
	super();
}
/**
 * Returns a negative, zero, or positive number depending on whether
 * the first element is less than, equal to, or greater than
 * the second element.
 */
public int compare(Viewer viewer, Object e1, Object e2) {
	if (e1 instanceof IActionSetDescriptor) {
		String str1 = removeAccel(((IActionSetDescriptor)e1).getLabel());
		String str2 = removeAccel(((IActionSetDescriptor)e2).getLabel());
		return str1.compareTo(str2);
	} else if (e1 instanceof ActionSetCategory) {
		ActionSetCategory cat1 = (ActionSetCategory)e1;
		ActionSetCategory cat2 = (ActionSetCategory)e2;
		if (cat1.getId().equals(ActionSetRegistry.OTHER_CATEGORY))
			return 1;
		if (cat2.getId().equals(ActionSetRegistry.OTHER_CATEGORY))
			return -1;
		String str1 = cat1.getLabel();
		String str2 = cat2.getLabel();
		return str1.compareTo(str2);
	}
	return 0;
}
/**
 * Removes the accelerator from a menu label.
 */
private String removeAccel(String label) {
	int aruga = label.indexOf('&');
	if (aruga >= 0)
		label = label.substring(aruga + 1);
	return label;
}
}
