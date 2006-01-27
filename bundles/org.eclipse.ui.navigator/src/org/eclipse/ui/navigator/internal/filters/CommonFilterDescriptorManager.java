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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.ui.navigator.INavigatorContentService;
import org.eclipse.ui.navigator.internal.extensions.NavigatorContentRegistryReader;

/**
 * @since 3.2
 * 
 */
public class CommonFilterDescriptorManager {

	private static final CommonFilterDescriptorManager INSTANCE = new CommonFilterDescriptorManager();

	private static final CommonFilterDescriptor[] NO_FILTER_DESCRIPTORS = new CommonFilterDescriptor[0];

	private final Set filters = new HashSet();

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
	 * @param contentService
	 *            A content service to filter the visible filters.
	 * @return The set of filters that are 'visible' to the given viewer
	 *         descriptor.
	 */
	public CommonFilterDescriptor[] findVisibleFilters(INavigatorContentService contentService) {

		List visibleFilters = new ArrayList();
		CommonFilterDescriptor descriptor;
		for (Iterator filtersItr = filters.iterator(); filtersItr.hasNext();) {
			descriptor = (CommonFilterDescriptor) filtersItr.next();
			if (contentService.isVisible(descriptor.getId()))
				visibleFilters.add(descriptor);
		}
		if (visibleFilters.size() == 0)
			return NO_FILTER_DESCRIPTORS;
		return (CommonFilterDescriptor[]) visibleFilters
				.toArray(new CommonFilterDescriptor[visibleFilters.size()]);
	}

	/**
	 * @param descriptor
	 *            A non-null descriptor
	 */
	private void addCommonFilter(CommonFilterDescriptor aDescriptor) {
		filters.add(aDescriptor);
	}

	private class CommonFilterDescriptorRegistry extends
			NavigatorContentRegistryReader {

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ui.navigator.internal.extensions.NavigatorContentRegistryReader#readElement(org.eclipse.core.runtime.IConfigurationElement)
		 */
		protected boolean readElement(IConfigurationElement element) {
			if (TAG_COMMON_FILTER.equals(element.getName())) {
				addCommonFilter(new CommonFilterDescriptor(element));
				return true;
			}
			return super.readElement(element);
		}

	}

}
