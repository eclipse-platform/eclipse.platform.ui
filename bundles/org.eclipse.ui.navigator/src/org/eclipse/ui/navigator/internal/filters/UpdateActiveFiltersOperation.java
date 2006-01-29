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

import java.util.Arrays;

import org.eclipse.core.commands.operations.AbstractOperation;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.ui.navigator.CommonViewer;
import org.eclipse.ui.navigator.ICommonFilterDescriptor;
import org.eclipse.ui.navigator.INavigatorContentService;
import org.eclipse.ui.navigator.INavigatorFilterService;
import org.eclipse.ui.navigator.internal.CommonNavigatorMessages;

/**
 * Ensures that a given set of filters is <i>active</i> and the complement of
 * that set of filters are not <i>active</i>.
 * 
 * <p>
 * This operation is smart enough not to force any change if each id in each set
 * is already in its desired state (<i>active</i> or <i>inactive</i>).
 * </p>
 * 
 * @since 3.2
 * 
 */
public class UpdateActiveFiltersOperation extends AbstractOperation {

	private String[] filterIdsToActivate; 

	private final CommonViewer commonViewer;

	private final INavigatorContentService contentService;

	/**
	 * Create an operation to activate extensions and refresh the viewer.
	 * 
	 * 
	 * @param aCommonViewer
	 *            The CommonViewer instance to update
	 * @param theActiveFilterIds
	 *            An array of ids that correspond to the filters that should be
	 *            in the <i>active</i> state after this operation executes. The
	 *            complement of this set will likewise be in the <i>inactive</i>
	 *            state after this operation executes.
	 */
	public UpdateActiveFiltersOperation(CommonViewer aCommonViewer,
			String[] theActiveFilterIds) {
		super(
				CommonNavigatorMessages.UpdateFiltersOperation_Update_CommonViewer_Filter_);
		Assert.isNotNull(theActiveFilterIds);
		
		commonViewer = aCommonViewer;
		contentService = commonViewer.getNavigatorContentService();
		filterIdsToActivate = theActiveFilterIds;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.commands.operations.AbstractOperation#execute(org.eclipse.core.runtime.IProgressMonitor,
	 *      org.eclipse.core.runtime.IAdaptable)
	 */
	public IStatus execute(IProgressMonitor monitor, IAdaptable info) {

		boolean updateFilterActivation = false;
		
		// we sort the array in order to use Array.binarySearch();
		Arrays.sort(filterIdsToActivate);
		

		try {
			commonViewer.getControl().setRedraw(false);

			INavigatorFilterService filterService = contentService
					.getFilterService();
			
			ICommonFilterDescriptor[] visibleFilterDescriptors = filterService.getVisibleFilterDescriptors();
			  
			int indexofFilterIdToBeActivated;

			/* is there a delta? */
			for (int i = 0; i < visibleFilterDescriptors.length && !updateFilterActivation; i++) {
				indexofFilterIdToBeActivated = Arrays.binarySearch(filterIdsToActivate, visibleFilterDescriptors[i].getId());
				
				/* Either we have a filter that should be active that isn't XOR 
				 * a filter that shouldn't be active that is currently
				 */
				if(indexofFilterIdToBeActivated >= 0 ^ filterService.isActive(visibleFilterDescriptors[i].getId()))  
					updateFilterActivation = true; 
			} 
			 
			/* If so, update */
			if (updateFilterActivation) {

				filterService.setActiveFilterIds(filterIdsToActivate);
				filterService.persistFilterActivationState();

				commonViewer.resetFilters();

				ViewerFilter[] visibleFilters = filterService
						.getVisibleFilters(true);
				for (int i = 0; i < visibleFilters.length; i++)
					commonViewer.addFilter(visibleFilters[i]);

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
