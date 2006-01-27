/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.navigator.internal.filters;

import org.eclipse.core.commands.operations.AbstractOperation;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.ui.navigator.CommonViewer;
import org.eclipse.ui.navigator.INavigatorContentService;
import org.eclipse.ui.navigator.INavigatorFilterService;
import org.eclipse.ui.navigator.NavigatorActivationService;
import org.eclipse.ui.navigator.internal.CommonNavigatorMessages;

class UpdateFiltersOperation extends AbstractOperation {

	private static final NavigatorActivationService NAVIGATOR_ACTIVATION_SERVICE = NavigatorActivationService
			.getInstance();

	private String[] activeContentExts;

	private String[] inactiveContentExts;

	private String[] activeFilterIds;

	private String[] inactiveFilterIds; 

	private final CommonViewer commonViewer;

	private final INavigatorContentService contentService;

	UpdateFiltersOperation(CommonViewer aCommonViewer,
			INavigatorContentService aContentService,
			String[] theActiveFilterIds, String[] theInactiveFilterIds,
			String[] theExtensionsToActivate, String[] theExtensionsToDeactivate) {
		super(
				CommonNavigatorMessages.UpdateFiltersOperation_Update_CommonViewer_Filter_);
		commonViewer = aCommonViewer;
		contentService = aContentService;
		activeFilterIds = theActiveFilterIds;
		inactiveFilterIds = theInactiveFilterIds;
		activeContentExts = theExtensionsToActivate;
		inactiveContentExts = theExtensionsToDeactivate;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.commands.operations.AbstractOperation#execute(org.eclipse.core.runtime.IProgressMonitor,
	 *      org.eclipse.core.runtime.IAdaptable)
	 */
	public IStatus execute(IProgressMonitor monitor, IAdaptable info) {

		boolean updateExtensionActivation = false;
		boolean updateFilterActivation = false; 

		try {
			commonViewer.getControl().setRedraw(false);

			/* is there a delta? */
			for (int i = 0; i < activeContentExts.length && !updateExtensionActivation; i++)
				updateExtensionActivation |= !contentService.isActive(activeContentExts[i]);
			for (int i = 0; i < inactiveContentExts.length && !updateExtensionActivation; i++)
				updateExtensionActivation |= contentService.isActive(inactiveContentExts[i]);
			
			/* If so, update */
			if (updateExtensionActivation) {
				contentService.activateExtensions(activeContentExts, true);
				NAVIGATOR_ACTIVATION_SERVICE
						.persistExtensionActivations(contentService
								.getViewerId());
			}

			INavigatorFilterService filterService = contentService
					.getFilterService();
			/* is there a delta? */
			for (int i = 0; i < activeFilterIds.length && !updateFilterActivation; i++)
				updateFilterActivation |= !filterService.isActive(activeFilterIds[i]);
			for (int i = 0; i < inactiveFilterIds.length && !updateFilterActivation; i++)
				updateFilterActivation |= filterService.isActive(inactiveFilterIds[i]);
			
			/* If so, update */
			if (updateFilterActivation) {
				filterService.setActiveFilterIds(activeFilterIds);
				filterService.persistFilterActivationState();

				commonViewer.resetFilters();

				ViewerFilter[] visibleFilters = filterService
						.getVisibleFilters(true);
				for (int i = 0; i < visibleFilters.length; i++)
					commonViewer.addFilter(visibleFilters[i]);
			}
			if (updateExtensionActivation || updateFilterActivation) {
				contentService.update();
				// the action providers may no longer be enabled, so we reset
				// the selection.
				commonViewer.setSelection(StructuredSelection.EMPTY);
			}

		} finally {
			commonViewer.getControl().setRedraw(true);
		}
		return Status.OK_STATUS;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.commands.operations.AbstractOperation#redo(org.eclipse.core.runtime.IProgressMonitor,
	 *      org.eclipse.core.runtime.IAdaptable)
	 */
	public IStatus redo(IProgressMonitor monitor, IAdaptable info) {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.commands.operations.AbstractOperation#undo(org.eclipse.core.runtime.IProgressMonitor,
	 *      org.eclipse.core.runtime.IAdaptable)
	 */
	public IStatus undo(IProgressMonitor monitor, IAdaptable info) {
		return null;
	}
}
