/*******************************************************************************
 * Copyright (c) 2003, 2016 IBM Corporation and others.
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
/*
 * Created on Feb 9, 2004
 */
package org.eclipse.ui.internal.navigator.filters;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.internal.navigator.CommonNavigatorMessages;
import org.eclipse.ui.navigator.CommonViewer;

/**
 *
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as part of a work in
 * progress. There is a guarantee neither that this API will work nor that it will remain the same.
 * Please do not use this API without consulting with the Platform/UI team.
 * </p>
 *
 * @since 3.2
 */
public class SelectFiltersAction extends Action {

	private final CommonViewer commonViewer;
	private FilterActionGroup filterGroup;

	/**
	 * Create an action to drive the Filter selection dialog
	 * for a particular instance of the CommonViewer.
	 */
	public SelectFiltersAction(CommonViewer aCommonViewer, FilterActionGroup aFilterGroup) {
		super(CommonNavigatorMessages.SelectFiltersActionDelegate_0);
		setToolTipText(CommonNavigatorMessages.SelectFiltersActionDelegate_1);
		commonViewer = aCommonViewer;
		filterGroup = aFilterGroup;
	}

	@Override
	public void run() {
		CommonFilterSelectionDialog filterSelectionDialog = new CommonFilterSelectionDialog(commonViewer);
		if (filterSelectionDialog.open() == Window.OK) {
			filterGroup.updateFilterShortcuts(filterSelectionDialog.getFilterDescriptorChangeHistory());
		}
	}

}
