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

package org.eclipse.ui.internal.services;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants;
import org.eclipse.ui.services.AbstractServiceFactory;
import org.eclipse.ui.services.IServiceLocator;
import org.eclipse.ui.statushandlers.StatusManager;

/**
 * This class will create a service from the matching factory. If the factory
 * doesn't exist, it will try and load it from the registry.
 * 
 * @since 3.4
 */
public class WorkbenchServiceRegistry {
	private static final String EXT_ID_SERVICES = "org.eclipse.ui.services"; //$NON-NLS-1$

	private static WorkbenchServiceRegistry registry = null;

	public static WorkbenchServiceRegistry getRegistry() {
		if (registry == null) {
			registry = new WorkbenchServiceRegistry();
		}
		return registry;
	}

	/**
	 * Used as the global service locator's parent.
	 */
	public static final IServiceLocator GLOBAL_PARENT = new IServiceLocator() {
		public Object getService(Class api) {
			return null;
		}

		public boolean hasService(Class api) {
			return false;
		}
	};

	private Map factories = new HashMap();

	public Object getService(Class key, IServiceLocator parentLocator,
			IServiceLocator locator) {
		Object f = factories.get(key.getName());
		if (f instanceof AbstractServiceFactory) {
			AbstractServiceFactory factory = (AbstractServiceFactory) f;
			return factory.create(key, parentLocator, locator);
		}
		return loadFromRegistry(key, parentLocator, locator);
	}

	private Object loadFromRegistry(Class key, IServiceLocator parentLocator,
			IServiceLocator locator) {
		Object service = null;
		IExtensionRegistry reg = Platform.getExtensionRegistry();
		IExtensionPoint ep = reg.getExtensionPoint(EXT_ID_SERVICES);
		IConfigurationElement[] serviceFactories = ep
				.getConfigurationElements();
		try {
			final String requestedName = key.getName();
			boolean done = false;
			for (int i = 0; i < serviceFactories.length && !done; i++) {
				final IConfigurationElement[] serviceNames = serviceFactories[i]
						.getChildren(IWorkbenchRegistryConstants.TAG_SERVICE);
				for (int j = 0; j < serviceNames.length && !done; j++) {
					String serviceName = serviceNames[j]
							.getAttribute(IWorkbenchRegistryConstants.ATTR_SERVICE_CLASS);
					if (requestedName.equals(serviceName)) {
						done = true;
					}
				}
				if (done) {
					final AbstractServiceFactory f = (AbstractServiceFactory) serviceFactories[i]
							.createExecutableExtension(IWorkbenchRegistryConstants.ATTR_FACTORY_CLASS);
					for (int j = 0; j < serviceNames.length; j++) {
						String serviceName = serviceNames[j]
								.getAttribute(IWorkbenchRegistryConstants.ATTR_SERVICE_CLASS);
						if (factories.containsKey(serviceName)) {
							WorkbenchPlugin.log("Factory already exists for " //$NON-NLS-1$
									+ serviceName);
						} else {
							factories.put(serviceName, f);
						}
					}
					service = f.create(key, parentLocator, locator);
				}
			}
		} catch (CoreException e) {
			StatusManager.getManager().handle(e.getStatus());
		}
		return service;
	}
}
