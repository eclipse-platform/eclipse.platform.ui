/*******************************************************************************
 * Copyright (c) 2006, 2015 IBM Corporation and others.
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
package org.eclipse.ui.model;

import java.util.Comparator;

import org.eclipse.jface.viewers.IBasicPropertyConstants;
import org.eclipse.jface.viewers.ViewerComparator;

/**
 *
 * A viewer comparator that sorts elements with registered workbench adapters by
 * their text property. Note that capitalization differences are not considered
 * by this sorter, so a &gt; B &gt; c
 *
 * @see IWorkbenchAdapter
 * @since 3.3
 */
public class WorkbenchViewerComparator extends ViewerComparator {

	/**
	 * Creates a workbench viewer sorter using the default collator.
	 */
	public WorkbenchViewerComparator() {
		super();
	}

	/**
	 * Creates a workbench viewer sorter using the given collator.
	 *
	 * @param comparator the comparator to use to sort strings
	 */
	public WorkbenchViewerComparator(Comparator comparator) {
		super(comparator);
	}

	@Override
	public boolean isSorterProperty(Object element, String propertyId) {
		return propertyId.equals(IBasicPropertyConstants.P_TEXT);
	}
}
