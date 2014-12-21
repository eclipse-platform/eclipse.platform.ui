/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.dialogs.cpd;

import org.eclipse.jface.viewers.ICheckStateProvider;
import org.eclipse.ui.internal.dialogs.cpd.CustomizePerspectiveDialog.Category;
import org.eclipse.ui.internal.dialogs.cpd.CustomizePerspectiveDialog.DisplayItem;
import org.eclipse.ui.internal.dialogs.cpd.CustomizePerspectiveDialog.ShortcutItem;
import org.eclipse.ui.internal.dialogs.cpd.TreeManager.TreeItem;

/**
 * Provides the check logic for the categories viewer in the shortcuts tab.
 * Categories have a dual concept of children - their proper children
 * (sub-Categories, as in the wizards), and the actual elements they
 * contribute to the menu system. The check state must take this into
 * account.
 *
 * @since 3.5
 */
class CategoryCheckProvider implements ICheckStateProvider {
	@Override
	public boolean isChecked(Object element) {
		Category category = (Category) element;

		if (category.getChildren().isEmpty()
				&& category.getContributionItems().isEmpty()) {
			return false;
		}

		// To be checked, any sub-Category can be checked.
		for (TreeItem treeItem : category.getChildren()) {
			Category child = (Category) treeItem;
			if (isChecked(child)) {
				return true;
			}
		}

		// To be checked, any ShortcutItem can be checked.
		for (ShortcutItem shortcutItem : category.getContributionItems()) {
			DisplayItem item = shortcutItem;
			if (item.getState()) {
				return true;
			}
		}

		return false;
	}

	@Override
	public boolean isGrayed(Object element) {
		boolean hasChecked = false;
		boolean hasUnchecked = false;
		Category category = (Category) element;

		// Search in sub-Categories and ShortcutItems for one that is
		// checked and one that is unchecked.

		for (TreeItem treeItem : category.getChildren()) {
			Category child = (Category) treeItem;
			if (isGrayed(child)) {
				return true;
			}
			if (isChecked(child)) {
				hasChecked = true;
			} else {
				hasUnchecked = true;
			}
			if (hasChecked && hasUnchecked) {
				return true;
			}
		}

		for (ShortcutItem shortcutItem : category.getContributionItems()) {
			DisplayItem item = shortcutItem;
			if (item.getState()) {
				hasChecked = true;
			} else {
				hasUnchecked = true;
			}
			if (hasChecked && hasUnchecked) {
				return true;
			}
		}

		return false;
	}
}