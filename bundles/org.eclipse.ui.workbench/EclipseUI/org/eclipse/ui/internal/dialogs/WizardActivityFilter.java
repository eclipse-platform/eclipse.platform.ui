/*******************************************************************************
 * Copyright (c) 2004, 2015 IBM Corporation and others.
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
 *     Lars Vogel <Lars.Vogel@vogella.com> - Bug 472654
 *******************************************************************************/
package org.eclipse.ui.internal.dialogs;

import java.util.ArrayList;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.ui.activities.WorkbenchActivityHelper;
import org.eclipse.ui.model.AdaptableList;

/**
 * Viewer filter designed to work with the new wizard page (and its
 * input/content provider). This will filter all non-primary wizards that are
 * not enabled by activity.
 *
 * @since 3.0
 */
public class WizardActivityFilter extends ViewerFilter {
	private boolean filterPrimaryWizards = false;

	public void setFilterPrimaryWizards(boolean filter) {
		filterPrimaryWizards = filter;
	}

	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		if (parentElement.getClass().equals(AdaptableList.class) && !filterPrimaryWizards) {
			return true; // top-level ("primary") wizards should always be returned
		}

		if (WorkbenchActivityHelper.filterItem(element)) {
			return false;
		}

		return true;
	}

	@Override
	public Object[] filter(Viewer viewer, Object parent, Object[] elements) {
		int size = elements.length;
		ArrayList<Object> out = new ArrayList<>(size);

		for (int i = 0; i < size; ++i) {
			Object element = elements[i];
			if (element instanceof WizardCollectionElement) {
				Object wizardCollection = WizardCollectionElement.filter(viewer, this,
						(WizardCollectionElement) element);
				if (wizardCollection != null) {
					out.add(wizardCollection);
				}
			} else if (select(viewer, parent, element)) {
				out.add(element);
			}
		}
		return out.toArray();
	}
}
