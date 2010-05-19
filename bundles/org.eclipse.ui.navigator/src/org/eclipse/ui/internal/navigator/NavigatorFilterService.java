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

package org.eclipse.ui.internal.navigator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.ui.internal.navigator.filters.CommonFilterDescriptor;
import org.eclipse.ui.internal.navigator.filters.CommonFilterDescriptorManager;
import org.eclipse.ui.internal.navigator.filters.SkeletonViewerFilter;
import org.eclipse.ui.navigator.ICommonFilterDescriptor;
import org.eclipse.ui.navigator.INavigatorFilterService;

/**
 * @since 3.2
 * 
 */
public class NavigatorFilterService implements INavigatorFilterService {

	private static final ViewerFilter[] NO_FILTERS = new ViewerFilter[0];

	private static final String ACTIVATION_KEY = ".filterActivation"; //$NON-NLS-1$

	private static final String DELIM = ":"; //$NON-NLS-1$

	private final NavigatorContentService contentService;

	/* Map of (ICommonFilterDescriptor, ViewerFilter)-pairs */
	private final Map declaredViewerFilters = new HashMap();

	/* Set of ViewerFilters enforced from visible/active content extensions */
	private final Set enforcedViewerFilters = new HashSet();

	/* A set of active filter String ids */
	private final Set activeFilters = new HashSet();

	/**
	 * @param aContentService
	 *            The corresponding content service
	 */
	public NavigatorFilterService(NavigatorContentService aContentService) {
		contentService = aContentService;
		restoreFilterActivation();
	}

