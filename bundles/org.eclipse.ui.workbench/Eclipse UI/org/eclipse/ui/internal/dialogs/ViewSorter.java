/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.dialogs;

import org.eclipse.ui.internal.registry.*;
import org.eclipse.jface.viewers.*;

/**
 * This is used to sort views in a ShowViewDialog.
 */
public class ViewSorter extends ViewerSorter {
	private ViewRegistry viewReg;
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
		String str1 = DialogUtil.removeAccel(((IViewDescriptor)e1).getLabel());
		String str2 = DialogUtil.removeAccel(((IViewDescriptor)e2).getLabel());
		return collator.compare(str1, str2);
	} else if (e1 instanceof ICategory) {
		if (e1 == viewReg.getMiscCategory())
			return 1;
		if (e2 == viewReg.getMiscCategory())
			return -1;
		String str1 = DialogUtil.removeAccel(((ICategory)e1).getLabel());
		String str2 = DialogUtil.removeAccel(((ICategory)e2).getLabel());
		return collator.compare(str1, str2);
	}
	return 0;
}
}
