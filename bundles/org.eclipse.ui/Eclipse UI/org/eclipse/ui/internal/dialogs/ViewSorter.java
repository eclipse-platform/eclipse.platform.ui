package org.eclipse.ui.internal.dialogs;

import org.eclipse.ui.internal.registry.*;
import org.eclipse.jface.viewers.*;

/**
 * This is used to sort views in a ShowViewDialog.
 */
public class ViewSorter extends ViewerSorter {
	ViewRegistry viewReg;
/**
 * ViewSorter constructor comment.
 */
public ViewSorter(ViewRegistry reg) {
	super();
	viewReg = reg;
}
/**
 * Returns a negative, zero, or positive number depending on whether
 * the first element is less than, equal to, or greater than
 * the second element.
 */
public int compare(Viewer viewer, Object e1, Object e2) {
	if (e1 instanceof IViewDescriptor) {
		String str1 = removeAccel(((IViewDescriptor)e1).getLabel());
		String str2 = removeAccel(((IViewDescriptor)e2).getLabel());
		return str1.compareTo(str2);
	} else if (e1 instanceof ICategory) {
		if (e1 == viewReg.getMiscCategory())
			return 1;
		if (e2 == viewReg.getMiscCategory())
			return -1;
		String str1 = removeAccel(((ICategory)e1).getLabel());
		String str2 = removeAccel(((ICategory)e2).getLabel());
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
