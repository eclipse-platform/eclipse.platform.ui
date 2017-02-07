/*******************************************************************************
 * Copyright (c) 2007, 2017 IBM Corporation and others.
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.dynamichelpers.ExtensionTracker;
import org.eclipse.core.runtime.dynamichelpers.IExtensionChangeHandler;
import org.eclipse.core.runtime.dynamichelpers.IExtensionTracker;
import org.eclipse.ui.AbstractSourceProvider;
import org.eclipse.ui.ISources;
import org.eclipse.ui.PlatformUI;
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
public class WorkbenchServiceRegistry implements IExtensionChangeHandler {
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

	private WorkbenchServiceRegistry() {
		PlatformUI.getWorkbench().getExtensionTracker().registerHandler(
				this,
				ExtensionTracker
						.createExtensionPointFilter(getExtensionPoint()));
	}

	/**
	 * Used as the global service locator's parent.
	 */
	public static final IServiceLocator GLOBAL_PARENT = new IServiceLocator() {
		@Override
		public <T> T getService(Class<T> api) {
			return null;
		}

		@Override
		public boolean hasService(Class<?> api) {
			return false;
		}
	};

	private Map factories = new HashMap();

	static class ServiceFactoryHandle {
		AbstractServiceFactory factory;
		WeakHashMap serviceLocators = new WeakHashMap();
		String[] serviceNames;
		ServiceFactoryHandle(AbstractServiceFactory factory) {
			this.factory = factory;
		}
	}

	public Object getService(Class key, IServiceLocator parentLocator,
			ServiceLocator locator) {
		ServiceFactoryHandle handle = (ServiceFactoryHandle) factories.get(key.getName());
		if (handle == null) {
			handle = loadFromRegistry(key);
		}
		if (handle != null) {
			Object result = handle.factory.create(key, parentLocator, locator);
			if (result != null) {
				handle.serviceLocators.put(locator, new Object());
				return result;
			}
		}
		return null;
	}

	private ServiceFactoryHandle loadFromRegistry(Class key) {
		ServiceFactoryHandle result = null;
		IConfigurationElement[] serviceFactories = getExtensionPoint()
				.getConfigurationElements();
		try {
			final String requestedName = key.getName();
			boolean done = false;
			for (int i = 0; i < serviceFactories.length && !done; i++) {
				final IConfigurationElement[] serviceNameElements = serviceFactories[i]
						.getChildren(IWorkbenchRegistryConstants.TAG_SERVICE);
				for (int j = 0; j < serviceNameElements.length && !done; j++) {
					String serviceName = serviceNameElements[j]
							.getAttribute(IWorkbenchRegistryConstants.ATTR_SERVICE_CLASS);
					if (requestedName.equals(serviceName)) {
						done = true;
					}
				}
				if (done) {
					final AbstractServiceFactory f = (AbstractServiceFactory) serviceFactories[i]
							.createExecutableExtension(IWorkbenchRegistryConstants.ATTR_FACTORY_CLASS);
					ServiceFactoryHandle handle = new ServiceFactoryHandle(f);
			    	PlatformUI.getWorkbench().getExtensionTracker().registerObject(
			    			serviceFactories[i].getDeclaringExtension(),
							handle, IExtensionTracker.REF_WEAK);

			    	List serviceNames = new ArrayList();
					for (IConfigurationElement configElement : serviceNameElements) {
						String serviceName = configElement.getAttribute(IWorkbenchRegistryConstants.ATTR_SERVICE_CLASS);
						if (factories.containsKey(serviceName)) {
							WorkbenchPlugin.log("Factory already exists for " //$NON-NLS-1$
									+ serviceName);
						} else {
							factories.put(serviceName, handle);
							serviceNames.add(serviceName);
						}
					}
					handle.serviceNames = (String[]) serviceNames.toArray(new String[serviceNames
							.size()]);
					result = handle;
				}
			}
		} catch (CoreException e) {
			StatusManager.getManager().handle(e.getStatus());
		}
		return result;
	}

	private IExtensionPoint getExtensionPoint() {
		IExtensionRegistry reg = Platform.getExtensionRegistry();
		IExtensionPoint ep = reg.getExtensionPoint(EXT_ID_SERVICES);
		return ep;
	}

	public AbstractSourceProvider[] getSourceProviders() {
		ArrayList providers = new ArrayList();
		IExtensionPoint ep = getExtensionPoint();
		for (IConfigurationElement configElement : ep.getConfigurationElements()) {
			if (configElement.getName().equals(
					IWorkbenchRegistryConstants.TAG_SOURCE_PROVIDER)) {
				try {
					Object sourceProvider = configElement
							.createExecutableExtension(IWorkbenchRegistryConstants.ATTR_PROVIDER);
					if (!(sourceProvider instanceof AbstractSourceProvider)) {
						String attributeName = configElement.getAttribute(IWorkbenchRegistryConstants.ATTR_PROVIDER);
						final String message = "Source Provider '" + //$NON-NLS-1$
								attributeName
								+ "' should extend AbstractSourceProvider"; //$NON-NLS-1$
						final IStatus status = new Status(IStatus.ERROR, WorkbenchPlugin.PI_WORKBENCH, message);
						WorkbenchPlugin.log(status);
						continue;
					}
					providers.add(sourceProvider);
					processVariables(configElement.getChildren(IWorkbenchRegistryConstants.TAG_VARIABLE));
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
		for (IConfigurationElement configElement : children) {
			String name = configElement.getAttribute(IWorkbenchRegistryConstants.ATT_NAME);
			if (name == null || name.length() == 0) {
				continue;
			}
			String level = configElement.getAttribute(IWorkbenchRegistryConstants.ATT_PRIORITY_LEVEL);
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

	@Override
	public void addExtension(IExtensionTracker tracker, IExtension extension) {
		// we don't need to react to adds because we are not caching the extensions we find -
		// next time a service is requested, we will look at all extensions again in
		// loadFromRegistry
	}

	@Override
	public void removeExtension(IExtension extension, Object[] objects) {
		for (Object object : objects) {
			if (object instanceof ServiceFactoryHandle) {
				ServiceFactoryHandle handle = (ServiceFactoryHandle) object;
				Set locatorSet = handle.serviceLocators.keySet();
				ServiceLocator[] locators = (ServiceLocator[]) locatorSet.toArray(new ServiceLocator[locatorSet.size()]);
				Arrays.sort(locators, (loc1, loc2) -> {
					int l1 = loc1.getService(IWorkbenchLocationService.class).getServiceLevel();
					int l2 = loc2.getService(IWorkbenchLocationService.class).getServiceLevel();
					return l1 < l2 ? -1 : (l1 > l2 ? 1 : 0);
				});
				for (ServiceLocator locator : locators) {
					ServiceLocator serviceLocator = locator;
					if (!serviceLocator.isDisposed()) {
						serviceLocator.unregisterServices(handle.serviceNames);
					}
				}
				handle.factory = null;
				for (String serviceName : handle.serviceNames) {
					if (factories.get(serviceName) == handle) {
						factories.remove(serviceName);
					}
				}
			}
		}
	}
}
