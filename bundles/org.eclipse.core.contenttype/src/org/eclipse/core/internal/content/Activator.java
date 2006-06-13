/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
public class Activator implements BundleActivator, ServiceTrackerCustomizer {

	private static Activator singleton;
	private static BundleContext bundleContext;
	private ServiceRegistration contentManagerService = null;
	private ServiceTracker parserTracker = null;
	private ServiceTracker debugTracker = null;
	private ServiceTracker registryTracker = null;

	/*
	 * Return this activator's singleton instance or null if it has not been started.
	 */
	public static Activator getDefault() {
		return singleton;
	}

	/* (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		bundleContext = context;
		singleton = this;
		// ContentTypeManager should be started first
		ContentTypeManager.startup();
		contentManagerService = bundleContext.registerService(IContentTypeManager.class.getName(), ContentTypeManager.getInstance(), new Hashtable());
		registryTracker = new ServiceTracker(context, IExtensionRegistry.class.getName(), this);
		registryTracker.open();
	}

	/* (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
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

	/*
	 * Return this plug-in's bundle context.
	 */
	static BundleContext getContext() {
		return bundleContext;
	}

	/*
	 * Return the registered SAX parser factory or null if one
	 * does not exist.
	 */
	public SAXParserFactory getFactory() {
		if (parserTracker == null) {
			parserTracker = new ServiceTracker(bundleContext, SAXParserFactory.class.getName(), null);
			parserTracker.open();
		}
		SAXParserFactory theFactory = (SAXParserFactory) parserTracker.getService();
		if (theFactory != null)
			theFactory.setNamespaceAware(true);
		return theFactory;
	}

	/*
	 * Return the boolean value in the debug options for the given key, or
	 * return the default value if there is none.
	 */
	public boolean getBooleanDebugOption(String option, boolean defaultValue) {
		if (debugTracker == null) {
			debugTracker = new ServiceTracker(bundleContext, DebugOptions.class.getName(), null);
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

	public Object addingService(ServiceReference reference) {
		IExtensionRegistry registry = (IExtensionRegistry) bundleContext.getService(reference);
		// registry is available; add the change listener
		ContentTypeManager.addRegistryChangeListener(registry);
		return registry;
	}

	public void modifiedService(ServiceReference reference, Object service) {
		// do nothing
	}

	public void removedService(ServiceReference reference, Object service) {
		// registry is unavailable; remove the change listener
		ContentTypeManager.removeRegistryChangeListener((IExtensionRegistry) service);
		bundleContext.ungetService(reference);
	}
}
