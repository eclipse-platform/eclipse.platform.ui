/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.runtime;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Hashtable;
import org.eclipse.core.internal.boot.PlatformURLBaseConnection;
import org.eclipse.core.internal.boot.PlatformURLHandler;
import org.eclipse.core.internal.registry.*;
import org.eclipse.core.runtime.*;
import org.eclipse.osgi.service.environment.EnvironmentInfo;
import org.osgi.framework.*;
import org.osgi.service.url.URLConstants;
import org.osgi.service.url.URLStreamHandlerService;

/**
 * Activator for the Eclipse runtime.
 */
public class PlatformActivator implements BundleActivator, ServiceListener {

	private static BundleContext context;
	private EclipseBundleListener pluginBundleListener;
	private ExtensionRegistry registry;
	private ServiceReference environmentServiceReference;
	private ServiceRegistration converterRegistration;
	
	private static File cacheFile = InternalPlatform.getDefault().getConfigurationMetadataLocation().append(".registry").toFile();

	public static BundleContext getContext() {
		return context;
	}

	public void start(BundleContext context) throws Exception {
		PlatformActivator.context = context;
		context.addServiceListener(this);
		tryToAcquireInfoService();
		installPlatformURLSupport();
	}

	private void installBackwardCompatibleURLSupport() {
		try {
			Class handler = Class.forName("org.eclipse.core.internal.runtime.PlatformURLPluginHandlerFactory");
			Method startupMethod = handler.getDeclaredMethod("startup", null);
			startupMethod.invoke(handler, null);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return; //TODO log a warning
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Register the platform URL support as a service to the URLHandler service
	 */
	private void installPlatformURLSupport() {
		PlatformURLPluginConnection.startup();
		PlatformURLFragmentConnection.startup();

		PlatformURLBaseConnection.startup(InternalPlatform.getDefault().getInstallURL());

		Hashtable properties = new Hashtable();
		properties.put(URLConstants.URL_HANDLER_PROTOCOL, new String[] { PlatformURLHandler.PROTOCOL });
		context.registerService(URLStreamHandlerService.class.getName(), new PlatformURLHandler(), properties);
	}

	private void startRegistry(BundleContext context) {
		boolean fromCache = true;
		if (InternalPlatform.cacheRegistry) {
			// Try to read the registry from the cache first. If that fails, create a new registry
			MultiStatus problems = new MultiStatus(IPlatform.PI_RUNTIME, ExtensionsParser.PARSE_PROBLEM, "Registry cache problems", null); //$NON-NLS-1$
			Factory factory = new Factory(problems);

			long start = 0;
			if (InternalPlatform.DEBUG)
				start = System.currentTimeMillis();
			registry = new RegistryCacheReader(cacheFile, factory, InternalPlatform.lazyRegistryCacheLoading).loadCache();

			if (InternalPlatform.DEBUG && registry != null)
				System.out.println("Reading registry cache: " + (System.currentTimeMillis() - start));

			if (InternalPlatform.DEBUG_REGISTRY) {
				if (registry == null)
					System.out.println("Reloading registry from manifest files...");
				else
					System.out.println("Using registry cache " + (InternalPlatform.lazyRegistryCacheLoading ? "with" : "without") + " lazy element loading...");
			}
			// TODO log any problems that occurred in loading the cache.
			if (!problems.isOK())
				System.out.println(problems);
		}
		if (registry == null) {
			fromCache = false;
			registry = new ExtensionRegistry(new ExtensionLinker());
		}

		// register a listener to catch new bundle installations/resolutions.
		pluginBundleListener = new EclipseBundleListener(registry);
		context.addBundleListener(pluginBundleListener);

		// populate the registry with all the currently installed bundles.
		if (!fromCache)
			pluginBundleListener.processBundles(context.getBundles());

		context.registerService(IExtensionRegistry.class.getName(), registry, new Hashtable()); //$NON-NLS-1$
		InternalPlatform.getDefault().setExtensionRegistry(registry);
	}
	public void stop(BundleContext context) throws Exception {
		// Stop the registry
		stopRegistry(context);
		unregisterPluginConverter();
		environmentInfoServiceReleased(environmentServiceReference);
		// Stop the platform orderly.		
		InternalPlatform.getDefault().stop(context);
	}


	private void stopRegistry(BundleContext context) {
		context.removeBundleListener(this.pluginBundleListener);
		if (registry != null && registry.isDirty()) {
			new RegistryCacheWriter(cacheFile).saveCache(registry);
			registry = null;
		}
	}

	private void tryToAcquireInfoService() {
		ServiceReference reference = context.getServiceReference(EnvironmentInfo.class.getName());
		if (reference == null)
			return;
		environmentInfoServiceAquired(reference);
	}

	private void environmentInfoServiceAquired(ServiceReference reference) {
		if (environmentServiceReference != null)
			return;
		environmentServiceReference = reference;
		EnvironmentInfo infoService = (EnvironmentInfo) context.getService(environmentServiceReference);
		InternalPlatform.infoService = infoService;
		toStart();
	}

	private void toStart() {
		try {
			InternalPlatform.getDefault().start(context);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		registerPluginConverter();
		startRegistry(context);
		
	}

	private void registerPluginConverter() {
		converterRegistration = context.registerService(IPluginConverter.class.getName(), new PluginConverter(), null);
	}
	
	private void unregisterPluginConverter() {
		converterRegistration.unregister();
	}

	private void environmentInfoServiceReleased(ServiceReference reference) {
		if (environmentServiceReference == null)
			return;
		if (environmentServiceReference != reference)
			return;

		InternalPlatform.infoService = null;
		context.ungetService(environmentServiceReference);
		environmentServiceReference = null;
	}

	public void serviceChanged(ServiceEvent event) {
		int type = event.getType();
		ServiceReference reference = event.getServiceReference();
		switch (type) {
			case ServiceEvent.REGISTERED :
				String[] servicesInterfaces = (String[]) reference.getProperty(Constants.OBJECTCLASS);
				for (int i = 0; i < servicesInterfaces.length; i++) {
					if (servicesInterfaces[i].equals(EnvironmentInfo.class.getName()))
						environmentInfoServiceAquired(reference);
				}
				break;
			case ServiceEvent.UNREGISTERING :
				servicesInterfaces = (String[]) reference.getProperty(Constants.OBJECTCLASS);
				for (int i = 0; i < servicesInterfaces.length; i++) {
					if (servicesInterfaces[i].equals(EnvironmentInfo.class.getName()))
						environmentInfoServiceReleased(reference);
				}
				break;
		}
	}
}
