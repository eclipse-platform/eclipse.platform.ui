/*******************************************************************************
 * Copyright (c) 2005, 2015 IBM Corporation and others.
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
package org.eclipse.ui.internal.dialogs;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.ui.activities.WorkbenchActivityHelper;

/**
 * The CapabilityFilter is a filter that uses the capabilities support as filter
 * for items.
 */
public class CapabilityFilter extends ViewerFilter {

	/**
	 * Create a new instance of a capability filter.
	 */
	public CapabilityFilter() {
		super();

	}

	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		return !WorkbenchActivityHelper.filterItem(element);
	}

}