	private synchronized void restoreFilterActivation() {
		SafeRunner.run(new NavigatorSafeRunnable() {
			public void run() throws Exception {
				IEclipsePreferences prefs = NavigatorContentService.getPreferencesRoot();

				if (prefs.get(getFilterActivationPreferenceKey(), null) != null) {
					String activatedFiltersPreferenceValue = prefs.get(
							getFilterActivationPreferenceKey(), null);
					String[] activeFilterIds = activatedFiltersPreferenceValue.split(DELIM);
					for (int i = 0; i < activeFilterIds.length; i++) {
						activeFilters.add(activeFilterIds[i]);
					}

				} else {
					ICommonFilterDescriptor[] visibleFilterDescriptors = getVisibleFilterDescriptors();
					for (int i = 0; i < visibleFilterDescriptors.length; i++) {
						if (visibleFilterDescriptors[i].isActiveByDefault()) {
							activeFilters.add(visibleFilterDescriptors[i].getId());
						}
					}
				}
			}
		});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.navigator.INavigatorFilterService#persistFilterActivationState()
	 */
	public void persistFilterActivationState() {

		synchronized (activeFilters) {
			CommonFilterDescriptorManager dm = CommonFilterDescriptorManager
			.getInstance();

			/*
			 * by creating a StringBuffer with DELIM, we ensure the string is
			 * not empty when persisted.
			 */
			StringBuffer activatedFiltersPreferenceValue = new StringBuffer(DELIM);

			for (Iterator activeItr = activeFilters.iterator(); activeItr.hasNext();) {
				String id = activeItr.next().toString();
				if (!dm.getFilterById(id).isVisibleInUi())
					continue;
				activatedFiltersPreferenceValue.append(id).append(DELIM);
			}

			IEclipsePreferences prefs = NavigatorContentService.getPreferencesRoot();
			prefs.put(getFilterActivationPreferenceKey(), activatedFiltersPreferenceValue.toString());
			NavigatorContentService.flushPreferences(prefs);
		}
	}
	
	/**
	 * Used for the tests
	 */
	public void resetFilterActivationState() {
		IEclipsePreferences prefs = NavigatorContentService.getPreferencesRoot();
		prefs.remove(getFilterActivationPreferenceKey());
		NavigatorContentService.flushPreferences(prefs);
	}

	/**
	 * @return The correct filter activation preference key for the
	 *         corresponding content service.
	 */
	private String getFilterActivationPreferenceKey() {
		return contentService.getViewerId() + ACTIVATION_KEY;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.navigator.INavigatorFilterService#getVisibleFilters(boolean)
	 */
	public ViewerFilter[] getVisibleFilters(boolean toReturnOnlyActiveFilters) {
		CommonFilterDescriptor[] descriptors = CommonFilterDescriptorManager
				.getInstance().findVisibleFilters(contentService);

		List filters = new ArrayList();

		ViewerFilter instance;
		for (int i = 0; i < descriptors.length; i++) {
			if (!toReturnOnlyActiveFilters || isActive(descriptors[i].getId())) {
				instance = getViewerFilter(descriptors[i]);
				if (instance != null) {
					filters.add(instance);
				}
			}
		}

		/* return the enforced viewer filters always */
		filters.addAll(enforcedViewerFilters);

		if (filters.size() == 0) {
			return NO_FILTERS;
		}
		return (ViewerFilter[]) filters
				.toArray(new ViewerFilter[filters.size()]);
	}

	/**
	 * @param descriptor
	 *            A key into the viewerFilters map.
	 * @return A non-null ViewerFilter from the extension (or
	 *         {@link SkeletonViewerFilter#INSTANCE}).
	 */
	public ViewerFilter getViewerFilter(ICommonFilterDescriptor descriptor) {
		ViewerFilter filter = null;
		synchronized (declaredViewerFilters) {
			filter = (ViewerFilter) declaredViewerFilters.get(descriptor);
			if (filter == null) {
				declaredViewerFilters.put(descriptor,
						(filter = ((CommonFilterDescriptor) descriptor)
								.createFilter()));
			}
		}
		return filter;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.navigator.INavigatorFilterService#getVisibleFilterIds()
	 */
	public ICommonFilterDescriptor[] getVisibleFilterDescriptors() {
		return CommonFilterDescriptorManager.getInstance().findVisibleFilters(
				contentService);
	}

	/**
	 * @return the visible filter descriptors for the UI
	 */
	public ICommonFilterDescriptor[] getVisibleFilterDescriptorsForUI() {
		return CommonFilterDescriptorManager.getInstance().findVisibleFilters(
				contentService, CommonFilterDescriptorManager.FOR_UI);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.navigator.INavigatorFilterService#isActive(java.lang.String)
	 */
	public boolean isActive(String aFilterId) {
		synchronized (activeFilters) {
			return activeFilters.contains(aFilterId);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.navigator.INavigatorFilterService#activateFilters(java.lang.String[])
	 */
	public void setActiveFilterIds(String[] theFilterIds) {
		Assert.isNotNull(theFilterIds);
		synchronized (activeFilters) {
			activeFilters.clear();
			activeFilters.addAll(Arrays.asList(theFilterIds));
		}
	}
	
	public void activateFilterIdsAndUpdateViewer(String[] filterIdsToActivate) {
		boolean updateFilterActivation = false;

		// we sort the array in order to use Array.binarySearch();
		Arrays.sort(filterIdsToActivate);
		CommonFilterDescriptor[] visibleFilterDescriptors = (CommonFilterDescriptor[]) getVisibleFilterDescriptors();

		int indexofFilterIdToBeActivated;

		List nonUiVisible = null;
		
		/* is there a delta? */
		for (int i = 0; i < visibleFilterDescriptors.length; i++) {
			indexofFilterIdToBeActivated = Arrays.binarySearch(filterIdsToActivate,
					visibleFilterDescriptors[i].getId());
			/*
			 * Either we have a filter that should be active that isn't XOR a
			 * filter that shouldn't be active that is currently
			 */
			if (indexofFilterIdToBeActivated >= 0 ^ isActive(visibleFilterDescriptors[i].getId())) {
				updateFilterActivation = true;
			}
			
			// We don't turn of non-UI visible filters here, they have to be manipulated explicitly
			if (!visibleFilterDescriptors[i].isVisibleInUi()) {
				if (nonUiVisible == null)
					nonUiVisible = new ArrayList();
				nonUiVisible.add(visibleFilterDescriptors[i].getId());
			}
		}

		/* If so, update */
		if (updateFilterActivation) {
			if (nonUiVisible != null) {
				for (int i = 0; i < filterIdsToActivate.length; i++)
					nonUiVisible.add(filterIdsToActivate[i]);
				filterIdsToActivate = (String[]) nonUiVisible.toArray(new String[]{});
			}
			
			setActiveFilterIds(filterIdsToActivate);
			persistFilterActivationState();
			updateViewer();
			// the action providers may no longer be enabled, so we
			// reset the selection.
			StructuredViewer commonViewer = (StructuredViewer) contentService.getViewer();
			commonViewer.setSelection(StructuredSelection.EMPTY);
		}
	}

	/**
	 * Updates the viewer filters to match the active filters.
	 */
	public void updateViewer() {
		StructuredViewer commonViewer = (StructuredViewer) contentService.getViewer();

		ViewerFilter[] visibleFilters =	getVisibleFilters(true);
		commonViewer.setFilters(visibleFilters);
	}		
		
	/**
	 * Activate the given array without disabling all other filters.
	 * 
	 * @param theFilterIds
	 *            The filter ids to activate.
	 */
	public void addActiveFilterIds(String[] theFilterIds) {
		Assert.isNotNull(theFilterIds);
		synchronized (activeFilters) {
			activeFilters.addAll(Arrays.asList(theFilterIds));
		}
	}
	
	/**
	 * 
	 * @param aFilterId The id of the filter to activate or deactivate
	 * @param toMakeActive True to make the filter active, false to make the filter inactive
	 */
	public void setActive(String aFilterId, boolean toMakeActive) {

		synchronized (activeFilters) {
			boolean isActive = activeFilters.contains(aFilterId);
			if(isActive ^ toMakeActive) {
				if(toMakeActive)
					activeFilters.remove(aFilterId);
				else
					activeFilters.add(aFilterId);
					
			}
				
		}
	}
 
}
