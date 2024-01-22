/*******************************************************************************
 * Copyright (c) 2007, 2015 IBM Corporation and others.
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

package org.eclipse.ui.internal.statushandlers;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.ui.IPluginContribution;
import org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants;

/**
 * The status handler product binding descriptor.
 *
 * @since 3.3
 */
class StatusHandlerProductBindingDescriptor implements IPluginContribution {

	/**
	 * Handler id attribute. Value <code>handlerId</code>.
	 */
	private static String ATT_HANDLER_ID = "handlerId"; //$NON-NLS-1$

	private String id;

	private String pluginId;

	private String productId;

	private String handlerId;

	public StatusHandlerProductBindingDescriptor(IConfigurationElement configElement) {
		super();
		id = configElement.getAttribute(IWorkbenchRegistryConstants.ATT_ID);
		pluginId = configElement.getContributor().getName();
		productId = configElement.getAttribute(IWorkbenchRegistryConstants.ATT_PRODUCTID);
		handlerId = configElement.getAttribute(ATT_HANDLER_ID);
	}

	@Override
	public String getLocalId() {
		return id;
	}

	@Override
	public String getPluginId() {
		return pluginId;
	}

	/**
	 * @return Returns the productId.
	 */
	public String getProductId() {
		return productId;
	}

	/**
	 * @return Returns the handlerId.
	 */
	public String getHandlerId() {
		return handlerId;
	}
}
