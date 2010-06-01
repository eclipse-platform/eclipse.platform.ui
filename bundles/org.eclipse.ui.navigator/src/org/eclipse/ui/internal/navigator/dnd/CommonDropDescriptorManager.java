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

package org.eclipse.ui.internal.navigator.dnd;

import java.util.ArrayList;
import java.util.Iterator;
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
 * 
 */
public class CommonDropDescriptorManager {

	private static final CommonDropDescriptorManager INSTANCE = new CommonDropDescriptorManager();

	private static final CommonDropAdapterDescriptor[] NO_DESCRIPTORS = new CommonDropAdapterDescriptor[0];

	/**
	 * A map of (INavigatorContentDescriptor,
	 * CommonDropAdapterDescriptor)-pairs.
	 */
	private final Map dropDescriptors = new TreeMap(ExtensionSequenceNumberComparator.INSTANCE);

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

		Set foundDescriptors = new LinkedHashSet();
		for (Iterator iter = dropDescriptors.keySet().iterator(); iter
				.hasNext();) {
			INavigatorContentDescriptor contentDescriptor = (INavigatorContentDescriptor) iter
					.next();
			if (aContentService.isVisible(contentDescriptor.getId())
					&& aContentService.isActive(contentDescriptor.getId())) {
				List dropDescriptors = getDropDescriptors(contentDescriptor);
				for (Iterator iterator = dropDescriptors.iterator(); iterator
						.hasNext();) {
					CommonDropAdapterDescriptor dropDescriptor = (CommonDropAdapterDescriptor) iterator
							.next();
					if (dropDescriptor.isDropElementSupported(aDropTarget)) {
						foundDescriptors.add(dropDescriptor);
					}
				}
			}
		}

		if (foundDescriptors.isEmpty()) {
			return NO_DESCRIPTORS;
		}
		return (CommonDropAdapterDescriptor[]) foundDescriptors
				.toArray(new CommonDropAdapterDescriptor[foundDescriptors
						.size()]);
	}

	private List getDropDescriptors(
			INavigatorContentDescriptor aContentDescriptor) {
		List descriptors = (List) dropDescriptors.get(aContentDescriptor);
		if (descriptors != null) {
			return descriptors;
		}
		synchronized (dropDescriptors) {
			descriptors = (List) dropDescriptors.get(aContentDescriptor);
			if (descriptors == null) {
				dropDescriptors.put(aContentDescriptor,
						(descriptors = new ArrayList()));
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

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ui.internal.navigator.extensions.RegistryReader#readElement(org.eclipse.core.runtime.IConfigurationElement)
		 */
		protected boolean readElement(IConfigurationElement element) {

			if (TAG_NAVIGATOR_CONTENT.equals(element.getName())) {

				String id = element.getAttribute(ATT_ID);
				if (id != null) {
					INavigatorContentDescriptor contentDescriptor = CONTENT_DESCRIPTOR_MANAGER
							.getContentDescriptor(id);
					if (contentDescriptor != null) {

						IConfigurationElement[] commonDropAdapters = element
								.getChildren(TAG_COMMON_DROP_ADAPTER);

						for (int i = 0; i < commonDropAdapters.length; i++) {
							addCommonDropAdapter(contentDescriptor,
									new CommonDropAdapterDescriptor(commonDropAdapters[i], contentDescriptor));
						} 
					}
				}

			}
			return super.readElement(element);
		}

	}

}
