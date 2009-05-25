/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.navigator.filters;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.ui.internal.navigator.NavigatorFilterService;
import org.eclipse.ui.navigator.CommonViewer;
import org.eclipse.ui.navigator.ICommonFilterDescriptor;

/**
 * @since 3.2
 * 
 */
public class ToggleFilterAction extends Action {

	private ICommonFilterDescriptor descriptor;

	private NavigatorFilterService filterService;

	private CommonViewer commonViewer;

	protected ToggleFilterAction(CommonViewer aCommonViewer,
			NavigatorFilterService aFilterService,
			ICommonFilterDescriptor aFilterDescriptor) {
		Assert.isNotNull(aCommonViewer);
		Assert.isNotNull(aFilterService);
		Assert.isNotNull(aFilterDescriptor);

		commonViewer = aCommonViewer;
		filterService = aFilterService;
		descriptor = aFilterDescriptor;

		setChecked(filterService.isActive(descriptor.getId()));
		setText(descriptor.getName());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.action.Action#run()
	 */
	public void run() {

		boolean toMakeActive = !isChecked();

		filterService.setActive(descriptor.getId(), toMakeActive);
		filterService.persistFilterActivationState();

		ViewerFilter viewerFilter = filterService.getViewerFilter(descriptor);
		if (toMakeActive) {
			commonViewer.addFilter(viewerFilter);
		} else {
			commonViewer.removeFilter(viewerFilter);
		}

		// the action providers may no longer be enabled, so we
		// reset the selection.
		commonViewer.setSelection(StructuredSelection.EMPTY);

		setChecked(toMakeActive);

	}
}
