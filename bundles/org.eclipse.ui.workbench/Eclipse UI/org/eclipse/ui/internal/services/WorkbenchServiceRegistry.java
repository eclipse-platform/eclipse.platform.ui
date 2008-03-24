/*******************************************************************************
 * Copyright (c) 2007, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.AbstractSourceProvider;
import org.eclipse.ui.ISources;
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
	/**
	 * 
	 */
	private static final String WORKBENCH_LEVEL = "workbench"; //$NON-NLS-1$

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

	public AbstractSourceProvider[] getSourceProviders() {
		ArrayList providers = new ArrayList();
		IExtensionRegistry reg = Platform.getExtensionRegistry();
		IExtensionPoint ep = reg.getExtensionPoint(EXT_ID_SERVICES);
		IConfigurationElement[] elements = ep.getConfigurationElements();
		for (int i = 0; i < elements.length; i++) {
			if (elements[i].getName().equals(
					IWorkbenchRegistryConstants.TAG_SOURCE_PROVIDER)) {
				try {
					providers
							.add(elements[i]
									.createExecutableExtension(IWorkbenchRegistryConstants.ATTR_PROVIDER));
					processVariables(elements[i]
							.getChildren(IWorkbenchRegistryConstants.TAG_VARIABLE));
				} catch (CoreException e) {
					StatusManager.getManager().handle(e.getStatus());
				}
			}
		}
		return (AbstractSourceProvider[]) providers
				.toArray(new AbstractSourceProvider[providers.size()]);
	}

	private static final String[] supportedLevels = { ISources.ACTIVE_CONTEXT_NAME,
			ISources.ACTIVE_SHELL_NAME, 
			ISources.ACTIVE_WORKBENCH_WINDOW_NAME, 
			ISources.ACTIVE_EDITOR_ID_NAME,
			ISources.ACTIVE_PART_ID_NAME, 
			ISources.ACTIVE_SITE_NAME
	};

	private void processVariables(IConfigurationElement[] children) {
		for (int i = 0; i < children.length; i++) {
			String name = children[i]
					.getAttribute(IWorkbenchRegistryConstants.ATT_NAME);
			if (name == null || name.length() == 0) {
				continue;
			}
			String level = children[i]
					.getAttribute(IWorkbenchRegistryConstants.ATT_PRIORITY_LEVEL);
			if (level == null || level.length() == 0) {
				level = WORKBENCH_LEVEL;
			} else {
				boolean found = false;
				for (int j = 0; j < supportedLevels.length && !found; j++) {
					if (supportedLevels[j].equals(level)) {
						found = true;
					}
				}
				if (!found) {
					level = WORKBENCH_LEVEL;
				}
			}
			int existingPriority = SourcePriorityNameMapping.getMapping(level);
			int newPriority = existingPriority << 1;
			SourcePriorityNameMapping.addMapping(name, newPriority);
		}
	}
}
