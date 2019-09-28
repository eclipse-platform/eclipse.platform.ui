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
import org.eclipse.ui.internal.navigator.CommonNavigatorActionGroup;
import org.eclipse.ui.internal.navigator.filters.FilterActionGroup;
import org.eclipse.ui.navigator.CommonNavigator;
import org.eclipse.ui.navigator.CommonViewer;
import org.eclipse.ui.navigator.LinkHelperService;

/**
 * @since 3.7
 */
public class ProjectExplorerActionGroup extends CommonNavigatorActionGroup {

	private ProjectExplorerFilterActionGroup fFilterActionGroup;

	public ProjectExplorerActionGroup(CommonNavigator aNavigator, CommonViewer aViewer,
			LinkHelperService linkHelperService) {
		super(aNavigator, aViewer, linkHelperService);
	}

	@Override
	protected FilterActionGroup createFilterActionGroup(CommonViewer pCommonViewer) {
		fFilterActionGroup = new ProjectExplorerFilterActionGroup(pCommonViewer);
		return fFilterActionGroup;
	}

	@Override
	protected void fillToolBar(IToolBarManager pToolBar) {
		super.fillToolBar(pToolBar);
		fFilterActionGroup.fillToolbar(pToolBar);
	}
}
