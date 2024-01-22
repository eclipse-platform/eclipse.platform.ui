/*******************************************************************************
 * Copyright (c) 2000, 2018 IBM Corporation and others.
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

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.ui.internal.dialogs.cpd.CustomizePerspectiveDialog.ActionSet;

/**
 * A Listener for a list of command groups, that updates the viewer and filter
 * who are dependent on the action set selection.
 *
 * @since 3.5
 */
final class ActionSetSelectionChangedListener implements ISelectionChangedListener {
	private final TreeViewer filterViewer;
	private final ActionSetFilter filter;

	public ActionSetSelectionChangedListener(TreeViewer viewer, ActionSetFilter menuStructureFilterByActionSet) {
		this.filterViewer = viewer;
		this.filter = menuStructureFilterByActionSet;
	}

	@Override
	public void selectionChanged(SelectionChangedEvent event) {
		Object element = event.getStructuredSelection().getFirstElement();
		filter.setActionSet((ActionSet) element);
		filterViewer.refresh();
		filterViewer.expandAll();
	}
}