/*******************************************************************************
 * Copyright (c) 2006, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.navigator.sorters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.internal.navigator.CommonNavigatorMessages;
import org.eclipse.ui.internal.navigator.NavigatorContentService;
import org.eclipse.ui.internal.navigator.NavigatorPlugin;
import org.eclipse.ui.internal.navigator.extensions.NavigatorContentDescriptor;
import org.eclipse.ui.internal.navigator.extensions.NavigatorContentRegistryReader;
import org.eclipse.ui.navigator.INavigatorContentDescriptor;

/**
 * @since 3.2
 * 
 */
public class CommonSorterDescriptorManager {

	private static final CommonSorterDescriptorManager INSTANCE = new CommonSorterDescriptorManager();

	private static final CommonSorterDescriptor[] NO_SORTER_DESCRIPTORS = new CommonSorterDescriptor[0];

	private final Map<INavigatorContentDescriptor, Set> sortersMap = new HashMap<INavigatorContentDescriptor, Set>();
	
	/**
	 * 
	 * @return An initialized singleton instance of the
	 *         CommonSorterDescriptorManager.
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
	 *            The parent used to search for a sorter.
	 * @return The set of filters that are 'visible' to the given viewer
	 *         descriptor.
	 */
	public CommonSorterDescriptor[] findApplicableSorters(
			NavigatorContentService contentService, Object aParent) {

		INavigatorContentDescriptor sourceOfContribution = contentService
				.getSourceOfContribution(aParent);
		return findApplicableSorters(contentService, sourceOfContribution, aParent);
	}

	/**
	 * 
	 * @param aContentService
	 *            A content service to filter the visible filters.
	 * @param theSource
	 *            The source of each *value.
	 * @param aParent
	 *            The parent used to search for a sorter.
	 * @return The set of filters that are 'visible' to the given viewer
	 *         descriptor.
	 */
	public CommonSorterDescriptor[] findApplicableSorters(
			NavigatorContentService aContentService,
			INavigatorContentDescriptor theSource, Object aParent) {

		List<CommonSorterDescriptor> applicableSorters = new ArrayList<CommonSorterDescriptor>();

		CommonSorterDescriptor descriptor;
		Set<CommonSorterDescriptor> sorters = getCommonSorters(theSource);
		for (Iterator<CommonSorterDescriptor> sortersItr = sorters.iterator(); sortersItr.hasNext();) {
			descriptor = sortersItr.next();
			if (descriptor.isEnabledForParent(aParent)) {
				applicableSorters.add(descriptor);
			}
		}
		if (applicableSorters.size() == 0) {
			return NO_SORTER_DESCRIPTORS;
		}
		return applicableSorters
				.toArray(new CommonSorterDescriptor[applicableSorters.size()]);
	}
	

	/**
	 * 
	 * @param theSource
	 *            The source of each *value. 
	 * @return The set of filters that are 'visible' to the given viewer
	 *         descriptor.
	 */
	public CommonSorterDescriptor[] findApplicableSorters(INavigatorContentDescriptor theSource) {
  
		Set<CommonSorterDescriptor> sorters = getCommonSorters(theSource); 
		if (sorters.size() == 0) {
			return NO_SORTER_DESCRIPTORS;
		}
		return sorters
				.toArray(new CommonSorterDescriptor[sorters.size()]);
	}

	private class CommonSorterDescriptorRegistry extends
			NavigatorContentRegistryReader {

		private CommonSorterDescriptorRegistry() {
		}

		@Override
		protected boolean readElement(IConfigurationElement element) {

			if (TAG_NAVIGATOR_CONTENT.equals(element.getName())) {
				String id = element.getAttribute(ATT_ID);
				if (id != null) {
					NavigatorContentDescriptor contentDescriptor = CONTENT_DESCRIPTOR_MANAGER
							.getContentDescriptor(id);
					if (contentDescriptor != null) {
						IConfigurationElement[] children = element
								.getChildren(TAG_COMMON_SORTER);

						if (children.length > 0) {
							Set<CommonSorterDescriptor> localSorters = getCommonSorters(contentDescriptor);
							for (int i = 0; i < children.length; i++) {
								localSorters.add(new CommonSorterDescriptor(
										children[i]));
							}
							return true;
						}
					} else {
						NavigatorPlugin
								.logError(
										0,
										NLS
												.bind(
														CommonNavigatorMessages.CommonSorterDescriptorManager_A_navigatorContent_extension_does_n_,
														new Object[] {
																id,
																element
																		.getDeclaringExtension()
																		.getNamespaceIdentifier() }),
										null);
					}
				} else {
					NavigatorPlugin
							.logError(
									0,
									NLS
											.bind(
													CommonNavigatorMessages.CommonSorterDescriptorManager_A_navigatorContent_extension_in_0_,
													element.getNamespaceIdentifier()),
									null);
				}
			}
			return super.readElement(element);
		}

	}

	private Set<CommonSorterDescriptor> getCommonSorters(INavigatorContentDescriptor contentDescriptor) {
		Set<CommonSorterDescriptor> descriptors = null;
		synchronized (sortersMap) {
			descriptors = sortersMap.get(contentDescriptor);
			if (descriptors == null) {
				sortersMap.put(contentDescriptor, descriptors = new HashSet<CommonSorterDescriptor>());
			}
		}
		return descriptors;
	}

}
