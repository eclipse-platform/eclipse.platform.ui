package org.eclipse.ui.internal.dialogs;

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
		String str1 = removeAccel(((ActionSetCategory)e1).getLabel());
		String str2 = removeAccel(((ActionSetCategory)e2).getLabel());
		if (str1.equals(ActionSetDialogInput.STR_OTHER))
			return 1;
		if (str2.equals(ActionSetDialogInput.STR_OTHER))
			return -1;
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
