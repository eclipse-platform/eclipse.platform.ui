/*******************************************************************************
 * Copyright (c) 2007, 2009 IBM Corporation and others.
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
import java.util.Comparator;
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
		public Object getService(Class api) {
			return null;
		}

		public boolean hasService(Class api) {
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
					for (int j = 0; j < serviceNameElements.length; j++) {
						String serviceName = serviceNameElements[j].getAttribute(IWorkbenchRegistryConstants.ATTR_SERVICE_CLASS);
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
		IConfigurationElement[] elements = ep.getConfigurationElements();
		for (int i = 0; i < elements.length; i++) {
			if (elements[i].getName().equals(
					IWorkbenchRegistryConstants.TAG_SOURCE_PROVIDER)) {
				try {
					Object sourceProvider = elements[i]
							.createExecutableExtension(IWorkbenchRegistryConstants.ATTR_PROVIDER);
					if (!(sourceProvider instanceof AbstractSourceProvider)) {
						String attributeName = elements[i]
								.getAttribute(IWorkbenchRegistryConstants.ATTR_PROVIDER);
						final String message = "Source Provider '" + //$NON-NLS-1$
								attributeName
								+ "' should extend AbstractSourceProvider"; //$NON-NLS-1$
						final IStatus status = new Status(IStatus.ERROR,
								WorkbenchPlugin.PI_WORKBENCH, message);
						WorkbenchPlugin.log(status);
						continue;
					}
					providers.add(sourceProvider);
				} catch (CoreException e) {
					StatusManager.getManager().handle(e.getStatus());
				}
			}
		}
		return (AbstractSourceProvider[]) providers
				.toArray(new AbstractSourceProvider[providers.size()]);
	}

	public void initializeSourcePriorities() {
		IExtensionPoint ep = getExtensionPoint();
		IConfigurationElement[] elements = ep.getConfigurationElements();
		for (int i = 0; i < elements.length; i++) {
			if (elements[i].getName().equals(IWorkbenchRegistryConstants.TAG_SOURCE_PROVIDER)) {
				processVariables(elements[i].getChildren(IWorkbenchRegistryConstants.TAG_VARIABLE));
			}
		}
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

	public void addExtension(IExtensionTracker tracker, IExtension extension) {
		// we don't need to react to adds because we are not caching the extensions we find -
		// next time a service is requested, we will look at all extensions again in
		// loadFromRegistry
	}

	public void removeExtension(IExtension extension, Object[] objects) {
		for (int i = 0; i < objects.length; i++) {
			Object object = objects[i];
			if (object instanceof ServiceFactoryHandle) {
				ServiceFactoryHandle handle = (ServiceFactoryHandle) object;
				Set locatorSet = handle.serviceLocators.keySet();
				ServiceLocator[] locators = (ServiceLocator[]) locatorSet.toArray(new ServiceLocator[locatorSet.size()]);
				Arrays.sort(locators, new Comparator(){
					public int compare(Object o1, Object o2) {
						ServiceLocator loc1 = (ServiceLocator) o1;
						ServiceLocator loc2 = (ServiceLocator) o2;
						int l1 = ((IWorkbenchLocationService) loc1
								.getService(IWorkbenchLocationService.class))
								.getServiceLevel();						
						int l2 = ((IWorkbenchLocationService) loc2
								.getService(IWorkbenchLocationService.class))
								.getServiceLevel();						
						return l1 < l2 ? -1 : (l1 > l2 ? 1 : 0);
					}
				});
				for (int j = 0; j < locators.length; j++) {
					ServiceLocator serviceLocator = locators[j];
					if (!serviceLocator.isDisposed()) {
						serviceLocator.unregisterServices(handle.serviceNames);
					}
				}
				handle.factory = null;
				for (int j = 0; j < handle.serviceNames.length; j++) {
					String serviceName = handle.serviceNames[j];
					if (factories.get(serviceName) == handle) {
						factories.remove(serviceName);
					}
				}
			}
		}
	}
}
