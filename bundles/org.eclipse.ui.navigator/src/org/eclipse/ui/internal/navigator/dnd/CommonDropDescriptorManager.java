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

package org.eclipse.ui.internal.navigator.dnd;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.ui.internal.navigator.extensions.INavigatorContentExtPtConstants;
import org.eclipse.ui.internal.navigator.extensions.NavigatorContentRegistryReader;
import org.eclipse.ui.navigator.INavigatorContentDescriptor;

/**
 * @since 3.2
 * 
 */
public class CommonDropDescriptorManager {

	private static final CommonDropDescriptorManager INSTANCE = new CommonDropDescriptorManager();

	/**
	 * A map of (INavigatorContentDescriptor,
	 * CommonDropAdapterDescriptor)-pairs.
	 */
	private final Map dropDescriptors = new HashMap();

	/**
	 * 
	 * @return An initialized singleton instance of the
	 *         CommonDropDescriptorManager.
	 */
	public CommonDropDescriptorManager getInstance() {
		return INSTANCE;
	}

	private CommonDropDescriptorManager() {
		new CommonDropAdapterRegistry().readRegistry();
	}
	
	// TODO Add fetch Drop Adapters by match method.

	/**
	 * @param aContentDescriptor
	 *            A non-null content descriptor. 
	 * @param aDropDescriptor
	 *            A non-null drop descriptor.
	 */
	private void addCommonDropAdapter(
			INavigatorContentDescriptor aContentDescriptor,
			CommonDropAdapterDescriptor aDropDescriptor) {
		dropDescriptors.put(aContentDescriptor, aDropDescriptor);
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

						IConfigurationElement[] commonDropAdapter = element
								.getChildren(TAG_COMMON_DROP_ADAPTER);

						if (commonDropAdapter.length == 1) {
							addCommonDropAdapter(contentDescriptor,
									new CommonDropAdapterDescriptor(element));
						}
					}
				}

			}
			return super.readElement(element);
		}

	}

}
