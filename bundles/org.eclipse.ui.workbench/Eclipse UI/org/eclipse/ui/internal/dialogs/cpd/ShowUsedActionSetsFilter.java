/*******************************************************************************
 * Copyright (c) 2000, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
 * A filter which will only show action sets which contribute items in the
 * given tree structure.
 *
 * @since 3.5
 */
final class ShowUsedActionSetsFilter extends ViewerFilter {
	private DisplayItem rootItem;

	public ShowUsedActionSetsFilter(DisplayItem rootItem) {
		this.rootItem = rootItem;
	}

	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		return (CustomizePerspectiveDialog.includeInSetStructure(rootItem, (ActionSet) element));
	}
}