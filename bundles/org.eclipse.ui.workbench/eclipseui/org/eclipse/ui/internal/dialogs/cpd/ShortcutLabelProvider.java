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

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.internal.dialogs.cpd.CustomizePerspectiveDialog.ShortcutItem;

/**
 * A label provider to include the description field of ShortcutItems in the
 * table.
 *
 * @since 3.5
 */
class ShortcutLabelProvider extends TreeManager.TreeItemLabelProvider implements ITableLabelProvider {
	@Override
	public Image getColumnImage(Object element, int columnIndex) {
		if (columnIndex == 0) {
			return this.getImage(element);
		}
		return null;
	}

	@Override
	public String getColumnText(Object element, int columnIndex) {
		if (columnIndex == 1) {
			return ((ShortcutItem) element).getDescription();
		}
		return this.getText(element);
	}

	@Override
	public void addListener(ILabelProviderListener listener) {
	}

	@Override
	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	@Override
	public void removeListener(ILabelProviderListener listener) {
	}
}