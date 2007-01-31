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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.ui.IPluginContribution;
import org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants;
import org.eclipse.ui.statushandlers.AbstractStatusHandler;

/**
 * The status handler descriptor.
 * 
 * @since 3.3
 */
public class StatusHandlerDescriptor implements IPluginContribution {

	private AbstractStatusHandler cachedInstance;

	private final static String PREFIX = "prefix"; //$NON-NLS-1$

	private IConfigurationElement configElement;

	private String id;

	private String pluginId;

	private String prefix;

	/**
	 * @param configElement
	 */
	public StatusHandlerDescriptor(IConfigurationElement configElement) {
		super();
		this.configElement = configElement;
		id = configElement.getAttribute(IWorkbenchRegistryConstants.ATT_ID);
		pluginId = configElement.getContributor().getName();
	}

	/**
	 * Gets an instance of the status handler defined in the descriptor.
	 * 
	 * @return the status handler
	 * @throws CoreException
	 *             thrown if there is a problem creating the handler
	 */
	public synchronized AbstractStatusHandler getStatusHandler()
			throws CoreException {
		if (cachedInstance == null) {
			AbstractStatusHandler statusHandler = (AbstractStatusHandler) configElement
					.createExecutableExtension(IWorkbenchRegistryConstants.ATT_CLASS);
			statusHandler.setId(configElement
					.getAttribute(IWorkbenchRegistryConstants.ATT_ID));

			IConfigurationElement parameters[] = configElement
					.getChildren(IWorkbenchRegistryConstants.TAG_PARAMETER);

			Map params = new HashMap();

			for (int i = 0; i < parameters.length; i++) {
				params
						.put(
								parameters[i]
										.getAttribute(IWorkbenchRegistryConstants.ATT_NAME),
								parameters[i]
										.getAttribute(IWorkbenchRegistryConstants.ATT_VALUE));
			}

			statusHandler.setParams(params);
			cachedInstance = statusHandler;
		}
		return cachedInstance;
	}

	/**
	 * Gets prefix parameter for the status handler defined in the descriptor.
	 * 
	 * @return prefix parameter
	 */
	public String getPrefix() {
		IConfigurationElement parameters[] = configElement
				.getChildren(IWorkbenchRegistryConstants.TAG_PARAMETER);

		for (int i = 0; i < parameters.length; i++) {
			if (parameters[i]
					.getAttribute(IWorkbenchRegistryConstants.ATT_NAME).equals(
							PREFIX)) {
				prefix = parameters[i]
						.getAttribute(IWorkbenchRegistryConstants.ATT_VALUE);
			}
		}
		return prefix;
	}

	/**
	 * Returns the id of the status handler.
	 * 
	 * @return the id
	 */
	public String getId() {
		return id;
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
}
