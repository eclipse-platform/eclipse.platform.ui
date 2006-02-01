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

package org.eclipse.ui.navigator.internal.sorters;

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
public class CommonSorterDescriptorManager {


	private static final CommonSorterDescriptorManager INSTANCE = new CommonSorterDescriptorManager();

	private static final CommonSorterDescriptor[] NO_SORTER_DESCRIPTORS = new CommonSorterDescriptor[0];

	private final Set sorters = new HashSet();

	/**
	 * 
	 * @return An initialized singleton instance of the
	 *         CommonFilterDescriptorManager.
	 */
	public static CommonSorterDescriptorManager getInstance() {
		return INSTANCE;
	}

	private CommonSorterDescriptorManager() {
		new CommonSorterDescriptorRegistry().readRegistry();
	}

	/**
	 * 
	 * @param contentService
	 *            A content service to filter the visible filters.
	 * @param aParent
	 * 			 The parent used to search for a sorter.
	 * @return The set of filters that are 'visible' to the given viewer
	 *         descriptor.
	 */
	public CommonSorterDescriptor[] findApplicableSorters(INavigatorContentService contentService, Object aParent) {

		List applicableSorters = new ArrayList();
		CommonSorterDescriptor descriptor;
		for (Iterator sortersItr = sorters.iterator(); sortersItr.hasNext();) {
			descriptor = (CommonSorterDescriptor) sortersItr.next();
			if (contentService.isVisible(descriptor.getId()) && descriptor.isEnabledForParent(aParent))
				applicableSorters.add(descriptor);
		}
		if (applicableSorters.size() == 0)
			return NO_SORTER_DESCRIPTORS;
		return (CommonSorterDescriptor[]) applicableSorters
				.toArray(new CommonSorterDescriptor[applicableSorters.size()]);
	}

	/**
	 * @param descriptor
	 *            A non-null descriptor
	 */
	private void addCommonSorter(CommonSorterDescriptor aDescriptor) {
		sorters.add(aDescriptor);
	}

	private class CommonSorterDescriptorRegistry extends
			NavigatorContentRegistryReader {

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ui.navigator.internal.extensions.NavigatorContentRegistryReader#readElement(org.eclipse.core.runtime.IConfigurationElement)
		 */
		protected boolean readElement(IConfigurationElement element) {
			if (TAG_COMMON_SORTER.equals(element.getName())) {
				addCommonSorter(new CommonSorterDescriptor(element));
				return true;
			}
			return super.readElement(element);
		}

	}

}
