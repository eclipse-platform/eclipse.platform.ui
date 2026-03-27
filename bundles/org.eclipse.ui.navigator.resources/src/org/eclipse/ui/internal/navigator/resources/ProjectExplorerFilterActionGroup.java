/*******************************************************************************
 * Copyright (c) 2003, 2026 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.navigator.resources;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.resource.ResourceLocator;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.internal.navigator.filters.FilterActionGroup;
import org.eclipse.ui.internal.navigator.filters.SelectFiltersAction;
import org.eclipse.ui.internal.navigator.resources.plugin.WorkbenchNavigatorMessages;
import org.eclipse.ui.navigator.CommonViewer;

/**
 * @since 3.7
 */
public class ProjectExplorerFilterActionGroup extends FilterActionGroup {

	private SelectFiltersAction selectFiltersAction;
	private Action toggleFilterAction;
	private final CommonViewer commonViewer;
	private final ProjectExplorerFilteredTree filteredTree;

	public ProjectExplorerFilterActionGroup(CommonViewer aCommonViewer,
			ProjectExplorerFilteredTree aFilteredTree) {
		super(aCommonViewer);
		commonViewer = aCommonViewer;
		filteredTree = aFilteredTree;
		makeActions();
	}

	public void makeActions() {
		selectFiltersAction = new SelectFiltersAction(commonViewer, this);
		String filterImagePath = "icons/full/elcl16/filter_ps.svg"; //$NON-NLS-1$
		ResourceLocator.imageDescriptorFromBundle(getClass(), filterImagePath).ifPresent(d -> {
			selectFiltersAction.setImageDescriptor(d);
		});

		toggleFilterAction = new Action(WorkbenchNavigatorMessages.ProjectExplorer_toggleFilterField,
				IAction.AS_CHECK_BOX) {
			@Override
			public void run() {
				filteredTree.setShowFilterControls(isChecked());
				if (isChecked()) {
					filteredTree.getFilterControl().setFocus();
				} else {
					filteredTree.getViewer().getControl().setFocus();
				}
			}
		};
		toggleFilterAction.setToolTipText(WorkbenchNavigatorMessages.ProjectExplorer_toggleFilterField);
		String searchImagePath = "icons/full/elcl16/filter_ps.svg"; //$NON-NLS-1$
		ResourceLocator.imageDescriptorFromBundle(getClass(), searchImagePath).ifPresent(d -> {
			toggleFilterAction.setImageDescriptor(d);
		});
	}

	@Override
	public void fillActionBars(IActionBars actionBars) {
		super.fillActionBars(actionBars);
	}

	protected void fillToolbar(IToolBarManager toolBar) {
		toolBar.add(toggleFilterAction);
		toolBar.add(selectFiltersAction);
	}
}