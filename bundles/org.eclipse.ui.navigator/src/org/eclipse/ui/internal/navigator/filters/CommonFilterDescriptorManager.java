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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.ui.internal.navigator.extensions.NavigatorContentRegistryReader;
import org.eclipse.ui.navigator.INavigatorContentService;

/**
 * @since 3.2
 *
 */
public class CommonFilterDescriptorManager {

	private static final CommonFilterDescriptorManager INSTANCE = new CommonFilterDescriptorManager();

	private static final CommonFilterDescriptor[] NO_FILTER_DESCRIPTORS = new CommonFilterDescriptor[0];

	// K(ID) V(CommonFilterDescriptor)
	private final Map<String, CommonFilterDescriptor> filters = new HashMap<String, CommonFilterDescriptor>();

	/**
	 *
	 * @return An initialized singleton instance of the
	 *         CommonFilterDescriptorManager.
	 */
	public static CommonFilterDescriptorManager getInstance() {
		return INSTANCE;
	}

	private CommonFilterDescriptorManager() {
		new CommonFilterDescriptorRegistry().readRegistry();
	}

	/**
	 *
	 */
	public static final boolean FOR_UI = true;

	/**
	 *
	 * @param contentService
	 *            A content service to filter the visible filters.
	 * @return The set of filters that are 'visible' to the given viewer
	 *         descriptor.
	 */
	public CommonFilterDescriptor[] findVisibleFilters(INavigatorContentService contentService) {
		return findVisibleFilters(contentService, !FOR_UI);
	}

	/**
	 *
	 * @param contentService
	 *            A content service to filter the visible filters.
	 * @param forUI true if only filters visible to the UI are desired
	 * @return The set of filters that are 'visible' to the given viewer
	 *         descriptor.
	 */
	public CommonFilterDescriptor[] findVisibleFilters(INavigatorContentService contentService, boolean forUI) {

		List<CommonFilterDescriptor> visibleFilters = new ArrayList<CommonFilterDescriptor>();
		for (CommonFilterDescriptor descriptor : filters.values()) {
			if (forUI && !descriptor.isVisibleInUi())
				continue;
			if (contentService.isVisible(descriptor.getId())) {
				visibleFilters.add(descriptor);
			}
		}
		if (visibleFilters.isEmpty()) {
			return NO_FILTER_DESCRIPTORS;
		}
		return visibleFilters
				.toArray(new CommonFilterDescriptor[visibleFilters.size()]);
	}

	/**
	 * @param id
	 * @return the CommonFilterDescriptor, if found
	 */
	public CommonFilterDescriptor getFilterById(String id) {
		return filters.get(id);
	}

	/**
	 * @param aDescriptor
	 *            A non-null descriptor
	 */
	private void addCommonFilter(CommonFilterDescriptor aDescriptor) {
		filters.put(aDescriptor.getId(), aDescriptor);
	}

	private class CommonFilterDescriptorRegistry extends
			NavigatorContentRegistryReader {

		@Override
		protected boolean readElement(IConfigurationElement element) {
			if (TAG_COMMON_FILTER.equals(element.getName())) {
				addCommonFilter(new CommonFilterDescriptor(element));
				return true;
			}
			return super.readElement(element);
		}

	}

}
