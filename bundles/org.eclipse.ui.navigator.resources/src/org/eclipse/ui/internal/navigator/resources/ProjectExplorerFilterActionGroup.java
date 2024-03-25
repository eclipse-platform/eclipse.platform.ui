/*******************************************************************************
 * Copyright (c) 2003, 2018 IBM Corporation and others.
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

import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.resource.ResourceLocator;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.internal.navigator.filters.FilterActionGroup;
import org.eclipse.ui.internal.navigator.filters.SelectFiltersAction;
import org.eclipse.ui.navigator.CommonViewer;

/**
 * @since 3.7
 */
public class ProjectExplorerFilterActionGroup extends FilterActionGroup {

	private SelectFiltersAction selectFiltersAction;
	private CommonViewer commonViewer;

	public ProjectExplorerFilterActionGroup(CommonViewer aCommonViewer) {
		super(aCommonViewer);
		commonViewer = aCommonViewer;
		makeActions();
	}

	public void makeActions() {
		selectFiltersAction = new SelectFiltersAction(commonViewer, this);
		String imageFilePath = "icons/full/elcl16/filter_ps.png"; //$NON-NLS-1$
		ResourceLocator.imageDescriptorFromBundle(getClass(), imageFilePath).ifPresent(d -> {
			selectFiltersAction.setImageDescriptor(d);
			selectFiltersAction.setHoverImageDescriptor(d);
		});
	}

	@Override
	public void fillActionBars(IActionBars actionBars) {
		super.fillActionBars(actionBars);
	}

	protected void fillToolbar(IToolBarManager toolBar) {
		toolBar.add(selectFiltersAction);
	}
}