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
package org.eclipse.ui.internal.dialogs.cpd;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.ui.internal.dialogs.cpd.CustomizePerspectiveDialog.ActionSet;
import org.eclipse.ui.internal.dialogs.cpd.CustomizePerspectiveDialog.DisplayItem;

/**
 * Filters out contribution items which are not in a given action set.
 *
 * @since 3.5
 */
class ActionSetFilter extends ViewerFilter {
	private ActionSet actionSet;

	public void setActionSet(ActionSet actionSet) {
		this.actionSet = actionSet;
	}

	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		if (!(element instanceof DisplayItem) || actionSet == null) {
			return false;
		}
		return CustomizePerspectiveDialog.includeInSetStructure((DisplayItem) element, actionSet);
	}
}