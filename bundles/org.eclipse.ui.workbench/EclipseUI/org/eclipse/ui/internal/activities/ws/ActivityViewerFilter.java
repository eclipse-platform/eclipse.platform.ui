/*******************************************************************************
 * Copyright (c) 2004, 2018 IBM Corporation and others.
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
package org.eclipse.ui.internal.activities.ws;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.ui.activities.WorkbenchActivityHelper;

/**
 * Generic viewer filter that works based on activity enablement.
 *
 * @since 3.0
 */
public class ActivityViewerFilter extends ViewerFilter {

	private boolean hasEncounteredFilteredItem = false;

	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		if (WorkbenchActivityHelper.filterItem(element)) {
			setHasEncounteredFilteredItem(true);
			return false;
		}
		return true;
	}

	/**
	 * @return returns whether the filter has filtered an item
	 */
	public boolean getHasEncounteredFilteredItem() {
		return hasEncounteredFilteredItem;
	}

	/**
	 * @param hasEncounteredFilteredItem sets whether the filter has filtered an
	 *                                   item
	 */
	public void setHasEncounteredFilteredItem(boolean hasEncounteredFilteredItem) {
		this.hasEncounteredFilteredItem = hasEncounteredFilteredItem;
	}
}
