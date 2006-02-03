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

package org.eclipse.ui.navigator.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.ui.navigator.ICommonFilterDescriptor;
import org.eclipse.ui.navigator.IExtensionActivationListener;
import org.eclipse.ui.navigator.INavigatorContentDescriptor;
import org.eclipse.ui.navigator.INavigatorFilterService;
import org.eclipse.ui.navigator.NavigatorActivationService;
import org.eclipse.ui.navigator.internal.extensions.NavigatorContentDescriptor;
import org.eclipse.ui.navigator.internal.extensions.NavigatorContentExtension;
import org.eclipse.ui.navigator.internal.filters.CommonFilterDescriptor;
import org.eclipse.ui.navigator.internal.filters.CommonFilterDescriptorManager;
import org.eclipse.ui.navigator.internal.filters.SkeletonViewerFilter;

/**
 * @since 3.2
 * 
 */
public class NavigatorFilterService implements INavigatorFilterService,
		IExtensionActivationListener {

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
	 * @param anAssistant
	 *            An assistant to help determine visibility
	 */
	public NavigatorFilterService(NavigatorContentService aContentService) {
		contentService = aContentService;
		restoreFilterActivation();
		updateDuplicateContentFilters();
		NavigatorActivationService.getInstance()
				.addExtensionActivationListener(contentService.getViewerId(),
						this);
	}
 
	private synchronized void restoreFilterActivation() {

		try {
			Preferences preferences = NavigatorPlugin.getDefault()
					.getPluginPreferences();

			
			if(preferences.contains(getFilterActivationPreferenceKey())) {  
				String activatedFiltersPreferenceValue = preferences
						.getString(getFilterActivationPreferenceKey());
				String[] activeFilterIds = activatedFiltersPreferenceValue
						.split(DELIM);
				for (int i = 0; i < activeFilterIds.length; i++)
					activeFilters.add(activeFilterIds[i]);

			} else { 
				ICommonFilterDescriptor[] visibleFilterDescriptors = getVisibleFilterDescriptors();
				for (int i = 0; i < visibleFilterDescriptors.length; i++)
					if(visibleFilterDescriptors[i].isActiveByDefault())
						activeFilters.add(visibleFilterDescriptors[i].getId());
			}

		} catch (RuntimeException e) {
			NavigatorPlugin.logError(0, e.getMessage(), e);
		}

	}

	/**
	 * Update the set of ViewerFilters returned for duplicate content from
	 * visible content extensions.
	 * 
	 */
	public void updateDuplicateContentFilters() {

		synchronized (enforcedViewerFilters) {
			enforcedViewerFilters.clear();
			INavigatorContentDescriptor[] visibleExtensions = contentService
					.getVisibleExtensions();

			for (int i = 0; i < visibleExtensions.length; i++) {
				if (contentService.isActive(visibleExtensions[i].getId())) {
					NavigatorContentExtension extension = contentService
							.getExtension(
									(NavigatorContentDescriptor) visibleExtensions[i],
									false);
					if(extension != null) {
						ViewerFilter[] enforcedFilters = extension
								.getDuplicateContentFilters();
						for (int j = 0; j < enforcedFilters.length; j++)
							enforcedViewerFilters.add(enforcedFilters[j]);
					}
				}

			}
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.navigator.INavigatorFilterService#persistFilterActivationState()
	 */
	public void persistFilterActivationState() {

		try {
			synchronized (activeFilters) {

				StringBuffer activatedFiltersPreferenceValue = new StringBuffer();

				for (Iterator activeItr = activeFilters.iterator(); activeItr
						.hasNext();)
					activatedFiltersPreferenceValue.append(
							activeItr.next().toString()).append(DELIM);

				Preferences preferences = NavigatorPlugin.getDefault()
						.getPluginPreferences();

				preferences.setValue(getFilterActivationPreferenceKey(),
						activatedFiltersPreferenceValue.toString());
			}

		} catch (RuntimeException e) {
			NavigatorPlugin.logError(0, e.getMessage(), e);
		}

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
		for (int i = 0; i < descriptors.length; i++)
			if (!toReturnOnlyActiveFilters || isActive(descriptors[i].getId())) {
				instance = getViewerFilter(descriptors[i]);
				if(instance != null)
					filters.add(instance);
			}

		/* return the enforced viewer filters always */
		filters.addAll(enforcedViewerFilters);

		if (filters.size() == 0)
			return NO_FILTERS;
		return (ViewerFilter[]) filters
				.toArray(new ViewerFilter[filters.size()]);
	}

	/**
	 * @param descriptor
	 *            A key into the viewerFilters map.
	 * @return A non-null ViewerFilter from the extension (or
	 *         {@link SkeletonViewerFilter#INSTANCE}).
	 */
	private ViewerFilter getViewerFilter(CommonFilterDescriptor descriptor) {
		ViewerFilter filter = (ViewerFilter) declaredViewerFilters
				.get(descriptor);
		if (filter != null)
			return filter;
		synchronized (declaredViewerFilters) {
			filter = (ViewerFilter) declaredViewerFilters.get(descriptor);
			if (filter == null)
				declaredViewerFilters
						.put(descriptor, (filter = descriptor.createFilter()));
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.navigator.IExtensionActivationListener#onExtensionActivation(java.lang.String,
	 *      java.lang.String[], boolean)
	 */
	public void onExtensionActivation(String aViewerId,
			String[] theNavigatorExtensionIds, boolean isActive) {
		updateDuplicateContentFilters();

	}

}
