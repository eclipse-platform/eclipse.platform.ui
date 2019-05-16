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
 *     Bachmann electronic GmbH - Bug 447530 - persist the id of active non visible filters
 ******************************************************************************/

package org.eclipse.ui.internal.navigator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
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
	private final Map<ICommonFilterDescriptor, ViewerFilter> declaredViewerFilters = new HashMap<ICommonFilterDescriptor, ViewerFilter>();

	/* Set of ViewerFilters enforced from visible/active content extensions */
	private final Set enforcedViewerFilters = new HashSet();

	/* A set of active filter String ids */
	private final Set<String> activeFilters = new HashSet<String>();

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
			@Override
			public void run() throws Exception {

				CommonFilterDescriptor[] visibleFilterDescriptors = CommonFilterDescriptorManager.getInstance()
						.findVisibleFilters(contentService);

				// add the non visible in ui active by default filters
				for (CommonFilterDescriptor filterDescription : visibleFilterDescriptors) {
					if (!filterDescription.isVisibleInUi() && filterDescription.isActiveByDefault()) {
						activeFilters.add(filterDescription.getId());
					}

				}

				IEclipsePreferences prefs = NavigatorContentService.getPreferencesRoot();

				if (prefs.get(getFilterActivationPreferenceKey(), null) != null) {
					// add all visible ui filters that had been activated by the user
					String activatedFiltersPreferenceValue = prefs.get(
							getFilterActivationPreferenceKey(), null);
					String[] activeFilterIds = activatedFiltersPreferenceValue.split(DELIM);
					for (String activeFilterId : activeFilterIds) {
						if (activeFilterId.isEmpty()) {
							continue;
						}
						activeFilters.add(activeFilterId);
					}

				} else {
					// add all visible in ui filters
					for (CommonFilterDescriptor filterDescription : visibleFilterDescriptors) {
						if (filterDescription.isVisibleInUi() && filterDescription.isActiveByDefault()) {
							activeFilters.add(filterDescription.getId());
						}
					}
				}
			}
		});
	}

	@Override
	public void persistFilterActivationState() {

		synchronized (activeFilters) {
			CommonFilterDescriptorManager dm = CommonFilterDescriptorManager
			.getInstance();

			/*
			 * by creating a StringBuilder with DELIM, we ensure the string is not empty
			 * when persisted.
			 */
			StringBuilder activatedFiltersPreferenceValue = new StringBuilder(DELIM);

			for (String id : activeFilters) {
				CommonFilterDescriptor filterDescriptor = dm.getFilterById(id);
				if (filterDescriptor == null || !filterDescriptor.isVisibleInUi()) {
					continue;
				}
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

	@Override
	public ViewerFilter[] getVisibleFilters(boolean toReturnOnlyActiveFilters) {
		CommonFilterDescriptor[] descriptors = CommonFilterDescriptorManager
				.getInstance().findVisibleFilters(contentService);

		List<ViewerFilter> filters = new ArrayList<ViewerFilter>();

		ViewerFilter instance;
		for (CommonFilterDescriptor descriptor : descriptors) {
			if (!toReturnOnlyActiveFilters || isActive(descriptor.getId())) {
				instance = getViewerFilter(descriptor);
				if (instance != null) {
					filters.add(instance);
				}
			}
		}

		/* return the enforced viewer filters always */
		filters.addAll(enforcedViewerFilters);

		if (filters.isEmpty()) {
			return NO_FILTERS;
		}
		return filters
				.toArray(new ViewerFilter[filters.size()]);
	}

	/**
	 * @param descriptor
	 *            A key into the viewerFilters map.
	 * @return A non-null ViewerFilter from the extension (or
	 *         {@link SkeletonViewerFilter#INSTANCE}).
	 */
	@Override
	public ViewerFilter getViewerFilter(ICommonFilterDescriptor descriptor) {
		ViewerFilter filter = null;
		synchronized (declaredViewerFilters) {
			filter = declaredViewerFilters.get(descriptor);
			if (filter == null) {
				declaredViewerFilters.put(descriptor,
						(filter = ((CommonFilterDescriptor) descriptor)
								.createFilter()));
			}
		}
		return filter;
	}

	@Override
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

	@Override
	public boolean isActive(String aFilterId) {
		synchronized (activeFilters) {
			return activeFilters.contains(aFilterId);
		}
	}

	@Override
	public void setActiveFilterIds(String[] theFilterIds) {
		Assert.isNotNull(theFilterIds);
		synchronized (activeFilters) {
			activeFilters.clear();
			activeFilters.addAll(Arrays.asList(theFilterIds));
		}
	}

	@Override
	public void activateFilterIdsAndUpdateViewer(String[] filterIdsToActivate) {
		boolean updateFilterActivation = false;

		// we sort the array in order to use Array.binarySearch();
		Arrays.sort(filterIdsToActivate);
		CommonFilterDescriptor[] visibleFilterDescriptors = (CommonFilterDescriptor[]) getVisibleFilterDescriptors();

		int indexofFilterIdToBeActivated;

		List<String> nonUiVisible = null;

	    /* is there a delta? */
		for (CommonFilterDescriptor visibleFilterDescriptor : visibleFilterDescriptors) {
			indexofFilterIdToBeActivated = Arrays.binarySearch(filterIdsToActivate, visibleFilterDescriptor.getId());
			/*
			 * Either we have a filter that should be active that isn't XOR a filter that
			 * shouldn't be active that is currently
			 */
			if (indexofFilterIdToBeActivated >= 0 ^ isActive(visibleFilterDescriptor.getId())) {
				updateFilterActivation = true;
			}
			// We don't turn of non-UI visible filters here, they have to be manipulated
			// explicitly
			if (!visibleFilterDescriptor.isVisibleInUi()) {
				if (nonUiVisible == null)
					nonUiVisible = new ArrayList<String>();
				nonUiVisible.add(visibleFilterDescriptor.getId());
			}
		}

		/* If so, update */
		if (updateFilterActivation) {
			if (nonUiVisible != null) {
				for (String filterIdToActivate : filterIdsToActivate)
					nonUiVisible.add(filterIdToActivate);
				filterIdsToActivate = nonUiVisible.toArray(new String[]{});
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
					activeFilters.add(aFilterId);
				else
					activeFilters.remove(aFilterId);

			}

		}
	}

}
