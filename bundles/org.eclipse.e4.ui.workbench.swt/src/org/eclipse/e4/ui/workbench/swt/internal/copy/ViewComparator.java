/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.workbench.swt.internal.copy;

import org.eclipse.e4.ui.model.application.descriptor.basic.MPartDescriptor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;

/**
 * Based on org.eclipse.ui.internal.dialogs.ViewComparator
 */
public class ViewComparator extends ViewerComparator {

	private static final String EMPTY_STRING = "";

	/**
	 * ViewSorter constructor comment.
	 */
	public ViewComparator() {
		super();
	}

	/**
	 * Returns a negative, zero, or positive number depending on whether the
	 * first element is less than, equal to, or greater than the second element.
	 */
	@Override
	public int compare(Viewer viewer, Object e1, Object e2) {
		// place "General" category first
		if (WorkbenchSWTMessages.ICategory_general.equals(e1))
			return -1;
		if (WorkbenchSWTMessages.ICategory_general.equals(e2))
			return 1;

		String str1;
		if (e1 instanceof MPartDescriptor)
			str1 = ((MPartDescriptor) e1).getLocalizedLabel();
		else
			str1 = e1.toString();

		String str2;
		if (e2 instanceof MPartDescriptor)
			str2 = ((MPartDescriptor) e2).getLocalizedLabel();
		else
			str2 = e2.toString();
		if (str1 == null)
			str1 = EMPTY_STRING;
		if (str2 == null)
			str2 = EMPTY_STRING;
		return getComparator().compare(str1, str2);
	}
}
