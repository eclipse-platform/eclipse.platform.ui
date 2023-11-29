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

package org.eclipse.ui.internal.navigator.dnd;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.ui.internal.navigator.extensions.ExtensionSequenceNumberComparator;
import org.eclipse.ui.internal.navigator.extensions.INavigatorContentExtPtConstants;
import org.eclipse.ui.internal.navigator.extensions.NavigatorContentRegistryReader;
import org.eclipse.ui.navigator.INavigatorContentDescriptor;
import org.eclipse.ui.navigator.INavigatorContentService;

/**
 * @since 3.2
 */
public class CommonDropDescriptorManager {

	private static final CommonDropDescriptorManager INSTANCE = new CommonDropDescriptorManager();

	private static final CommonDropAdapterDescriptor[] NO_DESCRIPTORS = new CommonDropAdapterDescriptor[0];

	/**
	 * A map of (INavigatorContentDescriptor,
	 * CommonDropAdapterDescriptor)-pairs.
	 */
	private final Map<INavigatorContentDescriptor, List> dropDescriptors = new TreeMap<>(ExtensionSequenceNumberComparator.INSTANCE);

	/**
	 *
	 * @return An initialized singleton instance of the
	 *         CommonDropDescriptorManager.
	 */
	public static CommonDropDescriptorManager getInstance() {
		return INSTANCE;
	}

	private CommonDropDescriptorManager() {
		new CommonDropAdapterRegistry().readRegistry();
	}

	/**
	 *
	 * @param aDropTarget
	 *            The drop target of the operation
	 * @param aContentService
	 *            The associated content service to filter results by.
	 * @return An array of drop descriptors that can handle the given drop
	 *         target and are <i>visible</i> and <i>active</i> for the given
	 *         service and <i>enabled</i> for the given drop target..
	 */
	public CommonDropAdapterDescriptor[] findCommonDropAdapterAssistants(Object aDropTarget, INavigatorContentService aContentService) {

		Set<CommonDropAdapterDescriptor> foundDescriptors = new LinkedHashSet<>();
		for (INavigatorContentDescriptor contentDescriptor : dropDescriptors.keySet()) {
			if (aContentService.isVisible(contentDescriptor.getId())
					&& aContentService.isActive(contentDescriptor.getId())) {
				List<CommonDropAdapterDescriptor> dropDescriptors = getDropDescriptors(contentDescriptor);
				for (CommonDropAdapterDescriptor dropDescriptor : dropDescriptors) {
					if (dropDescriptor.isDropElementSupported(aDropTarget)) {
						foundDescriptors.add(dropDescriptor);
					}
				}
			}
		}

		if (foundDescriptors.isEmpty()) {
			return NO_DESCRIPTORS;
		}
		return foundDescriptors
				.toArray(new CommonDropAdapterDescriptor[foundDescriptors
						.size()]);
	}

	private List<CommonDropAdapterDescriptor> getDropDescriptors(
			INavigatorContentDescriptor aContentDescriptor) {
		List<CommonDropAdapterDescriptor> descriptors = dropDescriptors.get(aContentDescriptor);
		if (descriptors != null) {
			return descriptors;
		}
		synchronized (dropDescriptors) {
			descriptors = dropDescriptors.get(aContentDescriptor);
			if (descriptors == null) {
				dropDescriptors.put(aContentDescriptor,
						(descriptors = new ArrayList<>()));
			}

		}
		return descriptors;
	}

	/**
	 * @param aContentDescriptor
	 *            A non-null content descriptor.
	 * @param aDropDescriptor
	 *            A non-null drop descriptor.
	 */
	private void addCommonDropAdapter(
			INavigatorContentDescriptor aContentDescriptor,
			CommonDropAdapterDescriptor aDropDescriptor) {
		getDropDescriptors(aContentDescriptor).add(aDropDescriptor);
	}

	private class CommonDropAdapterRegistry extends
			NavigatorContentRegistryReader implements
			INavigatorContentExtPtConstants {

		private CommonDropAdapterRegistry() {
		}

		@Override
		protected boolean readElement(IConfigurationElement element) {

			if (TAG_NAVIGATOR_CONTENT.equals(element.getName())) {

				String id = element.getAttribute(ATT_ID);
				if (id != null) {
					INavigatorContentDescriptor contentDescriptor = CONTENT_DESCRIPTOR_MANAGER
							.getContentDescriptor(id);
					if (contentDescriptor != null) {

						IConfigurationElement[] commonDropAdapters = element
								.getChildren(TAG_COMMON_DROP_ADAPTER);

						for (IConfigurationElement commonDropAdapter : commonDropAdapters) {
							addCommonDropAdapter(contentDescriptor,
									new CommonDropAdapterDescriptor(commonDropAdapter, contentDescriptor));
						}
					}
				}

			}
			return super.readElement(element);
		}

	}

}
