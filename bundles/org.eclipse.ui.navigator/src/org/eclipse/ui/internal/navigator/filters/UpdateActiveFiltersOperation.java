/*******************************************************************************
 * Copyright (c) 2006, 2015 IBM Corporation and others.
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
 ******************************************************************************/

package org.eclipse.ui.internal.navigator.filters;

import org.eclipse.core.commands.operations.AbstractOperation;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.internal.navigator.CommonNavigatorMessages;
import org.eclipse.ui.navigator.CommonViewer;
import org.eclipse.ui.navigator.INavigatorContentService;

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

	@Override
	public IStatus execute(IProgressMonitor monitor, IAdaptable info) {
		contentService.getFilterService().activateFilterIdsAndUpdateViewer(filterIdsToActivate);
		return Status.OK_STATUS;
	}

	@Override
	public IStatus redo(IProgressMonitor monitor, IAdaptable info) {
		return null;
	}

	@Override
	public IStatus undo(IProgressMonitor monitor, IAdaptable info) {
		return null;
	}
}
