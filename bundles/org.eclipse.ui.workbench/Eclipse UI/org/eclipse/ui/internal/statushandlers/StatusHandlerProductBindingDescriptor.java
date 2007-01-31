/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
class StatusHandlerProductBindingDescriptor implements
		IPluginContribution {
	
	/**
	 * Handler id attribute. Value <code>handlerId</code>.
	 */
	private static String ATT_HANDLER_ID = "handlerId"; //$NON-NLS-1$

	private String id;

	private String pluginId;

	private String productId;

	private String handlerId;

	/**
	 * @param configElement
	 */
	public StatusHandlerProductBindingDescriptor(
			IConfigurationElement configElement) {
		super();
		id = configElement.getAttribute(IWorkbenchRegistryConstants.ATT_ID);
		pluginId = configElement.getContributor().getName();
		productId = configElement.getAttribute(IWorkbenchRegistryConstants.ATT_PRODUCTID);
		handlerId = configElement.getAttribute(ATT_HANDLER_ID);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IPluginContribution#getLocalId()
	 */
	public String getLocalId() {
		return id;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IPluginContribution#getPluginId()
	 */
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
