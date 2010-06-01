/*******************************************************************************
 * Copyright (c) 2006, 2010 IBM Corporation and others.
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
 * <b>viewerContentBinding</b>s. That is, like content extensions, the id of a
 * commonFilter must match an includes expression for at least one
 * <b>viewerContentBinding</b> element for the corresponding
 * INavigatorContentService.
 * </p>
 * <p>
 * The activation of each filter should be persisted from session to session.
 * Clients of this interface have control over when the persistence occurs. In
 * particular, clients should call {@link  #persistFilterActivationState()}
 * after each call to {@link #setActiveFilterIds(String[])}.
 * </p> 
 * 
 * @see INavigatorContentService#getFilterService()
 * @see ViewerFilter
 * 
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
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
	 * Cause the specified set of filters to be activated, and any filters not
	 * specified to be deactivated. Updates the viewer filters for the
	 * associated viewer. This is a higher level operation that handles the
	 * filter activation completely, in contrast to
	 * {@link #setActiveFilterIds(String[])} which does not set the viewer
	 * filter state. This is probably the one you want if you are changing
	 * filters.
	 * 
	 * @param theFilterIds
	 *            An array of filter ids to activate.
	 * @since 3.5
	 */
	public void activateFilterIdsAndUpdateViewer(String[] theFilterIds);

	/**
	 * Activate the set of given filters. An <i>active</i> filter will always be
	 * returned from {@link #getVisibleFilters(boolean)}. An <i>inactive</i> filter will
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
	 * Persist the current activation state for visible filters.
	 */
	void persistFilterActivationState();
	
	/**
	 * 
	 * Return the viewer filter for the given descriptor
	 * 
	 * @param theDescriptor
	 *            A non-null filter descriptor.
	 * @return the viewer filter for the given descriptor
	 * @since 3.3
	 */
	ViewerFilter getViewerFilter(ICommonFilterDescriptor theDescriptor);
	
}
