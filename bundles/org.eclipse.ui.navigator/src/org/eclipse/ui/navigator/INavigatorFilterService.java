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

package org.eclipse.ui.navigator;

import org.eclipse.jface.viewers.ViewerFilter;

/**
 * Provides support for managing the filters defined for a Common Navigator
 * viewer.
 * 
 * <p>
 * An INavigatorFilterService manages the available common filters and their
 * current activation state for a particular INavigatorContentService. An
 * INavigatorFilterService cannot be acquired without an
 * INavigatorContentService (through
 * {@link INavigatorContentService#getFilterService}). Each instance will
 * provide information specific to the content service associated with it.
 * </p>
 * <p>
 * The visibility of commonFilters is controlled through matching
 * <b>viewerContentBinding</b>s. That is like content extensions, the id of a
 * commonFilter must match an includes expression for at least one
 * <b>viewerContentBinding</b> element for the corresponding
 * INavigatorContentService.
 * </p>
 * <p>
 * The activation of each filter should be persisted from session to session.
 * Clients of this interface have control over when the persistence occurs. In
 * particular, clients should call {@link  #persistFilterActivationState()}
 * after each call to {@link #setActiveFilterIds(String[])} or
 * {@link #deactivateFilters(String[])}.
 * </p>
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is a guarantee neither that this API will
 * work nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/UI team.
 * </p>
 * 
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 * 
 * @since 3.2
 * 
 */
public interface INavigatorFilterService {

	/**
	 * 
	 * Determine the set of filters which are <i>visible</i> to the
	 * content service associated with this filter service. 
	 * 
	 * @param toReturnOnlyActiveFilters
	 *            True indicates that only active filters should be returned.
	 * @return An array of ViewerFilters that should be applied to the viewer
	 *         rendering the content from this INavigatorContentService
	 */
	ViewerFilter[] getVisibleFilters(boolean toReturnOnlyActiveFilters);

	/**
	 * 
	 * <i>Visible</i> filters are filters whose ids match a
	 * <b>viewerContentBinding</b> for the corresponding viewer.
	 * 
	 * @return An array of all visible filter descriptors.
	 */
	ICommonFilterDescriptor[] getVisibleFilterDescriptors();

	/**
	 * @param aFilterId
	 *            Check the activation of aFilterId for the content service
	 *            corresponding to this filter service.
	 * @return True if the filter specified by the id is active for the content
	 *         service corresponding to this filter service.
	 */
	boolean isActive(String aFilterId);

	/**
	 * Activate the set of given filters. An 'active' filter will always be
	 * returned from {@link #getVisibleFilterIds()}. An 'inactive' filter will
	 * only be returned from {@link #getVisibleFilters(boolean)} when it is
	 * called with <b>false</b>.
	 * 
	 * 
	 * @param theFilterIds
	 *            An array of filter ids to activate.
	 * 
	 */
	void setActiveFilterIds(String[] theFilterIds);

	/**
	 * 
	 * Persist the current activation state for visible filters.
	 */
	void persistFilterActivationState();
}
