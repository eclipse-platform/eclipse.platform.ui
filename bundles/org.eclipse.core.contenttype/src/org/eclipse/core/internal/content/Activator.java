/*******************************************************************************
 * Copyright (c) 2005, 2015 IBM Corporation and others.
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
 *******************************************************************************/
package org.eclipse.core.internal.content;

import java.util.Hashtable;
import javax.xml.parsers.SAXParserFactory;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.content.IContentTypeManager;
import org.eclipse.osgi.service.debug.DebugOptions;
import org.osgi.framework.*;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

/**
 * The bundle activator for the runtime content manager plug-in.
 */
public class Activator implements BundleActivator, ServiceTrackerCustomizer<IExtensionRegistry, IExtensionRegistry> {

	private static Activator singleton;
	private static BundleContext bundleContext;
	private ServiceRegistration<IContentTypeManager> contentManagerService;
	private ServiceTracker<SAXParserFactory, Object> parserTracker;
	private ServiceTracker<DebugOptions, Object> debugTracker;
	private ServiceTracker<IExtensionRegistry, IExtensionRegistry> registryTracker;

	/**
	 * Return this activator's singleton instance or null if it has not been started.
	 */
	public static Activator getDefault() {
		return singleton;
	}

	@Override
	public void start(BundleContext context) throws Exception {
		bundleContext = context;
		singleton = this;
		contentManagerService = bundleContext.registerService(IContentTypeManager.class, ContentTypeManager.getInstance(), new Hashtable<String, Object>());
		registryTracker = new ServiceTracker<>(context, IExtensionRegistry.class, this);
		registryTracker.open();
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		if (contentManagerService != null) {
			contentManagerService.unregister();
			contentManagerService = null;
		}
		if (parserTracker != null) {
			parserTracker.close();
			parserTracker = null;
		}
		if (debugTracker != null) {
			debugTracker.close();
			debugTracker = null;
		}
		if (registryTracker != null) {
			registryTracker.close();
			registryTracker = null;
		}
		ContentTypeManager.shutdown();
		bundleContext = null;
	}

	/**
	 * Return the registered SAX parser factory or null if one
	 * does not exist.
	 */
	public SAXParserFactory getFactory() {
		if (parserTracker == null) {
			parserTracker = new ServiceTracker<>(bundleContext, SAXParserFactory.class, null);
			parserTracker.open();
		}
		SAXParserFactory theFactory = (SAXParserFactory) parserTracker.getService();
		if (theFactory != null)
			theFactory.setNamespaceAware(true);
		return theFactory;
	}

	/**
	 * Return the boolean value in the debug options for the given key, or
	 * return the default value if there is none.
	 */
	public boolean getBooleanDebugOption(String option, boolean defaultValue) {
		if (debugTracker == null) {
			debugTracker = new ServiceTracker<>(bundleContext, DebugOptions.class, null);
			debugTracker.open();
		}
		DebugOptions options = (DebugOptions) debugTracker.getService();
		if (options != null) {
			String value = options.getOption(option);
			if (value != null)
				return "true".equalsIgnoreCase(value); //$NON-NLS-1$
		}
		return defaultValue;
	}

	@Override
	public IExtensionRegistry addingService(ServiceReference<IExtensionRegistry> reference) {
		IExtensionRegistry registry = bundleContext.getService(reference);
		// registry is available; add the change listener
		ContentTypeManager.addRegistryChangeListener(registry);
		return registry;
	}

	@Override
	public void modifiedService(ServiceReference<IExtensionRegistry> reference, IExtensionRegistry service) {
		// do nothing
	}

	@Override
	public void removedService(ServiceReference<IExtensionRegistry> reference, IExtensionRegistry service) {
		// registry is unavailable; remove the change listener
		ContentTypeManager.removeRegistryChangeListener(service);
		bundleContext.ungetService(reference);
	}
}
